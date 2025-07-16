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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler;

import com.webank.wedatasphere.dss.appconn.core.ext.OptionalAppConn;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.standard.DolphinSchedulerStructureStandard;
import com.webank.wedatasphere.dss.appconn.scheduler.AbstractSchedulerAppConn;
import com.webank.wedatasphere.dss.appconn.scheduler.SchedulerStructureIntegrationStandard;
import com.webank.wedatasphere.dss.orchestrator.converter.standard.ConversionIntegrationStandard;
import com.webank.wedatasphere.dss.standard.app.structure.OptionalIntegrationStandard;
import com.webank.wedatasphere.dss.workflow.conversion.WorkflowConversionIntegrationStandard;

public class DolphinSchedulerAppConn extends AbstractSchedulerAppConn implements OptionalAppConn {

    public static final String DOLPHINSCHEDULER_APPCONN_NAME = "dolphinscheduler";

    @Override
    public ConversionIntegrationStandard createConversionIntegrationStandard() {
        if (super.getOrCreateConversionStandard() == null) {
            return new WorkflowConversionIntegrationStandard();
        } else {
            return super.getOrCreateConversionStandard();
        }
    }

    @Override
    public SchedulerStructureIntegrationStandard getOrCreateStructureStandard() {
        return DolphinSchedulerStructureStandard.getInstance();
    }

    @Override
    public OptionalIntegrationStandard getOrCreateOptionalStandard() {
        return OptionalAppConn.super.getOrCreateOptionalStandard();
    }
}
