package com.webank.wedatasphere.dss.orchestrator.server.service;

import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.CommonOrchestratorVo;

import java.util.List;

public interface WebankOrchestratorService {

    List<OrchestratorDetail> getOrchestratorDetails(String username, Long projectId, String dssLabel, List<String> permissionUsers);

    String setScheduleFlow(String username, String projectName, int orchestratorId, String scheduleTime, String alarmEmails, String alarmLevel);

    //删除调度设置
    int deleteScheduleFlow(Long orchestratorId) throws Exception;

    //更新调度设置的调度标示
    int updateScheduleFlow(Long orchestratorId, String activeFlag)throws Exception;

    void setOrchestratorPriv(String username, int workspaceId, long projectID, String projectName, int orchestratorId, List<String> accessUsers, int priv);

    Long getOrcIsPublishFlag(Long projectId,String uuid);

    CommonOrchestratorVo deleteOrchestratorByLabel(String username, Long projectId, String uuid, Long workspaceId, Workspace workspace, String dssLabel, String orchestratorName) throws Exception;

}
