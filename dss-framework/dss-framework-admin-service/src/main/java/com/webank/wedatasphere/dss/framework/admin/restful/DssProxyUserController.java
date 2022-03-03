package com.webank.wedatasphere.dss.framework.admin.restful;

import com.webank.wedatasphere.dss.framework.admin.common.constant.UserConstants;
import com.webank.wedatasphere.dss.framework.admin.common.domain.PasswordResult;
import com.webank.wedatasphere.dss.framework.admin.common.utils.PasswordUtils;
import com.webank.wedatasphere.dss.framework.admin.common.utils.StringUtils;
import com.webank.wedatasphere.dss.framework.admin.conf.ProjectConf;
import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssAdminUser;
import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.admin.service.DssProxyUserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(path = "/dss/framework/admin/user", produces = {"application/json"})
@RestController
public class DssProxyUserController {

    @Autowired
    DssProxyUserService dssProxyUserService;

    @RequestMapping(path = "proxy/list", method = RequestMethod.GET)
    public Message getProxyUserList(HttpServletRequest request) {
        String username = SecurityFilter.getLoginUsername(request);

        List<DssProxyUser> userList = dssProxyUserService.selectProxyUserList(username);
        List<String> proxyUserNameList=userList.stream().map(dssProxyUser -> dssProxyUser.getProxyUserName()).collect(Collectors.toList());
        return Message.ok().data("proxyUserList", proxyUserNameList);
    }

    @RequestMapping(path = "proxy/add", method = RequestMethod.POST)
    public Message add(@RequestBody DssProxyUser userRep, HttpServletRequest req) {
        try {
            if(StringUtils.isEmpty(userRep.getUserName())){
                return Message.error("User name is empty");
            }else if(StringUtils.isEmpty(userRep.getProxyUserName())){
                return Message.error("Proxy user name is empty");
            }else  if (dssProxyUserService.isExists(userRep.getUserName(),userRep.getProxyUserName())) {
                return Message.error("Failed to add proxy user，'userName：" + userRep.getUserName() + ",proxyName："+userRep.getProxyUserName()+" already exists");
            }
            int rows = dssProxyUserService.insertProxyUser(userRep);
            return Message.ok("Success to add proxy user");
        } catch (Exception exception) {
            return Message.error(exception.getMessage());
        }

    }
}
