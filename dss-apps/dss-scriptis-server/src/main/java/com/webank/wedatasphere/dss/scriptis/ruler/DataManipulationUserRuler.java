package com.webank.wedatasphere.dss.scriptis.ruler;

import com.google.common.collect.Lists;
import org.apache.linkis.server.conf.ServerConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权_c用户globallimits中结果集相关操作权限。
 */
public class DataManipulationUserRuler {

    private static final List<String> authList = Lists.newArrayList("exportResEnable", "downloadResEnable", "resCopyEnable");

    public Map<String, Object> rule(String username, Map<String, Object> params) {
        //LINKIE_USERNAME_SUFFIX_ENABLE为true时才生效
        if (ServerConfiguration.LINKIE_USERNAME_SUFFIX_ENABLE() && !username.endsWith(ServerConfiguration.LINKIE_USERNAME_SUFFIX_NAME())) {
            return params;
        }
        Map<String, Object> resMap = new HashMap<>(params);
        authList.forEach(auth -> resMap.put(auth, true));
        return resMap;
    }

}
