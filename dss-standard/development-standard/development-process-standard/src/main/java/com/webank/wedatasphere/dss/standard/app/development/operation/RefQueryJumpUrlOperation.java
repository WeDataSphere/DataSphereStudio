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
package com.webank.wedatasphere.dss.standard.app.development.operation;

import com.webank.wedatasphere.dss.standard.app.development.ref.QueryJumpUrlRequestRef;
import com.webank.wedatasphere.dss.standard.app.development.ref.QueryJumpUrlResponseRef;

/**
 *
 * 关于 QueryJumpUrlRequestRef，可参考 {@code QueryJumpUrlRequestRef}。
 * 返回的 {@code ResponseRef} 为：{@code QueryJumpUrlResponseRef}，用于当用户在前端双击该工作流节点时，返回一个可以跳转的 jumpURL；
 * 否则请返回一个带有错误信息的 ResponseRef，例如：ResponseRef.newExternalBuilder().error("error msg.")
 *
 * @author enjoyyin
 * @date 2022-04-14
 * @since 1.1.0
 */
public interface RefQueryJumpUrlOperation<K extends QueryJumpUrlRequestRef<K>,
        V extends QueryJumpUrlResponseRef> extends RefQueryOperation<K, V> {
}
