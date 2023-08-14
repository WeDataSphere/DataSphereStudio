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

package com.webank.wedatasphere.dss.framework.release.job;

import com.webank.wedatasphere.dss.common.conf.DSSConfiguration;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.framework.release.conf.ReleaseCodeEnum;
import com.webank.wedatasphere.dss.framework.release.entity.export.ExportResult;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorReleaseInfo;
import com.webank.wedatasphere.dss.framework.release.entity.project.ProjectInfo;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestReleaseOrchestration;
import org.apache.linkis.common.exception.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * created by cooperyang on 2020/11/17
 * Description: 一个编排模式发布的任务
 */
public class OrchestratorReleaseJob extends AbstractReleaseJob{


    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorReleaseJob.class);


    private Long orchestratorId;

    private Long orchestratorVersionId;

    private String orchestratorName;

    private RequestReleaseOrchestration request;


    public Long getOrchestratorVersionId() {
        return orchestratorVersionId;
    }

    public void setOrchestratorVersionId(Long orchestratorVersionId) {
        this.orchestratorVersionId = orchestratorVersionId;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public void setRequest(RequestReleaseOrchestration request) {
        this.request = request;
    }

    @Override
    public void setReleaseTask(ReleaseTask releaseTask) {
        super.setReleaseTask(releaseTask);
        this.orchestratorVersionId = releaseTask.getOrchestratorVersionId();
        this.orchestratorId = releaseTask.getOrchestratorId();
        this.orchestratorName = releaseTask.getOrchestratorName();
    }

    @Override
    boolean supportMultiEnv() {
        return true;
    }

    @Override
    public void run() {
        String errmsg = "";
        LOGGER.info("begin to do release project {} orchestratorId is {} versionId is {}", projectId, orchestratorId, orchestratorVersionId);
        try{
            this.setStatus(ReleaseStatus.RUNNING);
            EnvDSSLabel dssLabel = (EnvDSSLabel) getDssLabel().stream().filter(label -> label instanceof EnvDSSLabel).findFirst().get();
            this.releaseEnv.getReleaseJobListener().onJobStatusUpdate(this);
            String workspaceName = this.releaseEnv.getProjectService().getWorkspaceName(projectId);
            ProjectInfo projectInfo = this.releaseEnv.getProjectService().getProjectInfoById(projectId);

            errmsg = ReleaseCodeEnum.ERROR_FIRST.getCode();
            //1.进行导出
            ExportResult exportResult = this.releaseEnv.getExportService().export(releaseUser,
                    projectId,new OrchestratorInfo( orchestratorId, orchestratorVersionId),
                    projectInfo.getProjectName(), dssLabel, workspace);
            this.getReleaseTask().setOrchestratorVersionId(exportResult.getOrchestratorVersionId());
            BmlResource resourceList = exportResult.getBmlResource();
            LOGGER.info("No 1. release for orchestrator,export success");
            //2*.进行工程级别的封装

            //2.进行导入
            //todo nextLabel要用labelservice来进行获取
            nextLabel = new EnvDSSLabel(DSSConfiguration.ENV_LABEL_VALUE_PROD);

            errmsg = ReleaseCodeEnum.ERROR_SECOND.getCode();
            OrchestratorReleaseInfo importOrcInfos = this.releaseEnv.getImportService().importOrc(orchestratorName, releaseUser,
                    projectInfo.getProjectId(),projectInfo.getProjectName(), resourceList, nextLabel, workspaceName, workspace);
            LOGGER.info("No 2. release for orchestrator,import success");

//            //3.进行同步到调度中心
            errmsg = ReleaseCodeEnum.ERROR_THIRD.getCode();
            RequestFrameworkConvertOrchestration newRequest = new RequestFrameworkConvertOrchestration();
            BeanUtils.copyProperties(request, newRequest);
            Map<String, Object> labels = new HashMap<>();
            labels.put(EnvDSSLabel.DSS_ENV_LABEL_KEY, DSSConfiguration.ENV_LABEL_VALUE_PROD);
            newRequest.setLabels(labels);
            newRequest.setOrcIds(Collections.singletonList( importOrcInfos.getOrchestratorId()));
            this.releaseEnv.getConversionService().convert(newRequest, Collections.singletonList(nextLabel));
            LOGGER.info("No 3. release for orchestrator,release WTSS success");


            //4.在开发中心添加一个版本号
            errmsg = ReleaseCodeEnum.ERROR_FOURTH.getCode();
            Long vid = this.releaseEnv.getExportService().addVersionAfterPublish(releaseUser,
                    projectId, orchestratorId, orchestratorVersionId, projectInfo.getProjectName(), workspace, dssLabel,request.getComment());
            exportResult.setOrchestratorVersionId(vid);
            LOGGER.info("No 4. release for orchestrator,add orchestrator version id :{},success", vid);

            //5.如果都没有报错，那么默认任务应该是成功的,那么则将所有的状态进行置为完成
            this.releaseEnv.getReleaseJobListener().onJobSucceed(this);
        }catch(final Exception e) {
            LOGGER.error("");
            LOGGER.error("");
            LOGGER.error("release for orchestrator {} failed", orchestratorId, e);
            String errorCode = "";
            try {
                if (e instanceof ErrorException) {
                    ErrorException errorException = (ErrorException) e;
                    errorCode = errorException.getErrCode() + "";
                }
            } catch (Exception logE) {
                LOGGER.error(String.valueOf(logE));
            }
            String errorMsg = e.getMessage();
            this.releaseEnv.getReleaseJobListener().onJobFailed(this,errorMsg, errmsg, errorCode);
        }
    }

    @Override
    public void close() {
        //ensure to close
    }
}
