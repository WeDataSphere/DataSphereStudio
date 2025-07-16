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
package com.webank.wedatasphere.dss.git.common.protocol.request;

public class GitAddMemberRequest extends GitBaseRequest{
    private String username;
    private String flowNodeName;

    public GitAddMemberRequest(String username, String flowNodeName) {
        this.username = username;
        this.flowNodeName = flowNodeName;
    }

    public GitAddMemberRequest(Long workspaceId, String projectName, String username, String flowNodeName) {
        super(workspaceId, projectName);
        this.username = username;
        this.flowNodeName = flowNodeName;
    }

    public GitAddMemberRequest() {
    }

    public GitAddMemberRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFlowNodeName() {
        return flowNodeName;
    }

    public void setFlowNodeName(String flowNodeName) {
        this.flowNodeName = flowNodeName;
    }
}
