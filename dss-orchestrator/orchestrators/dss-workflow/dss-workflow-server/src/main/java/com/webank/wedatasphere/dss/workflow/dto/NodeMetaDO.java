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
package com.webank.wedatasphere.dss.workflow.dto;


public class NodeMetaDO {
    private Long id;
    private Long orchestratorId;
    private String proxyUser;
    private String metaResource;
    private String globalVar;

    public NodeMetaDO() {
    }

    public NodeMetaDO(Long id, Long orchestratorId, String proxyUser, String metaResource, String globalVar) {
        this.id = id;
        this.orchestratorId = orchestratorId;
        this.proxyUser = proxyUser;
        this.metaResource = metaResource;
        this.globalVar = globalVar;
    }

    public NodeMetaDO(Long orchestratorId, String proxyUser, String metaResource, String globalVar) {
        this.orchestratorId = orchestratorId;
        this.proxyUser = proxyUser;
        this.metaResource = metaResource;
        this.globalVar = globalVar;
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

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getMetaResource() {
        return metaResource;
    }

    public void setMetaResource(String metaResource) {
        this.metaResource = metaResource;
    }

    public String getGlobalVar() {
        return globalVar;
    }

    public void setGlobalVar(String globalVar) {
        this.globalVar = globalVar;
    }
}
