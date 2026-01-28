package com.webank.wedatasphere.dss.datamap.datamap;

import java.util.Map;

public class DMSColumnMaskInfo {
    private String originColumnName;
    private String targetColumnName;
    private Map<String, String> originColumnComment;
    private Map<String, String> targetColumnComment;
    /**
     * 脱敏函数
     */
    private String funcName;

    /**
     * 脱敏方式
     */
    private String funcDescription;
    /**
     * 字段口径加工规则
     */
    private Map<String, String> fieldRule;
    /**
     * 数据项编码
     */
    private Map<String, String> dataItemCode;

    /**
     * 数据项名称
     */
    private Map<String, String> dataItemName ;
    /**
     * 数据分类编码
     */
    private Map<String, String> classficationCode;

    /**
     * 数据分类名称
     */
    private Map<String, String> classficationName;

    public String getOriginColumnName() {
        return originColumnName;
    }

    public void setOriginColumnName(String originColumnName) {
        this.originColumnName = originColumnName;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(String targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getFuncDescription() {
        return funcDescription;
    }

    public void setFuncDescription(String funcDescription) {
        this.funcDescription = funcDescription;
    }

    public Map<String, String> getOriginColumnComment() {
        return originColumnComment;
    }

    public void setOriginColumnComment(Map<String, String> originColumnComment) {
        this.originColumnComment = originColumnComment;
    }

    public Map<String, String> getTargetColumnComment() {
        return targetColumnComment;
    }

    public void setTargetColumnComment(Map<String, String> targetColumnComment) {
        this.targetColumnComment = targetColumnComment;
    }

    public Map<String, String> getFieldRule() {
        return fieldRule;
    }

    public void setFieldRule(Map<String, String> fieldRule) {
        this.fieldRule = fieldRule;
    }

    public Map<String, String> getDataItemCode() {
        return dataItemCode;
    }

    public void setDataItemCode(Map<String, String> dataItemCode) {
        this.dataItemCode = dataItemCode;
    }

    public Map<String, String> getDataItemName() {
        return dataItemName;
    }

    public void setDataItemName(Map<String, String> dataItemName) {
        this.dataItemName = dataItemName;
    }

    public Map<String, String> getClassficationCode() {
        return classficationCode;
    }

    public void setClassficationCode(Map<String, String> classficationCode) {
        this.classficationCode = classficationCode;
    }

    public Map<String, String> getClassficationName() {
        return classficationName;
    }

    public void setClassficationName(Map<String, String> classficationName) {
        this.classficationName = classficationName;
    }
}