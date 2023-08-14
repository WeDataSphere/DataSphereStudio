package com.webank.wedatasphere.dss.framework.project.service;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;

import java.util.List;

public interface ScheduleService {

    //to-do 获取调度信息接口没有好
    List<RefOrchestrationExecutionInfoResponseRef.Execution> getSchedulerInfoContent(String username, String projectName, String orcName, Workspace workspace);

    void scheduleFlow(String username, String projectName, String orchestratorName, String scheduleTime,
                             String alarmEmails, String alarmLevel, Workspace workspace) throws Exception;

    /**
     * 删除调度
     * @param refProjectId
     * @param orchestratorName
     * @param username
     * @param workspace
     * @return
     * @throws Exception
     */
    boolean removeFlowSchedule(Long refProjectId, String orchestratorName, String username, Workspace workspace);

    /**
     * 禁用或启用调度
     * @return
     * @throws Exception
     */
    boolean setWorkflowActiveFlag(String activeFlag, Long refProjectId, String orchestratorName, String username, Workspace workspace);

//    /**
//     * 校验运维用户是否存在WTSS
//     * @param releaseUsers
//     * @param projectName
//     * @param username
//     * @param workspace
//     */
//    public void validReleaseUserExistWtss(List<String> releaseUsers,String createBy,String projectName, String username, Workspace workspace)throws Exception;
}
