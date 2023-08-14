package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 参数模板表
 * @TableName dss_ec_config_template
 */
public class ECConfigTemplateDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 参数模板的uuid
     */
    private String templateId;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 参数模板名
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * 可见范围类型，0全部可见，1指定用户可见
     */
    private Integer permissionType;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 修改时间
     */
    private Date modifyTime;

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
     * 参数模板的uuid
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * 参数模板的uuid
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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
     * 参数模板名
     */
    public String getName() {
        return name;
    }

    /**
     * 参数模板名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 模板描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 模板描述
     */
    public void setDescription(String description) {
        this.description = description;
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
     * 可见范围类型，0全部可见，1指定用户可见
     */
    public Integer getPermissionType() {
        return permissionType;
    }

    /**
     * 可见范围类型，0全部可见，1指定用户可见
     */
    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
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
     * 修改人
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * 修改人
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     * 修改时间
     */
    public Date getModifyTime() {
        return modifyTime;
    }

    /**
     * 修改时间
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
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
        ECConfigTemplateDO other = (ECConfigTemplateDO) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTemplateId() == null ? other.getTemplateId() == null : this.getTemplateId().equals(other.getTemplateId()))
            && (this.getWorkspaceId() == null ? other.getWorkspaceId() == null : this.getWorkspaceId().equals(other.getWorkspaceId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getEngineType() == null ? other.getEngineType() == null : this.getEngineType().equals(other.getEngineType()))
            && (this.getPermissionType() == null ? other.getPermissionType() == null : this.getPermissionType().equals(other.getPermissionType()))
            && (this.getCreator() == null ? other.getCreator() == null : this.getCreator().equals(other.getCreator()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getModifier() == null ? other.getModifier() == null : this.getModifier().equals(other.getModifier()))
            && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTemplateId() == null) ? 0 : getTemplateId().hashCode());
        result = prime * result + ((getWorkspaceId() == null) ? 0 : getWorkspaceId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getEngineType() == null) ? 0 : getEngineType().hashCode());
        result = prime * result + ((getPermissionType() == null) ? 0 : getPermissionType().hashCode());
        result = prime * result + ((getCreator() == null) ? 0 : getCreator().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getModifier() == null) ? 0 : getModifier().hashCode());
        result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", templateId=").append(templateId);
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", name=").append(name);
        sb.append(", description=").append(description);
        sb.append(", engineType=").append(engineType);
        sb.append(", permissionType=").append(permissionType);
        sb.append(", creator=").append(creator);
        sb.append(", createTime=").append(createTime);
        sb.append(", modifier=").append(modifier);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}