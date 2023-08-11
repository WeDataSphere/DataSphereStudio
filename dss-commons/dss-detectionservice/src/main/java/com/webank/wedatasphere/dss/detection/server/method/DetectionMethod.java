package com.webank.wedatasphere.dss.detection.server.method;

/**
 * @date 2023/3/15 11:19
 */
public interface DetectionMethod {


    /**
     * @return 检测方法的名字
     */
    String getMethodName();

    /**
     * @param line 待检测输入的文本信息
     * @return 检测的敏感数据，如无则返回空字符串
     */
    String detectLine(String line);
}