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
package com.webank.wedatasphere.dss.orchestrator.common.entity;

import java.util.Date;

public class OrchestratorReleaseVersionInfo {


    private Long id;
    // 编排id
    private Long orchestratorId;

    // bml版本号
    private String version;

    // 版本更新时间
    private Date updateTime;

    // 发布状态
    private String status;

    // 发布状态fail时的错误信息
    private String errorMsg;

    // 更新人
    private String updater;

    private Date releaseTime;

    private Long releaseTaskId;


    private String releaseUser;

    public OrchestratorReleaseVersionInfo(Long id, Long orchestratorId, String version, Date updateTime, String status, String errorMsg, String updater, Date releaseTime, Long releaseTaskId) {
        this.id = id;
        this.orchestratorId = orchestratorId;
        this.version = version;
        this.updateTime = updateTime;
        this.status = status;
        this.errorMsg = errorMsg;
        this.updater = updater;
        this.releaseTime = releaseTime;
        this.releaseTaskId = releaseTaskId;
    }

    public Long getReleaseTaskId() {
        return releaseTaskId;
    }

    public void setReleaseTaskId(Long releaseTaskId) {
        this.releaseTaskId = releaseTaskId;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public OrchestratorReleaseVersionInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
    }
}
