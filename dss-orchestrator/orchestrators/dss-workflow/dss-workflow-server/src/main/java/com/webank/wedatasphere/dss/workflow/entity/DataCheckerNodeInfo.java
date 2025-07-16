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
package com.webank.wedatasphere.dss.workflow.entity;

import java.util.Date;
import java.util.Map;

public class DataCheckerNodeInfo extends  NodeBaseInfo {



    // 节点描述
    private String nodeDesc;

    // source.type
    private String sourceType;

    // check.object
    private String checkObject;

    // max.check.hours
    private String maxCheckHours;

    //job.desc
    private String jobDesc;

    // qualities 校验
    private Boolean qualitisCheck;

    public String getNodeDesc() {
        return nodeDesc;
    }

    public void setNodeDesc(String nodeDesc) {
        this.nodeDesc = nodeDesc;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCheckObject() {
        return checkObject;
    }

    public void setCheckObject(String checkObject) {
        this.checkObject = checkObject;
    }

    public String getMaxCheckHours() {
        return maxCheckHours;
    }

    public void setMaxCheckHours(String maxCheckHours) {
        this.maxCheckHours = maxCheckHours;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public Boolean getQualitisCheck() {
        return qualitisCheck;
    }

    public void setQualitisCheck(Boolean qualitisCheck) {
        this.qualitisCheck = qualitisCheck;
    }
}
