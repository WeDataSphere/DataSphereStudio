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
package com.webank.wedatasphere.dss.common.entity;

public abstract class Alter {

    /**
     * 告警标题，少于100个字符，必填项
     */
    private String alterTitle;

    private String alterInfo;

    private String alterLevel;

    private String alterReceiver;

    protected Alter() {
    }

    protected Alter(String alterTitle, String alterInfo, String alterLevel, String alterReceiver) {
        this.alterTitle = alterTitle;
        this.alterInfo = alterInfo;
        this.alterLevel = alterLevel;
        this.alterReceiver = alterReceiver;
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

    public String getAlterLevel() {
        return alterLevel;
    }

    public void setAlterLevel(String alterLevel) {
        this.alterLevel = alterLevel;
    }

    public String getAlterReceiver() {
        return alterReceiver;
    }

    public void setAlterReceiver(String alterReceiver) {
        this.alterReceiver = alterReceiver;
    }


    @Override
    public String toString() {
        return "Alter{" +
                "alterTitle='" + alterTitle + '\'' +
                ", alterInfo='" + alterInfo + '\'' +
                ", alterLevel='" + alterLevel + '\'' +
                ", alterReceiver='" + alterReceiver + '\'' +
                '}';
    }
}
