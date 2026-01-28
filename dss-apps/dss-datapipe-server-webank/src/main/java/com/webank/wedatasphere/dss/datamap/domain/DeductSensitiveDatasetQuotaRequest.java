package com.webank.wedatasphere.dss.datamap.domain;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2024/10/30
 */
public class DeductSensitiveDatasetQuotaRequest {

    private List<String> paths;

    private String taskId;
    private String readType;
    /**
     * 本次要扣除的总流量
     */
    private Long amount;

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getAmount() {
        return amount;
    }



    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public String getReadType() {
        return readType;
    }

    public void setReadType(String readType) {
        this.readType = readType;
    }
}
