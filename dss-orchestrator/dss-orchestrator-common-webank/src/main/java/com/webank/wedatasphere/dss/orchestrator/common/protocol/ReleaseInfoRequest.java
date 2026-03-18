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

package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;
import java.util.List;

/**
 * 发布信息查询请求
 */
public class ReleaseInfoRequest implements Serializable {
    private String projectName;                       // 项目名称（必填）
    private List<String> orchestratorNames;          // 编排名称列表（必填，支持批量查询）

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getOrchestratorNames() {
        return orchestratorNames;
    }

    public void setOrchestratorNames(List<String> orchestratorNames) {
        this.orchestratorNames = orchestratorNames;
    }

    @Override
    public String toString() {
        return "ReleaseInfoRequest{" +
                "projectName='" + projectName + '\'' +
                ", orchestratorNames=" + orchestratorNames +
                '}';
    }
}