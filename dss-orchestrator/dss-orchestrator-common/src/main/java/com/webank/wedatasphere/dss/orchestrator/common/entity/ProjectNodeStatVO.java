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

/**
 * 项目节点统计 VO（轻量，非持久化）。
 *
 * <p>用于 {@code OrchestratorMapper.countNodesByProjectIds} 的结果载体，
 * 表示单个项目下所有工作流的节点总数（{@code dss_workflow_node_content} 计数）。
 */
public class ProjectNodeStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 ID */
    private Long projectId;

    /** 节点总数（LEFT JOIN 保证无节点项目为 0） */
    private Integer nodeCount;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }
}
