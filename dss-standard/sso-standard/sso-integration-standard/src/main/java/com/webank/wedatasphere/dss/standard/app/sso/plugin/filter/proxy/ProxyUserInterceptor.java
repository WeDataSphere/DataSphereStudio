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
package com.webank.wedatasphere.dss.standard.app.sso.plugin.filter.proxy;

import com.webank.wedatasphere.dss.standard.app.sso.plugin.filter.UserInterceptor;

/**
 * @author enjoyyin
 * @date 2022-09-02
 * @since 0.5.0
 */
public interface ProxyUserInterceptor extends UserInterceptor {

    default ProxyUserType getProxyUserType() {
        return ProxyUserType.ONLY_PROXY_USER;
    }

    enum ProxyUserType {
        /**
         * 该第三方应用只支持代理用户，即：如果DSS开启了用户登录+切换到代理用户的双用户体系之后，该第三方应用为了
         * 简化使用，只接受DSS传递代理用户给第三方系统，将不接受实际登录的用户，只使用代理用户进行所有操作。
         * 注意：如果用户不显示指定，则该模式为第三方应用的默认模式。
         */
        ONLY_PROXY_USER,
        /**
         * 该第三方应用支持 登录用户 + 切换到代理用户的双用户体系。
         * DSS 会同时传递登录用户和代理用户给第三方应用，由第三方应用自行决定如何使用这两个用户。
         */
        USER_WITH_PROXY_USER
    }
}
