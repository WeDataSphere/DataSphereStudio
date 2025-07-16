/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.orchestrator.common.entity;

public class DSSOrchestratorRefOrchestration {

    private Long id;
    private Long orchestratorId;
    private Long refProjectId;
    private Long refOrchestrationId;

    public DSSOrchestratorRefOrchestration(Long orchestratorId, Long refProjectId, Long refOrchestrationId) {
        this.orchestratorId = orchestratorId;
        this.refProjectId = refProjectId;
        this.refOrchestrationId = refOrchestrationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Long getRefProjectId() {
        return refProjectId;
    }

    public void setRefProjectId(Long refProjectId) {
        this.refProjectId = refProjectId;
    }

    public Long getRefOrchestrationId() {
        return refOrchestrationId;
    }

    public void setRefOrchestrationId(Long refOrchestrationId) {
        this.refOrchestrationId = refOrchestrationId;
    }
}
