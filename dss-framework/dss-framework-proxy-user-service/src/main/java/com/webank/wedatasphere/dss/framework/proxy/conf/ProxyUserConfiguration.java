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
package com.webank.wedatasphere.dss.framework.proxy.conf;

import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import org.apache.linkis.common.conf.CommonVars;

/**
 * @author enjoyyin
 * @date 2022-09-05
 * @since 0.5.0
 */
public class ProxyUserConfiguration {

    public static boolean isProxyUserEnable() {
        return CommonVars.apply(DSSCommonConf.ALL_GLOBAL_LIMITS_PREFIX.acquireNew() + "proxyEnable", false).acquireNew();
    }

    public static final CommonVars<String> DS_TRUST_TOKEN = CommonVars.apply("wds.dss.trust.token", "");
    public static final CommonVars<Boolean> DS_PROXY_SELF_ENABLE = CommonVars.apply("wds.dss.proxy.self.enable", true);

}
