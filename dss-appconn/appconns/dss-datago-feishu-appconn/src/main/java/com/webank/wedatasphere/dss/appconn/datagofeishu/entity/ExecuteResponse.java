/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import java.util.List;

/**
 * ③ 执行外发响应（{@code POST /api/export/execute} 的 {@code data}，optype=table）。
 * <p>
 * DataGo 一次性完成：创建飞书多维表格 + 写 sheet + 向 notifyUsers 发送飞书消息。
 */
public class ExecuteResponse {
    private String dmId;
    private String optype;
    private String bitableName;
    private String bitableUrl;
    private List<SheetResult> sheets;
    private List<String> notifyUsers;
    private String exportedAt;

    public String getDmId() { return dmId; }
    public String getOptype() { return optype; }
    public String getBitableName() { return bitableName; }
    public String getBitableUrl() { return bitableUrl; }
    public List<SheetResult> getSheets() { return sheets; }
    public List<String> getNotifyUsers() { return notifyUsers; }
    public String getExportedAt() { return exportedAt; }
}
