package com.webank.wedatasphere.dss.workflow.service.impl;

import com.webank.wedatasphere.dss.workflow.dao.ProjectOrchestratorWhiteMapper;
import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;
import com.webank.wedatasphere.dss.workflow.service.ProjectOrchestratorWhiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectOrchestratorWhiteServiceImpl implements ProjectOrchestratorWhiteService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProjectOrchestratorWhiteMapper projectOrchestratorWhiteMapper;




    @Override
    public boolean checkProjectAndOrchestratorIsWhite(Long projectId, Long orchestratorId) {


        if(projectId == null || orchestratorId == null){
            logger.info("projectId or orchestratorId is null, project is {}, orchestrator is {}", projectId, orchestratorId);
            return false;
        }

        ProjectOrchestratorWhite projectOrchestratorWhite = selectByProjectId(projectId,orchestratorId);

        logger.info("projectOrchestratorWhite is {}", projectOrchestratorWhite);

        return projectOrchestratorWhite != null;

    }



    @Override
    public ProjectOrchestratorWhite selectByProjectId(Long projectId, Long orchestratorId){
        return  projectOrchestratorWhiteMapper.selectByProjectId(projectId,orchestratorId);
    }

    @Override
    public void addProjectOrchestratorWhite(ProjectOrchestratorWhite projectOrchestratorWhite) {

        ProjectOrchestratorWhite  orchestratorWhite = projectOrchestratorWhiteMapper.selectByProjectIdAndOrchestratorId(projectOrchestratorWhite.getProjectId(),
                projectOrchestratorWhite.getOrchestratorId());

        if(orchestratorWhite == null){
            projectOrchestratorWhiteMapper.addProjectOrchestratorWhite(projectOrchestratorWhite);
        }else{

            projectOrchestratorWhite.setId(orchestratorWhite.getId());
            projectOrchestratorWhiteMapper.updateById(projectOrchestratorWhite);
        }

    }

    @Override
    public void deleteProjectOrchestratorWhite(Long projectId, Long orchestratorId) {
        projectOrchestratorWhiteMapper.deleteProjectOrchestratorWhite(projectId,orchestratorId);
    }

    @Override
    public void deleteProjectWhite(Long projectId) {
        projectOrchestratorWhiteMapper.deleteProjectWhite(projectId);
    }

    @Override
    public List<ProjectOrchestratorWhite> getWhiteListByProjectId(Long projectId) {
        return projectOrchestratorWhiteMapper.getWhiteListByProjectId(projectId);
    }

}
