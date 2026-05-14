package com.webank.wedatasphere.dss.workflow.dao;

import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectOrchestratorWhiteMapper {


    ProjectOrchestratorWhite selectByProjectId(@Param("projectId") Long projectId,@Param("orchestratorId") Long orchestratorId);



    void addProjectOrchestratorWhite(ProjectOrchestratorWhite projectOrchestratorWhite);


    void updateById(ProjectOrchestratorWhite projectOrchestratorWhite);


    void deleteProjectOrchestratorWhite(@Param("projectId") Long projectId,@Param("orchestratorId") Long orchestratorId);


    void deleteProjectWhite(@Param("projectId") Long projectId);


    List<ProjectOrchestratorWhite> getWhiteListByProjectId(@Param("projectId") Long projectId);


    ProjectOrchestratorWhite selectByProjectIdAndOrchestratorId(@Param("projectId") Long projectId,@Param("orchestratorId") Long orchestratorId);

    ProjectOrchestratorWhite selectByProjectIdAndType(@Param("projectId") Long projectId, @Param("orchestratorId") Long orchestratorId, @Param("type") String type);

}
