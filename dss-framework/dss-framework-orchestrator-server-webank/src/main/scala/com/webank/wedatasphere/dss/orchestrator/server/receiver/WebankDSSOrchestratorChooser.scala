/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.orchestrator.server.receiver

import com.webank.wedatasphere.dss.orchestrator.common.protocol._
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorDeleteRequest
import com.webank.wedatasphere.dss.orchestrator.server.service.{OrchestratorFrameworkService, OrchestratorService, WebankOrchestratorService}
import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser, Sender}

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration


/**
 * Created by allenlliu on 2020/10/21.
 */
@Component
class WebankDSSOrchestratorChooser extends ReceiverChooser {

  @Autowired
  var webankOrchestratorService: WebankOrchestratorService = _

  @Autowired
  var orchestratorService: OrchestratorService = _

  var webankDSSOrchestratorReceiver: Option[WebankDSSOrchestratorReceiver] = _

  @PostConstruct
  def init(): Unit = webankDSSOrchestratorReceiver = Some(new WebankDSSOrchestratorReceiver(webankOrchestratorService, orchestratorService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: RequestOrcDetail => webankDSSOrchestratorReceiver
    case _: RequestOrcSchedule => webankDSSOrchestratorReceiver
    case _: RequestOrcSchedualisDelete => webankDSSOrchestratorReceiver
    case _: RequestOrcSchedulePriv => webankDSSOrchestratorReceiver
    case _: RequestOrcSchedualisUpdate => webankDSSOrchestratorReceiver
    case _: RequestOrcIsPublishedFlag => webankDSSOrchestratorReceiver
    case _: RequestWtssPriority => webankDSSOrchestratorReceiver
    case _: RequestCreateOrchestrator=> webankDSSOrchestratorReceiver
    case _: RequestOrcDelete=> webankDSSOrchestratorReceiver
    case _ => None
  }
}