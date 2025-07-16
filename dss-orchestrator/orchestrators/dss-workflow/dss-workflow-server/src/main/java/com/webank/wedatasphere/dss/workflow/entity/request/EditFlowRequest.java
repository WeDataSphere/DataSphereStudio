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

import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;

import java.util.Map;

public class EditFlowRequest {
    private Long id;
    private Long orchestratorId;
    private String nodeKey;
    private String title;
    private String desc;
    private String businessTag;
    private String appTag;
    private String params;
    private String ecConfTemplateId;
    private String ecConfTemplateName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBusinessTag() {
        return businessTag;
    }

    public void setBusinessTag(String businessTag) {
        this.businessTag = businessTag;
    }

    public String getAppTag() {
        return appTag;
    }

    public void setAppTag(String appTag) {
        this.appTag = appTag;
    }

    public String getEcConfTemplateId() {
        return ecConfTemplateId;
    }

    public void setEcConfTemplateId(String ecConfTemplateId) {
        this.ecConfTemplateId = ecConfTemplateId;
    }

    public String getEcConfTemplateName() {
        return ecConfTemplateName;
    }

    public void setEcConfTemplateName(String ecConfTemplateName) {
        this.ecConfTemplateName = ecConfTemplateName;
    }
}
