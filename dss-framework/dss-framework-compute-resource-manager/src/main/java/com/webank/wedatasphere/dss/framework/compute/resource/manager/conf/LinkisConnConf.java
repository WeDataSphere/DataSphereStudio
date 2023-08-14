package com.webank.wedatasphere.dss.framework.compute.resource.manager.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

/**
 * Author: xlinliu
 * Date: 2022/11/25
 */
public interface LinkisConnConf {
    String LINKIS_RESOURCE_ADMIN_TOKEN_VALUE = CommonVars.apply("wds.dss.linkis.resource.admin.token.value","admin-kmsnd").getValue();
    String LINKIS_RESOURCE_ADMIN_TOKEN_KEY = CommonVars.apply("wds.dss.linkis.resource.admin.token.key","Token-Code").getValue();
    String LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY = CommonVars.apply("wds.dss.linkis.resource.admin.token.user.key","Token-User").getValue();
    String LINKIS_URL = CommonVars.apply("wds.linkis.gateway.url.v1", Configuration.getGateWayURL()).getValue();

}
