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
package com.webank.wedatasphere.dss.migrate.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.framework.project.entity.vo.DSSProjectVo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;

public interface MigrateService {


    void migrate(String userName, String inputZipPath, Workspace workspace) throws Exception;

    long importOrcToOrchestrator(String resourceId, String version, DSSProjectVo project,
                                        String username, String label, Workspace workspace, DSSOrchestratorInfo dssOrchestratorInfo);

    DSSOrchestratorInfo buildOrchestratorInfo(DSSFlow dssFlow, DSSProjectVo dssProject, Long workspaceId) throws DSSErrorException;

    String queryOrcUUIDByName(Long workspaceId,Long projectId,String orcName) throws DSSErrorException;
}
