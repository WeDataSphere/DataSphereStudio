package com.webank.wedatasphere.dss.workflow.entity.request;

public class UpdateTenantVariableRequest {

    private String orchestratorName;
    private String projectName;
    private String tenantValue;
    private String operator;

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTenantValue() {
        return tenantValue;
    }

    public void setTenantValue(String tenantValue) {
        this.tenantValue = tenantValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}