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

import com.webank.wedatasphere.dss.common.entity.node.DSSEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查项 ①：边引用检查（ERROR）。设计依据：design-doc §5.2。
 *
 * <p>遍历 edges，若 edge.source 或 edge.target 不在节点标识集合（D-1：key 优先/id fallback）
 * 中 → 边引用异常（悬空边），返回具体缺失端供前端高亮。结构损坏，必然无法正确执行，故 error 阻断保存。</p>
 */
public class EdgeReferenceChecker implements StructureChecker {

    @Override
    public List<ValidationIssue> check(DAGContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (DSSEdge edge : ctx.getEdges()) {
            if (edge == null) {
                continue;
            }
            String source = edge.getSource();
            String target = edge.getTarget();
            String edgeRef = source + "->" + target;
            if (source == null || !ctx.getNodeIdentities().contains(source)) {
                ValidationIssue issue = new ValidationIssue(
                        ValidationIssue.RULE_EDGE_REF, "边引用", IssueLevel.ERROR,
                        "边的起点指向不存在的节点: " + source);
                issue.setEdgeRef(edgeRef);
                issue.setNodeId(source);
                issue.setSuggestion("删除该悬空边或补齐缺失的节点");
                issues.add(issue);
            }
            if (target == null || !ctx.getNodeIdentities().contains(target)) {
                ValidationIssue issue = new ValidationIssue(
                        ValidationIssue.RULE_EDGE_REF, "边引用", IssueLevel.ERROR,
                        "边的终点指向不存在的节点: " + target);
                issue.setEdgeRef(edgeRef);
                issue.setNodeId(target);
                issue.setSuggestion("删除该悬空边或补齐缺失的节点");
                issues.add(issue);
            }
        }
        return issues;
    }
}
