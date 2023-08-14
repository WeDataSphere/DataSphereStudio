package com.webank.wedatasphere.dss.framework.workspace.bean;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleExecuteRecordDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 模板规则执行记录
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class ECTemplateApplyRecord {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 规则id
     */
    private String ruleId;


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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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

    public static ECTemplateApplyRecord fromDO(ECConfigTemplateApplyRuleExecuteRecordDO recordDO){
        ECTemplateApplyRecord record = new ECTemplateApplyRecord();
        BeanUtils.copyProperties(recordDO, record);
        return record;
    }
}
