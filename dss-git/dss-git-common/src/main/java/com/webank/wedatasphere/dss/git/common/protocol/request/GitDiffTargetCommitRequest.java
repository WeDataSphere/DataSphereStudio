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

public class GitDiffTargetCommitRequest extends GitBaseRequest{
    private String commitId;
    private String username;
    private String filePath;

    public GitDiffTargetCommitRequest(String commitId, String username, String filePath) {
        this.commitId = commitId;
        this.username = username;
        this.filePath = filePath;
    }

    public GitDiffTargetCommitRequest(Long workspaceId, String projectName, String commitId, String username, String filePath) {
        super(workspaceId, projectName);
        this.commitId = commitId;
        this.username = username;
        this.filePath = filePath;
    }

    public GitDiffTargetCommitRequest() {
    }

    public GitDiffTargetCommitRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
