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
package com.webank.wedatasphere.dss.standard.app.development.utils;

import org.apache.linkis.protocol.util.ImmutablePair;

/**
 * @author enjoyyin
 * @date 2022-04-14
 * @since 1.1.0
 */
public class QueryJumpUrlConstant {

    /**
     * 工作流节点 ID，对应到 DSS 工作流的 nodes 里面对应 node 的 key。
     */
    public static final ImmutablePair<String, String> NODE_ID = new ImmutablePair<>("nodeId", "${nodeId}");

    /**
     * 工作流节点 ID，对应到 DSS 工作流的 nodes 里面对应 node 的 name。
     */
    public static final ImmutablePair<String, String> NODE_NAME = new ImmutablePair<>("nodeName", "${nodeName}");

    /**
     * DSS 工程名。
     */
    public static final ImmutablePair<String, String> PROJECT_NAME = new ImmutablePair<>("projectName", "${projectName}");

}
