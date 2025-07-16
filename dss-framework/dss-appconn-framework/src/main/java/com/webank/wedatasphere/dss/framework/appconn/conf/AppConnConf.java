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
package com.webank.wedatasphere.dss.framework.appconn.conf;

import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.conf.CommonVars;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppConnConf {

    public static final CommonVars<String> PROJECT_QUALITY_CHECKER_IGNORE_LIST = CommonVars.apply("wds.dss.appconn.checker.project.ignore.list", "");

    public static final CommonVars<String> DEVELOPMENT_QUALITY_CHECKER_IGNORE_LIST = CommonVars.apply("wds.dss.appconn.checker.development.ignore.list", "");

    public static final CommonVars<Integer> APPCONN_UPLOAD_THREAD_NUM = CommonVars.apply("wds.dss.appconn.upload.thread.num", 2);

    public static final List<String> DISABLED_APP_CONNS = getDisabledAppConns();

    private static List<String> getDisabledAppConns() {
        String disabledAppConns = CommonVars.apply("wds.dss.appconn.disabled", "").getValue();
        if(StringUtils.isBlank(disabledAppConns)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(disabledAppConns.split(","));
        }
    }

}
