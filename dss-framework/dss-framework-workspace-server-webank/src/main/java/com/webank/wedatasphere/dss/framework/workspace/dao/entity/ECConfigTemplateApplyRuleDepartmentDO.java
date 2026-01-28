package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板应用规则覆盖用户表
 * @TableName dss_ec_config_template_apply_rule_department
 */
public class ECConfigTemplateApplyRuleDepartmentDO implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String departmentName;

    /**
     * 规则的uuid
     */
    private String ruleId;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

    public ECConfigTemplateApplyRuleDepartmentDO() {
    }

    public ECConfigTemplateApplyRuleDepartmentDO(String departmentName, String ruleId, Long workspaceId) {
        this.departmentName = departmentName;
        this.ruleId = ruleId;
        this.workspaceId = workspaceId;
    }

    /**
     * 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 用户名
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * 用户名
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    /**
     * 规则的uuid
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * 规则的uuid
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
        ECConfigTemplateApplyRuleDepartmentDO other = (ECConfigTemplateApplyRuleDepartmentDO) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDepartmentName() == null ? other.getDepartmentName() == null : this.getDepartmentName().equals(other.getDepartmentName()))
            && (this.getRuleId() == null ? other.getRuleId() == null : this.getRuleId().equals(other.getRuleId()))
            && (this.getWorkspaceId() == null ? other.getWorkspaceId() == null : this.getWorkspaceId().equals(other.getWorkspaceId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDepartmentName() == null) ? 0 : getDepartmentName().hashCode());
        result = prime * result + ((getRuleId() == null) ? 0 : getRuleId().hashCode());
        result = prime * result + ((getWorkspaceId() == null) ? 0 : getWorkspaceId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userName=").append(departmentName);
        sb.append(", ruleId=").append(ruleId);
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}