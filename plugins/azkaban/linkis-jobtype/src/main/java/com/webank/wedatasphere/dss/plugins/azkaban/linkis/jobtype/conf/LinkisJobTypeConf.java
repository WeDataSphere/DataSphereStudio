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

package com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.conf;

import org.apache.linkis.common.conf.CommonVars;


public class LinkisJobTypeConf {

    public static final String COMMAND = "command";

    public static final String JOB_ID = "azkaban.job.id";

    public static final String DSS_LABELS_KEY = "labels";

    public static final String FLOW_NAME =  "azkaban.flow.flowid";

    public static final String PROJECT_ID = "azkaban.flow.projectid";

    public static final String PROJECT_NAME = "azkaban.flow.projectname";

    public static final String FLOW_EXEC_ID =  "azkaban.flow.execid";

    public static final String PROXY_USER =  "user.to.proxy";

    public static final String FLOW_SUBMIT_USER = "azkaban.flow.submituser";

    public static final String MSG_SAVE_KEY = "msg.savekey";

    public static final String LINKIS_TYPE_KEY = "linkistype";
    public static final String FLOW_VARIABLE_PREFIX = "flow.variable.";

    public static final String BRANCH_ROUTE_LINKIS_TYPE = "linkis.appconn.branch.route";
    public static final String BRANCH_ROUTE_NODE_ID = "wds.branch.route.node.id";
    public static final String BRANCH_ROUTE_NODE_NAME = "wds.branch.route.node.name";
    public static final String BRANCH_ROUTE_RULE_TEXT = "wds.branch.route.rule.text";
    public static final String BRANCH_ROUTE_TARGETS = "wds.branch.route.targets";
    public static final String BRANCH_GUARD_RULES = "wds.branch.guard.rules";
    public static final String BRANCH_OUTPUT_MAPPING = "node.conf.special.branch.output.mapping";
    public static final String BRANCH_OUTPUT_MAPPING_ALIAS = "node.conf.special.branchOutputMapping";

    public final static CommonVars<String> SIGNAL_NODES = CommonVars.apply("wds.dss.flow.signal.nodes","linkis.appconn.eventchecker.eventreceiver");
    public final static CommonVars<Integer> LOG_MAX_RESULTSIZE = CommonVars.apply("wds.dss.log.max.resultsize",1024);

    public final static CommonVars<String> CONTEXT_ENV_DEV = CommonVars.apply("wds.dss.flow.env.dev","BDAP_DEV");
    public final static CommonVars<String> CONTEXT_ENV_PROD = CommonVars.apply("wds.dss.flow.env.prod","BDAP_PROD");
}
