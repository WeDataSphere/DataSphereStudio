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
package com.webank.wedatasphere.dss.sender.service.impl;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.sender.service.DSSSenderService;
import com.webank.wedatasphere.dss.sender.service.conf.DSSSenderServiceConf;
import org.apache.linkis.rpc.Sender;

import java.util.List;

public class DSSServerSenderServiceImpl implements DSSSenderService {
    private final Sender dssServerSender = Sender.getSender(DSSSenderServiceConf.DSS_SERVER_NAME.getValue());

    @Override
    public Sender getOrcSender() {
        return dssServerSender;
    }

    @Override
    public Sender getOrcSender(List<DSSLabel> dssLabels) {
        return dssServerSender;
    }

    @Override
    public Sender getScheduleOrcSender() {
        return dssServerSender;
    }

    @Override
    public Sender getWorkflowSender(List<DSSLabel> dssLabels) {
        return dssServerSender;
    }

    @Override
    public Sender getWorkflowSender() {
        return dssServerSender;
    }

    @Override
    public Sender getSchedulerWorkflowSender() {
        return dssServerSender;
    }

    @Override
    public Sender getProjectServerSender() {
        return dssServerSender;
    }

    @Override
    public Sender getGitSender() {
        return dssServerSender;
    }
}
