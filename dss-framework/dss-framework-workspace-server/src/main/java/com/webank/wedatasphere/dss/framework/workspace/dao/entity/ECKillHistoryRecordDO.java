package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 请求释放EC历史
 * @TableName dss_ec_kill_history
 */
public class ECKillHistoryRecordDO implements Serializable {


    /**
     * 释放规则id
     */
    private String strategyId;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 释放的EC实例名
     */
    private String instance;

    /**
     * EC类型
     */
    private String engineType;

    /**
     * 队列名
     */
    private String queue;

    /**
     * 本地释放核数
     */
    private Integer driverCore;

    /**
     * 本地释放内存，单位Byte
     */
    private Long driverMemory;

    /**
     * yarn释放核数
     */
    private Integer yarnCore;

    /**
     * yarn释放内存，单位Byte
     */
    private Long yarnMemory;

    /**
     * EC空闲时长,单位秒
     */
    private Long unlockDuration;

    /**
     * EC创建者
     */
    private String owner;

    /**
     * EC释放触发者
     */
    private String killer;

    /**
     * EC创建的时间
     */
    private String ecStartTime;

    /**
     * 请求killEC的时间
     */
    private Date killTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 负责发送的服务实例
     */
    private String executeInstance;

    private static final long serialVersionUID = 1L;


    /**
     * 释放规则id
     */
    public String getStrategyId() {
        return strategyId;
    }

    /**
     * 释放规则id
     */
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
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
     * 释放的EC实例名
     */
    public String getInstance() {
        return instance;
    }

    /**
     * 释放的EC实例名
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * EC类型
     */
    public String getEngineType() {
        return engineType;
    }

    /**
     * EC类型
     */
    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    /**
     * 队列名
     */
    public String getQueue() {
        return queue;
    }

    /**
     * 队列名
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * 本地释放核数
     */
    public Integer getDriverCore() {
        return driverCore;
    }

    /**
     * 本地释放核数
     */
    public void setDriverCore(Integer driverCore) {
        this.driverCore = driverCore;
    }

    /**
     * 本地释放内存，单位Byte
     */
    public Long getDriverMemory() {
        return driverMemory;
    }

    /**
     * 本地释放内存，单位Byte
     */
    public void setDriverMemory(Long driverMemory) {
        this.driverMemory = driverMemory;
    }

    /**
     * yarn释放核数
     */
    public Integer getYarnCore() {
        return yarnCore;
    }

    /**
     * yarn释放核数
     */
    public void setYarnCore(Integer yarnCore) {
        this.yarnCore = yarnCore;
    }

    /**
     * yarn释放内存，单位Byte
     */
    public Long getYarnMemory() {
        return yarnMemory;
    }

    /**
     * yarn释放内存，单位Byte
     */
    public void setYarnMemory(Long yarnMemory) {
        this.yarnMemory = yarnMemory;
    }

    /**
     * EC空闲时长,单位秒
     */
    public Long getUnlockDuration() {
        return unlockDuration;
    }

    /**
     * EC空闲时长,单位秒
     */
    public void setUnlockDuration(Long unlockDuration) {
        this.unlockDuration = unlockDuration;
    }

    /**
     * EC创建者
     */
    public String getOwner() {
        return owner;
    }

    /**
     * EC创建者
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * EC释放触发者
     */
    public String getKiller() {
        return killer;
    }

    /**
     * EC释放触发者
     */
    public void setKiller(String killer) {
        this.killer = killer;
    }

    /**
     * EC创建的时间
     */
    public String getEcStartTime() {
        return ecStartTime;
    }

    /**
     * EC创建的时间
     */
    public void setEcStartTime(String ecStartTime) {
        this.ecStartTime = ecStartTime;
    }

    /**
     * 请求killEC的时间
     */
    public Date getKillTime() {
        return killTime;
    }

