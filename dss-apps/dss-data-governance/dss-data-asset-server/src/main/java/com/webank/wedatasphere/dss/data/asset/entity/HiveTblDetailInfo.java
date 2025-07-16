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
package com.webank.wedatasphere.dss.data.asset.entity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class HiveTblDetailInfo implements Serializable {
    private HiveTblBasicInfo basic;
    private List<HiveColumnInfo> columns;
    private List<HiveColumnInfo> partitionKeys;
    private List<HiveClassificationInfo> classifications;

    @Data
    public static class HiveTblBasicInfo extends HiveTblSimpleInfo {
        private String store;     //存储量
        private Boolean isParTbl;     //是否分区表
        private String tableType;    //Hive表类型 tableType: EXTERNAL_TABLE, MANAGED_TABLE
        private String location;     //Hive表存储路径
    }

    @Data
    public static class HiveColumnInfo {
        private  String name;
        private  String type;
        private  String guid;
        private  String comment;
    }

    @Data
    @AllArgsConstructor
    public static class HiveClassificationInfo {
        private String typeName;
        private Set<String> superTypeNames;
        private Set<String> subTypeNames;
    }
}
