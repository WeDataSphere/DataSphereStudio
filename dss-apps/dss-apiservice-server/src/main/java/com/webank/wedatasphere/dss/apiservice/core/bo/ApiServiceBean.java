package com.webank.wedatasphere.dss.apiservice.core.bo;

import java.io.Serializable;
import java.util.Date;

public class ApiServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long id;
    private String submitUser;
    private String proxyUser;
    private String taskID;
    private String execID;
    private String user;

    private Date createTime;

    public ApiServiceBean() {
    }

    public ApiServiceBean(String submitUser, String proxyUser, String taskID, String execID, String user) {
        this.submitUser = submitUser;
        this.proxyUser = proxyUser;
        this.taskID = taskID;
        this.execID = execID;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getExecID() {
        return execID;
    }

    public void setExecID(String execID) {
        this.execID = execID;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
