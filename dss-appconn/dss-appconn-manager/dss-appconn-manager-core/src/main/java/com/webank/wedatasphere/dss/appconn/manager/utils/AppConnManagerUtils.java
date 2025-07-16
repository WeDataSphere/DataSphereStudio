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

import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;
import org.apache.linkis.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author enjoyyin
 * @date 2022-06-24
 * @since 1.1.0
 */
public class AppConnManagerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConnManagerUtils.class);

    /**
     * 主动加载所有的 AppConn，用于提高性能
     */
    public static void autoLoadAppConnManager() {
        TimeType delay = CommonVars.apply("wds.dss.appconn.auto-load.delay", new TimeType("20m")).getValue();
        LOGGER.info("We will try to auto-load all AppConns if no usage lasting {}.", delay.toString());
        Utils.defaultScheduler().schedule(() -> {
            LOGGER.info("Try to auto-load all AppConns since no usage lasting {}.", delay.toString());
            try {
                AppConnManager.getAppConnManager();
            } catch (Exception e) {
                LOGGER.info("Load AppConns failed.", e);
                return;
            }
            LOGGER.info("All AppConns have loaded successfully.");
        }, delay.toLong(), TimeUnit.MILLISECONDS);
    }
}
