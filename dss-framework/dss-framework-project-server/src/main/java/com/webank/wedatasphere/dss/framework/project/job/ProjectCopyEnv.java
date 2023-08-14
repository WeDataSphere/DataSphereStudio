package com.webank.wedatasphere.dss.framework.project.job;

import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectCopyTaskMapper;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.release.service.ExportService;
import com.webank.wedatasphere.dss.framework.release.service.ProjectService;
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

}
