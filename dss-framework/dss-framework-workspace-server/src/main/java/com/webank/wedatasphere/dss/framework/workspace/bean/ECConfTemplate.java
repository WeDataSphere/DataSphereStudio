package com.webank.wedatasphere.dss.framework.workspace.bean;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * 资源参数配置模板实例
 * Author: xlinliu
 * Date: 2023/6/14
 */
public class ECConfTemplate {

    public static int ALL_USER_PERMISSION_TYPE=0;
    public static int SPECIFIED_USER_PERMISSION_TYPE=1;
    private String templateId;
    private String name;
    private String description;
    private String engineType;
    private List<ECConfItem> paramDetails;
    /**
     * 可见范围类型。
     * 0表示全部可见
     * 1表示指定用户可见
     */
    private int permissionType;
    private List<PermissionUser> permissionUsers;
    private Long permissionUserCount;
    private String creator;
    private Date createTime;
    private String modifier;
    private Date modifyTime;


    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public int getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(int permissionType) {
        this.permissionType = permissionType;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public List<ECConfItem> getParamDetails() {
        return paramDetails;
    }

    public void setParamDetails(List<ECConfItem> paramDetails) {
        this.paramDetails = paramDetails;
    }

    public List<PermissionUser> getPermissionUsers() {
        return permissionUsers;
    }

    public void setPermissionUsers(List<PermissionUser> permissionUsers) {
        this.permissionUsers = permissionUsers;
    }

    public Long getPermissionUserCount() {
        return permissionUserCount;
    }

    public void setPermissionUserCount(Long permissionUserCount) {
        this.permissionUserCount = permissionUserCount;
    }

    public static ECConfTemplate fromDO(ECConfigTemplateDO templateDO){
        ECConfTemplate template=new ECConfTemplate();
        BeanUtils.copyProperties(templateDO, template);
        return template;
    }
    public ECConfigTemplateDO toDO(Long workspaceId){
        ECConfigTemplateDO templateDO = new ECConfigTemplateDO();
        BeanUtils.copyProperties(this, templateDO);
        templateDO.setWorkspaceId(workspaceId);
        return templateDO;
    }

}
