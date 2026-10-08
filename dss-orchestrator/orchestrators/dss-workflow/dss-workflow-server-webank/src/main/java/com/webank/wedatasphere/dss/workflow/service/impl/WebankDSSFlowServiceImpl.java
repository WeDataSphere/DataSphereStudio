package com.webank.wedatasphere.dss.workflow.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestExecutionHistory;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestWorkflowValidNode;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseWorkflowValidNode;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.parser.WorkFlowParser;
import com.webank.wedatasphere.dss.workflow.dao.FlowMapper;
import com.webank.wedatasphere.dss.workflow.dao.WebankDSSFlowMapper;
import com.webank.wedatasphere.dss.workflow.entity.WorkflowTaskVO;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.workflow.service.WebankDSSFlowService;
import com.webank.wedatasphere.dss.workflow.utils.TimeFormater;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WebankDSSFlowServiceImpl implements WebankDSSFlowService {

    private final static Logger log = LoggerFactory.getLogger(WebankDSSFlowServiceImpl.class);

    @Autowired
    WebankDSSFlowMapper webankDSSFlowMapper;
    @Autowired
    private FlowMapper flowMapper;
    @Autowired
    @Qualifier("workflowBmlService")
    private BMLService bmlService;
    @Autowired
    private WorkFlowParser workFlowParser;

    @Override
    public Pair<Integer, List<ExecutionHistoryVo>> getExecutionHistory(RequestExecutionHistory requestExecutionHistory) {
        TimeFormater timeFormater = new TimeFormater();
        Integer currentPage = requestExecutionHistory.getCurrentPage();
        Integer pageSize = requestExecutionHistory.getPageSize();
        List<ExecutionHistoryVo> executionList = null;
        Long totalPage = 0L;
        PageHelper.startPage(currentPage, pageSize);
        try {
            executionList = webankDSSFlowMapper.getExecutionHistoryByFlowId(requestExecutionHistory.getAppId());
            PageInfo pageInfo = new PageInfo(executionList);
            totalPage = pageInfo.getTotal();
        } finally {
            PageHelper.clearPage();
        }
        for (ExecutionHistoryVo executionHistoryVo : executionList) {
            String duration = executionHistoryVo.getDuration();
            String durationAfterDuration = timeFormater.format(Long.parseLong(duration));
            executionHistoryVo.setDuration(durationAfterDuration);
            conversion(executionHistoryVo);
        }
        return new Pair<>(totalPage.intValue(), executionList);
    }

    public List<String> getNodeList(String str) {
        List<String> tempNodeList = new ArrayList<>();
        if (StringUtils.isBlank(str)) {
            return tempNodeList;
        }
        String[] nodeListArr = str.split(",");
        for (int i = 0; i < nodeListArr.length; i++) {
            String nodeId = nodeListArr[i];
            if (StringUtils.isBlank(nodeId) || tempNodeList.contains(nodeId)) {
                continue;
            }
            tempNodeList.add(nodeId);
        }
        return tempNodeList;
    }

    @Override
    public boolean isExecuteSuccess(Long id) {
        //工作流校验开关
        String dicValue = webankDSSFlowMapper.getWorkflowCheckSwitch();
        if (StringUtils.isNotBlank(dicValue) && "0".equals(dicValue.trim())) {
            return true;
        }
        DSSFlow dssFlow = flowMapper.selectFlowByID(id);
        //优先采取这个dss_workflow_task表进行判断
        if (isHaveSuccessWorkflowTask(dssFlow)) {
            return true;
        }
        //然后采取这个dss_workflow_execute_info的成功节点进行判断
        String nodeListStr = webankDSSFlowMapper.getNodeListByFlowIdAndVersion(id, dssFlow.getBmlVersion());
        if (StringUtils.isBlank(nodeListStr)) {
            return false;
        }

        String[] tempNodeListArr = nodeListStr.split(";");
        //执行成功的节点
        List<String> tempNodeList = getNodeList(tempNodeListArr[0]);
        //跳过的节点（子节点）
        List<String> tempSubNodeList = tempNodeListArr.length == 2 ? getNodeList(tempNodeListArr[1]) : new ArrayList<>();

        String userName = dssFlow.getCreator();
        Map<String, Object> query = bmlService.query(userName, dssFlow.getResourceId(), dssFlow.getBmlVersion());
        String flowJson = query.get("string").toString();
        List<DSSNode> nodeJsonList = workFlowParser.getWorkFlowNodes(flowJson);
        if (CollectionUtils.isEmpty(nodeJsonList)) {
            return false;
        }

        for (DSSNode dssNode : nodeJsonList) {
            String tempNodeId = dssNode.getId();
            String nodeType = dssNode.getNodeType();
            if ("workflow.subflow".equalsIgnoreCase(nodeType)) {
                if (!tempSubNodeList.contains(tempNodeId)) {
                    return false;
                }
            } else {
                if (!tempNodeList.contains(tempNodeId)) {
                    return false;
                }
            }
        }
        return true;
    }

    //优先采取这个dss_workflow_task( flowId version status=succeed executeStrategy=execute)全部对上则返回true
    public boolean isHaveSuccessWorkflowTask(DSSFlow dssFlow) {
        try{
            WorkflowTaskVO workflowTaskVO = webankDSSFlowMapper.getLastExecutionHistoryByFlowId(dssFlow.getId());
            if (workflowTaskVO == null
                    || !"Succeed".equalsIgnoreCase(workflowTaskVO.getStatus())) {
                return false;
            }
            String params = workflowTaskVO.getParams();
            Map paramsMap = BDPJettyServerHelper.gson().fromJson(params, Map.class);
            String executeFlag = (String) paramsMap.get("executeStrategy");
            if(!"execute".equalsIgnoreCase(executeFlag)){
                return false;
            }
            String executionCode = workflowTaskVO.getExecutionCode();
            if (StringUtils.isBlank(executionCode) || !executionCode.contains("version")) {
                return false;
            }
            Map map = BDPJettyServerHelper.gson().fromJson(executionCode, Map.class);
            String version = (String) map.get("version");
            //工作流的bml版本号
            int flowVersion = Integer.parseInt(dssFlow.getBmlVersion().substring(1));
            //执行工作流的bml版本号
            int executeVersion = Integer.parseInt(version.substring(1));
            if (flowVersion == executeVersion) {
                return true;
            }
        }catch (Exception e){
            log.error("isHaveExecetionCodeError:",e);
        }
        return false;
    }

    @Override
    public ResponseWorkflowValidNode validWorkflowNode(RequestWorkflowValidNode requestWorkflowValidNode) {
        DSSOrchestratorVersion orchestratorVersion = webankDSSFlowMapper.getLatestOrcVersionByOrcId(requestWorkflowValidNode.getOrcId());
        DSSFlow cyFlow = flowMapper.selectFlowByID(orchestratorVersion.getAppId());
        String userName = cyFlow.getCreator();
        Map<String, Object> query = bmlService.query(userName, cyFlow.getResourceId(), cyFlow.getBmlVersion());
        String flowJson = query.get("string").toString();
        List<String> nodeJsonList = workFlowParser.getWorkFlowNodesJson(flowJson);
        int nodeCount = 0;
        if (CollectionUtils.isNotEmpty(nodeJsonList)) {
            nodeCount = nodeJsonList.size();
        }
        return new ResponseWorkflowValidNode(nodeCount, orchestratorVersion.getId());
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


}
