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

package com.webank.wedatasphere.dss.flow.execution.entrance.job

import java.util

import com.webank.wedatasphere.dss.flow.execution.entrance.exception.FlowExecutionErrorException
import com.webank.wedatasphere.dss.flow.execution.entrance.listener.NodeRunnerListener
import com.webank.wedatasphere.dss.flow.execution.entrance.node.NodeExecutionState.NodeExecutionState
import com.webank.wedatasphere.dss.flow.execution.entrance.node.{NodeExecutionState, NodeRunner}
import com.webank.wedatasphere.dss.flow.execution.entrance.{FlowContext, FlowContextImpl}
import com.webank.wedatasphere.dss.linkis.node.execution.execution.impl.LinkisNodeExecutionImpl
import com.webank.wedatasphere.dss.workflow.core.entity.{Workflow, WorkflowNode}
import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.execute.StorePathExecuteRequest
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteRequest, SuccessExecuteResponse}
import org.apache.linkis.scheduler.queue.SchedulerEventState.Running
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class FlowEntranceJob(persistManager: PersistenceManager) extends EntranceExecutionJob(persistManager) with NodeRunnerListener {

  private var flow: Workflow = _

  private val flowContext: FlowContext = new FlowContextImpl

  private val selectedBranchRoutes = new util.HashMap[String, String]()

  private val flowVariables = new util.HashMap[String, AnyRef]()

  def setFlow(flow: Workflow): Unit = this.flow = flow

  def getFlow: Workflow = this.flow

  def getFlowContext: FlowContext = this.flowContext

  def getFlowVariables: util.Map[String, AnyRef] = this.flowVariables

  private val STATUS_CHANGED_LOCK = "STATUS_CHANGED_LOCK".intern()

  override def init(): Unit = {}

  override def jobToExecuteRequest(): ExecuteRequest = {
    new ExecuteRequest with StorePathExecuteRequest with FlowExecutionRequest {
      override val code: String = FlowEntranceJob.this.getJobRequest.getExecutionCode
      override val storePath: String = FlowEntranceJob.this.getJobRequest match {
        case _ => ""
      }
      override val job: Job = FlowEntranceJob.this
    }
  }

  override def run(): Unit = {
    setResultSize(0)
    if (!isScheduled) return
    startTime = System.currentTimeMillis
    Utils.tryAndWarn(transition(Running))
    getExecutor.execute(jobToExecuteRequest())
  }

  def recordBranchSelection(branchNodeId: String, targetNodeId: String): Unit = {
    this.selectedBranchRoutes.synchronized {
      if (targetNodeId == null) this.selectedBranchRoutes.remove(branchNodeId)
      else this.selectedBranchRoutes.put(branchNodeId, targetNodeId)
    }
  }

  def hasBranchSelection(branchNodeId: String): Boolean = this.selectedBranchRoutes.synchronized {
    this.selectedBranchRoutes.containsKey(branchNodeId)
  }

  def isBranchTargetSelected(branchNodeId: String, targetNodeId: String): Boolean = this.selectedBranchRoutes.synchronized {
    targetNodeId != null && targetNodeId == this.selectedBranchRoutes.get(branchNodeId)
  }

  override def onStatusChanged(fromState: NodeExecutionState, toState: NodeExecutionState, node: WorkflowNode): Unit = {
    val nodeName = node.getDSSNode.getName
    toState match {
      case NodeExecutionState.Failed =>
        printLog(s"Failed to execute node($nodeName),prepare to kill flow job", "ERROR")
        if (NodeExecutionState.isRunning(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getRunningNodes, this.getFlowContext.getFailedNodes, node, "node execute fail")
        this.kill()
        info(s"Succeed to kill flow job")
      case NodeExecutionState.Cancelled =>
        printLog(s"node($nodeName) has cancelled execution", "WARN")
        if (NodeExecutionState.isRunning(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getRunningNodes, this.getFlowContext.getFailedNodes, node, "node has cancelled")
        if (NodeExecutionState.isScheduled(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getScheduledNodes, this.getFlowContext.getFailedNodes, node, "node has cancelled")
      case NodeExecutionState.Skipped =>
        printLog(s"node($nodeName) has skipped execution from $fromState", "WARN")
        if (NodeExecutionState.isScheduled(fromState)) {
          FlowContext.changedNodeState(this.getFlowContext.getScheduledNodes, this.getFlowContext.getSkippedNodes, node, "node has skipped")
        } else if (NodeExecutionState.isInited(fromState)) {
          FlowContext.changedNodeState(this.getFlowContext.getPendingNodes, this.getFlowContext.getSkippedNodes, node, "node has skipped")
        }
        this.STATUS_CHANGED_LOCK.synchronized {
          getExecutor.execute(jobToExecuteRequest())
        }
      case NodeExecutionState.Succeed =>
        collectNodeOutputVariables(nodeName)
        printLog(s"Succeed to execute node($nodeName)", "INFO")
        if (NodeExecutionState.isRunning(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getRunningNodes, this.getFlowContext.getSucceedNodes, node, "node execute success")
        this.STATUS_CHANGED_LOCK.synchronized {
          getExecutor.execute(jobToExecuteRequest())
        }
      case NodeExecutionState.Running =>
        printLog(s"Start to execute node($nodeName) ", "INFO")
        if (NodeExecutionState.isScheduled(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getScheduledNodes, this.getFlowContext.getRunningNodes, node, "node in running")
      case NodeExecutionState.Scheduled =>
        printLog(s"node($nodeName) from inited to scheduled", "INFO")
        if (NodeExecutionState.isInited(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getPendingNodes, this.getFlowContext.getScheduledNodes, node, "node in scheduled")
      case _ =>
    }
    tryCompleted
  }

  private def collectNodeOutputVariables(nodeName: String): Unit = {
    val runner = this.getFlowContext.getRunningNodes.get(nodeName)
    if (runner == null || runner.getLinkisJob == null || runner.getLinkisJob.getJobExecuteResult == null) {
      return
    }
    val resultVariables = Utils.tryCatch {
      LinkisNodeExecutionImpl.getLinkisNodeExecution.getResultVariables(runner.getLinkisJob, 128)
    } {
      case t: Throwable =>
        warn(s"Failed to collect output variables for node $nodeName", t)
        new util.LinkedHashMap[String, String]()
    }
    val outputVariables = resolveNodeOutputVariables(runner, resultVariables)
    if (outputVariables != null && !outputVariables.isEmpty) {
      this.flowVariables.synchronized {
        outputVariables.foreach { case (key, value) =>
          if (key != null && value != null) {
            this.flowVariables.put(key, value)
          }
        }
      }
      info(s"Collected output variables from node($nodeName): ${outputVariables.keySet().mkString(",")}")
    }
  }

  private def resolveNodeOutputVariables(runner: NodeRunner, resultVariables: util.Map[String, String]): util.Map[String, String] = {
    val variables = new util.LinkedHashMap[String, String]()
    if (resultVariables != null) {
      variables.putAll(resultVariables)
    }
    val mappings = getBranchOutputMappings(runner)
    if (mappings.isEmpty) {
      return variables
    }
    val mappedVariables = new util.LinkedHashMap[String, String]()
    mappings.foreach { case (targetKey, sourceKey) =>
      if (StringUtils.isNotBlank(targetKey) && StringUtils.isNotBlank(sourceKey) && resultVariables != null && resultVariables.containsKey(sourceKey)) {
        mappedVariables.put(targetKey, resultVariables.get(sourceKey))
      }
    }
    if (mappedVariables.isEmpty) variables else mappedVariables
  }

  private def getBranchOutputMappings(runner: NodeRunner): Map[String, String] = {
    val params = Option(runner).map(_.getNode).map(_.getDSSNode).map(_.getParams).orNull
    val mappings = new util.LinkedHashMap[String, String]()
    params match {
      case map: util.Map[_, _] =>
        readBranchOutputMapping(map.get("branchOutputMapping")).foreach { case (target, source) => mappings.put(target, source) }
        map.get("configuration") match {
          case configuration: util.Map[_, _] =>
            configuration.get("special") match {
              case special: util.Map[_, _] =>
                readBranchOutputMapping(special.get("branchOutputMapping")).foreach { case (target, source) => mappings.put(target, source) }
                readBranchOutputMapping(special.get("branch.output.mapping")).foreach { case (target, source) => mappings.put(target, source) }
              case _ =>
            }
          case _ =>
        }
      case _ =>
    }
    mappings.toMap
  }

  private def readBranchOutputMapping(raw: Any): Map[String, String] = raw match {
    case mapping: util.Map[_, _] =>
      mapping.collect { case (target, source) if target != null && source != null => target.toString.trim -> source.toString.trim }
        .filter { case (target, source) => StringUtils.isNotBlank(target) && StringUtils.isNotBlank(source) }
        .toMap
    case text if text != null && StringUtils.isNotBlank(text.toString) =>
      text.toString.split(",").flatMap { pair =>
        val parts = pair.split("=", 2).map(_.trim)
        if (parts.length == 2 && StringUtils.isNotBlank(parts(0)) && StringUtils.isNotBlank(parts(1))) {
          Some(parts(0) -> parts(1))
        } else None
      }.toMap
    case _ => Map.empty[String, String]
  }

  def printLog(log: String, level: String): Unit = level match {
    case "INFO" =>
      info(log)
      getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo(log)))
    case "WARN" =>
      warn(log)
      getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateWarn(log)))
    case "ERROR" =>
      error(log)
      getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateERROR(log)))
    case _ =>
  }

  override def kill(): Unit = if (!SchedulerEventState.isCompleted(this.getState)) this synchronized {
    if (!SchedulerEventState.isCompleted(this.getState)) {
      super.kill()
      Utils.tryAndWarn(this.killNodes)
      Utils.tryAndWarn(transitionCompleted(ErrorExecuteResponse(s"execute job(${getId}) failed!", new FlowExecutionErrorException(90101, s"This Flow killed by user"))))
    }
  }

  override def cancel(): Unit = if (!SchedulerEventState.isCompleted(this.getState)) this synchronized {
    if (!SchedulerEventState.isCompleted(this.getState)) {
      Utils.tryAndWarn(this.killNodes)
      super.cancel()
      Utils.tryAndWarn(transitionCompleted(ErrorExecuteResponse(s"cancel job(${getId}) execution!", new FlowExecutionErrorException(90101, s"This Flow killed by user"))))
    }
  }

  def isFlowCompleted: Boolean = this.getFlowContext.getRunningNodes.isEmpty && this.getFlowContext.getPendingNodes.isEmpty && this.getFlowContext.getScheduledNodes.isEmpty

  def tryCompleted: Unit = {
    if (this.isFlowCompleted) {
      info(s"This Flow(${getId}) is Completed")
      if (!SchedulerEventState.isCompleted(this.getState))
        transitionCompleted(SuccessExecuteResponse())
    }
  }

  def killNodes: Unit = {
    val runners = new ArrayBuffer[NodeRunner]()
    runners.addAll(this.getFlowContext.getRunningNodes.values())
    for (node <- runners) {
      Utils.tryAndWarn(node.cancel())
    }
  }

  override def clear(): Unit = {}
}
