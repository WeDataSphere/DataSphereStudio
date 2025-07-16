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
package com.webank.wedatasphere.dss.workflow.entity.response;

import java.util.List;

public class BatchEditNodeContentResponse {

    private Long orchestratorId;
    private String orchestratorName;

    private List<String> failNodeName;
    private List<String> successNodeName;

    private String errorMsg;
    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }


    public List<String> getFailNodeName() {
        return failNodeName;
    }

    public void setFailNodeName(List<String> failNodeName) {
        this.failNodeName = failNodeName;
    }

    public List<String> getSuccessNodeName() {
        return successNodeName;
    }

    public void setSuccessNodeName(List<String> successNodeName) {
        this.successNodeName = successNodeName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
