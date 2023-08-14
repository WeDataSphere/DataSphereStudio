package com.webank.wedatasphere.dss.framework.workspace.bean.vo;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class ECConfTemplateApplyRuleVO {
    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 规则类型，0为临时规则，1指定用户可见
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
    private String engineName;

    /**
     * 覆盖范围，0为全部工作空间用户，1指定用户，2为新用户
     */
    private Integer permissionType;

    private Long permissionUserCount;

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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    public Long getPermissionUserCount() {
        return permissionUserCount;
    }

    public void setPermissionUserCount(Long permissionUserCount) {
        this.permissionUserCount = permissionUserCount;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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


    public static ECConfTemplateApplyRuleVO fromDO(ECConfigTemplateApplyRuleDO ruleDO){
        ECConfTemplateApplyRuleVO vo = new ECConfTemplateApplyRuleVO();
        BeanUtils.copyProperties(ruleDO, vo);
        return vo;
    }
}
