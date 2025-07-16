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
package com.webank.wedatasphere.dss.standard.app.structure.optional;

import com.webank.wedatasphere.dss.standard.app.sso.operation.AbstractOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;

/**
 * @author enjoyyin
 * @date 2022-03-18
 * @since 0.5.0
 */
public abstract class AbstractOptionalOperation<K extends RequestRef, V extends ResponseRef>
        extends AbstractOperation<K, V> implements OptionalOperation<K, V> {

    /**
     * for more detail, please access the super explanation.
     * @return null if you don't want to use SSORequestOperation; otherwise, a AppConn name is needed.
     */
    @Override
    protected String getAppConnName() {
        return null;
    }

    @Override
    public final void setOptionalService(OptionalService optionalService) {
        service = optionalService;
    }

}
