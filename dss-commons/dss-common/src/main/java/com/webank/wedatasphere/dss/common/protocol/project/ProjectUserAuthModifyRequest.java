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
package com.webank.wedatasphere.dss.common.protocol.project;

import com.webank.wedatasphere.dss.common.entity.DSSWorkspace;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

public class ProjectUserAuthModifyRequest {

    private Long projectId;

    private String projectName;

    private String accessUser;

    private String workspaceName;

    private Long workspaceId;

    private String username;

    private Map<String, String> cookies = new HashMap<>();
    private String dssUrl;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAccessUser() {
        return accessUser;
    }

    public void setAccessUser(String accessUser) {
        this.accessUser = accessUser;
    }


    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getDssUrl() {
        return dssUrl;
    }

    public void setDssUrl(String dssUrl) {
        this.dssUrl = dssUrl;
    }

    @Override
    public String toString() {
        return "ProjectUserAuthModifyRequest{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", accessUser='" + accessUser + '\'' +
                ", workspaceName='" + workspaceName + '\'' +
                ", workspaceId=" + workspaceId +
                ", username='" + username + '\'' +
                '}';
    }

}

