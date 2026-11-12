package com.webank.wedatasphere.dss.framework.proxy.conf;

import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import org.apache.linkis.common.conf.CommonVars;

/**
 * @author enjoyyin
 * @date 2022-09-05
 * @since 0.5.0
 */
public class ProxyUserConfiguration {

    public static boolean isProxyUserEnable() {
        return CommonVars.apply(DSSCommonConf.ALL_GLOBAL_LIMITS_PREFIX.acquireNew() + "proxyEnable", false).acquireNew();
    }

    public static final CommonVars<String> DS_TRUST_TOKEN = CommonVars.apply("wds.dss.trust.token", "");
    public static final CommonVars<Boolean> DS_PROXY_SELF_ENABLE = CommonVars.apply("wds.dss.proxy.self.enable", true);

    /* ==================== AI 用户切换（REQ-01，1.26.0） ====================
     * 配置 key 与 dss-framework-compute-resource-manager-webank 的 LinkisConnConf 完全同源
     * （设计 §1.1.5 零新增配置项；OPT-D1 定论：模块内自声明同 key 常量，不引入跨模块编译依赖）。
     * 基址/Token 已被既有生产调用使用，无需新增部署配置。
     */
    /** Linkis 网关基址（与 LinkisConnConf.LINKIS_URL 同 key） */
    public static final CommonVars<String> LINKIS_GATEWAY_URL =
            CommonVars.apply("wds.linkis.gateway.url.v1", "");
    /** governanceStationAdmin 服务内路径（与 LinkisConnConf.CONF_GOVERNANCE_ADMIN_PATH 同源拼接） */
    public static final String GOVERNANCE_STATION_ADMIN_PATH = "/api/rest_j/v1/jobhistory/governanceStationAdmin";
    /** Linkis admin Token 认证 header（与 LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_* 同 key） */
    public static final CommonVars<String> LINKIS_ADMIN_TOKEN_KEY =
            CommonVars.apply("wds.dss.linkis.resource.admin.token.key", "Token-Code");
    public static final CommonVars<String> LINKIS_ADMIN_TOKEN_VALUE =
            CommonVars.apply("wds.dss.linkis.resource.admin.token.value", "");
    public static final CommonVars<String> LINKIS_ADMIN_TOKEN_USER_KEY =
            CommonVars.apply("wds.dss.linkis.resource.admin.token.user.key", "Token-User");
    /** aiSwitch 查询超时（毫秒），独立于 HttpClientUtil 统一超时，收敛用户交互路径延迟（设计 §1.1.6） */
    public static final CommonVars<Integer> AI_SWITCH_QUERY_TIMEOUT_MS =
            CommonVars.apply("wds.dss.proxy.ai.switch.query.timeout.ms", 3000);
    /** AI 通道用户后缀（与 Linkis linkis.ai.user.suffix 默认值对齐，设计 §1.1.3：本版本不做 DSS 侧可配置化） */
    public static final String AI_USER_SUFFIX = "_ai";
    /** 硬约束：hadoop 用户不允许切换 _ai 用户（需求方 2026-11-12 确认，设计 §1.1.3 R2.1 两拒口径） */
    public static final String HADOOP_USER = "hadoop";

}
