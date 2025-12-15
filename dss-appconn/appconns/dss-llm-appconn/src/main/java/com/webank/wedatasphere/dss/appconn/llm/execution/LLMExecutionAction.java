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

package com.webank.wedatasphere.dss.appconn.llm.execution;

import com.webank.wedatasphere.dss.standard.app.development.listener.common.AbstractRefExecutionAction;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef;

import java.util.Properties;

/**
 * LLM执行动作类
 * 用于存储LLM执行过程中的状态和结果
 */
public class LLMExecutionAction extends AbstractRefExecutionAction {

    // LLM响应内容
    private String llmResponse;

    // 执行响应引用
    private ExecutionResponseRef executionResponseRef;

    // 属性配置
    private Properties properties;

    // 结果保存键名
    private String saveKey = "llm.response";

    // Token使用统计
    private Integer promptTokens = 0;
    private Integer completionTokens = 0;
    private Integer totalTokens = 0;

    // Getters and Setters

    public String getLlmResponse() {
        return llmResponse;
    }

    public void setLlmResponse(String llmResponse) {
        this.llmResponse = llmResponse;
    }

    public ExecutionResponseRef getExecutionResponseRef() {
        return executionResponseRef;
    }

    public void setExecutionResponseRef(ExecutionResponseRef executionResponseRef) {
        this.executionResponseRef = executionResponseRef;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getSaveKey() {
        return saveKey;
    }

    public void setSaveKey(String saveKey) {
        this.saveKey = saveKey;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
}
