package com.webank.wedatasphere.dss.framework.appconn.entity;

import org.apache.ibatis.type.Alias;

/**
 * UI属性关联的校验规则
 */
@Alias("appconnFrameworkUiToValidate")
public class UiToValidate {
    /**
     * 
     */
    private Integer id;
    /**
     * UI属性id
     */
    private Integer uiId;
    /**
     * 校验规则id
     */
    private Integer validateId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUiId() {
        return uiId;
    }

    public void setUiId(Integer uiId) {
        this.uiId = uiId;
    }

    public Integer getValidateId() {
        return validateId;
    }

    public void setValidateId(Integer validateId) {
        this.validateId = validateId;
    }
}