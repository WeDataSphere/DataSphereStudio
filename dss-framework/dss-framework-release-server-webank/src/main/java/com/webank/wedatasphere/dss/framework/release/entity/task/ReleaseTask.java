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

package com.webank.wedatasphere.dss.framework.release.entity.task;

import com.webank.wedatasphere.dss.framework.release.job.ReleaseStatus;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * created by cooperyang on 2020/12/10
 * Description: 本方法是为了记录发布的历史内容
 */
public class ReleaseTask {

    private Long id;

    private Long projectId;

    private Long orchestratorVersionId;

    private Long orchestratorId;

    private String releaseUser;

    private Date createTime;

    private Date updateTime;

    private String status;

    private String instanceName;
    //错误信息
    private String errorMsg;

    private String orchestratorName;

    //发布描述信息
    private String comment;

    //日志信息，暂时没有使用到
    private String logMsg;
    //备用字段
    private String bak;


    public static ReleaseTask newTask(String releaseUser, Long projectId, Long orchestratorId, Long orchestratorVersionId, String orchestratorName,String comment, String instanceName) {
        ReleaseTask releaseTask = new ReleaseTask();
        releaseTask.setReleaseUser(releaseUser);
        releaseTask.setOrchestratorName(orchestratorName);
        releaseTask.setOrchestratorVersionId(orchestratorVersionId);
        releaseTask.setProjectId(projectId);
        releaseTask.setOrchestratorId(orchestratorId);
        Date currentTime = new Date(System.currentTimeMillis());
        releaseTask.setCreateTime(currentTime);
        releaseTask.setUpdateTime(currentTime);
        releaseTask.setStatus(ReleaseStatus.INIT.getStatus());
        releaseTask.setInstanceName(instanceName);

        if(StringUtils.isNotBlank(comment) && comment.length()>500){
            comment = comment.substring(0,490);
        }
        releaseTask.setComment(comment);
        return releaseTask;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Long getOrchestratorVersionId() {
        return orchestratorVersionId;
    }

    public void setOrchestratorVersionId(Long orchestratorVersionId) {
        this.orchestratorVersionId = orchestratorVersionId;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
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

    public String getStatus() {
        return status;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLogMsg() {
        return logMsg;
    }

    public void setLogMsg(String logMsg) {
        this.logMsg = logMsg;
    }

    public String getBak() {
        return bak;
    }

    public void setBak(String bak) {
        this.bak = bak;
    }

}
