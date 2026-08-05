/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.{Properties, UUID}

import com.webank.wedatasphere.dss.appconn.datagofeishu.client.DataGoFeishuClient
import com.webank.wedatasphere.dss.appconn.datagofeishu.conf.DataGoFeishuConfiguration
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.{NodeParams, SheetResult, TaskResponse}
import com.webank.wedatasphere.dss.appconn.datagofeishu.exception.DataGoFeishuException
import com.webank.wedatasphere.dss.appconn.datagofeishu.utils.DmInfoComparator
import com.webank.wedatasphere.dss.standard.app.development.listener.common._
import com.webank.wedatasphere.dss.standard.app.development.listener.core.{Killable, LongTermRefExecutionOperation, Procedure}
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef.ExecutionResponseRefBuilder
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.{AsyncExecutionResponseRef, ExecutionResponseRef, RefExecutionRequestRef}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.VariableUtils

import scala.collection.JavaConversions._

/**
 * DataGo飞书多维表格外发节点执行编排器（长周期异步执行）。
 * <p>
 * 三阶段状态机：INIT(参数解析+①表单校验) → DETECTING(②任务创建/轮询) → EXPORTING(③执行外发) → SUCCESS。
 * <ul>
 *   <li>submit：解析节点参数、调①表单获取并与 DM 单比对，初始化 action 进入 DETECTING</li>
 *   <li>state：DSS 周期调用，按当前阶段驱动 ②检测轮询 / ③执行外发，更新状态</li>
 *   <li>result/kill/progress/log：结果返回、终止、进度、日志</li>
 * </ul>
 * 任一阶段参数不一致、optype 非法、检测失败、外发失败、超时或接口异常均直接失败。
 */
