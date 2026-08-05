/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

/**
 * ③ 外发响应中单个 sheet（表）的写入结果。
 */
public class SheetResult {
    private String tableName;
    private Long rows;
    private String status;

    public String getTableName() { return tableName; }
    public Long getRows() { return rows; }
    public String getStatus() { return status; }
}
