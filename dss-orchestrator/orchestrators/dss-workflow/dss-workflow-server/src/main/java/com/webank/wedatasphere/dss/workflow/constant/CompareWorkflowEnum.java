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

package com.webank.wedatasphere.dss.workflow.constant;

import java.util.Arrays;

public enum CompareWorkflowEnum {
    same(CompareWorkflow.nodeFlag_same, "相同"),
    delete(CompareWorkflow.nodeFlag_delete,  "删除"),
    add(CompareWorkflow.nodeFlag_add,  "新增"),
    modify(CompareWorkflow.nodeFlag_modify,"修改"),
    ;

    private String type;
    private String name;

    CompareWorkflowEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getNameByType(String type) {
       return Arrays.stream(CompareWorkflowEnum.values())
                .filter(a -> a.getType().equals(type))
                .map(CompareWorkflowEnum::getName)
                .findFirst()
                .orElse(null);
    }
}
