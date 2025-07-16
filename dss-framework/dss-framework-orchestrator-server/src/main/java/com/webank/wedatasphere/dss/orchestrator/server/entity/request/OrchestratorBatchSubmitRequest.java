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
package com.webank.wedatasphere.dss.orchestrator.server.entity.request;


import com.webank.wedatasphere.dss.common.label.LabelRouteVO;

import java.util.List;

public class OrchestratorBatchSubmitRequest {
    private List<OrchestratorSubmitRequest> submitRequestList;
    private LabelRouteVO labels;
    private String comment;

    public OrchestratorBatchSubmitRequest(List<OrchestratorSubmitRequest> submitRequestList) {
        this.submitRequestList = submitRequestList;
    }

    public OrchestratorBatchSubmitRequest() {
    }

    public OrchestratorBatchSubmitRequest(List<OrchestratorSubmitRequest> submitRequestList, LabelRouteVO labels, String comment) {
        this.submitRequestList = submitRequestList;
        this.labels = labels;
        this.comment = comment;
    }

    public List<OrchestratorSubmitRequest> getSubmitRequestList() {
        return submitRequestList;
    }

    public void setSubmitRequestList(List<OrchestratorSubmitRequest> submitRequestList) {
        this.submitRequestList = submitRequestList;
    }

    public LabelRouteVO getLabels() {
        return labels;
    }

    public void setLabels(LabelRouteVO labels) {
        this.labels = labels;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
