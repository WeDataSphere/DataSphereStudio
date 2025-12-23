package com.webank.wedatasphere.dss.workflow.dao;

import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectOrchestratorWhiteMapper {


    ProjectOrchestratorWhite selectByProjectId(@Param("projectId") Long projectId,@Param("orchestratorId") Long orchestratorId);



    void addProjectOrchestratorWhite(ProjectOrchestratorWhite projectOrchestratorWhite);


    void updateById(ProjectOrchestratorWhite projectOrchestratorWhite);

}
