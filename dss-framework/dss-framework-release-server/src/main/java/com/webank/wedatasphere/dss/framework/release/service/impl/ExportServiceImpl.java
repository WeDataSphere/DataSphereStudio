/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.framework.release.service.impl;

import com.google.gson.Gson;
import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.utils.*;
import com.webank.wedatasphere.dss.framework.common.exception.DSSFrameworkErrorException;
import com.webank.wedatasphere.dss.framework.release.entity.export.BatchExportResult;
import com.webank.wedatasphere.dss.framework.release.entity.export.ExportResult;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.framework.release.service.ExportService;
import com.webank.wedatasphere.dss.framework.release.utils.ReleaseConf;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAddVersionAfterPublish;
import com.webank.wedatasphere.dss.orchestrator.common.ref.InternalOrchestratorRefConstant;
import com.webank.wedatasphere.dss.orchestrator.common.ref.OrchestratorRefConstant;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.development.operation.RefExportOperation;
import com.webank.wedatasphere.dss.standard.app.development.ref.ExportResponseRef;
import com.webank.wedatasphere.dss.standard.app.development.ref.ImportRequestRef;
import com.webank.wedatasphere.dss.standard.app.development.ref.impl.ThirdlyRequestRef;
import com.webank.wedatasphere.dss.standard.app.development.service.RefExportService;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.apache.linkis.common.exception.ErrorException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Resource;

/**
 * created by cooperyang on 2020/12/9
 * Description:
 */
@Service
public class ExportServiceImpl implements ExportService {
    private final String metaFileName = "meta.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportServiceImpl.class);

    private static final String ORCHESTRATOR_FRAMEWORK_NAME = ReleaseConf.ORCHESTRATOR_APPCONN_NAME.getValue();
    @Autowired
    @Qualifier("projectBmlService")
    private BMLService bmlService;


    @Override
    @SuppressWarnings("ConstantConditions")
    public ExportResult export(String releaseUser, Long projectId,OrchestratorInfo orchestrator,
                               String projectName, DSSLabel dssLabel, Workspace workspace) throws ErrorException {
        LOGGER.info("begin to export projectId {} orchestratorId {} orchestratorVersionId {} for user {}", projectId,
                orchestrator.getOrchestratorId(), orchestrator.getOrchestratorVersionId(), releaseUser);
        //获取到orchestratorFramework的appconn
        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(ORCHESTRATOR_FRAMEWORK_NAME);
        //2.通过Label拿到对应的Service
        DevelopmentIntegrationStandard developmentIntegrationStandard = ((OnlyDevelopmentAppConn) appConn).getOrCreateDevelopmentStandard();
        if (developmentIntegrationStandard == null) {
            LOGGER.error("orchestrator framework development is null");
            DSSExceptionUtils.dealErrorException(60021, "orchestrator framework development is null", DSSFrameworkErrorException.class);
        }
        List<DSSLabel> dssLabelList = new ArrayList<>();
        dssLabelList.add(dssLabel);
        AppInstance appInstance = appConn.getAppDesc().getAppInstancesByLabels(dssLabelList).get(0);
        RefExportService refExportService = developmentIntegrationStandard.getRefExportService(appInstance);
        RefExportOperation<ThirdlyRequestRef.RefJobContentRequestRefImpl> refExportOperation = refExportService.getRefExportOperation();
//        OrchestratorExportRequestRef requestRef = null;
//        try {
//            Class<?> clazz = appConn.getClass().
//                    getClassLoader().
//                    loadClass("com.webank.wedatasphere.dss.appconn.orchestrator.ref.DefaultOrchestratorExportRequestRef");
//            requestRef = (OrchestratorExportRequestRef) clazz.newInstance();
////                requestRef = refFactory.newRef(OrchestratorExportRequestRef.class, appConn.getClass().getClassLoader(),
////                        "com.webank.wedatasphere.dss.appconn.orchestrator.ref");
//        } catch (Exception e) {
//            LOGGER.error("failed to new ref for class {}", OrchestratorExportRequestRef.class.getName(), e);
//            DSSExceptionUtils.dealErrorException(60058, "failed to new ref", e, DSSFrameworkErrorException.class);
//        }
        ThirdlyRequestRef.RefJobContentRequestRefImpl requestRef = new ThirdlyRequestRef.RefJobContentRequestRefImpl();
//        requestRef.setOrcId(orchestratorId);
//        requestRef.setOrchestratorVersionId(orchestratorVersionId);
        Long orchestratorId = orchestrator.getOrchestratorId();
        Long orchestratorVersionId = orchestrator.getOrchestratorVersionId();
        requestRef.setRefJobContent(MapUtils.newCommonMap(OrchestratorRefConstant.ORCHESTRATOR_ID_KEY, orchestratorId,
                InternalOrchestratorRefConstant.ORCHESTRATOR_VERSION_ID_KEY, orchestratorVersionId));
        requestRef.setDSSLabels(Collections.singletonList(dssLabel));
        requestRef.setRefProjectId(projectId);
        requestRef.setUserName(releaseUser);
        requestRef.setWorkspace(workspace);
        requestRef.setProjectName(projectName);
        ExportResponseRef responseRef = refExportOperation.exportRef(requestRef);
        BmlResource bmlResource = new BmlResource((String) responseRef.getResourceMap().get(ImportRequestRef.RESOURCE_ID_KEY),
                (String) responseRef.getResourceMap().get(ImportRequestRef.RESOURCE_VERSION_KEY));
        return new ExportResult(bmlResource, (Long) responseRef.getResourceMap().get(InternalOrchestratorRefConstant.ORCHESTRATOR_VERSION_ID_KEY));

    }

