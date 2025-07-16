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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.standard;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.service.DolphinSchedulerOrchestrationService;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.service.DolphinSchedulerProjectService;
import com.webank.wedatasphere.dss.appconn.scheduler.AbstractSchedulerStructureIntegrationStandard;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.OrchestrationService;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectService;

public class DolphinSchedulerStructureStandard extends AbstractSchedulerStructureIntegrationStandard {

    private static volatile DolphinSchedulerStructureStandard instance;


    private DolphinSchedulerStructureStandard() {
    }

    public static DolphinSchedulerStructureStandard getInstance() {
        if (instance == null) {
            synchronized (DolphinSchedulerStructureStandard.class) {
                if (instance == null) {
                    instance = new DolphinSchedulerStructureStandard();
                }
            }
        }
        return instance;
    }

    @Override
    protected ProjectService createProjectService() {
        return new DolphinSchedulerProjectService();
    }

    @Override
    protected OrchestrationService createOrchestrationService() {
        return new DolphinSchedulerOrchestrationService();
    }
}
