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
import com.google.gson.JsonObject;

import com.webank.wedatasphere.dss.data.api.server.entity.ApiConfig;
import com.webank.wedatasphere.dss.data.api.server.entity.ApiGroup;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiExecuteInfo;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiGroupInfo;
import com.webank.wedatasphere.dss.data.api.server.exception.DataApiException;
import org.codehaus.jettison.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ApiConfigService extends IService<ApiConfig> {

    Boolean release(Integer status, String apiId);

    void addGroup(ApiGroup apiGroup);

    List<ApiGroupInfo> getGroupList(String workspaceId);

    void saveApi(ApiConfig apiConfig) throws JSONException, DataApiException;

    ApiExecuteInfo apiTest(String path, HttpServletRequest httpRequest, Map<String, Object> map,boolean isTest) throws  Exception;

    ApiExecuteInfo apiExecute(String path, HttpServletRequest request, Map<String, Object> map) throws Exception;
}
