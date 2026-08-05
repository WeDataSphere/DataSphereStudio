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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link DAGStructureValidatorImpl} 集成测试：error+warn 批量返回、isPassed 判定、
 * jsonFlow 解析异常→PARSE_FAILED error 不抛异常，以及 D-1 节点标识取值规则。
 */
public class DAGStructureValidatorImplTest {

    private final DAGStructureValidator validator = new DAGStructureValidatorImpl(new TestWorkFlowParser());

    private String flow(String nodes, String edges) {
        return "{\"nodes\":[" + nodes + "],\"edges\":[" + edges + "]}";
    }

    private String node(String id, String key, String title) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        first = append(sb, first, "id", quoted(id));
        first = append(sb, first, "key", quoted(key));
        append(sb, first, "title", quoted(title));
        sb.append("}");
        return sb.toString();
    }

    private boolean append(StringBuilder sb, boolean first, String k, String v) {
        if (v == null) {
            return first;
        }
        if (!first) {
            sb.append(",");
        }
        sb.append("\"").append(k).append("\":").append(v);
        return false;
    }

    private String quoted(String s) {
        return s == null ? null : "\"" + s + "\"";
    }

    private String edge(String s, String t) {
        return "{\"source\":\"" + s + "\",\"target\":\"" + t + "\"}";
    }

    // ========== D-1 节点标识取值规则 ==========

    @Test
    public void d1_identityShouldPreferKeyWhenPresent() {
        // 节点有 key="k1" id="i1"，边引用 key="k1" → 合法（无 edge ref error）
        String json = flow(node("i1", "k1", "A") + "," + node("i2", "k2", "B"), edge("k1", "k2"));
        ValidationResult r = validator.validate(json);
        assertTrue("key 优先：边引用 key 应合法", r.getErrors().isEmpty());
    }

    @Test
    public void d1_edgeReferencingIdWhenKeyPresentShouldFail() {
        // 节点有 key="k1" id="i1"，边引用 id="i1" → edge ref error（identity 是 key 不是 id）
        String json = flow(node("i1", "k1", "A") + "," + node("i2", "k2", "B"), edge("i1", "k2"));
        ValidationResult r = validator.validate(json);
        assertFalse("边引用 id（当 key 存在时）应报 edge ref error", r.getErrors().isEmpty());
        assertEquals(ValidationIssue.RULE_EDGE_REF, r.getErrors().get(0).getRuleId());
    }

    @Test
    public void d1_identityShouldFallbackToIdWhenKeyAbsent() {
        // 节点仅 id="i1"（无 key），边引用 id="i1" → 合法（id fallback）
        String json = flow(node("i1", null, "A") + "," + node("i2", null, "B"), edge("i1", "i2"));
        ValidationResult r = validator.validate(json);
        assertTrue("id fallback：边引用 id 应合法", r.getErrors().isEmpty());
    }

    // ========== error + warn 批量返回 ==========

    @Test
    public void shouldBatchReturnErrorsAndWarnings() {
        // 环路(error) + 孤岛(warn) 同时存在，批量返回
        // A->B->C->A 环 + 孤岛 D
        String nodes = node("A", "A", "a") + "," + node("B", "B", "b") + ","
                + node("C", "C", "c") + "," + node("D", "D", "d");
        String edges = edge("A", "B") + "," + edge("B", "C") + "," + edge("C", "A");
        ValidationResult r = validator.validate(flow(nodes, edges));
        assertTrue("应检出 error（环路）", r.hasErrors());
        assertTrue("应检出 warn（孤岛 D）", r.hasWarnings());
        assertFalse("有 error 时 isPassed=false", r.isPassed());
        // 环路 issue 应为 ERROR
        boolean hasCycle = false;
        boolean hasIsolated = false;
        for (ValidationIssue i : r.getIssues()) {
            if (ValidationIssue.RULE_CYCLE.equals(i.getRuleId())) {
                hasCycle = true;
                assertEquals(IssueLevel.ERROR, i.getLevel());
            }
            if (ValidationIssue.SUB_ISOLATED.equals(i.getSubType()) && "D".equals(i.getNodeId())) {
                hasIsolated = true;
                assertEquals(IssueLevel.WARN, i.getLevel());
            }
        }
        assertTrue("批量结果应含环路 error", hasCycle);
        assertTrue("批量结果应含孤岛 warn", hasIsolated);
    }

    @Test
    public void shouldReturnPassedForCleanDag() {
        String nodes = node("A", "A", "a") + "," + node("B", "B", "b") + "," + node("C", "C", "c");
        String edges = edge("A", "B") + "," + edge("B", "C");
        ValidationResult r = validator.validate(flow(nodes, edges));
        assertTrue("合法 DAG 应通过", r.isPassed());
        assertFalse(r.hasErrors());
    }

    @Test
    public void shouldDetectDuplicateNameAsError() {
        String nodes = node("A", "A", "load_data") + "," + node("B", "B", "load_data");
        ValidationResult r = validator.validate(flow(nodes, ""));
        assertTrue("重名应检出 error", r.hasErrors());
        assertEquals(ValidationIssue.RULE_DUPLICATE_NAME, r.getErrors().get(0).getRuleId());
    }

    // ========== 解析异常 → PARSE_FAILED ==========

    @Test
    public void shouldReturnParseFailedForMalformedJson() {
        ValidationResult r = validator.validate("{not valid json");
        assertFalse("非法 JSON 不应通过", r.isPassed());
        assertEquals("应返回 1 条 PARSE_FAILED error", 1, r.getErrors().size());
        assertEquals(ValidationIssue.RULE_PARSE_FAILED, r.getErrors().get(0).getRuleId());
    }

    @Test
    public void shouldReturnParseFailedForNullJson() {
        ValidationResult r = validator.validate(null);
        assertFalse(r.isPassed());
        assertEquals(ValidationIssue.RULE_PARSE_FAILED, r.getErrors().get(0).getRuleId());
    }

    @Test
    public void shouldReturnPassedForEmptyWorkflow() {
        ValidationResult r = validator.validate(flow("", ""));
        assertTrue("空工作流应通过", r.isPassed());
        assertTrue(r.getIssues().isEmpty());
    }

    @Test
    public void shouldNeverReturnNull() {
        ValidationResult r = validator.validate(flow("", ""));
        assertTrue(r != null);
    }
}
