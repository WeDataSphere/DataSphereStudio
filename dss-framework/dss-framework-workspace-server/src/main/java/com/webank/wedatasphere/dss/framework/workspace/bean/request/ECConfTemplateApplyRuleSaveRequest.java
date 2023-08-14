package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import com.webank.wedatasphere.dss.framework.workspace.bean.PermissionUser;

import java.util.List;

/**
 * 保存执行规则的request
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class ECConfTemplateApplyRuleSaveRequest {
    private int ruleType;
    private String engineName;
    private String templateId;
    private int permissionType;
    private List<PermissionUser> permissionUsers;
    private String application;

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(int permissionType) {
        this.permissionType = permissionType;
    }

    public List<PermissionUser> getPermissionUsers() {
        return permissionUsers;
    }

    public void setPermissionUsers(List<PermissionUser> permissionUsers) {
        this.permissionUsers = permissionUsers;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
