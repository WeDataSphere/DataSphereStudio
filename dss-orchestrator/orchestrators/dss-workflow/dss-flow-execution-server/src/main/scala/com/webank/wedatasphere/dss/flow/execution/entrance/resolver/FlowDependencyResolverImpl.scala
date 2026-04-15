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

package com.webank.wedatasphere.dss.flow.execution.entrance.resolver

import java.util

import com.webank.wedatasphere.dss.flow.execution.entrance.FlowContext
import com.webank.wedatasphere.dss.flow.execution.entrance.enums.ExecuteStrategyEnum
import com.webank.wedatasphere.dss.flow.execution.entrance.job.FlowEntranceJob
import com.webank.wedatasphere.dss.flow.execution.entrance.utils.BranchExpressionUtils
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode
import org.apache.linkis.common.utils.Logging
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class FlowDependencyResolverImpl extends FlowDependencyResolver with Logging {
  override def resolvedFlow(flowJob: FlowEntranceJob) = {

    info(s"${flowJob.getId} Start to get executable node")

    val flowContext: FlowContext = flowJob.getFlowContext
    val flow = flowJob.getFlow
    val nodes = flowContext.getPendingNodes.toMap.values.map(_.getNode)
    val workflowNodesById = flow.getWorkflowNodes.map(node => node.getId -> node).toMap
    val workflowEdges = flow.getWorkflowNodeEdges.map(_.getDSSEdge)
    val executeStrategy = Option(flowJob.getParams)
      .map(_.get("executeStrategy"))
      .map(_.toString)
      .orNull

    val isSelectedExecute = ExecuteStrategyEnum.IS_SELECTED_EXECUTE.getValue.equalsIgnoreCase(executeStrategy)
    def incomingEdges(node: WorkflowNode) = workflowEdges.filter(_.getTarget == node.getId)

    def isAllParentDependencyCompleted(parents: util.List[String]): Boolean = {
      for (parent <- parents) {
        if (!flowContext.isNodeCompleted(parent)) return false
      }
      true
    }

    def areAllParentsSkipped(node: WorkflowNode): Boolean = {
      node.getDependencys != null &&
        !node.getDependencys.isEmpty &&
        node.getDependencys.forall(flowContext.isNodeSkipped)
    }

    def shouldSkipByBranch(node: WorkflowNode): Boolean = {
      if (isSelectedExecute) {
        false
      } else {
        incomingEdges(node).exists { edge =>
          workflowNodesById.get(edge.getSource).exists { sourceNode =>
            BranchExpressionUtils.isBranchNode(sourceNode) &&
              flowContext.isNodeCompleted(sourceNode.getName) &&
              (flowContext.isNodeSkipped(sourceNode.getName) ||
                !flowJob.hasBranchSelection(sourceNode.getId) ||
                !flowJob.isBranchTargetSelected(sourceNode.getId, node.getId))
          }
        }
      }
    }

    def shouldSkip(node: WorkflowNode): Boolean = {
      shouldSkipByBranch(node) ||
        (!isSelectedExecute && areAllParentsSkipped(node))
    }

    def isBranchRouteMatched(node: WorkflowNode): Boolean = {
      if (isSelectedExecute) {
        true
      } else {
        incomingEdges(node).forall { edge =>
          workflowNodesById.get(edge.getSource) match {
            case Some(sourceNode) if BranchExpressionUtils.isBranchNode(sourceNode) =>
              flowContext.isNodeSucceed(sourceNode.getName) &&
                flowJob.hasBranchSelection(sourceNode.getId) &&
                flowJob.isBranchTargetSelected(sourceNode.getId, node.getId)
            case _ => true
          }
        }
      }
    }


    nodes.foreach { node =>
      val nodeName = node.getName
      def isCanExecutable: Boolean = {
        flowContext.getPendingNodes.containsKey(nodeName) &&
          !FlowContext.isNodeRunning(nodeName, flowContext) &&
          !flowContext.isNodeCompleted(nodeName) &&
          isAllParentDependencyCompleted(node.getDependencys) &&
          !shouldSkip(node) &&
          isBranchRouteMatched(node)
      }
      if (flowContext.getPendingNodes.containsKey(nodeName) && !flowContext.isNodeCompleted(nodeName) && isAllParentDependencyCompleted(node.getDependencys) && shouldSkip(node)) {
        flowContext synchronized {
          if (flowContext.getPendingNodes.containsKey(nodeName) && shouldSkip(node)) {
            flowContext.getPendingNodes.get(nodeName).tunToSkipped()
          }
        }
      } else if (isCanExecutable) {
        flowContext synchronized {
          if (isCanExecutable) flowContext.getPendingNodes.get(nodeName).tunToScheduled()
        }
      }
    }
    info(s"${flowJob.getId} Finished to get executable node(${flowContext.getScheduledNodes.size()})")
  }
}
