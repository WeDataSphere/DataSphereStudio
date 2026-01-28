package com.webank.wedatasphere.dss.framework.project.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 工作流应用模板
 * @author v_kangkangyuan
 * @TableName dss_ec_config_template_workflow
 */
public class ECTemplateWorkflowDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 工作空间id
     */
    private String workspaceId;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /***
     * 编排ID
     */
    private Long orchestratorId;

    /**
     * 编排名称
     */
    private String orchestratorName;

    /**
     * 工作流id
     */
    private Long flowId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public ECTemplateWorkflowDO(String templateId, Long projectId,  Long orchestratorId,  Long flowId) {
        this.templateId = templateId;
        this.projectId = projectId;
        this.orchestratorId = orchestratorId;
        this.flowId = flowId;
    }

    public ECTemplateWorkflowDO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
