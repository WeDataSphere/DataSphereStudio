/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.dss.apiservice.core.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceBean;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceJob;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceToken;
import com.webank.wedatasphere.dss.apiservice.core.bo.LinkisExecuteResult;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.constant.ParamType;
import com.webank.wedatasphere.dss.apiservice.core.constant.RequireEnum;
import com.webank.wedatasphere.dss.apiservice.core.dao.*;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceQueryException;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceRuntimeException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.execute.DefaultApiServiceJob;
import com.webank.wedatasphere.dss.apiservice.core.jdbc.DatasourceService;
import com.webank.wedatasphere.dss.apiservice.core.response.SimpleHttpResponse;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiService;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiServiceQueryService;
import com.webank.wedatasphere.dss.apiservice.core.stategy.EngineManagerService;
import com.webank.wedatasphere.dss.apiservice.core.util.*;
import com.webank.wedatasphere.dss.apiservice.core.vo.*;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus;
import org.apache.linkis.storage.source.FileSource;
import org.apache.linkis.storage.source.FileSource$;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import scala.Tuple3;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.*;
import static java.util.stream.Collectors.toMap;

/**
 * 接口调用service
 *
 * @author lidongzhang
 */
@Service
public class ApiServiceQueryServiceImpl implements ApiServiceQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceQueryServiceImpl.class);
    public static final String API_SUBMIT_USER = "dss_api_submit_user";
    public static final String API_SERVICE_TOKEN = "ApiServiceToken";
    private static final Pattern pattern = Pattern.compile("--+");
    private static final String REPLACEMENT = "\\-";
    private static final int ARRAY_SIZE = 5;
    private static final int TASKiD_LIMIT_SIZE = 30;

    /**
     * key:resourceId+version
     * value:bml
     */
    private static Cache<String, Pair<Object, ArrayList<String[]>>> bmlCache = CacheBuilder.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .maximumSize(10000)
            .removalListener((notification) -> {
                if (notification.getCause() == RemovalCause.SIZE) {
                    LOG.warn("bml缓存容量不足，移除key:" + notification.getKey());
                }
            })
            .build();

    /**
     * key:resourceId+version
     * value:configParam
     */
    private static Cache<String, Map<String, String>> configParamCache = CacheBuilder.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .maximumSize(10000)
            .removalListener((notification) -> {
                if (notification.getCause() == RemovalCause.SIZE) {
                    LOG.warn("configParamCache缓存容量不足，移除key:" + notification.getKey());
                }
            })
            .build();

    /**
     * key:datasourceMap
     * value:jdbc连接信息
     */
    private static Cache<Map<String, Object>, Tuple3> datasourceCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(2000)
            .removalListener((notification) -> {
                if (notification.getCause() == RemovalCause.SIZE) {
                    LOG.warn("datasource缓存容量不足，移除key:" + notification.getKey());
                }
            })
            .build();

    @Autowired
    private ApiServiceDao apiServiceDao;

    @Autowired
    private ApiServiceParamDao apiServiceParamDao;

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private ApiServiceVersionDao apiServiceVersionDao;

    @Autowired
    private ApiServiceApprovalDao apiServiceApprovalDao;

    @Autowired
    private ApiServiceTokenManagerDao apiServiceTokenManagerDao;

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApiServiceAccessDao apiServiceAccessDao;

    @Autowired
    private ApiServiceBeanDao apiServiceBeanDao;

    @Autowired
    private EngineManagerService engineManagerService;

    /**
     * Bml client
     */
    private BmlClient client;

    @PostConstruct
    public void init() {
        LOG.info("build client start ======");
        client = BmlClientFactory.createBmlClient();
        LOG.info("build client end =======");
    }

    @Override
    public LinkisExecuteResult query(String path,
                                     Map<String, Object> reqParams,
                                     String moduleName,
                                     String httpMethod,
                                     ApiServiceToken tokenDetail,
                                     String loginUser) throws ApiServiceQueryException {
        // 根据path查询resourceId和version
        // 得到metadata
        // 执行查询
        //path对于api必须唯一
        ApiServiceVo apiServiceVo = apiServiceDao.queryByPath(path);
        if (null == apiServiceVo) {
            throw new ApiServiceRuntimeException("根据脚本路径未匹配到数据服务！");
        }
        //增加公共数据服务功能，如代码搜索等，必须由管理员指定。
        if (!tokenDetail.getApplyUser().equals(loginUser) && !isPublicApiService(tokenDetail, apiServiceVo)) {
            throw new ApiServiceQueryException(40030, "用户Token检查未通过");
        }
        if (!apiService.checkUserWorkspace(loginUser, apiServiceVo.getWorkspaceId().intValue())) {
            throw new ApiServiceRuntimeException("用户工作空间检查不通过！");
        }
        if (!apiServiceVo.getId().equals(tokenDetail.getApiServiceId())) {
            throw new ApiServiceRuntimeException("用户token中服务ID不匹配！");
        }
        //创建者使用最新版本，授权用户使用最新审批通过版本
        ApiVersionVo maxApiVersionVo;
        if (apiServiceVo.getCreator().equals(loginUser)) {
            maxApiVersionVo = apiService.getMaxVersion(apiServiceVo.getId());
        } else {
            maxApiVersionVo = apiService.getMaxApprovedVersion(apiServiceVo.getId());
        }
        AssertUtil.notNull(apiServiceVo, "接口不存在，path=" + path);
        AssertUtil.isTrue(StringUtils.equals(httpMethod, apiServiceVo.getMethod().toUpperCase()),
                "该接口不支持" + httpMethod + "请求，请用" + apiServiceVo.getMethod() + "请求");
        AssertUtil.isTrue(1 == apiServiceVo.getStatus(), "接口已禁用");
        AssertUtil.notNull(maxApiVersionVo, "未找到最新的版本，path=" + path);

        try {
            Pair<Object, ArrayList<String[]>> collect = queryBml(apiServiceVo.getCreator(), maxApiVersionVo.getBmlResourceId(),
                    maxApiVersionVo.getBmlVersion(), apiServiceVo.getScriptPath());
            String executeCode = collect.getSecond().get(0)[0];

            Map<String, Object> variable = (Map) ((Map) collect.getFirst()).get("variable");

            //没有传入的参数，使用默认值
            Map<String, String> paramTypes = queryConfigParam(apiServiceVo.getId(), maxApiVersionVo.getVersion(),maxApiVersionVo.getBmlVersion());
            if (variable != null) {
                variable.forEach((k, v) -> {
                    if (!reqParams.containsKey(k)) {
                        if (ParamType.number.equals(paramTypes.get(k))) {
                            reqParams.put(k, Integer.valueOf(v.toString()));
                        } else {
                            reqParams.put(k, v);
                        }
                    }
                });
            }
            // 用户请求的参数值注入检查，排除token
            for (Map.Entry<String, Object> entry : reqParams.entrySet()) {
                String k = entry.getKey();
                String v = String.valueOf(entry.getValue());
                if (v.contains("--")) {
                    entry.setValue(replaceSymbol(v));
                }
                if (!k.equals(ApiServiceConfiguration.API_SERVICE_TOKEN_KEY.getValue())
                        && SQLCheckUtil.doParamInjectionCheck((String) reqParams.get(k))) {
                    // 如果注入直接返回null
                    LOG.warn("查询条件中包含非法关键字：{}", v.toString());
                    throw new ApiServiceQueryException(80005, "查询条件中包含非法关键字：" + (String) reqParams.get(k));
                }
            }

            //数组类型，如果没有加单引号，自动添加
            reqParams.forEach((k, v) -> {
                if (ParamType.array.equals(paramTypes.get(k))) {
                    String sourceStr = v.toString();
                    String targetStr = sourceStr;
                    sourceStr = sourceStr.replaceAll("(\n\r|\r\n|\r|\n)", ",");
                    sourceStr = sourceStr.replaceAll(",,", ",");

                    if (!sourceStr.contains("\'")) {
                        targetStr = Arrays.stream(sourceStr.split(",")).map(s -> "\'" + s + "\'").collect(Collectors.joining(","));
                        reqParams.put(k, targetStr);
                    } else {
                        reqParams.put(k, sourceStr);
                    }
                }
            });

//            AssertUtil.isTrue(MapUtils.isNotEmpty((Map) collect.getKey()), "数据源不能为空");
            //获取代理执行用户
            String executeUser = maxApiVersionVo.getExecuteUser();
            //数据服务合并提单后，executeUser字段放到api_version表中了，这里做兼容。
            if (StringUtils.isEmpty(executeUser)) {
                ApprovalVo approvalVo = apiServiceApprovalDao.queryByVersionId(maxApiVersionVo.getId());
                executeUser = approvalVo.getExecuteUser();
            }
            ApiServiceExecuteJob job = new DefaultApiServiceJob();
            //todo 不允许创建用户自己随意代理执行，创建用户只能用自己用户执行
            job.setUser(loginUser);
            if (!apiServiceVo.getCreator().equals(loginUser) && StringUtils.isNotEmpty(executeUser)) {
                if ("hadoop".equalsIgnoreCase(executeUser)) {
                    throw new ApiExecuteException(80004, "非法使用Hadoop用户作为执行用户");
                }
                job.setUser(executeUser);
            }
            job.setParams(null);
            //为公共数据服务添加用户限制
            reqParams.put(API_SUBMIT_USER, loginUser);
            // 脚本类型为py3, 使用spark3版本
            if(SQLCheckUtil.isPyspark3(apiServiceVo.getScriptPath())){
                reqParams.put("sparkVersion","3");
            }

            // 脚本类型是jdbc
            if(SQLCheckUtil.isJdbc(apiServiceVo.getScriptPath())){

                LOG.info("{} jdbc api parse jdbc datasource,script is {} ", apiServiceVo.getName(), apiServiceVo.getScriptPath());

                job.setVariableMap(reqParams);
                Map<String, Object> configuration = (Map) ((Map) collect.getFirst()).get("configuration");
                LOG.info("{} jdbc api datasource is {}",apiServiceVo.getName(), configuration);

                if(MapUtils.isEmpty(configuration)){
                    throw new ApiExecuteException(80005, "api service metadata.configuration is empty, " +
                            "Please republish the data service  (jdbc数据源为空,请重新发布数据服务)");
                }

                Map<String, Object> runtime = (Map<String, Object>) configuration.get("runtime");
                if(MapUtils.isEmpty(runtime)){

                    throw new ApiExecuteException(80005, "api service metadata.configuration.runtime is empty, " +
                            "Please republish the data service  (jdbc数据源为空,请重新发布数据服务)");
                }

                String datasource = (String)runtime.get(ApiCommonConstant.JDBC_DATASOURCE_KEY);

                if(StringUtils.isEmpty(datasource)){
                    throw new ApiExecuteException(80005, "api service metadata.configuration.runtime.[wds.linkis.engine.runtime.datasource] is empty, " +
                            "Please republish the data service  (jdbc数据源为空,请重新发布数据服务)");
                }


                job.setRuntimeParams(runtime);

            }else{
                job.setRuntimeParams(reqParams);
            }


            job.setScriptePath(apiServiceVo.getScriptPath());
            JobExecuteResult jobExecuteResult;
            try {
                jobExecuteResult = engineManagerService.engineExecute(job, paramTypes, maxApiVersionVo, apiServiceVo, executeCode);
            } catch (Exception e) {
                // 处理linkis查询参数超出的异常情况
                String message = e.getMessage();
                String[] errorInfo = new String[]{"/api/rest_j/v1/entrance/execute", "MysqlDataTruncation",
                        "Data too long for column 'params'"};
                if (StrUtil.containsAll(message, errorInfo)) {
                    LOG.error(message);
                    throw new ApiExecuteException(80004, "参数长度超过查询字符限制");
                }
                throw e;
            }


            //记录用户Api被执行信息
            ApiAccessVo apiAccessVo = new ApiAccessVo();
            apiAccessVo.setUser(loginUser);
            apiAccessVo.setApiPublisher(apiServiceVo.getCreator());
            apiAccessVo.setApiServiceName(apiServiceVo.getName());
            apiAccessVo.setApiServiceId(apiServiceVo.getId());
            apiAccessVo.setApiServiceVersionId(maxApiVersionVo.getId());
            apiAccessVo.setProxyUser(job.getUser());
            apiAccessVo.setAccessTime(DateUtil.getNow());
            apiAccessVo.setTaskID(jobExecuteResult.getTaskID());
            apiAccessVo.setTaskStatus(ExecutionNodeStatus.Running.name());
            apiAccessVo.setQueryParams(buildQueryParam(reqParams));
            apiServiceAccessDao.addAccessRecord(apiAccessVo);

            //记录执行任务用户和代理用户关系，没有代理用户的统一设置为登录用户
            ApiServiceBean apiServiceBean = new ApiServiceBean(loginUser, job.getUser(), jobExecuteResult.getTaskID(), jobExecuteResult.getExecID(), jobExecuteResult.getUser());

            apiServiceBeanDao.insert(apiServiceBean);

            return new LinkisExecuteResult(jobExecuteResult.getTaskID(), jobExecuteResult.getExecID());
        } catch (IOException | ApiExecuteException e) {
            throw new ApiServiceRuntimeException(e.getMessage(), e);
        }
    }

    private static String buildQueryParam(Map<String, Object> reqParams) {
        //去除非查询条件中的元素
        reqParams.remove(API_SUBMIT_USER);
        reqParams.remove(API_SERVICE_TOKEN);
        return DSSCommonUtils.COMMON_GSON.toJson(reqParams);
    }

    private static String replaceSymbol(String str) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String match = matcher.group();
            int length = match.length();
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < length; i++) {
                replacement.append(REPLACEMENT);
            }
            //避免将replacement识别为正则，将替换字符追加到sb中
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    private boolean isPublicApiService(ApiServiceToken tokenDetail, ApiServiceVo apiServiceVo) {
        if (tokenDetail.getPublisher().equals(apiServiceVo.getCreator()) && ApiUtils.isPublicApiService(apiServiceVo.getId())) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public ApiServiceVo queryByVersionId(String userName, Long versionId) throws ApiServiceQueryException {
        ApiVersionVo apiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(versionId);
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(apiVersionVo.getApiId());
        //授权后才可以查看内容，创建者不授权也能查看
        List<TokenManagerVo> userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUserAndVersionId(userName, versionId);
        if (apiServiceVo.getCreator().equals(userName) || userTokenManagerVos.size() > 0) {
            try {
                Pair<Object, ArrayList<String[]>> collect = queryBml(apiServiceVo.getCreator(), apiVersionVo.getBmlResourceId(),
                        apiVersionVo.getBmlVersion(), apiServiceVo.getScriptPath());
                String executeCode = collect.getSecond().get(0)[0];
                apiServiceVo.setContent(executeCode);

            } catch (IOException e) {
                throw new ApiServiceQueryException(800002, "查询数据服务API内容异常");
            }
            apiServiceVo.setScriptPath(apiVersionVo.getSource());
            return apiServiceVo;
        } else {
            throw new ApiServiceQueryException(800003, "没有权限查看数据服务API内容，请先提单授权");
        }
    }

    @Override
    public List<QueryParamVo> queryParamList(String scriptPath, Long versionId) {
        ApiVersionVo targetApiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(versionId);

        ApiServiceVo apiServiceVo = apiServiceDao.queryById(targetApiVersionVo.getApiId());

        AssertUtil.notNull(apiServiceVo, "接口不存在，path=" + scriptPath);

        AssertUtil.notNull(targetApiVersionVo, "目标参数版本不存在，path=" + scriptPath + ",version:" + versionId);

        // todo~！
        List<ParamVo> paramVoList = apiServiceParamDao.queryByVersionId(targetApiVersionVo.getId());


        List<QueryParamVo> queryParamVoList = new ArrayList<>();

        Map<String, ParamVo> paramMap = paramVoList.stream()
                .collect(Collectors.toMap(ParamVo::getName, k -> k, (k, v) -> k));
        Map<String, Object> variableMap = getVariable(apiServiceVo, versionId);
        paramMap.keySet()
                .forEach(keyItem -> {
                    ParamVo paramVo = paramMap.get(keyItem);
                    QueryParamVo queryParamVo = ModelMapperUtil.strictMap(paramVo, QueryParamVo.class);
                    queryParamVo.setTestValue(variableMap.containsKey(keyItem) ? variableMap.get(keyItem).toString() : "");
                    queryParamVo.setRequireStr(RequireEnum.getEnum(paramVo.getRequired()).getName());
                    queryParamVo.setType(paramVo.getType());

                    queryParamVoList.add(queryParamVo);
                });

        return queryParamVoList;
    }

    @Override
    public List<ApiVersionVo> queryApiVersionById(Long serviceId) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(serviceId);
        return apiVersionVoList;
    }

    private Map<String, Object> getVariable(ApiServiceVo apiServiceVo, Long versionId) {
        Map<String, Object> variableMap = null;
        ApiVersionVo apiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(versionId);
        if (null != apiServiceVo) {
            try {
                Pair<Object, ArrayList<String[]>> collect = queryBml(apiServiceVo.getCreator(), apiVersionVo.getBmlResourceId(),
                        apiVersionVo.getBmlVersion(), apiServiceVo.getScriptPath());

                variableMap = (Map) ((Map) collect.getFirst()).get("variable");
            } catch (IOException e) {
                throw new ApiServiceRuntimeException(e.getMessage(), e);
            }
        }
        return null == variableMap ? Collections.EMPTY_MAP : variableMap;
    }

    private Pair<Object, ArrayList<String[]>> queryBml(String userName, String resourceId, String version,
                                                       String scriptPath) throws IOException {
        String key = String.join("-", resourceId, version);
        Pair<Object, ArrayList<String[]>> collect = bmlCache.getIfPresent(key);

        if (collect == null) {
            synchronized (this) {
                collect = bmlCache.getIfPresent(key);
                if (collect == null) {
                    BmlDownloadResponse resource;
                    if (version == null) {
                        resource = client.downloadResource(userName, resourceId, null);
                    } else {
                        resource = client.downloadResource(userName, resourceId, version);
                    }

                    AssertUtil.isTrue(resource.isSuccess(), "查询bml错误");

                    try (InputStream inputStream = resource.inputStream();
                            FileSource fileSource = FileSource$.MODULE$.create(new FsPath(scriptPath), inputStream)) {
                        //todo   数组取了第一个
                        collect = fileSource.collect()[0];
                        bmlCache.put(key, collect);
                    }
                }
            }
        }


        return collect;
    }

    private Map<String, String> queryConfigParam(long apiId, String version,String bmlVersion) {
        String key = String.join("-", apiId + "", version,bmlVersion);
        Map<String, String> collect = configParamCache.getIfPresent(key);

        if (collect == null) {
            synchronized (this) {
                collect = configParamCache.getIfPresent(key);
                if (collect == null) {
                    List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(apiId);
                    ApiVersionVo apiVersionVo = apiVersionVoList.stream().filter(apiVersionVoTmp -> apiVersionVoTmp.getVersion().equals(version) && apiVersionVoTmp.getBmlVersion().equals(bmlVersion)).findFirst().orElse(null);

                    collect = apiServiceParamDao.queryByVersionId(apiVersionVo.getId())
                            .stream()
                            .collect(toMap(ParamVo::getName, ParamVo::getType, (type1, type2) -> type2));
                    configParamCache.put(key, collect);
                }
            }
        }

        return collect;
    }


