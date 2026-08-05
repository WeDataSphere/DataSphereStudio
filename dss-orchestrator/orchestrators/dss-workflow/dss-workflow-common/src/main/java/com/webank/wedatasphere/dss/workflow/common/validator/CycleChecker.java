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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查项 ②：环路检查（ERROR，Kahn 拓扑排序）。设计依据：design-doc §5.3。
 *
 * <p>基于 edges 的 source→target 构建邻接表 + 入度表（已在 DAGContext 中构建）。
 * Kahn 算法：入度=0 节点入队，出队时后继入度减 1，归零入队；若最终入队节点数 &lt; 总节点数 → 存在环，
 * <b>未被入队的节点即为环上节点</b>（含自环特例：source==target 使该点入度永&gt;0，自然落入环上节点）。</p>
 *
 * <p>分支网关多 branchLabel 出边均计入邻接表，不改变无环性判定（ADR-3）。O(V+E)。</p>
 *
 * <p>注：仅对存在于 {@code nodeIdentities} 集合中的节点做环检测（边引用异常导致的悬空 source/target
 * 由 {@link EdgeReferenceChecker} 检出，不在此重复处理，避免悬空端干扰环判定）。</p>
 */
public class CycleChecker implements StructureChecker {

    @Override
    public List<ValidationIssue> check(DAGContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        if (ctx.getNodeIdentities().isEmpty()) {
            return issues;
        }

        // 复制入度表（Kahn 会修改），只统计指向已注册节点的入边
        Map<String, Integer> inDegree = new HashMap<>();
        for (String id : ctx.getNodeIdentities()) {
            inDegree.put(id, 0);
        }
        for (String id : ctx.getNodeIdentities()) {
            List<String> succ = ctx.getAdjacency().get(id);
            if (succ == null) {
                continue;
            }
            for (String next : succ) {
                // 仅当后继是已注册节点时计入入度（悬空 target 不计入环判定）
                if (inDegree.containsKey(next)) {
                    inDegree.put(next, inDegree.get(next) + 1);
                }
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        int visited = 0;
        while (!queue.isEmpty()) {
            String n = queue.poll();
            visited++;
            List<String> succ = ctx.getAdjacency().get(n);
            if (succ == null) {
                continue;
            }
            for (String next : succ) {
                Integer deg = inDegree.get(next);
                if (deg == null) {
                    continue; // 悬空 target，跳过
                }
                deg = deg - 1;
                inDegree.put(next, deg);
                if (deg == 0) {
                    queue.add(next);
                }
            }
        }

        if (visited < ctx.getNodeIdentities().size()) {
            // 入度仍 > 0 的节点即为环上节点（含自环）
            List<String> cycleNodes = new ArrayList<>();
            for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
                if (e.getValue() > 0) {
                    cycleNodes.add(e.getKey());
                }
            }
            StringBuilder msg = new StringBuilder("存在环路，包含节点: ");
            for (int i = 0; i < cycleNodes.size(); i++) {
                if (i > 0) {
                    msg.append(", ");
                }
                msg.append(ctx.nameOf(cycleNodes.get(i)));
            }
            ValidationIssue issue = new ValidationIssue(
                    ValidationIssue.RULE_CYCLE, "环路", IssueLevel.ERROR, msg.toString());
            // 将环上节点列表拼接到 nodeId 字段，前端据此批量高亮
            issue.setNodeId(join(cycleNodes));
            issue.setSuggestion("移除构成环的连线");
            issues.add(issue);
        }
        return issues;
    }

    private String join(List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
