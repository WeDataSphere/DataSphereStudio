package com.webank.wedatasphere.dss.scriptis.config;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

/**
 * Author: xlinliu
 * Date: 2022/12/2
 */
public interface WebankDSSScriptisConfiguration {
    CommonVars<String> SUBSCRIBE_SUBSYSTEM_ID = CommonVars.apply("wds.dss.scriptis.subscribe.subsystem.id","5425");
    CommonVars<String> ITSM_SECRETKEY = CommonVars.apply("wds.dss.itsm.secretkey", "350965f1d6dfc38757cba3c34478163176aafcb2ed5ff2478d94a43b40d3ae42");
    String LINKIS_URL = CommonVars.apply("wds.linkis.gateway.url.v1", Configuration.getGateWayURL()).getValue();

}
