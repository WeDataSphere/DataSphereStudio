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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ① 边引用测试（ERROR）。AC1.1 / AC1.2。
 */
public class EdgeReferenceCheckerTest {

    private final EdgeReferenceChecker checker = new EdgeReferenceChecker();

    @Test
    public void shouldPassWhenAllEdgesReferenceExistingNodes() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "nodeA")
                .nodeKey("B", "nodeB")
                .edge("A", "B")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("合法边不应报告问题", issues.isEmpty());
    }

    @Test
    public void shouldReportWhenTargetNodeMissing() {
        // AC1.1: edge A->X, 节点 X 不存在
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "nodeA")
                .edge("A", "X")   // X 不存在
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("应检出 1 条边引用异常", 1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(IssueLevel.ERROR, issue.getLevel());
        assertEquals(ValidationIssue.RULE_EDGE_REF, issue.getRuleId());
        assertEquals("A->X", issue.getEdgeRef());
        assertTrue("定位缺失端 X", "X".equals(issue.getNodeId()));
    }

    @Test
    public void shouldReportWhenSourceNodeMissing() {
        // AC1.2: edge Y->A, 节点 Y 不存在
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "nodeA")
                .edge("Y", "A")   // Y 不存在
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("应检出 1 条边引用异常", 1, issues.size());
        assertEquals(IssueLevel.ERROR, issues.get(0).getLevel());
        assertEquals("Y->A", issues.get(0).getEdgeRef());
    }

    @Test
    public void shouldReportBothEndsWhenSourceAndTargetMissing() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "nodeA")
                .edge("Y", "Z")   // 两端都不存在
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("两端缺失应各报 1 条，共 2 条", 2, issues.size());
    }

    @Test
    public void shouldReturnEmptyForEmptyWorkflow() {
        DAGContext ctx = new DAGContextBuilder().build();
        assertTrue(checker.check(ctx).isEmpty());
    }
}
