/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.flow.execution.entrance.node

import java.util

import com.webank.wedatasphere.dss.flow.execution.entrance.conf.FlowExecutionEntranceConfiguration
import com.webank.wedatasphere.dss.flow.execution.entrance.listener.NodeRunnerListener
import com.webank.wedatasphere.dss.flow.execution.entrance.log.FlowExecutionLog
import com.webank.wedatasphere.dss.flow.execution.entrance.node.NodeExecutionState.NodeExecutionState
import com.webank.wedatasphere.dss.flow.execution.entrance.utils.FlowExecutionUtils
import com.webank.wedatasphere.dss.linkis.node.execution.conf.LinkisJobExecutionConfiguration
import com.webank.wedatasphere.dss.linkis.node.execution.execution.impl.LinkisNodeExecutionImpl
import com.webank.wedatasphere.dss.linkis.node.execution.job.{JobTypeEnum, LinkisJob}
import com.webank.wedatasphere.dss.linkis.node.execution.listener.LinkisExecutionListener
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode
import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}


class DefaultNodeRunner extends NodeRunner with Logging {

  private var node: WorkflowNode = _

  private var linkisJob: LinkisJob = _

  private var canceled: Boolean = false

  private var status: NodeExecutionState = NodeExecutionState.Inited

  private var nodeRunnerListener: NodeRunnerListener = _

  private var executedInfo: String = _

  private var startTime: Long = _

  private var nowTime:Long = _

  private var lastGetStatusTime: Long = 0

  /**
    * 最近一次成功获取到 entrance 状态的时刻。
    * 成功查询（无论终态与否）即刷新；节点启动时初始化为 startTime。
    * 用于"连续状态获取失败时长"判定（P0），及运行时长兜底的基础计时。
    * 并发说明：nodeRunnerQueue 为单队列 take-put 模型，同一 runner 同一时刻
    * 仅被一个 poller 线程持有，本字段读写天然串行，无需同步。
    */
  private var lastStatusSuccessTime: Long = _

  override def getNode: WorkflowNode = this.node

  def setNode(schedulerNode: WorkflowNode): Unit = {
    this.node = schedulerNode
  }

  override def getLinkisJob: LinkisJob = this.linkisJob


  override def isCanceled: Boolean = this.canceled

  override def getStatus: NodeExecutionState = {
    this.status
  }

