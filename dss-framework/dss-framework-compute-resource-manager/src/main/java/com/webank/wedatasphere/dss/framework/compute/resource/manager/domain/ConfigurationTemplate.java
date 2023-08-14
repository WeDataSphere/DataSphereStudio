package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain;

import java.util.List;

/**
 * 计算中间件的配置模板实体类
 * Author: xlinliu
 * Date: 2023/6/16
 */
public class ConfigurationTemplate {
    private String templateName;
    private String templateUid;
    private String engineType;
    private String operator;

    private List<Item> itemList;

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

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public static class Item{
        private String key;
        /**
         * 是否必填
         */
        private Boolean require;
        private String configValue;
        private String maxValue;
        /**
         * 系统默认值
         */
        private String defaultValue;
        /**
         * 参数描述
         */
        private String description;
        /**
         * 引擎类型
         */
        private String engineType;

        /**
         * 参数显示名
         */
        private String name;
        /**
         * 校验表达式，上下界，或者正则表达式
         */
        private String validateRange;
        /**
         * 校验表达式类型，分为NumInterval和Regex
         */
        private String validateType;
        /**
         * 0 无 none ;
         * 1 只有下限值 min ;
         * 2 只有上限值 max ;
         * 3 上下限值都有 both
         */
        private int boundaryType;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Boolean getRequire() {
            return require;
        }

        public void setRequire(Boolean require) {
            this.require = require;
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

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getEngineType() {
            return engineType;
        }

        public void setEngineType(String engineType) {
            this.engineType = engineType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValidateRange() {
            return validateRange;
        }

        public void setValidateRange(String validateRange) {
            this.validateRange = validateRange;
        }

        public String getValidateType() {
            return validateType;
        }

        public void setValidateType(String validateType) {
            this.validateType = validateType;
        }

        public int getBoundaryType() {
            return boundaryType;
        }

        public void setBoundaryType(int boundaryType) {
            this.boundaryType = boundaryType;
        }
    }
}
