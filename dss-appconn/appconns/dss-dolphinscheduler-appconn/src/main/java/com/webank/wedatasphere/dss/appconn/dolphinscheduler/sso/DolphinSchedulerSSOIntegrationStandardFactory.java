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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.sso;

import com.webank.wedatasphere.dss.standard.app.sso.SSOIntegrationStandard;
import com.webank.wedatasphere.dss.standard.app.sso.SSOIntegrationStandardFactory;
import com.webank.wedatasphere.dss.standard.app.sso.origin.HttpSSOIntegrationStandard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 1.1.0
 */
public class DolphinSchedulerSSOIntegrationStandardFactory implements SSOIntegrationStandardFactory {

    private SSOIntegrationStandard ssoIntegrationStandard;
    private Logger logger = LoggerFactory.getLogger(DolphinSchedulerSSOIntegrationStandardFactory.class);

    @Override
    public void init() {
        ssoIntegrationStandard = new HttpSSOIntegrationStandard();
        logger.info("DolphinScheduler AppConn will use {} to integrate with DSS in 1st SSO standard.", ssoIntegrationStandard.getClass().getName());
    }

    @Override
    public SSOIntegrationStandard getSSOIntegrationStandard() {
        return ssoIntegrationStandard;
    }
}
