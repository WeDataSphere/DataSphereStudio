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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.ref;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.constant.Constant;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.utils.DolphinSchedulerHttpUtils;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;

import java.util.Map;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 1.0.0
 */
public class DolphinSchedulerResponseRefBuilder
        extends ResponseRefBuilder.ExternalResponseRefBuilder<DolphinSchedulerResponseRefBuilder, ResponseRefImpl> {

    public static DolphinSchedulerResponseRefBuilder newBuilder() {
        return new DolphinSchedulerResponseRefBuilder();
    }

    private Object data;

    @Override
    public DolphinSchedulerResponseRefBuilder setResponseBody(String responseBody) {
        Map<String, Object> responseMap = DSSCommonUtils.COMMON_GSON.fromJson(responseBody, Map.class);
        status = (int) DolphinSchedulerHttpUtils.parseToLong(responseMap.get("code"));
        if(status != Constant.DS_RESULT_CODE_SUCCESS) {
            errorMsg = (String) responseMap.get("msg");
            throw new ExternalOperationFailedException(90051, "request to DolphinScheduler failed. Caused by: " + errorMsg);
        }
        Object data = responseMap.get("data");
        if(data instanceof Map && ((Map) data).containsKey("totalList")) {
            setResponseMap((Map<String, Object>) data);
        } else {
            this.data = data;
        }
        return super.setResponseBody(responseBody);
    }

    @Override
    protected ResponseRefImpl createResponseRef() {
        if(data != null) {
            return new DolphinSchedulerDataResponseRef(responseBody, status, errorMsg, responseMap, data);
        }
        return new DolphinSchedulerPageInfoResponseRef(responseBody, status, errorMsg, responseMap);
    }
}