    @Override
    public BatchExportResult batchExport(String userName, Long projectId, List<OrchestratorDetail> orchestrators,
                                         String projectName, DSSLabel dssLabel, Workspace workspace) throws ErrorException {
        if(orchestrators==null||orchestrators.isEmpty()){
            throw new DSSRuntimeException("workflow list is empty,nothing to export.(导出的工作流列表为空，没有任何工作流可以导出)");
        }
        String exportSaveBasePath = IoUtils.generateIOPath(userName, projectName, "");
        //写入元数据
        String metaInfoPath=exportSaveBasePath+ File.separator+"meta.json";
        String metaInfo=new Gson().toJson(orchestrators);
        try(OutputStream metaOutStream=IoUtils.generateExportOutputStream(metaInfoPath)) {
            metaOutStream.write(metaInfo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("write meta failed",e);
            throw new RuntimeException(e);
        }
        for (OrchestratorDetail orchestrator : orchestrators) {
            OrchestratorInfo orchestratorInfo = new OrchestratorInfo(orchestrator.getOrchestratorId(), orchestrator.getOrchestratorVersionId());
            BmlResource bmlOneOrc= export(userName, projectId, orchestratorInfo,
                    projectName, dssLabel, workspace)
                    .getBmlResource();
            String orcPath=exportSaveBasePath+orchestrator.getOrchestratorName()+".zip";
            bmlService.downloadToLocalPath(userName,bmlOneOrc.getResourceId(),bmlOneOrc.getVersion(),orcPath);
        }
        String zipFile = ZipHelper.zip(exportSaveBasePath);
        LOGGER.info("export zip file locate at {}",zipFile);
        //先上传
        InputStream inputStream = bmlService.readLocalResourceFile(userName, zipFile);
        BmlResource bmlResource= bmlService.upload(userName, inputStream, projectName + ".OrcsExport", projectName);
        //上传完之后，计算上传后二进制流的md5
        String checkCode;
        try(InputStream zipInputStream=(InputStream)bmlService.download(userName,
                bmlResource.getResourceId(),
                bmlResource.getVersion()).get("is")) {
            checkCode=  DigestUtils.md5Hex(zipInputStream);
        } catch (IOException e) {
            LOGGER.error("md5 sum failed",e);
            throw new DSSRuntimeException(e.getMessage());
        }
        LOGGER.info("export zip file upload to bmlResourceId:{} bmlResourceVersion:{}",
                bmlResource.getResourceId(),bmlResource.getVersion());
        return new BatchExportResult(bmlResource,checkCode);
    }

    @Override
    public Long addVersionAfterPublish(String releaseUser, Long projectId, Long orchestratorId,
                                       Long orchestratorVersionId, String projectName, Workspace workspace,
                                       DSSLabel dssLabel, String comment) {
        List<DSSLabel> dssLabelList = new ArrayList<>();
        dssLabelList.add(dssLabel);
        RequestAddVersionAfterPublish requestAddVersionAfterPublish = new RequestAddVersionAfterPublish(releaseUser, workspace,
                orchestratorId, orchestratorVersionId, projectName, dssLabelList, comment);
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getOrcSender(dssLabelList);
        return RpcAskUtils.processAskException(sender.ask(requestAddVersionAfterPublish), Long.class, RequestAddVersionAfterPublish.class);
    }

}