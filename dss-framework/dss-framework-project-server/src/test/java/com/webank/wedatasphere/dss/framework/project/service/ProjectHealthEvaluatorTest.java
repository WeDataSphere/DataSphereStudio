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

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ProjectHealthEvaluator} 健康判定边界用例单测。
 *
 * <p>覆盖联调 26 条用例中 SIT 无样本的「健康判定逻辑」类场景：空项目 / 描述缺失 /
 * 长期未更新阈值边界 / 多标签共存组合。被测对象是无任何外部依赖的纯函数，<b>这里
 * 使用真实实例</b>（非 Mock），反射注入 {@code staleThresholdDays=90} 后直接验证
 * 判定语义。整套测试为纯 JUnit 单测，不启动 Spring 容器、不依赖 Mockito。
 *
 * <p><b>STALE 阈值边界精度说明</b>：evaluate 内部使用
 * {@code daysDiff = (now - latestTime) / 86400000}（整数除法，截断）再
 * {@code > staleThresholdDays}（严格大于）。对「恰好 N 整天前」构造的 latestTime，
 * now 取值点的微小偏移（Date 构造 → evaluate 调用之间的数微秒）被整数除法截断吸收，
 * 故 91 天 → STALE、90 天 → 非 STALE、89 天 → 非 STALE，三者均为确定性断言，
 * 不存在 ±1 天漂移。
 */
public class ProjectHealthEvaluatorTest {

    private ProjectHealthEvaluator evaluator;

    private static final int STALE_THRESHOLD_DAYS = 90;
    private static final long ONE_DAY_MS = 1000L * 60 * 60 * 24;

    @Before
    public void setUp() {
        evaluator = new ProjectHealthEvaluator();
        ReflectionTestUtils.setField(evaluator, "staleThresholdDays", STALE_THRESHOLD_DAYS);
    }

    // ============================== 场景 1 ==============================
    // 空项目 EMPTY_PROJECT：workflowCount == 0 命中；workflowCount == null（聚合失败）绝不命中
    // ===================================================================================
    @Test
    public void evaluate_whenWorkflowCountZero_thenEmptyProjectLabel() {
        Date recent = new Date(System.currentTimeMillis() - 10L * ONE_DAY_MS); // 10 天前，不触发 STALE

        // workflowCount == 0 → 命中 EMPTY_PROJECT
        List<String> labels = evaluator.evaluate("有效描述", 0, recent);
        assertTrue("workflowCount=0 必须命中 EMPTY_PROJECT",
                labels.contains(ProjectHealthEvaluator.EMPTY_PROJECT));

        // 边界守护：workflowCount == null（聚合失败）绝不假阳性 EMPTY_PROJECT
        List<String> degradedLabels = evaluator.evaluate("有效描述", null, null);
        assertFalse("workflowCount=null（聚合失败）绝不假阳性标记 EMPTY_PROJECT",
                degradedLabels.contains(ProjectHealthEvaluator.EMPTY_PROJECT));
    }

    // ============================== 场景 2 ==============================
    // 描述缺失 NO_DESCRIPTION：null / 空串 / 纯空白（trim 后）均命中；有效描述不命中
    // ===================================================================================
    @Test
    public void evaluate_whenDescriptionNullOrBlank_thenNoDescriptionLabel() {
        Date recent = new Date(System.currentTimeMillis() - 10L * ONE_DAY_MS);

        // null 描述
        assertTrue("null 描述必须命中 NO_DESCRIPTION",
                evaluator.evaluate(null, 2, recent).contains(ProjectHealthEvaluator.NO_DESCRIPTION));
        // 空串
        assertTrue("空串描述必须命中 NO_DESCRIPTION",
                evaluator.evaluate("", 2, recent).contains(ProjectHealthEvaluator.NO_DESCRIPTION));
        // 纯空白（含制表符），trim 后为空
        assertTrue("纯空白描述 trim 后必须命中 NO_DESCRIPTION",
                evaluator.evaluate("   \t  ", 2, recent).contains(ProjectHealthEvaluator.NO_DESCRIPTION));

        // 有效描述 → 不命中
        assertFalse("有效描述绝不命中 NO_DESCRIPTION",
                evaluator.evaluate("这是一个有效项目描述", 2, recent)
                        .contains(ProjectHealthEvaluator.NO_DESCRIPTION));
    }

