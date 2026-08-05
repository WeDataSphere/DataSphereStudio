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

/**
 * 单条 DAG 结构校验问题。
 *
 * <p>设计依据：design-doc §4.2。每条问题携带检查项、级别、定位信息（节点标识或边 source→target）、
 * 问题描述与修复建议文案，便于前端做错误列表 + 画布高亮（D-3）。</p>
 *
 * <p>字段说明：
 * <ul>
 *   <li>{@code ruleId} —— 检查项常量：EDGE_REF / CYCLE / DUPLICATE_NAME / STRUCTURE / PARSE_FAILED。</li>
 *   <li>{@code nodeId} —— 定位节点标识（key 优先、id fallback，D-1）。</li>
 *   <li>{@code edgeRef} —— 定位边 "source->target"（边引用异常时填）。</li>
 *   <li>{@code subType} —— 结构检查细分类型（ISOLATED / UNREACHABLE_FROM_SOURCE /
 *       CANNOT_REACH_SINK / NO_SOURCE / NO_SINK / MULTI_SOURCE）。</li>
 * </ul>
 * </p>
 */
public class ValidationIssue {

    /** 检查项：边引用 */
    public static final String RULE_EDGE_REF = "EDGE_REF";
    /** 检查项：环路 */
    public static final String RULE_CYCLE = "CYCLE";
    /** 检查项：重名 */
    public static final String RULE_DUPLICATE_NAME = "DUPLICATE_NAME";
    /** 检查项：开始结束结构 */
    public static final String RULE_STRUCTURE = "STRUCTURE";
    /** 解析失败（异常兜底） */
    public static final String RULE_PARSE_FAILED = "PARSE_FAILED";

    /** 结构子类型：孤岛节点 */
    public static final String SUB_ISOLATED = "ISOLATED";
    /** 结构子类型：从起点不可达（上游断链） */
    public static final String SUB_UNREACHABLE_FROM_SOURCE = "UNREACHABLE_FROM_SOURCE";
    /** 结构子类型：无法到达终点（下游断链） */
    public static final String SUB_CANNOT_REACH_SINK = "CANNOT_REACH_SINK";
    /** 结构子类型：无合理起点 */
    public static final String SUB_NO_SOURCE = "NO_SOURCE";
    /** 结构子类型：无合理终点 */
    public static final String SUB_NO_SINK = "NO_SINK";
    /** 结构子类型：多起点 */
    public static final String SUB_MULTI_SOURCE = "MULTI_SOURCE";

    private String ruleId;
    /** 检查项中文名（边引用/环路/重名/开始结束结构） */
    private String checkName;
    private IssueLevel level;
    private String nodeId;
    private String edgeRef;
    private String message;
    private String suggestion;
    private String subType;

    public ValidationIssue() {
    }

    public ValidationIssue(String ruleId, String checkName, IssueLevel level, String message) {
        this.ruleId = ruleId;
        this.checkName = checkName;
        this.level = level;
        this.message = message;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public IssueLevel getLevel() {
        return level;
    }

    public void setLevel(IssueLevel level) {
        this.level = level;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getEdgeRef() {
        return edgeRef;
    }

    public void setEdgeRef(String edgeRef) {
        this.edgeRef = edgeRef;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public String toString() {
        return "ValidationIssue{"
                + "ruleId='" + ruleId + '\''
                + ", checkName='" + checkName + '\''
                + ", level=" + level
                + (nodeId != null ? ", nodeId='" + nodeId + '\'' : "")
                + (edgeRef != null ? ", edgeRef='" + edgeRef + '\'' : "")
                + (subType != null ? ", subType='" + subType + '\'' : "")
                + ", message='" + message + '\''
                + '}';
    }
}
