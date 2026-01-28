/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.service;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.rpc.Sender;

/**
 * @author allenlliu
 * @date 2021/6/24 17:23
 */
public class DSSAdaptorInnerConf {

    public static final CommonVars<String> ORCHESTRATOR_SERVER_PROD_NAME =
            CommonVars.apply("wds.dss.orc.server.prod.name", "DSS-Framework-Orchestrator-Server-Prod");

    public static final CommonVars<String> DSS_WORKFLOW_APPLICATION_NAME_PROD =
            CommonVars.apply("wds.dss.workflow.name.prod", "dss-workflow-server-prod");

    public static final CommonVars<String> DSS_SERVER_NAME_PROD =
            CommonVars.apply("wds.dss.server.name.prod", "dss-server-prod");


}
