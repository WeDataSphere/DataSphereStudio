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

package com.webank.wedatasphere.dss.framework.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 复制工程JOB
 *
 * @author v_wbzwchen
 * @since 2022-01-16
 */
@TableName(value = "dss_project_copy_task")
public class DSSProjectCopyTask implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //空间ID
    private Long workspaceId;
    //复制源工程ID
    private Long sourceProjectId;
    //复制源工程名称
    private String sourceProjectName;
    //复制工程ID
    private Long copyProjectId;
    //复制工程名称
    private String copyProjectName;
    //剩余复制数量
    private Integer surplusCount;
    //总数
    private Integer sumCount;
    //状态 0:初始化，1：复制中，2：复制成功，3：复制失败
    private Integer status;
    private String instanceName;
    //上个复制时间
    private Date updateTime;
    //创建时间
    private Date createTime;
    //创建人
    private String createBy;
    //失败原因
    private String errorMsg;

    private String errorOrc;

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

    public Long getSourceProjectId() {
        return sourceProjectId;
    }

    public void setSourceProjectId(Long sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    public String getSourceProjectName() {
        return sourceProjectName;
    }

    public void setSourceProjectName(String sourceProjectName) {
        this.sourceProjectName = sourceProjectName;
    }

    public Long getCopyProjectId() {
        return copyProjectId;
    }

    public void setCopyProjectId(Long copyProjectId) {
        this.copyProjectId = copyProjectId;
    }

    public String getCopyProjectName() {
        return copyProjectName;
    }

    public void setCopyProjectName(String copyProjectName) {
        this.copyProjectName = copyProjectName;
    }

    public Integer getSurplusCount() {
        return surplusCount;
    }

    public void setSurplusCount(Integer surplusCount) {
        this.surplusCount = surplusCount;
    }

    public Integer getSumCount() {
        return sumCount;
    }

    public void setSumCount(Integer sumCount) {
        this.sumCount = sumCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorOrc() {
        return errorOrc;
    }

    public void setErrorOrc(String errorOrc) {
        this.errorOrc = errorOrc;
    }

    @Override
    public String toString() {
        return "DSSProjectCopyTask{" +
                "id=" + id +
                ", workspaceId=" + workspaceId +
                ", sourceProjectId=" + sourceProjectId +
                ", sourceProjectName='" + sourceProjectName + '\'' +
                ", copyProjectId=" + copyProjectId +
                ", copyProjectName='" + copyProjectName + '\'' +
                ", surplusCount=" + surplusCount +
                ", sumCount=" + sumCount +
                ", status=" + status +
                ", instanceName='" + instanceName + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", createBy='" + createBy + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", errorOrc='" + errorOrc + '\'' +
                '}';
    }
}