class DataGoFeishuRefExecutionOperation
  extends LongTermRefExecutionOperation[RefExecutionRequestRef.RefExecutionRequestRefImpl]
    with Killable with Procedure {

  /** 不可变、线程安全，节点日志时间戳格式 */
  private val logTimestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * 提交节点执行：解析参数 → ① 表单获取 → DM 单一致性比对 → 进入 DETECTING 阶段。
   * <p>
   * 失败时调用 fail 置 Failed，action 仍返回（含错误响应）。
   */
  override def submit(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl): RefExecutionAction = {
    val action = new DataGoFeishuExecutionAction
    action.setId(UUID.randomUUID().toString)
    val properties = buildProperties(requestRef)
    try {
      val params = NodeParams.from(properties)
      val client = new DataGoFeishuClient(properties)
      // ① 表单获取：按 dmId 查库表/字段/分区/用户/optype/状态
      val form = client.queryExportForm(params.getDmId)
      val supportedOptype = properties.getProperty(
        DataGoFeishuConfiguration.SUPPORTED_OPTYPE, DataGoFeishuConfiguration.DEFAULT_SUPPORTED_OPTYPE)
      // 审批信息校验（optype/库表/字段/分区/通知人），不一致抛 82003
      DmInfoComparator.validate(params, form, supportedOptype)

      action.client = client
      action.nodeParams = params
      // 轮询/重试/超时参数：节点级 UI 值 > 实例级配置 > 硬编码默认（见 positiveLong/positiveInt）
      // detectPollInterval/executeRetryInterval/maxWaitTime 配置单位为秒，×1000 转毫秒后存入 action
      action.detectPollInterval = positiveLong(properties, "detectPollInterval",
        DataGoFeishuConfiguration.DETECT_INTERVAL, 30L) * 1000L
      action.executeRetryMax = positiveInt(properties, "executeRetryMax",
        DataGoFeishuConfiguration.EXECUTE_RETRY_MAX, 3)
      // executeRetryInterval 不暴露为节点 UI，nodeKey 传空串，仅走实例级配置（秒，×1000）
      action.executeRetryInterval = positiveLong(properties, "",
        DataGoFeishuConfiguration.EXECUTE_RETRY_INTERVAL, 30L) * 1000L
      action.maxWaitTime = positiveLong(properties, "maxWaitTime",
        DataGoFeishuConfiguration.MAX_WAIT_TIME, 7200L) * 1000L
      action.startedAt = System.currentTimeMillis()
      action.stage = DataGoFeishuStage.Detecting
      action.setState(RefExecutionState.Running)
      appendLog(action, "DM审批信息校验通过（optype=table），开始敏感数据检测")
      val targetCount = params.getDataTargets.size()
      val fieldCount = params.getDataTargets.map(_.getFields.size()).sum
      logger.info("DataGo Feishu task initialized, dmId={}, targetCount={}, fieldCount={}, notifyUserCount={}",
        params.getDmId, Int.box(targetCount), Int.box(fieldCount), Int.box(params.getNotifyUsers.size()))
    } catch {
      case t: Throwable =>
        fail(action, "DataGo飞书多维表格外发节点初始化失败: " + safeMessage(t), t)
    }
    action
  }

  /**
   * DSS 周期调用，按当前阶段驱动执行并返回状态。
   * <p>
   * 先判终态/超时/轮询节流，再按 stage 分发到 handleDetect/handleExport。
   */
  override def state(refAction: RefExecutionAction): RefExecutionState = refAction match {
    case action: DataGoFeishuExecutionAction =>
      if (action.getState.isCompleted) return action.getState
      val now = System.currentTimeMillis()
      // 超时判定：检测阶段 82006，外发阶段 82007
      if (now - action.startedAt > action.maxWaitTime) {
        val code = if (action.stage == DataGoFeishuStage.Detecting) 82006 else 82007
        logger.error("DataGo Feishu task timeout, dmId={}, stage={}, startedAt={}, maxWaitTime={}",
          action.nodeParams.getDmId, action.stage, Long.box(action.startedAt), Long.box(action.maxWaitTime))
        return fail(action, "DataGo飞书多维表格外发任务等待超时",
          new DataGoFeishuException(code, "任务超过最大等待时间")).getState
      }
      // 轮询节流：未到下次轮询时间则保持当前状态
      if (now < action.nextPollAt) return action.getState
      try {
        action.stage match {
          case DataGoFeishuStage.Detecting => handleDetect(action)
          case DataGoFeishuStage.Exporting => handleExport(action)
          case _ => throw new DataGoFeishuException(82011, "未知执行阶段: " + action.stage)
        }
      } catch {
        case t: Throwable => fail(action, "DataGo飞书多维表格外发执行失败: " + safeMessage(t), t)
      }
      action.getState
    case _ => RefExecutionState.Failed
  }

  /**
   * ② 任务生成(taskId空) / 状态查询(taskId非空)。
   * <p>
   * 首次调用 taskId 传 null 创建任务并取回 taskId；之后轮询状态：
   * inited/detecting 继续，detected_pass 进入 EXPORTING，detected_fail/detect_error 失败，exported 成功。
   */
  private def handleDetect(action: DataGoFeishuExecutionAction): Unit = {
    val response = action.client.createOrQueryTask(action.nodeParams, action.taskId)
    validateTaskResponse(response)
    if (action.taskId == null) {
      if (response.getTaskId == null) {
        throw new DataGoFeishuException(82004, "DataGo首次检测未返回taskId")
      }
      action.taskId = response.getTaskId
      logger.info(s"DataGo Feishu detect task created, dmId=${action.nodeParams.getDmId}, taskId=${action.taskId}")
    }
    action.lastRemoteStatus = safe(response.getStatus)
    action.lastSummary = safe(response.getResultSummary)
    logger.info(s"DataGo Feishu detect polled, dmId=${action.nodeParams.getDmId}, taskId=${action.taskId}, status=${action.lastRemoteStatus}, summary=${action.lastSummary}")
    normalize(response.getStatus) match {
      case "inited" | "detecting" =>
        action.nextPollAt = System.currentTimeMillis() + action.detectPollInterval
        appendLog(action, "检测中，下次轮询: " + action.detectPollInterval + "ms 后，状态: " + action.lastRemoteStatus)
      case "detected_pass" =>
        action.stage = DataGoFeishuStage.Exporting
        action.nextPollAt = 0L
        appendLog(action, "敏感数据检测通过，开始执行外发到飞书多维表格")
        logger.info(s"DataGo Feishu detect passed, dmId=${action.nodeParams.getDmId}, taskId=${action.taskId}, enter EXPORTING")
      case "detected_fail" =>
        throw new DataGoFeishuException(82005, "DataGo检测不通过: " + safe(response.getResultSummary))
      case "detect_error" =>
        throw new DataGoFeishuException(82006, "DataGo检测异常: " + safe(response.getResultSummary))
      case "exported" =>
        action.stage = DataGoFeishuStage.Success
        action.setState(RefExecutionState.Success)
        appendLog(action, "任务已外发完成")
      case status =>
        throw new DataGoFeishuException(82004, "DataGo检测接口返回未知状态: " + status)
    }
  }

  /**
   * ③ 执行外发（同步一次性：建表+写sheet+飞书消息），含重试策略。
   * <p>
   * 502/504（飞书不可达）→ 可重试（executeRetryMax 次）；413/409（超限/状态冲突）→ 不可重试；
   * 部分表 failed → 重试失败表（已写入不回滚），耗尽抛 82008。
   */
  private def handleExport(action: DataGoFeishuExecutionAction): Unit = {
    try {
      val response = action.client.executeExport(
        action.nodeParams.getDmId, action.nodeParams.getNotifyUsers, action.taskId)
      action.bitableUrl = safe(response.getBitableUrl)
      val failedSheets: Seq[SheetResult] = Option(response.getSheets) match {
        case None => Seq.empty
        case Some(list) => list.filter(s =>
          s != null && !"success".equalsIgnoreCase(normalize(s.getStatus)))
      }
      if (failedSheets.isEmpty) {
        action.stage = DataGoFeishuStage.Success
        action.setState(RefExecutionState.Success)
        appendLog(action, "数据已外发到飞书多维表格: " + action.bitableUrl)
        logger.info("DataGo Feishu export succeeded, dmId={}, taskId={}, bitableUrl={}",
          action.nodeParams.getDmId, action.taskId, action.bitableUrl)
      } else {
        // 部分表失败：重试，已写入不回滚
        if (action.executeRetryCount < action.executeRetryMax) {
          action.executeRetryCount += 1
          action.nextPollAt = System.currentTimeMillis() + action.executeRetryInterval
          logger.warn("DataGo Feishu export partial failure, dmId={}, taskId={}, retry={}/{}, failedSheets={}",
            action.nodeParams.getDmId, action.taskId, Int.box(action.executeRetryCount), Int.box(action.executeRetryMax), sheetSummary(failedSheets))
          appendLog(action, "外发部分表失败，第" + action.executeRetryCount + "次重试: " + sheetSummary(failedSheets))
        } else {
          throw new DataGoFeishuException(82008,
            "DataGo外发部分表失败且重试耗尽（已写入不回滚）: " + sheetSummary(failedSheets))
        }
      }
    } catch {
      case e: DataGoFeishuException if e.getHttpCode == 502 || e.getHttpCode == 504 =>
        // 飞书不可达，可重试
        if (action.executeRetryCount < action.executeRetryMax) {
          action.executeRetryCount += 1
          action.nextPollAt = System.currentTimeMillis() + action.executeRetryInterval
          logger.warn("DataGo Feishu export feishu unreachable({}), dmId={}, taskId={}, retry={}/{}, error={}",
            Int.box(e.getHttpCode), action.nodeParams.getDmId, action.taskId, Int.box(action.executeRetryCount), Int.box(action.executeRetryMax), e.getMessage)
          appendLog(action, "飞书服务不可达，第" + action.executeRetryCount + "次重试: " + e.getMessage)
        } else {
          throw new DataGoFeishuException(82007, "DataGo外发重试次数耗尽: " + e.getMessage, e)
        }
      // 413/409(82009) / 403(82003) / 400(82001) / 500等(82007) 不可重试，直接抛出由外层失败处理
      case e: DataGoFeishuException => throw e
    }
  }

  private def validateTaskResponse(response: TaskResponse): Unit = {
    if (response == null || isBlank(response.getStatus)) {
      throw new DataGoFeishuException(82004, "DataGo任务接口未返回有效状态")
    }
  }

  override def result(action: RefExecutionAction): ExecutionResponseRef = action match {
    case dataGoAction: DataGoFeishuExecutionAction =>
      if (dataGoAction.getState == RefExecutionState.Success) {
        new ExecutionResponseRefBuilder().success()
      } else if (dataGoAction.getExecutionResponseRef != null) {
        dataGoAction.getExecutionResponseRef
      } else {
        new ExecutionResponseRefBuilder().error()
      }
    case _ => new ExecutionResponseRefBuilder().error()
  }

  override def kill(action: RefExecutionAction): Boolean = action match {
    case dataGoAction: DataGoFeishuExecutionAction =>
      dataGoAction.setKilledFlag(true)
      dataGoAction.stage = DataGoFeishuStage.Killed
      dataGoAction.setState(RefExecutionState.Killed)
      appendLog(dataGoAction, "节点已终止，不再轮询DataGo")
      true
    case _ => false
  }

  override def progress(action: RefExecutionAction): Float = action match {
    case a: DataGoFeishuExecutionAction => a.stage match {
      case DataGoFeishuStage.Detecting => 0.3f
      case DataGoFeishuStage.Exporting => 0.8f
      case DataGoFeishuStage.Success => 1.0f
      case _ => 0.0f
    }
    case _ => 0.0f
  }

  override def log(action: RefExecutionAction): String = action match {
    case a: DataGoFeishuExecutionAction =>
      LogUtils.generateInfo("DataGo Feishu export stage=" + a.stage + ", remoteStatus=" + a.lastRemoteStatus)
    case _ => LogUtils.generateERROR("Invalid DataGo Feishu execution action")
  }

  override def createAsyncResponseRef(
      requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl,
      action: RefExecutionAction): AsyncExecutionResponseRef = action match {
    case a: DataGoFeishuExecutionAction =>
      val response = super.createAsyncResponseRef(requestRef, action)
      new AsyncExecutionResponseRef.Builder()
        .setMaxLoopTime(a.maxWaitTime)
        .setAskStatePeriod(a.askStatePeriod)
        .setAsyncExecutionResponseRef(response)
        .build()
    case _ => super.createAsyncResponseRef(requestRef, action)
  }

  private def buildProperties(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl): Properties = {
    val properties = new Properties
    service.getAppInstance.getConfig.foreach {
      case (key, value) if key != null && value != null => properties.setProperty(key, value.toString)
      case _ =>
    }
    requestRef.getExecutionRequestRefContext.getRuntimeMap.foreach {
      case ("job.desc", value) if value != null => parseJobDesc(value.toString, properties)
      case (key, value) if key != null && value != null =>
        properties.setProperty(key, replaceVariables(value.toString))
      case _ =>
    }
    val variable = requestRef.getRefJobContent.get("variable")
    if (variable != null && variable.isInstanceOf[java.util.Map[_, _]]) {
      variable.asInstanceOf[java.util.Map[String, Object]].foreach {
        case (key, value) if key != null && value != null =>
          properties.setProperty(key, replaceVariables(value.toString))
        case _ =>
      }
    }
    properties
  }

  private def parseJobDesc(value: String, properties: Properties): Unit = {
    value.split("[;\\r\\n]+").map(_.trim).filter(_.contains("=")).foreach { row =>
      val index = row.indexOf('=')
      if (index > 0) {
        properties.setProperty(row.substring(0, index).trim, replaceVariables(row.substring(index + 1).trim))
      }
    }
  }

  private def replaceVariables(value: String): String = {
    try VariableUtils.replace(value) catch { case _: Throwable => value }
  }

  /**
   * 解析正整数长整型参数，按三级优先级回退：
   * ① 节点级 UI 值（nodeKey，用户在节点参数面板填写，非空则用）
   * ② 实例级 enhance_json 配置（configKey，写入 dss_appconn_instance.enhance_json）
   * ③ 硬编码默认值（defaultValue）
   * nodeKey 传空串表示该参数不暴露为节点 UI 属性，仅走实例配置（如 executeRetryInterval）。
   */
  private def positiveLong(properties: Properties, nodeKey: String, configKey: String, defaultValue: Long): Long = {
    val raw = Option(properties.getProperty(nodeKey)).filter(_.trim.nonEmpty)
      .getOrElse(properties.getProperty(configKey, defaultValue.toString))
    try {
      val value = raw.trim.toLong
      if (value <= 0) throw new NumberFormatException("must be positive")
      value
    } catch {
      case e: NumberFormatException =>
        throw new DataGoFeishuException(82001, "参数 " + nodeKey + " 必须为正整数", e)
    }
  }

  /**
   * 解析非负整数参数，优先级同 [[positiveLong]]：节点级 UI 值 > 实例级配置 > 硬编码默认。
   * nodeKey 传空串表示该参数不暴露为节点 UI 属性，仅走实例配置。
   */
  private def positiveInt(properties: Properties, nodeKey: String, configKey: String, defaultValue: Int): Int = {
    val raw = Option(properties.getProperty(nodeKey)).filter(_.trim.nonEmpty)
      .getOrElse(properties.getProperty(configKey, defaultValue.toString))
    try {
      val value = raw.trim.toInt
      if (value < 0) throw new NumberFormatException("must be non-negative")
      value
    } catch {
      case e: NumberFormatException =>
        throw new DataGoFeishuException(82001, "参数 " + nodeKey + " 必须为非负整数", e)
    }
  }

  private def fail(action: DataGoFeishuExecutionAction, message: String, t: Throwable): DataGoFeishuExecutionAction = {
    action.stage = DataGoFeishuStage.Failed
    action.setState(RefExecutionState.Failed)
    action.setExecutionResponseRef(new ExecutionResponseRefBuilder().setErrorMsg(message).setException(t).error())
    t match {
      case e: DataGoFeishuException => logger.error(message + " [errorCode=" + e.getErrorCode + "]", t)
      case _ => logger.error(message, t)
    }
    appendLog(action, message)
    action
  }

  private def appendLog(action: DataGoFeishuExecutionAction, message: String): Unit = {
    if (action.getExecutionRequestRefContext != null) {
      action.getExecutionRequestRefContext.appendLog(timestamp() + " " + message)
    }
  }

  private def timestamp(): String = {
    logTimestampFormatter.format(LocalDateTime.now())
  }

  private def sheetSummary(sheets: Seq[SheetResult]): String = {
    if (sheets == null || sheets.isEmpty) "无"
    else sheets.map(s => s.getTableName + "(" + safe(s.getStatus) + ")").mkString(",")
  }

  private def normalize(value: String): String = if (value == null) "" else value.trim.toLowerCase
  private def isBlank(value: String): Boolean = value == null || value.trim.isEmpty
  private def safe(value: String): String = if (value == null || value.trim.isEmpty) "无" else value.trim
  private def safeMessage(t: Throwable): String =
    if (t == null) "未知异常"
    else if (isBlank(t.getMessage)) t.getClass.getSimpleName
    else t.getMessage
}
