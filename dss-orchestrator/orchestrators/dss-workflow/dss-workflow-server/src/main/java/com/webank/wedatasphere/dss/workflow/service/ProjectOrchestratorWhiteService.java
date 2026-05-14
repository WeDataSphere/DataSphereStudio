package com.webank.wedatasphere.dss.workflow.service;

import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;

import java.util.List;

public interface ProjectOrchestratorWhiteService {

    public boolean checkProjectAndOrchestratorIsWhite(Long projectId,Long orchestratorId);


    public ProjectOrchestratorWhite selectByProjectId(Long projectId, Long orchestratorId);


    public void addProjectOrchestratorWhite(ProjectOrchestratorWhite projectOrchestratorWhite);


    void deleteProjectOrchestratorWhite(Long projectId, Long orchestratorId);


    void deleteProjectWhite(Long projectId);

    List<ProjectOrchestratorWhite> getWhiteListByProjectId(Long projectId);

    public boolean checkProjectAndOrchestratorIsWhiteWithType(Long projectId, Long orchestratorId, String type);

}
