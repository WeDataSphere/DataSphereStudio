package com.webank.wedatasphere.dss.flow.execution.entrance.job.parser

import com.webank.wedatasphere.dss.appconn.manager.AppConnManager
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils
import com.webank.wedatasphere.dss.flow.execution.entrance.exception.FlowExecutionErrorException
import com.webank.wedatasphere.dss.flow.execution.entrance.job.FlowEntranceJob
import com.webank.wedatasphere.dss.standard.app.sso.Workspace
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.utils.Logging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import com.webank.wedatasphere.dss.appconn.manager.utils.AppConnManagerUtils

import javax.annotation.PostConstruct

@Order(9)
@Component
class ProxyUserCheckerFlowJobParser extends FlowEntranceJobParser with Logging {

  @PostConstruct
  def init(): Unit = {
    AppConnManagerUtils.autoLoadAppConnManager()
  }

  override def parse(flowEntranceJob: FlowEntranceJob): Unit = {
    val nodes = flowEntranceJob.getFlow.getWorkflowNodes
    if(CollectionUtils.isEmpty(nodes)) return
    val proxyUser = nodes.get(0).getDSSNode.getUserProxy
    val user = flowEntranceJob.getUser
    if(StringUtils.isBlank(proxyUser) || user.equals(proxyUser)) return
    flowEntranceJob.getParams.getOrDefault(DSSCommonUtils.DSS_LABELS_KEY, DSSCommonUtils.ENV_LABEL_VALUE_DEV) match {
      case label: String =>
        if(DSSCommonUtils.ENV_LABEL_VALUE_DEV == label) return
    }
    flowEntranceJob.printLog(s"user $user want to use proxyUser $proxyUser to execute workflow ${flowEntranceJob.getFlow.getName} with job ${flowEntranceJob.getId}, try to check permission.", "INFO")
    val schedulerAppConn = AppConnManager.getAppConnManager.getAppConn(classOf[InternalSchedulerAppConn])
    val appInstances = schedulerAppConn.getAppDesc.getAppInstances
    if (CollectionUtils.isEmpty(appInstances)) {
      throw new FlowExecutionErrorException(90222, schedulerAppConn.getClass.getSimpleName + " has no appInstance.")
    }
    flowEntranceJob.getParams.get("workspace") match {
      case workspace: Workspace =>
        val optionalService = schedulerAppConn.getOrCreateOptionalStandard.getOptionalService(appInstances.get(0))
        optionalService.getOptionalOperation(RefProxyUserFetchOperation.OPERATION_NAME) match {
          case operation: RefProxyUserFetchOperation =>
            val requestRef = new RefProxyUserFetchOperation.RefProxyUserFetchRequestRefImpl
            requestRef.setUserName(user).setWorkspace(workspace)
            val proxyUserList = operation.apply(requestRef).getProxyUserList
            if(!proxyUserList.contains(proxyUser)) {
              flowEntranceJob.printLog(s"user $user has no permission to use proxyUser $proxyUser to execute workflow ${flowEntranceJob.getFlow.getName}, the job is ${flowEntranceJob.getId}, proxyUser list is $proxyUserList.", "ERROR")
              throw new FlowExecutionErrorException(90223, s"user $user has no permission to use proxyUser $proxyUser to execute workflow(用户无权使用代理用户 $proxyUser) 执行工作流.")
            }
        }
      case _ =>
        throw new FlowExecutionErrorException(90222, "workspace is not exists.")
    }
    flowEntranceJob.printLog(s"check successfully! user $user will use proxyUser $proxyUser to execute workflow ${flowEntranceJob.getFlow.getName}, The job is ${flowEntranceJob.getId}.", "INFO")
  }

}