//    private Tuple3 getDatasourceInfo(final Map<String, Object> datasourceMap) {
//        Tuple3 tuple3 = datasourceCache.getIfPresent(datasourceMap);
//
//        if (tuple3 == null) {
//            synchronized (this) {
//                tuple3 = datasourceCache.getIfPresent(datasourceMap);
//                if (tuple3 == null) {
//                    tuple3 = JdbcUtil.getDatasourceInfo(datasourceMap);
//                    datasourceCache.put(datasourceMap, tuple3);
//                }
//            }
//        }
//
//        return tuple3;
//    }

//    private List<Map<String, Object>> executeJob(String executeCode,
//                                                 Object datasourceMap, Map<String, Object> params) {
//
////        Tuple3 tuple3 = getDatasourceInfo((Map<String, Object>) datasourceMap);
////        final String jdbcUrl = tuple3._1().toString();
////        final String username = tuple3._2().toString();
////        final String password = tuple3._3().toString();
//
////        NamedParameterJdbcTemplate namedParameterJdbcTemplate = datasourceService.getNamedParameterJdbcTemplate(jdbcUrl, username, password);
//
//        String namedSql = genNamedSql(executeCode, params);
//
////        return namedParameterJdbcTemplate.query(namedSql, new MapSqlParameterSource(params), new ColumnAliasMapRowMapper());
//
//    }

    private static String genNamedSql(String executeCode, Map<String, Object> params) {
        // 没有参数，无需生成namedSql
        if (MapUtils.isEmpty(params)) {
            return executeCode;
        }

        for (String paramName : params.keySet()) {
            for (String $name : new String[]{"'${" + paramName + "}'", "${" + paramName + "}", "\"${" + paramName + "}\""}) {
                if (executeCode.contains($name)) {
                    executeCode = StringUtils.replace(executeCode, $name, ":" + paramName);
                    break;
                }
            }
        }

        return executeCode;
    }


    public static class ColumnAliasMapRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            Map<String, Object> mapOfColValues = createColumnMap(columnCount);
            Map<String, Integer> mapOfColSuffix = new LinkedCaseInsensitiveMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String key = getColumnKey(JdbcUtils.lookupColumnName(rsmd, i));
                if (mapOfColValues.containsKey(key)) {
                    if (!mapOfColSuffix.containsKey(key)) {
                        mapOfColSuffix.put(key, 1);
                    } else {
                        mapOfColSuffix.put(key, mapOfColSuffix.get(key) + 1);
                    }

                    key = key + "_" + mapOfColSuffix.get(key);
                }

                Object obj = getColumnValue(rs, i);
                mapOfColValues.put(key, obj);
            }
            return mapOfColValues;
        }

        protected Map<String, Object> createColumnMap(int columnCount) {
            return new LinkedCaseInsensitiveMap<>(columnCount);
        }

        protected String getColumnKey(String columnName) {
            return columnName;
        }

        protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
            return JdbcUtils.getResultSetValue(rs, index);
        }

    }

    @Override
    public ApiServiceJob getJobByTaskId(String taskId) {
        ApiServiceBean apiServiceBean = apiServiceBeanDao.selectByTaskId(taskId);
        ApiServiceJob apiServiceJob = new ApiServiceJob();
        apiServiceJob.setSubmitUser(apiServiceBean.getSubmitUser());
        apiServiceJob.setProxyUser(apiServiceBean.getProxyUser());
        JobExecuteResult jobExecuteResult = new JobExecuteResult();
        jobExecuteResult.setTaskID(apiServiceBean.getTaskID());
        jobExecuteResult.setExecID(apiServiceBean.getExecID());
        jobExecuteResult.setUser(apiServiceBean.getUser());
        apiServiceJob.setJobExecuteResult(jobExecuteResult);
        return apiServiceJob;
    }

    @Override
    public void updateTaskStatus(String taskId, String status) {
        if (ExecutionNodeStatus.isCompleted(ExecutionNodeStatus.valueOf(status))) {
            ApiAccessVo apiAccessVo = new ApiAccessVo();
            apiAccessVo.setTaskID(taskId);
            apiAccessVo.setTaskStatus(status);
            apiServiceAccessDao.updateTaskStatus(apiAccessVo);
        }
    }

    @Override
    public List<DataApiServiceVo> getExecuteHistory(String username, Long apiId, Long apiVersionId) {
        List<String> taskIds = apiServiceAccessDao.getExecuteTaskIds(apiId, apiVersionId, username, TASKiD_LIMIT_SIZE);
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        //分割list
        List<List<String>> listTaskids = splitList(taskIds, ARRAY_SIZE);
        List<DataApiServiceVo> listResult = new ArrayList<>();
        // 使用创建用户查询历史记录
        ApiVersionVo apiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(apiVersionId);
        Map<String, String> headers = new HashMap<>();
        headers.put(LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY, apiVersionVo.getCreator());
        listTaskids.stream().forEach(itemIds -> {
            Map<String, String> map = new HashMap<>();
            map.put("taskID", itemIds.stream().collect(Collectors.joining(",")));
            SimpleHttpResponse reponse = HttpClientUtil.invokeGet(LINKIS_GATEWAY_URL + APISERVICE_GET_JOBHISTORY_URL, headers, map, "utf-8");
            if (reponse.getStatusCode() != 200) {
                LOG.error("get task result failed. message:{}", reponse.getBody());
                throw new DSSRuntimeException("get task result failed：" + reponse.getBody());
            }
            JsonArray jsonArray = new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                    .getAsJsonObject("data")
                    .getAsJsonArray("jobHistoryList");
            List<DataApiServiceVo> data = DSSCommonUtils.COMMON_GSON.fromJson(jsonArray, new TypeToken<List<DataApiServiceVo>>() {
            }.getType());
            listResult.addAll(data);
        });
        return listResult;
    }

    @Override
    public String getHistoryQueryParams(String username, Long taskId) {
        return apiServiceAccessDao.getHistoryQueryParams(username, taskId);
    }

    private static <T> List<List<T>> splitList(List<T> list, int batchSize) {
        return list.size() > batchSize ? IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList()) : Collections.singletonList(list);
    }


    private static String getRunTypeFromScriptsPath(String scriptsPath) {

        String res;
        String fileFlag = scriptsPath.substring(scriptsPath.lastIndexOf(".") + 1);
        switch (fileFlag) {
            case "sh":
                res = "shell";
                break;
            case "py":
                res = "pyspark";
                break;
            default:
                res = fileFlag;
                break;
        }
        return res;

    }
}
