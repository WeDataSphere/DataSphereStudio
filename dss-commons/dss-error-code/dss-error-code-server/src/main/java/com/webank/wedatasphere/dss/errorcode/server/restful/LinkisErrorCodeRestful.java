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

package com.webank.wedatasphere.dss.errorcode.server.restful;

import com.webank.wedatasphere.dss.errorcode.common.CommonConf;
import com.webank.wedatasphere.dss.errorcode.common.LinkisErrorCode;
import com.webank.wedatasphere.dss.errorcode.common.utils.GsonHelper;
import com.webank.wedatasphere.dss.errorcode.server.entity.ErrorCodeReport;
import com.webank.wedatasphere.dss.errorcode.server.service.LinkisErrorCodeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;

import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/dss/guide")
public class LinkisErrorCodeRestful {

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeRestful.class);

    @Autowired
    private LinkisErrorCodeService linkisErrorCodeService;
    final Object lock = new Object();
    String str = "jobhistory/(\\d+)/get";
    Pattern jobGetPattern = Pattern.compile(str);



    @RequestMapping(path = "/errorcode/" + CommonConf.GET_ERRORCODE_URL, method = RequestMethod.GET)
    public Message getErrorCodes(HttpServletRequest request) {
        List<LinkisErrorCode> errorCodes = linkisErrorCodeService.getAllErrorCodes();
        Message message = Message.ok();
        message.data("errorCodes", errorCodes);
        return message;
    }

    @RequestMapping(path = "/solution/reportProblem", method = RequestMethod.POST)
    public Message report(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        String user = SecurityFilter.getLoginUsername(request);
        String requestUrl = (String) map.get("requestUrl");
        Map requestBody = (Map) map.get("requestBody");
        Map requestHeaders = (Map) map.get("requestHeaders");
        Map responseBody = (Map) map.get("responseBody");
        Map queryParams = (Map) map.get("queryParams");

//        if (requestUrl.contains("jobhistory") || responseBody.containsKey("taskID")) {
        if (jobGetPattern.matcher(requestUrl).find()) {
            String errDesc = (String) responseBody.get("errDesc");
            String errCode = String.valueOf(responseBody.get("errCode"));
            String responseBodyJson = toJson(responseBody);
            linkisErrorCodeService.addTaskProblem(requestUrl, toJson(requestBody), toJson(requestHeaders),
                    toJson(queryParams), responseBodyJson, errCode, user, errDesc);
            ErrorCodeReport report = new ErrorCodeReport();
            report.setUri(requestUrl);
            report.setErrCode(errCode);
            report.setResponseBody(responseBodyJson);
            return Message.ok();
        }
        Map data = (Map) responseBody.get("data");
        String responseBodyJson = toJson(responseBody);
        String errCode = "";
        String errDesc = "";
        if (data != null && data.containsKey("errorMsg")) {
            Map errorMsg = (Map) data.get("errorMsg");
            errCode = String.valueOf(errorMsg.get("errCode"));
            errDesc = (String) responseBody.get("errDesc");

        }
        linkisErrorCodeService.addTaskProblem(requestUrl, toJson(requestBody), toJson(requestHeaders), toJson(queryParams), responseBodyJson, errCode, user, errDesc);
        return Message.ok();
    }

    @RequestMapping(path = "/solution/getAllProblemReport", method = RequestMethod.GET)
    public Message getAllProblemReport(HttpServletRequest request, @RequestParam("startTime")Long startTime,
                          @RequestParam("endTime")Long endTime) {
        String user = SecurityFilter.getLoginUsername(request);
        List<ErrorCodeReport>  reportList= linkisErrorCodeService.getAllErrorReports(user, new Date(startTime),
                new Date(endTime));
        return Message.ok().data("reportList", reportList);
    }


    String toJson(Object val) {
        return GsonHelper.toJson(val);
    }
}


