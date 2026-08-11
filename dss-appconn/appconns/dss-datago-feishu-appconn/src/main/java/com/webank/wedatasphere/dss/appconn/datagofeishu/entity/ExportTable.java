/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import java.util.List;

/**
 * ① 表单中的库表项（对应接口 {@code data.tables[]}）。
 */
public class ExportTable {
    private String dbName;
    private String tableName;
    private String partition;
    private List<String> columns;

    public String getDbName() { return dbName; }
    public String getTableName() { return tableName; }
    public String getPartition() { return partition; }
    public List<String> getColumns() { return columns; }
}
