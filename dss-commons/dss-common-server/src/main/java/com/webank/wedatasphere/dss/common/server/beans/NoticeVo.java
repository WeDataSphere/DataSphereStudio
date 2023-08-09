package com.webank.wedatasphere.dss.common.server.beans;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class NoticeVo {
    private Integer id;

    private String content;

    private Date startTime;

    private Date endTime;

    //0-未生效;1-生效中;2-已过期
    private Integer status;

    private Date createTime;

    private String createUser;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date statTime) {
        this.startTime = statTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
