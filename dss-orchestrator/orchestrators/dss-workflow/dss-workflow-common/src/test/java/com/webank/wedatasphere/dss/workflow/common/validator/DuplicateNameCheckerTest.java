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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 检查项 ③ 重名测试（ERROR）。AC3.1（两个节点 name 均为 load_data）。
 *
 * <p>v2.3：修复 nodeId 高亮 bug 后，按节点出 issue，nodeId 为节点身份（key 优先/id fallback），
 * 非 name 字符串。</p>
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
        // AC3.1: 两个节点 name 均为 load_data，key 分别 A/B
        DAGContext ctx = new DAGContextBuilder()
                .nodeKey("A", "load_data")
                .nodeKey("B", "load_data")
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals("两个重名节点应各出 1 条 issue", 2, issues.size());
        Set<String> nodeIds = new HashSet<>();
        for (ValidationIssue issue : issues) {
            assertEquals(IssueLevel.ERROR, issue.getLevel());
            assertEquals(ValidationIssue.RULE_DUPLICATE_NAME, issue.getRuleId());
            // nodeId 必须是节点身份（key），不能是 name 字符串 "load_data"
            nodeIds.add(issue.getNodeId());
            assertFalse("nodeId 不应是 name 字符串", "load_data".equals(issue.getNodeId()));
        }
        assertTrue("应覆盖两个重名节点身份 A 和 B", nodeIds.contains("A"));
        assertTrue("应覆盖两个重名节点身份 A 和 B", nodeIds.contains("B"));
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
        // 两组重复，每组 2 个节点，按节点出 issue 共 4 条
        assertEquals("两组重复应各按节点出 issue，共 4 条", 4, issues.size());
    }

    @Test
    public void shouldUseIdWhenKeyAbsentForNodeId() {
        // 无 key 节点：nodeId 应退化为 id（D-1）
        DSSNodeDefault n1 = new DSSNodeDefault();
        n1.setId("id1");
        n1.setName("dup");
        DSSNodeDefault n2 = new DSSNodeDefault();
        n2.setId("id2");
        n2.setName("dup");
        DAGContext ctx = new DAGContextBuilder()
                .node("id1", "dup", n1)
                .node("id2", "dup", n2)
                .build();
        List<ValidationIssue> issues = checker.check(ctx);
        assertEquals(2, issues.size());
        Set<String> nodeIds = new HashSet<>();
        for (ValidationIssue issue : issues) {
            nodeIds.add(issue.getNodeId());
        }
        assertTrue("无 key 时 nodeId 应为 id", nodeIds.contains("id1"));
        assertTrue("无 key 时 nodeId 应为 id", nodeIds.contains("id2"));
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
