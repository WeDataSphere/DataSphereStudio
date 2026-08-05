/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.client;

import com.google.gson.Gson;
import com.webank.wedatasphere.dss.appconn.datagofeishu.conf.DataGoFeishuConfiguration;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.DataGoFeishuResponse;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.DataTarget;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.ExecuteResponse;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.ExportForm;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.NodeParams;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.TaskResponse;
import com.webank.wedatasphere.dss.appconn.datagofeishu.exception.DataGoFeishuException;
import com.webank.wedatasphere.dss.appconn.datagofeishu.utils.DataGoFeishuHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DataGo 接口客户端，封装 ①②③ 三个通用外发接口（对齐接口文档 v1.1）。
 * <ul>
 *   <li>① {@code POST /api/export/form}    外发表单获取（按 dmId 查库表/字段/分区/用户/optype/状态）</li>
 *   <li>② {@code POST /api/export/task}    外发任务生成（taskId 空）或状态查询（taskId 非空）</li>
 *   <li>③ {@code POST /api/export/execute} 执行外发（optype=table：建多维表格+写sheet+飞书消息，同步一次性）</li>
 * </ul>
 * <p>
 * Base URL：{@code http://{DataGoHost}:{DataGoPort}}（默认端口 3003）；{@code Content-Type: application/json; charset=utf-8}。
 * 底层 HTTP 调用与请求/响应明细日志由 {@link DataGoFeishuHttpUtils} 负责；本类记录各接口的业务级调用入口与结果摘要。
 * <p>
 * 错误码映射：HTTP 状态码 → DSS 异常码（82001~82009），见设计文档 4.3。
 */
public class DataGoFeishuClient {
    private static final Logger logger = LoggerFactory.getLogger(DataGoFeishuClient.class);
    private final Gson gson = new Gson();
    private final DataGoFeishuHttpUtils http;
    private final String baseUrl;
    private final String formPath;
    private final String taskPath;
    private final String executePath;

    /**
     * 从实例配置（enhance_json）初始化客户端。
     *
     * @param properties 含 base.url / 各接口路径 / 超时 / token 的配置
     */
    public DataGoFeishuClient(Properties properties) {
        baseUrl = required(properties, DataGoFeishuConfiguration.BASE_URL).replaceAll("/+$", "");
        formPath = properties.getProperty(DataGoFeishuConfiguration.FORM_PATH, DataGoFeishuConfiguration.DEFAULT_FORM_PATH);
        taskPath = properties.getProperty(DataGoFeishuConfiguration.TASK_PATH, DataGoFeishuConfiguration.DEFAULT_TASK_PATH);
        executePath = properties.getProperty(DataGoFeishuConfiguration.EXECUTE_PATH, DataGoFeishuConfiguration.DEFAULT_EXECUTE_PATH);
        long connectTimeout = longValue(properties, DataGoFeishuConfiguration.CONNECT_TIMEOUT, 10000L);
        long readTimeout = longValue(properties, DataGoFeishuConfiguration.READ_TIMEOUT, 60000L);
        http = new DataGoFeishuHttpUtils(connectTimeout, readTimeout,
                properties.getProperty(DataGoFeishuConfiguration.API_TOKEN_HEADER, "Authorization"),
                properties.getProperty(DataGoFeishuConfiguration.API_TOKEN, ""));
        logger.info("DataGoFeishuClient initialized, baseUrl={}, formPath={}, taskPath={}, executePath={}, connectTimeout={}, readTimeout={}",
                baseUrl, formPath, taskPath, executePath, connectTimeout, readTimeout);
    }

    /**
     * ① 外发表单获取：按 dmId 查询审批单涉及的库表、字段、分区、用户、optype、检测/外发状态。
     * <p>
     * 用于在节点执行前与用户填写的节点参数做一致性比对。
     *
     * @param dmId DM审批单号
     * @return 表单数据；dm 单不存在时抛 82002
     * @throws DataGoFeishuException 接口调用失败(82002)或 dm 单不存在(82002)
     */
    public ExportForm queryExportForm(String dmId) {
        String url = endpoint(formPath);
        logger.info("① queryExportForm start, dmId={}", dmId);
        DataGoFeishuResponse resp;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("dmId", dmId);
            resp = http.post(url,body);
        } catch (DataGoFeishuException e) {
            logger.error("① queryExportForm http call failed, url={}, dmId={}, error={}", url, dmId, e.getMessage());
            throw new DataGoFeishuException(82002, "DataGo表单接口调用失败: url=" + url + ", " + e.getMessage(), e);
        }
        if (!resp.isHttpOk() || !resp.isSuccess()) {
            logger.error("① queryExportForm business failed, url={}, dmId={}, httpCode={}, bizCode={}, message={}",
                    url, dmId, resp.getHttpCode(), resp.getCode(), resp.getMessage());
            throw new DataGoFeishuException(82002,
                    "DataGo表单接口调用失败: " + message(url, resp), resp.getHttpCode());
        }
        if (resp.getData() == null) {
            logger.warn("① queryExportForm no data, url={}, dmId={} (dm单不存在或无审批信息)", url, dmId);
            throw new DataGoFeishuException(82002,
                    "DM单不存在或无审批信息: url=" + url + ", dmId=" + dmId, resp.getHttpCode());
        }
        ExportForm form = gson.fromJson(resp.getData(), ExportForm.class);
        int tableCount = form.getTables() == null ? 0 : form.getTables().size();
        logger.info("① queryExportForm success, dmId={}, optype={}, status={}, tableCount={}, dmUser={}",
                dmId, form.getOptype(), form.getStatus(), tableCount, form.getDmUser());
        return form;
    }

    /**
     * ② 外发任务生成 / 状态查询（同一接口，两种语义）。
     * <ul>
     *   <li>taskId 为 null → 创建检测任务，返回 taskId 与初始状态</li>
     *   <li>taskId 非空 → 查询该任务当前状态</li>
     * </ul>
     *
     * @param params 节点参数（dmId / tables / fields / partitions）
     * @param taskId 任务ID；null 表示创建，非空表示查询
     * @return 任务响应（taskId / optype / status / resultSummary）
     * @throws DataGoFeishuException 接口调用失败(82004)或 dm 单无效/optype 非法(82003)
     */
    public TaskResponse createOrQueryTask(NodeParams params, Long taskId) {
        boolean isCreate = taskId == null;
        String url = endpoint(taskPath);
        logger.info("② createOrQueryTask start, dmId={}, mode={}, taskId={}",
                params.getDmId(), isCreate ? "CREATE" : "QUERY", taskId);
        DataGoFeishuResponse resp;
        try {
            resp = http.post(url, taskBody(params, taskId));
        } catch (DataGoFeishuException e) {
            logger.error("② createOrQueryTask http call failed, url={}, dmId={}, mode={}, error={}",
                    url, params.getDmId(), isCreate ? "CREATE" : "QUERY", e.getMessage());
            throw new DataGoFeishuException(82004, "DataGo任务接口调用失败: url=" + url + ", " + e.getMessage(), e);
        }
        if (!resp.isHttpOk() || !resp.isSuccess() || resp.getData() == null) {
            // 403 通常为 dm 单无效/optype 非法，映射为 82003
            int errorCode = resp.getHttpCode() == 403 ? 82003 : 82004;
            logger.error("② createOrQueryTask business failed, url={}, dmId={}, mode={}, httpCode={}, bizCode={}, message={}",
                    url, params.getDmId(), isCreate ? "CREATE" : "QUERY", resp.getHttpCode(), resp.getCode(), resp.getMessage());
            throw new DataGoFeishuException(errorCode,
                    "DataGo任务接口调用失败: " + message(url, resp), resp.getHttpCode());
        }
        TaskResponse task = gson.fromJson(resp.getData(), TaskResponse.class);
        logger.info("② createOrQueryTask success, dmId={}, mode={}, taskId={}, optype={}, status={}, summary={}",
                params.getDmId(), isCreate ? "CREATE" : "QUERY",
                task.getTaskId(), task.getOptype(), task.getStatus(), task.getResultSummary());
        return task;
    }

    /**
     * ③ 执行外发（optype=table，**异步接口**，对齐接口文档 v2.0）。
     * <p>
     * 首次调用触发后台外发并立即返回 {@code status=exporting}；外发完成后任务置 {@code exported}
     * 并写入多维表格 URL，再次调用③即返回 {@code status=exported} 与 {@code bitableUrl}。
     * 调用方据此轮询 {@code status} 由 {@code exporting} → {@code exported}。
     * <p>
     * v2.0③**不入参 notifyUsers**（通知用户在②创建任务时按表提供并校验，③仅在响应返回供调用方调④）；
     * 新增可选 {@code department}（决定多维表格所在云盘目录与命名，缺省 common）。
     * 502/504（飞书不可达）由上层重试；409（状态冲突/optype不支持）不可重试；413 已下线。
     *
     * @param dmId       DM审批单号
     * @param taskId     检测通过的任务ID
     * @param department 部门（可选，空则不传，服务端缺省 common）
     * @return 外发响应（status=exporting/exported；exported 时带 bitableUrl）
     * @throws DataGoFeishuException 外发失败，httpCode 决定可否重试
     */
    public ExecuteResponse executeExport(String dmId, Long taskId, String department) {
        String url = endpoint(executePath);
        logger.info("③ executeExport start, dmId={}, taskId={}, department={}", dmId, taskId, department);
        DataGoFeishuResponse resp;
        try {
            resp = http.post(url, executeBody(dmId, taskId, department));
        } catch (DataGoFeishuException e) {
            logger.error("③ executeExport http call failed, url={}, dmId={}, taskId={}, error={}", url, dmId, taskId, e.getMessage());
            throw new DataGoFeishuException(82007, "DataGo执行外发接口调用失败: url=" + url + ", " + e.getMessage(), e);
        }
        if (resp.isBusinessOk()) {
            ExecuteResponse export = gson.fromJson(resp.getData(), ExecuteResponse.class);
            logger.info("③ executeExport response, dmId={}, taskId={}, status={}, bitableName={}, bitableUrl={}, exportedAt={}",
                    dmId, taskId, export.getStatus(), export.getBitableName(), export.getBitableUrl(), export.getExportedAt());
            return export;
        }
        int errorCode = mapExecuteErrorCode(resp.getHttpCode());
        logger.error("③ executeExport failed, url={}, dmId={}, taskId={}, httpCode={}, bizCode={}, message={}, mappedErrorCode={}",
                url, dmId, taskId, resp.getHttpCode(), resp.getCode(), resp.getMessage(), errorCode);
        throw new DataGoFeishuException(errorCode,
                "DataGo执行外发失败: " + message(url, resp), resp.getHttpCode());
    }

    /**
     * ③ 执行外发 HTTP 错误码 → DSS 异常码映射（v2.0：413 已下线）。
     * <ul>
     *   <li>400 → 82001（参数错误）</li>
     *   <li>403 → 82003（越权，防御性保留；v2.0③不再有403）</li>
     *   <li>409 → 82009（状态冲突/optype不支持，不可重试）</li>
     *   <li>500/502/504 → 82007（内部错误/飞书不可达，502/504可重试）</li>
     * </ul>
     */
    private int mapExecuteErrorCode(int httpCode) {
        switch (httpCode) {
            case 400:
                return 82001;
            case 403:
                return 82003;
            case 409:
                return 82009;
            case 502:
            case 504:
            case 500:
            default:
                return 82007;
        }
    }

    /**
     * 构造 ② 任务接口请求体：dmId / tables / fields / partitions / taskId。
     * taskId 为 null 时表示创建（Gson serializeNulls 保证 "taskId": null）。
     */
    private Map<String, Object> taskBody(NodeParams params, Long taskId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dmId", params.getDmId());
        // dataTargets 模型下每条目标一个 table 项
        java.util.List<Map<String, Object>> tables = new java.util.ArrayList<>();
        if (params.getDataTargets() != null) {
            for (DataTarget target : params.getDataTargets()) {
                Map<String, Object> table = new LinkedHashMap<>();
                table.put("dbName", target.getDbName());
                table.put("tableName", target.getTableName());
                if (target.getPartition() != null && !target.getPartition().isEmpty()) {
                    table.put("partition", target.getPartition());
                }
                table.put("notifyUsers",params.getNotifyUsers());
                tables.add(table);
            }
        }
        body.put("tables", tables);
        // fields 取所有目标的字段合集（与接口 fields 契约对齐）
        java.util.List<String> fields = new java.util.ArrayList<>();
        if (params.getDataTargets() != null) {
            for (DataTarget target : params.getDataTargets()) {
                if (target.getFields() != null) {
                    fields.addAll(target.getFields());
                }
            }
        }
        body.put("fields", fields);
        java.util.List<String> partitions = new java.util.ArrayList<>();
        if (params.getDataTargets() != null) {
            for (DataTarget target : params.getDataTargets()) {
                if (target.getPartition() != null && !target.getPartition().isEmpty()) {
                    partitions.add(target.getPartition());
                }
            }
        }
        body.put("partitions", partitions);
        body.put("taskId", taskId);
        return body;
    }

    /**
     * 构造 ③ 执行外发请求体（v2.0）：dmId / optype=table / taskIds / department(可选)。
     * <p>
     * v2.0③**不入参 notifyUsers**（通知用户在②按表提供并校验）。
     */
    private Map<String, Object> executeBody(String dmId, Long taskId, String department) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dmId", dmId);
        body.put("optype", "table");
        body.put("taskIds", Collections.singletonList(taskId));
        if (department != null && !department.trim().isEmpty()) {
            body.put("department", department.trim());
        }
        return body;
    }

    private String endpoint(String path) {
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DataGoFeishuException(82002, "DM单号编码失败", e);
        }
    }

    /**
     * 拼装异常消息：包含请求 URL、HTTP/业务 code、message，便于从异常栈直接定位失败接口。
     */
    private String message(String url, DataGoFeishuResponse resp) {
        String msg = resp.getMessage();
        int code = resp.getCode() != null ? resp.getCode() : resp.getHttpCode();
        return "url=" + url + ", code=" + code
                + ", message=" + (msg == null || msg.trim().isEmpty() ? "unknown" : msg);
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new DataGoFeishuException(82001, "AppConn配置 " + key + " 不能为空");
        }
        return value.trim();
    }

    public static long longValue(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new DataGoFeishuException(82001, "配置 " + key + " 必须为整数", e);
        }
    }
}
