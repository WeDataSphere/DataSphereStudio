/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.exception;

/**
 * DataGo飞书外发统一异常。
 * <p>
 * errorCode：DSS 侧异常码（82001~82009，见设计文档 4.3）。
 * httpCode：DataGo 接口返回的 HTTP 状态码（200/400/403/404/409/413/500/502/504），
 * 用于外发阶段区分可重试（502/504）与不可重试（413/409）。
 */
public class DataGoFeishuException extends RuntimeException {
    private final int errorCode;
    private final int httpCode;

    public DataGoFeishuException(int errorCode, String message) {
        this(errorCode, message, 0, null);
    }

    public DataGoFeishuException(int errorCode, String message, int httpCode) {
        this(errorCode, message, httpCode, null);
    }

    public DataGoFeishuException(int errorCode, String message, Throwable cause) {
        this(errorCode, message, 0, cause);
    }

    public DataGoFeishuException(int errorCode, String message, int httpCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
