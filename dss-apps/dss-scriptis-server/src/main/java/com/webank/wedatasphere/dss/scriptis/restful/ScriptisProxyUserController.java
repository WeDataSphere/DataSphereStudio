package com.webank.wedatasphere.dss.scriptis.restful;

import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.framework.proxy.restful.DssProxyUserController;
import com.webank.wedatasphere.dss.scriptis.dao.ScriptisProxyUserMapper;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.ProxyUserRevokeRequest;
import com.webank.wedatasphere.dss.scriptis.pojo.entity.ScriptisProxyUser;
import com.webank.wedatasphere.dss.scriptis.service.ScriptisProxyUserService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static com.webank.wedatasphere.dss.framework.common.conf.TokenConf.HPMS_USER_TOKEN;

@RequestMapping(path = "/dss/scriptis/proxy", produces = {"application/json"})
@RestController
public class ScriptisProxyUserController extends DssProxyUserController {

    @Autowired
    private ScriptisProxyUserService scriptisProxyUserService;

    @Resource
    private ScriptisProxyUserMapper scriptisProxyUserMapper;

    @RequestMapping(path = "add", method = RequestMethod.POST)
    public Message add(@RequestBody ScriptisProxyUser userRep, HttpServletRequest req) {
        String username = SecurityFilter.getLoginUsername(req);
        LOGGER.info("admin {} try to add proxy user, params:{}.", username, userRep);
        if(!ArrayUtils.contains(DSSCommonConf.SUPER_ADMIN_LIST, username)){
            return Message.error("Only super admin can add proxy users.");
        } else if(StringUtils.isEmpty(userRep.getUserName())){
            return Message.error("userName is null.");
        } else if(StringUtils.isEmpty(userRep.getProxyUserName())){
            return Message.error("proxyUser is null.");
        }
        // 修复(REQ-DSS-1.23.0-FIX-002)：移除 isExists 短路，委托 Service 层 upsert，
        // 续期单（已存在记录）由 Service 层 updateByUser 更新 expire_time，新建单由 insertUser 新增。
        // 旧短路文案保留为注释一个版本，便于调用方协调过渡（见1.23.0/转协查代理用户续期单失效修复_设计 文档 4.4）：
        // return Message.ok("Failed to add proxy user，'userName：" + userRep.getUserName() + ", proxyName："+userRep.getProxyUserName()+" already exists.");
        ScriptisProxyUser existing = scriptisProxyUserMapper.selectProxyUserByUser(
                userRep.getUserName(), userRep.getProxyUserName());
        String originalExpireTime = existing == null ? null : existing.getExpireTime();
        try {
            scriptisProxyUserService.insertProxyUser(userRep);
        } catch (Exception exception) {
            LOGGER.error("Failed to add proxy user.", exception);
            return Message.error(ExceptionUtils.getRootCauseMessage(exception));
        }
        AuditLogUtils.printLog(username, null, null, TargetTypeEnum.WORKSPACE_ROLE, null,
                originalExpireTime == null ? "createProxyUser" : "renewProxyUser",
                originalExpireTime == null ? OperateTypeEnum.CREATE : OperateTypeEnum.UPDATE,
                "userName:" + userRep.getUserName()
                        + ", proxyUserName:" + userRep.getProxyUserName()
                        + ", originalExpireTime:" + originalExpireTime
                        + ", newExpireTime:" + userRep.getExpireTime());
        return Message.ok("Success to add proxy user.");
    }
    @PostMapping("/revokeProxyUser")
    public Message revokeProxyUser(HttpServletRequest httpServletRequest,
                                   @Validated @RequestBody ProxyUserRevokeRequest proxyUserRevokeRequest){
        String userName = proxyUserRevokeRequest.getUserName();
        String[] proxyUserNames = proxyUserRevokeRequest.getProxyUserNames();
        String token = ModuleUserUtils.getToken(httpServletRequest);
        if (StringUtils.isNotBlank(token)) {
            if(!token.equals(HPMS_USER_TOKEN)){
                return Message.error("Token:" + token + " has no permission to revoke proxyUser.");
            }
        }else {
            return Message.error("User:" + userName + " has no permission to revoke proxyUser.");
        }
        scriptisProxyUserService.revokeProxyUser(userName,proxyUserNames);
        AuditLogUtils.printLog(userName,null, null, TargetTypeEnum.WORKSPACE_ROLE,null,
                "deleteProxyUser", OperateTypeEnum.DELETE,"userName:" + userName + " ,proxyUserNames:" + Arrays.toString(proxyUserNames));
        return Message.ok();
    }

}
