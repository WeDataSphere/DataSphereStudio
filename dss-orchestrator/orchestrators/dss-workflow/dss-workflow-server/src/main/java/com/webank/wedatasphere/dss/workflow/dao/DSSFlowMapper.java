package com.webank.wedatasphere.dss.workflow.dao;


import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.workflow.entity.WorkflowTaskVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DSSFlowMapper {

    /**
     * 过工作流ID查询工作流的执行情况
     * */
    List<ExecutionHistoryVo> getExecutionHistoryByFlowId(Long flowId);

    WorkflowTaskVO getLastExecutionHistoryByFlowId(@Param("flowId") Long flowID);

    @Select("SELECT * FROM dss_orchestrator_version_info WHERE orchestrator_id = #{orcId} order by id desc limit 1")
    DSSOrchestratorVersion getLatestOrcVersionByOrcId(@Param("orcId") Long orcId);

    @Select("SELECT * FROM dss_orchestrator_version_info WHERE app_id = #{appId} order by id desc limit 1")
    DSSOrchestratorVersion getLatestOrcVersionByAppId(@Param("appId") Long appId);

    @Select("select concat(succeed_jobs,';',Pending_jobs) FROM dss_workflow_execute_info WHERE flow_id = #{flowId} AND version = #{version} and status = 1 order by id desc limit 1 ")
    String getNodeListByFlowIdAndVersion(@Param("flowId") Long flowId,@Param("version") String version);

    @Select("SELECT dic_value FROM dss_workspace_dictionary WHERE dic_key = 'workflow_check_switch'")
    String getWorkflowCheckSwitch();

    @Select("SELECT node_type FROM dss_workflow_node WHERE appconn_name = 'scriptis' AND name not in ('connector','subFlow')")
    List<String> getLinkisNodeTypeWorkflow();
}
