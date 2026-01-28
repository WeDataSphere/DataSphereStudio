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

package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspace;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceMapper;
import com.webank.wedatasphere.dss.framework.workspace.service.WebankDSSWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @Author: bradyli
* @Date: 2021/12/7
* @Description: 
* @Param: 
* @return:  
**/
@Service
public class WebankDSSWorkspaceServiceImpl implements WebankDSSWorkspaceService {

    @Autowired
    private DSSWorkspaceMapper dssWorkspaceMapper;

    @Override
    public List<DSSWorkspace> getWorkspaces(String username) throws Exception {
        List<DSSWorkspace> dssWorkspacesList = dssWorkspaceMapper.getWorkspaces(username);
        return dssWorkspacesList;
    }
}
