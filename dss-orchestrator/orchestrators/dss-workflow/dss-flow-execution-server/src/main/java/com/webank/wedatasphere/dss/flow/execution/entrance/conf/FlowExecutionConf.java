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
package com.webank.wedatasphere.dss.flow.execution.entrance.conf;

import org.apache.linkis.common.conf.CommonVars;

/**
 * Author: xlinliu
 * Date: 2023/8/25
 */
public interface FlowExecutionConf {
    CommonVars<Boolean> DSS_EXECUTE_BY_PROXY_USER_ENABLE = CommonVars.apply("wds.dss.flowexecution.execute.byproxyuser.enable", Boolean.FALSE);
}
