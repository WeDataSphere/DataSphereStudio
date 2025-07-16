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
package com.webank.wedatasphere.dss.orchestrator.server.constant;

import com.webank.wedatasphere.dss.orchestrator.common.ref.OrchestratorRefConstant;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.OrchestratorStatusVo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OrchestratorStatusEnum {
    SAVE(OrchestratorRefConstant.FLOW_STATUS_SAVE, "待提交"),
    PUSH(OrchestratorRefConstant.FLOW_STATUS_PUSH, "待发布"),
    PUBLISH(OrchestratorRefConstant.FLOW_STATUS_PUBLISH, "已发布"),
    RUNNING(OrchestratorRefConstant.FLOW_STATUS_PUSHING, "提交中"),
    PUBLISHING(OrchestratorRefConstant.FLOW_STATUS_PUBLISHING, "发布中"),
    STATELESS(OrchestratorRefConstant.FLOW_STATUS_STATELESS, "--"),
    SUCCESS(OrchestratorRefConstant.FLOW_STATUS_PUSH_SUCCESS,"发布成功"),
    UNPUBLISHED(OrchestratorRefConstant.FLOW_STATUS_UNPUBLISHED,"未发布"),
    FAILED(OrchestratorRefConstant.FLOW_STATUS_PUSH_FAILED, "发布失败");


    private String status;
    private String name;

    OrchestratorStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }


    public static OrchestratorStatusEnum getEnum(String status) {
        if (status == null) {
            return STATELESS;
        }
        for (OrchestratorStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return STATELESS;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static List<OrchestratorStatusVo> getOrchestratorGitStatus() {

        return Stream.of(
                new OrchestratorStatusVo(PUSH.getStatus(), PUSH.getName()),
                new OrchestratorStatusVo(SAVE.getStatus(), SAVE.getName()),
                new OrchestratorStatusVo(PUBLISHING.getStatus(), PUBLISHING.getName()),
                new OrchestratorStatusVo(RUNNING.getStatus(), RUNNING.getName()),
                new OrchestratorStatusVo(PUBLISH.getStatus(),PUBLISH.getName()),
                new OrchestratorStatusVo(STATELESS.getStatus(), STATELESS.getName())
        ).collect(Collectors.toList());
    }

}
