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

import com.webank.wedatasphere.dss.common.entity.node.DSSEdge
import com.webank.wedatasphere.dss.flow.execution.entrance.job.FlowEntranceJob
import com.webank.wedatasphere.dss.flow.execution.entrance.node.NodeExecutionState.NodeExecutionState
import com.webank.wedatasphere.dss.flow.execution.entrance.utils.BranchExpressionUtils
import com.webank.wedatasphere.dss.linkis.node.execution.job.LinkisJob
import com.webank.wedatasphere.dss.workflow.core.entity.{Workflow, WorkflowNode}

import scala.collection.JavaConversions._

class BranchNodeRunner(flow: Workflow) extends NodeRunner {

  private var node: WorkflowNode = _
  private var canceled: Boolean = false
  private var status: NodeExecutionState = NodeExecutionState.Inited
  private var nodeRunnerListener: com.webank.wedatasphere.dss.flow.execution.entrance.listener.NodeRunnerListener = _
  private var executedInfo: String = _
  private var startTime: Long = _
  private var nowTime: Long = _

  override def getNode: WorkflowNode = this.node

  override def setNode(node: WorkflowNode): Unit = {
    this.node = node
  }

  override def getLinkisJob: LinkisJob = null

  override def cancel(): Unit = if (!this.canceled) this synchronized {
    if (!this.canceled) {
      this.canceled = true
      this.transitionState(NodeExecutionState.Cancelled)
    }
  }

  override def pause(): Unit = {}

  override def resume(): Boolean = true

  override def isCanceled: Boolean = this.canceled

  override def getStatus: NodeExecutionState = this.status

  override def isLinkisJobCompleted: Boolean = NodeExecutionState.isCompleted(this.status)

  override def setStatus(nodeExecutionState: NodeExecutionState): Unit = this.status = nodeExecutionState

  override def setNodeRunnerListener(nodeRunnerListener: com.webank.wedatasphere.dss.flow.execution.entrance.listener.NodeRunnerListener): Unit = {
    this.nodeRunnerListener = nodeRunnerListener
  }

  override def getNodeRunnerListener: com.webank.wedatasphere.dss.flow.execution.entrance.listener.NodeRunnerListener = this.nodeRunnerListener

  override def getNodeExecutedInfo(): String = this.executedInfo

  override def setNodeExecutedInfo(info: String): Unit = this.executedInfo = info

  override def getStartTime(): Long = this.startTime

  override def setStartTime(startTime: Long): Unit = this.startTime = startTime

  override def getNowTime(): Long = this.nowTime

  override def setNowTime(nowTime: Long): Unit = this.nowTime = nowTime

  override def run(): Unit = {
    this.setStartTime(System.currentTimeMillis())
    try {
      val currentNodeId = node.getId
      val outgoingEdges = BranchExpressionUtils.sortEdges(
        flow.getWorkflowNodeEdges.map(_.getDSSEdge).filter(edge => currentNodeId == edge.getSource)
      )
      val context = BranchExpressionUtils.buildEvaluationContext(node)
      val branchRuleText = BranchExpressionUtils.getBranchRuleText(node)
      if (!Option(branchRuleText).exists(_.trim.nonEmpty)) {
        throw new IllegalStateException(s"Branch node ${node.getName} must define branch.rules.")
      }
      val selectedEdge = selectEdgeByRules(outgoingEdges, BranchExpressionUtils.parseBranchRules(branchRuleText), context)
      if (selectedEdge.isEmpty) {
        throw new IllegalStateException(s"No branch rule matched for node ${node.getName}.")
      }
      val selectedTarget = selectedEdge.map(_.getTarget).orNull
      getNodeRunnerListener match {
        case flowEntranceJob: FlowEntranceJob =>
          flowEntranceJob.recordBranchSelection(currentNodeId, selectedTarget)
        case _ =>
      }
      this.executedInfo = Option(selectedTarget).getOrElse("default")
      this.transitionState(NodeExecutionState.Succeed)
    } catch {
      case t: Throwable =>
        error(s"Failed to execute branch node ${node.getName}" + t.getMessage)
        this.transitionState(NodeExecutionState.Failed)
    } finally {
      this.setNowTime(System.currentTimeMillis())
    }
  }

  private def selectEdgeByRules(edges: Seq[DSSEdge], rules: Seq[BranchExpressionUtils.BranchRule], context: Map[String, String]): Option[DSSEdge] = {
    val workflowNodesById = flow.getWorkflowNodes.map(node => node.getId -> node).toMap
    def matchEdge(targetName: String): Option[DSSEdge] = {
      val normalized = Option(targetName).map(_.trim).getOrElse("")
      edges.find { edge =>
        workflowNodesById.get(edge.getTarget).exists { targetNode =>
          normalized.equalsIgnoreCase(targetNode.getName) || normalized == targetNode.getId || normalized == edge.getTarget
        }
      }
    }
    rules.find(rule => !BranchExpressionUtils.isDefaultRule(rule) && BranchExpressionUtils.evaluateCondition(rule.condition, context))
      .flatMap(rule => matchEdge(rule.targetName))
      .orElse(rules.find(BranchExpressionUtils.isDefaultRule).flatMap(rule => matchEdge(rule.targetName)))
  }

}
