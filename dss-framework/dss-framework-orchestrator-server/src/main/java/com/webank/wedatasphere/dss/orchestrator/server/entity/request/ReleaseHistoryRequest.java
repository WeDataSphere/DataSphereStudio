package com.webank.wedatasphere.dss.orchestrator.server.entity.request;


import java.io.Serializable;
import java.util.HashMap;

public class ReleaseHistoryRequest implements Serializable {
    private Integer workspaceId;
    private Integer projectId;
    private Long orchestratorId;
    private transient HashMap<String, Object> labels;
    private Integer currentPage;
    private Integer pageSize;
    //发布人
    private String releaseUser;
    //发布开始时间
    private String startTime;
    //发布结束时间
    private String endTime;
    //描述
    private String comment;

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

    public HashMap<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, Object> labels) {
        this.labels = labels;
    }
    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ReleaseHistoryRequest{" +
                "workspaceId=" + workspaceId +
                ", projectId=" + projectId +
                ", orchestratorId=" + orchestratorId +
                ", labels=" + labels +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", releaseUser='" + releaseUser + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
