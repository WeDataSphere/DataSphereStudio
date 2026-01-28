package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2023/10/13
 */
public class DSSSubflowNode extends DSSCommonNode{
    private static final Map<String, Object> configuration = new HashMap<>();
    static {
        //默认初始化params.configuration

        configuration.put("special", new HashMap<>());
        configuration.put("runtime", new HashMap<>());
        configuration.put("startup", new HashMap<>());
    }

    private String creator = "";
    private JobContent jobContent;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public JobContent getJobContent() {
        return jobContent;
    }

    public void setJobContent(JobContent jobContent) {
        this.jobContent = jobContent;
    }

    public static DSSSubflowNode fromDwsNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        DSSSubflowNode dssShellNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSSubflowNode.class);
        dssShellNode.getParams().put("configuration", configuration);
        dssShellNode.setModifyTime(modifyTime);
        dssShellNode.setModifyUser(modifyUser);
        dssShellNode.setJobType(convertType(dssShellNode.getJobType()));
        return dssShellNode;
    }




    public static class JobContent{
        private Long embeddedFlowId;

        public Long getEmbeddedFlowId() {
            return embeddedFlowId;
        }

        public void setEmbeddedFlowId(Long embeddedFlowId) {
            this.embeddedFlowId = embeddedFlowId;
        }
    }
}
