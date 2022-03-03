package com.webank.wedatasphere.dss.framework.admin.restful;

import com.webank.wedatasphere.dss.framework.admin.common.utils.StringUtils;
import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.admin.service.DssProxyUserService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.security.ProxyUserSSOUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import scala.Tuple2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.framework.admin.conf.ProjectConf.DS_TRUST_TOKEN;
import static com.webank.wedatasphere.dss.framework.admin.conf.ProjectConf.LDAP_ADMIN_NAME;

@RequestMapping(path = "/dss/framework/admin/user", produces = {"application/json"})
@RestController
public class DssProxyUserController {
    private Boolean sslEnable = (Boolean) ServerConfiguration.BDP_SERVER_SECURITY_SSL().getValue();
    private String  PROXY_USER_TICKET_ID_STRING = ServerConfiguration.LINKIS_SERVER_SESSION_PROXY_TICKETID_KEY().getValue();
    @Autowired
    DssProxyUserService dssProxyUserService;

    @RequestMapping(path = "proxy/list", method = RequestMethod.GET)
    public Message getProxyUserList(HttpServletRequest request) {
        String username = SecurityFilter.getLoginUsername(request);

        List<DssProxyUser> userList = dssProxyUserService.selectProxyUserList(username);
        List<String> proxyUserNameList=userList.stream().map(dssProxyUser -> dssProxyUser.getProxyUserName()).collect(Collectors.toList());
        return Message.ok().data("proxyUserList", proxyUserNameList);
    }

    @RequestMapping(path = "proxy/addUserCookie", method = RequestMethod.POST)
    public Message setProxyUserCookie(@RequestBody DssProxyUser userRep, HttpServletRequest req, HttpServletResponse resp) {

        String username = SecurityFilter.getLoginUsername(req);
        String trustCode = DS_TRUST_TOKEN.getValue();
        try {
            if(StringUtils.isEmpty(userRep.getUserName())){
                return Message.error("User name is empty");
            }else if(StringUtils.isEmpty(userRep.getProxyUserName())){
                return Message.error("Proxy user name is empty");
            }else  if (dssProxyUserService.isExists(userRep.getUserName(),userRep.getProxyUserName())) {
                if (userRep.getUserName().equals(username)) {
                    for(Cookie cookie: req.getCookies()){
                        if(null!=cookie){
                            if(cookie.getName() == PROXY_USER_TICKET_ID_STRING)
                                cookie.setValue(null);
                                cookie.setMaxAge(0);
                                resp.addCookie(cookie);
                            }
                        }
                    }
                    Tuple2<String, String> userTicketIdKv = ProxyUserSSOUtils.getProxyUserTicketKV(username, trustCode);
                    Cookie cookie = new Cookie(userTicketIdKv._1, userTicketIdKv._2);
                    cookie.setMaxAge(-1);
                    if(sslEnable) cookie.setSecure(true);
                    cookie.setPath("/");
                    resp.addCookie(cookie);

                }else {
                    return Message.error("The requested user name is not a login user");
                }
                return Message.ok("Success to add proxy user into cookie");
        } catch (Exception exception) {
            return Message.error(exception.getMessage());
        }

    }

    @RequestMapping(path = "proxy/add", method = RequestMethod.POST)
    public Message add(@RequestBody DssProxyUser userRep, HttpServletRequest req) {
        String username = SecurityFilter.getLoginUsername(req);
        if(!username.equals(LDAP_ADMIN_NAME.getValue())){
            return Message.error("Only administrators can add proxy users");
        }
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
