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
package com.webank.wedatasphere.dss.framework.admin.common.domain;

//import lombok.Data;

import java.util.HashMap;
import java.util.Map;
//@Data
public class Message {
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    private String method;
    private Integer status;
    private String  message;
    private Map<String, Object> data = new HashMap<>();

    public Message() {
    }


    /**
     * 返回成功结果
     *
     * @return
     */
    public static Message ok() {
        Message message = new Message();
        message.setStatus(ResponseEnum.SUCCESS.getStatus());
        message.setMessage(ResponseEnum.SUCCESS.getMessage());
        return message;
    }


    /**
     * 返回失败结果
     *
     * @return
     */
    public static Message error() {
        Message message = new Message();
        message.setStatus(ResponseEnum.ERROR.getStatus());
        message.setMessage(ResponseEnum.ERROR.getMessage());
        return message;
    }

    /**
     * 返回特定结果
     *
     * @param responseEnum
     * @return
     */
    public static Message setResult(ResponseEnum responseEnum) {
        Message message = new Message();
        message.setStatus(responseEnum.getStatus());
        message.setMessage(responseEnum.getMessage());
        return message;
    }

    /**
     * 封装返回数据
     *
     * @param key
     * @param value
     * @return
     */
    public Message data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 封装返回数据，若为Map集合，则直接赋值即可
     *
     * @param map
     * @return
     */
    public Message data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * 设置特定的消息
     *
     * @param message
     * @return
     */
    public Message message(String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * 设置特定的响应码
     *
     * @param code
     * @return
     */
    public Message status(Integer code) {
        this.setStatus(code);
        return this;
    }

}
