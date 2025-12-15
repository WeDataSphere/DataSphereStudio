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

package com.webank.wedatasphere.dss.appconn.llm.utils;

import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.standard.app.development.listener.core.ExecutionRequestRefContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.cs.client.service.LinkisJobDataServiceImpl;
import org.apache.linkis.cs.client.utils.ContextServiceUtils;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.storage.LineRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * LLM工具类
 * 提供辅助功能，如读取上游节点数据等
 */
public class LLMUtils {

    private static final Logger logger = LoggerFactory.getLogger(LLMUtils.class);

    /**
     * 读取上游所有节点的结果数据
     * 将结果格式化为文本，供LLM处理
     *
     * @param context 执行请求上下文
     * @return 上游节点数据的文本表示
     */
    public static String readUpstreamData(ExecutionRequestRefContext context) {
//        StringBuilder dataBuilder = new StringBuilder();
//
//        try {
//            // 1. 获取上游节点ID列表
//            Long[] jobIds = getUpstreamJobIds(context);
//            if (jobIds == null || jobIds.length == 0) {
//                logger.info("No upstream nodes found");
//                return "";
//            }
//
//            logger.info("Found {} upstream nodes", jobIds.length);
//
//            // 2. 遍历每个JobID，读取其结果集
//            for (int i = 0; i < jobIds.length; i++) {
//                Long jobId = jobIds[i];
//                try {
//                    String nodeData = readJobResultSet(context, jobId, i + 1);
//                    if (StringUtils.isNotBlank(nodeData)) {
//                        dataBuilder.append(nodeData).append("\n\n");
//                    }
//                } catch (Exception e) {
//                    logger.warn("Failed to read data from job {}: {}", jobId, e.getMessage());
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error("Failed to read upstream data", e);
//        }
//
//        return dataBuilder.toString();
        return "";
    }

    /**
     * 获取上游节点的JobID列表
     */
    private static Long[] getUpstreamJobIds(ExecutionRequestRefContext context) {
        try {
            // 获取contextID
            Map<String, Object> runtimeMap = context.getRuntimeMap();
            String contextIDStr = ContextServiceUtils.getContextIDStrByMap(runtimeMap);

            // 获取上游节点ID列表（从content参数）
            Object contentObj = runtimeMap.get("content");
            List<String> nodeIds;

            if (contentObj instanceof String) {
                nodeIds = DSSCommonUtils.COMMON_GSON.fromJson((String) contentObj, List.class);
            } else if (contentObj instanceof List) {
                nodeIds = (List<String>) contentObj;
            } else {
                logger.warn("No content parameter found in runtime map");
                return new Long[0];
            }

            if (nodeIds == null || nodeIds.isEmpty()) {
                return new Long[0];
            }

            logger.info("Processing {} upstream nodes", nodeIds.size());

            // 转换nodeId为JobID
            Long[] jobIds = new Long[nodeIds.size()];
            for (int i = 0; i < nodeIds.size(); i++) {
                String nodeId = nodeIds.get(i);
                try {
                    // 通过nodeId获取nodeName
                    String nodeName = ContextServiceUtils.getNodeNameByNodeID(contextIDStr, nodeId);
                    if (nodeName == null) {
                        logger.warn("Node name is null for nodeId: {}", nodeId);
                        continue;
                    }

                    // 通过nodeName获取JobID
                    CommonContextKey contextKey = new CommonContextKey();
                    contextKey.setContextScope(ContextScope.PUBLIC);
                    contextKey.setContextType(ContextType.DATA);
                    contextKey.setKey(CSCommonUtils.NODE_PREFIX + nodeName + CSCommonUtils.JOB_ID);

                    jobIds[i] = LinkisJobDataServiceImpl.getInstance()
                            .getLinkisJobData(contextIDStr, SerializeHelper.serializeContextKey(contextKey))
                            .getJobID();

                    logger.info("Node {} -> JobID {}", nodeName, jobIds[i]);
                } catch (Exception e) {
                    logger.warn("Failed to get JobID for node {}: {}", nodeId, e.getMessage());
                }
            }

            return jobIds;
        } catch (Exception e) {
            logger.error("Failed to get upstream job IDs", e);
            return new Long[0];
        }
    }



    /**
     * 格式化文本类型的ResultSet
     */
    private static String formatTextResultSet(ResultSetReader<? extends MetaData, ? extends Record> reader) {
        StringBuilder content = new StringBuilder();

        try {
            content.append("```\n");

            int lineCount = 0;
            int maxLines = 500;  // 限制最多500行

            while (reader.hasNext() && lineCount < maxLines) {
                Record record = reader.getRecord();
                if (record instanceof LineRecord) {
                    LineRecord lineRecord = (LineRecord) record;
                    content.append(lineRecord.getLine()).append("\n");
                    lineCount++;
                }
            }

            if (reader.hasNext()) {
                content.append("... (还有更多内容，已省略)\n");
            }

            content.append("```\n");

        } catch (Exception e) {
            logger.warn("Failed to format text result set", e);
        }

        return content.toString();
    }

    /**
     * 验证参数是否为空
     *
     * @param paramName  参数名
     * @param paramValue 参数值
     * @throws IllegalArgumentException 如果参数为空
     */
    public static void validateNotEmpty(String paramName, String paramValue) {
        if (StringUtils.isBlank(paramValue)) {
            throw new IllegalArgumentException("参数 " + paramName + " 不能为空");
        }
    }

    /**
     * 安全地获取属性值
     *
     * @param properties   属性对象（可以是Properties或Map）
     * @param key          键名
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    public static String getPropertySafely(Object properties, String key, String defaultValue) {
        try {
            if (properties instanceof java.util.Properties) {
                return ((java.util.Properties) properties).getProperty(key, defaultValue);
            } else if (properties instanceof java.util.Map) {
                Object value = ((java.util.Map<?, ?>) properties).get(key);
                return value != null ? value.toString() : defaultValue;
            }
        } catch (Exception e) {
            logger.warn("Failed to get property {}: {}", key, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * 截断过长的文本
     *
     * @param text      原文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 格式化Token数量
     *
     * @param tokens Token数量
     * @return 格式化的字符串
     */
    public static String formatTokens(Integer tokens) {
        if (tokens == null) {
            return "N/A";
        }
        if (tokens >= 1000) {
            return String.format("%.1fK", tokens / 1000.0);
        }
        return tokens.toString();
    }
}
