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
package com.webank.wedatasphere.dss.workflow.common.protocol;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

public class RequestUnlockWorkflow {
    private String username;
    private Long flowId;
    private Boolean confirmDelete;

    private Workspace workspace;

    public RequestUnlockWorkflow(String username, Long flowId, boolean confirmDelete) {
        this.username = username;
        this.flowId = flowId;
        this.confirmDelete = confirmDelete;
    }

    public RequestUnlockWorkflow(String username, Long flowId, Boolean confirmDelete, Workspace workspace) {
        this.username = username;
        this.flowId = flowId;
        this.confirmDelete = confirmDelete;
        this.workspace = workspace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Boolean getConfirmDelete() {
        return confirmDelete;
    }

    public void setConfirmDelete(Boolean confirmDelete) {
        this.confirmDelete = confirmDelete;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
