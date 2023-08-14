package com.webank.wedatasphere.dss.orchestrator.common.protocol;

public class RequestReleaseOrchestrationStatus {

    private String id;

    public RequestReleaseOrchestrationStatus() {
    }

    public RequestReleaseOrchestrationStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

