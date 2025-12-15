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

package com.webank.wedatasphere.dss.appconn.llm.standard;

import com.webank.wedatasphere.dss.appconn.llm.service.LLMExecuteService;
import com.webank.wedatasphere.dss.standard.app.development.standard.OnlyExecutionDevelopmentStandard;
import com.webank.wedatasphere.dss.standard.app.development.service.RefExecutionService;

/**
 * LLM开发标准类
 * 继承OnlyExecutionDevelopmentStandard，表示该AppConn仅提供执行能力
 * 不涉及资源的增删改查、导入导出等操作
 */
public class LLMDevelopmentStandard extends OnlyExecutionDevelopmentStandard {

    @Override
    protected RefExecutionService createRefExecutionService() {
        // 创建并返回LLM执行服务实例
        return new LLMExecuteService();
    }

    @Override
    public void init() {
        // 初始化逻辑，当前无需特殊初始化
    }

    @Override
    public String getStandardName() {
        return "LLMDevelopmentStandard";
    }
}
