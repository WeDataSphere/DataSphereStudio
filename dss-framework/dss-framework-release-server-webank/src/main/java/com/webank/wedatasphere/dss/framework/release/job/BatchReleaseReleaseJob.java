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
import com.webank.wedatasphere.dss.framework.project.entity.OrchestratorBatchImportInfo;
import com.webank.wedatasphere.dss.framework.project.entity.po.OrchestratorImportInfo;
import com.webank.wedatasphere.dss.framework.release.conf.ReleaseCodeEnum;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorEntity;
import com.webank.wedatasphere.dss.framework.release.entity.project.ProjectInfo;
import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestReleaseOrchestration;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.OrchestratorBaseInfo;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by cooperyang on 2020/12/9
 * Description: 批量发布的任务
 */
public class BatchReleaseReleaseJob extends AbstractReleaseJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchReleaseReleaseJob.class);

    private List<OrchestratorEntity> orchestratorEntityList;

    private RequestReleaseOrchestration request;



    @Override
    public void run() {
        String projectname = orchestratorEntityList.stream().findAny().map(OrchestratorEntity::getProjectInfo)
                .map(ProjectInfo::getProjectName).orElse(null);
        // 1. 更新数据库任务状态在提交任务前就初始化了
        String errmsg = "";
        EnvDSSLabel dssLabel = (EnvDSSLabel) getDssLabel().stream().filter(label -> label instanceof EnvDSSLabel).findFirst().get();
        nextLabel = new EnvDSSLabel(DSSConfiguration.ENV_LABEL_VALUE_PROD);
        // 2. 批量完成导入导出
        try {
            List<OrchestratorBaseInfo> orchestrators = orchestratorEntityList.stream().map(
                    orchestratorEntity -> {
                        OrchestratorBaseInfo orchestratorBaseInfo = new OrchestratorBaseInfo();
                        orchestratorBaseInfo.setOrchestratorId(orchestratorEntity.getOrchestratorId());
                        orchestratorBaseInfo.setOrchestratorName(orchestratorBaseInfo.getOrchestratorName());
                        orchestratorBaseInfo.setOrchestratorVersionId(orchestratorBaseInfo.getOrchestratorVersionId());
                        return orchestratorBaseInfo;
                    }
            ).collect(Collectors.toList());
            String projectPath = this.releaseEnv.getExportService().batchExport(releaseUser, projectId, orchestrators, projectname, dssLabel, workspace);
            OrchestratorBatchImportInfo batchImportInfos = this.releaseEnv.getImportService().batchImportOrc(releaseUser, projectId, projectname, projectPath, nextLabel, workspace);
            List<OrchestratorImportInfo> importOrcsInfo = batchImportInfos.getTo();
            List<Long> orcIds = importOrcsInfo.stream().map(OrchestratorImportInfo::getOrchestratorId).collect(Collectors.toList());


            //3.批量同步到调度中心
            errmsg = ReleaseCodeEnum.ERROR_THIRD.getCode();
            RequestFrameworkConvertOrchestration newRequest = new RequestFrameworkConvertOrchestration();
            BeanUtils.copyProperties(request, newRequest);
            Map<String, Object> labels = new HashMap<>();
            labels.put(EnvDSSLabel.DSS_ENV_LABEL_KEY, DSSConfiguration.ENV_LABEL_VALUE_PROD);
            newRequest.setLabels(labels);
            newRequest.setOrcIds(orcIds);
            this.releaseEnv.getConversionService().convert(newRequest, Collections.singletonList(nextLabel));

        }catch (final Exception e) {
            LOGGER.error("batch release orchestrator failed", e);
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
            // 只能逐个置为失败
            for (OrchestratorEntity entity : orchestratorEntityList) {
                this.setReleaseTask(entity.getReleaseTask());
                this.releaseEnv.getReleaseJobListener().onJobFailed(this, errorMsg, errmsg, errorCode);
            }
            return;
        }
        for (OrchestratorEntity entity : orchestratorEntityList) {
            Long orchestratorId = entity.getOrchestratorId();
            Long orchestratorVersionId = entity.getOrchestratorVersionId();
            ProjectInfo projectInfo = entity.getProjectInfo();
            errmsg = ReleaseCodeEnum.ERROR_FIRST.getCode();
            this.setReleaseTask(entity.getReleaseTask());
            try {
                LOGGER.info("No 3. release for orchestrator,release scheduler success");
                // 3.1、上传工作流版本对比信息到schedulis
                try {
                    this.releaseEnv.getConversionService().uploadFlowVersionCompareInfo(releaseUser,
                            projectId, orchestratorId, workspace);
                    LOGGER.info("No 3.1 release for orchestrator,upload compare info to  scheduler success");
                } catch (Exception e) {
                    //对比信息上传失败，不影响主流程，仅仅记录下错误。
                    LOGGER.error("upload workflow compare info failed", e);
                    this.getReleaseTask().setLogMsg("upload workflow compare info failed");
                }
                //4.在开发中心添加一个版本号
                errmsg = ReleaseCodeEnum.ERROR_FOURTH.getCode();
                Long vid = this.releaseEnv.getExportService().addVersionAfterPublish(releaseUser,
                        projectId, orchestratorId, orchestratorVersionId, projectInfo.getProjectName(), workspace, dssLabel, request.getComment());

                LOGGER.info("No 4. release for orchestrator,add orchestrator version id :{},success", vid);

                //5.如果都没有报错，那么默认任务应该是成功的,那么则将所有的状态进行置为完成
                this.releaseEnv.getReleaseJobListener().onJobSucceed(this);

            } catch (final Exception e) {
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
                this.releaseEnv.getReleaseJobListener().onJobFailed(this, errorMsg, errmsg, errorCode);
            }
        }
    }


    @Override
    public void close() {
        // close io
    }

    @Override
    boolean supportMultiEnv() {
        return true;
    }


    public List<OrchestratorEntity> getOrchestratorEntityList() {
        return orchestratorEntityList;
    }

    public void setOrchestratorEntityList(List<OrchestratorEntity> orchestratorEntityList) {
        this.orchestratorEntityList = orchestratorEntityList;
    }

    public RequestReleaseOrchestration getRequest() {
        return request;
    }

    public void setRequest(RequestReleaseOrchestration request) {
        this.request = request;
    }
}
