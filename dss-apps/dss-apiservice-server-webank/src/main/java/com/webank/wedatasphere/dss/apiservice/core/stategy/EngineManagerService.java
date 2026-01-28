package com.webank.wedatasphere.dss.apiservice.core.stategy;

import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import org.apache.linkis.ujes.client.response.JobExecuteResult;

import java.util.Map;

public interface EngineManagerService {

    JobExecuteResult engineExecute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo,ApiServiceVo apiServiceVo, String executeCode) throws ApiExecuteException;

    Map<String, Object> getMetadataTableInfos(String loginUser, String engineType, String executeCode, Map<String, Object> matedata, String scriptisPath) throws ApiExecuteException;

}
