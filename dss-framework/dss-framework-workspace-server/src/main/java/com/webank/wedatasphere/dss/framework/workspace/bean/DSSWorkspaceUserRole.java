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

package com.webank.wedatasphere.dss.framework.workspace.bean;

import java.util.Date;

/**
* @Author: bradyli
* @Date: 2021/11/4
* @Description:
* @Param:
* @return:
**/
public class DSSWorkspaceUserRole {
    private Long id;
    private Long workspaceId;
    private String userName;
    private Integer roleId;
    private Date createTime;
    private String createBy;
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public DSSWorkspaceUserRole() {
    }

    public DSSWorkspaceUserRole(Long workspaceId, String userName, Integer roleId, Date createTime, String createBy) {
        this.workspaceId = workspaceId;
        this.userName = userName;
        this.roleId = roleId;
        this.createTime = createTime;
        this.createBy = createBy;
    }

    @Override
    public String toString() {
        return "WebankDSSWorkspaceUserRole{" +
                "id=" + id +
                ", workspaceId=" + workspaceId +
                ", userName='" + userName + '\'' +
                ", roleId=" + roleId +
                ", createTime=" + createTime +
                ", createBy='" + createBy + '\'' +
                ", userId=" + userId +
                '}';
    }
}
