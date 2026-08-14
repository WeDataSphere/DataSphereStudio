/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.webank.wedatasphere.dss.apiservice.core.bo.*;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceQueryException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ExecuteCodeHelper;
import com.webank.wedatasphere.dss.apiservice.core.execute.LinkisJobSubmit;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiServiceQueryService;
import com.webank.wedatasphere.dss.apiservice.core.token.JwtManager;
import com.webank.wedatasphere.dss.apiservice.core.util.ApiUtils;
import com.webank.wedatasphere.dss.apiservice.core.util.AssertUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.DataApiServiceVo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(path = "/dss/apiservice", produces = {"application/json"})
@RestController
public class ApiServiceExecuteRestfulApi {
    public static final String XLSX_RESPONSE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceExecuteRestfulApi.class);
    @Autowired
    private ApiServiceQueryService queryService;

    private static final String SYS_COLUMN_PREFIX = "_";

    private static final String requestBodyDemo = "{\"moduleName\":\"aladdin-demo\",\"params\":{\"param1\": \"value1\"}}";

    @RequestMapping(value = "/execute/{path:.*}", method = RequestMethod.POST)
    public Message post(@PathVariable("path") VariableString path, @RequestBody QueryRequest queryRequest,
                        HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return getResponse(userName, path.getPath(), queryRequest, HttpMethod.POST);
    }

    @RequestMapping(value = "/execute/{path:.*}", method = RequestMethod.GET)
    public Message get(@PathVariable("path") VariableString path,
                       HttpServletRequest req) throws JsonProcessingException {
        String userName = SecurityFilter.getLoginUsername(req);

        QueryRequest queryRequest = new QueryRequest();

        // 如果requestBody为空，尝试从url中获取参数
        if (MapUtils.isEmpty(queryRequest.getParams())) {
            String paramSuffix = "params.";
            // 【数据管理】 get方式请求,查询参数使用params[xxx]
            String startSuffix = "params[";
            Enumeration<String> parameterNames = req.getParameterNames();
            Map<String, Object> params = new HashMap<>();
            while (parameterNames.hasMoreElements()) {
                String name = parameterNames.nextElement();
                if (name.startsWith(paramSuffix)) {
                    params.put(name.substring(paramSuffix.length()), req.getParameter(name));
                }else if (name.startsWith(startSuffix)){
                    // 获取params[xx]中括号的参数,和参数相对应的数据，用于请求linkis
                    params.put(name.substring("params[".length(),name.length()-1),req.getParameter(name));
                }

            }
            queryRequest.setParams(params);
        }
        String paramsJsonStr = req.getParameter("params");
        if(!StringUtils.isEmpty(paramsJsonStr)){
            JavaType javaType = BDPJettyServerHelper.jacksonJson().getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
            Map<String, Object> parmas = BDPJettyServerHelper.jacksonJson().readValue(paramsJsonStr, javaType);
            queryRequest.setParams(parmas);
        }


        if (StringUtils.isEmpty(queryRequest.getModuleName())) {
            queryRequest.setModuleName(req.getParameter("moduleName"));
        }

        return getResponse(userName, path.getPath(), queryRequest, HttpMethod.GET);
    }

    @RequestMapping(value = "/execute/{path:.*}", method = RequestMethod.PUT)
    public Message put(@PathVariable("path") VariableString path, @RequestBody QueryRequest queryRequest,
                       HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return getResponse(userName, path.getPath(), queryRequest, HttpMethod.PUT);
    }

    @RequestMapping(value = "/execute/{path:.*}", method = RequestMethod.DELETE)
    public Message delete(@PathVariable("path") VariableString path, @RequestBody QueryRequest queryRequest,
                          HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return getResponse(userName, path.getPath(), queryRequest, HttpMethod.DELETE);
    }

    private void validParam(QueryRequest queryRequest) {
        AssertUtil.notNull(queryRequest, "请求体不能为空，正确的格式:" + requestBodyDemo);
        AssertUtil.notEmpty(queryRequest.getModuleName(), "moduleName不能为空，正确的格式:" + requestBodyDemo);
        AssertUtil.notNull(queryRequest.getParams().get(ApiServiceConfiguration.API_SERVICE_TOKEN_KEY.getValue()), "请求token不能为空");
    }

    @RequestMapping(value = "/getApiHistory", method = RequestMethod.GET)
    public Message getApiHistory(@RequestParam("apiId") Long apiId, @RequestParam("apiVersionId") Long apiVersionId,
                          HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<DataApiServiceVo> executeHistory = queryService.getExecuteHistory(userName, apiId, apiVersionId);
        return Message.ok().data("data",executeHistory);
    }

    @RequestMapping(value = "/getHistoryQueryParams", method = RequestMethod.GET)
    public Message getHistoryQueryParams(@RequestParam("taskId") Long taskId, HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return Message.ok().data("data", queryService.getHistoryQueryParams(userName, taskId));
    }

    @RequestMapping(value = "/getDirFileTrees", method = RequestMethod.GET)
    public void getDirFileTrees(HttpServletRequest req, HttpServletResponse resp,
                                @RequestParam(required = false, name = "path") String path,
                                @RequestParam(required = false, name = "taskId") String taskId) throws IOException, ApiServiceQueryException {
        resp.setCharacterEncoding("UTF-8");
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new ApiServiceQueryException(80004, path);
        }
        String dirFileTree = "";
        ApiServiceJob apiServiceJob = queryService.getJobByTaskId(taskId);
        if (null != apiServiceJob && userName.equals(apiServiceJob.getSubmitUser())) {
            JobExecuteResult jobExecuteResult = new JobExecuteResult();
            jobExecuteResult.setTaskID(taskId);
            jobExecuteResult.setUser(apiServiceJob.getProxyUser());
            Map<String, String> props = new HashMap<>();
            UJESClient client = LinkisJobSubmit.getClient(props);
            dirFileTree = ExecuteCodeHelper.getResultList(jobExecuteResult, client, path);
        } else {
            dirFileTree = "The task is not found, TaskId: " + taskId;
        }
        resp.getWriter().println(dirFileTree);
        resp.getWriter().flush();
    }

    @RequestMapping(value = "/openFile", method = RequestMethod.GET)
    public void openFile(HttpServletRequest req,
                         @RequestParam(required = false, name = "path") String path,
                         @RequestParam(required = false, name = "taskId") String taskId,
                         @RequestParam(required = false, name = "page", defaultValue = "1") Integer page,
                         @RequestParam(required = false, name = "pageSize", defaultValue = "5000") Integer pageSize,
                         @RequestParam(required = false, name = "charset", defaultValue = "utf-8") String charset,
                         @RequestParam(required = false, name = "enableLimit", defaultValue = "false") Boolean enableLimit,
                         @RequestParam(required = false, name  = "columnPage", defaultValue = "1") Integer columnPage,
                         @RequestParam(required = false, name =  "columnPageSize",defaultValue = "500") Integer columnPageSize,
                         @RequestParam(value = "maskedFieldNames", required = false) String maskedFieldNames,
                         @RequestParam(value = "truncateColumn", required = false) String truncateColumn,
                         HttpServletResponse resp) throws IOException, ApiServiceQueryException {
        resp.setCharacterEncoding(charset);
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new ApiServiceQueryException(80004, path);
        }
        if (StringUtils.isEmpty(taskId)) {
            throw new ApiServiceQueryException(80005, "taskId is null");
        }
        String fileContent = "";
        ApiServiceJob apiServiceJob = queryService.getJobByTaskId(taskId);
        if (null != apiServiceJob && userName.equals(apiServiceJob.getSubmitUser())) {
            Map<String, String> props = new HashMap<>();
            UJESClient client = LinkisJobSubmit.getClient(props);
            fileContent = ExecuteCodeHelper.getResultContent(apiServiceJob.getProxyUser(), path, pageSize, client
                    ,enableLimit,columnPage,columnPageSize,page,maskedFieldNames,truncateColumn);
        } else {
            fileContent = "当前用户不存在运行的TaskId: " + taskId;
        }
        resp.getWriter().println(fileContent);
        resp.getWriter().flush();
    }

    @RequestMapping(value = "resultsetToExcel", method = RequestMethod.GET)
    public void resultsetToExcel(
            HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false, name = "path") String path,
            @RequestParam(required = false, name = "taskId") String taskId,
            @RequestParam(required = false, name = "charset", defaultValue = "utf-8") String charset,
            @RequestParam(required = false, name = "outputFileType", defaultValue = "csv") String outputFileType,
            @RequestParam(required = false, name = "csvSeparator", defaultValue = ",") String csvSeparator,
            @RequestParam(required = false, name = "outputFileName", defaultValue = "downloadResultset") String outputFileName,
            @RequestParam(required = false, name = "sheetName", defaultValue = "result") String sheetName,
            @RequestParam(required = false, name = "nullValue", defaultValue = "NULL") String nullValue,
            @RequestParam(required = false, name = "isFullExcel",defaultValue = "false") Boolean isFullExcel) throws ApiServiceQueryException, IOException {

        resp.addHeader("Content-Disposition", "attachment;filename="
                + new String((outputFileName + ".").getBytes("UTF-8"), "ISO8859-1") + "" + outputFileType);
        resp.setCharacterEncoding(charset);

        switch (outputFileType) {
            case "csv":
                resp.addHeader("Content-Type", "text/plain");
                break;
            case "xlsx":
                resp.addHeader("Content-Type", XLSX_RESPONSE_CONTENT_TYPE);
                break;
            default:
                throw new ApiServiceQueryException(80015, "不支持的下载类型");

        }

        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new ApiServiceQueryException(80005, path);
        }
        InputStream inputStream;
        ApiServiceJob apiServiceJob = queryService.getJobByTaskId(taskId);
        if (null != apiServiceJob && userName.equals(apiServiceJob.getSubmitUser())) {
            Map<String, String> props = new HashMap<>();
            UJESClient client = LinkisJobSubmit.getClient(props);
            inputStream = ExecuteCodeHelper.downloadResultSet(apiServiceJob.getProxyUser(),
                    path,
                    charset,
                    outputFileType,
                    csvSeparator,
                    outputFileName,
                    sheetName,
                    nullValue,
                    client,
                    isFullExcel);
        } else {
            resp.getWriter().println("当前用户不存在运行的TaskId: " + taskId);
            resp.getWriter().flush();
            return;
        }
        try {
            IOUtils.copy(inputStream, resp.getOutputStream());
            resp.getOutputStream().flush();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @RequestMapping(value = "/{id}/get", method = RequestMethod.GET)
    public Message getTaskByID(HttpServletRequest req, @PathVariable("id") Long taskId) {
        String username = SecurityFilter.getLoginUsername(req);
        ApiServiceJob apiServiceJob = queryService.getJobByTaskId(taskId.toString());
        if (null != apiServiceJob && username.equals(apiServiceJob.getSubmitUser())) {
            Map<String, String> props = new HashMap<>();
            UJESClient client = LinkisJobSubmit.getClient(props);
            JobExecuteResult jobExecuteResult = apiServiceJob.getJobExecuteResult();
            jobExecuteResult.setUser(apiServiceJob.getProxyUser());
            Map<String, Object> vo = ExecuteCodeHelper.getTaskInfoById(jobExecuteResult, client);
            queryService.updateTaskStatus(taskId.toString(),(String)vo.get("status"));
            return Message.ok().data("task", vo);
        } else {
            return Message.ok().data("task", null);
        }
    }

    private Message getResponse(String user, String path, QueryRequest queryRequest, String httpMethod) {
        return ApiUtils.doAndResponse(() -> {

            validParam(queryRequest);
            String token = queryRequest.getParams().get(ApiServiceConfiguration.API_SERVICE_TOKEN_KEY.getValue()).toString();

            Message messageVo = null;
            ApiServiceToken tokenDetail = null;
            boolean isParseRight = true;
            try {
                tokenDetail = JwtManager.parseToken(token);
            } catch (Exception e) {
                isParseRight = false;
                logger.error("token解析错误，该token无效",e);
                messageVo = Message.error("token解析错误，该token无效！");
            }
            if (false == isParseRight) {
                return messageVo;
            }
            //增加Public数据服务，支持代码搜搜功能

            try {
                LinkisExecuteResult query = queryService.query("/" + path,
                        queryRequest.getParams() == null ? new HashMap<>() : queryRequest.getParams(),
                        queryRequest.getModuleName(), httpMethod, tokenDetail, user);
                messageVo = Message.ok().data("taskId", query.getTaskId()).data("execId", query.getExecId());
            } catch (Exception e) {
                logger.error("数据服务执行报错", e);
                throw e;
            }

            return messageVo;
        });
    }
}