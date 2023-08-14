package com.webank.wedatasphere.dss.framework.compute.resource.manager.vo;

/**
 * 队列信息
 * Author: xlinliu
 * Date: 2023/4/17
 */
public class QueueInfo {
    private BaseInfo queuename;
    private Resource usedResources;
    private ResourcePercentage usedPercentage;
    private Resource maxResources;


    public BaseInfo getQueuename() {
        return queuename;
    }

    public void setQueuename(BaseInfo queuename) {
        this.queuename = queuename;
    }

    public Resource getUsedResources() {
        return usedResources;
    }

    public void setUsedResources(Resource usedResources) {
        this.usedResources = usedResources;
    }

    public ResourcePercentage getUsedPercentage() {
        return usedPercentage;
    }

    public void setUsedPercentage(ResourcePercentage usedPercentage) {
        this.usedPercentage = usedPercentage;
    }

    public Resource getMaxResources() {
        return maxResources;
    }

    public void setMaxResources(Resource maxResources) {
        this.maxResources = maxResources;
    }




    public static class BaseInfo {
        private String applicationId;
        private String queueName;
        private Long queueMemory;
        private Integer queueCores;
        private Integer queueInstances;

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public Long getQueueMemory() {
            return queueMemory;
        }

        public void setQueueMemory(Long queueMemory) {
            this.queueMemory = queueMemory;
        }

        public Integer getQueueCores() {
            return queueCores;
        }

        public void setQueueCores(Integer queueCores) {
            this.queueCores = queueCores;
        }

        public Integer getQueueInstances() {
            return queueInstances;
        }

        public void setQueueInstances(Integer queueInstances) {
            this.queueInstances = queueInstances;
        }
    }

    public static class Resource {
        private Long memory;
        private Integer cores;


        public Long getMemory() {
            return memory;
        }

        public void setMemory(Long memory) {
            this.memory = memory;
        }

        public Integer getCores() {
            return cores;
        }

        public void setCores(Integer cores) {
            this.cores = cores;
        }
    }
    public static class ResourcePercentage{
        private Double memory;
        private Double cores;

        public Double getMemory() {
            return memory;
        }

        public void setMemory(Double memory) {
            this.memory = memory;
        }

        public Double getCores() {
            return cores;
        }

        public void setCores(Double cores) {
            this.cores = cores;
        }
    }
}
