package com.webank.wedatasphere.dss.framework.workspace.esb.conf;

import org.apache.linkis.common.conf.CommonVars;

/**
 * created by cooperyang on 2020/4/2
 * Description:
 */
public interface EsbConf {

    CommonVars<String> ESB_HTTP_URL =
            CommonVars.apply("wds.dss.esb.http.url", "http://10.38.240.10");

    CommonVars<String> ESB_APPID =
            CommonVars.apply("wds.dss.esb.appid", "67300697");

    CommonVars<String> ESB_TOKEN =
            CommonVars.apply("wds.dss.esb.token", "ytD06uaAhB");


    CommonVars<String> ESB_HR_STAFF_URL =
            CommonVars.apply("wds.dss.esb.hr.staff.url", "/service/hr/hrgetstaff");

    String DEFAULT_SPLIT = "-";

}
