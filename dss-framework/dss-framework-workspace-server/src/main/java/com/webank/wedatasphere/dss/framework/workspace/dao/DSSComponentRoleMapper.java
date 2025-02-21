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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSComponentRole;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspaceComponentPriv;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspaceHomepage;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface DSSComponentRoleMapper extends BaseMapper<DSSComponentRole> {


    public void insertBatch(@Param("list") List<DSSWorkspaceComponentPriv> list);

    List<DSSWorkspaceComponentPriv> queryDSSComponentRole(@Param("workspaceId") int workspaceId,@Param("updateBy") String updateBy);

    void updateDSSComponentRoleById(@Param("list") List<Integer> idList,@Param("updateBy") String updateBy);

}
