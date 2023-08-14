/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License; Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing; software
 *  * distributed under the License is distributed on an "AS IS" BASIS;
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;
import java.util.List;

public class RequestOrcSchedulePriv implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private int workspaceId;
    private long projectID;
    private String projectName;
    private int orchestratorId;
    private List<String> accessUsers;
    private int priv;

    public RequestOrcSchedulePriv(String username, int workspaceId, long projectID, String projectName, int orchestratorId, List<String> accessUsers, int priv) {
        this.username = username;
        this.workspaceId = workspaceId;
        this.projectID = projectID;
        this.projectName = projectName;
        this.orchestratorId = orchestratorId;
        this.accessUsers = accessUsers;
        this.priv = priv;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public long getProjectID() {
        return projectID;
    }

    public void setProjectID(long projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(int orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public List<String> getAccessUsers() {
        return accessUsers;
    }

    public void setAccessUsers(List<String> accessUsers) {
        this.accessUsers = accessUsers;
    }

    public int getPriv() {
        return priv;
    }

    public void setPriv(int priv) {
        this.priv = priv;
    }

    @Override
    public String toString() {
        return "RequestProdOrcSchedulePriv{" +
                "username='" + username + '\'' +
                ", workspaceId=" + workspaceId +
                ", projectID=" + projectID +
                ", projectName='" + projectName + '\'' +
                ", orchestratorId=" + orchestratorId +
                ", accessUsers=" + accessUsers +
                ", priv=" + priv +
                '}';
    }
}
