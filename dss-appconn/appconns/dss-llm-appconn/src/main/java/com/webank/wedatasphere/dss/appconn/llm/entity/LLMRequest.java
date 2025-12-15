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

package com.webank.wedatasphere.dss.appconn.llm.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM请求实体类
 * 封装OpenAI兼容API的请求参数
 */
public class LLMRequest {

    /** 模型名称，如gpt-4、gpt-3.5-turbo等 */
    private String model;

    /** 消息列表 */
    private List<Message> messages;

    /** 温度参数，控制输出随机性，范围0-2 */
    private Double temperature;

    /** 最大生成token数 */
    private Integer maxTokens;

    /** MCP工具配置（JSON格式） */
    private List<Tool> tools;

    /** 流式输出，默认false */
    private Boolean stream;

    public LLMRequest() {
        this.messages = new ArrayList<>();
        this.stream = false;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(String role, String content) {
        this.messages.add(new Message(role, content));
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    /**
     * 消息类
     */
    public static class Message {
        private String role;     // system, user, assistant
        private String content;  // 消息内容

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 工具类（MCP工具配置）
     */
    public static class Tool {
        private String type;          // 工具类型，固定为"function"
        private Function function;    // 工具函数定义

        public Tool() {}

        public Tool(String type, Function function) {
            this.type = type;
            this.function = function;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Function getFunction() {
            return function;
        }

        public void setFunction(Function function) {
            this.function = function;
        }
    }

    /**
     * 函数定义类
     */
    public static class Function {
        private String name;          // 函数名
        private String description;   // 函数描述
        private Object parameters;    // 参数JSON Schema

        public Function() {}

        public Function(String name, String description, Object parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getParameters() {
            return parameters;
        }

        public void setParameters(Object parameters) {
            this.parameters = parameters;
        }
    }
}
