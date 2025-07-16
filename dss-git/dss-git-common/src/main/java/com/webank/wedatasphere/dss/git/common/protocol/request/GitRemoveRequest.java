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

import java.util.List;

public class GitRemoveRequest extends GitBaseRequest{
    private List<String> path;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;


    public GitRemoveRequest(List<String> path, String username) {
        this.path = path;
        this.username = username;
    }

    public GitRemoveRequest(Long workspaceId, String projectName, List<String> path, String username) {
        super(workspaceId, projectName);
        this.path = path;
        this.username = username;
    }

    public GitRemoveRequest() {
    }

    public GitRemoveRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }
}
