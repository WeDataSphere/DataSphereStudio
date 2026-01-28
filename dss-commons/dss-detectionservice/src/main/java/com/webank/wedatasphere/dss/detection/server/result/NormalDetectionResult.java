package com.webank.wedatasphere.dss.detection.server.result;

/**
 * @date 2023/3/15 11:43
 */
public class NormalDetectionResult extends DetectionResult {

    public NormalDetectionResult () {
        super();
        this.setResultType(ResultType.NORMAL);
    }

}
