package com.webank.wedatasphere.dss.apiservice.core.stategy.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.APISERVICE_GET_DIRFILE_URL;
import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.LINKIS_GATEWAY_URL;

@Service
public class ApiJdbcEngineImpl implements ExecutionEngineService {


    private static final Logger LOGGER = LoggerFactory.getLogger(ApiJdbcEngineImpl.class);

    private static final String JDBC_ENGINE_TYPE = "jdbc";

    private static final String JDBC_SQL = "jdbc";


    @Override
    public boolean getEngineInstance(String engineType) {
        return JDBC_ENGINE_TYPE.contains(engineType);
    }

    @Override
    public void setParams(ApiServiceExecuteJob job, String executeCode, ApiVersionVo maxApiVersionVo, ApiServiceVo apiServiceVo) {
        //sql代码封装成explain执行
        job.setCode(executeCode);
        job.setEngineType(apiServiceVo.getType());
        job.setRunType(JDBC_SQL);
    }

    @Override
    public Map<String, Object> getMetaDataInfoByExecute(String loginUser, String executeCode, Map<String, Object> params, String scriptPath) throws ApiExecuteException {
        Map<String, String> props = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        String code = ExecuteCodeHelper.jdbcPackageCodeToExplain(executeCode);
        if(StringUtils.isEmpty(code)){

            resultMap.put("0", "[]");

        }else{

            UJESClient client = LinkisJobSubmit.getClient(props);
            ApiServiceExecuteJob job = new DefaultApiServiceJob();
            job.setCode(code);
            job.setEngineType(JDBC_ENGINE_TYPE);
            job.setRunType(JDBC_SQL);
            job.setUser(loginUser);
            job.setParams(null);
            Map<String,Object> configuration = (Map<String, Object>) params.get("configuration");
            job.setRuntimeParams((Map<String, Object>) configuration.get("runtime"));
            job.setVariableMap((Map<String, Object>) params.get("variable"));
            job.setScriptePath(scriptPath);
            JobExecuteResult jobExecuteResult = LinkisJobSubmit.executeNew(job, client, "IDE");
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

            resultMap.put("0", ExecuteCodeHelper.parseSqlTable(executeCode));

        }

        return resultMap;
    }

    @Override
    public JobExecuteResult execute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo, String executeCode) throws ApiExecuteException {
        UJESClient ujesClient = LinkisJobSubmit.getClient(paramTypes);
        return LinkisJobSubmit.executeNew(job, ujesClient,"IDE");
    }





}





