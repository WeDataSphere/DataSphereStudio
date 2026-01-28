package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

/**
 * 工作空间引擎实体VO
 * Author: xlinliu
 * Date: 2022/11/22
 */
public class ECInstance {
    /**
     * 引擎实例名
     */
    private String instance;
    /**
     * 引擎类型
     */
    private String engineType;
    /**
     * 引擎状态
     */
    private String instanceStatus;
    /**
     * 所用资源
     */
    private Resource useResource;

    /**
     * 创建者
     */
    private String owner;
    /**
     * 启动时间
     */
    private String createTime;
    /**
     * 毫秒时间戳
     */
    private Long lastUnlockTimestamp;

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

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Resource getUseResource() {
        return useResource;
    }

    public void setUseResource(Resource useResource) {
        this.useResource = useResource;
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

    public Long getLastUnlockTimestamp() {
        return lastUnlockTimestamp;
    }

    public void setLastUnlockTimestamp(Long lastUnlockTimestamp) {
        this.lastUnlockTimestamp = lastUnlockTimestamp;
    }

    public static class DriverInfo{
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
    public static class YarnInfo{
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
    public static class Resource{
        private YarnInfo yarn;
        private DriverInfo driver;

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
    }
}
