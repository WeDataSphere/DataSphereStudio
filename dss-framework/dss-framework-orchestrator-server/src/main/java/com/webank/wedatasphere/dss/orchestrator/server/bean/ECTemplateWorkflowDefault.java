package com.webank.wedatasphere.dss.orchestrator.server.bean;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import org.springframework.beans.BeanUtils;


public class ECTemplateWorkflowDefault {


    private Long id;

    private String templateId;

    private String templateName;

    private Long projectId;

    private Long orchestratorId;

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

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public static ECTemplateWorkflowDefault convertToBean(ECTemplateWorkflowDefaultDO ecTemplateWorkflowDefaultDO){
        ECTemplateWorkflowDefault eCTemplateWorkflowDefault = new ECTemplateWorkflowDefault();
        BeanUtils.copyProperties(ecTemplateWorkflowDefaultDO,eCTemplateWorkflowDefault);
        return eCTemplateWorkflowDefault;
    }
}
