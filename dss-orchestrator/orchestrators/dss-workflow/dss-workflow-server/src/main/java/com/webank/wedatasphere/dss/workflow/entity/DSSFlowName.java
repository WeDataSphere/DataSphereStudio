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
package com.webank.wedatasphere.dss.workflow.entity;

import java.util.ArrayList;
import java.util.List;

public class DSSFlowName {


    private List<String> nodeNameList = new ArrayList<>();

    private List<String> orchestratorNameList = new ArrayList<>();

    private List<String> templateNameList = new ArrayList<>();


    public List<String> getNodeNameList() {
        return nodeNameList;
    }

    public void setNodeNameList(List<String> nodeNameList) {
        this.nodeNameList = nodeNameList;
    }

    public List<String> getOrchestratorNameList() {
        return orchestratorNameList;
    }

    public void setOrchestratorNameList(List<String> orchestratorNameList) {
        this.orchestratorNameList = orchestratorNameList;
    }

    public List<String> getTemplateNameList() {
        return templateNameList;
    }

    public void setTemplateNameList(List<String> templateNameList) {
        this.templateNameList = templateNameList;
    }
}
