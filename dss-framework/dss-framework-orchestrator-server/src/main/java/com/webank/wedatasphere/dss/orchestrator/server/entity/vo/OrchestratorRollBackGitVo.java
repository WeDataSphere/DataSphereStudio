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
package com.webank.wedatasphere.dss.orchestrator.server.entity.vo;

import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;

public class OrchestratorRollBackGitVo {
    private DSSOrchestratorVersion oldOrcVersion;
    private DSSOrchestratorVersion dssOrchestratorVersion;
    private DSSOrchestratorInfo dssOrchestratorInfo;
    private String version;

    public DSSOrchestratorVersion getOldOrcVersion() {
        return oldOrcVersion;
    }

    public void setOldOrcVersion(DSSOrchestratorVersion oldOrcVersion) {
        this.oldOrcVersion = oldOrcVersion;
    }

    public DSSOrchestratorVersion getDssOrchestratorVersion() {
        return dssOrchestratorVersion;
    }

    public void setDssOrchestratorVersion(DSSOrchestratorVersion dssOrchestratorVersion) {
        this.dssOrchestratorVersion = dssOrchestratorVersion;
    }

    public DSSOrchestratorInfo getDssOrchestratorInfo() {
        return dssOrchestratorInfo;
    }

    public void setDssOrchestratorInfo(DSSOrchestratorInfo dssOrchestratorInfo) {
        this.dssOrchestratorInfo = dssOrchestratorInfo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
