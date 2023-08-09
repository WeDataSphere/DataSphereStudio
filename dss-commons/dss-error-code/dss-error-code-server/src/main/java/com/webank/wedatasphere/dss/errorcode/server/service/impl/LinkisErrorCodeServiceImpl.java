/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.dss.errorcode.server.service.impl;

import com.webank.wedatasphere.dss.errorcode.common.LinkisErrorCode;
import com.webank.wedatasphere.dss.errorcode.server.dao.ErrorCodeMapper;
import com.webank.wedatasphere.dss.errorcode.server.entity.ErrorCodeReport;
import com.webank.wedatasphere.dss.errorcode.server.service.LinkisErrorCodeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class LinkisErrorCodeServiceImpl implements LinkisErrorCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeServiceImpl.class);

    @Autowired
    ErrorCodeMapper errorCodeMapper;

    @Override
    public List<LinkisErrorCode> getAllErrorCodes() {
        LOGGER.info("Begin to get all ErrorCodes in server");
        return errorCodeMapper.getAllErrorCodes();
    }

    @Override
    public void addTaskProblem(String requestUrl, String requestBody, String requestHeaders,
                               String queryParams, String responseBody, String errCode, String reportBy, String errorDesc) {
        errorCodeMapper.addTaskProblem(requestUrl, requestBody, requestHeaders, queryParams, responseBody, errCode, reportBy, errorDesc);
    }

    @Override
    public List<ErrorCodeReport> getAllErrorReports() {
        LOGGER.info("Begin to get all ErrorCodes in server");
        return errorCodeMapper.getAllErrorReports();
    }
}
