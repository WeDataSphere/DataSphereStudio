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

import com.webank.wedatasphere.dss.common.entity.project.DSSProject;

import java.util.List;

public class ProjectInfoListResponse {
    private List<DSSProject> dssProjects;

    public ProjectInfoListResponse(List<DSSProject> dssProjects) {
        this.dssProjects = dssProjects;
    }

    public ProjectInfoListResponse() {
    }

    public List<DSSProject> getDssProjects() {
        return dssProjects;
    }

    public void setDssProjects(List<DSSProject> dssProjects) {
        this.dssProjects = dssProjects;
    }
}
