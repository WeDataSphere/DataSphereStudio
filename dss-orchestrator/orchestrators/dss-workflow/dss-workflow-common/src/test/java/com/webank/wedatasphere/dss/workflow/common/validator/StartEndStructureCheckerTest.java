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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ④ 开始结束结构测试（WARN，含 D-2 断链）。AC4.1（孤岛）/ AC4.2（多起点）/ AC4.3（无终点）。
 */
public class StartEndStructureCheckerTest {

    private final StartEndStructureChecker checker = new StartEndStructureChecker();

    private boolean hasSubType(List<ValidationIssue> issues, String subType) {
        for (ValidationIssue i : issues) {
            if (subType.equals(i.getSubType())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNodeId(List<ValidationIssue> issues, String nodeId) {
        for (ValidationIssue i : issues) {
            if (nodeId.equals(i.getNodeId())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void shouldPassForLinearDag() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C")
                .build();
        assertTrue("线性 DAG 不应报告结构 warn", checker.check(ctx).isEmpty());
    }

    @Test
    public void shouldReportIsolatedNode() {
        // AC4.1: A->B 主链 + 孤岛 C（无入边无出边）
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("应报告孤岛 C", hasSubTypeAndNode(issues, ValidationIssue.SUB_ISOLATED, "C"));
        for (ValidationIssue i : issues) {
            assertEquals("结构检查应为 WARN 级", IssueLevel.WARN, i.getLevel());
        }
    }

    private boolean hasSubTypeAndNode(List<ValidationIssue> issues, String subType, String nodeId) {
        for (ValidationIssue i : issues) {
            if (subType.equals(i.getSubType()) && nodeId.equals(i.getNodeId())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void shouldReportMultiSource() {
        // AC4.2: 两个入度=0 节点（多起点）
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c").nodeKey("D", "d")
                .edge("A", "B").edge("C", "D")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("应报告多起点", hasSubType(issues, ValidationIssue.SUB_MULTI_SOURCE));
    }

    @Test
    public void shouldReportNoSink() {
        // AC4.3: 无出度=0 节点（无合理终点）—— 全图成环
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B").edge("B", "C").edge("C", "A")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("应报告无终点", hasSubType(issues, ValidationIssue.SUB_NO_SINK));
        assertTrue("全成环也应报告无起点", hasSubType(issues, ValidationIssue.SUB_NO_SOURCE));
    }

    @Test
    public void shouldReportUnreachableFromSource() {
        // 上游断链：主链 A->B，加独立环路 X<->Y（X,Y 既无源可达）
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b")
                .nodeKey("X", "x").nodeKey("Y", "y")
                .edge("A", "B")
                .edge("X", "Y").edge("Y", "X")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("X 应报告上游断链", hasSubTypeAndNode(issues, ValidationIssue.SUB_UNREACHABLE_FROM_SOURCE, "X"));
        assertTrue("Y 应报告上游断链", hasSubTypeAndNode(issues, ValidationIssue.SUB_UNREACHABLE_FROM_SOURCE, "Y"));
    }

    @Test
    public void shouldReportCannotReachSink() {
        // 下游断链：主链 A->B->C(C 终点)，加分支 B->D->E<->D（D,E 到不了终点）
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .nodeKey("D", "d").nodeKey("E", "e")
                .edge("A", "B").edge("B", "C")
                .edge("B", "D").edge("D", "E").edge("E", "D")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertTrue("D 应报告下游断链", hasSubTypeAndNode(issues, ValidationIssue.SUB_CANNOT_REACH_SINK, "D"));
        assertTrue("E 应报告下游断链", hasSubTypeAndNode(issues, ValidationIssue.SUB_CANNOT_REACH_SINK, "E"));
        // C 是终点，不应报告
        assertFalse("C 是终点不应报告", hasNodeId(issues, "C"));
    }

    @Test
    public void shouldNotReportDuplicateForOneNode() {
        // 去重：孤岛节点只归 ISOLATED，不再报上游/下游断链
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "a").nodeKey("B", "b").nodeKey("C", "c")
                .edge("A", "B")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        // C 是孤岛，只应出现 1 条针对 C 的 issue
        int cCount = 0;
        for (ValidationIssue i : issues) {
            if ("C".equals(i.getNodeId())) {
                cCount++;
            }
        }
        assertEquals("孤岛节点 C 只应报 1 次", 1, cCount);
    }

    @Test
    public void shouldReturnEmptyForEmptyWorkflow() {
        assertTrue("空工作流不应报告任何 warn", checker.check(new DAGContextBuilder().build()).isEmpty());
    }
}
