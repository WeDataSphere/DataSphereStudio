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

import com.webank.wedatasphere.dss.contextservice.service.ContextService;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorCopyJobMapper;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorMapper;
import com.webank.wedatasphere.dss.orchestrator.db.hook.AddOrchestratorVersionHook;
import com.webank.wedatasphere.dss.orchestrator.loader.OrchestratorManager;
import com.webank.wedatasphere.dss.orchestrator.publish.ExportDSSOrchestratorPlugin;
import com.webank.wedatasphere.dss.orchestrator.publish.ImportDSSOrchestratorPlugin;
import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorFrameworkService;
import com.webank.wedatasphere.dss.workflow.dao.FlowMapper;
import com.webank.wedatasphere.dss.workflow.dao.LockMapper;
import com.webank.wedatasphere.dss.workflow.service.DSSFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorCopyEnv {

    @Autowired
    private OrchestratorCopyJobMapper orchestratorCopyJobMapper;

    @Autowired
    private OrchestratorMapper orchestratorMapper;

    @Autowired
    @Qualifier("orchestratorBmlService")
    private BMLService bmlService;

    @Autowired
    private OrchestratorFrameworkService orchestratorFrameworkService;

    @Autowired
    private ExportDSSOrchestratorPlugin exportDSSOrchestratorPlugin;

    @Autowired
    private ImportDSSOrchestratorPlugin importDSSOrchestratorPlugin;

    @Autowired
    private ContextService contextService;

    @Autowired
    AddOrchestratorVersionHook addOrchestratorVersionHook;

    @Autowired
    private DSSFlowService flowService;

    @Autowired
    private LockMapper lockMapper;

    public ContextService getContextService() {
        return contextService;
    }

    public void setContextService(ContextService contextService) {
        this.contextService = contextService;
    }

    public OrchestratorManager getOrchestratorManager() {
        return orchestratorManager;
    }

    public void setOrchestratorManager(OrchestratorManager orchestratorManager) {
        this.orchestratorManager = orchestratorManager;
    }

    @Autowired
    private OrchestratorManager orchestratorManager;

    public OrchestratorCopyJobMapper getOrchestratorCopyJobMapper() {
        return orchestratorCopyJobMapper;
    }

    public void setOrchestratorCopyJobMapper(OrchestratorCopyJobMapper orchestratorCopyJobMapper) {
        this.orchestratorCopyJobMapper = orchestratorCopyJobMapper;
    }

    public OrchestratorMapper getOrchestratorMapper() {
        return orchestratorMapper;
    }

    public void setOrchestratorMapper(OrchestratorMapper orchestratorMapper) {
        this.orchestratorMapper = orchestratorMapper;
    }

    public AddOrchestratorVersionHook getAddOrchestratorVersionHook() {
        return addOrchestratorVersionHook;
    }

    public void setAddOrchestratorVersionHook(AddOrchestratorVersionHook addOrchestratorVersionHook) {
        this.addOrchestratorVersionHook = addOrchestratorVersionHook;
    }

    public BMLService getBmlService() {
        return bmlService;
    }

    public void setBmlService(BMLService bmlService) {
        this.bmlService = bmlService;
    }


    public OrchestratorFrameworkService getOrchestratorFrameworkService() {
        return orchestratorFrameworkService;
    }

    public void setOrchestratorFrameworkService(OrchestratorFrameworkService orchestratorFrameworkService) {
        this.orchestratorFrameworkService = orchestratorFrameworkService;
    }

    public ExportDSSOrchestratorPlugin getExportDSSOrchestratorPlugin() {
        return exportDSSOrchestratorPlugin;
    }

    public void setExportDSSOrchestratorPlugin(ExportDSSOrchestratorPlugin exportDSSOrchestratorPlugin) {
        this.exportDSSOrchestratorPlugin = exportDSSOrchestratorPlugin;
    }

    public ImportDSSOrchestratorPlugin getImportDSSOrchestratorPlugin() {
        return importDSSOrchestratorPlugin;
    }

    public void setImportDSSOrchestratorPlugin(ImportDSSOrchestratorPlugin importDSSOrchestratorPlugin) {
        this.importDSSOrchestratorPlugin = importDSSOrchestratorPlugin;
    }

    public DSSFlowService getFlowService() {
        return flowService;
    }

    public void setFlowService(DSSFlowService flowService) {
        this.flowService = flowService;
    }

    public LockMapper getLockMapper() {
        return lockMapper;
    }

    public void setLockMapper(LockMapper lockMapper) {
        this.lockMapper = lockMapper;
    }
}
