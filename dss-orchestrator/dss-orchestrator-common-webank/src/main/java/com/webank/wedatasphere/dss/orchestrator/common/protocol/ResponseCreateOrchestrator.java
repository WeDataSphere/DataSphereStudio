package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorVo;

public class ResponseCreateOrchestrator {

    private OrchestratorVo  orchestratorVo;


    public ResponseCreateOrchestrator(OrchestratorVo orchestratorVo) {
        this.orchestratorVo = orchestratorVo;
    }

    public OrchestratorVo getOrchestratorVo() {
        return orchestratorVo;
    }

    public void setOrchestratorVo(OrchestratorVo orchestratorVo) {
        this.orchestratorVo = orchestratorVo;
    }
}
