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

import com.webank.wedatasphere.dss.apiservice.core.bo.ApiCommentUpdateRequest;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceQuery;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiService;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiServiceQueryService;
import com.webank.wedatasphere.dss.apiservice.core.util.ApiUtils;
import com.webank.wedatasphere.dss.apiservice.core.util.AssertUtil;
import com.webank.wedatasphere.dss.apiservice.core.util.SQLCheckUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.*;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.apiservice.core.util.ApiUtils.WRITABLE_PATTERN;

@RequestMapping(path = "/dss/apiservice", produces = {"application/json"})
@RestController
public class ApiServiceCoreRestfulApi {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceCoreRestfulApi.class);

    private static final String DATABASE_NO_PRIVILEGE = "Authorization failed:No privilege";

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApiServiceQueryService apiServiceQueryService;

    @Autowired
    private Validator beanValidator;

    /**
     * Scriptis页面新建数据服务
     *
     * @param apiService
     * @param req
     * @return
     */
    @RequestMapping(value = "/api", method = RequestMethod.POST)
    public Message insert(@RequestBody ApiServiceVo apiService, HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            if (apiService.getWorkspaceId() == null) {
                apiService.setWorkspaceId(180L);
            }
            if (StringUtils.isBlank(apiService.getAliasName())) {
                return Message.error("'api service alias name' is missing[缺少中文名]");
            }
            if (StringUtils.isBlank(apiService.getScriptPath())) {
                return Message.error("'api service script path' is missing[缺少脚本路径]");
            }
            if (StringUtils.isBlank(apiService.getContent())) {
                return Message.error("'api service script content' is missing[缺少脚本内容]");
            }
            if (null == apiService.getWorkspaceId()) {
                return Message.error("'api service workspaceId ' is missing[缺少工作空间ID]");
            }
//            if (apiService.getContent().contains(";") && !apiService.getContent().toLowerCase().startsWith("use ")) {
//                return Message.error("'api service script content exists semicolon[脚本内容包含分号]");
//            }
//                     check data change script


            if (SQLCheckUtil.isPySpark(apiService.getScriptPath())){
                // 抽取pySpark脚本中的sql
                String pysparkSql = SQLCheckUtil.parsePySparkSql(apiService.getContent());
                if (StringUtils.isBlank(pysparkSql)) {
                    return Message.error("'api service pyspark script content' is missing sql[脚本内容缺少sql]");
                }

                String sparkSql =  pysparkSql.replaceAll("=\\s*'.*?'", "='#'");
                for (String sql : sparkSql.split(";")) {
                    if(WRITABLE_PATTERN.matcher(sql.trim()).matches()){
                        return Message.error("'api service pyspark script content' only supports query[脚本内容只支持查询语句]");
                    }
                }

                apiService.setPySparkSql(pysparkSql);

            }else{

                if (WRITABLE_PATTERN.matcher(apiService.getContent()).matches()) {
                    return Message.error("'api service script content' only supports query[脚本内容只支持查询语句]");
                }

                // 校验是否包含ddl语句和update、insert、delete
                // 替换where条件后跟着dml和ddl关键字和分号情况。 eg: select * from a where a = 'a;create table'; create table b;
                String content =  apiService.getContent().replaceAll("=\\s*'.*?'", "='#'");
                for (String sql : content.split(";")) {
                    if(WRITABLE_PATTERN.matcher(sql.trim()).matches()){
                        return Message.error("'api service script content' only supports query[脚本内容只支持查询语句]");
                    }
                }

            }


            if (SQLCheckUtil.isJdbc(apiService.getScriptPath())) {

                Map<String, Object> metadata = apiService.getMetadata();

                if (MapUtils.isEmpty(metadata)) {
                    return Message.error("'api service metadata' is missing[请选择数据源]");
                }

                Map<String, Object> configuration = (Map<String, Object>) metadata.get("configuration");
                if (MapUtils.isEmpty(configuration)) {
                    return Message.error("'api service metadata.configuration' is missing[请选择数据源]");
                }

                Map<String, Object> runtime = (Map<String, Object>) configuration.get("runtime");
                if (MapUtils.isEmpty(runtime)) {
                    return Message.error("'api service metadata.configuration.runtime' is missing[请选择数据源]");
                }

                String datasource = (String)runtime.get(ApiCommonConstant.JDBC_DATASOURCE_KEY);

                if(StringUtils.isEmpty(datasource)){
                    return Message.error("'api service metadata.configuration.runtime.[wds.linkis.engine.runtime.datasource]' is missing[请选择数据源]");
                }

                apiService.setDatasource(datasource);

            }


            String userName = SecurityFilter.getLoginUsername(req);
            Set<ConstraintViolation<ApiServiceVo>> result = beanValidator.validate(apiService, Default.class);
            if (result.size() > 0) {
                throw new ConstraintViolationException(result);
            }
            apiService.setCreator(userName);
            apiService.setModifier(userName);
            this.apiService.save(apiService);
            AuditLogUtils.printLog(userName, apiService.getWorkspaceId(), null, TargetTypeEnum.APISERVICE,
                    apiService.getWorkspaceId(), apiService.getName(), OperateTypeEnum.CREATE, apiService);
            return Message.ok().data("insert_id", apiService.getId());
        }, "/apiservice/api", "Fail to insert service api[新增服务api失败]");
    }

    /**
     * 像dm发起提单，可同时包含多个数据服务
     *
     * @param apiSubmitVo
     * @param req
     * @return
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public Message submit(@RequestBody ApiSubmitVo apiSubmitVo, HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            if (StringUtils.isBlank(apiSubmitVo.getApprovalName())) {
                return Message.error("'approvalName' is missing[缺少审批单名字]");
            }

            if(apiSubmitVo.getApprovalName().length() > ApiServiceConfiguration.APPROVAL_NAME_MAX_SIZE.getValue()){
                return Message.error("'approvalName' shall not exceed 200 characters[审批单名称不得超过200个字符]");
            }

            if (StringUtils.isBlank(apiSubmitVo.getApplyUser())) {
                return Message.error("'applyUser' is missing[缺少申请用户名字]");
            }
            if (null == apiSubmitVo.getWorkspaceId()) {
                return Message.error("'api service workspaceId ' is missing[缺少工作空间ID]");
            }
            if (StringUtils.isBlank(apiSubmitVo.getBackgroundDesc())) {
                return Message.error("'backgroundDesc' is missing[缺少背景描述]");
            }
            if (CollectionUtils.isEmpty(apiSubmitVo.getSubmitApiInfos())) {
                return Message.error("'submitApiInfos' is missing[缺少提交的数据服务信息]");
            }
            if (apiSubmitVo.getSensitive() == null) {
                return Message.error("'sensitive' is missing[缺少是否涉及一级数据]");
            }

            // 判断是否包含jdbc api
            List<Long> apiVersionId = apiSubmitVo.getSubmitApiInfos().stream().map(ApiVersionIdVo::getApiVersionId).collect(Collectors.toList());

            if(apiService.checkSubmitApi(apiVersionId) != null){

                if(StringUtils.isEmpty(apiSubmitVo.getDevPrincipals())){
                    return Message.error("'devPrincipals' is missing[缺少开发负责人信息]");
                }

                if(StringUtils.isEmpty(apiSubmitVo.getProductInfo())){
                    return Message.error("'productInfo' is missing[缺少关联产品信息]");
                }

            }


            if (apiSubmitVo.getDuration().equals("*")) {
                apiSubmitVo.setDuration(ApiServiceConfiguration.API_TOKEN_FOREVER_DURATION.getValue().toString());
            }
            String userName = SecurityFilter.getLoginUsername(req);
            apiSubmitVo.setCreator(userName);
            this.apiService.submitToDm(apiSubmitVo);
            AuditLogUtils.printLog(userName, apiSubmitVo.getWorkspaceId(), null, TargetTypeEnum.APISERVICE,
                    apiSubmitVo.getWorkspaceId(), apiSubmitVo.getApprovalName(), OperateTypeEnum.CREATE, apiSubmitVo);
            return Message.ok();
        }, "/apiservice/api/submit", "Fail to submit service api[提交服务api审批失败]");
    }

    /**
     * 获取用户可提交审批的api
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/availableSubmitApi", method = RequestMethod.GET)
    public Message availableSubmitApi(HttpServletRequest req,
                                      @RequestParam(required = false, name = "workspaceId") Integer workspaceId) {
        return ApiUtils.doAndResponse(() -> {
            if (null == workspaceId) {
                return Message.error("'api service search workspaceId' is missing[缺少工作空间Id]");
            }
            String userName = SecurityFilter.getLoginUsername(req);
            if (!this.apiService.checkUserWorkspace(userName, workspaceId)) {
                return Message.error("'api service search workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            List<ApiServiceVo> availableSubmitApiList = apiService.queryAvailableSubmitApi(userName, workspaceId);
            return Message.ok().data("availableSubmitApiList", availableSubmitApiList);
        }, "/apiservice/availableSubmitApi", "Fail to query page of service api[查询可提交api失败]");
    }

    /**
     * bdp数据提取，dm创建数据服务时调这个接口，使用的1.1.5.1-webank分支代码
     *
     * @param apiService
     * @param req
     * @return
     */
