package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.webank.wedatasphere.dss.common.label.LabelRouteVO;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.project.service.WebankDSSOrchestratorService;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestDeleteOrchestrator;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcDelete;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorMapper;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorDeleteRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.CommonOrchestratorVo;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Created by enjoyyin on 2022/9/22.
 */
@Service
public class WeBankOrchestratorFrameworkServiceImpl extends OrchestratorFrameworkServiceImpl {

    @Autowired
    WebankDSSOrchestratorService webankDSSOrchestratorService;

    @Autowired
    OrchestratorMapper orchestratorMapper;

    @Override
    public CommonOrchestratorVo deleteOrchestrator(String username, OrchestratorDeleteRequest orchestratorDeleteRequest, Workspace workspace)
        throws Exception {
        DSSOrchestratorInfo orchestratorInfo = orchestratorMapper.getOrchestrator(orchestratorDeleteRequest.getId());
        CommonOrchestratorVo orchestratorVo = super.deleteOrchestrator(username, orchestratorDeleteRequest, workspace);
        String route = orchestratorDeleteRequest.getLabels().getRoute();
        if(DSSCommonUtils.ENV_LABEL_VALUE_DEV.equals(route)){
            //删除生产中心工作流
            if(ObjectUtil.isNotEmpty(orchestratorInfo)) {
                LabelRouteVO labelRouteVO = new LabelRouteVO();
                labelRouteVO.setRoute(DSSCommonUtils.ENV_LABEL_VALUE_PROD);
                RequestOrcDelete deleteOrchestrator = new RequestOrcDelete(username, workspace.getWorkspaceId(),
                        orchestratorDeleteRequest.getProjectId(), orchestratorInfo.getUUID(), workspace, DSSCommonUtils.ENV_LABEL_VALUE_PROD, orchestratorInfo.getName());
                DSSSenderServiceFactory.getOrCreateServiceInstance().getScheduleOrcSender().ask(deleteOrchestrator);
            }
        }
        RequestDeleteOrchestrator deleteOrchestrator = new RequestDeleteOrchestrator(username, String.valueOf(workspace.getWorkspaceId()),
            String.valueOf(orchestratorDeleteRequest.getProjectId()), orchestratorDeleteRequest.getId(),orchestratorVo.getOrchestratorName(), new ArrayList<>(0));
        DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender().send(deleteOrchestrator);
        return orchestratorVo;
    }
}
