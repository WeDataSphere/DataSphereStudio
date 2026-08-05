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

package com.webank.wedatasphere.dss.workflow.common.validator;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ② 环路测试（Kahn 拓扑排序，ERROR）。AC2.1（A->B->C->A）/ AC2.2（自环 A->A）。
 *
 * <p>v2.4：修复 nodeId 高亮 bug 后，按环上节点各出一条 issue，nodeId 为单个节点身份（key 优先/id fallback），
 * 非逗号拼接串。前端 cy.getElementById(nodeId) 可逐个高亮。</p>
 */
public class CycleCheckerTest {

    private final CycleChecker checker = new CycleChecker();

    @Test
    public void shouldPassForAcyclicDag() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C")
                .build();
        assertTrue("无环 DAG 不应报告环路", checker.check(ctx).isEmpty());
    }

    @Test
    public void shouldDetectTriangleCycle() {
        // AC2.1: A->B->C->A
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C").edge("C", "A")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        // 3 个环上节点 → 按节点各出 1 条 issue，共 3 条（v2.4 修复高亮 bug）
        assertEquals("应按环上节点数出 issue（3 个环上节点 → 3 条）", 3, issues.size());
        Set<String> reportedIds = new HashSet<>();
        for (ValidationIssue issue : issues) {
            assertEquals(IssueLevel.ERROR, issue.getLevel());
            assertEquals(ValidationIssue.RULE_CYCLE, issue.getRuleId());
            String nodeId = issue.getNodeId();
            // 每条 issue 的 nodeId 必须是单个节点身份（非逗号拼接串）
            assertNotNull("nodeId 不应为 null", nodeId);
            assertFalse("nodeId 不应为逗号拼接串: " + nodeId, nodeId.contains(","));
            reportedIds.add(nodeId);
        }
        // 三个环上节点应各被报告一次
        Set<String> expected = new HashSet<>(Arrays.asList("A", "B", "C"));
        assertEquals("应覆盖全部环上节点 A,B,C", expected, reportedIds);
    }

    @Test
    public void shouldDetectSelfLoop() {
        // AC2.2: 自环 A->A
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a")
                .edge("A", "A")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        // 自环：仅 A 在环上 → 1 条 issue
        assertEquals("自环应按节点出 1 条 issue", 1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(IssueLevel.ERROR, issue.getLevel());
        assertEquals(ValidationIssue.RULE_CYCLE, issue.getRuleId());
        // nodeId 应为单个节点身份 "A"（非逗号串）
        assertEquals("A", issue.getNodeId());
        assertFalse("nodeId 不应含逗号", issue.getNodeId().contains(","));
    }

    @Test
    public void shouldDetectPartialCycleInLargerGraph() {
        // A->B->C->B (B,C 成环，A 在环外)
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C").edge("C", "B")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        // B,C 成环 → 按节点各出 1 条 issue，共 2 条；A 不在环上
        assertEquals("应按环上节点数出 issue（B,C → 2 条）", 2, issues.size());
        Set<String> cycleNodes = new HashSet<>();
        for (ValidationIssue issue : issues) {
            assertEquals(IssueLevel.ERROR, issue.getLevel());
            assertEquals(ValidationIssue.RULE_CYCLE, issue.getRuleId());
            assertFalse("nodeId 不应为逗号拼接串: " + issue.getNodeId(),
                    issue.getNodeId().contains(","));
            cycleNodes.add(issue.getNodeId());
        }
        assertTrue("环上应含 B", cycleNodes.contains("B"));
        assertTrue("环上应含 C", cycleNodes.contains("C"));
        assertFalse("A 不在环上", cycleNodes.contains("A"));
    }

    @Test
    public void shouldHandleBranchGatewayMultipleOutgoingEdges() {
        // 分支网关多 branchLabel 出边不构成环
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("G", "gateway").nodeKey("A", "a").nodeKey("B", "b")
                .edge("G", "A").edge("G", "B")
                .build();
        assertTrue("多出边分支网关不应误判为环", checker.check(ctx).isEmpty());
    }

    @Test
    public void shouldReturnEmptyForEmptyWorkflow() {
        assertTrue(checker.check(new DAGContextBuilder().build()).isEmpty());
    }
}
