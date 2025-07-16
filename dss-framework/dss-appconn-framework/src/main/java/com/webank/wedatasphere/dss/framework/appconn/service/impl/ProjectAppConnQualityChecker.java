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
package com.webank.wedatasphere.dss.framework.appconn.service.impl;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyStructureAppConn;
import com.webank.wedatasphere.dss.framework.appconn.conf.AppConnConf;
import com.webank.wedatasphere.dss.framework.appconn.exception.AppConnQualityErrorException;
import com.webank.wedatasphere.dss.standard.app.structure.StructureIntegrationStandard;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectService;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.springframework.stereotype.Component;

/**
 * @author enjoyyin
 * @date 2022-04-14
 * @since 1.1.0
 */
@Component
public class ProjectAppConnQualityChecker extends AbstractAppConnQualityChecker {

    public ProjectAppConnQualityChecker() {
        super(AppConnConf.PROJECT_QUALITY_CHECKER_IGNORE_LIST.getValue());
    }

    @Override
    protected void checkAppConnQuality(AppConn appConn) throws AppConnQualityErrorException {
        if(!(appConn instanceof OnlyStructureAppConn)) {
            return;
        }
        String appConnName = appConn.getAppDesc().getAppName();
        checkAppInstance(appConn);
        AppInstance appInstance = appConn.getAppDesc().getAppInstances().get(0);
        OnlyStructureAppConn onlyStructureAppConn = (OnlyStructureAppConn) appConn;
        StructureIntegrationStandard structureIntegrationStandard = onlyStructureAppConn.getOrCreateStructureStandard();
        checkNull(structureIntegrationStandard, appConnName, "structureStandard");
        ProjectService projectService = structureIntegrationStandard.getProjectService(appInstance);
        checkNull(projectService, appConnName, "projectService");
        checkBoolean(projectService.isProjectNameUnique() && projectService.getProjectSearchOperation() == null,
                appConnName, "isProjectNameUnique is true but projectSearchOperation is not exists.");
        checkNull(projectService.getProjectCreationOperation(), appConnName, "projectCreationOperation");
        checkNull(projectService.getProjectUpdateOperation(), appConnName, "projectUpdateOperation");
        checkNull(projectService.getProjectDeletionOperation(), appConnName, "projectDeletionOperation");
    }


}
