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

import com.webank.wedatasphere.dss.common.conf.DSSConfiguration;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.sender.service.DSSSenderService;
import com.webank.wedatasphere.dss.sender.service.impl.DSSSenderServiceImpl;
import org.apache.linkis.rpc.Sender;
import java.util.List;

/**
 * @author allenlliu
 * @date 2021/6/24 17:15
 */
public class DSSSenderServiceInnerImpl extends DSSSenderServiceImpl {

    private final Sender orcSenderProd = Sender.getSender(DSSAdaptorInnerConf.ORCHESTRATOR_SERVER_PROD_NAME.getValue());

    private final Sender workflowSenderProd = Sender.getSender(DSSAdaptorInnerConf.DSS_WORKFLOW_APPLICATION_NAME_PROD.getValue());

    @Override
    public Sender getOrcSender(List<DSSLabel> dssLabels) {
        for(DSSLabel dssLabel : dssLabels){
            String userEnv = null;
            if(dssLabel instanceof EnvDSSLabel){
                userEnv = ((EnvDSSLabel)dssLabel).getEnv();
            }else{
                userEnv = dssLabel.getValue().get(dssLabel.getLabelKey());
            }
            if(DSSConfiguration.ENV_LABEL_VALUE_PROD.equalsIgnoreCase(userEnv)){
                return orcSenderProd;
            }
        }
        return getOrcSender();
    }

    @Override
    public Sender getScheduleOrcSender() {
        return orcSenderProd;
    }

    @Override
    public Sender getWorkflowSender(List<DSSLabel> dssLabels) {
        for(DSSLabel dssLabel : dssLabels){
            String userEnv = null;
            if(dssLabel instanceof EnvDSSLabel){
                userEnv = ((EnvDSSLabel)dssLabel).getEnv();
            }else{
                userEnv = dssLabel.getValue().get(dssLabel.getLabelKey());
            }
            if(DSSConfiguration.ENV_LABEL_VALUE_PROD.equalsIgnoreCase(userEnv)){
                return workflowSenderProd;
            }
        }
        return getWorkflowSender();
    }

    @Override
    public Sender getSchedulerWorkflowSender() {
        return workflowSenderProd;
    }
}
