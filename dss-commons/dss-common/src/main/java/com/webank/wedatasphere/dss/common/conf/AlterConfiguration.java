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
package com.webank.wedatasphere.dss.common.conf;

import com.webank.wedatasphere.dss.common.alter.CustomAlterServiceImpl;
import com.webank.wedatasphere.dss.common.alter.ExceptionAlterSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AlterConfiguration.class);

    private static final ExceptionAlterSender ALTER = createAlter();

    private static ExceptionAlterSender createAlter() {

        String alterClassName = DSSCommonConf.ALTER_CLASS.getValue();

        try {
            logger.info("Use user config Alter {}", alterClassName);
            return (ExceptionAlterSender) AlterConfiguration.class.getClassLoader().loadClass(alterClassName).newInstance();
        } catch (Exception e) {
            logger.warn("Use CustomAlter {}", alterClassName, e);
            return new CustomAlterServiceImpl();
        }

    }

    public static ExceptionAlterSender getAlter() {
        return ALTER;
    }

}
