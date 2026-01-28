package com.webank.wedatasphere.dss.framework.project.dao.entity;

import java.util.Date;

public class ProjectOperateRecordDO {
    private Long id;
    /**
     * 操作记录唯一Id
     */
    private String recordId;
    /**
     * 空间id
     */
    private Long workspaceId;
    /**
     * 项目Id
     */
    private Long projectId;
    /**
     * 操作类型
     */
    private Integer operateType;
    /**
     * 操作状态
     */
    private Integer status;
    /**
     * 执行任务的实例
     */
    private String instanceName;
    /**
     * 操作内容说明
     */
    private String content;
    /**
     * 操作结果资源的uri，是一个json
     */
    private String resultResourceUri;
    private String creator;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResultResourceUri() {
        return resultResourceUri;
    }

    public void setResultResourceUri(String resultResourceUri) {
        this.resultResourceUri = resultResourceUri;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
