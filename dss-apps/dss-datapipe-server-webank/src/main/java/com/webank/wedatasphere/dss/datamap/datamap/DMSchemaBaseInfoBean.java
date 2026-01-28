package com.webank.wedatasphere.dss.datamap.datamap;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSchemaBaseInfoBean {
    private String dbCode;
    private String usedSpace;
    private String tableNum;
    private String spaceQuota;

    private String description;

    public String getDbCode() {
        return dbCode;
    }

    public void setDbCode(String dbCode) {
        this.dbCode = dbCode;
    }

    public String getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(String usedSpace) {
        this.usedSpace = usedSpace;
    }

    public String getTableNum() {
        return tableNum;
    }

    public void setTableNum(String tableNum) {
        this.tableNum = tableNum;
    }

    public String getSpaceQuota() {
        return spaceQuota;
    }

    public void setSpaceQuota(String spaceQuota) {
        this.spaceQuota = spaceQuota;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
