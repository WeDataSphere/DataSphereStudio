package com.webank.wedatasphere.dss.framework.project.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.framework.project.entity.request.RemoveFlowRequest;
import com.webank.wedatasphere.dss.framework.project.request.ForbiddenFlowRequest;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.util.List;


public interface DSSOrchestratorService {

    /**
     * 获取指定项目下的所有工作流。
     * 所有工作流都是最新版本
     */
    List<OrchestratorDetail> getOrchestratorsByLabel(String username, String dssLabel, Long projectId,Workspace workspace,boolean withScheduleInfo) throws DSSErrorException;

    /**
     * 调度设置
     */
    void scheduleFlow(String username, String projectName, int orchestratorId, String scheduleTime, String alarmEmails, String alarmLevel,Workspace workspace) throws Exception;

    /**
     * 设置调度权限
     * */
    void setOrchestratorPriv(String username, int workspaceId, long projectID,String projectName, int orchestratorId, List<String> accessUsers,int privModel,Workspace workspace) throws DSSErrorException;

    /**
     * 删除调度
     */
    boolean removeFlowSchedule(RemoveFlowRequest removeFlowRequest, String username,Workspace workspace) throws Exception;

    /**
     *设置调度
     */
    boolean setFlowSchedule(ForbiddenFlowRequest forbiddenFlowRequest, String username, Workspace workspace) throws Exception;

    /**
     * 获取空间下的所有工作流。
     * */
    List<OrchestratorDetail> getAllOrchestratorDetailsOfWorkspace(String username, Long workspaceId, String dssLabel,Workspace workspace) throws Exception;

    /**
     * 获取生产中心编排模式版本信息
     * */
    List<DSSOrchestratorVersion> getOrchestratorVersions(String username, Long projectId, Long orchestratorId, String dssLabel);


    /**
     * 获取HistoryUrl
     * */
    String getHistoryUrl(String schedulerName);

    /**
     * 获取scheduleHistoryUrl
     * */
    String getScheduleHistoryUrl(String schedulerName) throws Exception;
}
