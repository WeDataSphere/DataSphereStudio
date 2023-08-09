package com.webank.wedatasphere.dss.common.server.conf;

import org.apache.linkis.common.conf.CommonVars;

public interface CommonServerConfiguration {
    String DSS_SERVER_RELEASE_VERSION = "v1.1.14";

    CommonVars<String> DSS_DEPLOY_ENV = CommonVars.apply("wds.dss.deploy.env", "DSS_BDAP_SIT");
    CommonVars<String> DSS_SYSTEM_ID = CommonVars.apply("wds.dss.system.id", "5425");
    CommonVars<String> IMS_ALERT_API_URL = CommonVars.apply("wds.dss.ims.alter.api.url", "http://127.0.0.1:10812/ims_data_access/send_alarm_auth.do");
    CommonVars<String> IMS_USER_AUTH_KEY = CommonVars.apply("wds.dss.ims.user.auth.key", "sit_test");


}
