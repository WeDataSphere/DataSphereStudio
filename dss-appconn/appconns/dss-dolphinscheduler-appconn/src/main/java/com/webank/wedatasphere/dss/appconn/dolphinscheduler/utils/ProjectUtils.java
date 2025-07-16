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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils;

import org.apache.commons.lang3.StringUtils;

public class ProjectUtils {

    /**
     * 根据DSS空间名和项目名生成DS项目名：DSS空间名-DSS项目名
     *
     * @param workspaceName
     * @param projectName
     * @return
     */
    public static String generateDolphinProjectName(String workspaceName, String projectName) {
        return StringUtils.joinWith("-", workspaceName, projectName);
    }

}
