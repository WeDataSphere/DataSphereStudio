package com.webank.wedatasphere.dss.orchestrator.server.entity.request;

import java.io.Serializable;
import java.util.Map;

public class OrchestratorCompareRequest implements Serializable {

    private Long firstVersionId;

    private Long secondVersionId;

    private transient Map<String, Object> labels;

    public Long getFirstVersionId() {
        return firstVersionId;
    }

    public void setFirstVersionId(Long firstVersionId) {
        this.firstVersionId = firstVersionId;
    }

    public Long getSecondVersionId() {
        return secondVersionId;
    }

    public void setSecondVersionId(Long secondVersionId) {
        this.secondVersionId = secondVersionId;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "OrchestratorCompareRequest{" +
                "firstVersionId=" + firstVersionId +
                ", secondVersionId=" + secondVersionId +
                ", labels=" + labels +
                '}';
    }
}
