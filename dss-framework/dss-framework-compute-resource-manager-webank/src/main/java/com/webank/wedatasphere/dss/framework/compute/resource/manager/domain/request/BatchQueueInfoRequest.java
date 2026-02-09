package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request;

import java.util.List;

/**
 * 批量获取队列信息请求
 * Author: xlinliu
 * Date: 2025/02/02
 */
public class BatchQueueInfoRequest {
    /**
     * 队列名列表
     */
    private List<String> queueNames;
    /**
     * 是否跨集群
     */
    private Boolean crossCluster;

    public BatchQueueInfoRequest() {
    }

    public BatchQueueInfoRequest(List<String> queueNames, Boolean crossCluster) {
        this.queueNames = queueNames;
        this.crossCluster = crossCluster;
    }

    public List<String> getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public Boolean getCrossCluster() {
        return crossCluster;
    }

    public void setCrossCluster(Boolean crossCluster) {
        this.crossCluster = crossCluster;
    }
}
