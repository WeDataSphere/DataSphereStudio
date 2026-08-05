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

package com.webank.wedatasphere.dss.orchestrator.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目编排统计 VO（轻量，非持久化）。
 *
 * <p>用于 {@code OrchestratorMapper} 批量统计方法的结果载体：
 * <ul>
 *     <li>{@code countOrcByProjectIds} —— 填充 {@link #count}（有效工作流数）</li>
 *     <li>{@code maxUpdateTimeByProjectIds} —— 填充 {@link #updateTime}（最近工作流更新时间）</li>
 * </ul>
 */
public class ProjectOrcStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 ID */
    private Long projectId;

    /** 有效工作流数（validFlag=1 去重 orchestrator 数） */
    private Integer count;

    /** 最近工作流更新时间（max(update_time)） */
    private Date updateTime;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
