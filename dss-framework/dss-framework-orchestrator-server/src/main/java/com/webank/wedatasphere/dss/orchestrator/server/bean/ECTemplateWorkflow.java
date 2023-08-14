package com.webank.wedatasphere.dss.orchestrator.server.bean;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDO;
import org.springframework.beans.BeanUtils;

public class ECTemplateWorkflow {

    /**
     * 主键
     */
    private Long id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 工作流id
     */
    private Long orchestratorId;

    /**
     * 工作流名称
     */
    private String orchestratorName;

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

    public static ECTemplateWorkflow convertToBean(ECTemplateWorkflowDO templateWorkflow){
        ECTemplateWorkflow ecTemplateWorkflow = new ECTemplateWorkflow();
        BeanUtils.copyProperties(templateWorkflow,ecTemplateWorkflow);
        return ecTemplateWorkflow;
    }
}
