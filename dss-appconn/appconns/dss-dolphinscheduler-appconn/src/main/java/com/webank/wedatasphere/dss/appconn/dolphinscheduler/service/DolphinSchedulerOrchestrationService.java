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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.service;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.DolphinSchedulerAppConn;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation.DolphinSchedulerWorkflowCreationOperation;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation.DolphinSchedulerWorkflowDeletionOperation;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation.DolphinSchedulerWorkflowSearchOperation;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation.DolphinSchedulerWorkflowUpdateOperation;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.sso.DolphinSchedulerTokenManager;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.*;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 0.5.0
 */
public class DolphinSchedulerOrchestrationService extends OrchestrationService {

    @Override
    public void setAppInstance(AppInstance appInstance) {
        super.setAppInstance(appInstance);
        DolphinSchedulerTokenManager.getDolphinSchedulerTokenManager(appInstance.getBaseUrl())
                .setSSORequestOperation(getSSORequestService()
                        .createSSORequestOperation(DolphinSchedulerAppConn.DOLPHINSCHEDULER_APPCONN_NAME));
    }

    @Override
    protected OrchestrationCreationOperation createOrchestrationCreationOperation() {
        return new DolphinSchedulerWorkflowCreationOperation();
    }

    @Override
    protected OrchestrationUpdateOperation createOrchestrationUpdateOperation() {
        return new DolphinSchedulerWorkflowUpdateOperation();
    }

    @Override
    protected OrchestrationDeletionOperation createOrchestrationDeletionOperation() {
        return new DolphinSchedulerWorkflowDeletionOperation();
    }

    @Override
    protected OrchestrationSearchOperation createOrchestrationSearchOperation() {
        return new DolphinSchedulerWorkflowSearchOperation();
    }
}
