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
package com.webank.wedatasphere.dss.framework.workspace.util;

public enum CommonMenuEnum {
    /**
     * 中文名需要和数据库插入的一致
     */
    APPLICATION_DEVELOPMENT("应用开发"),
    DATA_ANALYSIS("数据分析"),
    PRODUCTION_OPERATION("生产运维"),
    DATA_QUALITY("数据质量"),
    DATA_EXCHANGE("数据交换"),
    DATA_APPLICATION("数据应用"),
    ADMIN_FUNCTOIN("管理员功能");

    CommonMenuEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
