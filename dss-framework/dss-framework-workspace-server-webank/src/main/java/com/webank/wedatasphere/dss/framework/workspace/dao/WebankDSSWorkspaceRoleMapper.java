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

package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.bean.WebankDSSWorkspaceUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @Author: bradyli
* @Date: 2021/11/3
* @Description: 修改角色
* @Param:
* @return:
**/
@Mapper
public interface WebankDSSWorkspaceRoleMapper {
    /**
    * @Author: bradyli
    * @Date: 2021/11/3
    * @Description: 更新用户在工作空间中的角色信息，由transferor更改为recipient
    * @Param:
    * @return:
    **/
    void updateUserNameWithWorkSpaceBatch(@Param("workspaceIdList") List<Long> workspaceIdList, @Param("transferor") String transferor, @Param("recipient") String recipient);

    /**
     * @Author: bradyli
     * @Date: 2021/11/3
     * @Description: 更新用户在工作空间中的角色信息，由transferor更改为recipient
     * @Param:
     * @return:
     **/
    void updateUserNameWithWorkSpace(@Param("workspaceId") Long workspaceId, @Param("transferor") String transferor, @Param("recipient") String recipient);

    /**
    * @Author: bradyli
    * @Date: 2021/11/3
    * @Description: 根据transferor插入recipient的数据
    * @Param:
    * @return:
    **/
    void insertUserNameWithWorkSpaceBatch(@Param("workspaceId") Long workspaceId, @Param("transferor") String transferor, @Param("recipient") String recipient);

    /**
     * @Author: bradyli
     * @Date: 2021/11/3
     * @Description: 删除用户对应的工作空间的角色信息
     * @Param:
     * @return:
     **/
     void deleteWorkspaceRoleOfUser(@Param("workspaceId") Long workspaceId, @Param("userName") String userName);
     
     /**
     * @Author: bradyli
     * @Date: 2021/11/4
     * @Description: 
     * @Param: 
     * @return:  
     **/
     List<WebankDSSWorkspaceUserRole> queryUserRoleInWorkSpaces(@Param("workspaceIdList") List<Long> workspaceIdList, @Param("userName") String userName);

    /**
     * @Author: bradyli
     * @Date: 2021/11/4
     * @Description:
     * @Param:
     * @return:
     **/
     void insertUserRoleList(@Param("dssWorkspaceUserRoleList") List<WebankDSSWorkspaceUserRole> dssWorkspaceUserRoleList);
}
