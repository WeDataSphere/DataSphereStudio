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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查项 ④：开始结束结构检查（WARN，含 D-2 断链精确边界）。设计依据：design-doc §5.5 / D-2。
 *
 * <p>全部基于入度/出度统计 + 有向可达性（BFS），O(V+E)、只读。子项：
 * <ul>
 *   <li><b>孤岛节点 ISOLATED</b>：入度=0 且出度=0（完全游离）。</li>
 *   <li><b>从起点不可达 UNREACHABLE_FROM_SOURCE</b>：入度&gt;0（非起点），但从所有入度=0 节点正向 BFS 不可达（上游断链）。</li>
 *   <li><b>无法到达终点 CANNOT_REACH_SINK</b>：出度&gt;0（非终点），但从该节点正向 BFS 到不了任何出度=0 节点（下游断链）。</li>
 *   <li><b>无合理起点 NO_SOURCE</b>：全图无入度=0 节点（图级 warn）。</li>
 *   <li><b>无合理终点 NO_SINK</b>：全图无出度=0 节点（图级 warn）。</li>
 *   <li><b>多起点 MULTI_SOURCE</b>：入度=0 节点数 &gt; 1（合法并行，仅提示，图级 warn）。</li>
 * </ul>
 * </p>
 *
 * <p><b>去重归类原则</b>（避免一个节点重复告警）：每节点按「孤岛 &gt; 上游断链 &gt; 下游断链」优先级仅归一类；
 * 多起点/无起点/无终点作为图级 warn 单独报，不与节点级重复。</p>
 *
 * <p><b>「断链」最终界定（ADR-7）</b>：有向意义上的可达性断裂——一个节点要么能从某起点沿边方向到达，
 * 要么能沿边方向到达某终点；若两者皆不满足（且非孤岛），即处于"断开段"。2 次 BFS 完备判定，
 * 不依赖"主连通分量"这类模糊概念。</p>
 *
 * <p>可达性 BFS 仅在已注册节点标识集合内传播；边引用异常导致的悬空端由 {@link EdgeReferenceChecker} 检出，
 * 不在此重复处理。空工作流（0 节点）跳过，不报任何 warn（design §8）。</p>
 */
public class StartEndStructureChecker implements StructureChecker {

    @Override
    public List<ValidationIssue> check(DAGContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        Set<String> identities = ctx.getNodeIdentities();
        if (identities.isEmpty()) {
            // 空工作流：4 项检查均跳过，不报孤岛/断链
            return issues;
        }

        // 起点（入度=0）/ 终点（出度=0）
        List<String> sources = new ArrayList<>();
        List<String> sinks = new ArrayList<>();
        for (String id : identities) {
            if (ctx.inDegree(id) == 0) {
                sources.add(id);
            }
            if (ctx.outDegree(id) == 0) {
                sinks.add(id);
            }
        }

        // 图级 warn
        if (sources.isEmpty()) {
            issues.add(graphWarn(ValidationIssue.SUB_NO_SOURCE,
                    "工作流无合理起点（全部成环已被环路检查阻断）", null));
        }
        if (sinks.isEmpty()) {
            issues.add(graphWarn(ValidationIssue.SUB_NO_SINK,
                    "工作流无合理终点", null));
        }
        if (sources.size() > 1) {
            issues.add(graphWarn(ValidationIssue.SUB_MULTI_SOURCE,
                    "存在多个起点(" + namesOf(ctx, sources) + ")，属并行执行，请确认", null));
        }

        // 可达性：正向 BFS（从所有起点出发）= 从起点可达的节点集
        Set<String> reachableFromSource = bfsForward(ctx, sources);
        // 反向：从所有终点逆向 BFS（在逆邻接表上正向走）= 能到达终点的节点集
        Set<String> reachableToSink = bfsReverse(ctx, sinks);

        // 节点级 warn（按 孤岛>上游断链>下游断链 优先级归类，每节点仅归一类）
        for (String id : identities) {
            int inDeg = ctx.inDegree(id);
            int outDeg = ctx.outDegree(id);
            if (inDeg == 0 && outDeg == 0) {
                issues.add(nodeWarn(ctx, id, ValidationIssue.SUB_ISOLATED,
                        "孤岛节点(无入边无出边): " + ctx.nameOf(id)));
            } else if (inDeg > 0 && !reachableFromSource.contains(id)) {
                issues.add(nodeWarn(ctx, id, ValidationIssue.SUB_UNREACHABLE_FROM_SOURCE,
                        "节点无法从起点到达(上游断链): " + ctx.nameOf(id)));
            } else if (outDeg > 0 && !reachableToSink.contains(id)) {
                issues.add(nodeWarn(ctx, id, ValidationIssue.SUB_CANNOT_REACH_SINK,
                        "节点无法到达终点(下游断链): " + ctx.nameOf(id)));
            }
        }
        return issues;
    }

    /** 正向 BFS：从起点集合沿 adjacency 传播，返回从起点可达的节点集（含起点自身）。 */
    private Set<String> bfsForward(DAGContext ctx, List<String> startNodes) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        for (String s : startNodes) {
            if (visited.add(s)) {
                queue.add(s);
            }
        }
        while (!queue.isEmpty()) {
            String n = queue.poll();
            List<String> succ = ctx.getAdjacency().get(n);
            if (succ == null) {
                continue;
            }
            for (String next : succ) {
                // 仅在已注册节点内传播（悬空 target 不参与可达性）
                if (ctx.getNodeIdentities().contains(next) && visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    /** 逆向 BFS：从终点集合沿 reverseAdjacency 传播，返回能到达某终点的节点集（含终点自身）。 */
    private Set<String> bfsReverse(DAGContext ctx, List<String> startNodes) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        for (String s : startNodes) {
            if (visited.add(s)) {
                queue.add(s);
            }
        }
        while (!queue.isEmpty()) {
            String n = queue.poll();
            List<String> pred = ctx.getReverseAdjacency().get(n);
            if (pred == null) {
                continue;
            }
            for (String prev : pred) {
                if (ctx.getNodeIdentities().contains(prev) && visited.add(prev)) {
                    queue.add(prev);
                }
            }
        }
        return visited;
    }

    private ValidationIssue graphWarn(String subType, String message, String nodeId) {
        ValidationIssue issue = new ValidationIssue(
                ValidationIssue.RULE_STRUCTURE, "开始结束结构", IssueLevel.WARN, message);
        issue.setSubType(subType);
        if (nodeId != null) {
            issue.setNodeId(nodeId);
        }
        return issue;
    }

    private ValidationIssue nodeWarn(DAGContext ctx, String id, String subType, String message) {
        ValidationIssue issue = new ValidationIssue(
                ValidationIssue.RULE_STRUCTURE, "开始结束结构", IssueLevel.WARN, message);
        issue.setSubType(subType);
        issue.setNodeId(id);
        issue.setSuggestion("请检查该节点的上下游连线是否完整");
        return issue;
    }

    private String namesOf(DAGContext ctx, List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ctx.nameOf(ids.get(i)));
        }
        return sb.toString();
    }
}
