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

import com.webank.wedatasphere.dss.framework.workspace.bean.StaffInfo;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceDeptMapper;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceDeptService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DSSWorkspaceDeptServiceImpl implements DSSWorkspaceDeptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSSWorkspaceDeptServiceImpl.class);
    @Autowired
    DSSWorkspaceDeptMapper dssWorkspaceDeptMapper;
    @Override
    public void insertOrUpdateDept(Map<String, StaffInfo> staffInfoMap) {
        if(MapUtils.isEmpty(staffInfoMap)){
            return;
        }
        try {
            Set<String> allDeptName = dssWorkspaceDeptMapper.getAllDeptName();
            Set<String> allOrgFullName = staffInfoMap.values().stream().map(StaffInfo::getOrgFullName).collect(Collectors.toSet());
            List<String> addDeptName = (List<String>)CollectionUtils.subtract(allOrgFullName, allDeptName);
            List<String> deleteDeptName = (List<String>)CollectionUtils.subtract(allDeptName, allOrgFullName);
            if(CollectionUtils.isNotEmpty(addDeptName)){
                dssWorkspaceDeptMapper.insertDept(addDeptName);
            }
            if(CollectionUtils.isNotEmpty(deleteDeptName)){
                dssWorkspaceDeptMapper.deleteDept(deleteDeptName);
            }
        }catch (Exception e){
            LOGGER.error("failed to save Org info to database",e);
        }

    }

}
