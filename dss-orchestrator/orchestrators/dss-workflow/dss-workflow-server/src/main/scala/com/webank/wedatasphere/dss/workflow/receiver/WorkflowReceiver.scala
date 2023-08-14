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

package com.webank.wedatasphere.dss.workflow.receiver

import com.webank.wedatasphere.dss.orchestrator.common.protocol.{RequestAppCompare, RequestExecutionHistory, RequestWorkflowValidNode}
import com.webank.wedatasphere.dss.workflow.service.{DSSFlowService, CompareWorkflowService}
import org.apache.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration


/**
 * Created by allenlliu on 2020/10/21.
 */
class WorkflowReceiver(dssFlowService: DSSFlowService, compareWorkflowService: CompareWorkflowService) extends Receiver {

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case executionHistoryRequest: RequestExecutionHistory =>
      dssFlowService.getExecutionHistory(executionHistoryRequest)
    case appCompareRequest: RequestAppCompare =>
      compareWorkflowService.compareWorkflow(appCompareRequest)
    case requestWorkflowValidNode: RequestWorkflowValidNode =>
      dssFlowService.validWorkflowNode(requestWorkflowValidNode)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
