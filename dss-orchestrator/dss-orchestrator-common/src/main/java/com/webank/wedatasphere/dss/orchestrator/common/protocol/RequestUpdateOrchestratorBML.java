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
package com.webank.wedatasphere.dss.orchestrator.common.protocol;


import com.webank.wedatasphere.dss.common.entity.BmlResource;

public class RequestUpdateOrchestratorBML {
    private Long flowId;
    private BmlResource bmlResource;

    public RequestUpdateOrchestratorBML() {
    }

    public RequestUpdateOrchestratorBML(Long flowId, BmlResource bmlResource) {
        this.flowId = flowId;
        this.bmlResource = bmlResource;
    }


    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public BmlResource getBmlResource() {
        return bmlResource;
    }

    public void setBmlResource(BmlResource bmlResource) {
        this.bmlResource = bmlResource;
    }
}
