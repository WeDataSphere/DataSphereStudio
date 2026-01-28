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

import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorVo
import com.webank.wedatasphere.dss.orchestrator.common.protocol._
import com.webank.wedatasphere.dss.orchestrator.server.service.{OrchestratorService, WebankOrchestratorService}
import org.apache.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration


/**
 * Created by allenlliu on 2020/10/21.
 */
class WebankDSSOrchestratorReceiver(webankOrchestratorService: WebankOrchestratorService, orchestratorService: OrchestratorService)  extends Receiver {

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case requestOrcDetail: RequestOrcDetail =>
      val projectId = requestOrcDetail.getProjectId
      val dssLabel = requestOrcDetail.getDssLabel
      val username = requestOrcDetail.getUsername
      val permissionUsers = requestOrcDetail.getPermissionUsers
      val orchestratorDetails = webankOrchestratorService.getOrchestratorDetails(username, projectId, dssLabel, permissionUsers)
      new ResponseOrcDetail(projectId, dssLabel, username, orchestratorDetails)

    case requestOrcSchedule: RequestOrcSchedule =>
      val username = requestOrcSchedule.getUsername
      val projectName = requestOrcSchedule.getProjectName
      val orchestratorId = requestOrcSchedule.getOrchestratorId
      val scheduleTime = requestOrcSchedule.getScheduleTime
      val alarmEmails = requestOrcSchedule.getAlarmEmails
      val alarmLevel = requestOrcSchedule.getAlarmLevel
      val orchestratorName = webankOrchestratorService.setScheduleFlow(username,  projectName,  orchestratorId,  scheduleTime,  alarmEmails,  alarmLevel)
      new ResponseOrcSchedule(orchestratorName)

    case requestOrcSchedualisDelete: RequestOrcSchedualisDelete =>
      val orchestratorId = requestOrcSchedualisDelete.getOrchestratorId
      webankOrchestratorService.deleteScheduleFlow(orchestratorId)

    case requestOrcSchedulePriv: RequestOrcSchedulePriv =>
      val username = requestOrcSchedulePriv.getUsername
      val workspaceId = requestOrcSchedulePriv.getWorkspaceId
      val projectID  = requestOrcSchedulePriv.getProjectID
      val projectName = requestOrcSchedulePriv.getProjectName
      val orchestratorId  = requestOrcSchedulePriv.getOrchestratorId
      val accessUsers  = requestOrcSchedulePriv.getAccessUsers
      val priv  = requestOrcSchedulePriv.getPriv
      webankOrchestratorService.setOrchestratorPriv(username, workspaceId, projectID, projectName, orchestratorId, accessUsers,priv)

    case requestOrcSchedualisUpdate: RequestOrcSchedualisUpdate =>
      val orchestratorId  = requestOrcSchedualisUpdate.getOrchestratorId
      val activeFlag = requestOrcSchedualisUpdate.getActiveFlag
      webankOrchestratorService.updateScheduleFlow(orchestratorId, activeFlag)

    //获取编排模式的发布标识
    case requestOrcIsPublishedFlag: RequestOrcIsPublishedFlag =>
      val projectId  = requestOrcIsPublishedFlag.getProjectId
      val uuid   = requestOrcIsPublishedFlag.getUuid
      webankOrchestratorService.getOrcIsPublishFlag(projectId,uuid)

    case requestCreateOrchestrator: RequestCreateOrchestrator =>
      val orchestratorVo = orchestratorService.createOrchestrator(requestCreateOrchestrator.getUserName, requestCreateOrchestrator.getWorkspace,
        requestCreateOrchestrator.getProjectName, requestCreateOrchestrator.getProjectId, requestCreateOrchestrator.getDescription,
        requestCreateOrchestrator.getDssOrchestratorInfo, requestCreateOrchestrator.getDssLabels)
      new ResponseCreateOrchestrator(orchestratorVo)

    case requestOrcDelete: RequestOrcDelete =>
      val username = requestOrcDelete.getUserName
      val projectId = requestOrcDelete.getProjectId
      val uuid = requestOrcDelete.getUuid
      val workspaceId = requestOrcDelete.getWorkspaceId
      val workspace = requestOrcDelete.getWorkspace
      val dssLabel = requestOrcDelete.getDssLabel
      val orchestratorName = requestOrcDelete.getOrchestratorName
      webankOrchestratorService.deleteOrchestratorByLabel(username, projectId, uuid, workspaceId, workspace, dssLabel, orchestratorName)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
