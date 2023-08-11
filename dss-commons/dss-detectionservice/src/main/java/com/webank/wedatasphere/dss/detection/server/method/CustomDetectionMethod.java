package com.webank.wedatasphere.dss.detection.server.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @date 2023/3/15 11:28
 */
public class CustomDetectionMethod implements DetectionMethod {

    String methodName = "custom-scanrules";

    private static final Logger logger = LoggerFactory.getLogger(CustomDetectionMethod.class);

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String detectLine(String line) {
        // 校验line
        if (line == null || line.length() == 0) {
            return null;
        }
        //todo 待实现
        return line;
    }
}
