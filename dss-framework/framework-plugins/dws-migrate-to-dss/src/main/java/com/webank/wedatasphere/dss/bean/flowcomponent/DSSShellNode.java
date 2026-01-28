package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * dss shell 节点
 * Author: xlinliu
 * Date: 2023/10/12
 */
public class DSSShellNode extends DSSCommonNode{
    private static final  Map<String, Object> configuration = new HashMap<>();
    static {
        //默认初始化params.configuration
        Map<String, String> startup = new HashMap<>(1);
        startup.put("ReuseEngine", "true");
        configuration.put("special", new HashMap<>());
        configuration.put("runtime", new HashMap<>());
        configuration.put("startup", startup);
    }
    private JobContent jobContent;


    public JobContent getJobContent() {
        return jobContent;
    }

    public void setJobContent(JobContent jobContent) {
        this.jobContent = jobContent;
    }

    public static DSSShellNode fromDwsNode(JsonObject dwsNode,long modifyTime,String modifyUser){
        DSSShellNode dssShellNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSShellNode.class);
        dssShellNode.getParams().put("configuration", configuration);
        dssShellNode.setModifyTime(modifyTime);
        dssShellNode.setModifyUser(modifyUser);
        dssShellNode.setJobType(convertType(dssShellNode.getJobType()));
        return dssShellNode;
    }


    public static class JobContent{
        private String script;

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }
    }

}
