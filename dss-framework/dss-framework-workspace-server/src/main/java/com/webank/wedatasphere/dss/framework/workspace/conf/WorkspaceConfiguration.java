package com.webank.wedatasphere.dss.framework.workspace.conf;

import org.apache.linkis.common.conf.CommonVars;

/**
 * Author: xlinliu
 * Date: 2022/12/27
 */
public interface WorkspaceConfiguration {
    Boolean DSS_NEED_INIT_USER_STAGE = CommonVars.apply("wds.dss.server.need.init.user.stage",false).getValue();
}
