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
 * 节点类型分布 VO（轻量，非持久化）。
 *
 * <p>用于 {@code OrchestratorMapper.listNodeTypeDistributionByProjectId} 的结果载体，
 * 供项目详情抽屉按 {@code job_type} 展示节点类型分布（按 count 降序）。
 *
 * <p>当 {@code job_type} 在 {@code dss_workflow_node} 目录表中无匹配时，
 * {@link #nodeTypeName} 为 null，前端可回退显示 {@link #jobType} 原始值。
 */
public class NodeTypeDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点类型（如 linkis.hive.hql） */
    private String jobType;

    /** 节点显示名（取自 dss_workflow_node.name，可能为 null） */
    private String nodeTypeName;

    /** 该类型节点数量 */
    private Integer count;

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
