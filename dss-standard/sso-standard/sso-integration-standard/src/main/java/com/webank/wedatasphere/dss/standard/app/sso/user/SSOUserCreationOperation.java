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
public abstract class SSOUserCreationOperation<R extends DSSUserContentRequestRef<R>>
        extends SSOUserOperation<R, RefUserContentResponseRef> {

    /**
     * 新增用户操作，当 DSS Admin 模块新建一个用户时，或是该用户第一次登陆到 DSS 时，同步请求第三方 AppConn 创建一个用户。
     * <br/>
     * 如果创建成功，请在 ResponseRef 中返回对应的 refUserId。
     * DSS 不会存储该 refUserId，只所以希望第三方 AppConn 返回 refUserId，是因为 DSS 内嵌的应用工具可能会使用。
     * 如果您确认 DSS 内嵌的应用工具不会使用该 refUserId，您可以不用返回。
     * @param requestRef 请求创建用户 RequestRef
     * @return 如果创建成功，请在 ResponseRef 中返回对应的 refUserId
     * @throws ExternalOperationFailedException 创建失败时报错
     */
    public abstract RefUserContentResponseRef createUser(R requestRef) throws ExternalOperationFailedException;

}
