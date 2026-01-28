package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;

/**
 * Author: xlinliu
 * Date: 2023/12/20
 */
public class RequestAppWithSubFlowCompare implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long oldFlowId;

    private Long newFlowId;

    public RequestAppWithSubFlowCompare() {
    }

    public RequestAppWithSubFlowCompare(Long oldFlowId, Long newFlowId) {
        this.oldFlowId = oldFlowId;
        this.newFlowId = newFlowId;
    }

    public Long getOldFlowId() {
        return oldFlowId;
    }

    public void setOldFlowId(Long oldFlowId) {
        this.oldFlowId = oldFlowId;
    }

    public Long getNewFlowId() {
        return newFlowId;
    }

    public void setNewFlowId(Long newFlowId) {
        this.newFlowId = newFlowId;
    }
}
