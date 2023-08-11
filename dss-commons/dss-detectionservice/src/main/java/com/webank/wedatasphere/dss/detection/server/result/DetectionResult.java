package com.webank.wedatasphere.dss.detection.server.result;

import com.webank.wedatasphere.dss.detection.server.message.SubmitInformation;
import com.webank.wedatasphere.dss.detection.server.method.DetectionMethod;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @date 2023/3/15 11:34
 */
public abstract class DetectionResult {

    private List<String> sensitiveMsg;

    private List<String> sensitiveMsgMask;

    private DetectionMethod detectionMethod;

    private ResultType resultType;

    private SubmitInformation submitInformation;

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public SubmitInformation getSubmitInformation() {
        return submitInformation;
    }

    public void setSubmitInformation(SubmitInformation submitInformation) {
        this.submitInformation = submitInformation;
    }

    /**
     * 按照某种格式返回敏感信息字符串
     * @return
     */
    public String generateSensitiveText() {
        if(sensitiveMsg != null) {
            return String.join(",", sensitiveMsg);
        } else {
            return null;
        }
    }

    public Boolean hasSensitiveMsg() {
        return !CollectionUtils.isEmpty(sensitiveMsg);
    }

    public List<String> getSensitiveMsg() {
        return sensitiveMsg;
    }

    public void setSensitiveMsg(List<String> sensitiveMsg) {
        this.sensitiveMsg = sensitiveMsg;
    }

    public DetectionMethod getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(DetectionMethod detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public List<String> getSensitiveMsgMask() {
        return sensitiveMsgMask;
    }

    public void setSensitiveMsgMask(List<String> sensitiveMsgMask) {
        this.sensitiveMsgMask = sensitiveMsgMask;
    }
}
