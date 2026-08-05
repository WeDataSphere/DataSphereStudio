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
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DAG 校验上下文。设计依据：design-doc §4.3 / §5.1。
 *
 * <p>一次解析 jsonFlow 构建，三项 Checker（边引用/环路/重名）共享，避免重复解析。包含：
 * <ul>
 *   <li>{@code nodes} / {@code edges} —— 原始节点/边（来自 WorkFlowParser，只读使用）。</li>
 *   <li>{@code nodeIdentities} —— 节点标识集合（D-1：key 优先、id fallback）。</li>
 *   <li>{@code identityToName} —— 标识 → 显示名(name/title)，供定位输出。</li>
 *   <li>{@code adjacency} —— 正向邻接表 source → [target]（含分支网关多 branchLabel 出边）。</li>
 * </ul>
 * </p>
 *
 * <p>纯数据载体，无可变行为。所有 Checker 只读访问本上下文，不修改。</p>
 *
 * <p><b>v2.3</b>：移除④「开始结束结构」后，{@code reverseAdjacency} / {@code inDeg} / {@code outDeg}
 * 已无 Checker 使用（环路检查自建入度表），故清理。</p>
 */
public class DAGContext {

    private final List<DSSNode> nodes;
    private final List<DSSEdge> edges;
    /** 节点标识集合（按插入顺序保留，便于稳定输出） */
    private final Set<String> nodeIdentities;
    /** 标识 -> 显示名 */
    private final Map<String, String> identityToName;
    /** source -> [target]（含分支多出边，不去重） */
    private final Map<String, List<String>> adjacency;

    public DAGContext() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nodeIdentities = new LinkedHashSet<>();
        this.identityToName = new LinkedHashMap<>();
        this.adjacency = new HashMap<>();
    }

    /**
     * 注册一个节点标识到上下文（构建邻接表/度数表的占位）。
     *
     * @param identity 节点标识（key 优先 / id fallback，外部已计算）
     * @param name     显示名（name/title），可能为 null
     */
    public void registerNode(String identity, String name) {
        nodeIdentities.add(identity);
        adjacency.put(identity, new ArrayList<String>());
        if (name != null) {
            identityToName.putIfAbsent(identity, name);
        }
    }

    /**
     * 登记一条有向边 source -> target（含自环、分支多出边），更新正向邻接表。
     *
     * <p>注意：source/target 不一定在 nodeIdentities 中（边引用异常时），仍登记以便邻接统计；
     * 边引用异常由 {@code EdgeReferenceChecker} 独立检出。</p>
     */
    public void registerEdge(String source, String target) {
        // 正向邻接
        List<String> out = adjacency.get(source);
        if (out == null) {
            out = new ArrayList<>();
            adjacency.put(source, out);
        }
        out.add(target);
    }

    public List<DSSNode> getNodes() {
        return nodes;
    }

    public List<DSSEdge> getEdges() {
        return edges;
    }

    public Set<String> getNodeIdentities() {
        return Collections.unmodifiableSet(nodeIdentities);
    }

    public Map<String, String> getIdentityToName() {
        return identityToName;
    }

    public Map<String, List<String>> getAdjacency() {
        return adjacency;
    }

    /** 便捷：取节点标识对应显示名，缺失回退为标识本身 */
    public String nameOf(String identity) {
        String name = identityToName.get(identity);
        return (name == null || name.isEmpty()) ? identity : name;
    }
}
