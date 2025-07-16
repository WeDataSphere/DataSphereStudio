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

import java.util.List;

public class GitBatchCommitRequest extends GitBaseRequest{
    private String comment;
    private String username;
    private List<String> filePath;
    private BmlResource bmlResource;

    public GitBatchCommitRequest(String comment, String username, List<String> filePath, BmlResource bmlResource) {
        this.comment = comment;
        this.username = username;
        this.filePath = filePath;
        this.bmlResource = bmlResource;
    }

    public GitBatchCommitRequest(Long workspaceId, String projectName, String comment, String username, List<String> filePath, BmlResource bmlResource) {
        super(workspaceId, projectName);
        this.comment = comment;
        this.username = username;
        this.filePath = filePath;
        this.bmlResource = bmlResource;
    }

    public GitBatchCommitRequest() {
    }

    public GitBatchCommitRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFilePath() {
        return filePath;
    }

    public void setFilePath(List<String> filePath) {
        this.filePath = filePath;
    }

    public BmlResource getBmlResource() {
        return bmlResource;
    }

    public void setBmlResource(BmlResource bmlResource) {
        this.bmlResource = bmlResource;
    }
}
