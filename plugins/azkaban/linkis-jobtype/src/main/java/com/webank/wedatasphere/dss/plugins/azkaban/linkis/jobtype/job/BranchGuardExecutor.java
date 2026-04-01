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

public class BranchGuardExecutor {

    private final AbstractJob job;
    private final Map<String, String> jobProps;

    public BranchGuardExecutor(AbstractJob job, Map<String, String> jobProps) {
        this.job = job;
        this.jobProps = jobProps;
    }

    public boolean shouldSkip() throws Exception {
        List<Map<String, Object>> guardRules = parseGuardRules(jobProps.get(LinkisJobTypeConf.BRANCH_GUARD_RULES));
        if (guardRules.isEmpty()) {
            return false;
        }
        String flowExecId = jobProps.get(LinkisJobTypeConf.FLOW_EXEC_ID);
        for (Map<String, Object> guardRule : guardRules) {
            String branchNodeId = stringValue(guardRule.get("branchNodeId"));
            Set<String> allowedTargetIds = toStringSet(guardRule.get("allowedTargetIds"));
            String selectedTargetId = BranchRuntimeStore.getSelection(flowExecId, branchNodeId);
            if (isBlank(selectedTargetId)) {
                throw new IllegalStateException("Missing selected target for branch node " + branchNodeId + " before executing guarded node " + job.getId());
            }
            if (!allowedTargetIds.contains(selectedTargetId)) {
                job.info("Skip guarded node " + job.getId() + " because branch " + branchNodeId + " selected target " + selectedTargetId + " and allowed targets are " + allowedTargetIds);
                return true;
            }
        }
        return false;
    }

    private List<Map<String, Object>> parseGuardRules(String json) throws Exception {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        Object parsed = JsonUtils.jackson().readValue(json, List.class);
        List<Map<String, Object>> rules = new ArrayList<>();
        if (parsed instanceof List) {
            for (Object item : (List<?>) parsed) {
                if (item instanceof Map) {
                    rules.add(new LinkedHashMap<>((Map<String, Object>) item));
                }
            }
        }
        return rules;
    }

    private Set<String> toStringSet(Object value) {
        Set<String> values = new LinkedHashSet<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (item != null) {
                    values.add(String.valueOf(item));
                }
            }
        }
        return values;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
