package com.webank.wedatasphere.dss.framework.proxy.restful;

import com.google.gson.Gson;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.DomainUtils;
import com.webank.wedatasphere.dss.common.utils.ScalaFunctionAdapter;
import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.dto.AiSwitchResponse;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUserImpl;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.exception.AppStandardWarnException;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.security.ProxyUserSSOUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import scala.Tuple2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.framework.common.conf.TokenConf.HPMS_USER_TOKEN;
import static com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration.DS_PROXY_SELF_ENABLE;
import static com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration.DS_TRUST_TOKEN;


public class DssProxyUserController {

    private Boolean sslEnable = (Boolean) ServerConfiguration.BDP_SERVER_SECURITY_SSL().getValue();
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected DssProxyUserService dssProxyUserService;

    @RequestMapping(path = {"list","listProxyUsers"}, method = RequestMethod.GET)
    public Message getProxyUserList(HttpServletRequest request) {
        if(!ProxyUserConfiguration.isProxyUserEnable()) {
            return Message.error("proxy user service is not enable, please ask admin for help.");
        }
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = new Workspace();
        try {
            workspace = SSOHelper.getWorkspace(request);
        } catch (AppStandardWarnException ignored) {}
        Workspace finalWorkspace = workspace;
        return DSSExceptionUtils.applyMessage(() -> dssProxyUserService.selectProxyUserList(username, finalWorkspace),
            userList -> {
               LOGGER.info("user {} got proxy list. userList: {}.", username, userList);
               List<String> proxyUserNameList = userList.stream().map(DssProxyUser::getProxyUserName).collect(Collectors.toList());
               if(DS_PROXY_SELF_ENABLE.getValue()&&!proxyUserNameList.contains(username)) {
                   proxyUserNameList.add(username);
               }
               return Message.ok().data("proxyUserList", proxyUserNameList);
           }, "fetch proxy user list failed.");
    }

    @RequestMapping(path = "getProxyUser", method = RequestMethod.GET)
    public Message getProxyUser(HttpServletRequest request) {
        if(!ProxyUserConfiguration.isProxyUserEnable()) {
            return Message.error("proxy user service is not enable, please ask admin for help.");
        }
        String username = SecurityFilter.getLoginUsername(request);
        LOGGER.info("user {} try to get proxy user.", username);
        return DSSExceptionUtils.getMessage(() -> dssProxyUserService.getProxyUser(request),
                proxyUser -> Message.ok().data("userName", username).data("proxyUser", proxyUser), "");
    }

