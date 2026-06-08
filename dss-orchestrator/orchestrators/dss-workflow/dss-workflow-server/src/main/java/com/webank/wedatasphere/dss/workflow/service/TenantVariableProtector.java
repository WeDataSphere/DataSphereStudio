package com.webank.wedatasphere.dss.workflow.service;

import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 租户变量保护工具类
 * 用于从flowJson中提取tenant变量值，以及保护tenant不被普通接口修改
 */
public class TenantVariableProtector {

    private static final Logger logger = LoggerFactory.getLogger(TenantVariableProtector.class);

    /**
     * 从flowJson中提取tenant变量的值
     *
     * @param flowJson 工作流JSON字符串
     * @return tenant变量的值，如果不存在则返回null
     */
    public static String extractTenantValue(String flowJson) {
        if (flowJson == null || flowJson.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, Object>> props = DSSCommonUtils.getFlowAttribute(flowJson, "props");
            if (props == null || props.isEmpty()) {
                return null;
            }
            for (Map<String, Object> prop : props) {
                if (prop.containsKey("tenant")) {
                    Object value = prop.get("tenant");
                    return value != null ? value.toString() : null;
                }
            }
        } catch (Exception e) {
            logger.warn("提取tenant变量值失败", e);
        }
        return null;
    }
}
