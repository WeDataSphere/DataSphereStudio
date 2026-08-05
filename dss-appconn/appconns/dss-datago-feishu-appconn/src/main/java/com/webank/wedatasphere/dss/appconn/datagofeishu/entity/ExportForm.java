/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import java.util.List;

/**
 * ① 外发表单获取响应（{@code POST /api/export/form} 的 {@code data}）。
 */
public class ExportForm {
    private String dmId;
    private String dmTitle;
    private String dmUser;
    private String optype;
    private List<ExportTable> tables;
    private String status;
    /** 审批允许的通知人集合（可选，若①返回则用于客户端侧 notifyUsers 子集校验） */
    private List<String> notifyUsers;

    public String getDmId() { return dmId; }
    public String getDmTitle() { return dmTitle; }
    public String getDmUser() { return dmUser; }
    public String getOptype() { return optype; }
    public List<ExportTable> getTables() { return tables; }
    public String getStatus() { return status; }
    public List<String> getNotifyUsers() { return notifyUsers; }
}