    /**
     * 请求killEC的时间
     */
    public void setKillTime(Date killTime) {
        this.killTime = killTime;
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
     * 负责发送的服务实例
     */
    public String getExecuteInstance() {
        return executeInstance;
    }

    /**
     * 负责发送的服务实例
     */
    public void setExecuteInstance(String executeInstance) {
        this.executeInstance = executeInstance;
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
        ECKillHistoryRecordDO other = (ECKillHistoryRecordDO) that;
        return (this.getStrategyId() == null ? other.getStrategyId() == null : this.getStrategyId().equals(other.getStrategyId()))
            && (this.getWorkspaceId() == null ? other.getWorkspaceId() == null : this.getWorkspaceId().equals(other.getWorkspaceId()))
            && (this.getInstance() == null ? other.getInstance() == null : this.getInstance().equals(other.getInstance()))
            && (this.getEngineType() == null ? other.getEngineType() == null : this.getEngineType().equals(other.getEngineType()))
            && (this.getQueue() == null ? other.getQueue() == null : this.getQueue().equals(other.getQueue()))
            && (this.getDriverCore() == null ? other.getDriverCore() == null : this.getDriverCore().equals(other.getDriverCore()))
            && (this.getDriverMemory() == null ? other.getDriverMemory() == null : this.getDriverMemory().equals(other.getDriverMemory()))
            && (this.getYarnCore() == null ? other.getYarnCore() == null : this.getYarnCore().equals(other.getYarnCore()))
            && (this.getYarnMemory() == null ? other.getYarnMemory() == null : this.getYarnMemory().equals(other.getYarnMemory()))
            && (this.getUnlockDuration() == null ? other.getUnlockDuration() == null : this.getUnlockDuration().equals(other.getUnlockDuration()))
            && (this.getOwner() == null ? other.getOwner() == null : this.getOwner().equals(other.getOwner()))
            && (this.getKiller() == null ? other.getKiller() == null : this.getKiller().equals(other.getKiller()))
            && (this.getEcStartTime() == null ? other.getEcStartTime() == null : this.getEcStartTime().equals(other.getEcStartTime()))
            && (this.getKillTime() == null ? other.getKillTime() == null : this.getKillTime().equals(other.getKillTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getExecuteInstance() == null ? other.getExecuteInstance() == null : this.getExecuteInstance().equals(other.getExecuteInstance()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getStrategyId() == null) ? 0 : getStrategyId().hashCode());
        result = prime * result + ((getWorkspaceId() == null) ? 0 : getWorkspaceId().hashCode());
        result = prime * result + ((getInstance() == null) ? 0 : getInstance().hashCode());
        result = prime * result + ((getEngineType() == null) ? 0 : getEngineType().hashCode());
        result = prime * result + ((getQueue() == null) ? 0 : getQueue().hashCode());
        result = prime * result + ((getDriverCore() == null) ? 0 : getDriverCore().hashCode());
        result = prime * result + ((getDriverMemory() == null) ? 0 : getDriverMemory().hashCode());
        result = prime * result + ((getYarnCore() == null) ? 0 : getYarnCore().hashCode());
        result = prime * result + ((getYarnMemory() == null) ? 0 : getYarnMemory().hashCode());
        result = prime * result + ((getUnlockDuration() == null) ? 0 : getUnlockDuration().hashCode());
        result = prime * result + ((getOwner() == null) ? 0 : getOwner().hashCode());
        result = prime * result + ((getKiller() == null) ? 0 : getKiller().hashCode());
        result = prime * result + ((getEcStartTime() == null) ? 0 : getEcStartTime().hashCode());
        result = prime * result + ((getKillTime() == null) ? 0 : getKillTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getExecuteInstance() == null) ? 0 : getExecuteInstance().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", strategyId=").append(strategyId);
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", instance=").append(instance);
        sb.append(", engineType=").append(engineType);
        sb.append(", queue=").append(queue);
        sb.append(", driverCore=").append(driverCore);
        sb.append(", driverMemory=").append(driverMemory);
        sb.append(", yarnCore=").append(yarnCore);
        sb.append(", yarnMemory=").append(yarnMemory);
        sb.append(", unlockDuration=").append(unlockDuration);
        sb.append(", owner=").append(owner);
        sb.append(", killer=").append(killer);
        sb.append(", ecStartTime=").append(ecStartTime);
        sb.append(", killTime=").append(killTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", executeInstance=").append(executeInstance);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}