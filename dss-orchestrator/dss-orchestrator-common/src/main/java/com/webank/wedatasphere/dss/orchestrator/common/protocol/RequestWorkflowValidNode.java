package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;

public class RequestWorkflowValidNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orcId;

    private Long orcVersionId;

    public Long getOrcId() {
        return orcId;
    }

    public void setOrcId(Long orcId) {
        this.orcId = orcId;
    }

    public Long getOrcVersionId() {
        return orcVersionId;
    }

    public void setOrcVersionId(Long orcVersionId) {
        this.orcVersionId = orcVersionId;
    }
}
