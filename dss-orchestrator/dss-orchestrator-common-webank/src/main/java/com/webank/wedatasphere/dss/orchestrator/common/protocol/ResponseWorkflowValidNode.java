package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;

public class ResponseWorkflowValidNode implements Serializable {
    private Integer nodeCount;
    private Long orcVersionId;

    public ResponseWorkflowValidNode(Integer nodeCount, Long orcVersionId) {
        this.nodeCount = nodeCount;
        this.orcVersionId = orcVersionId;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Long getOrcVersionId() {
        return orcVersionId;
    }

    public void setOrcVersionId(Long orcVersionId) {
        this.orcVersionId = orcVersionId;
    }
}
