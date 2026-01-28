package com.webank.wedatasphere.dss.datamap.domain.vo;

/**
 * @author: jinyangrao on 2020/11/05
 */
public class SchemaBaseInfoVo {
    private String dbName;
    private String dbSize;
    private String dbCapacity;
    private Integer tableQuantity;
    private String description;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbSize() {
        return dbSize;
    }

    public void setDbSize(String dbSize) {
        this.dbSize = dbSize;
    }

    public String getDbCapacity() {
        return dbCapacity;
    }

    public void setDbCapacity(String dbCapacity) {
        this.dbCapacity = dbCapacity;
    }

    public Integer getTableQuantity() {
        return tableQuantity;
    }

    public void setTableQuantity(Integer tableQuantity) {
        this.tableQuantity = tableQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}