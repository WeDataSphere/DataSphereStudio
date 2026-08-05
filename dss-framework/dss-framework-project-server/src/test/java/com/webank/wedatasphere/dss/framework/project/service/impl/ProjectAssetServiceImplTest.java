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

import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectDetailVO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.QueryProjectVo;
import com.webank.wedatasphere.dss.framework.project.service.ProjectHealthEvaluator;
import com.webank.wedatasphere.dss.orchestrator.common.entity.NodeTypeDistributionVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ProjectNodeStatVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ProjectOrcStatVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.WorkflowSummaryVO;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ProjectAssetServiceImpl} 降级用例单测。
 *
 * <p>覆盖联调 26 条用例中 SIT 运行时无法 Mock 的「降级」类场景：Mapper 抛异常时，
 * 服务应区块级隔离降级、不假阳性、单元格回退占位符 "--"。
 *
 * <p><b>设计决策</b>：被测依赖中 {@link OrchestratorMapper} / {@link DSSProjectMapper}
 * 有外部 DB 依赖，使用 Mockito @Mock 隔离；而 {@link ProjectHealthEvaluator} 是无任何
 * 外部依赖的纯函数，<b>这里使用真实实例（非 Mock）</b>，从而让 "不假阳性 EMPTY_PROJECT /
 * NO_DESCRIPTION 按 description 正常计算" 这类断言真正验证健康标签计算语义，而非自证
 * Mock 的桩值。整套测试为纯 Mockito 单测，不启动 Spring 容器。
 *
 * <p>每个测试方法独立 new 服务实例（@Before），内部 statsCache（ConcurrentHashMap）
 * 天然按方法隔离，不存在缓存串扰。
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectAssetServiceImplTest {

    @Mock
    private OrchestratorMapper orchestratorMapper;
    @Mock
    private DSSProjectMapper projectMapper;

    /** 真实健康标签计算器（纯函数，反射注入阈值后即用）。 */
    private ProjectHealthEvaluator healthEvaluator;

    private ProjectAssetServiceImpl service;

    private static final Long WORKSPACE_ID = 100L;
    private static final Long PROJECT_ID = 1L;

    @Before
    public void setUp() {
        service = new ProjectAssetServiceImpl();
        healthEvaluator = new ProjectHealthEvaluator();
        // 注入真实协作对象与 @Value 字段默认值
        ReflectionTestUtils.setField(service, "orchestratorMapper", orchestratorMapper);
        ReflectionTestUtils.setField(service, "projectMapper", projectMapper);
        ReflectionTestUtils.setField(service, "healthEvaluator", healthEvaluator);
        ReflectionTestUtils.setField(healthEvaluator, "staleThresholdDays", 90);
        ReflectionTestUtils.setField(service, "staleThresholdDays", 90);
        ReflectionTestUtils.setField(service, "cacheTtlMinutes", 5);
        ReflectionTestUtils.setField(service, "exportMaxRows", 5000);
        ReflectionTestUtils.setField(service, "exportTimeoutSeconds", 30);
    }

    // ============================== 场景 1 ==============================
    // enrichProjectResponses：orchestrator 聚合失败降级，不假阳性 EMPTY_PROJECT
    // ===================================================================================
    @Test
    public void enrichProjectResponses_whenOrchestratorAggregationFails_thenDegradedAndNoFalsePositive() {
        // given：workspace 下有项目，orchestrator 聚合首个统计调用即抛异常
        // （countOrcByProjectIds 在 try 块中先于 maxUpdateTimeByProjectIds 调用，
        //  抛出后整段 orchestrator 聚合进入 catch 降级，maxUpdateTime 不会被调用）
        when(projectMapper.getProjectIdsByWorkspaceId(WORKSPACE_ID, 1))
                .thenReturn(Collections.singletonList(PROJECT_ID));
        when(orchestratorMapper.countOrcByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenThrow(new RuntimeException("simulate dss_orchestrator_info unavailable"));
        // 节点统计正常（验证独立降级：orc 失败不影响 node 查询路径）
        when(orchestratorMapper.countNodesByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.<ProjectNodeStatVO>emptyList());

        ProjectResponse resp = newProjectResponse(PROJECT_ID, "这是一个有描述的项目");

        // when
        service.enrichProjectResponses(WORKSPACE_ID, Collections.singletonList(resp));

        // then：orc 降级 → workflowCount / latestTime 置 null，statsDegraded=true
        assertNull("orchestrator 降级时 workflowCount 必须为 null", resp.getWorkflowCount());
        assertNull("orchestrator 降级时 latestWorkflowUpdateTime 必须为 null",
                resp.getLatestWorkflowUpdateTime());
        assertTrue("statsDegraded 必须为 true", resp.getStatsDegraded());

        // then：健康标签走 evaluateOnDegraded —— 含 UNAVAILABLE，绝不假阳性 EMPTY_PROJECT；
        //       description 非空 → NO_DESCRIPTION 不应出现
        assertNotNull(resp.getHealthLabels());
        assertTrue("降级应带 UNAVAILABLE 标签", resp.getHealthLabels().contains(ProjectHealthEvaluator.UNAVAILABLE));
        assertFalse("聚合失败时绝不假阳性标记 EMPTY_PROJECT",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.EMPTY_PROJECT));
        assertFalse("description 非空时不应标记 NO_DESCRIPTION",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.NO_DESCRIPTION));
    }

    // ============================== 场景 2 ==============================
    // getOrLoadStats：节点统计失败独立降级，workflowCount 仍正常（两套降级互不影响）
    // ===================================================================================
    @Test
    public void enrichProjectResponses_whenNodeStatsFail_thenWorkflowCountStillCorrect() {
        // given：orchestrator 统计成功，节点统计抛异常
        Date recent = new Date(System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000); // 10 天前，不触发 STALE
        when(projectMapper.getProjectIdsByWorkspaceId(WORKSPACE_ID, 1))
                .thenReturn(Collections.singletonList(PROJECT_ID));
        ProjectOrcStatVO countVo = new ProjectOrcStatVO();
        countVo.setProjectId(PROJECT_ID);
        countVo.setCount(3);
        when(orchestratorMapper.countOrcByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(countVo));
        ProjectOrcStatVO timeVo = new ProjectOrcStatVO();
        timeVo.setProjectId(PROJECT_ID);
        timeVo.setUpdateTime(recent);
        when(orchestratorMapper.maxUpdateTimeByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(timeVo));
        // 节点统计失败
        when(orchestratorMapper.countNodesByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenThrow(new RuntimeException("simulate dss_workflow_node_content unavailable"));

        ProjectResponse resp = newProjectResponse(PROJECT_ID, "节点统计失败的独立降级项目");

        // when
        service.enrichProjectResponses(WORKSPACE_ID, Collections.singletonList(resp));

        // then：orchestrator 未降级 → workflowCount / latestTime 正常填充
        assertEquals("节点失败不应影响 orchestrator 统计，workflowCount 必须正常",
                Integer.valueOf(3), resp.getWorkflowCount());
        assertEquals("latestWorkflowUpdateTime 必须正常填充", recent, resp.getLatestWorkflowUpdateTime());
        // then：节点降级 → nodeCount 置 null，statsDegraded=true（因 nodeDegraded）
        assertNull("节点降级时 nodeCount 必须为 null", resp.getNodeCount());
        assertTrue("节点降级时 statsDegraded 必须为 true", resp.getStatsDegraded());
        // then：非降级健康评估路径（workflowCount=3 非空项目，无 EMPTY_PROJECT 假阳性）
        assertNotNull(resp.getHealthLabels());
        assertFalse("workflowCount=3 不应标记 EMPTY_PROJECT",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.EMPTY_PROJECT));
    }

    // ============================== 场景 2.1 ==============================
    // enrichProjectResponses：项目有工作流但无节点（nodeCount=0，非降级）
    // 区分点：countNodes 成功返回空列表 → nodeCount=0（非 null）；
    //         区别于场景 2 的 countNodes 抛异常 → nodeCount=null + nodeDegraded
    // ===================================================================================
    @Test
    public void enrichProjectResponses_whenWorkflowHasNoNodes_thenNodeCountZeroNotDegrade() {
        // given：项目有 1 个工作流（workflowCount=1），最近更新（不触发 STALE），但无节点
        // countNodesByProjectIds 成功返回空列表 → 走 getOrLoadStats 的"未在结果中 nodeCount=0"补防御分支
        Date recent = new Date(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000); // 3 天前，不触发 STALE
        when(projectMapper.getProjectIdsByWorkspaceId(WORKSPACE_ID, 1))
                .thenReturn(Collections.singletonList(PROJECT_ID));
        ProjectOrcStatVO countVo = new ProjectOrcStatVO();
        countVo.setProjectId(PROJECT_ID);
        countVo.setCount(1);
        when(orchestratorMapper.countOrcByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(countVo));
        ProjectOrcStatVO timeVo = new ProjectOrcStatVO();
        timeVo.setProjectId(PROJECT_ID);
        timeVo.setUpdateTime(recent);
        when(orchestratorMapper.maxUpdateTimeByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(timeVo));
        // 节点统计成功返回空列表（项目无节点，非异常）—— 关键：返回空而非抛异常
        when(orchestratorMapper.countNodesByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.<ProjectNodeStatVO>emptyList());

        ProjectResponse resp = newProjectResponse(PROJECT_ID, "有工作流但无节点的项目");

        // when
        service.enrichProjectResponses(WORKSPACE_ID, Collections.singletonList(resp));

        // then：orchestrator + 节点统计均成功，不降级
        assertEquals("有工作流时 workflowCount 必须正常填充为 1",
                Integer.valueOf(1), resp.getWorkflowCount());
        assertEquals("latestWorkflowUpdateTime 必须正常填充", recent, resp.getLatestWorkflowUpdateTime());
        // 关键区分点：nodeCount=0（非 null）—— 节点统计成功但项目无节点，走补防御分支置 0
        assertEquals("节点统计成功返回空时 nodeCount 必须为 0（非 null，区别于降级场景的 null）",
                Integer.valueOf(0), resp.getNodeCount());
        assertFalse("节点统计成功时 statsDegraded 必须为 false（区别于场景 2 的 nodeDegraded=true）",
                resp.getStatsDegraded());
        // then：健康标签 —— 有工作流（workflowCount=1）绝不假阳性 EMPTY_PROJECT；近期更新不含 STALE
        assertNotNull(resp.getHealthLabels());
        assertFalse("有工作流时不应标记 EMPTY_PROJECT（区别于真正的空项目 workflowCount=0）",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.EMPTY_PROJECT));
        assertFalse("近期更新（3 天前）不应标记 STALE",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.STALE));
        assertFalse("描述非空时不应标记 NO_DESCRIPTION",
                resp.getHealthLabels().contains(ProjectHealthEvaluator.NO_DESCRIPTION));
    }

    // ============================== 场景 3 ==============================
    // getProjectDetail：工作流查询失败降级，basicInfo / dataSourceList / nodeTypeDistribution 正常
    // ===================================================================================
    @Test
    public void getProjectDetail_whenWorkflowQueryFails_thenWorkflowDegradedButOthersIntact() {
        // given：基础信息正常，工作流摘要抛异常，节点分布正常
        DSSProjectDO project = new DSSProjectDO();
        project.setId(PROJECT_ID);
        project.setName("detail-wf-fail");
        project.setDataSourceListJson(null);
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project);
        when(orchestratorMapper.listOrcSummaryByProjectId(PROJECT_ID))
                .thenThrow(new RuntimeException("simulate listOrcSummary unavailable"));
        NodeTypeDistributionVO dist = new NodeTypeDistributionVO();
        dist.setJobType("linkis.hive.hql");
        dist.setCount(2);
        when(orchestratorMapper.listNodeTypeDistributionByProjectId(PROJECT_ID))
                .thenReturn(Collections.singletonList(dist));

        // when
        ProjectDetailVO detail = service.getProjectDetail(PROJECT_ID, WORKSPACE_ID);

        // then：区块级隔离 —— 工作流降级，其余正常
        assertNotNull("basicInfo 必须始终返回", detail.getBasicInfo());
        assertEquals(project, detail.getBasicInfo());
        assertTrue("工作流查询失败 → workflowList 必须为空列表",
                detail.getWorkflowList() == null || detail.getWorkflowList().isEmpty());
        assertEquals("工作流查询失败 → workflowDegraded 必须为 true",
                Boolean.TRUE, detail.getWorkflowDegraded());
        // dataSourceList 在 json 为 null 时回退空列表（不降级）
        assertNotNull("dataSourceList 不应因工作流失败而 null", detail.getDataSourceList());
        assertTrue(detail.getDataSourceList().isEmpty());
        // 节点分布不受工作流失败影响
        assertNotNull("节点分布不受工作流失败影响", detail.getNodeTypeDistribution());
        assertEquals(1, detail.getNodeTypeDistribution().size());
    }

    // ============================== 场景 4 ==============================
    // getProjectDetail：节点分布查询失败降级，基础信息 / 工作流正常
    // ===================================================================================
    @Test
    public void getProjectDetail_whenNodeTypeDistributionFails_thenOnlyDistributionEmpty() {
        // given：基础信息 + 工作流正常，节点分布抛异常
        DSSProjectDO project = new DSSProjectDO();
        project.setId(PROJECT_ID);
        project.setName("detail-node-fail");
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project);
        WorkflowSummaryVO wf = new WorkflowSummaryVO();
        wf.setOrchestratorId(200L);
        wf.setName("wf-1");
        wf.setStatus("已发布");
        when(orchestratorMapper.listOrcSummaryByProjectId(PROJECT_ID))
                .thenReturn(Collections.singletonList(wf));
        when(orchestratorMapper.listNodeTypeDistributionByProjectId(PROJECT_ID))
                .thenThrow(new RuntimeException("simulate node distribution unavailable"));

        // when
        ProjectDetailVO detail = service.getProjectDetail(PROJECT_ID, WORKSPACE_ID);

        // then：节点分布降级为空列表，工作流与基础信息保持正常
        assertNotNull("节点分布查询失败应回退空列表而非 null", detail.getNodeTypeDistribution());
        assertTrue("节点分布查询失败应回退空列表", detail.getNodeTypeDistribution().isEmpty());
        assertNotNull("工作流列表不应受节点分布失败影响", detail.getWorkflowList());
        assertEquals(1, detail.getWorkflowList().size());
        assertEquals("wf-1", detail.getWorkflowList().get(0).getName());
        assertEquals("工作流未失败 → workflowDegraded 必须为 false",
                Boolean.FALSE, detail.getWorkflowDegraded());
        assertNotNull("basicInfo 不受节点分布失败影响", detail.getBasicInfo());
    }

    // ============================== 场景 5 ==============================
    // preFilterByHealth：异常回退返回 null（不过滤、不假阳性返回“空项目”）
    // ===================================================================================
    @Test
    public void preFilterByHealth_whenQueryThrows_thenReturnsNullNotFalsePositive() {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setWorkspaceId(WORKSPACE_ID);
        request.setHealthStatus(Arrays.asList(ProjectHealthEvaluator.EMPTY_PROJECT));

        // 子场景 a：queryProjectList 本身抛异常
        when(projectMapper.queryProjectList(WORKSPACE_ID, null))
                .thenThrow(new RuntimeException("simulate queryProjectList unavailable"));

        List<Integer> result = service.preFilterByHealth(request);
        // 关键：返回 null（交由上层不过滤），绝不能返回空列表造成“所有项目都被过滤掉”的假阳性
        assertNull("异常时必须回退 null（不过滤），不得返回空列表造成假阳性过滤", result);
    }

    @Test
    public void preFilterByHealth_whenStatsLoadThrows_thenReturnsNullNotFalsePositive() {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setWorkspaceId(WORKSPACE_ID);
        request.setHealthStatus(Arrays.asList(ProjectHealthEvaluator.STALE));

        // 子场景 b：queryProjectList 正常返回非空，但 getProjectIdsByWorkspaceId（getOrLoadStats 首行）
        //          抛异常，触发 preFilterByHealth 的 catch → 回退 null
        QueryProjectVo pvo = new QueryProjectVo();
        pvo.setId(PROJECT_ID);
        when(projectMapper.queryProjectList(WORKSPACE_ID, null))
                .thenReturn(Collections.singletonList(pvo));
        when(projectMapper.getProjectIdsByWorkspaceId(WORKSPACE_ID, 1))
                .thenThrow(new RuntimeException("simulate getProjectIds unavailable"));

        List<Integer> result = service.preFilterByHealth(request);
        assertNull("stats 加载异常时同样必须回退 null，避免假阳性", result);
    }

    // ============================== 场景 6 ==============================
    // exportCsv：节点统计失败、工作流正常 → CSV 中 nodeCount 列="--"、workflowCount 列正常数值
    // ===================================================================================
    @Test
    public void exportCsv_whenNodeStatsFail_thenNodeCountCellDegradedButWorkflowNormal() throws Exception {
        // given：全量查询返回 1 个项目
        QueryProjectVo pvo = new QueryProjectVo();
        pvo.setId(PROJECT_ID);
        pvo.setName("csv-project");
        pvo.setDescription("csv 导出部分降级");
        pvo.setCreateBy("alice");
        when(projectMapper.queryProjectList(WORKSPACE_ID, null))
                .thenReturn(Collections.singletonList(pvo));
        // orchestrator 统计成功（workflowCount=5），节点统计失败
        Date recent = new Date(System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000); // 5 天前，不触发 STALE
        when(projectMapper.getProjectIdsByWorkspaceId(WORKSPACE_ID, 1))
                .thenReturn(Collections.singletonList(PROJECT_ID));
        ProjectOrcStatVO countVo = new ProjectOrcStatVO();
        countVo.setProjectId(PROJECT_ID);
        countVo.setCount(5);
        when(orchestratorMapper.countOrcByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(countVo));
        ProjectOrcStatVO timeVo = new ProjectOrcStatVO();
        timeVo.setProjectId(PROJECT_ID);
        timeVo.setUpdateTime(recent);
        when(orchestratorMapper.maxUpdateTimeByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenReturn(Collections.singletonList(timeVo));
        when(orchestratorMapper.countNodesByProjectIds(Collections.singletonList(PROJECT_ID)))
                .thenThrow(new RuntimeException("simulate node count unavailable on export"));

        // 捕获 CSV 输出
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // no-op
            }
        });

        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setWorkspaceId(WORKSPACE_ID);

        // when
        int rows = service.exportCsv(request, response);

        // then：返回写入行数
        assertEquals("导出 1 个项目应返回 1", 1, rows);
        String csv = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        // 表头 + 数据行
        String[] lines = csv.split("\n");
        assertTrue("CSV 应至少包含表头 + 1 数据行", lines.length >= 2);
        // 找到数据行（非表头行），按逗号切分校验列
        // 表头列序：项目ID(0),项目名称(1),描述(2),创建人(3),创建时间(4),更新时间(5),
        //          工作流数(6),节点数(7),数据源数(8),成员数(9),最近工作流更新时间(10),健康标签(11)
        String dataLine = findDataLine(lines, "csv-project");
        assertNotNull("CSV 中必须能定位到 csv-project 数据行", dataLine);
        String[] cols = dataLine.split(",", -1);
        assertEquals("工作流数列必须为正常数值 5", "5", cols[6]);
        assertEquals("节点统计失败时节点数列必须为降级占位符 '--'", "--", cols[7]);
    }

    // ============================== 场景 7 ==============================
    // getProjectDetail：job_type 未在目录表 → nodeTypeName=null 的项原样保留（前端回退显示 jobType）
    // ===================================================================================
    @Test
    public void getProjectDetail_whenJobTypeNotInCatalog_thenItemPreservedWithNullNodeTypeName() {
        // given：基础信息 + 工作流正常，节点分布返回一项 jobType 未命中目录表（nodeTypeName=null）
        DSSProjectDO project = new DSSProjectDO();
        project.setId(PROJECT_ID);
        project.setName("detail-unknown-jobtype");
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project);
        WorkflowSummaryVO wf = new WorkflowSummaryVO();
        wf.setOrchestratorId(300L);
        wf.setName("wf-unknown-node");
        wf.setStatus("已发布");
        when(orchestratorMapper.listOrcSummaryByProjectId(PROJECT_ID))
                .thenReturn(Collections.singletonList(wf));
        // jobType 未在 dss_workflow_node 目录表 → nodeTypeName 为 null
        NodeTypeDistributionVO unknownDist = new NodeTypeDistributionVO();
        unknownDist.setJobType("custom.unknown");
        unknownDist.setCount(3);
        unknownDist.setNodeTypeName(null);
        when(orchestratorMapper.listNodeTypeDistributionByProjectId(PROJECT_ID))
                .thenReturn(Collections.singletonList(unknownDist));

        // when
        ProjectDetailVO detail = service.getProjectDetail(PROJECT_ID, WORKSPACE_ID);

        // then：未知 jobType 项原样透传，不被过滤、不报错；前端按 jobType 回退显示
        assertNotNull("节点分布不应为 null", detail.getNodeTypeDistribution());
        assertEquals("未知 jobType 项必须原样保留，不被过滤", 1, detail.getNodeTypeDistribution().size());
        NodeTypeDistributionVO actual = detail.getNodeTypeDistribution().get(0);
        assertEquals("jobType 必须原样透传", "custom.unknown", actual.getJobType());
        assertEquals("count 必须正确", Integer.valueOf(3), actual.getCount());
        assertNull("jobType 未命中目录表时 nodeTypeName 必须为 null（前端回退显示 jobType）",
                actual.getNodeTypeName());
        // 其余区块正常
        assertNotNull("basicInfo 不受影响", detail.getBasicInfo());
        assertNotNull("workflowList 不受影响", detail.getWorkflowList());
        assertEquals("wf-unknown-node", detail.getWorkflowList().get(0).getName());
        assertEquals("工作流未失败 → workflowDegraded 必须为 false",
                Boolean.FALSE, detail.getWorkflowDegraded());
    }

    // ============================== 场景 8 ==============================
    // getProjectDetail：无节点项目 → nodeTypeDistribution 为空列表（非 null），查询成功无失败标记
    // ===================================================================================
    @Test
    public void getProjectDetail_whenNoNodes_thenReturnEmptyDistributionListNotDegrade() {
        // given：基础信息 + 工作流正常，节点分布查询返回空列表（项目无节点，非异常）
        DSSProjectDO project = new DSSProjectDO();
        project.setId(PROJECT_ID);
        project.setName("detail-no-nodes");
        when(projectMapper.selectById(PROJECT_ID)).thenReturn(project);
        WorkflowSummaryVO wf = new WorkflowSummaryVO();
        wf.setOrchestratorId(400L);
        wf.setName("wf-empty-nodes");
        wf.setStatus("已发布");
        when(orchestratorMapper.listOrcSummaryByProjectId(PROJECT_ID))
                .thenReturn(Collections.singletonList(wf));
        // 无节点 → mapper 正常返回空列表（非异常）
        when(orchestratorMapper.listNodeTypeDistributionByProjectId(PROJECT_ID))
                .thenReturn(Collections.<NodeTypeDistributionVO>emptyList());

        // when
        ProjectDetailVO detail = service.getProjectDetail(PROJECT_ID, WORKSPACE_ID);

        // then：节点分布为空列表（非 null）
        assertNotNull("无节点时 nodeTypeDistribution 必须为空列表而非 null", detail.getNodeTypeDistribution());
        assertTrue("无节点时 nodeTypeDistribution 必须为空列表", detail.getNodeTypeDistribution().isEmpty());
        // 查询成功：工作流未降级
        assertEquals("工作流查询成功 → workflowDegraded 必须为 false",
                Boolean.FALSE, detail.getWorkflowDegraded());
        // getProjectDetail 当前不主动置 nodeDegraded（分布的 try/catch 内聚于 getNodeTypeDistribution，
        // 异常已被吞并回退空列表，详情层无法观测分布失败）；断言其绝不激活失败标记（null/false 均可，不得为 true）
        assertFalse("节点分布查询成功时不得激活失败标记 nodeDegraded=true",
                Boolean.TRUE.equals(detail.getNodeDegraded()));
        // basicInfo / workflowList 正常
        assertNotNull("basicInfo 必须正常返回", detail.getBasicInfo());
        assertNotNull("workflowList 必须正常返回", detail.getWorkflowList());
        assertEquals(1, detail.getWorkflowList().size());
    }

    // ============================== 辅助方法 ==============================

    private ProjectResponse newProjectResponse(Long id, String description) {
        ProjectResponse resp = new ProjectResponse();
        resp.setId(id);
        resp.setDescription(description);
        return resp;
    }

    /** 从 CSV 行数组中定位包含指定项目名称的数据行（跳过表头）。 */
    private String findDataLine(String[] lines, String projectName) {
        for (String line : lines) {
            if (line != null && line.contains(projectName)) {
                return line;
            }
        }
        return null;
    }
}
