package com.webank.wedatasphere.dss.datamap.domain;

import java.util.Date;

/**
 * Author: xlinliu
 * Date: 2024/11/4
 */
public class DatasetReadHistory {
    private String username;
    private String readType;
    private Date readTime;

    public DatasetReadHistory() {
    }

    public DatasetReadHistory(String username, String readType, Date readTime) {
        this.username = username;
        this.readType = readType;
        this.readTime = readTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReadType() {
        return readType;
    }

    public void setReadType(String readType) {
        this.readType = readType;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }
}
