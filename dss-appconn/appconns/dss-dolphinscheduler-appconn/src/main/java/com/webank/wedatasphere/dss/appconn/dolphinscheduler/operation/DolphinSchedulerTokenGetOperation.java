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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.operation;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.DolphinSchedulerAppConn;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.sso.DolphinSchedulerTokenManager;
import com.webank.wedatasphere.dss.common.utils.MapUtils;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.optional.AbstractOptionalOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;

/**
 * @author enjoyyin
 * @date 2022-03-18
 * @since 1.1.0
 */
public class DolphinSchedulerTokenGetOperation extends AbstractOptionalOperation<StructureRequestRef, ResponseRef> {

    @Override
    protected String getAppConnName() {
        return DolphinSchedulerAppConn.DOLPHINSCHEDULER_APPCONN_NAME;
    }

    @Override
    public String getOperationName() {
        return "getToken";
    }

    @Override
    public ResponseRef apply(StructureRequestRef ref) {
        String token = DolphinSchedulerTokenManager.getDolphinSchedulerTokenManager(getBaseUrl()).getToken(ref.getUserName());
        long expireTime = DolphinSchedulerTokenManager.getDolphinSchedulerTokenManager(getBaseUrl()).getTokenExpireTime(ref.getUserName());
        return ResponseRef.newExternalBuilder().setResponseMap(MapUtils.newCommonMap("token", token, "expireTime", expireTime)).success();
    }

}
