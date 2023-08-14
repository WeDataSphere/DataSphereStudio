package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板应用规则表
 * @TableName dss_ec_config_template_apply_rule
 */
public class ECConfigTemplateApplyRuleDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 规则id
     */
    private String ruleId;
    private String engineName;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 规则类型，0为临时规则，1指定用户可见,2工作空间新用户
     */
    private Integer ruleType;

    /**
     * 关联的参数模板id
     */
    private String templateId;

    /**
     * 关联的参数模板名
     */
    private String templateName;

    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * 覆盖范围，0为全部工作空间用户，1指定用户，2为新用户
     */
    private Integer permissionType;

    /**
     * 应用类型
     */
    private String application;

    /**
     * 执行状态：0未执行 1执行成功  2执行失败 3部分失败
     */
    private Integer status;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 执行人
     */
    private String executeUser;

    /**
     * 最近执行时间
     */
    private Date executeTime;

    private static final long serialVersionUID = 1L;

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
     * 规则类型，0为临时规则，1指定用户可见
     */
    public Integer getRuleType() {
        return ruleType;
    }

    /**
     * 规则类型，0为临时规则，1指定用户可见
     */
    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    /**
     * 关联的参数模板id
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * 关联的参数模板id
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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
     * 覆盖范围，0为全部工作空间用户，1指定用户，2为新用户
     */
    public Integer getPermissionType() {
        return permissionType;
    }

    /**
     * 覆盖范围，0为全部工作空间用户，1指定用户，2为新用户
     */
    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
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
     * 执行状态：0未执行 1执行成功  2执行失败 3部分失败
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 执行状态：0未执行 1执行成功  2执行失败 3部分失败
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 创建人
     */
    public String getCreator() {
        return creator;
    }

    /**
     * 创建人
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
        ECConfigTemplateApplyRuleDO other = (ECConfigTemplateApplyRuleDO) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRuleId() == null ? other.getRuleId() == null : this.getRuleId().equals(other.getRuleId()))
            && (this.getWorkspaceId() == null ? other.getWorkspaceId() == null : this.getWorkspaceId().equals(other.getWorkspaceId()))
            && (this.getRuleType() == null ? other.getRuleType() == null : this.getRuleType().equals(other.getRuleType()))
            && (this.getTemplateId() == null ? other.getTemplateId() == null : this.getTemplateId().equals(other.getTemplateId()))
            && (this.getTemplateName() == null ? other.getTemplateName() == null : this.getTemplateName().equals(other.getTemplateName()))
            && (this.getEngineType() == null ? other.getEngineType() == null : this.getEngineType().equals(other.getEngineType()))
            && (this.getPermissionType() == null ? other.getPermissionType() == null : this.getPermissionType().equals(other.getPermissionType()))
            && (this.getApplication() == null ? other.getApplication() == null : this.getApplication().equals(other.getApplication()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getExecuteUser() == null ? other.getExecuteUser() == null : this.getExecuteUser().equals(other.getExecuteUser()))
            && (this.getExecuteTime() == null ? other.getExecuteTime() == null : this.getExecuteTime().equals(other.getExecuteTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRuleId() == null) ? 0 : getRuleId().hashCode());
        result = prime * result + ((getWorkspaceId() == null) ? 0 : getWorkspaceId().hashCode());
        result = prime * result + ((getRuleType() == null) ? 0 : getRuleType().hashCode());
        result = prime * result + ((getTemplateId() == null) ? 0 : getTemplateId().hashCode());
        result = prime * result + ((getTemplateName() == null) ? 0 : getTemplateName().hashCode());
        result = prime * result + ((getEngineType() == null) ? 0 : getEngineType().hashCode());
        result = prime * result + ((getPermissionType() == null) ? 0 : getPermissionType().hashCode());
        result = prime * result + ((getApplication() == null) ? 0 : getApplication().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreator() == null) ? 0 : getCreator().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
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
        sb.append(", ruleId=").append(ruleId);
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", ruleType=").append(ruleType);
        sb.append(", templateId=").append(templateId);
        sb.append(", templateName=").append(templateName);
        sb.append(", engineType=").append(engineType);
        sb.append(", permissionType=").append(permissionType);
        sb.append(", application=").append(application);
        sb.append(", status=").append(status);
        sb.append(", creator=").append(creator);
        sb.append(", createTime=").append(createTime);
        sb.append(", executeUser=").append(executeUser);
        sb.append(", executeTime=").append(executeTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}