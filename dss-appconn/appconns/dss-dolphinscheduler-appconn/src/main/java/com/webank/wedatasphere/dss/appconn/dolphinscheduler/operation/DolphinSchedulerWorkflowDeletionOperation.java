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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.DolphinSchedulerAppConn;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils.DolphinSchedulerHttpUtils;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils.ProjectUtils;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.OrchestrationDeletionOperation;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.common.utils.MapUtils;
import com.webank.wedatasphere.dss.standard.app.structure.AbstractStructureOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import org.apache.commons.lang3.StringUtils;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 0.5.0
 */
public class DolphinSchedulerWorkflowDeletionOperation
    extends AbstractStructureOperation<RefOrchestrationContentRequestRef.RefOrchestrationContentRequestRefImpl, ResponseRef>
    implements OrchestrationDeletionOperation<RefOrchestrationContentRequestRef.RefOrchestrationContentRequestRefImpl> {

    private String deleteProcessDefinitionByIdUrl;

    @Override
    public void init() {
        super.init();
        String baseUrl = DolphinSchedulerHttpUtils.getDolphinSchedulerBaseUrl(getBaseUrl());
        this.deleteProcessDefinitionByIdUrl = mergeUrl(baseUrl, "projects/${projectName}/process/delete");
    }

    @Override
    public ResponseRef deleteOrchestration(RefOrchestrationContentRequestRef.RefOrchestrationContentRequestRefImpl requestRef) {
        String dolphinProjectName =
                ProjectUtils.generateDolphinProjectName(requestRef.getWorkspace().getWorkspaceName(), requestRef.getProjectName());
        String deleteUrl =
                StringUtils.replace(this.deleteProcessDefinitionByIdUrl, "${projectName}", dolphinProjectName);
        logger.info("begin to create workflow in DolphinScheduler, project name is {}, workflow name is {}, creator is {}.",
                dolphinProjectName, requestRef.getOrchestrationName(), requestRef.getUserName());
        DolphinSchedulerHttpUtils.getHttpGetResult(ssoRequestOperation, deleteUrl, requestRef.getUserName(), MapUtils.newCommonMap("processDefinitionId", requestRef.getRefOrchestrationId()));
        return ResponseRef.newExternalBuilder().success();
    }

    @Override
    protected String getAppConnName() {
        return DolphinSchedulerAppConn.DOLPHINSCHEDULER_APPCONN_NAME;
    }
}
