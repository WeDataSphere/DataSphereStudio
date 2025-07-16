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

public class GitCommitInfoBetweenRequest extends GitBaseRequest{
    private String oldCommitId;
    private String newCommitId;
    private String dirName;

    public GitCommitInfoBetweenRequest(String oldCommitId, String newCommitId, String dirName) {
        this.oldCommitId = oldCommitId;
        this.newCommitId = newCommitId;
        this.dirName = dirName;
    }

    public GitCommitInfoBetweenRequest(Long workspaceId, String projectName, String oldCommitId, String newCommitId, String dirName) {
        super(workspaceId, projectName);
        this.oldCommitId = oldCommitId;
        this.newCommitId = newCommitId;
        this.dirName = dirName;
    }

    public GitCommitInfoBetweenRequest() {
    }

    public String getOldCommitId() {
        return oldCommitId;
    }

    public void setOldCommitId(String oldCommitId) {
        this.oldCommitId = oldCommitId;
    }

    public String getNewCommitId() {
        return newCommitId;
    }

    public void setNewCommitId(String newCommitId) {
        this.newCommitId = newCommitId;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }
}
