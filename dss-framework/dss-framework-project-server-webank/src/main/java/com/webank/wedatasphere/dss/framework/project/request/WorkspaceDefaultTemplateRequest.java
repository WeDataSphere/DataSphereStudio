package com.webank.wedatasphere.dss.framework.project.request;

import java.util.Date;
import java.util.List;

public class WorkspaceDefaultTemplateRequest {

    private Long  workspaceId;

    private List<String> templateIds;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public List<String> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(List<String> templateIds) {
        this.templateIds = templateIds;
    }


    @Override
    public String toString() {
        return "WorkspaceDefaultTemplateRequest{" +
                "workspaceId=" + workspaceId +
                ", templateIds=" + templateIds +
                '}';
    }
}
