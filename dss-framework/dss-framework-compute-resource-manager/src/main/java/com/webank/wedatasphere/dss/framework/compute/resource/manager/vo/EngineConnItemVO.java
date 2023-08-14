package com.webank.wedatasphere.dss.framework.compute.resource.manager.vo;

import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ECInstance;
import org.springframework.beans.BeanUtils;

/**
 * 工作空间引擎实体VO
 * Author: xlinliu
 * Date: 2022/11/22
 */
public class EngineConnItemVO {
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
     * 队列信息
     */
    private YarnInfo yarn;
    /**
     * 已使用的本地资源
     */
    private DriverInfo driver;

    /**
     * 创建者
     */
    private String creator;
    /**
     * 启动时间
     */
    private String createTime;

    public static EngineConnItemVO fromECInstannce(ECInstance ecInstance){
        EngineConnItemVO item =new EngineConnItemVO();
        BeanUtils.copyProperties(ecInstance,item);
        item.setCreator(ecInstance.getOwner());
        if(ecInstance.getUseResource()!=null&&ecInstance.getUseResource().getDriver()!=null) {
            EngineConnItemVO.DriverInfo driverInfo = new EngineConnItemVO.DriverInfo();
            BeanUtils.copyProperties(ecInstance.getUseResource().getDriver(), driverInfo);
            item.setDriver(driverInfo);
        }
        if(ecInstance.getUseResource()!=null&&ecInstance.getUseResource().getYarn()!=null) {
            EngineConnItemVO.YarnInfo yarnInfo = new EngineConnItemVO.YarnInfo();
            BeanUtils.copyProperties(ecInstance.getUseResource().getYarn(), yarnInfo);
            item.setYarn(yarnInfo);
        }
        return item;
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

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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
}
