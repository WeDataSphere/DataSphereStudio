package com.webank.wedatasphere.dss.orchestrator.server.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.CompareWorkflowResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestExecutionHistory;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestPublishHistory;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorCompareRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseHistoryRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseInfoRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseUserRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.response.ReleaseInfoVO;
import org.apache.commons.math3.util.Pair;

import java.util.List;

public interface WebankAppService {

    /**
     * 获取工作流执行历史
     * */
    Pair<Integer, List<ExecutionHistoryVo>> getExecutionHistory(RequestExecutionHistory requestExecutionHistory);


    Pair<Integer, List<ReleaseHistoryDetail>> getReleaseHistory(ReleaseHistoryRequest request) throws DSSErrorException;

    List<String> getReleaseUserList(ReleaseUserRequest request) throws DSSErrorException;

    /**
     * 获取编排版本列表
     * @param request
     * @return
     * @throws DSSErrorException
     */
    Pair<Integer, List<ReleaseHistoryDetail>> getOrchestratorVersionList(ReleaseHistoryRequest request) throws DSSErrorException;

    /**
     * 获取编排版本的创建人或发布人
     * @param orcId
     * @return
     * @throws DSSErrorException
     */
    List<String> getOrchestratorVersionUserList(Long orcId) throws DSSErrorException;

    /**
     * 比较两个编排模式不同的版本
     * @param orchestratorCompareRequest
     * @return
     * @throws DSSErrorException
     */
    List<CompareWorkflowResult> compareOrchestrator(OrchestratorCompareRequest orchestratorCompareRequest) throws DSSErrorException;

    /**
     * 获取批量编排的发布信息（最新发布成功版本）
     * @param request
     * @return
     * @throws DSSErrorException
     */
    List<ReleaseInfoVO> getReleaseInfo(ReleaseInfoRequest request) throws DSSErrorException;

}
