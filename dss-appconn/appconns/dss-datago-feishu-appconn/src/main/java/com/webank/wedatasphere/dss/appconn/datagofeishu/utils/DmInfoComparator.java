/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.utils;

import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.DataTarget;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.ExportForm;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.ExportTable;
import com.webank.wedatasphere.dss.appconn.datagofeishu.entity.NodeParams;
import com.webank.wedatasphere.dss.appconn.datagofeishu.exception.DataGoFeishuException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DM单表单（① 返回）与节点参数一致性比对。
 * <p>
 * 比对项：DM单存在性、optype、状态、逐外发目标库表命中、字段一致、分区范围、通知人授权。
 * notifyUsers 的子集校验在①返回了审批允许通知人集合时执行，否则交由③（403）兜底。
 */
public final class DmInfoComparator {
    private DmInfoComparator() { }

    public static void validate(NodeParams params, ExportForm form, String supportedOptype) {
        if (form == null) {
            throw new DataGoFeishuException(82002, "DataGo未返回DM单信息");
        }
        String optype = normalize(form.getOptype());
        String supported = normalize(supportedOptype);
        if (supported != null && !supported.isEmpty() && !supported.equals(optype)) {
            throw new DataGoFeishuException(82003,
                    "DM单optype=" + optype + "非" + supported + "，请走现有报告外发接口");
        }
        validateStatus(form.getStatus());
        // 逐外发目标比对：库表命中、字段一致、分区范围
        int idx = 0;
        for (DataTarget target : params.getDataTargets()) {
            idx++;
            ExportTable table = matchTable(form, target, idx);
            validateFields(target, table, idx);
            validatePartition(target, table, idx);
        }
        validateNotifyUsers(params, form);
    }

    private static void validateStatus(String status) {
        String value = normalize(status);
        if (value == null || value.isEmpty()) {
            return;
        }
        switch (value) {
            case "detected_fail":
                throw new DataGoFeishuException(82005, "DM单检测不通过（命中敏感），DataGo已飞书通知");
            case "detect_error":
                throw new DataGoFeishuException(82006, "DM单检测异常，DataGo已飞书通知");
            case "exported":
                throw new DataGoFeishuException(82002, "DM单已外发，不可重复外发");
            default:
                // inited / detecting / detected_pass 等可继续
        }
    }

    private static ExportTable matchTable(ExportForm form, DataTarget target, int idx) {
        if (form.getTables() == null || form.getTables().isEmpty()) {
            mismatch("DM单未包含任何库表");
        }
        for (ExportTable table : form.getTables()) {
            if (equals(target.getDbName(), table.getDbName())
                    && equals(target.getTableName(), table.getTableName())) {
                return table;
            }
        }
        mismatch("第" + idx + "个外发目标库表 " + target.getDbName() + "." + target.getTableName()
                + " 不在DM单审批范围内");
        return null;
    }

    private static void validateFields(DataTarget target, ExportTable table, int idx) {
        List<String> approved = normalize(table.getColumns());
        List<String> node = normalize(target.getFields());
        if (approved.isEmpty()) {
            mismatch("第" + idx + "个外发目标 " + target.tableKey() + " DM单未返回审批字段，无法校验");
        }
        if (!approved.equals(node)) {
            mismatch("第" + idx + "个外发目标 " + target.tableKey() + " 字段与DM单审批字段不一致，审批字段: "
                    + approved + "，节点字段: " + node);
        }
    }

    private static void validatePartition(DataTarget target, ExportTable table, int idx) {
        String nodePartition = target.getPartition();
        if (nodePartition == null || nodePartition.trim().isEmpty()) {
            return; // 节点未指定分区，不强制校验
        }
        String approved = table.getPartition();
        if (approved == null || approved.trim().isEmpty()) {
            return; // DM单未限定分区，放行（③检测阶段以实际分区为准）
        }
        if (!normalize(approved).equals(normalize(nodePartition))) {
            mismatch("第" + idx + "个外发目标 " + target.tableKey() + " 分区不在DM单审批范围内，审批分区: "
                    + approved + "，节点分区: " + nodePartition);
        }
    }

    private static void validateNotifyUsers(NodeParams params, ExportForm form) {
        List<String> allowed = normalize(form.getNotifyUsers());
        if (allowed.isEmpty()) {
            // ①未返回审批允许通知人集合，子集校验交由③（403越权）兜底
            return;
        }
        List<String> node = normalize(params.getNotifyUsers());
        if (!allowed.containsAll(node)) {
            node.removeAll(allowed);
            mismatch("飞书通知人超出DM单授权范围，越权用户: " + node);
        }
    }

    private static List<String> normalize(Collection<String> values) {
        List<String> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.trim().isEmpty() && !result.contains(value.trim())) {
                    result.add(value.trim());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean equals(String left, String right) {
        return left != null && right != null && left.trim().equals(right.trim());
    }

    private static void mismatch(String message) {
        throw new DataGoFeishuException(82003, message);
    }
}
