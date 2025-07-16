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
package com.webank.wedatasphere.dss.data.asset.dao;

import com.webank.wedatasphere.dss.data.asset.entity.HivePartInfo;
import com.webank.wedatasphere.dss.data.asset.entity.HiveStorageInfo;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;

@Mapper
public interface MetaInfoMapper {
    Long getTableStorage() throws SQLException;
    List<HiveStorageInfo> getTop10Table() throws SQLException;
    int getTableInfo(@Param("dbName") String dbName,@Param("tableName") String tableName,@Param("isPartTable") Boolean isPartTable) throws  SQLException;

    List<HivePartInfo> getPartInfo(@Param("dbName") String dbName, @Param("tableName") String tableName) throws SQLException;



}
