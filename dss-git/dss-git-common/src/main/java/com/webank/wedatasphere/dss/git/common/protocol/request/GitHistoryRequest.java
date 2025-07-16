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

public class GitHistoryRequest extends GitBaseRequest{
    private String filePath;

    public GitHistoryRequest() {
    }

    public GitHistoryRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public GitHistoryRequest(String filePath) {
        this.filePath = filePath;
    }

    public GitHistoryRequest(Long workspaceId, String projectName, String filePath) {
        super(workspaceId, projectName);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
