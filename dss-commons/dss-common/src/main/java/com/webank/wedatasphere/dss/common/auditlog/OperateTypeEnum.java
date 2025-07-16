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
package com.webank.wedatasphere.dss.common.auditlog;

/**
 * 审计日志操作类型枚举
 * Author: xlinliu
 * Date: 2022/8/10
 */
public enum OperateTypeEnum {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    COPY("copy"),
    PUBLISH("publish"),
    DISABLE("disable"),
    ENABLE("enable"),
    ADD_TO_FAVORITES("add_to_favorites"),
    REM_FROM_FAVORITES("rem_from_favorites"),
    UPDATE_ROLE_MENU("update_role_menu"),
    UPDATE_ROLE_COMPONENT("update_role_component"),
    ADD_USERS("add_users"),
    UPDATE_USERS("update_users"),
    KILL("kill"),
    SEND_EMAIL("send_email"),


    ;

    private String name;

    OperateTypeEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
