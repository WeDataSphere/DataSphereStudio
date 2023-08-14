package com.webank.wedatasphere.dss.orchestrator.common.protocol;


import java.io.Serializable;

public class RequestPublishUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orchestratorId;

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }
}
