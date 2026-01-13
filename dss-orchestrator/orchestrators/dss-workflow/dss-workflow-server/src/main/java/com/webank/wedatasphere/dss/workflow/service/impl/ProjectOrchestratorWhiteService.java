package com.webank.wedatasphere.dss.workflow.service.impl;

import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;

public interface ProjectOrchestratorWhiteService {

    public boolean checkProjectAndOrchestratorIsWhite(Long projectId,Long orchestratorId);


    public ProjectOrchestratorWhite selectByProjectId(Long projectId, Long orchestratorId);


    public void addProjectOrchestratorWhite(ProjectOrchestratorWhite projectOrchestratorWhite);


    void deleteProjectOrchestratorWhite(Long projectId, Long orchestratorId);


    void deleteProjectWhite(Long projectId);

}
