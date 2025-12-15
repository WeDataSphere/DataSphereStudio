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

package com.webank.wedatasphere.dss.appconn.llm.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.appconn.llm.entity.LLMRequest;
import com.webank.wedatasphere.dss.appconn.llm.entity.LLMResponse;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * LLM客户端类
 * 负责调用OpenAI兼容的LLM API
 * 支持API Key认证、MCP工具配置、重试机制等
 */
public class LLMClient {

    private static final Logger logger = LoggerFactory.getLogger(LLMClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String apiUrl;
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final int maxRetries;
    private final long retryDelayMs;

    /**
     * 构造函数
     * @param properties 配置属性
     */
    public LLMClient(Properties properties) {
        // 读取API配置
        this.apiUrl = properties.getProperty("llm.api.url", "https://api.openai.com/v1/chat/completions");
        this.apiKey = properties.getProperty("llm.api.key");
        this.maxRetries = Integer.parseInt(properties.getProperty("llm.max.retries", "3"));
        this.retryDelayMs = Long.parseLong(properties.getProperty("llm.retry.delay.ms", "1000"));

        // 验证必需参数
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("LLM API Key不能为空，请在AppConn实例配置中设置llm.api.key");
        }

        // 创建HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)  // LLM响应可能较慢
                .build();

        logger.info("LLMClient initialized with API URL: {}", apiUrl);
    }

    /**
     * 调用LLM Chat API
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param model        模型名称
     * @param temperature  温度参数
     * @param maxTokens    最大Token数
     * @param mcpToolsJson MCP工具配置（JSON格式）
     * @return LLM响应
     */
    public LLMResponse chat(String systemPrompt, String userPrompt, String model,
                            Double temperature, Integer maxTokens, String mcpToolsJson) {
        // 1. 构建请求对象
        LLMRequest request = new LLMRequest();
        request.setModel(model);
        request.setTemperature(temperature);
        request.setMaxTokens(maxTokens);

        // 2. 添加系统提示词
        if (StringUtils.isNotBlank(systemPrompt)) {
            request.addMessage("system", systemPrompt);
        }

        // 3. 添加用户提示词
        if (StringUtils.isNotBlank(userPrompt)) {
            request.addMessage("user", userPrompt);
        } else {
            throw new IllegalArgumentException("用户提示词不能为空");
        }

        // 4. 解析并添加MCP工具
        if (StringUtils.isNotBlank(mcpToolsJson)) {
            try {
                List<LLMRequest.Tool> tools = gson.fromJson(
                        mcpToolsJson,
                        new TypeToken<List<LLMRequest.Tool>>() {}.getType()
                );
                request.setTools(tools);
                logger.info("Loaded {} MCP tools from configuration", tools.size());
            } catch (JsonSyntaxException e) {
                logger.warn("Failed to parse MCP tools JSON, ignoring: {}", e.getMessage());
            }
        }

        // 5. 执行API调用（带重试机制）
        return executeWithRetry(request);
    }

    /**
     * 执行API调用（带重试机制）
     */
    private LLMResponse executeWithRetry(LLMRequest request) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Calling LLM API, attempt {}/{}", attempt, maxRetries);
                return executeRequest(request);
            } catch (IOException e) {
                lastException = e;
                logger.warn("LLM API call failed (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());

                // 如果不是最后一次重试，等待后重试
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);  // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试等待被中断", ie);
                    }
                }
            } catch (Exception e) {
                // 非网络异常，直接抛出不重试
                throw new RuntimeException("LLM API调用失败: " + e.getMessage(), e);
            }
        }

        // 所有重试均失败
        throw new RuntimeException("LLM API调用失败，已重试" + maxRetries + "次", lastException);
    }

    /**
     * 执行单次API请求
     */
    private LLMResponse executeRequest(LLMRequest request) throws IOException {
        // 1. 序列化请求体
        String requestBody = gson.toJson(request);
        logger.debug("LLM API request body: {}", requestBody);

        // 2. 构建HTTP请求
        Request httpRequest = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(requestBody, JSON))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        // 3. 执行请求
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            // 4. 检查响应状态
            if (!response.isSuccessful()) {
                logger.error("LLM API returned error, status: {}, body: {}", response.code(), responseBody);
                throw new IOException("LLM API返回错误: " + response.code() + " - " + responseBody);
            }

            // 5. 解析响应
            logger.debug("LLM API response body: {}", responseBody);
            LLMResponse llmResponse = gson.fromJson(responseBody, LLMResponse.class);

            // 6. 验证响应
            if (llmResponse == null || llmResponse.getChoices() == null || llmResponse.getChoices().isEmpty()) {
                throw new IOException("LLM API返回了空响应");
            }

            String content = llmResponse.getContent();
            if (StringUtils.isBlank(content)) {
                throw new IOException("LLM API返回了空内容");
            }

            logger.info("LLM API call successful, response length: {} chars, tokens: {}",
                    content.length(),
                    llmResponse.getUsage() != null ? llmResponse.getUsage().getTotalTokens() : "unknown");

            return llmResponse;
        }
    }

    /**
     * 关闭客户端（释放资源）
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}
