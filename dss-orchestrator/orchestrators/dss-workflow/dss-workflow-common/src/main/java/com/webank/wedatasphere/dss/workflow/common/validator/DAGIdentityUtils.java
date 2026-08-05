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

import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.common.entity.node.DSSNodeDefault;

/**
 * DAG 节点标识/名称取值工具。设计依据：design-doc D-1 / §5.1。
 *
 * <p><b>D-1 节点标识取值规则</b>：key 优先、id fallback，与运行时
 * {@code DSSFlowServiceImpl.validateBranchNodeConfig()}(:493-498) 的解析逻辑完全一致，
 * 保证校验结果与实际执行行为对齐，杜绝误报/漏报。</p>
 *
 * <p>{@code DSSNodeDefault} 同时拥有 {@code id} 与 {@code key} 两个字段，二者在重命名时被设成同值
 * （DSSFlowServiceImpl:1580-1581）；edges.source/target 承载的是节点标识（重命名时用 oldKey/newKey
 * 同步更新），故边引用检查必须用同一套取值规则。</p>
 */
public final class DAGIdentityUtils {

    private DAGIdentityUtils() {
    }

    /**
     * 取节点唯一标识：key 优先，key 为空则退化为 id（D-1）。
     *
     * @param node 节点（DSSNodeDefault 实现 DSSNode）
     * @return 标识；若 id 与 key 均空则返回 null
     */
    public static String identityOf(DSSNode node) {
        if (node == null) {
            return null;
        }
        // D-1: key 优先
        String key = null;
        if (node instanceof DSSNodeDefault) {
            key = ((DSSNodeDefault) node).getKey();
        }
        if (key != null && !key.isEmpty()) {
            return key;
        }
        String id = node.getId();
        return (id != null && !id.isEmpty()) ? id : null;
    }

    /**
     * 取节点显示名：name（即 title）。DSSNode.getName() 实际返回 title。
     *
     * @return 名称；可能为 null（节点未设置 title 时）
     */
    public static String nameOf(DSSNode node) {
        if (node == null) {
            return null;
        }
        return node.getName();
    }
}
