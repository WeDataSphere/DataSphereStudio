/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.entity;

import com.webank.wedatasphere.dss.appconn.datagofeishu.exception.DataGoFeishuException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 节点参数模型，字段名对齐接口文档 v1.1。
 * <p>
 * dmId / notifyUsers / dataTargets（多库多表多字段多分区）。
 * <p>
 * dataTargets 为行式 DSL，每行一个外发目标：
 * <pre>
 * dt.序号=db=库名|table=表名|fields=字段1,字段2|partition={分区值}
 * </pre>
 * 行间用分号(;)或换行分隔；partition 段可选，值为 {..} 时取花括号内内容，为空或省略表示非分区表。
 */
public class NodeParams {
    private String dmId;
    private List<String> notifyUsers;
    private List<DataTarget> dataTargets;

    public static NodeParams from(Properties properties) {
        NodeParams params = new NodeParams();
        params.dmId = required(properties, "dmId");
        params.notifyUsers = split(required(properties, "notifyUsers"), "[,;]");
        if (params.notifyUsers.isEmpty()) {
            throw new DataGoFeishuException(82001, "飞书通知人不能为空");
        }
        String targetsRaw = properties.getProperty("dataTargets");
        if (targetsRaw != null && !targetsRaw.trim().isEmpty()) {
            params.dataTargets = parseDataTargets(targetsRaw);
        } else {
            // 兼容回退：旧单值字段包装成单元素 target
            params.dataTargets = Collections.singletonList(legacySingleTarget(properties));
        }
        if (params.dataTargets.isEmpty()) {
            throw new DataGoFeishuException(82001, "外发目标不能为空");
        }
        return params;
    }

    /**
     * 解析 dataTargets DSL。
     * 行间 ; / \r / \n 分隔；每行首段 dt.序号=主体；主体按 | 切段，每段按首个 = 切 key/value；
     * partition 段值形如 {..} 则去花括号。
     */
    public static List<DataTarget> parseDataTargets(String raw) {
        List<DataTarget> targets = new ArrayList<>();
        Set<String> tableKeys = new LinkedHashSet<>();
        String[] rows = raw.trim().split("[;\\r\\n]+");
        for (String row : rows) {
            String trimmed = row.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int headEq = trimmed.indexOf('=');
            if (headEq <= 0) {
                throw new DataGoFeishuException(82001, "外发目标格式错误，需以 dt.序号= 开头: " + trimmed);
            }
            String head = trimmed.substring(0, headEq).trim();
            if (!head.matches("dt\\.\\d+")) {
                throw new DataGoFeishuException(82001, "外发目标编号格式错误，需为 dt.序号: " + head);
            }
            String body = trimmed.substring(headEq + 1).trim();
            if (body.isEmpty()) {
                throw new DataGoFeishuException(82001, "外发目标 " + head + " 缺少内容");
            }

            String dbName = null;
            String tableName = null;
            List<String> fields = null;
            String partition = null;
            for (String seg : body.split("\\|")) {
                String s = seg.trim();
                if (s.isEmpty()) {
                    continue;
                }
                int eq = s.indexOf('=');
                if (eq <= 0) {
                    throw new DataGoFeishuException(82001, "外发目标 " + head + " 段格式错误: " + s);
                }
                String key = s.substring(0, eq).trim();
                String value = s.substring(eq + 1);
                switch (key) {
                    case "db":
                        dbName = value.trim();
                        break;
                    case "table":
                        tableName = value.trim();
                        break;
                    case "fields":
                        fields = split(value, ",");
                        break;
                    case "partition":
                        partition = unwrapBraces(value.trim());
                        break;
                    default:
                        throw new DataGoFeishuException(82001, "外发目标 " + head + " 存在未知字段: " + key);
                }
            }
            if (isBlank(dbName) || isBlank(tableName) || fields == null || fields.isEmpty()) {
                throw new DataGoFeishuException(82001,
                        "外发目标 " + head + " 缺少 db/table/fields 必填段");
            }
            DataTarget target = new DataTarget(dbName, tableName, fields, partition);
            if (!tableKeys.add(target.tableKey())) {
                throw new DataGoFeishuException(82001,
                        "外发目标 " + head + " " + target.tableKey() + " 重复，请检查");
            }
            targets.add(target);
        }
        return targets;
    }

    /** partition 段值若形如 {..} 去花括号；空串归一为 null（非分区表） */
    private static String unwrapBraces(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            String inner = value.substring(1, value.length() - 1);
            return inner.isEmpty() ? null : inner;
        }
        return value;
    }

    /** 旧单值字段兼容：dbName/tableName/fields/partition 包装成单个 target */
    private static DataTarget legacySingleTarget(Properties properties) {
        String dbName = required(properties, "dbName");
        String tableName = required(properties, "tableName");
        List<String> fields = split(required(properties, "fields"), "[,;]");
        if (fields.isEmpty()) {
            throw new DataGoFeishuException(82001, "字段名称不能为空");
        }
        String partition = properties.getProperty("partition");
        return new DataTarget(dbName, tableName, fields, isBlank(partition) ? null : partition.trim());
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new DataGoFeishuException(82001, "节点参数 " + key + " 不能为空");
        }
        return value.trim();
    }

    public static List<String> split(String value, String separator) {
        Set<String> normalized = new LinkedHashSet<>();
        if (value != null) {
            for (String item : value.split(separator)) {
                if (item != null && !item.trim().isEmpty()) {
                    normalized.add(item.trim());
                }
            }
        }
        List<String> result = new ArrayList<>(normalized);
        Collections.sort(result);
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getDmId() { return dmId; }
    public List<String> getNotifyUsers() { return notifyUsers; }
    public List<DataTarget> getDataTargets() { return dataTargets; }
}