  override def isLinkisJobCompleted: Boolean = Utils.tryCatch{

    val interval = System.currentTimeMillis() - lastGetStatusTime

    if ( interval < FlowExecutionEntranceConfiguration.NODE_STATUS_INTERVAL.getValue){
      return false
    }

    lastGetStatusTime = System.currentTimeMillis()

    if(NodeExecutionState.isCompleted(getStatus)) return true

    // P1: 节点级最大运行时长兜底（默认关闭；信号节点默认 2 小时），超限翻 Timeout 终态
    if (isNodeRunningTimeout()) {
      return transitionToTimeout()
    }

    val toState = NodeExecutionState.fromLinkisStateName(
      LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob))
    // 成功获取到状态（无论终态与否），刷新连续失败计时基准
    this.lastStatusSuccessTime = System.currentTimeMillis()
    if (NodeExecutionState.isCompleted(toState)) {
      val listener = LinkisNodeExecutionImpl.getLinkisNodeExecution.asInstanceOf[LinkisExecutionListener]
      listener.onStatusChanged(getStatus.toString, toState.toString, this.linkisJob)
      this.transitionState(toState)
      info(s"Finished to execute node of ${this.node.getName}")
      true
    } else {
      false
    }
  }{
    case e:ErrorException => logger.warn(s"failed to get ${this.node.getName} state", e)
      false
    case t :Throwable => logger.error(s"failed to get ${this.node.getName} state", t)
      // REQ-DSS-1.26.0-FIX-001: 查询异常属于暂时性故障，默认继续轮询；
      // 仅当连续失败时长超过阈值才升级为任务失败（kill + Failed），消除"返回 true 但不翻转状态"的语义裂缝
      escalateIfStateFetchTimeout(t)
  }

  /**
    * P1: 生效的节点级最大运行时长（毫秒），<=0 表示关闭。
    * 普通节点读 NODE_MAX_RUNNING_TIME（默认 -1 关闭）；
    * 信号节点（FlowExecutionUtils.isSignalNode 按节点 nodeType 识别）未显式配置普通项时
    * 按 SIGNAL_NODE_MAX_RUNNING_TIME 兜底（默认 2 小时）。
    */
  private def effectiveMaxRunningTime(): Long = {
    val nodeType = if (this.node != null) this.node.getNodeType else ""
    var maxRunningTime = FlowExecutionEntranceConfiguration.NODE_MAX_RUNNING_TIME.getValue
    if (maxRunningTime <= 0 && FlowExecutionUtils.isSignalNode(nodeType)) {
      maxRunningTime = FlowExecutionEntranceConfiguration.SIGNAL_NODE_MAX_RUNNING_TIME.getValue
    }
    maxRunningTime
  }

  /**
    * P1: 节点级最大运行时长兜底判定。
    */
  private def isNodeRunningTimeout(): Boolean = {
    val maxRunningTime = effectiveMaxRunningTime()
    maxRunningTime > 0 && this.startTime > 0 &&
      System.currentTimeMillis() - this.startTime >= maxRunningTime
  }

  /**
    * P1: 运行超时翻转。先尝试 kill Linkis 任务（失败仅告警），再翻 Timeout 终态。
    * Timeout 属于终态（NodeExecutionState.isCompleted），FlowEntranceJob 的 Timeout 分支
    * 会迁移节点并结束工作流。
    */
  private def transitionToTimeout(): Boolean = {
    logger.error(s"Node ${this.node.getName} exceeded max running time " +
      s"(${effectiveMaxRunningTime()}ms configured), started at $startTime, will be turned to Timeout.")
    killLinkisJobQuietly()
    this.transitionState(NodeExecutionState.Timeout)
    true
  }

  /**
    * P0: 连续状态获取失败升级判定。
    * 自 lastStatusSuccessTime（最近一次成功获取状态的时刻）起算，
    * 持续失败达到 NODE_STATE_FETCH_FAIL_TIMEOUT（默认 30 分钟）时：
    * kill Linkis 任务（尽力而为）→ 节点置 Failed → error 告警 → 返回 true 结束轮询；
    * 未超阈值返回 false 继续轮询（附带连续失败时长 warn，便于运维提前介入）。
    */
  private def escalateIfStateFetchTimeout(t: Throwable): Boolean = {
    val failTimeout = FlowExecutionEntranceConfiguration.NODE_STATE_FETCH_FAIL_TIMEOUT.getValue
    val failedDuration = System.currentTimeMillis() - this.lastStatusSuccessTime
    if (failTimeout > 0 && failedDuration >= failTimeout) {
      logger.error(s"Node ${this.node.getName} has failed to fetch Linkis job state for " +
        s"${failedDuration / 1000}s (threshold ${failTimeout / 1000}s since last success at " +
        s"$lastStatusSuccessTime). Escalate to Failed. Last exception: ${t.getMessage}", t)
      killLinkisJobQuietly()
      this.transitionState(NodeExecutionState.Failed)
      true
    } else {
      logger.warn(s"Node ${this.node.getName} failed to fetch Linkis job state, " +
        s"continuous failure ${failedDuration / 1000}s (threshold ${failTimeout / 1000}s), " +
        s"keep polling. Cause: ${t.getMessage}")
      false
    }
  }

  /**
    * 尽力而为地 kill Linkis 任务：
    * 1) 复用 DefaultNodeRunner.cancel 的 kill 链路（LinkisNodeExecutionImpl.cancel → UJESClient.kill），
    *    但不走 cancel() 方法本身——避免置 Cancelled 状态与 canceled 标志位副作用；
    * 2) kill 一个已完成的任务是幂等的（设计文档 4.7），entrance 不可用时 kill 大概率失败，
    *    失败仅记录 warn，不阻断节点状态翻转。
    */
  private def killLinkisJobQuietly(): Unit = Utils.tryAndWarn {
    if (this.linkisJob != null && this.linkisJob.getJobExecuteResult != null) {
      LinkisNodeExecutionImpl.getLinkisNodeExecution.cancel(this.linkisJob)
    }
  }

  override def setNodeRunnerListener(nodeRunnerListener: NodeRunnerListener): Unit = this.nodeRunnerListener = nodeRunnerListener

  override def getNodeRunnerListener: NodeRunnerListener = this.nodeRunnerListener

  override def run(): Unit = {
    info(s"start to run node of ${node.getName}")
    try {
      val jobProps = node.getDSSNode.getParams.remove(FlowExecutionEntranceConfiguration.PROPS_MAP) match {
        case propsMap: util.Map[String, String] => propsMap
        case _ => new util.HashMap[String, String]()
      }
      this.linkisJob = AppConnJobBuilder.builder().setNode(node).setJobProps(jobProps).build.asInstanceOf[LinkisJob]
      this.linkisJob.setLogObj(new FlowExecutionLog(this))
      //set start time
      this.setStartTime(System.currentTimeMillis())
      // 连续状态获取失败的计时基准初始化为节点启动时刻，保证节点从提交起就处于计时保护之下
      this.lastStatusSuccessTime = this.startTime
      if (JobTypeEnum.EmptyJob == this.linkisJob.getJobType) {
        warn("This node is empty type")
        this.transitionState(NodeExecutionState.Succeed)
        return
      }
      this.linkisJob.getJobProps.put(LinkisJobExecutionConfiguration.LINKIS_VERSION_KEY, LinkisJobExecutionConfiguration.LINKIS_DEFAULT_VERSION.getValue)
      LinkisNodeExecutionImpl.getLinkisNodeExecution.runJob(this.linkisJob)
      info(s"Finished to run node of ${node.getName}")
    } catch {
      case t: Throwable =>
        warn(s"Failed to execute node of ${node.getName}", t)
        this.transitionState(NodeExecutionState.Failed)

    }

  }


  override def cancel(): Unit = if (!this.canceled) this synchronized {
    if (this.canceled) return
    this.canceled = true
    if (this.linkisJob != null && this.linkisJob.getJobExecuteResult != null)
      LinkisNodeExecutionImpl.getLinkisNodeExecution.cancel(this.linkisJob)
    this.transitionState(NodeExecutionState.Cancelled)
    warn(s"This node(${node.getName}) has been canceled")
  }

  override def pause(): Unit = {}

  override def resume(): Boolean = true

  override def setStatus(nodeExecutionState: NodeExecutionState): Unit = this.status = nodeExecutionState

  override def getNodeExecutedInfo(): String = this.executedInfo

  override def setNodeExecutedInfo(info: String): Unit = this.executedInfo = info

  override def getStartTime(): Long = this.startTime

  override def setStartTime(startTime: Long): Unit = this.startTime = startTime

  override def getNowTime(): Long = this.nowTime

  override def setNowTime(nowTime: Long): Unit = this.nowTime = nowTime
}
