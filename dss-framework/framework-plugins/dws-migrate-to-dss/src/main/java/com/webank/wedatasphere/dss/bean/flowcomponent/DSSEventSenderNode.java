package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import static  com.webank.wedatasphere.dss.GsonUtils.getString;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2023/10/13
 */
public class DSSEventSenderNode extends DSSCommonNode{
    private static Map<String, Object> getConfiguration() {
        Map<String, Object> configuration = new HashMap<>();
        //默认初始化params.configuration
        Map<String, String> startup = new HashMap<>(1);
        startup.put("ReuseEngine", "true");
        configuration.put("special", new HashMap<>());
        configuration.put("runtime", new HashMap<>());
        configuration.put("startup", startup);
        return configuration;
    }

    public static DSSEventSenderNode fromDwsNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        DSSEventSenderNode dssNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSEventSenderNode.class);
        Map<String, Object> configuration = getConfiguration();
        dssNode.getParams().put("configuration", configuration);
        dssNode.setModifyTime(modifyTime);
        dssNode.setModifyUser(modifyUser);
        dssNode.setJobType(convertType(dssNode.getJobType()));
        JsonObject jobParams=dwsNode.getAsJsonObject("jobContent").getAsJsonObject("jobParams");
        String msgSender =getString( jobParams,"msgSender");
        String msgTopic = getString( jobParams,"msgTopic");
        String msgName = getString( jobParams,"msgName");
        String msgBody = getString( jobParams,"msgBody");
        Map<String, String> runtimeMap = new HashMap<>();
        runtimeMap.put("msg.type", "SEND");
        runtimeMap.put("msg.sender", msgSender);
        runtimeMap.put("msg.topic", msgTopic);
        runtimeMap.put("msg.name", msgName);
        runtimeMap.put("msg.body", msgBody);
        configuration.put("runtime", runtimeMap);

        return dssNode;
    }

}
