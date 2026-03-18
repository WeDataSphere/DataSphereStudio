package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.label.LabelKeyConvertor;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.CompareWorkflowResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponsePublishUser;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.*;
import com.webank.wedatasphere.dss.orchestrator.db.dao.OrchestratorMapper;
import com.webank.wedatasphere.dss.orchestrator.db.dao.WebankOrchestratorMapper;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorCompareRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseHistoryRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseInfoRequest;
import com.webank.wedatasphere.dss.framework.release.job.ReleaseStatus;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseUserRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.response.ReleaseInfoVO;
import com.webank.wedatasphere.dss.orchestrator.server.service.WebankAppService;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import org.apache.linkis.rpc.Sender;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebankAppServiceImpl implements WebankAppService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebankAppServiceImpl.class);
    @Autowired
    private OrchestratorMapper orchestratorMapper;
    @Autowired
    private WebankOrchestratorMapper webankOrchestratorMapper;
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_HOLDER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Override
    public Pair<Integer, List<ExecutionHistoryVo>> getExecutionHistory(RequestExecutionHistory requestExecutionHistory) {
        List<DSSLabel> dssLabelList = getDSSLabelList(new Gson().toJson(requestExecutionHistory));
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getWorkflowSender(dssLabelList);
        Pair<Integer, List<ExecutionHistoryVo>> queryResult = (Pair<Integer, List<ExecutionHistoryVo>>) sender.ask(requestExecutionHistory);
        return queryResult;
    }

    @Override
    public Pair<Integer, List<ReleaseHistoryDetail>> getReleaseHistory(ReleaseHistoryRequest request) throws DSSErrorException {
        LOGGER.info("start to query release history:{}", request.getWorkspaceId());
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender();
        RequestPublishHistory requestPublishHistory = getRequestPublishHistory(request);
        ResponsePublishHistory responsePublishHistory = RpcAskUtils.processAskException(sender.ask(requestPublishHistory),
                ResponsePublishHistory.class, RequestPublishHistory.class);
        List<ReleaseHistoryDetail> releaseHistoryDetails = responsePublishHistory.getReleaseHistorys();
        Integer totalPage = responsePublishHistory.getTotalPage();
        return new Pair<>(totalPage, releaseHistoryDetails);
    }

    //参数封装
    public RequestPublishHistory getRequestPublishHistory(ReleaseHistoryRequest request){
        RequestPublishHistory requestPublishHistory = new RequestPublishHistory();
        requestPublishHistory.setOrchestratorId(request.getOrchestratorId());
        requestPublishHistory.setCurrentPage(request.getCurrentPage());
        requestPublishHistory.setPageSize(request.getPageSize());
        requestPublishHistory.setComment(request.getComment());
        if("ALL".equalsIgnoreCase(request.getReleaseUser())){
            request.setReleaseUser(null);
        }
        requestPublishHistory.setReleaseUser(request.getReleaseUser());
        requestPublishHistory.setStartTime(request.getStartTime());
        requestPublishHistory.setEndTime(request.getEndTime());
        return requestPublishHistory;
    }

    @Override
    public List<String> getReleaseUserList(ReleaseUserRequest request) throws DSSErrorException {
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender();
        RequestPublishUser requestPublishUser = new RequestPublishUser();
        requestPublishUser.setOrchestratorId(request.getOrchestratorId());
        ResponsePublishUser publishUser= RpcAskUtils.processAskException(sender.ask(requestPublishUser), ResponsePublishUser.class,RequestPublishUser.class);
        return publishUser.getUserList();
    }

    @Override
    public Pair<Integer, List<ReleaseHistoryDetail>> getOrchestratorVersionList(ReleaseHistoryRequest request) throws DSSErrorException {
        RequestPublishHistory requestPublishHistory = getRequestPublishHistory(request);
        List<DSSOrchestratorVersion> versionList = null;
        Integer currentPage = request.getCurrentPage();
        Integer pageSize = request.getPageSize();
        Long totalPage = 0L;
        PageHelper.startPage(currentPage, pageSize);
        try {
            versionList = webankOrchestratorMapper.getOrchestratorVersionByParam(requestPublishHistory);
            PageInfo pageInfo = new PageInfo(versionList);
            totalPage = pageInfo.getTotal();
        } finally {
            PageHelper.clearPage();
        }
        return new Pair<>(totalPage.intValue(), formatList(versionList));
    }

    //返回值，进行format
    public List<ReleaseHistoryDetail> formatList(List<DSSOrchestratorVersion> versionList) {
        if (CollectionUtils.isEmpty(versionList)) {
            return new ArrayList<>();
        }
        List<ReleaseHistoryDetail> releaseHistoryDetails = new ArrayList<>(versionList.size());
        SimpleDateFormat simpleDateFormat = SIMPLE_DATE_FORMAT_HOLDER.get();
        versionList.forEach(a -> {
            ReleaseHistoryDetail releaseHistoryDetail = new ReleaseHistoryDetail();
            releaseHistoryDetail.setId(a.getId().intValue());
            if (a.getUpdateTime() != null) {
                releaseHistoryDetail.setReleaseTime(simpleDateFormat.format(a.getUpdateTime()));
            }
            releaseHistoryDetail.setLastModifyUser(a.getUpdater());
            releaseHistoryDetail.setReleaseUser(a.getUpdater());
            releaseHistoryDetail.setOrchestratorVersionId(a.getId());
            releaseHistoryDetail.setVersion(a.getVersion());
            releaseHistoryDetail.setRecode(a.getComment());
            releaseHistoryDetails.add(releaseHistoryDetail);
        });
        SIMPLE_DATE_FORMAT_HOLDER.remove();
        return releaseHistoryDetails;
    }

    @Override
    public List<String> getOrchestratorVersionUserList(Long orcId) throws DSSErrorException {
        return webankOrchestratorMapper.getOrchestratorVersionUserList(orcId,1);
    }

    @Override
    public List<CompareWorkflowResult> compareOrchestrator(OrchestratorCompareRequest orchestratorCompareRequest)throws DSSErrorException {
        DSSOrchestratorVersion secondOrc = webankOrchestratorMapper.getAppIdByVersionId(orchestratorCompareRequest.getSecondVersionId());
        if (secondOrc == null) {
            throw new DSSErrorException(70007,"不存在对应的secondVersionId的版本");
        }
        DSSOrchestratorVersion firstOrc = null;
        if (orchestratorCompareRequest.getFirstVersionId() == null) {
            firstOrc = webankOrchestratorMapper.getNextAppIdByVersionId(secondOrc.getOrchestratorId(),orchestratorCompareRequest.getSecondVersionId());
            if (firstOrc == null) {
                throw new DSSErrorException(70008,"不存在对应的secondVersionId的下个版本");
            }
            orchestratorCompareRequest.setFirstVersionId(firstOrc.getId());
        }else{
            firstOrc = webankOrchestratorMapper.getAppIdByVersionId(orchestratorCompareRequest.getFirstVersionId());
            if (firstOrc == null) {
                throw new DSSErrorException(70008,"不存在对应的firstVersionId的版本");
            }
        }
        Long firstAppId = firstOrc.getAppId();
        Long secondAppId = secondOrc.getAppId();

        List<DSSLabel> dssLabelList = getDSSLabelList(new Gson().toJson(orchestratorCompareRequest));
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getWorkflowSender(dssLabelList);
        RequestAppCompare requestAppCompare = new RequestAppCompare();
        requestAppCompare.setFirstFlowId(firstAppId);
        requestAppCompare.setSecondFlowId(secondAppId);
        ResponseAppCompare responseAppCompare = RpcAskUtils.processAskException(sender.ask(requestAppCompare), ResponseAppCompare.class, RequestAppCompare.class);
        List<CompareWorkflowResult> compareWorkflowResultList = responseAppCompare.getList();
        addOrcVersion(compareWorkflowResultList, firstOrc, secondOrc);
        return compareWorkflowResultList;
    }

    /**
     * 添加编排模式版本号
     * @param compareWorkflowResultList
     * @param firstOrc
     * @param secondOrc
     */
    public void addOrcVersion(List<CompareWorkflowResult> compareWorkflowResultList,DSSOrchestratorVersion firstOrc,DSSOrchestratorVersion secondOrc){
        if(CollectionUtils.isEmpty(compareWorkflowResultList)){
            return;
        }
        Map<Long,String> versionMap = new HashMap<>();
        versionMap.put(firstOrc.getAppId(),firstOrc.getVersion());
        versionMap.put(secondOrc.getAppId(),secondOrc.getVersion());
        compareWorkflowResultList.forEach(a->{
            a.setFirstOrcVersion(firstOrc.getVersion());
            a.setSecondOrcVersion(secondOrc.getVersion());
        });
    }

    /**
     * 如果执行成功，错误码为0，转换为 无，错误信息转换为 无
     * 如果发执行失败，错误码为真实的错误码，错误信息转换为 节点失败导致取消
     */
    private void conversion(ExecutionHistoryVo executionHistoryVo) {
        if (StringUtils.isNotBlank(executionHistoryVo.getErrorCode()) &&
                executionHistoryVo.getErrorCode().equals("0")) {
            executionHistoryVo.setErrorCode("无");
            executionHistoryVo.setErrorMessage("无");
        }
    }

    //生成label list
    public List<DSSLabel> getDSSLabelList(String labels) {
        String labelStr = DSSCommonUtils.ENV_LABEL_VALUE_DEV;
        Map<String, Object> labelMap = DSSCommonUtils.COMMON_GSON.fromJson(labels, Map.class);
        if (labelMap.containsKey(LabelKeyConvertor.ROUTE_LABEL_KEY)) {
            labelStr = (String) labelMap.get(LabelKeyConvertor.ROUTE_LABEL_KEY);
        }
        List<DSSLabel> dssLabelList = Arrays.asList(new EnvDSSLabel(labelStr));
        return dssLabelList;
    }

    @Override
    public List<ReleaseInfoVO> getReleaseInfo(ReleaseInfoRequest request) throws DSSErrorException {
        LOGGER.info("start to query release info: {}", request);

        // 校验所有编排ID是否属于同一个项目
        validateOrchestratorsInSameProject(request.getOrchestratorIds(), request.getProjectId());

        List<ReleaseInfoVO> resultList = new ArrayList<>();

        // 通过RPC调用ProjectServer查询发布信息
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender();

        // 逐个查询每个orchestratorId的发布信息
        for (Long orchestratorId : request.getOrchestratorIds()) {
            RequestPublishHistory requestPublishHistory = new RequestPublishHistory();
            requestPublishHistory.setOrchestratorId(orchestratorId);
            // 设置状态过滤为success，只查询发布成功的记录
            requestPublishHistory.setStatus(ReleaseStatus.SUCCESS.getStatus());
            requestPublishHistory.setComment(request.getComment());
            requestPublishHistory.setReleaseUser(request.getReleaseUser());
            requestPublishHistory.setStartTime(request.getStartTime());
            requestPublishHistory.setEndTime(request.getEndTime());
            requestPublishHistory.setCurrentPage(1);
            requestPublishHistory.setPageSize(1);

            ResponsePublishHistory responsePublishHistory = RpcAskUtils.processAskException(sender.ask(requestPublishHistory),
                    ResponsePublishHistory.class, RequestPublishHistory.class);

            List<ReleaseHistoryDetail> releaseHistoryDetails = responsePublishHistory.getReleaseHistorys();

            if (CollectionUtils.isNotEmpty(releaseHistoryDetails)) {
                // SQL已过滤出success状态，第一条就是最新的成功记录
                ReleaseHistoryDetail detail = releaseHistoryDetails.get(0);
                ReleaseInfoVO vo = new ReleaseInfoVO();
                // 复制基础字段
                vo.setId(detail.getId());
                vo.setStatus(detail.getStatus());
                vo.setRecode(detail.getRecode());
                vo.setReleaseUser(detail.getReleaseUser());
                vo.setVersion(detail.getVersion());
                vo.setLastModifyUser(detail.getLastModifyUser());
                vo.setReleaseTime(detail.getReleaseTime());
                vo.setErrorMessage(detail.getErrorMessage());
                vo.setOrchestratorVersionId(detail.getOrchestratorVersionId());
                vo.setAppId(detail.getAppId());
                vo.setLogMsg(detail.getLogMsg());
                vo.setBak(detail.getBak());

                // 获取编排信息并设置扩展字段
                String orchestratorName = webankOrchestratorMapper.getOrchestratorNameById(orchestratorId.intValue());
                vo.setOrchestratorId(orchestratorId);
                vo.setOrchestratorName(orchestratorName);
                vo.setProjectId(request.getProjectId());
                vo.setWorkspaceId(request.getWorkspaceId());

                resultList.add(vo);
            }
        }

        return resultList;
    }

    /**
     * 校验所有编排ID是否属于同一个项目
     */
    private void validateOrchestratorsInSameProject(List<Long> orchestratorIds, Integer projectId) throws DSSErrorException {
        if (CollectionUtils.isEmpty(orchestratorIds)) {
            return;
        }

        // 获取项目的所有编排详情
        Map<String, Object> params = new HashMap<>();
        params.put("project_id", projectId);
        List<DSSOrchestratorInfo> orchestratorInfoList =  orchestratorMapper.queryOrchestratorInfos(params);

        Set<Long> projectOrchestratorIds = orchestratorInfoList.stream()
                .map(DSSOrchestratorInfo::getId)
                .collect(Collectors.toSet());

        for (Long orchestratorId : orchestratorIds) {
            if (!projectOrchestratorIds.contains(orchestratorId)) {
                throw new DSSErrorException(70010, "编排ID " + orchestratorId + " 不属于项目 " + projectId);
            }
        }
    }
}
