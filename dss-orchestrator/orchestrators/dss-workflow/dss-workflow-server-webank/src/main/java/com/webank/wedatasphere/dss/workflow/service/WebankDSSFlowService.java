package com.webank.wedatasphere.dss.workflow.service;

import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestExecutionHistory;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestWorkflowValidNode;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseWorkflowValidNode;
import org.apache.commons.math3.util.Pair;

import java.util.List;

public interface WebankDSSFlowService {

    /**
     * 获取工作流执行历史
     * */
    Pair<Integer, List<ExecutionHistoryVo>> getExecutionHistory(RequestExecutionHistory requestExecutionHistory);

    /**
     * 该工作流是否执行成功
     * @param flowId 工作流ID
     * @return
     */
    boolean isExecuteSuccess(Long flowId) throws Exception ;

    /**
     * 校验是否含有工作流节点
     * @param requestWorkflowValidNode
     * @return
     */
    public ResponseWorkflowValidNode validWorkflowNode(RequestWorkflowValidNode requestWorkflowValidNode);
}
