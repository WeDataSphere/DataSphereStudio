package com.webank.wedatasphere.dss.scriptis.service.impl;

import com.webank.wedatasphere.dss.scriptis.ruler.DataManipulationUserRuler;
import org.springframework.stereotype.Service;
import com.webank.wedatasphere.dss.common.utils.GlobalLimitsUtils;

import java.util.Map;

@Service
public class WebankScriptisAuthServiceImpl extends ScriptisAuthServiceImpl {

    private final DataManipulationUserRuler dataManipulationUserRuler = new DataManipulationUserRuler();

    @Override
    public Map<String, Object> getGlobalLimits(String username) {
        return GlobalLimitsUtils.getAllGlobalLimits();
    }
}
