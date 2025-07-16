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
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.framework.appconn.conf.AppConnConf;
import com.webank.wedatasphere.dss.framework.appconn.exception.AppConnQualityErrorException;
import com.webank.wedatasphere.dss.standard.app.development.service.RefCRUDService;
import com.webank.wedatasphere.dss.standard.app.development.service.RefExecutionService;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.springframework.stereotype.Component;

/**
 * @author enjoyyin
 * @date 2022-04-14
 * @since 1.1.0
 */
@Component
public class DevelopmentAppConnQualityChecker extends AbstractAppConnQualityChecker {

    public DevelopmentAppConnQualityChecker() {
        super(AppConnConf.DEVELOPMENT_QUALITY_CHECKER_IGNORE_LIST.getValue());
    }

    @Override
    protected void checkAppConnQuality(AppConn appConn) throws AppConnQualityErrorException {
        if(!(appConn instanceof OnlyDevelopmentAppConn)) {
            return;
        }
        String appConnName = appConn.getAppDesc().getAppName();
        checkAppInstance(appConn);
        AppInstance appInstance = appConn.getAppDesc().getAppInstances().get(0);
        DevelopmentIntegrationStandard developmentIntegrationStandard = ((OnlyDevelopmentAppConn) appConn).getOrCreateDevelopmentStandard();
        checkNull(developmentIntegrationStandard, appConnName, "developmentStandard");
        RefExecutionService refExecutionService = developmentIntegrationStandard.getRefExecutionService(appInstance);
        checkNull(refExecutionService, appConnName, "refExecutionService");
        checkNull(refExecutionService.getRefExecutionOperation(), appConnName, "refExecutionOperation");
        checkBoolean(developmentIntegrationStandard.getRefCRUDService(appInstance) != null &&
                developmentIntegrationStandard.getRefImportService(appInstance) == null, appConnName,
                "RefImportService is needed since refCRUDService is exists.");
        checkBoolean(developmentIntegrationStandard.getRefCRUDService(appInstance) != null &&
                        developmentIntegrationStandard.getRefExportService(appInstance) == null, appConnName,
                "RefExportService is needed since refCRUDService is exists.");
        RefCRUDService refCRUDService = developmentIntegrationStandard.getRefCRUDService(appInstance);
        if(refCRUDService == null) {
            return;
        }
        checkNull(refCRUDService.getRefUpdateOperation(), appConnName, "refUpdateOperation");
        checkNull(refCRUDService.getRefCopyOperation(), appConnName, "refCopyOperation");
        checkNull(refCRUDService.getRefDeletionOperation(), appConnName, "refDeletionOperation");
        checkNull(developmentIntegrationStandard.getRefImportService(appInstance).getRefImportOperation(), appConnName, "refImportOperation");
        checkNull(developmentIntegrationStandard.getRefExportService(appInstance).getRefExportOperation(), appConnName, "refExportOperation");
    }

}
