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
package com.webank.wedatasphere.dss.scriptis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedatasphere.dss.scriptis.dao.DssAuditMapper;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.DssScriptDownloadAudit;
import com.webank.wedatasphere.dss.scriptis.service.DssScriptDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DssScriptDownloadServiceImpl extends ServiceImpl<DssAuditMapper, DssScriptDownloadAudit> implements DssScriptDownloadService {
    @Autowired
    public DssAuditMapper dssAuditMapper;

    @Override
    public List<DssScriptDownloadAudit> getDownloadAuditList(String userName, String startIme, String endTime) {
        return dssAuditMapper.getDownloadAuditList(userName, startIme, endTime);
    }

}
