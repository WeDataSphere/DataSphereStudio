package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;
import java.util.HashMap;


public class RequestExecutionHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long appId;

    private Long orchestratorId;

    private Integer projectId;

    private Integer workspaceId;

    private transient HashMap<String, Object> labels;

    private Integer currentPage;

    private Integer pageSize;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    public HashMap<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, Object> labels) {
        this.labels = labels;
    }
}
