package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;

/**
 * kill引擎统计信息
 * @author v_kangkangyuan
 */
public class ECKillSumHistoryRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * EC创建者
     */
    private String owner;

    /**
     * 规则ID
     */
    private String strategyId;

    /**
     * 工作空间ID
     */
    private Long workspaceId;

    /**
     * 队列名
     */
    private String queue;

    /**
     * kill EC示例数
     */
    private Integer instances;

    /**
     * 总共释放yarn核数
     */
    private Integer yarnCores;

    /**
     * 释放内存总数
     */
    private Long yarnMemories;

    /**
     * 空闲时间总时长
     */
    private Long unlockDurations;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Integer getInstances() {
        return instances;
    }

    public void setInstances(Integer instances) {
        this.instances = instances;
    }

    public Integer getYarnCores() {
        return yarnCores;
    }

    public void setYarnCores(Integer yarnCores) {
        this.yarnCores = yarnCores;
    }

    public Long getYarnMemories() {
        return yarnMemories;
    }

    public void setYarnMemories(Long yarnMemories) {
        this.yarnMemories = yarnMemories;
    }

    public Long getUnlockDurations() {
        return unlockDurations;
    }

    public void setUnlockDurations(Long unlockDurations) {
        this.unlockDurations = unlockDurations;
    }
}
