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

import com.webank.wedatasphere.dss.standard.app.sso.user.ref.DSSUserContentRequestRef;
import com.webank.wedatasphere.dss.standard.app.sso.user.ref.RefUserContentResponseRef;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;

/**
 * @author enjoyyin
 * @date 2022-04-25
 * @since 1.1.0
 */
public abstract class SSOUserGetOperation<R extends DSSUserContentRequestRef<R>>
        extends SSOUserOperation<R, RefUserContentResponseRef> {

    /**
     * 请求第三方 AppConn，获取唯一英文用户名为 username 的 第三方 AppConn 用户信息。
     * 如果第三方 AppConn 不存在该用户，请将 refUserId 置为 null。
     * @param requestRef
     * @return 第三方 AppConn 用户信息，第三方 AppConn 不存在该用户，请将 refUserId 置为 null。
     * @throws ExternalOperationFailedException
     */
    public abstract RefUserContentResponseRef getUser(R requestRef) throws ExternalOperationFailedException;

}
