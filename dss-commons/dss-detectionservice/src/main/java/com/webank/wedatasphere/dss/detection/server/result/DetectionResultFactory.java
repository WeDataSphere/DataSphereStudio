package com.webank.wedatasphere.dss.detection.server.result;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2023/3/15 14:31
 */
public class DetectionResultFactory {

    private List<DetectionResult> detectionResults;

    public DetectionResult getDetectionResult() {
        return getDetectionResult(ResultType.NORMAL);
    }

    public DetectionResult getDetectionResult(ResultType resultType) {
        //resultType是normal为默认情况，后续如果有新的结果集类型，可以在这里增加case
        switch (resultType) {
            default: {
                NormalDetectionResult normalDetectionResult = new NormalDetectionResult();
                normalDetectionResult.setSensitiveMsg(new ArrayList<>());
                normalDetectionResult.setSensitiveMsgMask(new ArrayList<>());
                return normalDetectionResult;
            }
        }
    }

}
