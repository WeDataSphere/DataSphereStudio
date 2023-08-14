package com.webank.wedatasphere.dss.workflow.service.impl;

import com.webank.wedatasphere.dss.appconn.scheduler.SchedulerAppConn;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestReleaseOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestReleaseOrchestrationStatus;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseConvertOrchestrator;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import com.webank.wedatasphere.dss.workflow.exception.DSSWorkflowErrorException;
import com.webank.wedatasphere.dss.workflow.service.DSSFlowService;
import org.apache.linkis.rpc.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by enjoyyin on 2021/8/10.
 */
@Component
public class InternalPublishServiceImpl extends PublishServiceImpl {
    @Autowired
    DSSFlowService dssFlowService;

    @Override
    protected Sender getOrchestratorSender() {
        // dss-framework-release-server-webank will put into project server, so use projectSender directly.
        return DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender();
    }

    @Override
    public String submitPublish(String convertUser, Long workflowId, Map<String, Object> dssLabel, Workspace workspace, String comment) throws Exception {
        //校验是否执行通过，如果工作流执行不成功，则不发布工作流
        if (!dssFlowService.isExecuteSuccess(workflowId)) {
            throw new DSSWorkflowErrorException(80001, "请先执行工作流，成功后，再发布！");
        }
        return super.submitPublish(convertUser, workflowId, dssLabel, workspace, comment);
    }

    @Override
    protected ResponseConvertOrchestrator requestConvertOrchestration(String comment, Long workflowId, String convertUser, Workspace workspace,
                                                                      SchedulerAppConn schedulerAppConn, Map<String, Object> dssLabel, AppInstance appInstance) {
        RequestReleaseOrchestration requestReleaseOrchestration = new RequestReleaseOrchestration();
        requestReleaseOrchestration.setComment(comment);
        requestReleaseOrchestration.setOrcAppId(workflowId);
        requestReleaseOrchestration.setUserName(convertUser);
        requestReleaseOrchestration.setWorkspace(workspace);
        requestReleaseOrchestration.setConvertAllOrcs(schedulerAppConn.getOrCreateConversionStandard().getDSSToRelConversionService(appInstance).isConvertAllOrcs());
        requestReleaseOrchestration.setLabels(dssLabel);
        ResponseConvertOrchestrator response = RpcAskUtils.processAskException(getOrchestratorSender().ask(requestReleaseOrchestration),
                ResponseConvertOrchestrator.class, RequestReleaseOrchestration.class);
        return response;
    }

    @Override
    protected ResponseConvertOrchestrator requestConvertOrchestrationStatus(String taskId){
        RequestReleaseOrchestrationStatus req =  new RequestReleaseOrchestrationStatus();
        req.setId(taskId);
        return RpcAskUtils.processAskException(getOrchestratorSender().ask(req), ResponseConvertOrchestrator.class,
                RequestReleaseOrchestrationStatus.class);
    }
}
