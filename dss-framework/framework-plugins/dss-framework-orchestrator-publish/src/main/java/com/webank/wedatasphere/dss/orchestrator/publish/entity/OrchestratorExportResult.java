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
package com.webank.wedatasphere.dss.orchestrator.publish.entity;

import com.webank.wedatasphere.dss.common.entity.BmlResource;

/**
 * 工作流导出结果
 * Author: xlinliu
 * Date: 2022/8/22
 */
public class OrchestratorExportResult {
    /**
     * 导出工作流的bml文件
     */
    private BmlResource bmlResource;
    /**
     * 工作流的版本
     */
    private String orcVersionId;

    public OrchestratorExportResult() {
    }

    public OrchestratorExportResult(BmlResource bmlResource, String orcVersionId) {
        this.bmlResource = bmlResource;
        this.orcVersionId = orcVersionId;
    }

    public BmlResource getBmlResource() {
        return bmlResource;
    }

    public void setBmlResource(BmlResource bmlResource) {
        this.bmlResource = bmlResource;
    }

    public String getOrcVersionId() {
        return orcVersionId;
    }

    public void setOrcVersionId(String orcVersionId) {
        this.orcVersionId = orcVersionId;
    }
}