//    @RequestMapping(value = "/create",method = RequestMethod.POST)
//    @Deprecated
//    public Message create(@RequestBody ApiServiceVo apiService, HttpServletRequest req) {
//
//        ApprovalVo approvalVo = apiService.getApprovalVo();
//
//        LOG.info("Apply to create api service for approval No is " + approvalVo.getApprovalNo());
//
//        LOG.info("Print detail info of api service:"+apiService.toString());
//
//        Message message;
//
//        String userName = SecurityFilter.getLoginUsername(req);
//        LOG.info("user {} begin to create api. params: {}", userName, apiService);
//
//        apiService.setCreator(userName);
//        apiService.setModifier(userName);
//
//        try {
//
//            ApiUtils.checkApiCreateParams(this.apiService,apiService, approvalVo,beanValidator);
//            this.apiService.saveByApp(apiService);
//            AuditLogUtils.printLog(userName, apiService.getWorkspaceId(), null, TargetTypeEnum.APISERVICE, apiService.getTargetServiceId(),
//                    apiService.getName(), OperateTypeEnum.CREATE, apiService);
//            message = Message.ok().data("insert_id", apiService.getId()).data("approval_no", approvalVo.getApprovalNo());
//        }catch (ApiServiceParamsCheckException e){
//            message=ApiUtils.buildMessageAndResponse(100015,"创建数据服务参数检查失败,错误原因:"+ e.getMessage());
//        } catch(ApiExecuteException e){
//           if(e.getMessage().contains(DATABASE_NO_PRIVILEGE)){
//                message= ApiUtils.buildMessageAndResponse(100016,"创建数据服务执行SQL库表权限错误，请检查库表权限后重新执行, 错误原因:"+e.getMessage());
//            }else{
//                message= ApiUtils.buildMessageAndResponse(100017,"创建数据服务执行SQL失败，错误原因:"+e.getMessage());
//            }
//        }catch (Exception e) {
//            message=ApiUtils.buildMessageAndResponse(100018,"创建数据服务失败,错误原因:"+ e.getMessage());
//        }
//        return message;
//    }
    @RequestMapping(value = "/api/{api_service_version_id}", method = RequestMethod.PUT)
    public Message update(@RequestBody ApiServiceVo apiService,
                          @PathVariable("api_service_version_id") Long apiServiceVersionId,
                          HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            if (StringUtils.isBlank(apiService.getScriptPath())) {
                return Message.error("'api service script path' is missing[缺少脚本路径]");
            }
            if (apiServiceVersionId != 0 && StringUtils.isBlank(apiService.getPath())) {
                return Message.error("'api service api path' is missing[缺少api路径]");
            }
            if (StringUtils.isBlank(apiService.getContent())) {
                return Message.error("'api service script content' is missing[缺少脚本内容]");
            }

            if (null == apiService.getWorkspaceId()) {
                return Message.error("'api service workspaceId ' is missing[缺少工作空间ID]");
            }

            if (null == apiService.getTargetServiceId()) {
                return Message.error("'api service update to target service id ' is missing[缺少更新目标服务ID]");
            }


            if (SQLCheckUtil.isPySpark(apiService.getScriptPath())){
                // 抽取pySpark脚本中的sql
                String pysparkSql = SQLCheckUtil.parsePySparkSql(apiService.getContent());
                if (StringUtils.isBlank(pysparkSql)) {
                    return Message.error("'api service pyspark script content' is missing sql[脚本内容缺少sql]");
                }

                String sparkSql =  pysparkSql.replaceAll("=\\s*'.*?'", "='#'");
                for (String sql : sparkSql.split(";")) {
                    if(WRITABLE_PATTERN.matcher(sql.trim()).matches()){
                        return Message.error("'api service pyspark script content' only supports query[脚本内容只支持查询语句]");
                    }
                }

                apiService.setPySparkSql(pysparkSql);

            }else{

//             check data change script
                if (WRITABLE_PATTERN.matcher(apiService.getContent()).matches()) {
                    return Message.error("'api service script content' only supports query[脚本内容只支持查询语句]");
                }

                String content =  apiService.getContent().replaceAll("=\\s*'.*?'", "='#'");
                for (String sql : content.split(";")) {
                    if(WRITABLE_PATTERN.matcher(sql.trim()).matches()){
                        return Message.error("'api service script content' only supports query[脚本内容只支持查询语句]");
                    }
                }
            }



            if (SQLCheckUtil.isJdbc(apiService.getScriptPath())) {

                Map<String, Object> metadata = apiService.getMetadata();

                if (MapUtils.isEmpty(metadata)) {
                    return Message.error("'api service metadata' is missing[请选择数据源]");
                }

                Map<String, Object> configuration = (Map<String, Object>) metadata.get("configuration");
                if (MapUtils.isEmpty(configuration)) {
                    return Message.error("'api service metadata.configuration' is missing[请选择数据源]");
                }

                Map<String, Object> runtime = (Map<String, Object>) configuration.get("runtime");
                if (MapUtils.isEmpty(runtime)) {
                    return Message.error("'api service metadata.configuration.runtime' is missing[请选择数据源]");
                }

                String datasource = (String)runtime.get(ApiCommonConstant.JDBC_DATASOURCE_KEY);

                if(StringUtils.isEmpty(datasource)){
                    return Message.error("'api service metadata.configuration.runtime.[wds.linkis.engine.runtime.datasource]' is missing[请选择数据源]");
                }

                apiService.setDatasource(datasource);

            }

            String userName = SecurityFilter.getLoginUsername(req);
            LOG.info("user {} begin to update service api, params: {}", userName, apiService);
//            Bean validation
            Set<ConstraintViolation<ApiServiceVo>> result = beanValidator.validate(apiService, Default.class);
            if (result.size() > 0) {
                throw new ConstraintViolationException(result);
            }
            apiService.setLatestVersionId(apiServiceVersionId);
            apiService.setModifier(userName);
            apiService.setModifyTime(Calendar.getInstance().getTime());
            this.apiService.update(apiService);
            AuditLogUtils.printLog(userName, apiService.getWorkspaceId(), null, TargetTypeEnum.APISERVICE, apiService.getTargetServiceId(),
                    apiService.getName(), OperateTypeEnum.UPDATE, apiService);
            return Message.ok().data("update_id", apiServiceVersionId);
        }, "/apiservice/api/" + apiServiceVersionId, "Fail to update service api[更新服务api失败]");
    }


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Message query(@RequestParam(required = false, name = "name") String name,
                         @RequestParam(required = false, name = "tag") String tag,
                         @RequestParam(required = false, name = "status") Integer status,
                         @RequestParam(required = false, name = "creator") String creator,
                         @RequestParam(required = false, name = "workspaceId") Integer workspaceId,
                         HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);

        return ApiUtils.doAndResponse(() -> {
            if (null == workspaceId) {
                return Message.error("'api service search workspaceId' is missing[缺少工作空间Id]");
            }
            ApiServiceQuery query = new ApiServiceQuery(userName, name, tag, status, creator);
            query.setWorkspaceId(workspaceId);
            if (!this.apiService.checkUserWorkspace(userName, workspaceId)) {
                return Message.error("'api service search workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            List<ApiServiceVo> queryList = apiService.query(query);
            return Message.ok().data("query_list", queryList);
        }, "/apiservice/search", "Fail to query page of service api[查询服务api失败]");
    }


    /**
     * 获取创建者在工作空间创建的数据服务
     *
     * @param workspaceId
     * @param req
     * @return
     */
    @RequestMapping(value = "/getUserServices", method = RequestMethod.GET)
    public Message getUserServices(@RequestParam(required = false, name = "workspaceId") Integer workspaceId,
                                   @RequestParam(required = true, name = "type") String type,
                                   HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return ApiUtils.doAndResponse(() -> {
            if (!this.apiService.checkUserWorkspace(userName, workspaceId)) {
                return Message.error("'api service getUserServices workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            List<ApiServiceVo> apiServiceList = apiService.queryByWorkspaceId(workspaceId, userName, type);
            return Message.ok().data("query_list", apiServiceList);
        }, "/apiservice/getUserServices", "Fail to query page of user service api[查询用户服务api列表失败]");
    }


    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public Message query(HttpServletRequest req, @RequestParam(required = false, name = "workspaceId") Integer workspaceId) {
        String userName = SecurityFilter.getLoginUsername(req);
        return ApiUtils.doAndResponse(() -> {

            List<String> tags = apiService.queryAllTags(userName, workspaceId);
            return Message.ok().data("tags", tags);
        }, "/apiservice/tags", "Fail to query page of service tag[查询服务tag失败]");
    }


    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Message queryByScriptPath(@RequestParam(required = false, name = "scriptPath") String scriptPath,
                                     HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isBlank(scriptPath)) {
                return Message.error("'api service scriptPath' is missing[缺少脚本路径]");
            }
            ApiServiceVo apiServiceVo = apiService.queryByScriptPath(scriptPath);
            if (null != apiServiceVo) {
                if (!this.apiService.checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
                    return Message.error("'api service query workspaceId' is wrong[该用户不属于该工作空间Id]");
                }

                if (apiServiceVo.getCreator().equals(userName)) {
                    return Message.ok().data("result", apiServiceVo);
                } else {
                    return Message.ok().data("result", null);
                }
            } else {
                return Message.ok().data("result", apiServiceVo);
            }
        }, "/apiservice/query", "Fail to query page of service api[查询服务api失败]");
    }

    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Message queryById(@RequestParam(required = false, name = "id") Long id,
                             HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return ApiUtils.doAndResponse(() -> {
            if (id == null) {
                return Message.error("'api service id' is missing[缺少服务ID]");
            }
            ApiServiceVo apiServiceVo = apiService.queryById(id, userName);
            AssertUtil.notNull(apiServiceVo, "未找到数据服务，有可能已经被删除");
            if (!this.apiService.checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
                return Message.error("'api service queryById for workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            return Message.ok().data("result", apiServiceVo);
        }, "/apiservice/queryById", "Fail to query page of service api[查询服务api失败]");
    }

    @RequestMapping(value = "/queryByManager", method = RequestMethod.GET)
    public Message queryByManager(@RequestParam(required = false, name = "id") Long id, HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return ApiUtils.doAndResponse(() -> {
            if (id == null) {
                return Message.error("'api service id' is missing[缺少服务ID]");
            }
            ApiServiceVo apiServiceVo = apiService.queryByManager(id, userName);
            AssertUtil.notNull(apiServiceVo, "未找到数据服务，有可能已经被删除");
            if (!this.apiService.checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
                return Message.error("'api service queryById for workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            return Message.ok().data("result", apiServiceVo);
        }, "/apiservice/queryByManager", "Fail to query page of service api[查询服务api失败]");
    }

    @RequestMapping(value = "/checkPath", method = RequestMethod.GET)
    public Message checkPath(@RequestParam(required = false, name = "scriptPath") String scriptPath, @RequestParam(required = false, name = "path") String path) {
        //需要跨用户查询
        return ApiUtils.doAndResponse(() -> {
            if (StringUtils.isBlank(scriptPath)) {
                return Message.error("'api service scriptPath' is missing[缺少api脚本路径]");
            }
            if (StringUtils.isBlank(path)) {
                return Message.error("'api service path' is missing[缺少api路径]");
            }
            Integer apiCount = apiService.queryCountByPath(scriptPath, path);
            return Message.ok().data("result", 0 > Integer.valueOf(0).compareTo(apiCount));
        }, "/apiservice/checkPath", "Fail to check path of service api[校验服务api路径失败]");
    }

    @RequestMapping(value = "/checkName", method = RequestMethod.GET)
    public Message checkName(@RequestParam(required = false, name = "name") String name) {
        //需要跨用户查询
        return ApiUtils.doAndResponse(() -> {
            if (StringUtils.isBlank(name)) {
                return Message.error("'api service name' is missing[缺少api名称]");
            }
            Integer count = apiService.queryCountByName(name);
            return Message.ok().data("result", count > 0);
        }, "/apiservice/checkName", "Fail to check name of service api[校验服务api名称失败]");
    }

    @RequestMapping(value = "/apiDisable", method = RequestMethod.GET)
    public Message apiDisable(@RequestParam(required = false, name = "id") Long id,
                              HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (null == id) {
                return Message.error("'api service api id' is missing[缺少api id]");
            }
            boolean resultFlag = apiService.disableApi(id, userName);
            return Message.ok().data("result", resultFlag);
        }, "/apiservice/apiDisable", "Fail to disable api[禁用api失败]");
    }

    @RequestMapping(value = "/apiEnable", method = RequestMethod.GET)
    public Message apiEnable(@RequestParam(required = false, name = "id") Long id,
                             HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (null == id) {
                return Message.error("'api service api id' is missing[缺少api id]");
            }
            boolean resultFlag = apiService.enableApi(id, userName);
            return Message.ok().data("result", resultFlag);
        }, "/apiservice/apiEnable", "Fail to enable api[启用api失败]");
    }

    @RequestMapping(value = "/apiDelete", method = RequestMethod.GET)
    public Message apiDelete(@RequestParam(required = false, name = "id") Long id,
                             HttpServletRequest req) {
        //目前暂时不实际删除数据，只做不可见和不可用。
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (null == id) {
                return Message.error("'api service api id' is missing[缺少api id]");
            }
            LOG.info("user {} begin to delete api, id: {}", userName, id);
            boolean resultFlag = apiService.deleteApi(id, userName);
            return Message.ok().data("result", resultFlag);
        }, "/apiservice/apiDelete", "Fail to delete api[删除api失败]");
    }

    @RequestMapping(value = "/apiCommentUpdate", method = RequestMethod.POST)
    public Message apiCommentUpdate(HttpServletRequest req,
                                    @RequestBody ApiCommentUpdateRequest apiCommentUpdateRequest) {
        Long id = apiCommentUpdateRequest.getId();
        String comment = apiCommentUpdateRequest.getComment();
        //目前暂时不实际删除数据，只做不可见和不可用。
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (null == id) {
                return Message.error("'api service api id' is missing[缺少api id]");
            }
            LOG.info("user {} begin to update api comment, params: {}", userName, apiCommentUpdateRequest);
            boolean resultFlag = apiService.updateComment(id, comment, userName);
            return Message.ok().data("result", resultFlag);
        }, "/apiservice/apiCommentUpdate", "Fail to update api comment[更新失败]");
    }


    @RequestMapping(value = "/apiParamQuery", method = RequestMethod.GET)
    public Message apiParamQuery(@RequestParam(required = false, name = "scriptPath") String scriptPath,
                                 @RequestParam(required = false, name = "versionId") Long versionId,
                                 HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(scriptPath)) {
                return Message.error("'api service api scriptPath' is missing[缺少api scriptPath]");
            }
            if (null == versionId) {
                return Message.error("'api service api version' is missing[缺少api 版本号]");
            }
            List<QueryParamVo> queryParamVoList = apiServiceQueryService.queryParamList(scriptPath, versionId);
            return Message.ok().data("result", queryParamVoList);
        }, "/apiservice/apiParamQuery", "Fail to query api info[查询api信息失败]");
    }

    @RequestMapping(value = "/apiVersionQuery", method = RequestMethod.GET)
    public Message apiVersionQuery(@RequestParam(required = false, name = "serviceId") Long serviceId,
                                   HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            if (null == serviceId) {
                return Message.error("'api service api serviceId' is missing[缺少api serviceId]");
            }
            List<ApiVersionVo> apiVersionVoList = apiServiceQueryService.queryApiVersionById(serviceId)
                    .stream().filter(apiVersionVo -> apiVersionVo.getCreator().equals(userName))
                    .collect(Collectors.toList());
            return Message.ok().data("result", apiVersionVoList);
        }, "/apiservice/apiVersionQuery", "Fail to query api version[查询api版本失败]");
    }

    @RequestMapping(value = "/apiContentQuery", method = RequestMethod.GET)
    public Message apiContentQuery(@RequestParam(required = false, name = "versionId") Long versionId,
                                   HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        return ApiUtils.doAndResponse(() -> {
            if (null == versionId) {
                return Message.error("'api service api versionId' is missing[缺少api versionId]");
            }
            ApiServiceVo apiServiceVo = apiServiceQueryService.queryByVersionId(userName, versionId);
            if (!this.apiService.checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
                return Message.error("'api service apiContentQuery for workspaceId' is wrong[该用户不属于该工作空间Id]");
            }
            return Message.ok().data("result", apiServiceVo);
        }, "/apiservice/apiContentQuery", "Fail to query api Content[查询api版本内容失败]");
    }

    @RequestMapping(value = "/updateApiServiceBML", method = RequestMethod.GET)
    public Message updateApiServiceBML(@RequestParam(required = false, name = "serverUrl") String serverUrl, HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            this.apiService.updateApiServiceBML(serverUrl);
            return Message.ok();
        }, "/apiservice/updateApiServiceBML", "Fail to move bml[迁移失败]");
    }

    @RequestMapping(value = "/checkApiServiceBML", method = RequestMethod.GET)
    public Message checkApiServiceBML(@RequestParam(required = false, name = "serverUrl") String serverUrl, HttpServletRequest req) {
        return ApiUtils.doAndResponse(() -> {
            this.apiService.checkApiServiceBML(serverUrl);
            return Message.ok();
        }, "/apiservice/checkApiServiceBML", "Fail to check bml[检测BML内容失败]");
    }


    @RequestMapping(value = "/checkSubmitApi",method = RequestMethod.POST)
    public Message checkSubmitApi(@RequestBody CheckSubmitApiRequest checkSubmitApiRequest,HttpServletRequest req){

        if(CollectionUtils.isEmpty(checkSubmitApiRequest.getSubmitApiInfos())){
            return Message.error("Unselected API information (未选择api信息)");
        }
        List<Long> apiVersionId = checkSubmitApiRequest.getSubmitApiInfos().stream().map(ApiVersionIdVo::getApiVersionId).collect(Collectors.toList());
        ApiServiceVo apiServiceVo = apiService.checkSubmitApi(apiVersionId);
        CheckSubmitApiResponse response = new CheckSubmitApiResponse();
        response.setSubmitApiInfos(checkSubmitApiRequest.getSubmitApiInfos());
        // apiServiceVo ==null 则是可以从dm获取数据库信息
        response.setStatus(apiServiceVo == null ? 1 : 0);
        response.setApiServiceVo(apiServiceVo);

        return Message.ok().data("data",response);

    }

}
