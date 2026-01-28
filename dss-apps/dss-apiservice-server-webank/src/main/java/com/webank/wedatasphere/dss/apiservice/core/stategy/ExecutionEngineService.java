package com.webank.wedatasphere.dss.apiservice.core.stategy;

import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ApiServiceExecuteJob;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import org.apache.linkis.ujes.client.response.JobExecuteResult;

import java.util.Map;

/**
 * @Author: v_kangkangyuan
 * @Date: 2024/02/27
 * @Description: 后续若需要支持其它引擎类型，只需要实现该类即可
 */
public interface ExecutionEngineService {

    /**
     * 根据类型确定使用哪种引擎
     * @param engineType
     * @return
     */
    boolean getEngineInstance(String engineType);

    /**
     * 执行引擎时，配置差异化参数
     * @param job
     * @param executeCode
     * @param maxApiVersionVo
     * @param apiServiceVo
     */
    void setParams(ApiServiceExecuteJob job, String executeCode, ApiVersionVo maxApiVersionVo, ApiServiceVo apiServiceVo);

    /**
     * 用于在数据服务发布、修改时解析获取元数据表
     * @param loginUser
     * @param executeCode
     * @param params
     * @param scriptPath
     * @return
     * @throws ApiExecuteException
     */
    Map<String, Object> getMetaDataInfoByExecute(String loginUser, String executeCode, Map<String, Object> params, String scriptPath) throws ApiExecuteException;

    /**
     * 提交任务到相应引擎执行
     * @param job
     * @param paramTypes
     * @param maxApiVersionVo
     * @param loginUser
     * @return
     * @throws ApiExecuteException
     */
    JobExecuteResult execute(ApiServiceExecuteJob job, Map<String, String> paramTypes, ApiVersionVo maxApiVersionVo, String executeCode) throws ApiExecuteException;


}
