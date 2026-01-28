package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request;

/**
 * kill引擎请求
 * Author: xlinliu
 * Date: 2022/11/28
 */
public class ECInstanceKillRequest {
    /**
     * 引擎名
     */
    private String engineInstance;
    /**
     * 引擎创建者
     */
    private String owner;

    public ECInstanceKillRequest() {
    }

    public ECInstanceKillRequest(String engineInstance, String owner) {
        this.engineInstance = engineInstance;
        this.owner = owner;
    }

    public String getEngineInstance() {
        return engineInstance;
    }

    public void setEngineInstance(String engineInstance) {
        this.engineInstance = engineInstance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
