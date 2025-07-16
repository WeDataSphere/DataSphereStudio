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
public class HttpMsgSendRequest {
    private String sender;
    private String topic;
    private String msgName;
    private String runDate;
    private String msgId;
    private Map<String, Object> msgBody;

    public HttpMsgSendRequest(String sender, String topic, String msgName, String runDate, String msgId, Map<String, Object> msgBody) {
        this.sender = sender;
        this.topic = topic;
        this.msgName = msgName;
        this.runDate = runDate;
        this.msgId = msgId;
        this.msgBody = msgBody;
    }

    public HttpMsgSendRequest() {
        this.msgBody = new HashMap<>();
    }

    public HttpMsgSendRequest setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public HttpMsgSendRequest setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public HttpMsgSendRequest setMsgName(String msgName) {
        this.msgName = msgName;
        return this;
    }

    public HttpMsgSendRequest setRunDate(String runDate) {
        this.runDate = runDate;
        return this;
    }

    public HttpMsgSendRequest setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }


    public HttpMsgSendRequest addMsgBodyField(String key, Object value) {
        this.msgBody.put(key, value);
        return this;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
