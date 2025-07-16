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
package com.webank.wedatasphere.dss.standard.app.sso.operation;

import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestOperation;
import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestService;
import com.webank.wedatasphere.dss.standard.common.app.AppIntegrationService;
import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.service.Operation;

/**
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public abstract class AbstractOperation<K extends RequestRef, V extends ResponseRef>
        implements Operation<K, V> {

    protected SSORequestOperation ssoRequestOperation;
    protected AppIntegrationService<SSORequestService> service;

    /**
     * This method is used to create a SSORequestOperation.
     * If the third-part AppConn wants to use SSORequestOperation which dependents the HttpClient of Linkis
     * to request the third-part system, please override this method. Otherwise, the operations of
     * third-part AppConn has no necessity to override this method.
     * @return the appConn name
     */
    protected abstract String getAppConnName();

    @Override
    public void init() {
        if(getAppConnName() != null) {
            this.ssoRequestOperation = service.getSSORequestService().createSSORequestOperation(getAppConnName());
        }
    }

    protected String getBaseUrl() {
        return service.getAppInstance().getBaseUrl();
    }

    protected String mergeUrl(String url, String suffix) {
        if(url.endsWith("/")) {
            return url + suffix;
        } else {
            return url + "/" + suffix;
        }
    }

    protected String mergeBaseUrl(String suffix) {
        return mergeUrl(getBaseUrl(), suffix);
    }

    protected String toJson(Object object) {
        if(object == null) {
            return null;
        } else {
            return DSSCommonUtils.COMMON_GSON.toJson(object);
        }
    }

}
