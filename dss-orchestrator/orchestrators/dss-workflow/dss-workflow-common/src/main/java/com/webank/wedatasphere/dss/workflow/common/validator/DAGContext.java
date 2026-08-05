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
 * <p>一次解析 jsonFlow 构建，四项 Checker 共享，避免重复解析。包含：
 * <ul>
 *   <li>{@code nodes} / {@code edges} —— 原始节点/边（来自 WorkFlowParser，只读使用）。</li>
 *   <li>{@code nodeIdentities} —— 节点标识集合（D-1：key 优先、id fallback）。</li>
 *   <li>{@code identityToName} —— 标识 → 显示名(name/title)，供定位输出。</li>
 *   <li>{@code adjacency} —— 正向邻接表 source → [target]（含分支网关多 branchLabel 出边）。</li>
 *   <li>{@code reverseAdjacency} —— 逆向邻接表 target → [source]，供结构检查逆向 BFS。</li>
 *   <li>{@code inDeg} / {@code outDeg} —— 入度/出度（基于 edges 的 source/target 统计）。</li>
 * </ul>
 * </p>
 *
 * <p>纯数据载体，无可变行为。所有 Checker 只读访问本上下文，不修改。</p>
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
    /** target -> [source] */
    private final Map<String, List<String>> reverseAdjacency;
    private final Map<String, Integer> inDeg;
    private final Map<String, Integer> outDeg;

    public DAGContext() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nodeIdentities = new LinkedHashSet<>();
        this.identityToName = new LinkedHashMap<>();
        this.adjacency = new HashMap<>();
        this.reverseAdjacency = new HashMap<>();
        this.inDeg = new HashMap<>();
        this.outDeg = new HashMap<>();
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
        reverseAdjacency.put(identity, new ArrayList<String>());
        if (!inDeg.containsKey(identity)) {
            inDeg.put(identity, 0);
        }
        if (!outDeg.containsKey(identity)) {
            outDeg.put(identity, 0);
        }
        if (name != null) {
            identityToName.putIfAbsent(identity, name);
        }
    }

    /**
     * 登记一条有向边 source -> target（含自环、分支多出边），更新邻接表与度数。
     *
     * <p>注意：source/target 不一定在 nodeIdentities 中（边引用异常时），仍登记以便度数统计；
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
        // 逆向邻接
        List<String> in = reverseAdjacency.get(target);
        if (in == null) {
            in = new ArrayList<>();
            reverseAdjacency.put(target, in);
        }
        in.add(source);
        // 度数
        outDeg.put(source, outDeg.getOrDefault(source, 0) + 1);
        inDeg.put(target, inDeg.getOrDefault(target, 0) + 1);
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

    public Map<String, List<String>> getReverseAdjacency() {
        return reverseAdjacency;
    }

    public Map<String, Integer> getInDeg() {
        return inDeg;
    }

    public Map<String, Integer> getOutDeg() {
        return outDeg;
    }

    /** 便捷：取节点标识对应显示名，缺失回退为标识本身 */
    public String nameOf(String identity) {
        String name = identityToName.get(identity);
        return (name == null || name.isEmpty()) ? identity : name;
    }

    /** 便捷：取某节点入度（不存在视为 0） */
    public int inDegree(String identity) {
        Integer v = inDeg.get(identity);
        return v == null ? 0 : v;
    }

    /** 便捷：取某节点出度（不存在视为 0） */
    public int outDegree(String identity) {
        Integer v = outDeg.get(identity);
        return v == null ? 0 : v;
    }
}
