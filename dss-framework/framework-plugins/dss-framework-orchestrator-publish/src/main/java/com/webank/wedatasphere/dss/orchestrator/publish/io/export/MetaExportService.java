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

package com.webank.wedatasphere.dss.orchestrator.publish.io.export;


import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlowRelation;

import java.io.IOException;
import java.util.List;


public interface MetaExportService {


    /**
     *
     * @param dssOrchestratorInfo
     * @param savePath
     * @throws IOException
     */
    void export(DSSOrchestratorInfo dssOrchestratorInfo, String savePath) throws IOException;
    void exportNew(DSSOrchestratorInfo dssOrchestratorInfo, String flowMetaPath) throws IOException;

    void exportFlowBaseInfo(List<DSSFlow> allDSSFlows, List<DSSFlowRelation> allFlowRelations, String savePath) throws IOException;



}
