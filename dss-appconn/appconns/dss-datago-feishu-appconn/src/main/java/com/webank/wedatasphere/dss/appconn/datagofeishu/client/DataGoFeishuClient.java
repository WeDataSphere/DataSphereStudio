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
 * DataGo 接口客户端，封装 ①②③ 三个通用外发接口（对齐接口文档 v2.0）。
 * <ul>
 *   <li>① {@code POST /api/export/form}    外发表单获取（按 dmId 查库表/字段/分区/用户/optype/状态）</li>
 *   <li>② {@code POST /api/export/task}    外发任务生成（taskId 空）或状态查询（taskId 非空）；fields/partitions/notifyUsers 内嵌于每个 tables[]</li>
 *   <li>③ {@code POST /api/export/execute} 执行外发（optype=table，异步：触发后台外发并轮询 exported；③ 不代发通知，由调用方调④）</li>
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
     * @param params 节点参数（dmId / notifyUsers / dataTargets；每表 fields/partitions/notifyUsers 内嵌于 tables[]）
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
            // 透传 envelope 顶层 message（非 data 字段），供 export_failed 时打印接口返回的失败提示
            export.setMessage(resp.getMessage());
            logger.info("③ executeExport response, dmId={}, taskId={}, status={}, bitableName={}, bitableUrl={}, exportedAt={}, message={}",
                    dmId, taskId, export.getStatus(), export.getBitableName(), export.getBitableUrl(), export.getExportedAt(), export.getMessage());
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
     * 构造 ② 任务接口请求体（对齐接口文档 v1.7/v1.8/v2.0）：dmId / tables[] / taskId。
     * <p>
     * v1.7 起 {@code fields}/{@code partitions}/{@code notifyUsers} 全部内嵌于每个 {@code tables[]} 元素，
     * 外层不再有这些参数；{@code partitions} 由单值改为每表 {@code string[]}。
     * 同一 (db, table) 的多个外发目标按表合并为单一元素（同表多分区写入同一 sheet）：
     * {@code fields} 取该表所有目标字段并集（去重保序）、{@code partitions} 取该表所有非空分区并集（去重保序）、
     * {@code notifyUsers} 为节点通知人（每表一致）。
     * taskId 为 null 时表示创建（Gson serializeNulls 保证 "taskId": null）。
     */
    private Map<String, Object> taskBody(NodeParams params, Long taskId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dmId", params.getDmId());
        body.put("tables", buildTables(params));
        body.put("taskId", taskId);
        return body;
    }

    /**
     * 由 dataTargets 构造 ② 的 tables[]：按归一化 (db, table) 合并，每表一个元素。
     * <p>
     * 同表多目标并入同一元素：{@code fields} 取该表所有目标字段并集（trim、去重、保序），
     * {@code partitions} 取该表所有非空分区并集（trim、去重、保序，无分区则不输出该字段），
     * {@code notifyUsers} 取节点通知人（每表一致）。合并键仅 (db, table)，与 {@link DataTarget#tableKey()}
     * （含 partition）的去重语义互不影响——后者管解析期同 (db,table,partition) 去重，前者管请求体同表合并。
     */
    private java.util.List<Map<String, Object>> buildTables(NodeParams params) {
        java.util.List<Map<String, Object>> tables = new java.util.ArrayList<>();
        if (params.getDataTargets() == null) {
            return tables;
        }
        // 同表合并：首次出现的目标提供 dbName/tableName，后续目标仅并入 fields/partitions
        Map<String, DataTarget> firstByTable = new LinkedHashMap<>();
        Map<String, java.util.LinkedHashSet<String>> fieldsByTable = new LinkedHashMap<>();
        Map<String, java.util.LinkedHashSet<String>> partitionsByTable = new LinkedHashMap<>();
        for (DataTarget target : params.getDataTargets()) {
            String key = tableKeyOf(target.getDbName(), target.getTableName());
            firstByTable.putIfAbsent(key, target);
            // 无条件初始化字段桶，保证 firstByTable 的 key 在 fieldsByTable 必然存在，避免下游 NPE
            java.util.LinkedHashSet<String> fieldSet = fieldsByTable.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>());
            if (target.getFields() != null) {
                for (String field : target.getFields()) {
                    if (field != null && !field.trim().isEmpty()) {
                        fieldSet.add(field.trim());
                    }
                }
            }
            if (target.hasPartition()) {
                partitionsByTable.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>()).add(target.getPartition().trim());
            }
        }
        for (Map.Entry<String, DataTarget> entry : firstByTable.entrySet()) {
            String key = entry.getKey();
            DataTarget first = entry.getValue();
            Map<String, Object> table = new LinkedHashMap<>();
            table.put("dbName", first.getDbName());
            table.put("tableName", first.getTableName());
            table.put("fields", new java.util.ArrayList<>(fieldsByTable.get(key)));
            java.util.LinkedHashSet<String> partitions = partitionsByTable.get(key);
            if (partitions != null && !partitions.isEmpty()) {
                table.put("partitions", new java.util.ArrayList<>(partitions));
            }
            table.put("notifyUsers", params.getNotifyUsers());
            tables.add(table);
        }
        return tables;
    }

    /** 归一化 (db, table) 合并键：trim 后以 '.' 拼接，仅用于 ② tables[] 同表合并，不含分区。 */
    private static String tableKeyOf(String dbName, String tableName) {
        return (dbName == null ? "" : dbName.trim()) + "." + (tableName == null ? "" : tableName.trim());
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
