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

import java.util.List;

public class EventReceiverNodeRequest {

    private Integer pageNow;

    private Integer pageSize;

    // 工作流名称
    private String orchestratorName;

    // 项目名称
    private List<String> projectNameList;

    // 节点名称
    private List<String> nodeNameList;


    private String msgReceiver;

    private String msgTopic;

    private String msgName;

    private String maxReceiveHours;

    private String msgSaveKey;

    private Boolean onlyReceiveToday;

    private Boolean msgReceiveUseRunDate;


    public Integer getPageNow() {
        return pageNow;
    }

    public void setPageNow(Integer pageNow) {
        this.pageNow = pageNow;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public List<String> getProjectNameList() {
        return projectNameList;
    }

    public void setProjectNameList(List<String> projectNameList) {
        this.projectNameList = projectNameList;
    }

    public List<String> getNodeNameList() {
        return nodeNameList;
    }

    public void setNodeNameList(List<String> nodeNameList) {
        this.nodeNameList = nodeNameList;
    }

    public String getMsgReceiver() {
        return msgReceiver;
    }

    public void setMsgReceiver(String msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    public String getMsgTopic() {
        return msgTopic;
    }

    public void setMsgTopic(String msgTopic) {
        this.msgTopic = msgTopic;
    }

    public String getMsgName() {
        return msgName;
    }

    public void setMsgName(String msgName) {
        this.msgName = msgName;
    }

    public String getMaxReceiveHours() {
        return maxReceiveHours;
    }

    public void setMaxReceiveHours(String maxReceiveHours) {
        this.maxReceiveHours = maxReceiveHours;
    }

    public String getMsgSaveKey() {
        return msgSaveKey;
    }

    public void setMsgSaveKey(String msgSaveKey) {
        this.msgSaveKey = msgSaveKey;
    }

    public Boolean getOnlyReceiveToday() {
        return onlyReceiveToday;
    }

    public void setOnlyReceiveToday(Boolean onlyReceiveToday) {
        this.onlyReceiveToday = onlyReceiveToday;
    }

    public Boolean getMsgReceiveUseRunDate() {
        return msgReceiveUseRunDate;
    }

    public void setMsgReceiveUseRunDate(Boolean msgReceiveUseRunDate) {
        this.msgReceiveUseRunDate = msgReceiveUseRunDate;
    }
}