    @RequestMapping(path = "setProxyUser", method = RequestMethod.POST)
    public Message setProxyUserCookie(@RequestBody DssProxyUserImpl userRep,
                                      HttpServletRequest req,
                                      HttpServletResponse resp) {
        String username = SecurityFilter.getLoginUsername(req);
        userRep.setUserName(username);
        if (StringUtils.isEmpty(userRep.getUserName())) {
            return Message.error("userName is null.");
        } else if (StringUtils.isEmpty(userRep.getProxyUserName())) {
            return Message.error("proxyUserName is null.");
        }
        LOGGER.info("user {} try to add user proxy cookie, params:{}.", username, userRep);
        // 兼容linkis端直接为登陆用户名加_c后缀的场景
        if (userRep.getProxyUserName().equals(username)) {
            userRep.setUserName(userRep.getProxyUserName());
        }
        Workspace workspace = new Workspace();
        try {
            workspace = SSOHelper.getWorkspace(req);
        } catch (AppStandardWarnException ignored) {} // ignore error
        try {
            if ((ProxyUserConfiguration.DS_PROXY_SELF_ENABLE.getValue() && userRep.getUserName().equalsIgnoreCase(userRep.getProxyUserName())) ||
                    dssProxyUserService.isExists(userRep.getUserName(), userRep.getProxyUserName(), workspace) ||
                    isAiSwitchBypass(username, userRep.getProxyUserName())) {
                ProxyUserSSOUtils.removeProxyUser(ScalaFunctionAdapter.doSupplier(req::getCookies));
            } else {
                LOGGER.info("user {} have no permission to proxy to user {}.", userRep.getUserName(), userRep.getProxyUserName());
                return Message.error("user " + userRep.getUserName() + " have no permission to proxy to user " + userRep.getProxyUserName());
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to set proxy user to cookie.", exception);
            return Message.error(ExceptionUtils.getRootCauseMessage(exception));
        }
        Tuple2<String, String> userTicketIdKv = ProxyUserSSOUtils.getProxyUserTicketKV(userRep.getProxyUserName(), DS_TRUST_TOKEN.getValue());
        Cookie cookie = new Cookie(userTicketIdKv._1, userTicketIdKv._2);
        cookie.setMaxAge(-1);
        if (sslEnable){
            cookie.setSecure(true);
        }
        cookie.setDomain(DomainUtils.getCookieDomain(req));
        cookie.setPath("/");
        resp.addCookie(cookie);
        return Message.ok("Success to add proxy user into cookie.");
    }

    @GetMapping("/getProxyUserName")
    public Message getProxyUserName(@RequestParam("userName")String userName){
        List<DssProxyUser> dssProxyUsers = dssProxyUserService.selectProxyUserList(userName, null);
        List<String> proxyUsernames = dssProxyUsers.stream().map(DssProxyUser::getProxyUserName).collect(Collectors.toList());
        return Message.ok().data("userNames", proxyUsernames);
    }

    /* ==================== AI 用户切换豁免（REQ-01，1.26.0） ====================
     * 设计依据：docs/1.26.0/design/REQ-01_新增AI用户切换_设计.md §1.1.2~§1.1.6 / §2.1 / §2.2。
     * 豁免语义：aiSwitch=true 且目标为当前登录用户派生的 _ai 用户时，跳过 isExists 代理授权校验。
     * 调用次序说明：豁免判定置于 isExists 之后（布尔等价重排）——仅本地无授权记录的请求才触发
     * Linkis 外部调用，本人切换分支（短路在前）与已有授权用户零额外开销（设计 §1.1.6 延迟目标）。
     */

    /**
     * AI 用户切换豁免判定（设计 §1.1.3 / §2.1）。
     * 判定次序：hadoop 两拒硬约束最先 → 豁免目标精确全等 → aiSwitch 实时查询。
     *
     * @param username      当前登录用户名（SecurityFilter.getLoginUsername；代理已生效时为代理后身份，
     *                      单源即覆盖设计"当前用户/当前代理用户"两个派生场景）
     * @param proxyUserName 目标代理用户
     * @return true 表示豁免 isExists 授权校验
     */
    private boolean isAiSwitchBypass(String username, String proxyUserName) {
        // R2.1 hadoop 两拒（硬约束：hadoop 用户不允许切换 _ai 用户；派生源 hadoop 的 hadoop_ai 目标同样拒绝）
        if (ProxyUserConfiguration.HADOOP_USER.equalsIgnoreCase(username) ||
                ProxyUserConfiguration.HADOOP_USER.equalsIgnoreCase(stripAiSuffix(proxyUserName))) {
            LOGGER.info("user {} try to switch to {} is rejected by hadoop ai-switch deny rule.", username, proxyUserName);
            return false;
        }
        // R2.4 豁免目标精确全等：仅 当前登录用户+_ai（他人派生 _ai 等任意其他目标不豁免，走原校验）
        if (!proxyUserName.equals(username + ProxyUserConfiguration.AI_USER_SUFFIX)) {
            return false;
        }
        // R1.1 aiSwitch 以当次请求实时查询为准，不做本地缓存
        boolean aiSwitch = queryAiSwitch(username);
        if (aiSwitch) {
            LOGGER.info("user {} bypass proxy authorization by aiSwitch, target {}.", username, proxyUserName);
        }
        return aiSwitch;
    }

    /**
     * 去除 _ai 后缀，用于 hadoop 两拒的派生源判定；非 _ai 结尾原样返回。
     */
    private String stripAiSuffix(String proxyUserName) {
        if (StringUtils.isEmpty(proxyUserName)) {
            return proxyUserName;
        }
        String suffix = ProxyUserConfiguration.AI_USER_SUFFIX;
        return proxyUserName.endsWith(suffix)
                ? proxyUserName.substring(0, proxyUserName.length() - suffix.length())
                : proxyUserName;
    }

    /**
     * 实时查询 Linkis 集群级 _ai 能力开关（设计 §1.1.2 / §2.2 降级语义）。
     * 复用既有 governanceStationAdmin 调用范式（同 LinkisConnConf.CONF_GOVERNANCE_ADMIN_PATH 的
     * URL/Token 认证；HTTP 客户端因 pom 不依赖 dss-common-server-webank，按设计 §2.6 依赖方向原则
     * 以 JDK HttpURLConnection 实现，零新增模块间编译依赖）。
     * 降级语义（R1.2）：接口不可达/超时/非200/解析失败/data.aiSwitch 缺失或类型非法 → 一律返回 false，
     * warn 留痕含完整 URL、异常类型、耗时；不抛异常、不缓存、不重试。
     *
     * @param username Token-User 认证头取值（与既有 HttpResourceManageClient 调用范式一致）
     * @return aiSwitch 开关值；任何失败情形返回 false
     */
    private boolean queryAiSwitch(String username) {
        long start = System.currentTimeMillis();
        String url = null;
        try {
            String base = ProxyUserConfiguration.LINKIS_GATEWAY_URL.getValue();
            if (StringUtils.isEmpty(base)) {
                LOGGER.warn("query aiSwitch degraded: empty linkis gateway url(wds.linkis.gateway.url.v1), elapsed {}ms.",
                        System.currentTimeMillis() - start);
                return false;
            }
            url = base + ProxyUserConfiguration.GOVERNANCE_STATION_ADMIN_PATH;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            int timeout = ProxyUserConfiguration.AI_SWITCH_QUERY_TIMEOUT_MS.getValue();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(ProxyUserConfiguration.LINKIS_ADMIN_TOKEN_KEY.getValue(),
                    ProxyUserConfiguration.LINKIS_ADMIN_TOKEN_VALUE.getValue());
            conn.setRequestProperty(ProxyUserConfiguration.LINKIS_ADMIN_TOKEN_USER_KEY.getValue(), username);
            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                LOGGER.warn("query aiSwitch degraded: http status {}, url {}, elapsed {}ms.",
                        statusCode, url, System.currentTimeMillis() - start);
                return false;
            }
            String responseBody;
            try (InputStream in = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                responseBody = reader.lines().collect(Collectors.joining(""));
            }
            AiSwitchResponse response = new Gson().fromJson(responseBody, AiSwitchResponse.class);
            Boolean aiSwitch = (response != null && response.getData() != null) ? response.getData().getAiSwitch() : null;
            if (aiSwitch == null) {
                LOGGER.warn("query aiSwitch degraded: field missing or invalid, url {}, elapsed {}ms.",
                        url, System.currentTimeMillis() - start);
                return false;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("query aiSwitch success: {}, url {}, elapsed {}ms.",
                        aiSwitch, url, System.currentTimeMillis() - start);
            }
            return aiSwitch;
        } catch (Exception exception) {
            LOGGER.warn("query aiSwitch degraded: {}: {}, url {}, elapsed {}ms.",
                    exception.getClass().getSimpleName(), exception.getMessage(), url,
                    System.currentTimeMillis() - start, exception);
            return false;
        }
    }

}
