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
import com.webank.wedatasphere.dss.workflow.service.{CompareWorkflowService, DSSFlowService}
import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct


/**
 * Created by allenlliu on 2020/10/21.
 */
@Component
class WorkflowChooser extends ReceiverChooser {

  @Autowired
  var compareWorkflowService: CompareWorkflowService = _

  @Autowired
  var dssFlowService: DSSFlowService = _

  var receiver: Option[WorkflowReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new WorkflowReceiver(dssFlowService,compareWorkflowService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: RequestExecutionHistory => receiver
    case _: RequestAppCompare => receiver
    case _: RequestWorkflowValidNode => receiver
    case _ => None
  }
}