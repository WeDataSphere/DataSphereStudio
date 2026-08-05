/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.conf;

/**
 * DataGo飞书多维表格外发 AppConn 配置键。
 * <p>
 * 对齐接口文档 v1.1：通用外发接口 {@code /api/export/*}（form/task/execute）。
 */
public final class DataGoFeishuConfiguration {
    public static final String BASE_URL = "wds.dss.appconn.datago.feishu.api.base.url";

    /** ① 外发表单获取 */
    public static final String FORM_PATH = "wds.dss.appconn.datago.feishu.api.form.path";
    /** ② 外发任务生成/状态查询 */
    public static final String TASK_PATH = "wds.dss.appconn.datago.feishu.api.task.path";
    /** ③ 执行外发 */
    public static final String EXECUTE_PATH = "wds.dss.appconn.datago.feishu.api.execute.path";

    /** 服务间调用鉴权 token 值（内网默认网络层隔离，为空则不追加鉴权头） */
    public static final String API_TOKEN = "wds.dss.appconn.datago.feishu.api.token";
    /** 鉴权头名称，默认 Authorization */
    public static final String API_TOKEN_HEADER = "wds.dss.appconn.datago.feishu.api.token.header";

    /** 本期仅处理 optype=table，datago 走现有报告外发 */
    public static final String SUPPORTED_OPTYPE = "wds.dss.appconn.datago.feishu.optype.supported";

    /** ② 检测状态轮询间隔（毫秒，建议 10000~30000） */
    public static final String DETECT_INTERVAL = "wds.dss.appconn.datago.feishu.detect.poll.interval.ms";
    /** ③ 外发失败最大重试次数（502/504 可重试，413/409 不可重试） */
    public static final String EXECUTE_RETRY_MAX = "wds.dss.appconn.datago.feishu.execute.retry.max";
    /** ③ 外发重试间隔（毫秒） */
    public static final String EXECUTE_RETRY_INTERVAL = "wds.dss.appconn.datago.feishu.execute.retry.interval.ms";
    /** 节点最大等待时间（毫秒），超过则判定超时失败 */
    public static final String MAX_WAIT_TIME = "wds.dss.appconn.datago.feishu.max.wait.time.ms";
    /** HTTP 连接超时（毫秒） */
    public static final String CONNECT_TIMEOUT = "wds.dss.appconn.datago.feishu.http.connect.timeout.ms";
    /** HTTP 读取超时（毫秒） */
    public static final String READ_TIMEOUT = "wds.dss.appconn.datago.feishu.http.read.timeout.ms";

    /** ① 默认路径 */
    public static final String DEFAULT_FORM_PATH = "/api/export/form";
    /** ② 默认路径 */
    public static final String DEFAULT_TASK_PATH = "/api/export/task";
    /** ③ 默认路径 */
    public static final String DEFAULT_EXECUTE_PATH = "/api/export/execute";

    /** 默认仅支持 optype=table */
    public static final String DEFAULT_SUPPORTED_OPTYPE = "table";

    private DataGoFeishuConfiguration() {
    }
}
