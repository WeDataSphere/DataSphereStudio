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
package com.webank.wedatasphere.dss.data.common.conf;

import org.apache.linkis.common.conf.CommonVars;

public interface AtlasConf {
    /**
     * atlas server info
     */
    CommonVars<String> ATLAS_APPLICATION_PROPERTIES = CommonVars.apply("atlas-application.properties");
    CommonVars<String> ATLAS_REST_ADDRESS = CommonVars.apply("atlas.rest.address");
    CommonVars<String> ATLAS_USERNAME = CommonVars.apply("atlas.username");
    CommonVars<String> ATLAS_PASSWORD = CommonVars.apply("atlas.password");
    CommonVars<String> ATLAS_CLUSTER_NAME = CommonVars.apply("atlas.cluster.name","primary");

    /**
     * 系统初始化分类信息： 主题域、系统分层、自定义分层
     */
    CommonVars<String> ATLAS_CLASSIFICATION_SUBJECT = CommonVars.apply("atlas.classification.subject", "subject");
    CommonVars<String> ATLAS_CLASSIFICATION_LAYER_SYSTEM = CommonVars.apply("atlas.classification.layer_system", "layer_system");
    CommonVars<String> ATLAS_CLASSIFICATION_LAYER_USER = CommonVars.apply("atlas.classification.layer", "layer");


    /**
     * hive metastore db
     */
    CommonVars<String> METASTORE_DATASOURCE_DRIVER = CommonVars.apply("metastore.datasource.driver", "com.mysql.jdbc.Driver");
    CommonVars<String> METASTORE_DATASOURCE_URL = CommonVars.apply("metastore.datasource.url");
    CommonVars<String> METASTORE_DATASOURCE_USERNAME = CommonVars.apply("metastore.datasource.username");
    CommonVars<String> METASTORE_DATASOURCE_PASSWORD = CommonVars.apply("metastore.datasource.password");


}
