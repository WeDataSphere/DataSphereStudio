package com.webank.wedatasphere.dss.apiservice.core.stategy.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.execute.DefaultApiServiceJob;
import com.webank.wedatasphere.dss.apiservice.core.execute.ExecuteCodeHelper;
import com.webank.wedatasphere.dss.apiservice.core.execute.LinkisJobSubmit;
import com.webank.wedatasphere.dss.apiservice.core.response.SimpleHttpResponse;
import com.webank.wedatasphere.dss.apiservice.core.stategy.ExecutionEngineService;
import com.webank.wedatasphere.dss.apiservice.core.util.HttpClientUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.FileContentVo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.*;

/**
 * @author v_kangkangyuan
 */
@Service
public class ApiHiveEngineImpl implements ExecutionEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiHiveEngineImpl.class);

    private static final String HIVE_ENGINE_TYPE = "hive";
    private static final String HIVE_HQL = "hql";
    /**
     * MANAGED_TABLE表示由数据库系统管理的表
     */
    private static final String TABLE_TYPE = "MANAGED_TABLE";
    private static final int SUCCESS_CODE = 200;

    private static final int REQUEST_TIME_OUT= 60000;

    @Override
    public boolean getEngineInstance(String engineType) {
        return HIVE_ENGINE_TYPE.contains(engineType);
    }

    /**
     * set差异化参数
     * @param job
     * @param executeCode
     * @param maxApiVersionVo
     * @param apiServiceVo
     */
    @Override
    public void setParams(ApiServiceExecuteJob job, String executeCode, ApiVersionVo maxApiVersionVo, ApiServiceVo apiServiceVo) {
        //sql代码封装成explain执行
        job.setCode(ExecuteCodeHelper.packageCodeToExplain(executeCode));
        job.setEngineType(apiServiceVo.getType());
        job.setRunType(HIVE_HQL);
    }

    /**
     * 调用引擎执行脚本
     * @param job
     * @param paramTypes
     * @param maxApiVersionVo
     * @return
     * @throws ApiExecuteException
     */
    @Override
    public JobExecuteResult execute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo, String executeCode) throws ApiExecuteException {
        UJESClient ujesClient = LinkisJobSubmit.getClient(paramTypes);
        JobExecuteResult jobExecuteResult = LinkisJobSubmit.execute(job, ujesClient);
        job.setJobExecuteResult(jobExecuteResult);
        try {
            ExecuteCodeHelper.waitForComplete(job, ujesClient);
        } catch (ApiExecuteException e) {
            LOGGER.error("Reason for failure: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failed to execute job", e);
            String reason = ExecuteCodeHelper.getLog(job, ujesClient);
            LOGGER.error("Reason for failure: " + reason);
            throw new ApiExecuteException(800024, "数据服务SQL执行出错,请检查SQL后重新执行。" + e.getMessage());
        }

        // 比较explain中的元数据表与数据库中的是否一致
        List<String> mateDataTables = Arrays.stream(maxApiVersionVo.getMetadataInfo().replaceAll("[\\[\\]]", "").split(", "))
                .collect(Collectors.toList());

        if (!equalsLists(getExplainTables(job.getUser(), jobExecuteResult.getTaskID()), mateDataTables)) {
            throw new ApiExecuteException(800024 , "库表名和发布时的不一致,不能进行执行!");
        }

        job.setCode(executeCode);
        return LinkisJobSubmit.execute(job, ujesClient);
    }

    /**
     * 用于数据API新增/修改，获取元数据
     * @param loginUser
     * @param executeCode
     * @param params
     * @param scriptPath
     * @return
     * @throws ApiExecuteException
     */
    @Override
    public Map<String, Object> getMetaDataInfoByExecute(String loginUser, String executeCode, Map<String, Object> params, String scriptPath) throws ApiExecuteException {
        Map<String, String> props = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        UJESClient client = LinkisJobSubmit.getClient(props);
        ApiServiceExecuteJob job = new DefaultApiServiceJob();
        job.setCode(ExecuteCodeHelper.packageCodeToExplain(executeCode));
        job.setEngineType(HIVE_ENGINE_TYPE);
        job.setRunType(HIVE_HQL);
        job.setUser(loginUser);
        job.setParams(null);
        job.setRuntimeParams((Map<String, Object>) params.get("variable"));
        job.setScriptePath(scriptPath);
        JobExecuteResult jobExecuteResult = LinkisJobSubmit.execute(job, client, "IDE");
        job.setJobExecuteResult(jobExecuteResult);
        try {
            ExecuteCodeHelper.waitForComplete(job, client);
        } catch (ApiExecuteException e) {
            LOGGER.error("Reason for failure: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failed to execute job", e);
            String reason = ExecuteCodeHelper.getLog(job, client);
            LOGGER.error("Reason for failure: " + reason);
            throw new ApiExecuteException(800024, "数据服务SQL执行出错,请检查SQL后重新执行。" + e.getMessage());
        }
        resultMap.put("0", getExplainTables(loginUser, jobExecuteResult.getTaskID()));
        return resultMap;
    }

    /**
     * 获取执行计划中的表
     * @param proxyUser 代理用户
     * @param taskId 任务ID
     * @return 执行计划中的表
     */
    private List<String> getExplainTables(String proxyUser, String taskId) throws ApiExecuteException {
        //获取结果集文件
        List<String> resultPaths = getResultFiles(proxyUser, taskId);
        if (CollectionUtils.isEmpty(resultPaths)) {
            throw new ApiExecuteException(800024, proxyUser + " get result file failed!");
        }

        // 根据文件获取结果集，并解析元数据表
        List<String> explainTables = absolveExplainTables(resultPaths, proxyUser);
        if (CollectionUtils.isEmpty(resultPaths)) {
            throw new ApiExecuteException(800024 ,proxyUser + " absolve explain error!");
        }
        return explainTables;
    }

    /**
     * 调用linkis接口，根据文件路径获取结果集文件
     * @param proxyUser 代理用户
     * @param taskId 任务ID
     * @return 结果集文件列表
     */
    private List<String> getResultFiles(String proxyUser, String taskId) throws ApiExecuteException {
        Map<String, String> queryMap = new HashMap<>();
        String resultLocation = getResultLocation(proxyUser, taskId);
        if(StringUtils.isEmpty(resultLocation)){
            throw new ApiExecuteException(800024 ,proxyUser + " result file path failed!");
        }
        queryMap.put("path", resultLocation);
        LOGGER.info("begin to get result file...");
        SimpleHttpResponse filePathResponse = HttpClientUtil.invokeGet(LINKIS_GATEWAY_URL + APISERVICE_GET_DIRFILE_URL, buildReqHeader(proxyUser), queryMap, "utf-8");
        if (filePathResponse.getStatusCode() != SUCCESS_CODE) {
            LOGGER.error("user {} get result file failed. message:{}", proxyUser, filePathResponse.getBody());
            throw new ApiExecuteException(800024 ,proxyUser + " get result file failed：" + filePathResponse.getBody());
        }
        JsonArray jsonArray = new JsonParser().parse(filePathResponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("dirFileTrees")
                .getAsJsonArray("children");
        if (jsonArray == null) {
            throw new ApiExecuteException(800024 ,proxyUser + " get result file failed：" + filePathResponse.getBody());
        }
        // 可能有多个结果集文件
        List<String> resultPaths = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            JsonObject childObject = element.getAsJsonObject();
            String path = childObject.get("path").getAsString();
            resultPaths.add(path);
        }
        return resultPaths;
    }

    /**
     * 根据结果集文件路径，调用linkis接口获取结果集，返回执行计划元数据表
     * @param resultPaths 结果集文件路径
     * @param proxyUser 代理用户
     * @return 执行计划元数据表
     */
    private List<String> absolveExplainTables(List<String> resultPaths, String proxyUser) {
        LOGGER.info("user:{} begin to get explain result!", proxyUser);
        List<String> explainTables = new ArrayList<>();
        resultPaths.forEach(item -> {
            Map<String, String> map = new HashMap<>();
            map.put("path", item);
            SimpleHttpResponse resultResponse = HttpClientUtil.invokeGet(LINKIS_GATEWAY_URL + APISERVICE_OPEN_RESULT_URL, buildReqHeader(proxyUser), map, "utf-8");
            if (resultResponse.getStatusCode() != SUCCESS_CODE) {
                LOGGER.error("user {} get explain result failed. message:{}", proxyUser ,resultResponse.getBody());
                throw new DSSRuntimeException(proxyUser + " get explain result failed：" + resultResponse.getBody());
            }
            //解析执行计划
            JsonArray fileContentArray = new JsonParser().parse(resultResponse.getBody()).getAsJsonObject()
                    .getAsJsonObject("data")
                    .getAsJsonArray("fileContent")
                    .get(0)
                    .getAsJsonArray();

            List<String> resultsTables = IntStream.range(0, fileContentArray.size())
                    .mapToObj(index -> DSSCommonUtils.COMMON_GSON.fromJson(fileContentArray.get(index).getAsString(), FileContentVo.class))
                    .flatMap(fileContentVo -> Arrays.stream(fileContentVo.getInput_tables()))
                    .filter(inputTable -> inputTable.getTabletype().equals(TABLE_TYPE))
                    .map(inputTable -> inputTable.getTablename().replaceAll("@", "."))
                    .collect(Collectors.toList());
            explainTables.addAll(resultsTables);
        });
        return explainTables;
    }

    /**
     * 调用linkis get接口获取结果集文件所在路径
     * @param username 登录用户/代理用户
     * @param taskId 任务ID
     * @return 结果集文件路径
     */
    private String getResultLocation(String username, String taskId) throws ApiExecuteException {
        LOGGER.info("user {} begin to get result file path, taskId: {}", username, taskId);
        long startTime = System.currentTimeMillis();
        // 在一分钟之内如果获取不到则表示接口超时，抛出异常
        while (System.currentTimeMillis() - startTime < REQUEST_TIME_OUT) {
            SimpleHttpResponse filePathResponse = HttpClientUtil.invokeGet(String.format(LINKIS_GATEWAY_URL + APISERVICE_GET_RESULTLOCATION_URL, taskId), buildReqHeader(username), null, "utf-8");
            if (filePathResponse.getStatusCode() == SUCCESS_CODE) {
                String resultLocation = new JsonParser().parse(filePathResponse.getBody()).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonObject("task")
                        .get("resultLocation").getAsString();

                if (!StringUtils.isEmpty(resultLocation)) {
                    return resultLocation; // 获取到resultLocation不为空时直接返回
                }
            } else {
                LOGGER.error("user {} get result file path failed. message:{}", username, filePathResponse.getBody());
                throw new ApiExecuteException(800024, username + " get result file path failed：" + filePathResponse.getBody());
            }
            Utils.sleepQuietly(ApiServiceConfiguration.LINKIS_JOB_REQUEST_STATUS_TIME.getValue()); // 等待一段时间再继续获取
        }
        // 超时仍未获取到resultLocation，抛出异常
        throw new ApiExecuteException(800025, "Failed to get result location within 1 minute");
    }

    /**
     * 构建请求头
     * @param username 登录用户
     * @return 请求头
     */
    private Map<String, String> buildReqHeader(String username) {
        Map<String, String> headers = new HashMap<>();
        headers.put(LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY, username);
        return headers;
    }

    /**
     * 比较执行计划中的元数据元素与表中的元数据元素是否相同
     * @param explainTables 执行计划元数据表
     * @param mateDataTables 数据库元数据表
     * @return true/false
     */
    private boolean equalsLists(List<String> explainTables, List<String> mateDataTables) {
        Set<String> explainSets = new HashSet<>(explainTables);
        Set<String> mateDataSets = new HashSet<>(mateDataTables);
        // 执行计划元素大于元数据元素则说明提单表缺少了，直接返回false
        if(explainSets.size() > mateDataSets.size()){
            return false;
        }
        return mateDataSets.containsAll(explainSets);
    }
}
