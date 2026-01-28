/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RequestOrcDelete implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;
    private Long workspaceId;
    private Long projectId;
    private String uuid;

    protected Workspace workspace;
    private String dssLabel;
    private String orchestratorName;

    public RequestOrcDelete(String userName, Long workspaceId, Long projectId, String uuid, Workspace workspace, String dssLabel, String orchestratorName) {
        this.userName = userName;
        this.workspaceId = workspaceId;
        this.projectId = projectId;
        this.uuid = uuid;
        this.workspace = workspace;
        this.dssLabel = dssLabel;
        this.orchestratorName = orchestratorName;
    }

    public String getDssLabel() {
        return dssLabel;
    }

    public void setDssLabel(String dssLabel) {
        this.dssLabel = dssLabel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }
}
