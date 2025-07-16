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
package com.webank.wedatasphere.dss.workflow.dto;


import com.google.common.base.Objects;

import java.util.Date;

public class NodeContentDO {
    private Long id;
    private String nodeKey;
    private String nodeId;
    private String jobType;
    private Long orchestratorId;
    private Long flowId;
    private Date createTime;
    private Date modifyTime;
    private String modifyUser;

    public NodeContentDO() {
    }

    public NodeContentDO(Long id, String nodeKey, String nodeId, String jobType, Long orchestratorId, Date createTime, Date modifyTime, String modifyUser) {
        this.id = id;
        this.nodeKey = nodeKey;
        this.nodeId = nodeId;
        this.jobType = jobType;
        this.orchestratorId = orchestratorId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
    }

    public NodeContentDO(String nodeKey, String nodeId, String jobType, Long orchestratorId, Long flowId, Date createTime, Date modifyTime, String modifyUser) {
        this.nodeKey = nodeKey;
        this.nodeId = nodeId;
        this.jobType = jobType;
        this.orchestratorId = orchestratorId;
        this.flowId = flowId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeContentDO that = (NodeContentDO) o;
        return Objects.equal(nodeKey, that.nodeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeKey);
    }

    @Override
    public String toString() {
        return "NodeContentDO{" +
                "id=" + id +
                ", nodeKey='" + nodeKey + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", jobType='" + jobType + '\'' +
                ", orchestratorId='" + orchestratorId + '\'' +
                ", flowId='" + flowId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", modifyTime='" + modifyTime + '\'' +
                ", modifyUser='" + modifyUser + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }
}
