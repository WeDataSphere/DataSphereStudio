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
package com.webank.wedatasphere.dss.framework.project.entity.response;

import com.webank.wedatasphere.dss.common.entity.BmlResource;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2022/9/9
 */
public class BatchExportResult {
    /**
     * 导出文件上传后的bml资源
     */
    private BmlResource bmlResource;

    /**
     * 导出文件的md5校验码
     */
    private String checkSum;

    public BatchExportResult(BmlResource bmlResource, String checkSum) {
        this.bmlResource = bmlResource;
        this.checkSum = checkSum;
    }

    public BmlResource getBmlResource() {
        return bmlResource;
    }

    public void setBmlResource(BmlResource bmlResource) {
        this.bmlResource = bmlResource;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
