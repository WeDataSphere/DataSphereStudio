package com.webank.wedatasphere.dss.framework.project.job;

import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectCopyTaskMapper;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.service.ExportService;
import com.webank.wedatasphere.dss.framework.release.service.ProjectService;
import com.webank.wedatasphere.dss.workflow.service.ProjectOrchestratorWhiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectCopyEnv {
    @Autowired
    private DSSProjectMapper dssProjectMapper;
    @Autowired
    private DSSProjectCopyTaskMapper dssProjectCopyTaskMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ExportService exportService;

    @Autowired
    private ProjectOrchestratorWhiteService projectOrchestratorWhiteService;

    public ExportService getExportService() {
        return exportService;
    }

    public DSSProjectMapper getDssProjectMapper() {
        return dssProjectMapper;
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public DSSProjectCopyTaskMapper getDssProjectCopyTaskMapper() {
        return dssProjectCopyTaskMapper;
    }


    public ProjectOrchestratorWhiteService getProjectOrchestratorWhiteService() {
        return projectOrchestratorWhiteService;
    }

    public void setProjectOrchestratorWhiteService(ProjectOrchestratorWhiteService projectOrchestratorWhiteService) {
        this.projectOrchestratorWhiteService = projectOrchestratorWhiteService;
    }
}
