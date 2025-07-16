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
package com.webank.wedatasphere.dss.scriptis.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.ScriptisProxyUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScriptisProxyUserMapper extends BaseMapper<ScriptisProxyUser> {

    List<ScriptisProxyUser> selectProxyUserList(@Param("userName")String userName,@Param("expireTime") String expireTime);

    int insertUser(ScriptisProxyUser user);

    void deleteProxyUser(@Param("userName") String userName, @Param("proxyUserNames") String[] proxyUserNames);


    ScriptisProxyUser selectProxyUserByUser(@Param("userName") String userName,@Param("proxyUserName") String proxyUserName);


    int updateByUser(ScriptisProxyUser user);

}
