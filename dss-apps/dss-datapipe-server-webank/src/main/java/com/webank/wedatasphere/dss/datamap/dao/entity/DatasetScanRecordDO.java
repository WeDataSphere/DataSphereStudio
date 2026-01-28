package com.webank.wedatasphere.dss.datamap.dao.entity;

import java.util.Date;

/**
 * Author: xlinliu
 * Date: 2024/10/31
 */
public class DatasetScanRecordDO {
    private Long id;
    private String path;
    private String taskId;
    private Long rowSize;
    private Integer hasSensitiveInfo;
    private String scanInfo;
    private String readHistory;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getRowSize() {
        return rowSize;
    }

    public void setRowSize(Long rowSize) {
        this.rowSize = rowSize;
    }

    public Integer getHasSensitiveInfo() {
        return hasSensitiveInfo;
    }

    public void setHasSensitiveInfo(Integer hasSensitiveInfo) {
        this.hasSensitiveInfo = hasSensitiveInfo;
    }

    public String getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(String scanInfo) {
        this.scanInfo = scanInfo;
    }

    public String getReadHistory() {
        return readHistory;
    }

    public void setReadHistory(String readHistory) {
        this.readHistory = readHistory;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}