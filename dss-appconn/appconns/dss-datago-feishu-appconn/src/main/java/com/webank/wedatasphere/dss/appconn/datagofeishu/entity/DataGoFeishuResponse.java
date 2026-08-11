/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import com.google.gson.JsonObject;

/**
 * DataGo 接口统一响应持有者，对齐接口文档 {@code {success, code, message, data}}。
 * <p>
 * httpCode 为 HTTP 状态码（用于外发阶段重试判定：502/504 可重试，413/409 不可重试）；
 * success/code/message/data 为业务响应体字段；data 可能为 null（如 dm 单不存在）。
 */
public class DataGoFeishuResponse {
    private int httpCode;
    private boolean success;
    private Integer code;
    private String message;
    private JsonObject data;

    public int getHttpCode() { return httpCode; }
    public void setHttpCode(int httpCode) { this.httpCode = httpCode; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public JsonObject getData() { return data; }
    public void setData(JsonObject data) { this.data = data; }

    /** HTTP 层 2xx 视为通信成功 */
    public boolean isHttpOk() {
        return httpCode >= 200 && httpCode < 300;
    }

    /** 通信成功 + 业务 success + data 非空 */
    public boolean isBusinessOk() {
        return isHttpOk() && success && data != null;
    }
}
