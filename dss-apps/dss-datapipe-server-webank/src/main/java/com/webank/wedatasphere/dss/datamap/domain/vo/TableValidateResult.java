package com.webank.wedatasphere.dss.datamap.domain.vo;

import java.util.Objects;

/**
 * Author: xlinliu
 * Date: 2024/10/21
 */
public class TableValidateResult {
    private  String db;
    private String table;
    private String partition;
    private Boolean view;

    public TableValidateResult(String db, String table, String partition,Boolean view) {
        this.db = db;
        this.table = table;
        this.partition = partition;
        this.view = view;
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

    public Boolean getView() {
        return view;
    }

    public void setView(Boolean view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableValidateResult)) return false;
        TableValidateResult codeMeta = (TableValidateResult) o;
        return Objects.equals(db, codeMeta.db) && Objects.equals(table, codeMeta.table) && Objects.equals(partition, codeMeta.partition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(db, table, partition);
    }
}
