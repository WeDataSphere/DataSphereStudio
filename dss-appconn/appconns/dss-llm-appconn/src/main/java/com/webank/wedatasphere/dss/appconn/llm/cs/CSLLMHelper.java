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

package com.webank.wedatasphere.dss.appconn.llm.cs;

import org.apache.linkis.cs.client.service.CSVariableService;
import org.apache.linkis.cs.client.utils.ContextServiceUtils;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.object.LinkisVariable;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Context Service LLM辅助类
 * 负责将LLM响应结果保存到Context Service
 * 供下游节点通过${变量名}方式引用
 */
public class CSLLMHelper {

    private static final Logger logger = LoggerFactory.getLogger(CSLLMHelper.class);

    /**
     * 将LLM响应保存到Context Service
     *
     * @param properties  属性配置，包含contextID和nodeName
     * @param llmResponse LLM响应内容
     * @param saveKey     保存键名
     */
    public static void putVariable(Properties properties, String llmResponse, String saveKey) {
        try {
            // 1. 获取Context ID和Node Name
            String contextIDStr = ContextServiceUtils.getContextIDStrByProperties(properties);
            String nodeNameStr = ContextServiceUtils.getNodeNameStrByProperties(properties);

            logger.info("Saving LLM response to Context Service, contextID: {}, nodeName: {}, saveKey: {}",
                    contextIDStr, nodeNameStr, saveKey);

            // 2. 创建ContextKey
            ContextKey contextKey = new CommonContextKey();
            contextKey.setContextScope(ContextScope.PUBLIC);  // PUBLIC作用域，全局可访问
            contextKey.setContextType(ContextType.OBJECT);
            contextKey.setKey(CSCommonUtils.getVariableKey(nodeNameStr, saveKey));

            // 3. 创建LinkisVariable
            LinkisVariable varValue = new LinkisVariable();
            varValue.setKey(saveKey);
            varValue.setValue(llmResponse);

            // 4. 保存到Context Service
            CSVariableService.getInstance().putVariable(
                    contextIDStr,
                    SerializeHelper.serializeContextKey(contextKey),
                    varValue
            );

            logger.info("Successfully saved LLM response to Context Service with key: {}, value length: {} chars",
                    saveKey, llmResponse != null ? llmResponse.length() : 0);

        } catch (Exception e) {
            logger.error("Failed to put LLM variable to Context Service", e);
            throw new RuntimeException("保存LLM响应到Context Service失败: " + e.getMessage(), e);
        }
    }

}
