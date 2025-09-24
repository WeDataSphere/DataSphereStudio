package com.webank.wedatasphere.dss.flow.execution.entrance.entity;

public class SendImsTaskInfo {

    private String jobId;
    private String alterTitle;
    private String alterInfo;


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getAlterTitle() {
        return alterTitle;
    }

    public void setAlterTitle(String alterTitle) {
        this.alterTitle = alterTitle;
    }

    public String getAlterInfo() {
        return alterInfo;
    }

    public void setAlterInfo(String alterInfo) {
        this.alterInfo = alterInfo;
    }
}
