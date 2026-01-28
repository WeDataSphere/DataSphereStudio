package com.webank.wedatasphere.dss.apiservice.core.stategy.impl;

import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.stategy.EngineManagerService;
import com.webank.wedatasphere.dss.apiservice.core.stategy.ExecutionEngineService;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author v_kangkangyuan
 */
@Service
public class EngineManagerServiceImpl implements EngineManagerService {

    @Autowired
    private List<ExecutionEngineService> engineServices;

    @Override
    public JobExecuteResult engineExecute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo, ApiServiceVo apiServiceVo, String executeCode) throws ApiExecuteException {
        getEngineServiceInstance(apiServiceVo.getType()).setParams(job, executeCode, maxApiVersionVo, apiServiceVo);
        return getEngineServiceInstance(apiServiceVo.getType()).execute(job, paramTypes, maxApiVersionVo, executeCode);
    }

    @Override
    public Map<String, Object> getMetadataTableInfos(String loginUser, String engineType, String executeCode, Map<String, Object> matedata, String scriptisPath) throws ApiExecuteException {
        return getEngineServiceInstance(engineType).getMetaDataInfoByExecute(loginUser, executeCode, matedata, scriptisPath);
    }

    private ExecutionEngineService getEngineServiceInstance(String engineType){
        Optional<ExecutionEngineService> engineInstance = engineServices.stream()
                .filter(service -> service.getEngineInstance(engineType))
                .findFirst();
        if (!engineInstance.isPresent()) {
            throw new DSSRuntimeException("不支持" + engineType + "类型的引擎！");
        }
        return engineInstance.get();
    }


}
