/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import java.util.List;

/**
 * 单个外发目标：一个库表 + 字段集合 + 分区。
 * <p>
 * 对应 dataTargets DSL 的一行，例如：
 * <pre>
 * dt.01=db=bdap_desensitized|table=customer_export_view|fields=cust_id,cust_name,amount|partition={dt=20260716}
 * </pre>
 * partition 为 null/空 表示非分区表。
 */
public class DataTarget {
    private final String dbName;
    private final String tableName;
    private final List<String> fields;
    private final String partition;

    public DataTarget(String dbName, String tableName, List<String> fields, String partition) {
        this.dbName = dbName;
        this.tableName = tableName;
        this.fields = fields;
        this.partition = partition;
    }

    public String getDbName() { return dbName; }
    public String getTableName() { return tableName; }
    public List<String> getFields() { return fields; }
    public String getPartition() { return partition; }

    /** 分区是否为空（非分区表） */
    public boolean hasPartition() {
        return partition != null && !partition.trim().isEmpty();
    }

    /** 同一 (db, table) 唯一性判定与去重依据 */
    public String tableKey() {
        return (dbName == null ? "" : dbName.trim()) + "." + (tableName == null ? "" : tableName.trim());
    }
}
