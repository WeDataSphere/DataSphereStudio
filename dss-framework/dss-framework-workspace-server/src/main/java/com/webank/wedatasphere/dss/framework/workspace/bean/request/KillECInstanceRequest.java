package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceKillRequest;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2022/11/29
 */
public class KillECInstanceRequest {
    /**
     * 引擎名
     */
    private List<ECInstanceKillRequest> instances;

    public List<ECInstanceKillRequest> getInstances() {
        return instances;
    }

    public void setInstances(List<ECInstanceKillRequest> instances) {
        this.instances = instances;
    }
}