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

import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.service.Operation;

/**
 * 这是一个可选的 Operation，用于协助第三方AppConn想要实现一些特殊的Operation，这些Operation本身并不属于
 * 三大规范强制要求要实现的 Operation，但是会提供给三大规范或是DSS内嵌的应用工具使用。
 * @author enjoyyin
 * @date 2022-03-18
 * @since 1.1.0
 */
public interface OptionalOperation<K extends RequestRef, V extends ResponseRef> extends Operation<K, V> {

    String getOperationName();

    void setOptionalService(OptionalService optionalService);

    V apply(K requestRef);

}
