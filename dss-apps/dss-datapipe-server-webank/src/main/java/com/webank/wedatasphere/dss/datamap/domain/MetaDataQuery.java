package com.webank.wedatasphere.dss.datamap.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author: jinyangrao on 2020/11/7
 */
public class MetaDataQuery {
    private String loginUser;
    private String dbName;
    private Integer isTableOwner;
    private String tableName;
    private String orderBy;
    private Integer pageSize;
    private Integer currentPage;
    /**
     * 是否精确匹配表名
     */
    private Boolean exactTableName;
    /**
     * 工作空间ID
     * */
    private Long workspaceId;

    /**
     * 属主
     * **/
    private String tableOwner;

    /**
     * 表使用热度
     * **/
    private String usageHeat;

    public MetaDataQuery(String dbName, Integer isTableOwner, String tableName, String orderBy) {
        this.dbName = dbName;
        this.isTableOwner = isTableOwner;
        this.tableName = tableName;
        this.orderBy = orderBy;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Integer getIsTableOwner() {
        return isTableOwner;
    }

    public void setIsTableOwner(Integer isTableOwner) {
        this.isTableOwner = isTableOwner;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Boolean getExactTableName() {
        return exactTableName;
    }

    public void setExactTableName(Boolean exactTableName) {
        this.exactTableName = exactTableName;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(String tableOwner) {
        this.tableOwner = tableOwner;
    }

    public String getUsageHeat() {
        return usageHeat;
    }

    public void setUsageHeat(String usageHeat) {
        this.usageHeat = usageHeat;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("loginUser", loginUser)
                .append("dbName", dbName)
                .append("isTableOwner", isTableOwner)
                .append("tableName", tableName)
                .append("orderBy", orderBy)
                .append("pageSize", pageSize)
                .append("currentPage", currentPage)
                .append("exactTableName", exactTableName)
                .toString();
    }

}
