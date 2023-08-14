package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request;


import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/6/28
 */
public class UpdateKeyMappingRequest {

    private String templateName;
    private String templateUid;
    private String engineType;
    private String operator;

    private List<TemplateConfigKey> itemList;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateUid() {
        return templateUid;
    }

    public void setTemplateUid(String templateUid) {
        this.templateUid = templateUid;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<TemplateConfigKey> getItemList() {
        return itemList;
    }

    public void setItemList(List<TemplateConfigKey> itemList) {
        this.itemList = itemList;
    }

    public static  class TemplateConfigKey {

        private String key;

        /** 配置值 表字段 : config_value 字段类型 : varchar(200) */
        private String configValue;

        /** 上限值 表字段 : max_value 字段类型 : varchar(50) */
        private String maxValue;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }

        public String getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(String maxValue) {
            this.maxValue = maxValue;
        }
    }
}
