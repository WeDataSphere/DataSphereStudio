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
package com.webank.wedatasphere.dss.flow.execution.entrance.entity;

import java.util.List;
import java.util.Map;

public class WorkflowExecuteInfoVo extends WorkflowExecuteInfo {
    List<Map<String, Object>> runningJobsList;
    List<Map<String, Object>> failedJobsList;
    List<Map<String, Object>> succeedJobsList;
    List<Map<String, Object>> pendingJobsList;
    List<Map<String, Object>> skippedJobsList;

    public WorkflowExecuteInfoVo() {
    }

    public List<Map<String, Object>> getRunningJobsList() {
        return runningJobsList;
    }

    public void setRunningJobsList(List<Map<String, Object>> runningJobsList) {
        this.runningJobsList = runningJobsList;
    }

    public List<Map<String, Object>> getFailedJobsList() {
        return failedJobsList;
    }

    public void setFailedJobsList(List<Map<String, Object>> failedJobsList) {
        this.failedJobsList = failedJobsList;
    }

    public List<Map<String, Object>> getSucceedJobsList() {
        return succeedJobsList;
    }

    public void setSucceedJobsList(List<Map<String, Object>> succeedJobsList) {
        this.succeedJobsList = succeedJobsList;
    }

    public List<Map<String, Object>> getPendingJobsList() {
        return pendingJobsList;
    }

    public void setPendingJobsList(List<Map<String, Object>> pendingJobsList) {
        this.pendingJobsList = pendingJobsList;
    }

    public List<Map<String, Object>> getSkippedJobsList() {
        return skippedJobsList;
    }

    public void setSkippedJobsList(List<Map<String, Object>> skippedJobsList) {
        this.skippedJobsList = skippedJobsList;
    }


}
