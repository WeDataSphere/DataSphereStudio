package com.webank.wedatasphere.dss.apiservice.core.stategy.impl;

import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.execute.DefaultApiServiceJob;
import com.webank.wedatasphere.dss.apiservice.core.execute.ExecuteCodeHelper;
import com.webank.wedatasphere.dss.apiservice.core.execute.LinkisJobSubmit;
import com.webank.wedatasphere.dss.apiservice.core.stategy.ExecutionEngineService;
import com.webank.wedatasphere.dss.apiservice.core.util.SQLCheckUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author v_kangkangyuan
 */
@Service
public class ApiSparkEngineImpl implements ExecutionEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiSparkEngineImpl.class);

    private static final String SPARK_ENGINE_TYPE = "spark";
    private static final String SPARK_SCALA = "scala";

    private static final String SPARK_PY = "py";

    @Override
    public boolean getEngineInstance(String engineType) {
        return SPARK_ENGINE_TYPE.contains(engineType);
    }

    @Override
    public void setParams(ApiServiceExecuteJob job, String executeCode, ApiVersionVo maxApiVersionVo, ApiServiceVo apiServiceVo) {

        if(SQLCheckUtil.isPySpark(job.getScriptPath())){
            // pySpark代码
            job.setCode(executeCode);
            job.setEngineType(apiServiceVo.getType());
            job.setRunType(SPARK_PY);

        }else{
            //sql代码封装成scala执行
            job.setCode(ExecuteCodeHelper.packageCodeToExecute(executeCode, maxApiVersionVo.getMetadataInfo()));
            job.setEngineType(apiServiceVo.getType());
            job.setRunType(SPARK_SCALA);
        }

    }

    @Override
    public Map<String,Object> getMetaDataInfoByExecute(String loginUser, String executeCode,Map<String, Object> params,String scriptPath ) throws ApiExecuteException {
        Map<String, String> props = new HashMap<>();
        Map<String,Object>  resultMap = new HashMap<>();
        UJESClient client = LinkisJobSubmit.getClient(props);
        ApiServiceExecuteJob job = new DefaultApiServiceJob();
        //sql代码封装成scala执行
        job.setCode(ExecuteCodeHelper.packageCodeToRelease(executeCode));
        job.setEngineType("spark");
        job.setRunType("scala");
        job.setUser(loginUser);
        job.setParams(null);
        job.setRuntimeParams((Map<String,Object>)params.get("variable"));// pattern注入
        job.setScriptePath(scriptPath);
        JobExecuteResult jobExecuteResult = LinkisJobSubmit.execute(job,client, "IDE");
        job.setJobExecuteResult(jobExecuteResult);
        try {
            ExecuteCodeHelper.waitForComplete(job,client);
        } catch (ApiExecuteException e){
            LOGGER.error("Reason for failure: " + e.getMessage(),e);
            throw e;
        }catch (Exception e) {
            LOGGER.warn("Failed to execute job", e);
            String reason = ExecuteCodeHelper.getLog(job,client);
            LOGGER.error("Reason for failure: " + reason);
            throw new ApiExecuteException(800024,"数据服务SQL执行出错,请检查SQL后重新执行。"+e.getMessage());
        }

        int resultSize = ExecuteCodeHelper.getResultSize(job,client);
        for(int i =0; i < resultSize; i++){
            String result = ExecuteCodeHelper.getResult(job, i, ApiServiceConfiguration.RESULT_PRINT_SIZE.getValue().intValue(),client);
            LOGGER.info("The content of the " + (i + 1) + "th resultset is :"
                    +  result);
            resultMap.put(Integer.toString(i),result);

        }

        LOGGER.info("Finished to execute job");
        return  resultMap;
    }

    @Override
    public JobExecuteResult execute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo, String executeCode) throws ApiExecuteException {
        UJESClient ujesClient = LinkisJobSubmit.getClient(paramTypes);
        return LinkisJobSubmit.execute(job, ujesClient);
    }
}
