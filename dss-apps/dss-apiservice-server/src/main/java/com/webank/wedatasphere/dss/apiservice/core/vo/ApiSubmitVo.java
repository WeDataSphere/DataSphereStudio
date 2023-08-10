/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ApiSubmitVo implements Serializable {
    Long id;
    //选择批量提交的apiId和versionid
    List<ApiVersionIdVo> submitApiInfos;
    //审批单名称
    String approvalName;
    //授权用户，逗号分割
    String applyUser;
    //提交者
    String creator;
    //状态
    Integer status;
    Date createTime;
    Date updateTime;
    //单号，生成的uuid
    String approvalNo;
    //授权期限，单位天，前端需要输入*代表永久
    String duration;
    //背景描述
    String backgroundDesc;
    //重要程度。1：高，2：中，3：低
    Long importance;
    //关注人，逗号分割
    String attentionUser;

    Long workspaceId;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApprovalName() {
        return approvalName;
    }

    public void setApprovalName(String approvalName) {
        this.approvalName = approvalName;
    }

    public String getApplyUser() {
        return applyUser;
    }

    public void setApplyUser(String applyUser) {
        this.applyUser = applyUser;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getApprovalNo() {
        return approvalNo;
    }

    public void setApprovalNo(String approvalNo) {
        this.approvalNo = approvalNo;
    }

    public List<ApiVersionIdVo> getSubmitApiInfos() {
        return submitApiInfos;
    }

    public void setSubmitApiInfos(List<ApiVersionIdVo> submitApiInfos) {
        this.submitApiInfos = submitApiInfos;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAttentionUser() {
        return attentionUser;
    }

    public void setAttentionUser(String attentionUser) {
        this.attentionUser = attentionUser;
    }

    public String getBackgroundDesc() {
        return backgroundDesc;
    }

    public void setBackgroundDesc(String backgroundDesc) {
        this.backgroundDesc = backgroundDesc;
    }

    public Long getImportance() {
        return importance;
    }

    public void setImportance(Long importance) {
        this.importance = importance;
    }

    @Override
    public String toString() {
        return "ApiSubmitVo{" +
                "id=" + id +
                ", submitApiInfos=" + submitApiInfos +
                ", approvalName='" + approvalName + '\'' +
                ", applyUser='" + applyUser + '\'' +
                ", creator='" + creator + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", approvalNo='" + approvalNo + '\'' +
                ", duration=" + duration +
                ", backgroundDesc='" + backgroundDesc + '\'' +
                ", importance=" + importance +
                ", attentionUser='" + attentionUser + '\'' +
                '}';
    }

}