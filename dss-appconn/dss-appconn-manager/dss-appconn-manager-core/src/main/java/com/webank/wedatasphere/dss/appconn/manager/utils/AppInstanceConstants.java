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

package com.webank.wedatasphere.dss.appconn.manager.utils;

import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.apache.commons.lang.StringUtils;

public class AppInstanceConstants {

    static final String INDEX_FILE_PREFIX = "index_";
    static final String INDEX_FILE_SUFFIX = ".index";

    public static String getHomepageUrl(AppInstance appInstance,
                                        Long workspaceId, String workspaceName) {
        return getHomepageUrl(appInstance.getBaseUrl(), appInstance.getHomepageUri(),
                workspaceId, workspaceName);
    }

    public static String getHomepageUrl(String baseUrl, String homepageUri,
                                        Long workspaceId, String workspaceName) {
        if(StringUtils.isBlank(homepageUri)) {
            return baseUrl;
        }
        if(workspaceId != null && homepageUri.contains("${workspaceId}")) {
            homepageUri = homepageUri.replace("${workspaceId}", workspaceId.toString());
        }
        if(StringUtils.isNotBlank(workspaceName) && homepageUri.contains("${workspaceName}")) {
            homepageUri = homepageUri.replace("${workspaceName}", workspaceName);
        }
        if(baseUrl.endsWith("/")) {
            return baseUrl + homepageUri;
        } else {
            return baseUrl + "/" + homepageUri;
        }
    }

}
