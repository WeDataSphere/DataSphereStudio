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

package com.webank.wedatasphere.dss.appconn.llm.execution;

import com.webank.wedatasphere.dss.appconn.llm.cs.CSLLMHelper;
import com.webank.wedatasphere.dss.appconn.llm.entity.LLMResponse;
import com.webank.wedatasphere.dss.appconn.llm.service.LLMClient;
import com.webank.wedatasphere.dss.appconn.llm.utils.LLMUtils;
import com.webank.wedatasphere.dss.standard.app.development.listener.common.RefExecutionAction;
import com.webank.wedatasphere.dss.standard.app.development.listener.common.RefExecutionState;
import com.webank.wedatasphere.dss.standard.app.development.listener.core.Killable;
import com.webank.wedatasphere.dss.standard.app.development.listener.core.LongTermRefExecutionOperation;
import com.webank.wedatasphere.dss.standard.app.development.listener.core.Procedure;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.RefExecutionRequestRef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.common.utils.VariableUtils;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * LLM执行操作类
 * 负责处理LLM节点的执行逻辑：
 * 1. 读取上游节点数据
 * 2. 组装prompt并调用LLM API
 * 3. 保存结果到Context Service
 */
public class LLMRefExecutionOperation
        extends LongTermRefExecutionOperation<RefExecutionRequestRef.RefExecutionContextRequestRef>
        implements Killable, Procedure {

    private static final Logger logger = LoggerFactory.getLogger(LLMRefExecutionOperation.class);

    /**
     * 提交执行任务
     */
    @Override
    public RefExecutionAction submit(RefExecutionRequestRef.RefExecutionContextRequestRef requestRef) {
        LLMExecutionAction action = new LLMExecutionAction();
        action.setId(UUID.randomUUID().toString());

        try {
            // 1. 获取配置参数
            Map<String, Object> instanceConfig = service.getAppInstance().getConfig();
            Map<String, Object> runtimeMap = requestRef.getExecutionRequestRefContext().getRuntimeMap();

            // 2. 合并配置
            Properties properties = new Properties();
            properties.put(VariableUtils.RUN_DATE(), requestRef.getRunDate());

            // 添加实例配置
            for (Map.Entry<String, Object> entry : instanceConfig.entrySet()) {
                if (entry.getValue() != null) {
                    properties.put(entry.getKey(), entry.getValue().toString());
                }
            }

            // 添加运行时参数
            for (Map.Entry<String, Object> entry : runtimeMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    properties.put(entry.getKey(), entry.getValue().toString());
                }
            }

            action.setProperties(properties);

            // 3. 读取参数
            String systemPrompt = properties.getProperty("llm.system.prompt", "");
            String userPrompt = properties.getProperty("llm.user.prompt", "");
            String model = properties.getProperty("llm.model", "gpt-3.5-turbo");
            double temperature = Double.parseDouble(properties.getProperty("llm.temperature", "0.7"));
            int maxTokens = Integer.parseInt(properties.getProperty("llm.max.tokens", "2000"));
            String saveKey = properties.getProperty("llm.save.key", "llm.response");
            String mcpToolsJson = properties.getProperty("llm.mcp.tools", "");

            action.setSaveKey(saveKey);

            // 4. 读取上游节点数据
            String upstreamData = LLMUtils.readUpstreamData(requestRef.getExecutionRequestRefContext());
            logger.info("Read upstream data: {} characters", upstreamData.length());

            // 5. 组装完整的用户prompt（用户提示词 + 上游数据）
            StringBuilder fullUserPrompt = new StringBuilder();

            if (StringUtils.isNotEmpty(userPrompt)) {
                fullUserPrompt.append(userPrompt).append("\n\n");
            }

            if (StringUtils.isNotEmpty(upstreamData)) {
                fullUserPrompt.append("## 上游节点数据：\n");
                fullUserPrompt.append(upstreamData);
            }

            logger.info("Full user prompt length: {}", fullUserPrompt.length());

            // 6. 调用LLM API
            LLMClient llmClient = new LLMClient(properties);
            LLMResponse response = llmClient.chat(
                    systemPrompt,
                    fullUserPrompt.toString(),
                    model,
                    temperature,
                    maxTokens,
                    mcpToolsJson
            );

            // 7. 保存响应结果
            action.setLlmResponse(response.getContent());
            action.setTotalTokens(response.getUsage().getTotalTokens());
            action.setPromptTokens(response.getUsage().getPromptTokens());
            action.setCompletionTokens(response.getUsage().getCompletionTokens());

            logger.info("LLM response received, tokens: {}", action.getTotalTokens());

            // 8. 保存到Context Service供下游节点使用
            CSLLMHelper.putVariable(properties, response.getContent(), saveKey);

            action.setState(RefExecutionState.Success);
        } catch (Exception t) {
            logger.error("LLM execution failed", t);
            action.setState(RefExecutionState.Failed);
            putErrorMsg("LLM执行失败：" + t.getMessage(), t, action);
        }

        return action;
    }

    /**
     * 查询执行状态
     */
    @Override
    public RefExecutionState state(RefExecutionAction action) {
        if (action instanceof LLMExecutionAction) {
            return ((LLMExecutionAction) action).getState();
        }
        return RefExecutionState.Failed;
    }

    /**
     * 获取执行结果
     */
    @Override
    public ExecutionResponseRef result(RefExecutionAction action) {
        if (!(action instanceof LLMExecutionAction)) {
            return new ExecutionResponseRef.ExecutionResponseRefBuilder().error();
        }

        LLMExecutionAction llmAction = (LLMExecutionAction) action;

        if (llmAction.getState().equals(RefExecutionState.Success)) {
            // 创建结果集写入器
            ResultSetWriter resultSetWriter = null;
            try {
                resultSetWriter = llmAction.getExecutionRequestRefContext().createTextResultSetWriter();
                resultSetWriter.addMetaData(null);

                // 写入LLM响应内容
                resultSetWriter.addRecord(new LineRecord(llmAction.getLlmResponse()));

                // 写入Token使用统计
                String tokenInfo = "\n\n--- Token使用统计 ---\n" +
                        "提示词Token: " + llmAction.getPromptTokens() + "\n" +
                        "完成Token: " + llmAction.getCompletionTokens() + "\n" +
                        "总Token: " + llmAction.getTotalTokens();
                resultSetWriter.addRecord(new LineRecord(tokenInfo));

                return new ExecutionResponseRef.ExecutionResponseRefBuilder().success();
            } catch (Exception e) {
                logger.error("Failed to write result", e);
                return new ExecutionResponseRef.ExecutionResponseRefBuilder().error();
            } finally {
                IOUtils.closeQuietly(resultSetWriter);
            }
        } else if (llmAction.getExecutionResponseRef() != null) {
            return llmAction.getExecutionResponseRef();
        } else {
            return new ExecutionResponseRef.ExecutionResponseRefBuilder().error();
        }
    }

    /**
     * 获取执行进度
     */
    @Override
    public float progress(RefExecutionAction action) {
        if (!(action instanceof LLMExecutionAction)) {
            RefExecutionState state = ((LLMExecutionAction) action).getState();
            if (state == RefExecutionState.Running) {
                return 0.5f;
            } else if (state == RefExecutionState.Success) {
                return 1.0f;
            } else {
                return 0.0f;
            }
        }else {
            logger.error("Cannot get progress for unknown action type");
            return 1.0f;
        }

    }

    /**
     * 获取执行日志
     */
    @Override
    public String log(RefExecutionAction action) {
        if (!(action instanceof LLMExecutionAction)) {
            return "未知的执行动作类型";
        }

        LLMExecutionAction llmAction = (LLMExecutionAction) action;

        if (!llmAction.getState().isCompleted()) {
            return "LLM正在处理请求...";
        } else if (llmAction.getState().equals(RefExecutionState.Success)) {
            return "LLM执行成功，使用了 " + llmAction.getTotalTokens() + " 个Token";
        } else {
            return "LLM执行失败";
        }
    }

    /**
     * 终止执行
     */
    @Override
    public boolean kill(RefExecutionAction action) {
        if (!(action instanceof LLMExecutionAction)) {
            logger.error("Cannot kill unknown action type");
            return false;
        }

        LLMExecutionAction llmAction = (LLMExecutionAction) action;
        llmAction.setState(RefExecutionState.Killed);
        logger.info("LLM execution killed: {}", llmAction.getId());
        return true;
    }

    /**
     * 构建错误响应
     */
    protected LLMExecutionAction putErrorMsg(String errorMsg, Throwable t, LLMExecutionAction action) {
        ExecutionResponseRef responseRef = new ExecutionResponseRef.ExecutionResponseRefBuilder()
                .setErrorMsg(errorMsg)
                .setException(t)
                .error();
        action.setExecutionResponseRef(responseRef);
        return action;
    }
}
