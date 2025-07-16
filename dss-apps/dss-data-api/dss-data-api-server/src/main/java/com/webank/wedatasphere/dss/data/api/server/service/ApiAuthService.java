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
package com.webank.wedatasphere.dss.data.api.server.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedatasphere.dss.data.api.server.entity.ApiAuth;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiAuthInfo;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiGroupInfo;
import org.apache.linkis.common.exception.ErrorException;

import java.util.List;

public interface ApiAuthService extends IService<ApiAuth> {
    public List<ApiGroupInfo> getApiGroupList(Long workspaceId);
    public boolean saveApiAuth(ApiAuth apiAuth) throws ErrorException;

    public List<ApiAuthInfo> getApiAuthList(Long workspaceId, String caller, List<Long> totals, Integer pageNow, Integer pageSize);

    public void deleteApiAuth(Long id);


}
