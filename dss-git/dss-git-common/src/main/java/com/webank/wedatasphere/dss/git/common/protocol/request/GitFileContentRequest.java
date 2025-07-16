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


import com.webank.wedatasphere.dss.common.entity.BmlResource;

import java.util.Map;

public class GitFileContentRequest extends GitBaseRequest{
    private String commitId;
    /**
     * 需获取内容的文件相对路径
     */
    private String filePath;

    private String username;

    private Boolean publish;

    public GitFileContentRequest() {
    }

    public GitFileContentRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public GitFileContentRequest(String commitId, String filePath, String username, Boolean publish) {
        this.commitId = commitId;
        this.filePath = filePath;
        this.username = username;
        this.publish = publish;
    }

    public GitFileContentRequest(Long workspaceId, String projectName, String commitId, String filePath, String username, Boolean publish) {
        super(workspaceId, projectName);
        this.commitId = commitId;
        this.filePath = filePath;
        this.username = username;
        this.publish = publish;
    }


    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
