package com.webank.wedatasphere.dss.framework.workspace.bean;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleUserDO;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 参数模板应用规则实体
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class ECConfTemplateApplyRule {
    public  static int STATE_NEW=0;
    public  static int STATE_SUCCESS=1;
    public  static int STATE_FAIL=2;
    public  static int STATE_PART_FAIL=3;

    /**
     * 临时规则
     */
    public static int RULE_TYPE_NORMAL=0;
    /**
     * 新用户规则
     */
    public static int RULE_TYPE_NEWUSER=1;



    public static int ALL_USER_PERMISSION_TYPE=0;
    public static int SPECIFIED_USER_PERMISSION_TYPE=1;
    public static int ONLY_NEW_USER_PERMISSION_TYPE=2;
    private String ruleId;
    private String engineName;
    private ECConfTemplate template;
    private int ruleType;
    private int permissionType;
    private List<PermissionUser> permissionUsers;
    private String application;
    private int status;
    private String creator;
    private Date createTime;
    private String executeUser;
    private Date executeTime;

    public ECConfTemplateApplyRule(ECConfTemplate template, int ruleType,String engineName, int permissionType, List<PermissionUser> permissionUsers, String application,String creator) {
        this.template = template;
        this.ruleType = ruleType;
        this.engineName=engineName;
        this.permissionType = permissionType;
        if(permissionType==SPECIFIED_USER_PERMISSION_TYPE) {
            this.permissionUsers = permissionUsers==null? Collections.emptyList():permissionUsers;
        }
        this.application = application;
        this.creator = creator;
        init();

    }

    private ECConfTemplateApplyRule(ECConfTemplate template, ECConfigTemplateApplyRuleDO ruleDO) {
        this.ruleId = ruleDO.getRuleId();
        this.template = template;
        this.ruleType = ruleDO.getRuleType();
        this.engineName = ruleDO.getEngineName();
        this.permissionType = ruleDO.getPermissionType();
        this.application = ruleDO.getApplication();
        this.status = ruleDO.getStatus();
        this.creator = ruleDO.getCreator();
        this.createTime = ruleDO.getCreateTime();
        this.executeUser = ruleDO.getExecuteUser();
        this.executeTime = ruleDO.getExecuteTime();
    }

    private void init(){
        ruleId = UUID.randomUUID().toString();
        status=STATE_NEW;
        createTime = new Date();
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public ECConfTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ECConfTemplate template) {
        this.template = template;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(String executeUser) {
        this.executeUser = executeUser;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public ECConfigTemplateApplyRuleDO toDO(Long workspaceId){
        ECConfigTemplateApplyRuleDO applyRuleDO = new ECConfigTemplateApplyRuleDO();
        BeanUtils.copyProperties(this, applyRuleDO);
        applyRuleDO.setTemplateId(template.getTemplateId());
        applyRuleDO.setEngineType(template.getEngineType());
        applyRuleDO.setTemplateName(template.getName());
        applyRuleDO.setWorkspaceId(workspaceId);
        return applyRuleDO;
    }
    public List<ECConfigTemplateApplyRuleUserDO> getUserDOList(Long workspaceId){
        if(permissionType!=SPECIFIED_USER_PERMISSION_TYPE){
            throw  new DSSRuntimeException("only specified user permission type has user list");
        }
        return
                permissionUsers.stream().map(user ->
                        new ECConfigTemplateApplyRuleUserDO(user.getName(), getRuleId(), workspaceId)
                ).collect(Collectors.toList());

    }

    public static ECConfTemplateApplyRule fromDO(ECConfigTemplateApplyRuleDO ruleDO,ECConfTemplate ecConfTemplate){
        return new ECConfTemplateApplyRule(ecConfTemplate, ruleDO);
    }
}
