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
import com.webank.wedatasphere.dss.workflow.common.parser.WorkFlowParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link DAGStructureValidator} 默认实现。设计依据：design-doc §5（算法伪码）/ §10（零侵入只读）。
 *
 * <p>纯函数式只读：无任何 Mapper/Repository 注入，无持久化能力；不修改 jsonFlow（原样透传）。
 * 解析复用 common 既有的 {@link WorkFlowParser}（与运行时解析一致），构建 DAGContext 后依次执行 4 项 Checker，
 * 批量汇总返回。</p>
 *
 * <p>异常兜底（design §8.3）：jsonFlow 为 null 或解析异常时，包成 1 条 PARSE_FAILED error 返回，
 * <b>不抛异常</b>打断 FlowRestfulApi 既有 try-catch 流程，保证只读不副作用。</p>
 *
 * <p>构造注入 WorkFlowParser：运行期由 server 的 {@code DefaultWorkFlowParser}(@Component) 注入；
 * 单元测试可传入测试桩。</p>
 */
@Component
public class DAGStructureValidatorImpl implements DAGStructureValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DAGStructureValidatorImpl.class);

    private final WorkFlowParser workFlowParser;

    @Autowired
    public DAGStructureValidatorImpl(WorkFlowParser workFlowParser) {
        this.workFlowParser = workFlowParser;
    }

    @Override
    public ValidationResult validate(String jsonFlow) {
        ValidationResult result = new ValidationResult();
        if (jsonFlow == null || jsonFlow.isEmpty()) {
            result.addIssue(parseFailed("工作流JSON为空"));
            return result;
        }

        // 解析 + 构建上下文（design §8.3：解析异常包成 PARSE_FAILED，不抛异常）
        DAGContext ctx;
        try {
            List<DSSNode> nodes = workFlowParser.getWorkFlowNodes(jsonFlow);
            List<DSSEdge> edges = workFlowParser.getWorkFlowEdges(jsonFlow);
            ctx = buildContext(nodes, edges);
        } catch (Exception e) {
            // 包括 JsonSyntaxException / IllegalStateException / NPE 等
            LOGGER.warn("DAG 结构校验解析 jsonFlow 失败", e);
            result.addIssue(parseFailed("工作流JSON解析失败: " + safeMsg(e)));
            return result;
        }

        // 依次执行 4 项检查，批量汇总（非遇错即停，design §5.6）
        List<StructureChecker> checkers = new ArrayList<>();
        checkers.add(new EdgeReferenceChecker());
        checkers.add(new CycleChecker());
        checkers.add(new DuplicateNameChecker());
        checkers.add(new StartEndStructureChecker());
        for (StructureChecker checker : checkers) {
            try {
                List<ValidationIssue> issues = checker.check(ctx);
                if (issues != null) {
                    result.addAll(issues);
                }
            } catch (Exception e) {
                // 单项检查异常不应中断其余检查（防御性，正常不应触发）
                LOGGER.warn("DAG 结构检查器执行异常: " + checker.getClass().getSimpleName(), e);
            }
        }
        return result;
    }

    /**
     * 构建 DAGContext：D-1 标识取值（key 优先/id fallback）+ 邻接表 + 入度/出度。design §5.1。
     */
    private DAGContext buildContext(List<DSSNode> nodes, List<DSSEdge> edges) {
        DAGContext ctx = new DAGContext();
        List<DSSNode> safeNodes = nodes == null ? Collections.<DSSNode>emptyList() : nodes;
        List<DSSEdge> safeEdges = edges == null ? Collections.<DSSEdge>emptyList() : edges;

        ctx.getNodes().addAll(safeNodes);
        ctx.getEdges().addAll(safeEdges);

        // 先注册节点标识（D-1）
        for (DSSNode node : safeNodes) {
            String identity = DAGIdentityUtils.identityOf(node);
            if (identity == null || identity.isEmpty()) {
                // 无 id 无 key 的异常节点不进标识集合；其连边会被边引用检查检出（design §8）
                continue;
            }
            ctx.registerNode(identity, DAGIdentityUtils.nameOf(node));
        }
        // 再登记边（更新邻接表/度数；含分支网关多 branchLabel 出边与自环）
        for (DSSEdge edge : safeEdges) {
            if (edge == null) {
                continue;
            }
            String source = edge.getSource();
            String target = edge.getTarget();
            if (source == null && target == null) {
                continue;
            }
            ctx.registerEdge(source, target);
        }
        return ctx;
    }

    private ValidationIssue parseFailed(String message) {
        ValidationIssue issue = new ValidationIssue(
                ValidationIssue.RULE_PARSE_FAILED, "解析", IssueLevel.ERROR, message);
        issue.setSuggestion("请检查工作流 JSON 格式是否完整");
        return issue;
    }

    private String safeMsg(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
