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

package com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype;


import azkaban.jobExecutor.AbstractJob;
import azkaban.utils.Props;
import com.webank.wedatasphere.dss.linkis.node.execution.conf.LinkisJobExecutionConfiguration;
import com.webank.wedatasphere.dss.linkis.node.execution.execution.impl.LinkisNodeExecutionImpl;
import com.webank.wedatasphere.dss.linkis.node.execution.job.Job;
import com.webank.wedatasphere.dss.linkis.node.execution.job.JobTypeEnum;
import com.webank.wedatasphere.dss.linkis.node.execution.job.LinkisJob;
import com.webank.wedatasphere.dss.linkis.node.execution.listener.LinkisExecutionListener;
import com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.conf.LinkisJobTypeConf;
import com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.job.JobBuilder;
import com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.log.AzkabanJobLog;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.utils.JsonUtils;
import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class AzkabanDssJobType extends AbstractJob {

    private static final String SENSITIVE_JOB_PROP_NAME_SUFFIX = "_X";
    private static final String SENSITIVE_JOB_PROP_VALUE_PLACEHOLDER = "[MASKED]";
    private static final String JOB_DUMP_PROPERTIES_IN_LOG = "job.dump.properties";

    private final Logger log;

    protected volatile Props jobProps;

    protected volatile Props sysProps;

    protected volatile Map<String, String> jobPropsMap;

    private final String type;

    private Job job;

    private volatile Props generatedProperties = new Props();

    private boolean isCanceled = false;

    public AzkabanDssJobType(String jobId, Props sysProps, Props jobProps, Logger log) {
        super(jobId, log);
        this.jobProps = jobProps;
        this.sysProps = sysProps;
        this.jobPropsMap = this.jobProps.getMapByPrefix("");
        this.log = log;
        this.type = jobProps.getString(JOB_TYPE, LinkisJobExecutionConfiguration.JOB_DEFAULT_TYPE.getValue(this.jobPropsMap));
        if(!LinkisJobExecutionConfiguration.JOB_DEFAULT_TYPE.getValue(this.jobPropsMap).equalsIgnoreCase(this.type) ){
            throw new RuntimeException("This job(" + this.type + " )is not linkis type");
        }
    }

    @Override
    public void run() throws Exception {
        info("Start to execute job");
        logJobProperties();
        String runDate = getRunDate();
        if (StringUtils.isNotBlank(runDate)) {
            this.jobPropsMap.put("run_date", runDate);
        }
        String runTodayH = getRunTodayh(false);
        if (StringUtils.isNotBlank(runTodayH)) {
            this.jobPropsMap.put("run_today_h", runTodayH);
            this.jobPropsMap.put("run_today_hour", runTodayH);
        }

        this.job = JobBuilder.getAzkanbanBuilder().setJobProps(this.jobPropsMap).build();
        this.job.setLogObj(new AzkabanJobLog(this));
        if(JobTypeEnum.EmptyJob == ((LinkisJob)this.job).getJobType()){
            warn("This node is empty type");
            return;
        }
        info("runtimeMap is " + job.getRuntimeParams());
        LinkisNodeExecutionImpl.getLinkisNodeExecution().runJob(this.job);

        try {
            LinkisNodeExecutionImpl.getLinkisNodeExecution().waitForComplete(this.job);
        } catch (Exception e) {
            warn("Failed to execute job", e);
            throw e;
        }
        try {
            String endLog = LinkisNodeExecutionImpl.getLinkisNodeExecution().getLog(this.job);
            info(endLog);
        } catch (Throwable e){
            info("Skip fetching end log because no final log content is available from Linkis.");
        }

        LinkisExecutionListener listener = (LinkisExecutionListener)LinkisNodeExecutionImpl.getLinkisNodeExecution();
        listener.onStatusChanged(null,  LinkisNodeExecutionImpl.getLinkisNodeExecution().getState(this.job),this.job);
        int resultSize =  0;
        try{
            resultSize = LinkisNodeExecutionImpl.getLinkisNodeExecution().getResultSize(this.job);
        }catch(final Throwable t){
            error("failed to get result size");
            resultSize = -1;
        }
        for (int i = 0; i < resultSize; i++) {
            String result = LinkisNodeExecutionImpl.getLinkisNodeExecution().getResult(this.job, i, LinkisJobExecutionConfiguration.RESULT_PRINT_SIZE.getValue(this.jobPropsMap));
            if (result.length() > LinkisJobTypeConf.LOG_MAX_RESULTSIZE.getValue()) {
                result = result.substring(0, LinkisJobTypeConf.LOG_MAX_RESULTSIZE.getValue());
            }
            info("The content of the " + (i + 1) + "th resultset is :" + result);
        }
        collectBranchVariables();
        info("Finished to execute job");
    }

    @Override
    public void cancel() throws Exception {
        LinkisNodeExecutionImpl.getLinkisNodeExecution().cancel(this.job);
        isCanceled = true;
        warn("This job has been canceled");
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public Props getJobGeneratedProperties() {
        return this.generatedProperties == null ? new Props() : this.generatedProperties;
    }

    @Override
    public double getProgress() throws Exception {
        return LinkisNodeExecutionImpl.getLinkisNodeExecution().getProgress(this.job);
    }

    private void collectBranchVariables() {
        Map<String, String> mergedVariables = new LinkedHashMap<>(collectFlowVariablesFromJobProps());
        try {
            if (this.job != null) {
                Map<String, String> resultVariables = LinkisNodeExecutionImpl.getLinkisNodeExecution().getResultVariables(this.job, 128);
                Map<String, String> resolvedVariables = resolveBranchOutputVariables(resultVariables);
                if (!resolvedVariables.isEmpty()) {
                    mergedVariables.putAll(resolvedVariables);
                    info("Collected branch result variables: " + resolvedVariables);
                }
            }
        } catch (Throwable t) {
            warn("Failed to collect branch flow variables from current job.", t);
        }
        if (!mergedVariables.isEmpty()) {
            Props props = new Props();
            props.putAll(mergedVariables);
            this.generatedProperties = props;
            info("Collected generated flow variables: " + mergedVariables);
        }
    }

    private Map<String, String> collectFlowVariablesFromJobProps() {
        Map<String, String> flowVariables = new LinkedHashMap<>();
        if (this.jobPropsMap == null || this.jobPropsMap.isEmpty()) {
            return flowVariables;
        }
        for (Map.Entry<String, String> entry : this.jobPropsMap.entrySet()) {
            if (entry.getKey() != null
                    && entry.getKey().startsWith(LinkisJobTypeConf.FLOW_VARIABLE_PREFIX)
                    && entry.getValue() != null) {
                String variableKey = entry.getKey().substring(LinkisJobTypeConf.FLOW_VARIABLE_PREFIX.length());
                if (!"user.to.proxy".equals(variableKey)) {
                    flowVariables.put(variableKey, entry.getValue());
                }
            }
        }
        return flowVariables;
    }
    private Map<String, String> resolveBranchOutputVariables(Map<String, String> resultVariables) {
        if (resultVariables == null || resultVariables.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> mappings = getBranchOutputMappings();
        if (mappings.isEmpty()) {
            return resultVariables;
        }
        Map<String, String> mappedVariables = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            if (resultVariables.containsKey(entry.getValue())) {
                mappedVariables.put(entry.getKey(), resultVariables.get(entry.getValue()));
            }
        }
        return mappedVariables.isEmpty() ? resultVariables : mappedVariables;
    }

    private Map<String, String> getBranchOutputMappings() {
        String raw = this.jobPropsMap.get(LinkisJobTypeConf.BRANCH_OUTPUT_MAPPING);
        if (StringUtils.isBlank(raw)) {
            raw = this.jobPropsMap.get(LinkisJobTypeConf.BRANCH_OUTPUT_MAPPING_ALIAS);
        }
        if (StringUtils.isBlank(raw)) {
            return Collections.emptyMap();
        }
        try {
            if (raw.trim().startsWith("{")) {
                Map<String, Object> parsed = JsonUtils.jackson().readValue(raw, Map.class);
                Map<String, String> mappings = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : parsed.entrySet()) {
                    if (entry.getValue() != null) {
                        mappings.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
                return mappings;
            }
        } catch (Throwable t) {
            warn("Failed to parse branch output mapping as JSON, fallback to key=value parsing.", t);
        }
        Map<String, String> mappings = new LinkedHashMap<>();
        for (String pair : raw.split(",")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && StringUtils.isNotBlank(parts[0]) && StringUtils.isNotBlank(parts[1])) {
                mappings.put(parts[0].trim(), parts[1].trim());
            }
        }
        return mappings;
    }

    private void logJobProperties() {
        if (this.jobProps != null && this.jobProps.getBoolean(JOB_DUMP_PROPERTIES_IN_LOG, true)) {
            try {
                this.info("******   Job properties   ******");
                this.info(String.format("- Note : value is masked if property name ends with '%s'.", SENSITIVE_JOB_PROP_NAME_SUFFIX));
                for (final Map.Entry<String, String> entry : this.jobPropsMap.entrySet()) {
                    final String key = entry.getKey();
                    final String value = key.endsWith(SENSITIVE_JOB_PROP_NAME_SUFFIX) ?
                            SENSITIVE_JOB_PROP_VALUE_PLACEHOLDER :
                            entry.getValue();
                    this.info(String.format("%s=%s", key, value));
                }
                this.info("****** End Job properties  ******");
            } catch (final Exception ex) {
                this.log.error("failed to log job properties ", ex);
            }
        }
    }

    private String getRunDate(){
        this.info("begin to get run date");
        if (this.jobProps != null && this.jobProps.getBoolean(JOB_DUMP_PROPERTIES_IN_LOG, true)) {
            try {
                for (final Map.Entry<String, String> entry : this.jobPropsMap.entrySet()) {
                    final String key = entry.getKey();
                    final String value = key.endsWith(SENSITIVE_JOB_PROP_NAME_SUFFIX) ?
                            SENSITIVE_JOB_PROP_VALUE_PLACEHOLDER :
                            entry.getValue();
                    if ("azkaban.flow.start.timestamp".equals(key)){
                        this.info("run time is " + value);
                        String runDateNow = value.substring(0, 10).replaceAll("-", "");
                        this.info("run date now is " + runDateNow);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                        try {
                            Date date = simpleDateFormat.parse(runDateNow);
                            String runDate = simpleDateFormat.format(new Date(date.getTime() - 24 * 60 * 60 * 1000));
                            this.info("runDate is " + runDate);
                            return runDate;
                        } catch (ParseException e) {
                            this.log.error("failed to parse run date " + runDateNow, e);
                        }
                    }
                }
            } catch (final Exception ex) {
                this.log.error("failed to get run date ", ex);
            }
        }
        return null;
    }

    private String getRunTodayh(boolean stdFormat) {
        this.info("begin to get run_today_h");
        if (this.jobProps != null && this.jobProps.getBoolean(JOB_DUMP_PROPERTIES_IN_LOG, true)) {
            try {
                for (final Map.Entry<String, String> entry : this.jobPropsMap.entrySet()) {
                    final String key = entry.getKey();
                    final String value = key.endsWith(SENSITIVE_JOB_PROP_NAME_SUFFIX) ?
                            SENSITIVE_JOB_PROP_VALUE_PLACEHOLDER :
                            entry.getValue();
                    if ("azkaban.flow.start.timestamp".equals(key)) {
                        this.info("run time is " + value);
                        String runTodayh = value.substring(0, 13).replaceAll("-", "").replaceAll("T", "");
                        this.info("run today h is " + runTodayh);
                        if(!stdFormat){
                            return runTodayh;
                        }
                    }
                }
            } catch (final Exception ex) {
                this.log.error("failed to get run_today_h ", ex);
            }
        }
        return null;
    }
}

