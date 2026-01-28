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
public class DSSEventReceiverNode extends DSSCommonNode{
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

    public static DSSEventReceiverNode fromDwsNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        DSSEventReceiverNode dssNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSEventReceiverNode.class);
        Map<String, Object> configuration = getConfiguration();
        dssNode.getParams().put("configuration", configuration);
        dssNode.setModifyTime(modifyTime);
        dssNode.setModifyUser(modifyUser);
        dssNode.setJobType(convertType(dssNode.getJobType()));
        JsonObject jobParams=dwsNode.getAsJsonObject("jobContent").getAsJsonObject("jobParams");
        String msgReceiver = getString(jobParams,"msgReceiver");
        String msgTopic = getString(jobParams,"msgTopic");
        String msgName = getString(jobParams,"msgName");
        String queryFrequency = getString(jobParams,"queryFrequency");
        String waitTime = getString(jobParams,"waitTime");
        String msgSavekey = getString(jobParams,"msgSavekey");
        String msgReceToday = getString(jobParams,"msgReceToday");
        Map<String, String> runtimeMap = new HashMap<>();
        runtimeMap.put("msg.type", "RECEIVE");
        runtimeMap.put("msg.receiver", msgReceiver);
        runtimeMap.put("msg.topic", msgTopic);
        runtimeMap.put("msg.name", msgName);
        runtimeMap.put("query.frequency", queryFrequency);
        runtimeMap.put("max.receive.hours", waitTime);
        runtimeMap.put("msg.savekey", msgSavekey);
        runtimeMap.put("only.receive.today", msgReceToday);

        configuration.put("runtime", runtimeMap);

        return dssNode;
    }
}
