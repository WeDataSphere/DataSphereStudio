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
package com.webank.wedatasphere.dss.workflow.entity.request;

import com.webank.wedatasphere.dss.common.label.LabelRouteVO;

import java.util.List;

public class BatchPublishWorkflowRequest {
    private List<Long> orchestratorList;
    private String comment;
    private LabelRouteVO labels;

    private String dssLabel;

    public BatchPublishWorkflowRequest(String comment, LabelRouteVO labels, String dssLabel) {
        this.comment = comment;
        this.labels = labels;
        this.dssLabel = dssLabel;
    }

    public BatchPublishWorkflowRequest() {
    }

    public BatchPublishWorkflowRequest(List<Long> orchestratorList, String comment, LabelRouteVO labels, String dssLabel) {
        this.orchestratorList = orchestratorList;
        this.comment = comment;
        this.labels = labels;
        this.dssLabel = dssLabel;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LabelRouteVO getLabels() {
        return labels;
    }

    public void setLabels(LabelRouteVO labels) {
        this.labels = labels;
    }

    public String getDssLabel() {
        return dssLabel;
    }

    public void setDssLabel(String dssLabel) {
        this.dssLabel = dssLabel;
    }

    public List<Long> getOrchestratorList() {
        return orchestratorList;
    }

    public void setOrchestratorList(List<Long> orchestratorList) {
        this.orchestratorList = orchestratorList;
    }
}
