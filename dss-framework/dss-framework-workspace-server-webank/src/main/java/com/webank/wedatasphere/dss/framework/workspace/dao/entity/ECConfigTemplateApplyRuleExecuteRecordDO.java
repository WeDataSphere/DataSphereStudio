package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板应用规则执行记录表
 * @TableName dss_ec_config_template_apply_rule_execute_record
 */
public class ECConfigTemplateApplyRuleExecuteRecordDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 关联的参数模板名
     */
    private String templateName;

    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * 应用类型
     */
    private String application;

    /**
     * 执行状态：0未执行 1执行成功  2执行失败
     */
    private Integer status;

    /**
     * 执行人
     */
    private String executeUser;

    /**
     * 最近执行时间
     */
    private Date executeTime;

    private static final long serialVersionUID = 1L;

    public ECConfigTemplateApplyRuleExecuteRecordDO() {
    }

    public ECConfigTemplateApplyRuleExecuteRecordDO( String userName, String ruleId, Long workspaceId, String templateName, String engineType, String application, Integer status, String executeUser) {
        this.userName = userName;
        this.ruleId = ruleId;
        this.workspaceId = workspaceId;
        this.templateName = templateName;
        this.engineType = engineType;
        this.application = application;
        this.status = status;
        this.executeUser = executeUser;
    }

    /**
     * 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 规则id
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * 规则id
     */
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * 工作空间id
     */
    public Long getWorkspaceId() {
        return workspaceId;
    }

    /**
     * 工作空间id
     */
    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    /**
     * 关联的参数模板名
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * 关联的参数模板名
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * 引擎类型
     */
    public String getEngineType() {
        return engineType;
    }

    /**
     * 引擎类型
     */
    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    /**
     * 应用类型
     */
    public String getApplication() {
        return application;
    }

    /**
     * 应用类型
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * 执行状态：0未执行 1执行成功  2执行失败
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 执行状态：0未执行 1执行成功  2执行失败
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 执行人
     */
    public String getExecuteUser() {
        return executeUser;
    }

    /**
     * 执行人
     */
    public void setExecuteUser(String executeUser) {
        this.executeUser = executeUser;
    }

    /**
     * 最近执行时间
     */
    public Date getExecuteTime() {
        return executeTime;
    }

    /**
     * 最近执行时间
     */
    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ECConfigTemplateApplyRuleExecuteRecordDO other = (ECConfigTemplateApplyRuleExecuteRecordDO) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getRuleId() == null ? other.getRuleId() == null : this.getRuleId().equals(other.getRuleId()))
            && (this.getWorkspaceId() == null ? other.getWorkspaceId() == null : this.getWorkspaceId().equals(other.getWorkspaceId()))
            && (this.getTemplateName() == null ? other.getTemplateName() == null : this.getTemplateName().equals(other.getTemplateName()))
            && (this.getEngineType() == null ? other.getEngineType() == null : this.getEngineType().equals(other.getEngineType()))
            && (this.getApplication() == null ? other.getApplication() == null : this.getApplication().equals(other.getApplication()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getExecuteUser() == null ? other.getExecuteUser() == null : this.getExecuteUser().equals(other.getExecuteUser()))
            && (this.getExecuteTime() == null ? other.getExecuteTime() == null : this.getExecuteTime().equals(other.getExecuteTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getRuleId() == null) ? 0 : getRuleId().hashCode());
        result = prime * result + ((getWorkspaceId() == null) ? 0 : getWorkspaceId().hashCode());
        result = prime * result + ((getTemplateName() == null) ? 0 : getTemplateName().hashCode());
        result = prime * result + ((getEngineType() == null) ? 0 : getEngineType().hashCode());
        result = prime * result + ((getApplication() == null) ? 0 : getApplication().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getExecuteUser() == null) ? 0 : getExecuteUser().hashCode());
        result = prime * result + ((getExecuteTime() == null) ? 0 : getExecuteTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userName=").append(userName);
        sb.append(", ruleId=").append(ruleId);
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", templateName=").append(templateName);
        sb.append(", engineType=").append(engineType);
        sb.append(", application=").append(application);
        sb.append(", status=").append(status);
        sb.append(", executeUser=").append(executeUser);
        sb.append(", executeTime=").append(executeTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}