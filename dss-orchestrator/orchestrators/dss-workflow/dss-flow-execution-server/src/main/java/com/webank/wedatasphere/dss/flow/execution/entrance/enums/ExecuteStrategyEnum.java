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
package com.webank.wedatasphere.dss.flow.execution.entrance.enums;

import java.util.Arrays;

public enum ExecuteStrategyEnum {


    IS_EXECUTE("isExecute", "execute", "执行"),
    IS_RE_EXECUTE("isReExecute", "reExecute", "失败重跑"),
    IS_SELECTED_EXECUTE("isSelectedExecute", "selectedExecute", "选中执行");

    private String name;
    private String value;
    private String desc;

    ExecuteStrategyEnum(String name, String value, String desc) {
        this.name = name;
        this.value = value;
        this.desc = desc;
    }

    public static ExecuteStrategyEnum getEnum(String value) {
        return Arrays.stream(ExecuteStrategyEnum.values()).filter(e -> e.getValue().equals(value)).findFirst().orElseThrow(NullPointerException::new);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
