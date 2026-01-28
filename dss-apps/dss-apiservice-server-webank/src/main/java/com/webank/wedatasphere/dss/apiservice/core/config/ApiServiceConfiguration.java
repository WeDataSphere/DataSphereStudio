/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.config;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;

/**
 * @author allenlliu
 * @version 2.0.0
 * @date 2020/08/12 03:46 PM
 */
public class ApiServiceConfiguration {
    public static final CommonVars<String> LINKIS_AUTHOR_USER_TOKEN = CommonVars.apply("wds.linkis.client.api.service.author.user.token", "");
    public static final CommonVars<String> LINKIS_ADMIN_USER = CommonVars.apply("wds.linkis.client.api.service.adminuser", "ws");

    public static final CommonVars<Integer> LINKIS_CONNECTION_TIMEOUT = CommonVars.apply("wds.linkis.flow.connection.timeout", 600000);
    public static final CommonVars<String> LINKIS_API_VERSION = CommonVars.apply("wds.linkis.server.version", "v1");

    public static final CommonVars<String> LINKIS_JOB_CREATOR = CommonVars.apply("wds.linkis.flow.job.creator", "apiservice");

    public static final CommonVars<String> API_SERVICE_TOKEN_KEY = CommonVars.apply("wds.dss.api.service.token.key", "ApiServiceToken");

    public static final CommonVars<String> DSS_API_TOKEN_SECRET_ID = CommonVars.apply("wds.dss.api.service.secret", "LINKISDSSSECRET");

    public static final CommonVars<Integer> LINKIS_JOB_REQUEST_STATUS_TIME = CommonVars.apply("wds.linkis.job.status.timeout", 3000);

    public static final CommonVars<Integer> LOG_ARRAY_LEN = CommonVars.apply("wds.linkis.log.array.len", 4);

    public static final CommonVars<Integer> RESULT_PRINT_SIZE = CommonVars.apply("wds.linkis.result.print.size", 10);

    public static final CommonVars<String> DOWNLOAD_URL = CommonVars.apply("wds.linkis.filesystem.url", "/api/rest_j/v1/filesystem/resultsetToExcel", "fileSystem下载");

    public static final CommonVars<String> LINKIS_URL_1_X = CommonVars.apply("wds.linkis.gateway.url.v1", Configuration.getGateWayURL());

    public static final CommonVars<String> PUBLIC_API_LIST = CommonVars.apply("wds.dss.api.service.public.id.list", "100000,100001");

    public static final CommonVars<Integer> DOWNLOAD_MAX_SIZE = CommonVars.apply("wds.linkis.download.max.size", 1000000);

    public static final CommonVars<Long> API_TOKEN_FOREVER_DURATION = CommonVars.apply("wds.dss.api.token.forever.duration", 7300L);

    public static final CommonVars<Integer> RESULT_ROW_MAX_SIZE = CommonVars.apply("wds.linkis.result.row.max.size",1000000);

    public static final String LINKIS_GATEWAY_URL = CommonVars.apply("wds.linkis.gateway.url.v1", Configuration.getGateWayURL()).getValue();

    public static final String APISERVICE_GET_JOBHISTORY_URL = CommonVars.apply("wds.linkis.api.get.jobhistory","/api/rest_j/v1/jobhistory/list-taskids").getValue();

    public static final String LINKIS_RESOURCE_ADMIN_TOKEN_KEY = CommonVars.apply("wds.dss.linkis.resource.admin.token.key","Token-Code").getValue();

    public static final String LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY = CommonVars.apply("wds.dss.linkis.resource.admin.token.user.key","Token-User").getValue();

    public static final String LINKIS_RESOURCE_ADMIN_TOKEN_VALUE = CommonVars.apply("wds.dss.linkis.resource.admin.token.value","").getValue();

    public static final String APISERVICE_GET_DIRFILE_URL = CommonVars.apply("wds.linkis.api.get.dirfile","/api/rest_j/v1/filesystem/getDirFileTrees").getValue();

    public static final String APISERVICE_OPEN_RESULT_URL = CommonVars.apply("wds.linkis.api.get.result","/api/rest_j/v1/filesystem/openFile").getValue();

    public static final String APISERVICE_GET_RESULTLOCATION_URL = CommonVars.apply("wds.linkis.api.get.resultLocation","/api/rest_j/v1/jobhistory/%s/get").getValue();

    public static final  CommonVars<Integer> APPROVAL_NAME_MAX_SIZE = CommonVars.apply("wds.dss.approval.name.max.size",200);
}
