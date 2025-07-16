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

import java.util.ArrayList;
import java.util.List;

public class ProjectListQueryResponse {

    private List<DSSProject> projectList;


    // ！！！ 新增字段 也不要更改这个构造函数
    public ProjectListQueryResponse(List<DSSProject> projectList) {
        this.projectList = projectList;
    }

    public ProjectListQueryResponse() {
    }

    public List<DSSProject> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<DSSProject> projectList) {
        this.projectList = projectList;
    }
}
