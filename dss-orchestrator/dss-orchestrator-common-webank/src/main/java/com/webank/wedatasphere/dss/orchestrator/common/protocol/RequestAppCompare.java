package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;

public class RequestAppCompare implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long firstFlowId;

    private Long secondFlowId;


    public Long getFirstFlowId() {
        return firstFlowId;
    }

    public void setFirstFlowId(Long firstFlowId) {
        this.firstFlowId = firstFlowId;
    }

    public Long getSecondFlowId() {
        return secondFlowId;
    }

    public void setSecondFlowId(Long secondFlowId) {
        this.secondFlowId = secondFlowId;
    }
}
