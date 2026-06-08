package com.webank.wedatasphere.dss.workflow.entity;

import java.util.Date;

public class TenantVariableLogEntry {

    private Long id;
    private Long orchestratorId;
    private String orchestratorName;
    private Long projectId;
    private String projectName;
    private String oldTenantValue;
    private String newTenantValue;
    private String operator;
    private String status;
    private String errorMessage;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOldTenantValue() {
        return oldTenantValue;
    }

    public void setOldTenantValue(String oldTenantValue) {
        this.oldTenantValue = oldTenantValue;
    }

    public String getNewTenantValue() {
        return newTenantValue;
    }

    public void setNewTenantValue(String newTenantValue) {
        this.newTenantValue = newTenantValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
