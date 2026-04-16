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

package com.webank.wedatasphere.dss.appconn.schedulis.linkisjob;

import com.webank.wedatasphere.dss.appconn.scheduler.utils.SchedulerConf;
import com.webank.wedatasphere.dss.appconn.schedulis.conf.AzkabanConf;
import com.webank.wedatasphere.dss.appconn.schedulis.constant.AzkabanConstant;
import com.webank.wedatasphere.dss.appconn.schedulis.constant.BranchSchedulisConstant;
import com.webank.wedatasphere.dss.appconn.schedulis.conversion.NodeConverter;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.workflow.core.constant.WorkflowConstant;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkisJobConverter implements NodeConverter {

    private LinkisJobTuning[] linkisJobTunings;

    public LinkisJobConverter(){
        this.linkisJobTunings = new LinkisJobTuning[]{new AzkabanSubFlowJobTuning(), new BranchRouteJobTuning()};
    }

    @Override
    public String conversion(WorkflowNode workflowNode){
        return baseConversion(workflowNode);
    }

    private String baseConversion(WorkflowNode workflowNode){
        LinkisJob job = new LinkisJob();
        job.setConf(new HashMap<>());
        job.setName(workflowNode.getName());
        job.setComment(workflowNode.getDSSNode().getDesc());
        convertHead(workflowNode,job);
        convertDependencies(workflowNode,job);
        convertProxyUser(workflowNode,job);
        convertConfiguration(workflowNode,job);
        convertBranchControl(workflowNode, job);
        convertJobCommand(workflowNode,job);
        Arrays.stream(linkisJobTunings).forEach(t ->{
            if(t.ifJobCantuning(workflowNode.getNodeType())) {
                t.tuningJob(job);
            }
        });
        return convertJobToString(job);
    }

    private String convertJobToString(LinkisJob job){
        HashMap<String, String> map = new HashMap<>(16);
        boolean decisionJob = BranchSchedulisConstant.DECISION_JOB_TYPE.equalsIgnoreCase(job.getType());
        if (!decisionJob) {
            map.put(AzkabanConstant.LINKIS_VERSION, AzkabanConf.LINKIS_VERSION.getValue());
            map.put(AzkabanConstant.LINKIS_TYPE,job.getLinkistype());
            map.put(AzkabanConstant.JOB_COMMAND,job.getCommand());
        }
        map.put(AzkabanConstant.JOB_TYPE,job.getType());
        map.put(AzkabanConstant.ZAKABAN_DEPENDENCIES_KEY,job.getDependencies());
        map.put(WorkflowConstant.PROXY_USER,job.getProxyUser());
        map.put(AzkabanConstant.JOB_COMMENT,job.getComment());
        map.put(AzkabanConstant.AUTO_DISABLED,job.getAutoDisabled());
        Map<String, Object> labels = new HashMap<>(1);
        labels.put("route", SchedulerConf.JOB_LABEL.getValue());
        map.put(AzkabanConstant.JOB_LABELS, DSSCommonUtils.COMMON_GSON.toJson(labels));
        map.putAll(job.getConf());
        StringBuilder stringBuilder = new StringBuilder();
        map.forEach((k,v)->{
            if(v != null) {
                //for value contains "\n"
                v = v.replace("\n", ";");
                stringBuilder.append(k).append("=").append(v).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    private void convertHead(WorkflowNode workflowNode, LinkisJob job){
        job.setType("linkis");
        job.setLinkistype(workflowNode.getNodeType());
    }

    private void convertDependencies(WorkflowNode workflowNode, LinkisJob job){
        List<String> dependencys = workflowNode.getDSSNode().getDependencys();
        if(dependencys != null && !dependencys.isEmpty()) {
            StringBuilder dependencies = new StringBuilder();
            dependencys.forEach(d -> dependencies.append(d).append(","));
            job.setDependencies(dependencies.substring(0,dependencies.length()-1));
        }
    }

    private void convertProxyUser(WorkflowNode workflowNode, LinkisJob job){
        String userProxy = workflowNode.getDSSNode().getUserProxy();
        if(!StringUtils.isEmpty(userProxy)) {
            job.setProxyUser(userProxy);
        }
    }

    private void convertConfiguration(WorkflowNode workflowNode, LinkisJob job){
        Map<String, Object> params = workflowNode.getDSSNode().getParams();
        if (params != null && !params.isEmpty()) {
            Map<String, Map<String,Object>> configuration = (Map<String, Map<String, Object>>) params.get("configuration");
            String confprefix = "node.conf.";
            if (configuration != null) {
                configuration.forEach((k,v)-> {
                    if(null!=v) {
                        v.forEach((k2, v2) -> {
                            if(v2!=null) {
                                String vStr = stringifyConfValue(v2);
                                if (AzkabanConstant.AUTO_DISABLED.equals(k2) ) {
                                    job.setAutoDisabled(vStr);
                                } else {
                                    job.getConf().put(confprefix + k + "." + k2, vStr);
                                }
                            }
                        });
                    }
                });
            }
        }

    }

    private void convertBranchControl(WorkflowNode workflowNode, LinkisJob job) {
        Map<String, Object> params = workflowNode.getDSSNode().getParams();
        if (params == null || params.isEmpty()) {
            return;
        }
        if (isBranchNode(workflowNode)) {
            putDecisionRules(job, stringifyConfValue(params.get(BranchSchedulisConstant.BRANCH_ROUTE_RULE_TEXT)));
        }
    }

    private boolean isBranchNode(WorkflowNode workflowNode) {
        return workflowNode != null && BranchSchedulisConstant.BRANCH_NODE_TYPE.equalsIgnoreCase(workflowNode.getNodeType());
    }

    private void putDecisionRules(LinkisJob job, String branchRuleText) {
        if (StringUtils.isBlank(branchRuleText)) {
            return;
        }
        List<DecisionRule> decisionRules = parseDecisionRules(branchRuleText);
        for (int i = 0; i < decisionRules.size(); i++) {
            DecisionRule rule = decisionRules.get(i);
            int index = i + 1;
            job.getConf().put(BranchSchedulisConstant.DECISION_CONDITION_PREFIX + index, rule.condition);
            job.getConf().put(BranchSchedulisConstant.DECISION_ON_SUCCESS_PREFIX + index, rule.targetJobName);
            job.getConf().put(BranchSchedulisConstant.DECISION_ON_FAILURE_PREFIX + index, rule.onFailure);
        }
    }

    private List<DecisionRule> parseDecisionRules(String branchRuleText) {
        Map<Integer, DecisionRuleBuilder> decisionRuleMap = new java.util.TreeMap<>();
        for (String ruleText : branchRuleText.split("[\\r\\n;]+")) {
            if (StringUtils.isBlank(ruleText)) {
                continue;
            }
            int separatorIndex = ruleText.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }
            String key = ruleText.substring(0, separatorIndex).trim();
            String value = ruleText.substring(separatorIndex + 1);
            IndexedRuleKey indexedRuleKey = parseIndexedRuleKey(key);
            if (indexedRuleKey == null) {
                continue;
            }
            DecisionRuleBuilder builder = decisionRuleMap.computeIfAbsent(indexedRuleKey.index, ignored -> new DecisionRuleBuilder());
            if ("condition".equals(indexedRuleKey.ruleType)) {
                builder.condition = value.trim();
            } else if ("on.success".equals(indexedRuleKey.ruleType)) {
                builder.targetJobName = value.trim();
            } else if ("on.failure".equals(indexedRuleKey.ruleType)) {
                builder.onFailure = value == null ? "" : value.trim();
            }
        }
        List<DecisionRule> rules = new ArrayList<>();
        for (DecisionRuleBuilder builder : decisionRuleMap.values()) {
            if (StringUtils.isBlank(builder.condition) || StringUtils.isBlank(builder.targetJobName)) {
                continue;
            }
            if (isUnsupportedDefaultKeyword(builder.condition)) {
                continue;
            }
            rules.add(new DecisionRule(builder.condition, builder.targetJobName, StringUtils.defaultString(builder.onFailure)));
        }
        return rules;
    }

    private IndexedRuleKey parseIndexedRuleKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (key.startsWith("condition.")) {
            return buildIndexedRuleKey("condition", key.substring("condition.".length()));
        }
        if (key.startsWith("on.success.")) {
            return buildIndexedRuleKey("on.success", key.substring("on.success.".length()));
        }
        if (key.startsWith("on.failure.")) {
            return buildIndexedRuleKey("on.failure", key.substring("on.failure.".length()));
        }
        return null;
    }

    private IndexedRuleKey buildIndexedRuleKey(String ruleType, String rawIndex) {
        if (StringUtils.isBlank(rawIndex)) {
            return null;
        }
        try {
            return new IndexedRuleKey(ruleType, Integer.parseInt(rawIndex.trim()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isUnsupportedDefaultKeyword(String condition) {
        if (StringUtils.isBlank(condition)) {
            return false;
        }
        String normalized = condition.trim().toLowerCase();
        return "default".equals(normalized) || "else".equals(normalized) || "*".equals(normalized);
    }
    private void putBranchConf(LinkisJob job, Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value != null) {
            job.getConf().put(key, stringifyConfValue(value));
        }
    }

    private String stringifyConfValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            Number numValue = (Number) value;
            return numValue.longValue() == numValue.doubleValue() ? String.valueOf(numValue.longValue()) : numValue.toString();
        }
        if (value instanceof CharSequence || value instanceof Boolean) {
            return value.toString();
        }
        return DSSCommonUtils.COMMON_GSON.toJson(value);
    }

    private void convertJobCommand(WorkflowNode workflowNode, LinkisJob job){
        Map<String, Object> jobContent = workflowNode.getDSSNode().getJobContent();
        if(jobContent != null) {
            jobContent.remove("jobParams");
            job.setCommand(DSSCommonUtils.COMMON_GSON.toJson(jobContent));
        }
    }

    private static class DecisionRule {
        private final String condition;
        private final String targetJobName;
        private final String onFailure;

        private DecisionRule(String condition, String targetJobName, String onFailure) {
            this.condition = condition;
            this.targetJobName = targetJobName;
            this.onFailure = onFailure;
        }
    }

    private static class DecisionRuleBuilder {
        private String condition;
        private String targetJobName;
        private String onFailure;
    }

    private static class IndexedRuleKey {
        private final String ruleType;
        private final int index;

        private IndexedRuleKey(String ruleType, int index) {
            this.ruleType = ruleType;
            this.index = index;
        }
    }
}
