package com.webank.wedatasphere.dss.framework.appconn.entity;

/**
 * 校验规则
 */
public class Validate {

    /**
     * id
     */
    private Integer id;

    /**
     * 校验类型，有：None, NumInterval, FloatInterval, Include, Regex, OPF'
     */
    private String validateType;

    /**
     * 校验范围，比如：[1,15]
     */
    private String validateRange;

    /**
     * 校验失败时的中文错误提示信息
     */
    private String errorMsg;

    /**
     * 校验失败时的英文错误提示信息
     */
    private String errorMsgEn;

    /**
     * 校验的触发规则，有：blur、change。其中 blur 指失去鼠标焦点，change 指内容发生变化时
     */
    private String trigger;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getValidateType() {
        return validateType;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }
    public String getValidateRange() {
        return validateRange;
    }

    public void setValidateRange(String validateRange) {
        this.validateRange = validateRange;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getErrorMsgEn() {
        return errorMsgEn;
    }

    public void setErrorMsgEn(String errorMsgEn) {
        this.errorMsgEn = errorMsgEn;
    }
    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
}