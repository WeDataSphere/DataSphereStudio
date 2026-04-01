package com.webank.wedatasphere.dss.appconn.schedulis.conversion;

import com.webank.wedatasphere.dss.appconn.schedulis.constant.BranchSchedulisConstant;
import com.webank.wedatasphere.dss.common.entity.node.DSSEdge;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ConvertedRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.PreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ProjectPreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.operation.WorkflowToRelConverter;
import com.webank.wedatasphere.dss.workflow.core.entity.Workflow;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNodeEdge;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BranchWorkflowRewriteConverter implements WorkflowToRelConverter {

    @Override
    public ConvertedRel convertToRel(PreConversionRel rel) {
        ((ProjectPreConversionRel) rel).getWorkflows().forEach(this::rewriteWorkflow);
        return (ConvertedRel) rel;
    }

    private void rewriteWorkflow(Workflow workflow) {
        if (workflow == null || CollectionUtils.isEmpty(workflow.getWorkflowNodes()) || CollectionUtils.isEmpty(workflow.getWorkflowNodeEdges())) {
            rewriteChildren(workflow);
            return;
        }
        Map<String, WorkflowNode> nodeById = workflow.getWorkflowNodes().stream()
            .collect(Collectors.toMap(WorkflowNode::getId, node -> node, (left, right) -> left, LinkedHashMap::new));
        Map<String, List<DSSEdge>> outgoingEdges = buildOutgoingEdges(workflow.getWorkflowNodeEdges());
        workflow.getWorkflowNodes().stream()
            .filter(this::isBranchNode)
            .forEach(branchNode -> rewriteBranchNode(branchNode, nodeById, outgoingEdges));
        rewriteChildren(workflow);
    }

    private void rewriteChildren(Workflow workflow) {
        if (workflow != null && workflow.getChildren() != null) {
            workflow.getChildren().forEach(this::rewriteWorkflow);
        }
    }

    private Map<String, List<DSSEdge>> buildOutgoingEdges(List<WorkflowNodeEdge> workflowNodeEdges) {
        Map<String, List<DSSEdge>> outgoingEdges = new LinkedHashMap<>();
        for (WorkflowNodeEdge workflowNodeEdge : workflowNodeEdges) {
            DSSEdge edge = workflowNodeEdge.getDSSEdge();
            if (edge == null || StringUtils.isBlank(edge.getSource()) || StringUtils.isBlank(edge.getTarget())) {
                continue;
            }
            outgoingEdges.computeIfAbsent(edge.getSource(), key -> new ArrayList<>()).add(edge);
        }
        outgoingEdges.values().forEach(edges -> edges.sort(Comparator.comparing(edge -> edge.getPriority() == null ? Integer.MAX_VALUE : edge.getPriority())));
        return outgoingEdges;
    }

    private void rewriteBranchNode(WorkflowNode branchNode,
                                   Map<String, WorkflowNode> nodeById,
                                   Map<String, List<DSSEdge>> outgoingEdges) {
        List<DSSEdge> directEdges = outgoingEdges.getOrDefault(branchNode.getId(), Collections.emptyList());
        if (directEdges.isEmpty()) {
            return;
        }
        annotateRouteMetadata(branchNode, directEdges, nodeById);
        Map<String, Set<String>> reachableTargetMap = collectReachableTargets(branchNode, directEdges, outgoingEdges);
        reachableTargetMap.forEach((nodeId, allowedTargets) -> {
            WorkflowNode node = nodeById.get(nodeId);
            if (node != null && !branchNode.getId().equals(node.getId())) {
                annotateGuardMetadata(node, branchNode, allowedTargets);
            }
        });
    }

    private void annotateRouteMetadata(WorkflowNode branchNode, List<DSSEdge> directEdges, Map<String, WorkflowNode> nodeById) {
        Map<String, Object> params = getOrCreateParams(branchNode);
        params.put(BranchSchedulisConstant.BRANCH_ROUTE_ENABLED, Boolean.TRUE);
        params.put(BranchSchedulisConstant.BRANCH_ROUTE_NODE_ID, branchNode.getId());
        params.put(BranchSchedulisConstant.BRANCH_ROUTE_NODE_NAME, branchNode.getName());
        params.put(BranchSchedulisConstant.BRANCH_ROUTE_RULE_TEXT, getBranchRuleText(branchNode));
        List<Map<String, Object>> targets = new ArrayList<>();
        for (DSSEdge edge : directEdges) {
            WorkflowNode targetNode = nodeById.get(edge.getTarget());
            if (targetNode == null) {
                continue;
            }
            Map<String, Object> target = new LinkedHashMap<>();
            target.put("targetId", targetNode.getId());
            target.put("targetName", targetNode.getName());
            target.put("priority", edge.getPriority());
            target.put("condition", edge.getCondition());
            target.put("branchKey", edge.getBranchKey());
            target.put("branchLabel", edge.getBranchLabel());
            target.put("default", edge.getDefault());
            targets.add(target);
        }
        params.put(BranchSchedulisConstant.BRANCH_ROUTE_TARGETS, targets);
    }

    private Map<String, Set<String>> collectReachableTargets(WorkflowNode branchNode,
                                                             List<DSSEdge> directEdges,
                                                             Map<String, List<DSSEdge>> outgoingEdges) {
        Map<String, Set<String>> reachableTargets = new LinkedHashMap<>();
        for (DSSEdge directEdge : directEdges) {
            String targetId = directEdge.getTarget();
            if (StringUtils.isBlank(targetId)) {
                continue;
            }
            Deque<String> queue = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            queue.add(targetId);
            while (!queue.isEmpty()) {
                String currentNodeId = queue.poll();
                if (!visited.add(currentNodeId) || branchNode.getId().equals(currentNodeId)) {
                    continue;
                }
                reachableTargets.computeIfAbsent(currentNodeId, key -> new LinkedHashSet<>()).add(targetId);
                for (DSSEdge next : outgoingEdges.getOrDefault(currentNodeId, Collections.emptyList())) {
                    if (StringUtils.isNotBlank(next.getTarget())) {
                        queue.add(next.getTarget());
                    }
                }
            }
        }
        return reachableTargets;
    }

    private void annotateGuardMetadata(WorkflowNode workflowNode, WorkflowNode branchNode, Set<String> allowedTargets) {
        if (CollectionUtils.isEmpty(allowedTargets)) {
            return;
        }
        Map<String, Object> params = getOrCreateParams(workflowNode);
        List<Map<String, Object>> guardRules = getOrCreateGuardRules(params);
        guardRules.removeIf(rule -> branchNode.getId().equals(String.valueOf(rule.get("branchNodeId"))));
        Map<String, Object> guardRule = new LinkedHashMap<>();
        guardRule.put("branchNodeId", branchNode.getId());
        guardRule.put("branchNodeName", branchNode.getName());
        guardRule.put("allowedTargetIds", new ArrayList<>(allowedTargets));
        guardRules.add(guardRule);
        params.put(BranchSchedulisConstant.BRANCH_GUARD_RULES, guardRules);
    }

    private List<Map<String, Object>> getOrCreateGuardRules(Map<String, Object> params) {
        Object existing = params.get(BranchSchedulisConstant.BRANCH_GUARD_RULES);
        if (existing instanceof List) {
            List<Map<String, Object>> copiedRules = new ArrayList<>();
            for (Object item : (List<?>) existing) {
                if (item instanceof Map) {
                    copiedRules.add(new LinkedHashMap<>((Map<String, Object>) item));
                }
            }
            return copiedRules;
        }
        return new ArrayList<>();
    }

    private Map<String, Object> getOrCreateParams(WorkflowNode workflowNode) {
        Map<String, Object> params = workflowNode.getDSSNode().getParams();
        if (params == null) {
            params = new HashMap<>();
            workflowNode.getDSSNode().setParams(params);
        }
        return params;
    }

    private boolean isBranchNode(WorkflowNode workflowNode) {
        return workflowNode != null && BranchSchedulisConstant.BRANCH_NODE_TYPE.equalsIgnoreCase(workflowNode.getNodeType());
    }

    private String getBranchRuleText(WorkflowNode workflowNode) {
        Map<String, Object> params = workflowNode.getDSSNode().getParams();
        if (params == null || params.isEmpty()) {
            return "";
        }
        Object topLevelRule = params.get("branch.rules");
        if (topLevelRule != null && StringUtils.isNotBlank(topLevelRule.toString())) {
            return topLevelRule.toString();
        }
        Object configurationObj = params.get("configuration");
        if (!(configurationObj instanceof Map)) {
            return "";
        }
        Object specialObj = ((Map<?, ?>) configurationObj).get("special");
        if (!(specialObj instanceof Map)) {
            return "";
        }
        Object specialRule = ((Map<?, ?>) specialObj).get("branch.rules");
        return specialRule == null ? "" : String.valueOf(specialRule);
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
