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

import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2024/8/6
 */
public class HttpMsgReceiveResponse {
    private int retCode;
    private String message;
    private long msgId;
    private Map<String, Object> msgBody;
    /**
     * success成功
     * fail失败
     * running运行中
     */
    private String status;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public Map<String, Object> getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(Map<String, Object> msgBody) {
        this.msgBody = msgBody;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
