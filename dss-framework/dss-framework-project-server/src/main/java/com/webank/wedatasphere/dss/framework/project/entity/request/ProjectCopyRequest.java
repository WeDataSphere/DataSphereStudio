package com.webank.wedatasphere.dss.framework.project.entity.request;

import java.io.Serializable;

public class ProjectCopyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;

    private String copyProjectName;

    private Long workspaceId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getCopyProjectName() {
        return copyProjectName;
    }

    public void setCopyProjectName(String copyProjectName) {
        this.copyProjectName = copyProjectName;
    }

    @Override
    public String toString() {
        return "ProjectCopyRequest{" +
                "projectId=" + projectId +
                ", copyProjectName='" + copyProjectName + '\'' +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
