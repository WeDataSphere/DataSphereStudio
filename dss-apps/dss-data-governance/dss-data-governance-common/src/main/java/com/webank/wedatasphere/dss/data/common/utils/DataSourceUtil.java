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
package com.webank.wedatasphere.dss.data.common.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.webank.wedatasphere.dss.data.common.conf.AtlasConf;

public class DataSourceUtil {
    private static volatile DruidDataSource druidDataSource =null;

    private DataSourceUtil(){}

    public static DruidDataSource getDataSource(){
        if(druidDataSource ==null) {
            synchronized (DataSourceUtil.class){
                if(druidDataSource ==null){
                    druidDataSource = new DruidDataSource();
                    druidDataSource.setDriverClassName(AtlasConf.METASTORE_DATASOURCE_DRIVER.getValue());
                    druidDataSource.setUrl(AtlasConf.METASTORE_DATASOURCE_URL.getValue());
                    druidDataSource.setUsername(AtlasConf.METASTORE_DATASOURCE_USERNAME.getValue());
                    druidDataSource.setPassword(AtlasConf.METASTORE_DATASOURCE_PASSWORD.getValue());
                }
            }
        }
        return druidDataSource;
    }

}
