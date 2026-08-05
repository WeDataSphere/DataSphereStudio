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

package com.webank.wedatasphere.dss.framework.project.entity.vo;

import com.webank.wedatasphere.dss.orchestrator.common.entity.NodeTypeDistributionVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.WorkflowSummaryVO;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.DSSProjectDataSource;

import java.io.Serializable;
import java.util.List;

/**
 * 项目详情 VO（详情抽屉聚合载体，非持久化）。
 *
 * <p>聚合四区块：基础信息 + 工作流列表 + 数据源摘要 + 节点类型分布。
 * 各区块独立降级，失败区块置空并标记 degraded，不影响其他区块。
 */
public class ProjectDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 基础信息（项目名称/描述/创建人/工作空间/时间/领域/业务/产品等） */
    private Object basicInfo;

    /** 工作流列表（名称 + 更新时间 + 发布状态） */
    private List<WorkflowSummaryVO> workflowList;

    /** 工作流区块降级标记（查询失败时为 true，前端显示"获取失败"） */
    private Boolean workflowDegraded;

    /** 数据源摘要 */
    private List<DSSProjectDataSource> dataSourceList;

    /** 节点类型分布（按 count 降序） */
    private List<NodeTypeDistributionVO> nodeTypeDistribution;

    /** 节点统计降级标记（分布查询失败时为 true，前端显示"获取失败"） */
    private Boolean nodeDegraded;

    public Object getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(Object basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<WorkflowSummaryVO> getWorkflowList() {
        return workflowList;
    }

    public void setWorkflowList(List<WorkflowSummaryVO> workflowList) {
        this.workflowList = workflowList;
    }

    public Boolean getWorkflowDegraded() {
        return workflowDegraded;
    }

    public void setWorkflowDegraded(Boolean workflowDegraded) {
        this.workflowDegraded = workflowDegraded;
    }

    public List<DSSProjectDataSource> getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(List<DSSProjectDataSource> dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public List<NodeTypeDistributionVO> getNodeTypeDistribution() {
        return nodeTypeDistribution;
    }

    public void setNodeTypeDistribution(List<NodeTypeDistributionVO> nodeTypeDistribution) {
        this.nodeTypeDistribution = nodeTypeDistribution;
    }

    public Boolean getNodeDegraded() {
        return nodeDegraded;
    }

    public void setNodeDegraded(Boolean nodeDegraded) {
        this.nodeDegraded = nodeDegraded;
    }
}
