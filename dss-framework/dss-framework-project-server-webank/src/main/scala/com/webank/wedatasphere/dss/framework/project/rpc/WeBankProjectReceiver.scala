package com.webank.wedatasphere.dss.framework.project.rpc

import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum
import com.webank.wedatasphere.dss.framework.project.service.WebankDSSProjectOperateService
import com.webank.wedatasphere.dss.framework.project.service.impl.ProjectAuditHttpRequestHook
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestDeleteOrchestrator
import org.apache.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2022/9/22.
  */
class WeBankProjectReceiver(webankDSSProjectOperateService: WebankDSSProjectOperateService) extends Receiver {

  override def receive(message: Any, sender: Sender): Unit = receiveAndReply(message, sender)

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case orchestrator: RequestDeleteOrchestrator =>
      val record = ProjectOperateRecordBO.of(orchestrator.getWorkspaceName.toLong, orchestrator.getProjectName.toLong, ProjectOperateTypeEnum.DELETE_ORCHESTRATOR,
        s"${orchestrator.getUserName} deleted orchestration,orchestratorId:${orchestrator.getOrchestratorId},orchestratorName:${orchestrator.getOrchestratorName}.", orchestrator.getUserName)
      record.success()
      webankDSSProjectOperateService.addOneRecord(record)
    case _ =>
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {
    //never use
  }
}
