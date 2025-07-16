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
package com.webank.wedatasphere.dss.workflow.entity;


public class StarRocksNodeInfo {

    private Long workspaceId;
    private Long projectId;
    private Long orchestratorId;
    private Long nodeContentId;
    private String nodeKey;
    private String nodeUiKey;
    private String nodeUiValue;
    private String nodeType;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Long getNodeContentId() {
        return nodeContentId;
    }

    public void setNodeContentId(Long nodeContentId) {
        this.nodeContentId = nodeContentId;
    }

    public String getNodeUiKey() {
        return nodeUiKey;
    }

    public void setNodeUiKey(String nodeUiKey) {
        this.nodeUiKey = nodeUiKey;
    }

    public String getNodeUiValue() {
        return nodeUiValue;
    }

    public void setNodeUiValue(String nodeUiValue) {
        this.nodeUiValue = nodeUiValue;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }
}
