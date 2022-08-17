package com.webank.wedatasphere.dss.orchestrator.server.entity.vo;

import java.util.Date;
import java.util.List;

public class OrchestratorCopyHistory {

    private Long id;

    private String username;

    private String workspaceName;

    private String sourceOrchestratorName;

    private String targetOrchestratorName;

    private String sourceProjectName;

    private String targetProjectName;

    /**
     * 目标工作流节点后缀
     */
    private String workflowNodeSuffix;

    private String microserverName;

    private String exceptionInfo;

    /**
     * 复制任务最终状态，1成功，0失败
     */
    private Integer status;

    /**
     * 当前编排是否在被复制，1有，0没有
     */
    private Integer isCopying;

    /**
     * 复制开始时间
     */
    private Date startTime;

    /**
     * 复制结束时间
     */
    private Date endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getSourceOrchestratorName() {
        return sourceOrchestratorName;
    }

    public void setSourceOrchestratorName(String sourceOrchestratorName) {
        this.sourceOrchestratorName = sourceOrchestratorName;
    }

    public String getTargetOrchestratorName() {
        return targetOrchestratorName;
    }

    public void setTargetOrchestratorName(String targetOrchestratorName) {
        this.targetOrchestratorName = targetOrchestratorName;
    }

    public String getSourceProjectName() {
        return sourceProjectName;
    }

    public void setSourceProjectName(String sourceProjectName) {
        this.sourceProjectName = sourceProjectName;
    }

    public String getTargetProjectName() {
        return targetProjectName;
    }

    public void setTargetProjectName(String targetProjectName) {
        this.targetProjectName = targetProjectName;
    }

    public String getWorkflowNodeSuffix() {
        return workflowNodeSuffix;
    }

    public void setWorkflowNodeSuffix(String workflowNodeSuffix) {
        this.workflowNodeSuffix = workflowNodeSuffix;
    }

    public String getMicroserverName() {
        return microserverName;
    }

    public void setMicroserverName(String microserverName) {
        this.microserverName = microserverName;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public Integer isStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer isCopying() {
        return isCopying;
    }

    public void setCopying(Integer copying) {
        isCopying = copying;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "OrchestratorCopyHistory{" +
                "id=" + id +
                "username=" + username +
                ", workspaceName='" + workspaceName + '\'' +
                ", sourceOrchestratorName='" + sourceOrchestratorName + '\'' +
                ", targetOrchestratorName='" + targetOrchestratorName + '\'' +
                ", sourceProjectName='" + sourceProjectName + '\'' +
                ", targetProjectName='" + targetProjectName + '\'' +
                ", workflowNodeSuffix='" + workflowNodeSuffix + '\'' +
                ", microserverName='" + microserverName + '\'' +
                ", exceptionInfo='" + exceptionInfo + '\'' +
                ", status=" + status +
                ", isCopying=" + isCopying +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}