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


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wedatasphere.dss.common.conf.DSSConfiguration;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.protocol.JobStatus;
import com.webank.wedatasphere.dss.framework.release.conf.ReleaseCodeEnum;
import com.webank.wedatasphere.dss.framework.release.context.ReleaseContext;
import com.webank.wedatasphere.dss.framework.release.context.ReleaseEnv;
import com.webank.wedatasphere.dss.framework.release.dao.ReleaseTaskMapper;
import com.webank.wedatasphere.dss.framework.release.entity.project.ProjectInfo;
import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;
import com.webank.wedatasphere.dss.framework.release.job.AbstractReleaseJob;
import com.webank.wedatasphere.dss.framework.release.job.OrchestratorReleaseJob;
import com.webank.wedatasphere.dss.framework.release.job.ReleaseStatus;
import com.webank.wedatasphere.dss.framework.release.service.ReleaseService;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponsePublishUser;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.*;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.*;

/**
 * created by cooperyang on 2020/11/17
 * Description:
 */
@Service
public class ReleaseServiceImpl implements ReleaseService {


    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseServiceImpl.class);


    @Autowired
    private ReleaseEnv releaseEnv;

    @Autowired
    private ReleaseContext releaseContext;

    @Autowired
    private ReleaseTaskMapper releaseTaskMapper;

    @Override
    @SuppressWarnings("ConstantConditions")
    public ResponseConvertOrchestrator releaseOrchestrator(RequestReleaseOrchestration request) {
        String releaseUser = request.getUserName();
        OrchestratorInfo orchestratorInfo = releaseEnv.getOrchestratorMapper().getOrcInfoByAppId(request.getOrcAppId());
        Long orchestratorVersionId = orchestratorInfo.getOrchestratorVersionId();
        Long orchestratorId = orchestratorInfo.getOrchestratorId();
        List<DSSLabel> dssLabels = LabelBuilderFactoryContext.getLabelBuilderFactory().getLabels(request.getLabels());
        Workspace workspace = (Workspace) request.getWorkspace();
        LOGGER.info("received a release orchestrator request releaseUser is {}, orchestratorVersionId is {}", releaseUser, orchestratorVersionId);

        //1.通过orchestratorVersionId获取到project的信息
        ProjectInfo projectInfo = releaseEnv.getProjectService().getProjectInfoByOrchestratorId(orchestratorId);
        String orchestratorName = releaseEnv.getProjectService().getOrchestratorName(orchestratorId, orchestratorVersionId);

        //2.查看发布工程下是否含有编排模式正在发布
        //查询出状态是初始化或运行中，创建时间是不超过30分钟的数量
        int count = releaseTaskMapper.getReleaseTaskByOrcId(orchestratorId);
        if (count > 0) {
            return new ResponseConvertOrchestrator(serializeId(-999L), ResponseOperateOrchestrator.failed("another orchestrator is publishing,please wait a minute(工程下已有另一个编排正在发布，请稍后再试)"));
        }

        //3.插入数据到数据库
        ReleaseTask releaseTask = releaseEnv.getTaskService().addReleaseTask(releaseUser,
                projectInfo.getProjectId(),
                orchestratorId,
                orchestratorVersionId, orchestratorName,request.getComment(), Sender.getThisInstance());
        //4.生成任务,然后放入到线程池中
        OrchestratorReleaseJob job = new OrchestratorReleaseJob();
        job.setReleaseTask(releaseTask);
        job.setRequest(request);
        job.setReleaseEnv(releaseEnv);
        job.setWorkspace(workspace);
        job.setDssLabel(dssLabels);
        releaseContext.submitReleaseJob(job);
        //返回具体的jobId
        LOGGER.info("finished to get release job id for releaseUser {} orchestratorId {}, jobId is {}", releaseUser, orchestratorId, job.getJobId());
        return new ResponseConvertOrchestrator(serializeId(job.getJobId()), ResponseOperateOrchestrator.inited());
    }
    @Override
    public  void releaseOrchestratorFromProToSchedulis(RequestFrameworkConvertOrchestration request) throws DSSErrorException {
        this.releaseEnv.getConversionService().convert(request,
                Collections.singletonList(new EnvDSSLabel( DSSConfiguration.ENV_LABEL_VALUE_PROD)));
        LOGGER.info(" release orchestrator from product center to schedulis  success");

    }

    private String serializeId(Long id) {
        return String.valueOf(id);
    }

    private Long deserializeId(String id) {
        return Long.parseLong(id);
    }

    @Override
    public ResponseConvertOrchestrator getStatus(String jobId) {
        long id = deserializeId(jobId);
        Tuple2<String, String> tuple2 = releaseContext.getReleaseJobStatus(id);
        ResponseOperateOrchestrator response = new ResponseOperateOrchestrator();
        response.setJobStatus(JobStatus.valueOf(tuple2._1));
        response.setMessage(tuple2._2);
        return new ResponseConvertOrchestrator(jobId, response);
    }

    @Override
    public ResponsePublishHistory getReleaseHistory(RequestPublishHistory request){
        Integer currentPage = request.getCurrentPage();
        Integer pageSize = request.getPageSize();
        List<ReleaseHistoryDetail> historyList = null;
        Long totalPage = 0L;
        PageHelper.startPage(currentPage, pageSize);
        try {
            historyList = releaseTaskMapper.getReleaseHistoryByOrchestratorId(request);
            setErrorMessage(historyList);
            PageInfo pageInfo = new PageInfo(historyList);
            totalPage = pageInfo.getTotal();
        } finally {
            PageHelper.clearPage();
        }
        ResponsePublishHistory responsePublishHistory = new ResponsePublishHistory();
        responsePublishHistory.setTotalPage(totalPage.intValue());
        responsePublishHistory.setReleaseHistorys(historyList);
        return responsePublishHistory;
    }

    //根据错误状态码，返回发布错误消息
    public void setErrorMessage(List<ReleaseHistoryDetail> historyList){
        if (CollectionUtils.isEmpty(historyList)) {
            return;
        }
        for (ReleaseHistoryDetail releaseHistoryDetail : historyList) {
            //非发布错误直接过滤掉
            if(!ReleaseStatus.FAILED.getStatus().equalsIgnoreCase(releaseHistoryDetail.getStatus())){
                continue;
            }
            if(analyseErrorMsg(releaseHistoryDetail)){
                continue;
            }
            //错误码
            String bak = releaseHistoryDetail.getBak();
            //发布流程错误码
            String logMsg = releaseHistoryDetail.getLogMsg();
            if (StringUtils.isNotBlank(bak) && StringUtils.isNotBlank(logMsg)) {
                String msg1 = ReleaseCodeEnum.getErrorMsgByCode(bak);
                String msg2 = ReleaseCodeEnum.getErrorMsgByCode(logMsg);
                if (StringUtils.isNotBlank(msg1) && StringUtils.isNotBlank(msg2)) {
                    releaseHistoryDetail.setErrorMessage(msg2 + "，错误信息: " + msg1);
                }
            }
        }
    }


    //分析错误原因
    private boolean analyseErrorMsg(ReleaseHistoryDetail releaseHistoryDetail){
        try{
            if(!ReleaseStatus.FAILED.getStatus().equalsIgnoreCase(releaseHistoryDetail.getStatus())){
                return false;
            }
            String errorMsg = releaseHistoryDetail.getRecode();
            if(StringUtils.isBlank(errorMsg)){
                return false;
            }

            String midmsg = "，报错原因：";
            //DESC: errCode: 90012 ,DESC: ErrorException: errCode: 90014 ,DESC: errCode: 90013
            if(errorMsg.contains("90012") && errorMsg.contains("90014") && errorMsg.contains("90013")){
                releaseHistoryDetail.setRecode(ReleaseCodeEnum.ERROR_THIRD.getMsg());
                return true;
            }
            if(errorMsg.contains("Export Visualis Exception")){
                releaseHistoryDetail.setRecode(ReleaseCodeEnum.ERROR_FIRST.getMsg()+midmsg+ReleaseCodeEnum.export_visualis_error.getMsg());
                return true;
            }
            if(errorMsg.contains("import orchestrator ref failed")){
                releaseHistoryDetail.setRecode(ReleaseCodeEnum.ERROR_FIRST.getMsg()+midmsg+ReleaseCodeEnum.import_orchestrator_error.getMsg());
                return true;
            }
            if(errorMsg.contains("Read timed out executing POST http://DSS-Framework-Orchestrator-Server-Dev")){
                releaseHistoryDetail.setRecode(ReleaseCodeEnum.ERROR_FIRST.getMsg()+midmsg+ReleaseCodeEnum.post_orchestrator_server_out_time.getMsg());
                return true;
            }
            if(errorMsg.contains("Read timed out executing POST http://DSS-Framework-Orchestrator-Server-Dev")){
                releaseHistoryDetail.setRecode(ReleaseCodeEnum.ERROR_FIRST.getMsg()+midmsg+ReleaseCodeEnum.post_orchestrator_server_out_time.getMsg());
                return true;
            }

        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public ResponsePublishUser getReleaseUserList(RequestPublishUser request) {
        List<String> usernameList = releaseTaskMapper.getReleaseUserListByOrcId(request.getOrchestratorId());
        ResponsePublishUser publishUser = new ResponsePublishUser();
        publishUser.setUserList(usernameList);
        return publishUser;
    }

    //两个时间差，单位为：分钟
    public static long getDateMinute(Date startDate, Date endDate) {
        long nh = 1000L * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startDate.getTime();
        long hour = diff / nh;
        return hour;
    }
}
