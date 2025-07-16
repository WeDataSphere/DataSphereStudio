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
package com.webank.wedatasphere.dss.orchestrator.server.job;

import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Description 定时清理ContextId的任务
 */
@Component
@EnableScheduling
public class BatchClearCsTask {

    @Autowired
    private OrchestratorService  orchestratorService;

    @Scheduled(cron = "#{@getBatchClearCsTaskCron}")
    public void batchClearCsTask(){
        orchestratorService.batchClearContextId();

    }
}
