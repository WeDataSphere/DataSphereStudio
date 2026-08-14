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

package com.webank.wedatasphere.dss.workflow.server.receiver

import com.webank.wedatasphere.dss.orchestrator.common.protocol.{RequestAppCompare, RequestAppWithSubFlowCompare, RequestExecutionHistory, RequestWorkflowValidNode}
import com.webank.wedatasphere.dss.workflow.service.{WebankCompareWorkflowService, WebankDSSFlowService}
import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Created by allenlliu on 2020/10/21.
 */
@Component
class WebankDSSWorkflowChooser extends ReceiverChooser {

  @Autowired
  var webankCompareWorkflowService: WebankCompareWorkflowService = _

  @Autowired
  var webankDSSFlowService: WebankDSSFlowService = _

  var receiver: Option[WebankDSSWorkflowReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new WebankDSSWorkflowReceiver(webankDSSFlowService,webankCompareWorkflowService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: RequestExecutionHistory => receiver
    case _: RequestAppCompare => receiver
    case _: RequestAppWithSubFlowCompare => receiver
    case _: RequestWorkflowValidNode => receiver
    case _ => None
  }
}