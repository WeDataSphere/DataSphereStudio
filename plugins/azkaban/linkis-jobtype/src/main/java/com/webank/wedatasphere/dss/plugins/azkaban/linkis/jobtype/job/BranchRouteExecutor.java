package com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.job;

import azkaban.jobExecutor.AbstractJob;
import com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.conf.LinkisJobTypeConf;
import org.apache.linkis.common.utils.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchRouteExecutor {

    private final AbstractJob job;
    private final Map<String, String> jobProps;

    public BranchRouteExecutor(AbstractJob job, Map<String, String> jobProps) {
        this.job = job;
        this.jobProps = jobProps;
    }

    public static boolean isBranchRouteJob(Map<String, String> jobProps) {
        return LinkisJobTypeConf.BRANCH_ROUTE_LINKIS_TYPE.equalsIgnoreCase(jobProps.get(LinkisJobTypeConf.LINKIS_TYPE_KEY));
    }

    public void execute() throws Exception {
        String flowExecId = jobProps.get(LinkisJobTypeConf.FLOW_EXEC_ID);
        String branchNodeId = jobProps.get(LinkisJobTypeConf.BRANCH_ROUTE_NODE_ID);
        String branchNodeName = jobProps.get(LinkisJobTypeConf.BRANCH_ROUTE_NODE_NAME);
        String branchRuleText = jobProps.get(LinkisJobTypeConf.BRANCH_ROUTE_RULE_TEXT);
        if (isBlank(branchNodeId)) {
            throw new IllegalStateException("Missing branch route node id.");
        }
        List<Map<String, Object>> targets = parseTargetDefinitions(jobProps.get(LinkisJobTypeConf.BRANCH_ROUTE_TARGETS));
        if (targets.isEmpty()) {
            throw new IllegalStateException("Missing branch route target definitions for node " + branchNodeId);
        }
        Map<String, String> targetNameToId = new LinkedHashMap<>();
        for (Map<String, Object> target : targets) {
            String targetId = stringValue(target.get("targetId"));
            String targetName = stringValue(target.get("targetName"));
            if (!isBlank(targetId)) {
                targetNameToId.put(targetId, targetId);
            }
            if (!isBlank(targetName) && !isBlank(targetId)) {
                targetNameToId.put(targetName, targetId);
            }
        }
        Map<String, String> context = buildEvaluationContext(branchNodeId, branchNodeName);
        job.info("Branch route node " + branchNodeName + " evaluate context: " + context);
        String selectedTargetId = selectTarget(branchRuleText, targetNameToId, context);
        BranchRuntimeStore.recordSelection(flowExecId, branchNodeId, selectedTargetId);
        job.info("Branch route node " + branchNodeName + " selected target id: " + selectedTargetId);
    }

    private Map<String, String> buildEvaluationContext(String branchNodeId, String branchNodeName) {
        Map<String, String> context = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : jobProps.entrySet()) {
            if (!isBlank(entry.getKey()) && entry.getValue() != null) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : jobProps.entrySet()) {
            if (entry.getKey().startsWith(LinkisJobTypeConf.FLOW_VARIABLE_PREFIX) && entry.getValue() != null) {
                context.put(entry.getKey().substring(LinkisJobTypeConf.FLOW_VARIABLE_PREFIX.length()), entry.getValue());
            }
        }
        mergeMissingVariables(context, BranchRuntimeStore.snapshotFlowVariables(jobProps.get(LinkisJobTypeConf.FLOW_EXEC_ID)));
        context.put("node.id", branchNodeId);
        context.put("node.name", branchNodeName == null ? "" : branchNodeName);
        context.put("node.type", LinkisJobTypeConf.BRANCH_ROUTE_LINKIS_TYPE);
        return context;
    }

    private void mergeMissingVariables(Map<String, String> context, Map<String, String> fallbackVariables) {
        if (fallbackVariables == null || fallbackVariables.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : fallbackVariables.entrySet()) {
            if (!isBlank(entry.getKey()) && entry.getValue() != null && !context.containsKey(entry.getKey())) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private String selectTarget(String branchRuleText, Map<String, String> targetNameToId, Map<String, String> context) {
        List<BranchExpressionUtils.BranchRule> rules = BranchExpressionUtils.parseBranchRules(branchRuleText);
        for (BranchExpressionUtils.BranchRule rule : rules) {
            if (!BranchExpressionUtils.isDefaultRule(rule) && BranchExpressionUtils.evaluateCondition(rule.getCondition(), context)) {
                String targetId = targetNameToId.get(rule.getTargetName());
                if (!isBlank(targetId)) {
                    return targetId;
                }
            }
        }
        for (BranchExpressionUtils.BranchRule rule : rules) {
            if (BranchExpressionUtils.isDefaultRule(rule)) {
                String targetId = targetNameToId.get(rule.getTargetName());
                if (!isBlank(targetId)) {
                    return targetId;
                }
            }
        }
        throw new IllegalStateException("No branch rule matched and no default rule was resolved.");
    }

    private List<Map<String, Object>> parseTargetDefinitions(String json) throws Exception {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        Object parsed = JsonUtils.jackson().readValue(json, List.class);
        List<Map<String, Object>> targets = new ArrayList<>();
        if (parsed instanceof List) {
            for (Object item : (List<?>) parsed) {
                if (item instanceof Map) {
                    targets.add(new LinkedHashMap<>((Map<String, Object>) item));
                }
            }
        }
        return targets;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

