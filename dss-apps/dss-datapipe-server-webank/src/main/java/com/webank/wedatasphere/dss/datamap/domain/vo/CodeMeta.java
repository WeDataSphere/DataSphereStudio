package com.webank.wedatasphere.dss.datamap.domain.vo;

import java.util.Objects;

/**
 * Author: xlinliu
 * Date: 2024/10/21
 */
public class CodeMeta {
    private  String db;
    private String table;
    private String partition;

    public CodeMeta(String db, String table, String partition) {
        this.db = db;
        this.table = table;
        this.partition = partition;
    }

    public CodeMeta() {
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeMeta)) return false;
        CodeMeta codeMeta = (CodeMeta) o;
        return Objects.equals(db, codeMeta.db) && Objects.equals(table, codeMeta.table) && Objects.equals(partition, codeMeta.partition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(db, table, partition);
    }
}
