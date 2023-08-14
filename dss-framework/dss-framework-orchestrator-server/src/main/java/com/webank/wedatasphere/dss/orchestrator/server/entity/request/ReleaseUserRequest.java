package com.webank.wedatasphere.dss.orchestrator.server.entity.request;


import java.io.Serializable;
import java.util.HashMap;

public class ReleaseUserRequest implements Serializable {
    private Long orchestratorId;
    private transient HashMap<String, Object> labels;

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public HashMap<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, Object> labels) {
        this.labels = labels;
    }
}
