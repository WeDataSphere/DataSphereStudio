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

package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectDetailVO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.QueryProjectVo;
import com.webank.wedatasphere.dss.framework.project.service.ProjectAssetService;
import com.webank.wedatasphere.dss.framework.project.service.ProjectHealthEvaluator;
import com.webank.wedatasphere.dss.orchestrator.common.entity.NodeTypeDistributionVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ProjectNodeStatVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ProjectOrcStatVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.WorkflowSummaryVO;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorMapper;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.DSSProjectDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目资产统计聚合服务实现。
 *
 * <p>设计要点：
 * <ul>
 *     <li>同 JVM 共享 Mapper 直查（OrchestratorMapper），方案④，零网络开销</li>
 *     <li>workspace 级统计缓存（TTL 可配，默认 5min），翻页/导出复用</li>
 *     <li>orchestrator 统计与节点统计独立 try-catch，独立降级互不影响</li>
 *     <li>降级不假阳性：聚合失败时统计列置 null，空项目标签不置 true</li>
 *     <li>全程只读（导出审计日志除外）</li>
 * </ul>
 */
@Service
public class ProjectAssetServiceImpl implements ProjectAssetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAssetServiceImpl.class);

    private static final String CSV_COLUMN_DELIMITER = ",";
    private static final String CSV_LINE_SEPARATOR = "\n";
    private static final String DEGRADED_PLACEHOLDER = "--";

    @Autowired
    private OrchestratorMapper orchestratorMapper;

    @Autowired
    private DSSProjectMapper projectMapper;

    @Autowired
    private ProjectHealthEvaluator healthEvaluator;

    /** 长期未更新阈值（天），默认 90 */
    @Value("${dss.project.ledger.stale-threshold-days:90}")
    private int staleThresholdDays;

    /** 统计缓存 TTL（分钟），默认 5 */
    @Value("${dss.project.ledger.stats-cache-ttl-minutes:5}")
    private int cacheTtlMinutes;

    /** 导出行数上限，默认 5000 */
    @Value("${dss.project.ledger.export-max-rows:5000}")
    private int exportMaxRows;

    /** 导出超时（秒），默认 30 */
    @Value("${dss.project.ledger.export-timeout-seconds:30}")
    private int exportTimeoutSeconds;

    /** workspace 级统计缓存：workspaceId -> 带过期的缓存条目 */
    private final ConcurrentHashMap<Long, StatsCacheEntry> statsCache = new ConcurrentHashMap<>();

    /** 审计日志异步执行器（单线程，失败仅记本地日志） */
    private final ExecutorService auditExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "project-asset-audit");
        t.setDaemon(true);
        return t;
    });

    // ============================== 统计填充 ==============================

    @Override
    public void enrichProjectResponses(Long workspaceId, List<ProjectResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        Map<Long, AssetStats> statsMap;
        try {
            statsMap = getOrLoadStats(workspaceId);
        } catch (Exception e) {
            LOGGER.error("enrichProjectResponses load stats failed, workspaceId={}", workspaceId, e);
            statsMap = Collections.emptyMap();
        }
        for (ProjectResponse resp : responses) {
            AssetStats stats = statsMap.get(resp.getId());
            boolean degraded = (stats == null) || !stats.isOrchestratorAvailable();
            fillStats(resp, stats);
            if (degraded) {
                resp.setHealthLabels(healthEvaluator.evaluateOnDegraded(resp.getDescription()));
                resp.setStatsDegraded(true);
            } else {
                resp.setHealthLabels(healthEvaluator.evaluate(
                        resp.getDescription(),
                        resp.getWorkflowCount(),
                        resp.getLatestWorkflowUpdateTime()));
                resp.setStatsDegraded(stats != null && stats.isNodeDegraded());
            }
        }
    }

    /** 填充单条 ProjectResponse 的统计字段（dataSource/member 可从响应自身派生）。 */
    private void fillStats(ProjectResponse resp, AssetStats stats) {
        if (stats != null) {
            if (stats.isOrchestratorAvailable()) {
                resp.setWorkflowCount(stats.getWorkflowCount());
                resp.setLatestWorkflowUpdateTime(stats.getLatestWorkflowUpdateTime());
            }
            if (!stats.isNodeDegraded()) {
                resp.setNodeCount(stats.getNodeCount());
            }
        }
        // 数据源数：从主查询已填充的 dataSourceList 派生
        resp.setDataSourceCount(resp.getDataSourceList() == null ? 0 : resp.getDataSourceList().size());
        // 成员数：发布+编辑+查看去重
        resp.setMemberCount(dedupMemberCount(resp));
    }

    /** 发布+编辑+查看用户去重计数。 */
    private int dedupMemberCount(ProjectResponse resp) {
        Set<String> members = new HashSet<>();
        if (resp.getReleaseUsers() != null) {
            members.addAll(resp.getReleaseUsers());
        }
        if (resp.getEditUsers() != null) {
            members.addAll(resp.getEditUsers());
        }
        if (resp.getAccessUsers() != null) {
            members.addAll(resp.getAccessUsers());
        }
        // 移除空串
        members.removeIf(StringUtils::isBlank);
        return members.size();
    }

    // ============================== 健康预过滤 ==============================

    @Override
    public List<Integer> preFilterByHealth(ProjectQueryRequest request) {
        if (request == null || request.getHealthStatus() == null || request.getHealthStatus().isEmpty()) {
            return null;
        }
        try {
            List<QueryProjectVo> allProjects = projectMapper.queryProjectList(
                    request.getWorkspaceId(), null);
            if (allProjects == null || allProjects.isEmpty()) {
                return Collections.emptyList();
            }
            Map<Long, AssetStats> statsMap = getOrLoadStats(request.getWorkspaceId());
            Set<String> selected = new HashSet<>(request.getHealthStatus());
            List<Integer> filtered = new ArrayList<>();
            for (QueryProjectVo pvo : allProjects) {
                AssetStats stats = statsMap.get(pvo.getId());
                Integer workflowCount = (stats != null && stats.isOrchestratorAvailable())
                        ? stats.getWorkflowCount() : null;
                Date latestTime = (stats != null && stats.isOrchestratorAvailable())
                        ? stats.getLatestWorkflowUpdateTime() : null;
                if (healthEvaluator.matchAny(pvo.getDescription(), workflowCount, latestTime, selected)) {
                    filtered.add(pvo.getId() == null ? null : pvo.getId().intValue());
                }
            }
            filtered.removeIf(Objects::isNull);
            return filtered;
        } catch (Exception e) {
            LOGGER.error("preFilterByHealth failed, workspaceId={}, fallback to no health filter",
                    request.getWorkspaceId(), e);
            return null;
        }
    }

    // ============================== 项目详情 ==============================

    @Override
    public ProjectDetailVO getProjectDetail(Long projectId, Long workspaceId) {
        ProjectDetailVO detail = new ProjectDetailVO();
        // 基础信息（始终返回）
        DSSProjectDO project = projectMapper.selectById(projectId);
        detail.setBasicInfo(project);
        // 数据源摘要
        detail.setDataSourceList(parseDataSourceList(project));
        // 工作流列表（独立降级）
        try {
            detail.setWorkflowList(orchestratorMapper.listOrcSummaryByProjectId(projectId));
            detail.setWorkflowDegraded(false);
        } catch (Exception e) {
            LOGGER.error("listOrcSummaryByProjectId failed, projectId={}", projectId, e);
            detail.setWorkflowList(Collections.emptyList());
            detail.setWorkflowDegraded(true);
        }
        // 节点类型分布（按需查询，独立降级）
        detail.setNodeTypeDistribution(getNodeTypeDistribution(projectId));
        return detail;
    }

    @Override
    public List<NodeTypeDistributionVO> getNodeTypeDistribution(Long projectId) {
        try {
            List<NodeTypeDistributionVO> list = orchestratorMapper.listNodeTypeDistributionByProjectId(projectId);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            LOGGER.error("listNodeTypeDistributionByProjectId failed, projectId={}", projectId, e);
            return Collections.emptyList();
        }
    }

    /** 解析项目的数据源列表 JSON。 */
    private List<DSSProjectDataSource> parseDataSourceList(DSSProjectDO project) {
        if (project == null || StringUtils.isBlank(project.getDataSourceListJson())) {
            return Collections.emptyList();
        }
        try {
            return new Gson().fromJson(project.getDataSourceListJson(),
                    new TypeToken<List<DSSProjectDataSource>>() {
                    }.getType());
        } catch (Exception e) {
            LOGGER.warn("parse dataSourceListJson failed, projectId={}", project.getId(), e);
            return Collections.emptyList();
        }
    }

    // ============================== 统计缓存 ==============================

    /**
     * 获取或加载 workspace 级统计 Map。
     *
     * <p>orchestrator 统计与节点统计分独立 try-catch：前者失败 workflowCount/latestTime 置 null，
     * 后者失败 nodeCount 置 null，两者互不影响。
     */
    private Map<Long, AssetStats> getOrLoadStats(Long workspaceId) {
        StatsCacheEntry entry = statsCache.get(workspaceId);
        long now = System.currentTimeMillis();
        if (entry != null && entry.expireAt > now) {
            return entry.stats;
        }
        // 加载全 workspace 项目 ID（visible=1）
        List<Long> allProjectIds = projectMapper.getProjectIdsByWorkspaceId(workspaceId, 1);
        if (allProjectIds == null || allProjectIds.isEmpty()) {
            Map<Long, AssetStats> empty = new HashMap<>();
            statsCache.put(workspaceId, new StatsCacheEntry(empty, now + cacheTtlMinutes * 60_000L));
            return empty;
        }
        Map<Long, AssetStats> statsMap = new HashMap<>();
        // 确保每个 projectId 都有条目
        for (Long pid : allProjectIds) {
            statsMap.put(pid, new AssetStats());
        }
        // orchestrator 统计（独立降级）
        try {
            List<ProjectOrcStatVO> counts = orchestratorMapper.countOrcByProjectIds(allProjectIds);
            List<ProjectOrcStatVO> times = orchestratorMapper.maxUpdateTimeByProjectIds(allProjectIds);
            mergeOrcStats(statsMap, counts, times);
            // 标记 orchestrator 可用
            for (AssetStats s : statsMap.values()) {
                s.setOrchestratorAvailable(true);
            }
        } catch (Exception e) {
            LOGGER.error("orchestrator stats failed, workspaceId={}", workspaceId, e);
            // 降级：保持 orchestratorAvailable=false，workflowCount/latestTime 为 null
        }
        // 节点统计（独立降级）
        try {
            List<ProjectNodeStatVO> nodeCounts = orchestratorMapper.countNodesByProjectIds(allProjectIds);
            if (nodeCounts != null) {
                for (ProjectNodeStatVO ns : nodeCounts) {
                    AssetStats s = statsMap.computeIfAbsent(ns.getProjectId(), k -> new AssetStats());
                    s.setNodeCount(ns.getNodeCount());
                }
            }
            // 节点查询成功：未在结果中的项目 nodeCount=0（LEFT JOIN 已保证 0，但补防御）
            for (AssetStats s : statsMap.values()) {
                if (s.getNodeCount() == null) {
                    s.setNodeCount(0);
                }
            }
        } catch (Exception e) {
            LOGGER.error("node stats failed, workspaceId={}", workspaceId, e);
            // 降级：nodeCount 保持 null，标记 nodeDegraded
            for (AssetStats s : statsMap.values()) {
                s.setNodeDegraded(true);
            }
        }
        statsCache.put(workspaceId, new StatsCacheEntry(statsMap, now + cacheTtlMinutes * 60_000L));
        return statsMap;
    }

    /** 合并 orchestrator 批量统计到 statsMap。 */
    private void mergeOrcStats(Map<Long, AssetStats> statsMap,
                               List<ProjectOrcStatVO> counts, List<ProjectOrcStatVO> times) {
        if (counts != null) {
            for (ProjectOrcStatVO c : counts) {
                statsMap.computeIfAbsent(c.getProjectId(), k -> new AssetStats())
                        .setWorkflowCount(c.getCount() == null ? 0 : c.getCount());
            }
        }
        if (times != null) {
            for (ProjectOrcStatVO t : times) {
                statsMap.computeIfAbsent(t.getProjectId(), k -> new AssetStats())
                        .setLatestWorkflowUpdateTime(t.getUpdateTime());
            }
        }
        // LEFT/INNER JOIN 未命中的项目工作流数为 0（orchestrator 查询成功时）
        for (AssetStats s : statsMap.values()) {
            if (s.getWorkflowCount() == null) {
                s.setWorkflowCount(0);
            }
        }
    }

    // ============================== CSV 导出 ==============================

    @Override
    public int exportCsv(ProjectQueryRequest request, HttpServletResponse response) throws IOException {
        // 查全量匹配项目（不分页）
        List<QueryProjectVo> allProjects = projectMapper.queryProjectList(
                request.getWorkspaceId(), null);
        if (allProjects == null) {
            allProjects = Collections.emptyList();
        }
        // 内存应用基础筛选（项目名/创建人）
        List<QueryProjectVo> filtered = applyBaseFilters(allProjects, request);
        // 行数上限校验
        if (filtered.size() > exportMaxRows) {
            return -1;
        }
        // 加载统计缓存（复用翻页缓存）
        Map<Long, AssetStats> statsMap;
        try {
            statsMap = getOrLoadStats(request.getWorkspaceId());
        } catch (Exception e) {
            LOGGER.error("exportCsv load stats failed, workspaceId={}", request.getWorkspaceId(), e);
            statsMap = Collections.emptyMap();
        }
        // 流式写 CSV
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"project_ledger_" + System.currentTimeMillis() + ".csv\"");
        // UTF-8 BOM，避免 Excel 中文乱码
        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write('﻿');
            // 表头
            writer.write(buildCsvHeader());
            // 数据行
            for (QueryProjectVo pvo : filtered) {
                AssetStats stats = statsMap.get(pvo.getId());
                writer.write(buildCsvRow(pvo, stats));
            }
            writer.flush();
        }
        return filtered.size();
    }

    /** 内存应用项目名/创建人筛选（导出场景，全量集合上过滤）。 */
    private List<QueryProjectVo> applyBaseFilters(List<QueryProjectVo> all, ProjectQueryRequest req) {
        Stream<QueryProjectVo> stream = all.stream();
        if (req.getProjectNames() != null && !req.getProjectNames().isEmpty()) {
            stream = stream.filter(p -> p.getName() != null && req.getProjectNames().contains(p.getName()));
        }
        if (req.getCreateUsers() != null && !req.getCreateUsers().isEmpty()) {
            stream = stream.filter(p -> p.getCreateBy() != null && req.getCreateUsers().contains(p.getCreateBy()));
        }
        return stream.collect(Collectors.toList());
    }

    private String buildCsvHeader() {
        return String.join(CSV_COLUMN_DELIMITER,
                "项目ID", "项目名称", "描述", "创建人", "创建时间", "更新时间",
                "工作流数", "节点数", "数据源数", "成员数", "最近工作流更新时间", "健康标签") + CSV_LINE_SEPARATOR;
    }

    private String buildCsvRow(QueryProjectVo pvo, AssetStats stats) {
        Integer workflowCount = (stats != null && stats.isOrchestratorAvailable()) ? stats.getWorkflowCount() : null;
        Date latestTime = (stats != null && stats.isOrchestratorAvailable()) ? stats.getLatestWorkflowUpdateTime() : null;
        Integer nodeCount = (stats != null && !stats.isNodeDegraded()) ? stats.getNodeCount() : null;
        List<String> labels = healthEvaluator.evaluate(pvo.getDescription(), workflowCount, latestTime);
        return String.join(CSV_COLUMN_DELIMITER,
                csv(pvo.getId()),
                csv(pvo.getName()),
                csv(pvo.getDescription()),
                csv(pvo.getCreateBy()),
                csv(pvo.getCreateTime()),
                csv(pvo.getUpdateTime()),
                workflowCount == null ? DEGRADED_PLACEHOLDER : String.valueOf(workflowCount),
                nodeCount == null ? DEGRADED_PLACEHOLDER : String.valueOf(nodeCount),
                DEGRADED_PLACEHOLDER, // 数据源数导出场景无主查询填充，置占位
                DEGRADED_PLACEHOLDER, // 成员数导出场景无主查询填充，置占位
                latestTime == null ? DEGRADED_PLACEHOLDER : String.valueOf(latestTime),
                csv(String.join(";", labels))) + CSV_LINE_SEPARATOR;
    }

    /** CSV 单元格转义：含逗号/引号/换行则用双引号包裹并转义内部引号。 */
    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value);
        if (s.contains(CSV_COLUMN_DELIMITER) || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ============================== 审计日志 ==============================

    @Override
    public void auditExportAsync(String username, ProjectQueryRequest request, int rows) {
        auditExecutor.submit(() -> {
            try {
                LOGGER.info("project ledger export audit | user={} | workspaceId={} | rows={} | filters={}",
                        username, request == null ? null : request.getWorkspaceId(), rows, request);
            } catch (Exception e) {
                LOGGER.warn("audit export failed silently", e);
            }
        });
    }

    // ============================== 内部数据结构 ==============================

    /** 单项目资产统计（非持久化，缓存值元素）。 */
    private static class AssetStats {
        private Integer workflowCount;
        private Date latestWorkflowUpdateTime;
        private Integer nodeCount;
        /** orchestrator 批量统计是否成功（false 表示降级） */
        private boolean orchestratorAvailable = false;
        /** 节点统计是否降级 */
        private boolean nodeDegraded = false;

        Integer getWorkflowCount() {
            return workflowCount;
        }

        AssetStats setWorkflowCount(Integer workflowCount) {
            this.workflowCount = workflowCount;
            return this;
        }

        Date getLatestWorkflowUpdateTime() {
            return latestWorkflowUpdateTime;
        }

        AssetStats setLatestWorkflowUpdateTime(Date latestWorkflowUpdateTime) {
            this.latestWorkflowUpdateTime = latestWorkflowUpdateTime;
            return this;
        }

        Integer getNodeCount() {
            return nodeCount;
        }

        AssetStats setNodeCount(Integer nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        boolean isOrchestratorAvailable() {
            return orchestratorAvailable;
        }

        void setOrchestratorAvailable(boolean orchestratorAvailable) {
            this.orchestratorAvailable = orchestratorAvailable;
        }

        boolean isNodeDegraded() {
            return nodeDegraded;
        }

        void setNodeDegraded(boolean nodeDegraded) {
            this.nodeDegraded = nodeDegraded;
        }
    }

    /** 统计缓存条目（带过期时间）。 */
    private static class StatsCacheEntry {
        final Map<Long, AssetStats> stats;
        final long expireAt;

        StatsCacheEntry(Map<Long, AssetStats> stats, long expireAt) {
            this.stats = stats;
            this.expireAt = expireAt;
        }
    }
}
