/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu

import com.webank.wedatasphere.dss.appconn.datagofeishu.client.DataGoFeishuClient
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.NodeParams
import com.webank.wedatasphere.dss.standard.app.development.listener.common.AbstractRefExecutionAction
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef

/**
 * 执行状态机阶段常量。
 * <p>
 * INIT(参数解析+①表单校验) → DETECTING(②任务创建/轮询) → EXPORTING(③执行外发) → SUCCESS；
 * 失败转 FAILED，被 kill 转 KILLED。
 */
object DataGoFeishuStage {
  val Init = "INIT"
  val Detecting = "DETECTING"
  val Exporting = "EXPORTING"
  val Success = "SUCCESS"
  val Failed = "FAILED"
  val Killed = "KILLED"
}

/**
 * 节点执行状态载体，保存跨次 state() 调用所需的状态。
 * <p>
 * ② taskId 为 DataGo 外发任务主键（数值型），null 表示尚未创建；
 * nextPollAt 控制轮询节流；executeRetryCount 记录 ③ 外发重试次数。
 */
class DataGoFeishuExecutionAction extends AbstractRefExecutionAction {
  private var response: ExecutionResponseRef = _
  /** DataGo 接口客户端（submit 阶段初始化） */
  var client: DataGoFeishuClient = _
  /** 解析后的节点参数 */
  var nodeParams: NodeParams = _
  /** ② 检测任务ID，null 表示尚未创建 */
  var taskId: java.lang.Long = _
  /** 当前状态机阶段 */
  var stage: String = DataGoFeishuStage.Detecting

  /** ② 检测状态轮询间隔（毫秒） */
  var detectPollInterval: Long = 30000L
  /** ③ 外发失败最大重试次数 */
  var executeRetryMax: Int = 3
  /** ③ 外发重试间隔（毫秒） */
  var executeRetryInterval: Long = 30000L
  /** 节点最大等待时间（毫秒） */
  var maxWaitTime: Long = 7200000L
  /** 执行开始时间戳，用于超时判定 */
  var startedAt: Long = System.currentTimeMillis()
  /** 下次允许轮询的时间戳，未到则 state() 直接返回当前状态 */
  var nextPollAt: Long = 0L

  /** ③ 外发已重试次数 */
  var executeRetryCount: Int = 0
  /** 最近一次远端状态（② status） */
  var lastRemoteStatus: String = ""
  /** 最近一次远端摘要（② resultSummary） */
  var lastSummary: String = ""
  /** ③ 外发返回的多维表格 URL */
  var bitableUrl: String = ""

  def setExecutionResponseRef(value: ExecutionResponseRef): Unit = response = value
  def getExecutionResponseRef: ExecutionResponseRef = response
  /** DSS 询问状态周期：取检测轮询与外发重试间隔的较小值，下限 1s */
  def askStatePeriod: Long = Math.max(1000L, Math.min(detectPollInterval, executeRetryInterval))
}
