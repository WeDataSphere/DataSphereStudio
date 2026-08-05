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
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ② 环路测试（Kahn 拓扑排序，ERROR）。AC2.1（A->B->C->A）/ AC2.2（自环 A->A）。
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
        assertEquals("应检出 1 条环路", 1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(IssueLevel.ERROR, issue.getLevel());
        assertEquals(ValidationIssue.RULE_CYCLE, issue.getRuleId());
        // 环上节点应包含 A,B,C
        Set<String> cycleNodes = new HashSet<>(Arrays.asList(issue.getNodeId().split(",")));
        assertTrue("环上应含 A", cycleNodes.contains("A"));
        assertTrue("环上应含 B", cycleNodes.contains("B"));
        assertTrue("环上应含 C", cycleNodes.contains("C"));
    }

    @Test
    public void shouldDetectSelfLoop() {
        // AC2.2: 自环 A->A
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a")
                .edge("A", "A")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("自环应被检出为环路", 1, issues.size());
        assertEquals(IssueLevel.ERROR, issues.get(0).getLevel());
        assertTrue("环上应含 A", issues.get(0).getNodeId().contains("A"));
    }

    @Test
    public void shouldDetectPartialCycleInLargerGraph() {
        // A->B->C->B (B,C 成环，A 在环外)
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C").edge("C", "B")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("应检出 1 条环路", 1, issues.size());
        Set<String> cycleNodes = new HashSet<>(Arrays.asList(issues.get(0).getNodeId().split(",")));
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
