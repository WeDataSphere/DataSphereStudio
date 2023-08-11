package com.webank.wedatasphere.dss.scriptis.service.impl;

import com.webank.wedatasphere.dss.common.utils.GlobalLimitsUtils;
import com.webank.wedatasphere.dss.scriptis.ruler.DataManipulationUserRuler;
import com.webank.wedatasphere.dss.scriptis.service.ScriptisAuthService;

import java.util.Map;

public class ScriptisAuthServiceImpl implements ScriptisAuthService {

    private final DataManipulationUserRuler dataManipulationUserRuler = new DataManipulationUserRuler();

    @Override
    public Map<String, Object> getGlobalLimits(String username) {
        Map<String, Object> globalLimits = GlobalLimitsUtils.getAllGlobalLimits();
        return dataManipulationUserRuler.rule(username, globalLimits);
    }
}
