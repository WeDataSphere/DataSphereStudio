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
package com.webank.wedatasphere.dss.standard.app.sso.user;

import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestService;
import com.webank.wedatasphere.dss.standard.common.app.AppIntegrationService;

/**
 * DSS 用户与第三方 AppConn 的用户同步服务
 * @author enjoyyin
 * @date 2022-04-25
 * @since 1.1.0
 */
public interface SSOUserService extends AppIntegrationService<SSORequestService> {

    /**
     * 用于请求第三方 AppConn 创建同名用户
     * @return
     */
    SSOUserCreationOperation getSSOUserCreationOperation();

    /**
     * 用于修改第三方 AppConn 用户的基础信息
     * @return SSOUserUpdateOperation
     */
    SSOUserUpdateOperation getSSOUserUpdateOperation();

    /**
     * 用于请求第三方 AppConn 获取同名用户信息
     * @return
     */
    SSOUserGetOperation getSSOUserGetOperation();

    /**
     * 预留接口，用于删除第三方 AppConn 用户
     * @return
     */
    SSOUserDeletionOperation getSSOUserDeletionOperation();

}
