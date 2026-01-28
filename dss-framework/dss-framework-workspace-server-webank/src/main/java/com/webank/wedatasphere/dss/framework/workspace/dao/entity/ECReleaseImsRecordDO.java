package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * EC释放通知发送记录表
 * @author v_kangkangyuan
 */
public class ECReleaseImsRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /**
     * 发送记录id
     */
    private String recordId;

    /**
     * 释放规则id
     */
    private String strategyId;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 通知状态：0未发送 1已发送  2发送失败
     */
    private Integer status;

    /**
     * 负责发送的服务实例
     */
    private String executeInstance;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getExecuteInstance() {
        return executeInstance;
    }

    public void setExecuteInstance(String executeInstance) {
        this.executeInstance = executeInstance;
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

    @Override
    public String toString() {
        return "ECReleaseImsRecordDO{" +
                "recordId='" + recordId + '\'' +
                ", strategyId='" + strategyId + '\'' +
                ", workspaceId=" + workspaceId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", executeInstance='" + executeInstance + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
