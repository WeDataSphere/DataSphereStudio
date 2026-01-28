package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.webank.wedatasphere.dss.common.label.LabelRouteVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.*;
import com.webank.wedatasphere.dss.orchestrator.db.dao.WebankOrchestratorMapper;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorDeleteRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.CommonOrchestratorVo;
import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorFrameworkService;
import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorService;
import com.webank.wedatasphere.dss.orchestrator.server.service.WebankOrchestratorService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebankOrchestratorServiceImpl implements WebankOrchestratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebankOrchestratorServiceImpl.class);

    @Autowired
    private WebankOrchestratorMapper webankOrchestratorMapper;

    @Autowired
    private OrchestratorFrameworkService orchestratorFrameworkService;


    @Autowired
    private OrchestratorService orchestratorService;


    @Override
    public List<OrchestratorDetail> getOrchestratorDetails(String username, Long projectId, String dssLabel, List<String> permissionUsers) {
        LOGGER.info("{} ask the orc {} detail for projectId {}", dssLabel,username, projectId);
        List<OrchestratorDetail> dbList = webankOrchestratorMapper.getOrchestratorDetails(projectId);
        List<OrchestratorDetail> orchestratorDetails = new ArrayList<>(dbList);
        LOGGER.info("getOrchestratorDetails.size={}", orchestratorDetails.size());
        List<OrchestratorDetail> realDetails = new ArrayList<>();
        for (OrchestratorDetail orchestratorDetail : orchestratorDetails) {
            orchestratorDetail.setProjectId(projectId);
            DSSReleasedFlowVO.ScheduleInfo scheduleInfo = webankOrchestratorMapper.getScheduleInfo(orchestratorDetail.getOrchestratorId());
            orchestratorDetail.setScheduleInfo(scheduleInfo);
            if (scheduleInfo != null) {
                orchestratorDetail.setScheduleSettings(scheduleInfo.getScheduleTime());
                orchestratorDetail.setActiveFlag(new Boolean(scheduleInfo.getActiveFlag()));
            }else{
                orchestratorDetail.setActiveFlag(true);
            }
            List<OrchestratorUser> orchestratorUserList = webankOrchestratorMapper.getOrchestratorUserByOrcId(orchestratorDetail.getOrchestratorId());
            Integer privModel = null;
            List<String> privUsers = null;
            DSSReleasedFlowVO.FlowPriv flowPriv = new DSSReleasedFlowVO.FlowPriv();
            if (CollectionUtils.isNotEmpty(orchestratorUserList)) {
                privUsers = orchestratorUserList.stream().map(OrchestratorUser::getUsername).collect(Collectors.toList());
                privModel = orchestratorUserList.get(0).getPriv();
                flowPriv.setPrivModel(privModel);
                flowPriv.setUsernames(privUsers);
            } else {
                flowPriv.setPrivModel(privModel == null ? 1 : privModel);
                flowPriv.setUsernames(new ArrayList<>());
            }
            orchestratorDetail.setFlowPriv(flowPriv);
            String creator = orchestratorDetail.getCreator();
            //String lastUpdater = orchestratorDetail.getLastUpdater();
            if (username.equalsIgnoreCase(creator)) {
                realDetails.add(orchestratorDetail);
            } else {
                //私密
                //if (privModel != null && privModel != 0 && privUsers.contains(username)) {
                //    realDetails.add(orchestratorDetail);
                //}
                if (CollectionUtils.isNotEmpty(permissionUsers) && permissionUsers.contains(username)) {
                    realDetails.add(orchestratorDetail);
                }
            }
        }
        LOGGER.info("projectId is {}, retList is {} and class is {},permissionUsers is {}", projectId, realDetails, realDetails.getClass(), permissionUsers);
        return realDetails;
    }

    @Override
    public String setScheduleFlow(String username, String projectName, int orchestratorId, String scheduleTime, String alarmEmails, String alarmLevel) {
        webankOrchestratorMapper.deleteScheduleInfo(orchestratorId);
        webankOrchestratorMapper.setScheduleInfo(projectName, username, scheduleTime, alarmEmails, alarmLevel, orchestratorId);
        String orchestratorName = webankOrchestratorMapper.getOrchestratorNameById(orchestratorId);
        return orchestratorName;
    }

    @Override
    public int deleteScheduleFlow(Long orchestratorId) throws Exception{
        return webankOrchestratorMapper.deleteScheduleInfo(orchestratorId.intValue());
    }

    @Override
    public int updateScheduleFlow(Long orchestratorId, String activeFlag) throws Exception{
        return webankOrchestratorMapper.updateScheduleInfoActiveFlag(orchestratorId,activeFlag);
    }

    @Override
    public void setOrchestratorPriv(String username, int workspaceId, long projectID, String projectName, int orchestratorId, List<String> accessUsers,int priv) {
        Date date = new Date(System.currentTimeMillis());
        webankOrchestratorMapper.deleteAllOrchestratorPriv(workspaceId, projectID, orchestratorId);
        webankOrchestratorMapper.setOrchestratorPriv(workspaceId, projectID, orchestratorId, accessUsers, priv, date);
    }

    @Override
    public Long getOrcIsPublishFlag(Long projectId,String uuid) {
        Long id = webankOrchestratorMapper.getOrcIsPublishFlag(projectId, uuid);
        return id == null ? 0L : id;
    }

    @Override
    public CommonOrchestratorVo deleteOrchestratorByLabel(String username, Long projectId, String uuid, Long workspaceId, Workspace workspace, String dssLabel, String orchestratorName) throws Exception {
        LOGGER.info("{} begins to delete a orchestrator uuid: {}.", username, uuid);
        Long orchestratorId = webankOrchestratorMapper.getOrcIdByUuid(projectId, orchestratorName, uuid);
        if(orchestratorId!=null && orchestratorId>0) {
            OrchestratorDeleteRequest orchestratorDeleteRequest = new OrchestratorDeleteRequest();
            orchestratorDeleteRequest.setId(orchestratorId);
            orchestratorDeleteRequest.setWorkspaceId(workspaceId);
            orchestratorDeleteRequest.setProjectId(projectId);
            LabelRouteVO dssLabel1 = new LabelRouteVO();
            dssLabel1.setRoute(dssLabel);
            orchestratorDeleteRequest.setLabels(dssLabel1);
            return orchestratorFrameworkService.deleteOrchestrator(username, orchestratorDeleteRequest, workspace);
        }
        return null;
    }



}
