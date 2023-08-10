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

package com.webank.wedatasphere.dss.apiservice.core.config;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;


public class ApiServiceConfiguration {
    public final static CommonVars<String> LINKIS_AUTHOR_USER_TOKEN = CommonVars.apply("wds.linkis.client.api.service.author.user.token","WS-AUTH");
    public final static CommonVars<String> LINKIS_ADMIN_USER = CommonVars.apply("wds.linkis.client.api.service.adminuser","ws");

    public final static  CommonVars<Integer>  LINKIS_CONNECTION_TIMEOUT = CommonVars.apply("wds.linkis.flow.connection.timeout",30000);
    public final static CommonVars<String> LINKIS_API_VERSION = CommonVars.apply("wds.linkis.server.version","v1");
    public final static CommonVars<String> LINKIS_JOB_CREATOR = CommonVars.apply("wds.linkis.flow.job.creator", "apiservice");

    public final static CommonVars<String> API_SERVICE_TOKEN_KEY = CommonVars.apply("wds.dss.api.service.token.key","ApiServiceToken");


    public final static CommonVars<String> DSS_API_TOKEN_SECRET_ID = CommonVars.apply("wds.dss.api.service.secret", "DSSSECRETTEST001002");

    public final static  CommonVars<Integer>  LINKIS_JOB_REQUEST_STATUS_TIME = CommonVars.apply("wds.linkis.job.status.timeout",3000);

    public final static CommonVars<Integer> LOG_ARRAY_LEN = CommonVars.apply("wds.linkis.log.array.len",4);

    public final static CommonVars<Integer> RESULT_PRINT_SIZE = CommonVars.apply("wds.linkis.result.print.size",10);

    public final static CommonVars<String> DOWNLOAD_URL = CommonVars.apply("wds.linkis.filesystem.url", "/api/rest_j/v1/filesystem/resultsetToExcel", "fileSystem下载");

    public final static CommonVars<String> LINKIS_URL_1_X = CommonVars.apply("wds.linkis.gateway.url.v1", Configuration.getGateWayURL());

    public final static CommonVars<String> PUBLIC_API_LIST = CommonVars.apply("wds.dss.api.service.public.id.list", "100000,100001");

    public final static CommonVars<Integer> DOWNLOAD_MAX_SIZE = CommonVars.apply("wds.linkis.download.max.size", 5000);

    public final static CommonVars<Long> API_TOKEN_FOREVER_DURATION = CommonVars.apply("wds.dss.api.token.forever.duration", 7300L);

}
