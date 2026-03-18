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

package com.webank.wedatasphere.dss.orchestrator.server.entity.response;

import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;

import java.io.Serializable;

/**
 * 发布信息VO，继承ReleaseHistoryDetail并扩展编排相关字段
 */
public class ReleaseInfoVO extends ReleaseHistoryDetail implements Serializable {
    private Long orchestratorId;        // 编排ID
    private String orchestratorName;    // 编排名称
    private Integer projectId;          // 项目ID
    private Integer workspaceId;        // 工作空间ID

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return "ReleaseInfoVO{" +
                "id=" + getId() +
                ", status='" + getStatus() + '\'' +
                ", recode='" + getRecode() + '\'' +
                ", releaseUser='" + getReleaseUser() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", lastModifyUser='" + getLastModifyUser() + '\'' +
                ", releaseTime='" + getReleaseTime() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                ", orchestratorVersionId=" + getOrchestratorVersionId() +
                ", appId=" + getAppId() +
                ", orchestratorId=" + orchestratorId +
                ", orchestratorName='" + orchestratorName + '\'' +
                ", projectId=" + projectId +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
