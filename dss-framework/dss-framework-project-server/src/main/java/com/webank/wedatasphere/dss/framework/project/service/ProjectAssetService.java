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

package com.webank.wedatasphere.dss.framework.project.service;

import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectDetailVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.NodeTypeDistributionVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 项目资产统计聚合服务（新增）。
 *
 * <p>核心职责：
 * <ol>
 *     <li>为项目列表批量填充统计字段（工作流数/数据源数/成员数/最近更新时间/节点数）</li>
 *     <li>计算健康标签（空项目/长期未更新/描述缺失）</li>
 *     <li>管理 workspace 级统计缓存与降级</li>
 *     <li>提供详情聚合（含节点类型分布）与 CSV 导出</li>
 * </ol>
 *
 * <p>全程只读（导出审计日志除外），聚合失败时独立降级，不影响主查询。
 */
public interface ProjectAssetService {

    /**
     * 批量填充项目统计字段（二次填充，不修改主查询结果）。
     *
     * <p>核心逻辑：
     * <ol>
     *     <li>查 workspace 级缓存，未命中则批量聚合 orchestrator + node 统计</li>
     *     <li>填充 workflowCount / latestWorkflowUpdateTime / dataSourceCount / memberCount / nodeCount</li>
     *     <li>派生计算 healthLabels</li>
     *     <li>聚合失败时统计字段置 null，statsDegraded=true</li>
     * </ol>
     *
     * @param workspaceId 工作空间 ID
     * @param responses   当前页项目列表（将原地填充）
     */
    void enrichProjectResponses(Long workspaceId, List<ProjectResponse> responses);

    /**
     * 健康状态预过滤（当 healthStatus 筛选存在时）。
     *
     * <p>核心逻辑：查基础筛选匹配的全量 projectId → 批量算健康标签 → 按 healthStatus 过滤。
     *
     * @param request 查询请求（含基础筛选 + healthStatus）
     * @return 过滤后 projectIdList（null 表示无需预过滤）
     */
    List<Integer> preFilterByHealth(ProjectQueryRequest request);

    /**
     * 最近更新时间预过滤（按 latestWorkflowUpdateTime 过滤）。
     *
     * <p>语义对齐：统计列"最近更新时间"取自 orchestrator 的 latestWorkflowUpdateTime（工作流最近更新），
     * 而非 dss_project.update_time（项目元数据更新时间）。故 updateStartTime/updateEndTime 筛选需基于
     * latestWorkflowUpdateTime 在内存中预过滤，避免筛选项与展示列语义不一致。
     *
     * <p>核心逻辑：查全量项目 → 取每项目 latestWorkflowUpdateTime → 按 [start, end] 范围过滤。
     * latestWorkflowUpdateTime 为 null（无工作流 / orchestrator 降级）的项目排除。
     *
     * @param request 查询请求（含 updateStartTime / updateEndTime，均为 String 类型，格式 yyyy-MM-dd）
     * @return 过滤后 projectIdList（null 表示无需预过滤，即两个时间参数均未传或均解析失败）
     */
    List<Integer> preFilterByUpdateTime(ProjectQueryRequest request);

    /**
     * 获取项目详情（基础信息 + 工作流列表 + 数据源摘要 + 节点类型分布）。
     *
     * @param projectId   项目 ID
     * @param workspaceId 工作空间 ID（越权校验用）
     * @return 项目详情聚合 VO
     */
    ProjectDetailVO getProjectDetail(Long projectId, Long workspaceId);

    /**
     * 获取项目节点类型分布。
     *
     * <p>查 dss_workflow_node_content JOIN dss_orchestrator_info 按 job_type 分组计数，
     * LEFT JOIN dss_workflow_node 取节点显示名，按 count 降序。
     *
     * @param projectId 项目 ID
     * @return 节点类型分布列表（jobType + nodeTypeName + count），失败返回空列表
     */
    List<NodeTypeDistributionVO> getNodeTypeDistribution(Long projectId);

    /**
     * 流式导出 CSV（按筛选全量，≤ 上限行数）。
     *
     * @param request  筛选条件
     * @param response HTTP 响应（用于写出 CSV 流）
     * @return 实际导出行数；-1 表示超限未导出
     */
    int exportCsv(ProjectQueryRequest request, HttpServletResponse response) throws IOException;

    /**
     * 异步记录导出审计日志。
     *
     * @param username 操作人
     * @param request  筛选条件
     * @param rows     实际导出行数
     */
    void auditExportAsync(String username, ProjectQueryRequest request, int rows);
}
