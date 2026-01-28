package com.webank.wedatasphere.dss.framework.project.entity.request;

import java.io.Serializable;

public class ProjectCopyStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long workspaceId;

    private Long copyProjectId;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getCopyProjectId() {
        return copyProjectId;
    }

    public void setCopyProjectId(Long copyProjectId) {
        this.copyProjectId = copyProjectId;
    }
}
