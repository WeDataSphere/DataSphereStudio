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
    SAVE(OrchestratorRefConstant.FLOW_STATUS_SAVE, "待提交","ready to submit"),
    PUSH(OrchestratorRefConstant.FLOW_STATUS_PUSH, "待发布", "ready to publish"),
    PUBLISH(OrchestratorRefConstant.FLOW_STATUS_PUBLISH, "已发布","already published"),
    RUNNING(OrchestratorRefConstant.FLOW_STATUS_PUSHING, "提交中", "submitting"),
    PUBLISHING(OrchestratorRefConstant.FLOW_STATUS_PUBLISHING, "发布中","publishing"),
    STATELESS(OrchestratorRefConstant.FLOW_STATUS_STATELESS, "--","--"),
    SUCCESS(OrchestratorRefConstant.FLOW_STATUS_PUSH_SUCCESS,"发布成功","published successfully"),
    UNPUBLISHED(OrchestratorRefConstant.FLOW_STATUS_UNPUBLISHED,"未发布","unpublished"),
    FAILED(OrchestratorRefConstant.FLOW_STATUS_PUSH_FAILED, "发布失败","publication failed");


    private String status;
    private String name;
    private String nameEn;

    OrchestratorStatusEnum(String status, String name,String nameEn) {
        this.status = status;
        this.name = name;
        this.nameEn = nameEn;
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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public static List<OrchestratorStatusVo> getOrchestratorGitStatus(boolean isEnglish) {

        return Stream.of(

                new OrchestratorStatusVo(PUSH.getStatus(), isEnglish ? PUSH.getNameEn():PUSH.getName()),
                new OrchestratorStatusVo(SAVE.getStatus(), isEnglish ? SAVE.getNameEn():SAVE.getName()),
                new OrchestratorStatusVo(PUBLISHING.getStatus(), isEnglish ? PUBLISHING.getNameEn(): PUBLISHING.getName()),
                new OrchestratorStatusVo(RUNNING.getStatus(), isEnglish? RUNNING.getNameEn() : RUNNING.getName()),
                new OrchestratorStatusVo(PUBLISH.getStatus(),isEnglish ? PUBLISH.getNameEn() : PUBLISH.getName()),
                new OrchestratorStatusVo(STATELESS.getStatus(), isEnglish ? STATELESS.getNameEn() : STATELESS.getName())

        ).collect(Collectors.toList());
    }

}
