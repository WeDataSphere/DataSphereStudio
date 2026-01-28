package com.webank.wedatasphere.dss.service;

import com.google.gson.JsonObject;
import static  com.webank.wedatasphere.dss.GsonUtils.getString;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSCommonNode;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSFlowJson;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSSubflowNode;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlowRelation;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;


/**
 * Author: xlinliu
 * Date: 2023/10/23
 */
public class DSSFlowParser {
    public static DSSFlow fromDwsFlow(JsonObject dwsflow){
        JsonObject latestVersion = dwsflow.getAsJsonObject("latestVersion");
        DSSFlow dssFlow = DSSCommonUtils.COMMON_GSON.fromJson(dwsflow,DSSFlow.class);
        dssFlow.setState(false);
        dssFlow.setRootFlow (dwsflow.get("rootFlow").getAsBoolean());
        dssFlow.setRank (0);

        dssFlow.setLinkedAppConnNames (null);
        dssFlow.setDssLabels("{\"DSSEnv\":\"dev\"}");

        dssFlow.setHasSaved (true);
        dssFlow.setUses("");

        dssFlow.setResourceId (getString(latestVersion,"jsonPath"));
        dssFlow.setBmlVersion(getString(latestVersion,"version"));
        dssFlow.setMetrics(null);
        dssFlow.setFlowIdParamConfTemplateIdTuples (null);
        dssFlow.setFlowJson(null);
        return dssFlow;
    }

    /**
     * 生成节点父子关系
     * @param flowIdDSSFlowJsonMap
     * @return
     */
    public static List<DSSFlowRelation> fromDwsRelation(Map<Long,DSSFlowJson> flowIdDSSFlowJsonMap){
        List<DSSFlowRelation> relations = new ArrayList<>();
        for (Map.Entry<Long, DSSFlowJson> flowIdDSSFlowJsonEntry : flowIdDSSFlowJsonMap.entrySet()) {
            long parentFlowId = flowIdDSSFlowJsonEntry.getKey();
            DSSFlowJson dssFlowJson = flowIdDSSFlowJsonEntry.getValue();
            for (DSSCommonNode node : dssFlowJson.getNodes()) {
                if (node instanceof DSSSubflowNode) {
                    DSSSubflowNode subflowNode=(DSSSubflowNode) node;
                    Long subflowId= subflowNode.getJobContent().getEmbeddedFlowId();
                    relations.add(new DSSFlowRelation(subflowId, parentFlowId));
                }
            }
        }
        return relations;
    }
    public static DSSOrchestratorInfo fromDwsOrc(JsonObject dwsflow){
        DSSOrchestratorInfo orc = new DSSOrchestratorInfo();
        Long id = dwsflow.get("id").getAsLong();
        String name = getString(dwsflow,"name");
        String desc = getString(dwsflow, "description");
        orc.setId (null);
        orc.setName (name);
        orc.setType ("workflow");
        orc.setDesc (desc);
        orc.setUses ("");
        orc.setAppConnName ("workflow");
        //用name+id的方式代替uuid，确保每次导出时，uuid是相同的
        String uuid = DigestUtils.md5Hex(name+id);
        orc.setUUID (uuid);
        orc.setSecondaryType ("[pom_work_flow_DAG]");
        orc.setUpdateTime (new Date());
        orc.setComment (desc);
        return orc;
    }
}
