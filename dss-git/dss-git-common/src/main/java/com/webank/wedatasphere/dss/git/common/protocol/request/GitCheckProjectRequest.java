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


public class GitCheckProjectRequest extends GitBaseRequest {
    /**
     * DSS用户名
     */
    private String username;
    private String gitUser;
    private String gitToken;



    public GitCheckProjectRequest() {
    }

    public GitCheckProjectRequest(String username) {
        this.username = username;
    }

    public GitCheckProjectRequest(Long workspaceId, String projectName, String username) {
        super(workspaceId, projectName);
        this.username = username;
    }

    public GitCheckProjectRequest(String username, String gitUser, String gitToken) {
        this.username = username;
        this.gitUser = gitUser;
        this.gitToken = gitToken;
    }

    public GitCheckProjectRequest(Long workspaceId, String projectName, String username, String gitUser, String gitToken) {
        super(workspaceId, projectName);
        this.username = username;
        this.gitUser = gitUser;
        this.gitToken = gitToken;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGitUser() {
        return gitUser;
    }

    public void setGitUser(String gitUser) {
        this.gitUser = gitUser;
    }

    public String getGitToken() {
        return gitToken;
    }

    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }
}
