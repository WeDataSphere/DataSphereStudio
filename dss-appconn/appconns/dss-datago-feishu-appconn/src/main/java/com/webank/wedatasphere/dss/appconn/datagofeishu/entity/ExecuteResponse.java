/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import java.util.List;

/**
 * ③ 执行外发响应（{@code POST /api/export/execute} 的 {@code data}，optype=table）。
 * <p>
 * 对齐接口文档 v2.0：③ 为**异步接口**——首次调用触发后台外发并立即返回
 * {@code status=exporting}；外发完成后任务置 {@code exported} 并写入多维表格 URL，
 * 再次调用③（或用② taskId 查询）即返回 {@code status=exported} 与 {@code bitableUrl}。
 * {@code bitableName}/{@code bitableUrl} 仅在 {@code status=exported} 时有值。
 * <p>
 * v2.0③响应**不再返回 {@code sheets[]}**（各表写入明细写入审计 export_result，不随异步响应返回），
 * 此处保留 {@code sheets} 字段以兼容历史/未来，反序列化时为 null。
 */
public class ExecuteResponse {
    private String dmId;
    private String optype;
    /** 外发状态：exporting（进行中）/ exported（完成）/ export_failed|failed（外发失败终态） */
    private String status;
    /** 本次外发涉及的任务 id */
    private List<Long> taskIds;
    private String bitableName;
    private String bitableUrl;
    /** v2.0③响应不再返回；保留字段兼容，反序列化为 null */
    private List<SheetResult> sheets;
    private List<String> notifyUsers;
    private String exportedAt;
    /**
     * 接口 {@code data} 内的 message 字段（失败明细）。
     * <p>
     * {@code status=export_failed}/{@code failed} 时，DataGo 在 {@code data.message} 返回具体失败原因
     * （如"全部表外发失败(1): ... createSheet failed: ..."），由 Gson 反序列化 {@code data} 时自动填充。
     * 注意区分 envelope 顶层 message（响应体 {@code {success,code,message,data}} 的 message，常为通用
     * "外发进行中"文案），失败定位以本字段（{@code data.message}）为准。
     */
    private String message;

    public String getDmId() { return dmId; }
    public String getOptype() { return optype; }
    public String getStatus() { return status; }
    public List<Long> getTaskIds() { return taskIds; }
    public String getBitableName() { return bitableName; }
    public String getBitableUrl() { return bitableUrl; }
    public List<SheetResult> getSheets() { return sheets; }
    public List<String> getNotifyUsers() { return notifyUsers; }
    public String getExportedAt() { return exportedAt; }
    public String getMessage() { return message; }
}
