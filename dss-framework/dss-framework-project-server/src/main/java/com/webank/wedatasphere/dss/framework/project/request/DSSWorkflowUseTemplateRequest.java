package com.webank.wedatasphere.dss.framework.project.request;

import javax.validation.constraints.NotNull;

public class DSSWorkflowUseTemplateRequest {

    @NotNull
    private String templateId;

    private String projectName;

    private String orchestratorName;

    private Long workspaceId;

    @NotNull
    private Integer pageNow;

    @NotNull
    private Integer pageSize;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getPageNow() {
        return pageNow;
    }

    public void setPageNow(Integer pageNow) {
        this.pageNow = pageNow;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "DSSWorkflowUseTemplateRequest{" +
                "templateId='" + templateId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", orchestratorName='" + orchestratorName + '\'' +
                ", workspaceId=" + workspaceId +
                ", pageNow=" + pageNow +
                ", pageSize=" + pageSize +
                '}';
    }
}
