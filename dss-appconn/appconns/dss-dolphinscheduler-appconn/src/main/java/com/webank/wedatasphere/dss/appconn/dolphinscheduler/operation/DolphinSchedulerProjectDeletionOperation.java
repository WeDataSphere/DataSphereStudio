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
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.conf.DolphinSchedulerConf;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils.DolphinSchedulerHttpUtils;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils.ProjectUtils;
import com.webank.wedatasphere.dss.common.utils.MapUtils;
import com.webank.wedatasphere.dss.standard.app.structure.AbstractStructureOperation;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectDeletionOperation;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.ProjectResponseRef;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.RefProjectContentRequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;

public class DolphinSchedulerProjectDeletionOperation
        extends AbstractStructureOperation<RefProjectContentRequestRef.RefProjectContentRequestRefImpl, ResponseRef>
        implements ProjectDeletionOperation<RefProjectContentRequestRef.RefProjectContentRequestRefImpl> {

    private String deleteProjectByIdUrl;

    public DolphinSchedulerProjectDeletionOperation() {}

    @Override
    protected String getAppConnName() {
        return DolphinSchedulerAppConn.DOLPHINSCHEDULER_APPCONN_NAME;
    }

    @Override
    public void init() {
        super.init();
        String baseUrl = DolphinSchedulerHttpUtils.getDolphinSchedulerBaseUrl(getBaseUrl());
        this.deleteProjectByIdUrl = mergeUrl(baseUrl, "projects/delete");
    }

    @Override
    public ResponseRef deleteProject(RefProjectContentRequestRef.RefProjectContentRequestRefImpl projectRef) throws ExternalOperationFailedException {
        // Dolphin Scheduler项目名
        String dsProjectName =
                ProjectUtils.generateDolphinProjectName(projectRef.getWorkspace().getWorkspaceName(), projectRef.getProjectName());
        logger.info("User {} begin to delete project in DolphinScheduler, project name is {}.", projectRef.getUserName(), dsProjectName);
        DolphinSchedulerHttpUtils.getHttpGetResult(ssoRequestOperation, this.deleteProjectByIdUrl, DolphinSchedulerConf.DS_ADMIN_USER.getValue(),
                MapUtils.newCommonMap("projectId", projectRef.getRefProjectId()));
        return ProjectResponseRef.newExternalBuilder().success();
    }
}
