package com.webank.wedatasphere.dss.framework.workspace.bean.vo;

import java.util.Date;

/**
 * 历史记录vo
 * Author: xlinliu
 * Date: 2023/4/28
 */
public class ECKillHistoryRecordVO {
    /**
     * 释放规则id
     */
    private String strategyId;

    /**
     * 工作空间id
     */
    private Long workspaceId;
    /**
     * 引擎实例名
     */
    private String instance;
    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * yarn资源
     */
    private YarnInfo yarn;
    /**
     * 本地资源
     */
    private DriverInfo driver;

    /**
     * 创建者
     */
    private String owner;
    /**
     * 启动时间
     */
    private String createTime;

    private String killer;
    private Date killTime;
    private String unlockDuration;

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

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public YarnInfo getYarn() {
        return yarn;
    }

    public void setYarn(YarnInfo yarn) {
        this.yarn = yarn;
    }

    public DriverInfo getDriver() {
        return driver;
    }

    public void setDriver(DriverInfo driver) {
        this.driver = driver;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getKiller() {
        return killer;
    }

    public void setKiller(String killer) {
        this.killer = killer;
    }

    public Date getKillTime() {
        return killTime;
    }

    public void setKillTime(Date killTime) {
        this.killTime = killTime;
    }

    public String getUnlockDuration() {
        return unlockDuration;
    }

    public void setUnlockDuration(String unlockDuration) {
        this.unlockDuration = unlockDuration;
    }

    public static class DriverInfo {
        private Integer cpu;
        private Integer instance;

        private String memory;

        public Integer getCpu() {
            return cpu;
        }

        public void setCpu(Integer cpu) {
            this.cpu = cpu;
        }

        public Integer getInstance() {
            return instance;
        }

        public void setInstance(Integer instance) {
            this.instance = instance;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(String memory) {
            this.memory = memory;
        }
    }

    public static class YarnInfo {
        private String queueName;

        private String queueMemory;
        private Integer instance;
        private Integer queueCpu;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getQueueMemory() {
            return queueMemory;
        }

        public void setQueueMemory(String queueMemory) {
            this.queueMemory = queueMemory;
        }

        public Integer getInstance() {
            return instance;
        }

        public void setInstance(Integer instance) {
            this.instance = instance;
        }

        public Integer getQueueCpu() {
            return queueCpu;
        }

        public void setQueueCpu(Integer queueCpu) {
            this.queueCpu = queueCpu;
        }
    }

}
