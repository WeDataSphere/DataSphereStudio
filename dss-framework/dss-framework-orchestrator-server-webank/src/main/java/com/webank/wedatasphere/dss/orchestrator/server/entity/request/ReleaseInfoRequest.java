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

package com.webank.wedatasphere.dss.orchestrator.server.entity.request;

import java.io.Serializable;
import java.util.List;

/**
 * 发布信息查询请求
 */
public class ReleaseInfoRequest implements Serializable {
    private Integer workspaceId;                       // 工作空间ID（必填）
    private Integer projectId;                         // 项目ID（必填）
    private List<Long> orchestratorIds;                // 编排ID列表（必填，支持批量查询）
    private String releaseUser;                        // 发布人
    private String startTime;                          // 发布开始时间
    private String endTime;                            // 发布结束时间
    private String comment;                            // 描述

    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<Long> getOrchestratorIds() {
        return orchestratorIds;
    }

    public void setOrchestratorIds(List<Long> orchestratorIds) {
        this.orchestratorIds = orchestratorIds;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ReleaseInfoRequest{" +
                "workspaceId=" + workspaceId +
                ", projectId=" + projectId +
                ", orchestratorIds=" + orchestratorIds +
                ", releaseUser='" + releaseUser + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
