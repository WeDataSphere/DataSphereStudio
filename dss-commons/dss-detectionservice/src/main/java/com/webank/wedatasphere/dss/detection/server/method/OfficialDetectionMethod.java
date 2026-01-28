package com.webank.wedatasphere.dss.detection.server.method;

import com.webank.utils.Sense;
import com.webank.wedatasphere.dss.detection.server.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @date 2023/3/15 11:28
 */
public class OfficialDetectionMethod implements DetectionMethod {

    String methodName = "webank-scanrules";

    private static final Logger logger = LoggerFactory.getLogger(OfficialDetectionMethod.class);

    // 校验规则
    private int rules = Sense.SenseType.ID | Sense.SenseType.BANK | Sense.SenseType.PHONE | Sense.SenseType.EMAIL
            | Sense.SenseType.HOME | Sense.SenseType.CAR | Sense.SenseType.CN | Sense.SenseType.OPENID;

    @Override
    public String getMethodName() {
        return methodName;
    }

    //todo 待实现：接入webank官方提供的scanrules包
    @Override
    public String detectLine(String line) {
        // 校验line
        if (line == null || line.length() == 0) {
            return null;
        }
        // 使用webank官方scanrules包检测敏感信息
        String senseInfo = Sense.auditSenseInfo(line, rules, false, false);
        if (senseInfo == null || senseInfo.length() == 0) {
            return null;
        }
        return senseInfo;
    }
}
