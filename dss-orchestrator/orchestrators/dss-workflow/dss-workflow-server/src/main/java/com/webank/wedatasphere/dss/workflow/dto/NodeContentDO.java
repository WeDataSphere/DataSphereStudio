package com.webank.wedatasphere.dss.workflow.dto;


import java.util.Date;

public class NodeContentDO {
    private Long id;
    private String key;
    private String nodeId;
    private String jobType;
    private Long orchestratorId;
    private Date createTime;
    private Date modifyTime;
    private String modifyUser;

    public NodeContentDO() {
    }

    public NodeContentDO(Long id, String key, String nodeId, String jobType, Long orchestratorId, Date createTime, Date modifyTime, String modifyUser) {
        this.id = id;
        this.key = key;
        this.nodeId = nodeId;
        this.jobType = jobType;
        this.orchestratorId = orchestratorId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
    }

    public NodeContentDO(String key, String nodeId, String jobType, Long orchestratorId, Date createTime, Date modifyTime, String modifyUser) {
        this.key = key;
        this.nodeId = nodeId;
        this.jobType = jobType;
        this.orchestratorId = orchestratorId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.modifyUser = modifyUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}