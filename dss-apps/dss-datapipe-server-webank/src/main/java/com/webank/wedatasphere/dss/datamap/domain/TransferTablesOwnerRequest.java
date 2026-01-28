package com.webank.wedatasphere.dss.datamap.domain;

import java.io.Serializable;
import java.util.List;

public class TransferTablesOwnerRequest implements Serializable {

    //审批单标题
    private String approvalTitle;
    //数据库名
    private String dbName;
    //表名
    private List<String> tablesName;
    //原owner
    private String oldOwner;
    //新owner
    private String newOwner;
    //数据治理管理员
    private String dataGovernanceAdmin;
    //描述
    private String description;

    public String getApprovalTitle() {
        return approvalTitle;
    }

    public void setApprovalTitle(String approvalTitle) {
        this.approvalTitle = approvalTitle;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<String> getTablesName() {
        return tablesName;
    }

    public void setTablesName(List<String> tablesName) {
        this.tablesName = tablesName;
    }

    public String getOldOwner() {
        return oldOwner;
    }

    public void setOldOwner(String oldOwner) {
        this.oldOwner = oldOwner;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    public String getDataGovernanceAdmin() {
        return dataGovernanceAdmin;
    }

    public void setDataGovernanceAdmin(String dataGovernanceAdmin) {
        this.dataGovernanceAdmin = dataGovernanceAdmin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TransferTablesOwnerRequest{" +
                "approvalTitle='" + approvalTitle + '\'' +
                ", dbName='" + dbName + '\'' +
                ", tablesName=" + tablesName +
                ", oldOwner='" + oldOwner + '\'' +
                ", newOwner='" + newOwner + '\'' +
                ", dataGovernanceAdmin='" + dataGovernanceAdmin + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
