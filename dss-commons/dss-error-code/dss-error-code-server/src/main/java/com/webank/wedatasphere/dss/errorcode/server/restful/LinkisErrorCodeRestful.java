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
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/dss/guide")
public class LinkisErrorCodeRestful {

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeRestful.class);

    @Autowired
    private LinkisErrorCodeService linkisErrorCodeService;

    List<ErrorCodeReport> reportList = null;
    final Object lock = new Object();
    String str = "jobhistory/(\\d+)/get";
    Pattern jobGetPattern = Pattern.compile(str);

    @PostConstruct
    public void init() {
        reportList = linkisErrorCodeService.getAllErrorReports();
    }

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
            if (reportList.stream().anyMatch(l -> StringUtils.equals(l.getUri(),requestUrl) && StringUtils.equals(l.getResponseBody(),errDesc) )) {
                return Message.error("该问题已经被上报，无需重复上报");
            }
            String responseBodyJson = toJson(responseBody);
            linkisErrorCodeService.addTaskProblem(requestUrl, toJson(requestBody), toJson(requestHeaders),
                    toJson(queryParams), responseBodyJson, errCode, user, errDesc);
            ErrorCodeReport report = new ErrorCodeReport();
            report.setUri(requestUrl);
            report.setErrCode(errCode);
            report.setResponseBody(responseBodyJson);
            reportList.add(report);
            return Message.ok();
        }
        Map data = (Map) responseBody.get("data");
        if (data != null && data.containsKey("errorMsg")) {
            Map errorMsg = (Map) data.get("errorMsg");
            String errCode = String.valueOf(errorMsg.get("errCode"));
            String errDesc = (String) responseBody.get("errDesc");
            //同一个接口同一个错误码
            //todo 改为定时任务获取，数据库error_desc加上唯一键
            if (reportList.stream().anyMatch(l -> StringUtils.equals(l.getUri(),requestUrl) && StringUtils.equals( l.getErrCode(),errCode))) {
                return Message.error("该问题已经被上报，无需重复上报");
            }
            String responseBodyJson = toJson(responseBody);
            linkisErrorCodeService.addTaskProblem(requestUrl, toJson(requestBody), toJson(requestHeaders), toJson(queryParams), responseBodyJson, errCode, user, errDesc);
            ErrorCodeReport report = new ErrorCodeReport();
            report.setUri(requestUrl);
            report.setErrCode(errCode);
            report.setResponseBody(responseBodyJson);
            reportList.add(report);
        }
        Message message = Message.ok();
        return message;
    }


    String toJson(Object val) {
        return GsonHelper.toJson(val);
    }
}


