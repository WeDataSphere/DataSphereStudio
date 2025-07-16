/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.workflow.entity;

import java.util.Date;

/**
 * 工作流编辑锁对象
 */
public class DSSFlowEditLock {

    private Integer id;

    private Long flowID;

    private String flowVersion;

    private Date createTime;

    private Date updateTime;

    private String owner;

    private Integer lockStamp;

    private Boolean isExpire;

    private String lockContent;//uuid

    private String username;

    public DSSFlowEditLock() {
    }

    public DSSFlowEditLock(String flowVersion, String lockContent) {
        this.flowVersion = flowVersion;
        this.lockContent = lockContent;
    }

    public String getLockContent() {
        return lockContent;
    }

    public void setLockContent(String lockContent) {
        this.lockContent = lockContent;
    }

    public Boolean getExpire() {
        return isExpire;
    }

    public void setExpire(Boolean expire) {
        isExpire = expire;
    }

    public String getFlowVersion() {
        return flowVersion;
    }

    public void setFlowVersion(String flowVersion) {
        this.flowVersion = flowVersion;
    }

    public Integer getLockStamp() {
        return lockStamp;
    }

    public void setLockStamp(Integer lockStamp) {
        this.lockStamp = lockStamp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getFlowID() {
        return flowID;
    }

    public void setFlowID(Long flowID) {
        this.flowID = flowID;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "DSSFlowEditLock{" +
                "id=" + id +
                ", flowID=" + flowID +
                ", flowVersion='" + flowVersion + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", owner='" + owner + '\'' +
                ", lockStamp=" + lockStamp +
                ", isExpire=" + isExpire +
                ", lockContent='" + lockContent + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
