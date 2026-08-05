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
import com.webank.wedatasphere.dss.common.entity.node.DSSEdgeDefault;
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.common.entity.node.DSSNodeDefault;

/**
 * 单元测试辅助：从节点/边规格直接构建 {@link DAGContext}，绕开 JSON 解析，
 * 用于隔离测试各 Checker 的图算法逻辑。D-1 标识取值由本构建器显式控制：
 * 通过 {@link #node(String, String, String)} 设置 key/id/name，或 {@link #nodeKey(String, String)}
 * （仅 key）/ {@link #nodeId(String, String)}（仅 id）。
 */
public class DAGContextBuilder {

    private final DAGContext ctx = new DAGContext();

    /**
     * 添加节点。
     *
     * @param identity 用作 registerNode 的标识（应与 DAGIdentityUtils.identityOf 一致）
     * @param name     显示名（name/title）
     * @param raw      原始 DSSNode（供 DuplicateNameChecker 读取 name）
     */
    public DAGContextBuilder node(String identity, String name, DSSNode raw) {
        ctx.getNodes().add(raw);
        ctx.registerNode(identity, name);
        return this;
    }

    /** 仅 key 的节点（key=id=identity，name=name）。 */
    public DAGContextBuilder nodeKey(String key, String name) {
        DSSNodeDefault n = new DSSNodeDefault();
        n.setKey(key);
        n.setId(key);
        n.setName(name);
        return node(key, name, n);
    }

    /** 仅 id 的节点（id=identity，无 key，name=name）。 */
    public DAGContextBuilder nodeId(String id, String name) {
        DSSNodeDefault n = new DSSNodeDefault();
        n.setId(id);
        n.setName(name);
        return node(id, name, n);
    }

    /** key 与 id 并存的节点（D-1：取 key）。 */
    public DAGContextBuilder nodeKeyId(String key, String id, String name) {
        DSSNodeDefault n = new DSSNodeDefault();
        n.setKey(key);
        n.setId(id);
        n.setName(name);
        return node(key, name, n);
    }

    /** 添加边 source->target（同时加入 getEdges 与邻接表/度数）。 */
    public DAGContextBuilder edge(String source, String target) {
        DSSEdge e = new DSSEdgeDefault();
        e.setSource(source);
        e.setTarget(target);
        ctx.getEdges().add(e);
        ctx.registerEdge(source, target);
        return this;
    }

    public DAGContext build() {
        return ctx;
    }
}
