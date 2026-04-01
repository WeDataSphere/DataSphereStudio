package com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.job;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BranchRuntimeStore {

    private static final ConcurrentMap<String, ConcurrentMap<String, String>> BRANCH_SELECTIONS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ConcurrentMap<String, String>> FLOW_VARIABLES = new ConcurrentHashMap<>();

    private BranchRuntimeStore() {
    }

    public static void recordSelection(String flowExecId, String branchNodeId, String targetNodeId) {
        if (isBlank(flowExecId) || isBlank(branchNodeId) || isBlank(targetNodeId)) {
            return;
        }
        BRANCH_SELECTIONS.computeIfAbsent(flowExecId, key -> new ConcurrentHashMap<>()).put(branchNodeId, targetNodeId);
    }

    public static String getSelection(String flowExecId, String branchNodeId) {
        Map<String, String> selections = BRANCH_SELECTIONS.get(flowExecId);
        return selections == null ? null : selections.get(branchNodeId);
    }

    public static void mergeFlowVariables(String flowExecId, Map<String, String> variables) {
        if (isBlank(flowExecId) || variables == null || variables.isEmpty()) {
            return;
        }
        ConcurrentMap<String, String> flowVariableMap = FLOW_VARIABLES.computeIfAbsent(flowExecId, key -> new ConcurrentHashMap<>());
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (!isBlank(entry.getKey()) && entry.getValue() != null) {
                flowVariableMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static Map<String, String> snapshotFlowVariables(String flowExecId) {
        Map<String, String> flowVariableMap = FLOW_VARIABLES.get(flowExecId);
        return flowVariableMap == null ? Collections.emptyMap() : new LinkedHashMap<>(flowVariableMap);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
