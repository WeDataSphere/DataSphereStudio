package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2023/10/13
 */
public class DSSConnectorNode extends DSSCommonNode{
    private static final Map<String, Object> configuration = new HashMap<>();
    static {
        //默认初始化params.configuration
        Map<String, String> startup = new HashMap<>(1);
        startup.put("ReuseEngine", "true");
        configuration.put("special", new HashMap<>());
        configuration.put("runtime", new HashMap<>());
        configuration.put("startup", startup);
    }


    public static DSSConnectorNode fromDwsNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        DSSConnectorNode dssConnectorNode = DSSCommonUtils.COMMON_GSON.fromJson(dwsNode, DSSConnectorNode.class);
        dssConnectorNode.getParams().put("configuration", configuration);
        dssConnectorNode.setModifyTime(modifyTime);
        dssConnectorNode.setModifyUser(modifyUser);
        dssConnectorNode.setJobType(convertType(dssConnectorNode.getJobType()));
        return dssConnectorNode;
    }
}
