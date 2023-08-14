package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.util.Date;

/**
 * 引擎释放规则DO对象
 * Author: xlinliu
 * Date: 2023/4/17
 */
public class ECReleaseStrategyDO {
    private String strategyId;
    private Long workspaceId;
    private String name;
    private String description;
    private String queue;
    private String triggerConditionConf;
    private String terminateConditionConf;
    private String imsConf;
    private String creator;
    private String modifier;
    private Date createTime;
    private Date modifyTime;

    private int status;

    /**
     * 执行实例
     */
    private String executeInstance;
    /**
     * 执行时间
     */
    private Date executeTime;

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getTriggerConditionConf() {
        return triggerConditionConf;
    }

    public void setTriggerConditionConf(String triggerConditionConf) {
        this.triggerConditionConf = triggerConditionConf;
    }

    public String getTerminateConditionConf() {
        return terminateConditionConf;
    }

    public void setTerminateConditionConf(String terminateConditionConf) {
        this.terminateConditionConf = terminateConditionConf;
    }

    public String getImsConf() {
        return imsConf;
    }

    public void setImsConf(String imsConf) {
        this.imsConf = imsConf;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getExecuteInstance() {
        return executeInstance;
    }

    public void setExecuteInstance(String executeInstance) {
        this.executeInstance = executeInstance;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }
}
