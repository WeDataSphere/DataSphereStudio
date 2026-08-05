/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

/**
 * ② 外发任务生成/状态查询响应（{@code POST /api/export/task} 的 {@code data}）。
 * <p>
 * taskId 为数值类型（DataGo 外发任务主键）；status 取值：
 * inited / detecting / detected_pass / detected_fail / detect_error / exported。
 */
public class TaskResponse {
    private Long taskId;
    private String optype;
    private String status;
    private String resultSummary;

    public Long getTaskId() { return taskId; }
    public String getOptype() { return optype; }
    public String getStatus() { return status; }
    public String getResultSummary() { return resultSummary; }
}
