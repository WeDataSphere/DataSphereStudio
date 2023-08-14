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

package com.webank.wedatasphere.dss.framework.project.request;

import java.io.Serializable;
import java.util.List;


public class ProductWorkflowPrivRequest implements Serializable {
    private Integer privModel;
    private List<String> accessUsers;
    private Integer orchestratorId;
    private String projectName;
    private Long projectID;
    private Integer workspaceId;

    public Integer getPrivModel() {
        return privModel;
    }

    public void setPrivModel(Integer privModel) {
        this.privModel = privModel;
    }

    public List<String> getAccessUsers() {
        return accessUsers;
    }

    public void setAccessUsers(List<String> accessUsers) {
        this.accessUsers = accessUsers;
    }

    public Integer getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Integer orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(Long projectID) {
        this.projectID = projectID;
    }

    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return "ProductWorkflowPrivRequest{" +
                "privModel=" + privModel +
                ", accessUsers=" + accessUsers +
                ", orchestratorId=" + orchestratorId +
                ", projectName='" + projectName + '\'' +
                ", projectID=" + projectID +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
