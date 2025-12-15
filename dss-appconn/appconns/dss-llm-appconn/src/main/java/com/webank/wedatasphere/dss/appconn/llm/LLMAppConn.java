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

package com.webank.wedatasphere.dss.appconn.llm;

import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.appconn.core.impl.AbstractAppConn;
import com.webank.wedatasphere.dss.appconn.llm.standard.LLMDevelopmentStandard;
import com.webank.wedatasphere.dss.standard.app.development.service.DevelopmentService;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;

/**
 * LLMAppConn主入口类
 * 用于在DSS工作流中集成LLM大模型调用能力
 * 支持配置系统提示词、MCP工具及读取上游节点数据
 */
public class LLMAppConn extends AbstractAppConn implements OnlyDevelopmentAppConn {

    private LLMDevelopmentStandard standard;

    @Override
    protected void initialize() {
        // 初始化开发标准实例
        standard = new LLMDevelopmentStandard();
    }

    @Override
    public DevelopmentIntegrationStandard getOrCreateDevelopmentStandard() {
        return standard;
    }
}
