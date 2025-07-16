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
package com.webank.wedatasphere.dss.orchestrator.server.entity.vo;

import java.util.List;

public class OrchestratorDiffNodeVo {

    private String orchestratorName;

    private Long orchestratorId;

    private String projectName;

    private Long projectId;

    private List<String> notContainsKeywordsNodeList;

    private String enabledFlowKeywordsCheck;

    private String workspaceName;

    private Long workspaceId;

    private Boolean  associateGit;


    public OrchestratorDiffNodeVo() {
    }


    public OrchestratorDiffNodeVo(String orchestratorName, Long orchestratorId, List<String> notContainsKeywordsNodeList) {
        this.orchestratorName = orchestratorName;
        this.orchestratorId = orchestratorId;
        this.notContainsKeywordsNodeList = notContainsKeywordsNodeList;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {

        this.orchestratorName = orchestratorName;
    }


    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public List<String> getNotContainsKeywordsNodeList() {
        return notContainsKeywordsNodeList;
    }

    public void setNotContainsKeywordsNodeList(List<String> notContainsKeywordsNodeList) {
        this.notContainsKeywordsNodeList = notContainsKeywordsNodeList;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }


    public String getEnabledFlowKeywordsCheck() {
        return enabledFlowKeywordsCheck;
    }

    public void setEnabledFlowKeywordsCheck(String enabledFlowKeywordsCheck) {
        this.enabledFlowKeywordsCheck = enabledFlowKeywordsCheck;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Boolean getAssociateGit() {
        return associateGit;
    }

    public void setAssociateGit(Boolean associateGit) {
        this.associateGit = associateGit;
    }
}
