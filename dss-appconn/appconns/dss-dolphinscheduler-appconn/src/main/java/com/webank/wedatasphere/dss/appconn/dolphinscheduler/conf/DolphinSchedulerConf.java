/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;

public interface DolphinSchedulerConf {

    CommonVars<String> DS_ADMIN_USER = CommonVars.apply("wds.dss.appconn.ds.admin.user", "admin");

    CommonVars<String> DS_ADMIN_TOKEN = CommonVars.apply("wds.dss.appconn.ds.admin.token", "");

    CommonVars<TimeType> DS_TOKEN_EXPIRE_TIME = CommonVars.apply("wds.dss.appconn.ds.token.expire.time", new TimeType("2h"));

    CommonVars<TimeType> DS_TOKEN_EXPIRE_TIME_GAP = CommonVars.apply("wds.dss.appconn.ds.token.expire.gap", new TimeType("1m"));

    CommonVars<String> DS_VERSION = CommonVars.apply("wds.dss.appconn.ds.version", "1.3.9");

    CommonVars<String> DSS_DOLPHINSCHEDULER_CLIENT_HOME = CommonVars.apply("wds.dss.appconn.ds.client.home", "${DSS_DOLPHINSCHEDULER_CLIENT_HOME}");

    CommonVars<String> DOLPHIN_SCHEDULER_URI_PREFIX = CommonVars.apply("wds.dss.appconn.ds.url.prefix", "dolphinscheduler");

    CommonVars<String> LINKIS_1_X_VERSION = CommonVars.apply("wds.dss.appconn.ds.linkis.version", "1.0.0");

}
