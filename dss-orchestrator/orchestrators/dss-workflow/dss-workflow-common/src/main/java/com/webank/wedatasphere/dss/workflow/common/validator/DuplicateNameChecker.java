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
import java.util.HashMap;
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
 * <p><b>与 service 层的关系</b>：service 层 {@code DSSFlowServiceImpl.checkIsExistSameFlow}(:1416-1420)
 * 已用 {@code workFlowNodes.stream().map(Node::getName).distinct().count() < size} 做了相同检查，
 * 抛 DSSErrorException(80001)。本 Checker 与之目标一致但体验更优（返回具体重复 name + 前端高亮）。
 * 二者并存：DAGStructureValidator（controller 前置）主导交互，service 层保留作防绕过兜底（零侵入，双重幂等无害）。</p>
 */
public class DuplicateNameChecker implements StructureChecker {

    @Override
    public List<ValidationIssue> check(DAGContext ctx) {
        List<ValidationIssue> issues = new ArrayList<>();
        // name -> 出现次数（仅统计非空 name）
        Map<String, Integer> nameCount = new HashMap<>();
        for (DSSNode node : ctx.getNodes()) {
            if (node == null) {
                continue;
            }
            String name = DAGIdentityUtils.nameOf(node);
            if (name == null || name.isEmpty()) {
                continue;
            }
            Integer c = nameCount.get(name);
            nameCount.put(name, c == null ? 1 : c + 1);
        }
        for (Map.Entry<String, Integer> e : nameCount.entrySet()) {
            if (e.getValue() > 1) {
                ValidationIssue issue = new ValidationIssue(
                        ValidationIssue.RULE_DUPLICATE_NAME, "重名", IssueLevel.ERROR,
                        "节点名重复: " + e.getKey() + " (共 " + e.getValue() + " 个)");
                issue.setNodeId(e.getKey());
                issue.setSuggestion("节点名需唯一（影响依赖解析与定位），请重命名重复节点");
                issues.add(issue);
            }
        }
        return issues;
    }
}
