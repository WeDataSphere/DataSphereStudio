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
package com.webank.wedatasphere.dss.appconn.eventchecker.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2024/8/2
 */
public class HttpMsgReceiveRequest {
    private String receiver;
    private String topic;
    private String msgName;
    private String runDate;
    private boolean onlyReceiveToday;
    private boolean receiveUseRunDate;


    public HttpMsgReceiveRequest(String receiver, String topic, String msgName, String runDate, boolean onlyReceiveToday, boolean receiveUseRunDate) {
        this.receiver = receiver;
        this.topic = topic;
        this.msgName = msgName;
        this.runDate = runDate;
        this.onlyReceiveToday = onlyReceiveToday;
        this.receiveUseRunDate = receiveUseRunDate;
    }

    public HttpMsgReceiveRequest() {

    }

    public HttpMsgReceiveRequest setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public HttpMsgReceiveRequest setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public HttpMsgReceiveRequest setMsgName(String msgName) {
        this.msgName = msgName;
        return this;
    }

    public HttpMsgReceiveRequest setRunDate(String runDate) {
        this.runDate = runDate;
        return this;
    }

    public HttpMsgReceiveRequest setOnlyReceiveToday(boolean onlyReceiveToday) {
        this.onlyReceiveToday = onlyReceiveToday;
        return this;
    }

    public HttpMsgReceiveRequest setReceiveUseRunDate(boolean receiveUseRunDate) {
        this.receiveUseRunDate = receiveUseRunDate;
        return this;
    }


    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
