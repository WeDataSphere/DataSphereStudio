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

import java.util.List;

public class ProjectUserAuthResponse {
    private Long projectId;
    private String userName;

    private List<Integer> privList;

    private String projectOwner;

    public ProjectUserAuthResponse(Long projectId, String userName, List<Integer> privList, String projectOwner) {
        this.projectId = projectId;
        this.userName = userName;
        this.privList = privList;
        this.projectOwner = projectOwner;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Integer> getPrivList() {
        return privList;
    }

    public void setPrivList(List<Integer> privList) {
        this.privList = privList;
    }

    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

}
