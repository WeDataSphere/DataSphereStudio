/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.DataGoFeishuResponse;
import com.webank.wedatasphere.dss.appconn.datagofeishu.exception.DataGoFeishuException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DataGo 接口 HTTP 工具，封装对 DataGo 通用外发接口（{@code /api/export/*}）的底层调用。
 * <p>
 * 统一响应结构 {@code {success, code, message, data}}；仅在网络/解析异常时抛出，
 * HTTP 与业务层失败均封装为 {@link DataGoFeishuResponse}（携带 httpCode）交由上层判定。
 * <p>
 * Gson 开启 serializeNulls，使 ② 创建请求的 {@code "taskId": null} 符合接口契约。
 * <p>
 * <b>日志策略</b>：对每次请求记录「方法 + URL + 请求体」「HTTP 状态码 + 耗时 + 响应体摘要」，
 * 便于全链路排查 DataGo 接口调用问题。响应体过长时仅记录前 N 个字符，避免日志膨胀。
 */
public class DataGoFeishuHttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataGoFeishuHttpUtils.class);
    /** 响应体日志截断长度，超过部分以 "...(truncated)" 标识 */
    private static final int LOG_BODY_LIMIT = 2000;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final String tokenHeader;
    private final String token;

    public DataGoFeishuHttpUtils(long connectTimeout, long readTimeout, String tokenHeader, String token) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
        this.tokenHeader = tokenHeader;
        this.token = token;
    }

    /**
     * 发送 GET 请求。
     *
     * @param url 完整请求地址（含 query 参数）
     * @return DataGo 统一响应持有者（携带 httpCode / success / code / message / data）
     */
    public DataGoFeishuResponse get(String url) {
        Request request = addToken(new Request.Builder().url(url).get()).build();
        return execute(request, "GET", url, null);
    }

    /**
     * 发送 POST 请求（JSON body）。
     *
     * @param url  完整请求地址
     * @param body 请求体对象，由 Gson 序列化为 JSON（含 null 字段）
     * @return DataGo 统一响应持有者
     */
    public DataGoFeishuResponse post(String url, Object body) {
        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = addToken(new Request.Builder().url(url).post(requestBody)).build();
        return execute(request, "POST", url, jsonBody);
    }

    /**
     * 追加服务间调用鉴权头（内网默认网络层隔离，token 为空时不追加）。
     */
    private Request.Builder addToken(Request.Builder builder) {
        if (token != null && !token.trim().isEmpty()) {
            builder.header(tokenHeader, token);
        }
        return builder;
    }

    /**
     * 执行 HTTP 请求并记录详细请求/响应日志。
     * <p>
     * 请求日志：方法、URL、请求体（POST）。
     * 响应日志：HTTP 状态码、耗时(ms)、业务 success/code、响应体摘要（截断）。
     * 网络/解析异常时抛 {@link DataGoFeishuException}（错误码 82011）。
     *
     * @param request  OkHttp 请求对象
     * @param method   HTTP 方法（GET/POST，仅用于日志）
     * @param url      请求 URL（用于日志）
     * @param jsonBody POST 请求体 JSON（GET 传 null）
     * @return DataGo 统一响应持有者
     */
    private DataGoFeishuResponse execute(Request request, String method, String url, String jsonBody) {
        // 请求日志：记录调用入口，便于排查「调用了哪个接口、带什么参数」
        logger.info("DataGo request >>> {} {} body={}", method, url, jsonBody);

        long start = System.currentTimeMillis();
        DataGoFeishuResponse result = new DataGoFeishuResponse();
        String rawText = "";
        try (Response response = client.newCall(request).execute()) {
            long elapsed = System.currentTimeMillis() - start;
            result.setHttpCode(response.code());
            ResponseBody responseBody = response.body();
            rawText = responseBody == null ? "" : responseBody.string();
            if (rawText.trim().isEmpty()) {
                logger.warn("DataGo response <<< {} {} httpCode={} elapsed={}ms body=<empty>",
                        method, url, response.code(), elapsed);
                throw new DataGoFeishuException(82011,
                        "DataGo接口响应是空值,response body is: " + rawText);
            }
            JsonElement json = JsonParser.parseString(rawText);
            if (!json.isJsonObject()) {
                logger.error("DataGo response <<< {} {} httpCode={} elapsed={}ms body is NOT a json object: {}",
                        method, url, response.code(), elapsed, rawText);
                throw new DataGoFeishuException(82011,
                        "DataGo接口响应不是JSON对象", response.code());
            }
            JsonObject root = json.getAsJsonObject();
            result.setSuccess(root.has("success") && !root.get("success").isJsonNull()
                    && root.get("success").getAsBoolean());
            result.setCode(readInt(root, "code"));
            result.setMessage(readString(root, "message", "msg"));
            if (root.has("data") && root.get("data").isJsonObject()) {
                result.setData(root.getAsJsonObject("data"));
            }
            // 响应日志：HTTP 状态码 + 耗时 + 业务 code/success + 响应体摘要
            logger.info("DataGo response <<< {} {} httpCode={} success={} bizCode={} elapsed={}ms body={}",
                    method, url, response.code(), result.isSuccess(), result.getCode(),
                    elapsed, rawText);
            return result;
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - start;
            logger.error("DataGo request failed <<< {} {} elapsed={}ms error={}",
                    method, url, elapsed, e.getMessage());
            throw new DataGoFeishuException(82011, "DataGo接口请求失败: " + e.getMessage(), e);
        }
    }

    private Integer readInt(JsonObject root, String key) {
        if (root.has(key) && !root.get(key).isJsonNull()) {
            try {
                return root.get(key).getAsInt();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String readString(JsonObject root, String... keys) {
        for (String key : keys) {
            if (root.has(key) && !root.get(key).isJsonNull()) {
                return root.get(key).getAsString();
            }
        }
        return null;
    }
}
