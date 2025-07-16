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
package com.webank.wedatasphere.dss.orchestrator.server.entity.request;

import java.util.List;

public class EncryptCopyOrchestratorRequest {

    private Long workspaceId;

    private Long projectId;

    private Long orchestratorId;

    private List<String> enableNodeIdList;

    private String username;

    // copy工作流后缀
    private String copyFlowSuffix;

    // 节点后缀
    private String copyNodeSuffix;

    // 查看用户
    private String accessUser;

    private String flowProxyUser;

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


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCopyFlowSuffix() {
        return copyFlowSuffix;
    }

    public void setCopyFlowSuffix(String copyFlowSuffix) {
        this.copyFlowSuffix = copyFlowSuffix;
    }

    public List<String> getEnableNodeIdList() {
        return enableNodeIdList;
    }

    public void setEnableNodeIdList(List<String> enableNodeIdList) {
        this.enableNodeIdList = enableNodeIdList;
    }

    public String getCopyNodeSuffix() {
        return copyNodeSuffix;
    }

    public void setCopyNodeSuffix(String copyNodeSuffix) {
        this.copyNodeSuffix = copyNodeSuffix;
    }

    public String getAccessUser() {
        return accessUser;
    }

    public void setAccessUser(String accessUser) {
        this.accessUser = accessUser;
    }

    public String getFlowProxyUser() {
        return flowProxyUser;
    }

    public void setFlowProxyUser(String flowProxyUser) {
        this.flowProxyUser = flowProxyUser;
    }

    @Override
    public String toString() {
        return "EncryptCopyOrchestratorRequest{" +
                "workspaceId=" + workspaceId +
                ", projectId=" + projectId +
                ", orchestratorId=" + orchestratorId +
                ", enableNodeIdList=" + enableNodeIdList +
                ", username='" + username + '\'' +
                ", copyFlowSuffix='" + copyFlowSuffix + '\'' +
                ", copyNodeSuffix='" + copyNodeSuffix + '\'' +
                ", accessUser='" + accessUser + '\'' +
                ", flowProxyUser='" + flowProxyUser + '\'' +
                '}';
    }
}
