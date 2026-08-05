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

import com.webank.wedatasphere.dss.common.entity.node.DSSNodeDefault;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ③ 重名测试（ERROR）。AC3.1（两个节点 name 均为 load_data）。
 */
public class DuplicateNameCheckerTest {

    private final DuplicateNameChecker checker = new DuplicateNameChecker();

    @Test
    public void shouldPassWhenAllNamesUnique() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "load_data")
                .nodeKey("B", "transform")
                .build();
        assertTrue(checker.check(ctx).isEmpty());
    }

    @Test
    public void shouldReportDuplicateNames() {
        // AC3.1: 两个节点 name 均为 load_data
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "load_data")
                .nodeKey("B", "load_data")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("应检出 1 条重名问题", 1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(IssueLevel.ERROR, issue.getLevel());
        assertEquals(ValidationIssue.RULE_DUPLICATE_NAME, issue.getRuleId());
        assertEquals("重复 name 应为 load_data", "load_data", issue.getNodeId());
    }

    @Test
    public void shouldReportMultipleDistinctDuplicateGroups() {
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "load")
                .nodeKey("B", "load")
                .nodeKey("C", "export")
                .nodeKey("D", "export")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("两组重复应各报 1 条", 2, issues.size());
    }

    @Test
    public void shouldIgnoreBlankNames() {
        DSSNodeDefault n1 = new DSSNodeDefault();
        n1.setKey("A"); // name 为 null
        DSSNodeDefault n2 = new DSSNodeDefault();
        n2.setKey("B"); // name 为 null
        DAGContext ctx = new DAGContextBuilder()
                .node("A", null, n1)
                .node("B", null, n2)
                .build();
        assertTrue("空 name 不应计入重名", checker.check(ctx).isEmpty());
    }

    @Test
    public void shouldReturnEmptyForEmptyWorkflow() {
        assertTrue(checker.check(new DAGContextBuilder().build()).isEmpty());
    }
}
