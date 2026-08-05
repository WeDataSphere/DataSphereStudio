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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查项 ③：重名检查（ERROR）。设计依据：design-doc §5.4。
 *
 * <p>检查同一工作流内<b>节点名(name)</b>是否重复。name 即 {@code DSSNode.getName()}（实际返回 title），
 * 是用户可见显示名，且 DSS 的 dependencys 字段以 name 引用上游（{@code AzkabanWorkflowParser} 写入
 * {@code addDependency(node.getName())}）。name 重复会造成依赖解析歧义与展示混乱，故为 error。</p>
 *
 * <p>本检查仅针对 name，不涉及 id/key 唯一性（id/key 取值规则见 D-1）。</p>
 *
 * <p><b>issue 定位（v2.3 修复）</b>：按 name 分组后，对 {@code count>1} 的组里<b>每个节点各出一条 issue</b>，
 * {@code nodeId = DAGIdentityUtils.identityOf(node)}（key 优先/id fallback）。这样符合 ValidationIssue 契约
 * （nodeId 为节点身份标识，非 name 字符串），前端 {@code cy.getElementById(nodeId)} 能正确逐个高亮所有重名节点。
 * 旧实现把 nodeId 设成重复的 name 字符串，导致前端查不到元素、高亮失效。</p>
 *
 * <p><b>与 service 层的关系</b>：service 层 {@code DSSFlowServiceImpl.checkIsExistSameFlow}(:1416-1420)
 * 已用 {@code workFlowNodes.stream().map(Node::getName).distinct().count() < size} 做了相同检查，
 * 抛 DSSErrorException(80001)。本 Checker 与之目标一致但体验更优（返回具体重复 name + 前端高亮）。
 * 二者并存：DAGStructureValidator（controller 前置）主导交互，service 层保留作防绕过兜底（零侵入，双重幂等无害）。</p>
 */
public class DuplicateNameChecker implements StructureChecker {

    @Override
    public List<ValidationIssue> check(DAGContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        // name -> 命中该 name 的节点列表（仅统计非空 name；保留插入顺序便于稳定输出）
        Map<String, List<DSSNode>> nameToNodes = new LinkedHashMap<>();
        for (DSSNode node : ctx.getNodes()) {
            if (node == null) {
                continue;
            }
            String name = DAGIdentityUtils.nameOf(node);
            if (name == null || name.isEmpty()) {
                continue;
            }
            List<DSSNode> bucket = nameToNodes.get(name);
            if (bucket == null) {
                bucket = new ArrayList<>();
                nameToNodes.put(name, bucket);
            }
            bucket.add(node);
        }
        for (Map.Entry<String, List<DSSNode>> e : nameToNodes.entrySet()) {
            List<DSSNode> dupNodes = e.getValue();
            if (dupNodes.size() > 1) {
                String name = e.getKey();
                String message = "节点名重复: " + name + " (共 " + dupNodes.size() + " 个)";
                // 按节点出 issue：nodeId 取节点身份（key 优先/id fallback），前端据此逐个高亮
                for (DSSNode node : dupNodes) {
                    ValidationIssue issue = new ValidationIssue(
                            ValidationIssue.RULE_DUPLICATE_NAME, "重名", IssueLevel.ERROR, message);
                    issue.setNodeId(DAGIdentityUtils.identityOf(node));
                    issue.setSuggestion("节点名需唯一（影响依赖解析与定位），请重命名重复节点");
                    issues.add(issue);
                }
            }
        }
        return issues;
    }
}
