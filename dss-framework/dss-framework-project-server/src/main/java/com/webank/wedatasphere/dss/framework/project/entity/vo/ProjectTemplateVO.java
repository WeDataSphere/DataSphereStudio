package com.webank.wedatasphere.dss.framework.project.entity.vo;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/7/27
 */
public class ProjectTemplateVO {
    private String enginType;
    private List<Item> child ;

    public ProjectTemplateVO(String enginType, List<Item> child) {
        this.enginType = enginType;
        this.child = child;
    }

    public String getEnginType() {
        return enginType;
    }

    public void setEnginType(String enginType) {
        this.enginType = enginType;
    }

    public List<Item> getChild() {
        return child;
    }

    public void setChild(List<Item> child) {
        this.child = child;
    }

    public static  class Item{
        public Item(String templateId, String templateName, boolean workflowDefault) {
            this.templateId = templateId;
            this.templateName = templateName;
            this.workflowDefault = workflowDefault;
        }

        public Item() {
        }

        private String templateId;
        private String templateName;
        private boolean workflowDefault;

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public boolean isWorkflowDefault() {
            return workflowDefault;
        }

        public void setWorkflowDefault(boolean workflowDefault) {
            this.workflowDefault = workflowDefault;
        }
    }


}
