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

package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;

public class RequestOrcSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String projectName;
    private int orchestratorId;
    private String scheduleTime;
    private String alarmEmails;
    private String alarmLevel;

    public RequestOrcSchedule() {
    }

    public RequestOrcSchedule(String username, String projectName, int orchestratorId, String scheduleTime, String alarmEmails, String alarmLevel) {
        this.username = username;
        this.projectName = projectName;
        this.orchestratorId = orchestratorId;
        this.scheduleTime = scheduleTime;
        this.alarmEmails = alarmEmails;
        this.alarmLevel = alarmLevel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(int orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getAlarmEmails() {
        return alarmEmails;
    }

    public void setAlarmEmails(String alarmEmails) {
        this.alarmEmails = alarmEmails;
    }

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
}
