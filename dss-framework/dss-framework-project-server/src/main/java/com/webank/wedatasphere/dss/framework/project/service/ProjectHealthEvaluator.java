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

package com.webank.wedatasphere.dss.framework.project.service;

import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 项目健康标签计算器。
 *
 * <p>三类独立标签（可多命中）：
 * <ul>
 *     <li><b>EMPTY_PROJECT</b> —— workflowCount == 0（聚合成功时才判定，降级时不置 true 避免假阳性）</li>
 *     <li><b>STALE</b> —— latestWorkflowUpdateTime 距今超过阈值（默认 90 天，可配）</li>
 *     <li><b>NO_DESCRIPTION</b> —— description 为 null 或空白（不依赖聚合，始终判定）</li>
 * </ul>
 *
 * <p>降级时额外加 <b>UNAVAILABLE</b> 标签，前端显示"无法评估"。
 */
@Component
public class ProjectHealthEvaluator {

    /** 长期未更新阈值（天），可通过 dss.project.ledger.stale-threshold-days 配置 */
    @Value("${dss.project.ledger.stale-threshold-days:90}")
    private int staleThresholdDays;

    /** 空项目标签 */
    public static final String EMPTY_PROJECT = "EMPTY_PROJECT";
    /** 长期未更新标签 */
    public static final String STALE = "STALE";
    /** 描述缺失标签 */
    public static final String NO_DESCRIPTION = "NO_DESCRIPTION";
    /** 无法评估标签（降级时使用） */
    public static final String UNAVAILABLE = "UNAVAILABLE";

    /**
     * 计算健康标签（聚合成功时）。
     *
     * @param description           项目描述（可能为 null）
     * @param workflowCount         工作流数（null 表示聚合失败，不判定 EMPTY_PROJECT）
     * @param latestWorkflowUpdateTime 最近工作流更新时间（null 表示聚合失败或无工作流，不判定 STALE）
     * @return 健康标签列表
     */
    public List<String> evaluate(String description, Integer workflowCount, Date latestWorkflowUpdateTime) {
        List<String> labels = new ArrayList<>();
        // 空项目：仅在聚合成功（workflowCount 非 null）时判定，避免假阳性
        if (workflowCount != null && workflowCount == 0) {
            labels.add(EMPTY_PROJECT);
        }
        // 长期未更新：仅在有更新时间时判定
        if (latestWorkflowUpdateTime != null) {
            long daysDiff = (System.currentTimeMillis() - latestWorkflowUpdateTime.getTime()) / (1000L * 60 * 60 * 24);
            if (daysDiff > staleThresholdDays) {
                labels.add(STALE);
            }
        }
        // 描述缺失：不依赖聚合，始终判定
        if (description == null || description.trim().isEmpty()) {
            labels.add(NO_DESCRIPTION);
        }
        return labels;
    }

    /**
     * 降级时计算健康标签（不假阳性）。
     *
     * <p>空项目/长期未更新不评估（依赖聚合数据），仅判定描述缺失 + 无法评估标记。
     *
     * @param description 项目描述
     * @return 降级健康标签列表（含 UNAVAILABLE）
     */
    public List<String> evaluateOnDegraded(String description) {
        List<String> labels = new ArrayList<>();
        labels.add(UNAVAILABLE);
        if (description == null || description.trim().isEmpty()) {
            labels.add(NO_DESCRIPTION);
        }
        return labels;
    }

    /**
     * 判断指定项目是否命中任一选中健康标签（用于健康预过滤）。
     *
     * @param description           项目描述
     * @param workflowCount         工作流数
     * @param latestWorkflowUpdateTime 最近工作流更新时间
     * @param selectedLabels        选中标签集合
     * @return true 表示命中任一选中标签
     */
    public boolean matchAny(String description, Integer workflowCount, Date latestWorkflowUpdateTime,
                            java.util.Set<String> selectedLabels) {
        if (selectedLabels == null || selectedLabels.isEmpty()) {
            return true;
        }
        List<String> labels = evaluate(description, workflowCount, latestWorkflowUpdateTime);
        for (String label : labels) {
            if (selectedLabels.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