    // ============================== 场景 3 ==============================
    // STALE 阈值边界：严格 >，91 天命中、90 天不命中、89 天不命中
    // ===================================================================================
    @Test
    public void evaluate_staleThresholdBoundary_strictGreaterThan() {
        String desc = "有效描述";
        Integer wfCount = 2;

        // 91 天前 → 超过阈值 → STALE
        Date elevenDays = new Date(System.currentTimeMillis() - 91L * ONE_DAY_MS);
        assertTrue("距今 91 天（> 阈值 90）必须命中 STALE",
                evaluator.evaluate(desc, wfCount, elevenDays).contains(ProjectHealthEvaluator.STALE));

        // 恰好 90 天前 → 等于阈值，严格 > 不含 → 非 STALE
        Date exactly90 = new Date(System.currentTimeMillis() - 90L * ONE_DAY_MS);
        assertFalse("恰好 90 天前（== 阈值）必须不命中 STALE（严格 > 语义）",
                evaluator.evaluate(desc, wfCount, exactly90).contains(ProjectHealthEvaluator.STALE));

        // 89 天前 → 低于阈值 → 非 STALE
        Date eightyNine = new Date(System.currentTimeMillis() - 89L * ONE_DAY_MS);
        assertFalse("距今 89 天（< 阈值）必须不命中 STALE",
                evaluator.evaluate(desc, wfCount, eightyNine).contains(ProjectHealthEvaluator.STALE));
    }

    // ============================== 场景 4 ==============================
    // 多标签共存（6 组合笛卡尔样本）：三类独立标签可同时命中，按集合断言（顺序无关）
    // ===================================================================================
    @Test
    public void evaluate_multiLabelCombinations_allSix() {
        String emptyDesc = "   "; // 纯空白，trim 后为空 → NO_DESCRIPTION
        String validDesc = "有效项目描述";
        Date staleTime = new Date(System.currentTimeMillis() - 91L * ONE_DAY_MS); // 91 天前 → STALE
        Date freshTime = new Date(System.currentTimeMillis() - 30L * ONE_DAY_MS); // 30 天前 → 非 STALE

        // A: wf=0, time=null, desc 空 → [EMPTY_PROJECT, NO_DESCRIPTION]
        assertLabelsEquals("A: 空项目 + 描述缺失 + 无更新时间",
                evaluator.evaluate(emptyDesc, 0, null),
                ProjectHealthEvaluator.EMPTY_PROJECT, ProjectHealthEvaluator.NO_DESCRIPTION);

        // B: wf=0, time=null, desc 有 → [EMPTY_PROJECT]
        assertLabelsEquals("B: 空项目 + 描述完整",
                evaluator.evaluate(validDesc, 0, null),
                ProjectHealthEvaluator.EMPTY_PROJECT);

        // C: wf=2, time=91 天前, desc 空 → [STALE, NO_DESCRIPTION]
        assertLabelsEquals("C: 长期未更新 + 描述缺失",
                evaluator.evaluate(emptyDesc, 2, staleTime),
                ProjectHealthEvaluator.STALE, ProjectHealthEvaluator.NO_DESCRIPTION);

        // D: wf=2, time=91 天前, desc 有 → [STALE]
        assertLabelsEquals("D: 仅长期未更新",
                evaluator.evaluate(validDesc, 2, staleTime),
                ProjectHealthEvaluator.STALE);

        // E: wf=2, time=30 天前, desc 空 → [NO_DESCRIPTION]
        assertLabelsEquals("E: 仅描述缺失",
                evaluator.evaluate(emptyDesc, 2, freshTime),
                ProjectHealthEvaluator.NO_DESCRIPTION);

        // F: wf=2, time=30 天前, desc 有 → [] 空列表（健康项目）
        assertLabelsEquals("F: 健康项目（无任何标签）",
                evaluator.evaluate(validDesc, 2, freshTime));
    }

    // ============================== 辅助方法 ==============================

    /**
     * 断言健康标签集合精确等于期望集合（顺序无关）。
     *
     * <p>evaluate 实际产出顺序为 EMPTY_PROJECT → STALE → NO_DESCRIPTION（代码顺序），
     * 但对前端而言标签是一个集合，故按集合比较以表达真实契约、避免对实现顺序过度耦合。
     */
    private void assertLabelsEquals(String message, List<String> actual, String... expected) {
        Set<String> expectedSet = new HashSet<>(Arrays.asList(expected));
        Set<String> actualSet = new HashSet<>(actual);
        assertEquals(message, expectedSet, actualSet);
    }
}
