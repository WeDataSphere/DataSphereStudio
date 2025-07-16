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
package com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration;

import com.webank.wedatasphere.dss.standard.app.structure.AbstractStructureService;

/**
 * 统一编排规范，专门用于打通 DSS 与 SchedulerAppConn（调度系统）的编排体系。
 * 例如：打通 DSS 工作流 与 Schedulis 工作流。
 * 请注意，如果对接的 SchedulerAppConn 系统本身不支持管理工作流，则无需实现该接口。
 * @author enjoyyin
 * @date 2022-03-14
 * @since 1.1.0
 */
public abstract class OrchestrationService extends AbstractStructureService {

    public final OrchestrationCreationOperation getOrchestrationCreationOperation() {
        return getOrCreate(this::createOrchestrationCreationOperation, OrchestrationCreationOperation.class);
    }

    protected abstract OrchestrationCreationOperation createOrchestrationCreationOperation();

    public final OrchestrationUpdateOperation getOrchestrationUpdateOperation() {
        return getOrCreate(this::createOrchestrationUpdateOperation, OrchestrationUpdateOperation.class);
    }

    protected abstract OrchestrationUpdateOperation createOrchestrationUpdateOperation();

    public final OrchestrationDeletionOperation getOrchestrationDeletionOperation() {
        return getOrCreate(this::createOrchestrationDeletionOperation, OrchestrationDeletionOperation.class);
    }

    protected abstract OrchestrationDeletionOperation createOrchestrationDeletionOperation();


    public final OrchestrationSearchOperation getOrchestrationSearchOperation() {
        return getOrCreate(this::createOrchestrationSearchOperation, OrchestrationSearchOperation.class);
    }

    protected abstract OrchestrationSearchOperation createOrchestrationSearchOperation();

}
