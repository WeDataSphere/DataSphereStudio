package com.webank.wedatasphere.dss.orchestrator.server.service;

import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;

import java.util.Map;

public interface AddOrchestratorVersionHook {

    void beforeAdd(DSSOrchestratorVersion oldVersion, Map<String, Object> content);

    void afterAdd(DSSOrchestratorVersion newVersion, Map<String, Object> content);
}
