package org.apache.dolphinscheduler.api.interceptor;

import com.webank.wedatasphere.dss.standard.app.sso.plugin.filter.HttpRequestUserInterceptor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * @author enjoyyin
 * @date 2022-11-16
 * @since 1.1.1
 */
@Component
public class DolphinSchedulerHttpRequestUserInterceptor implements HttpRequestUserInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Authenticator authenticator;
    @Autowired
    private SessionService sessionService;

    @Override
    public HttpServletRequest addUserToRequest(String user, HttpServletRequest req) {
        logger.info("try to add a new session.id for DSS user {}.", user);
        User userObj = userMapper.queryByUserNameAccurately(user);
        if(userObj == null) {
            logger.error("DSS user {} is not exists in DolphinScheduler db.", user);
            throw new ServiceException(Status.USER_NOT_EXIST);
        }
        Session session = sessionService.createSessionIfAbsent(userObj);
        String sessionId = session.getId();
        Cookie cookie = new Cookie(Constants.SESSION_ID, sessionId);
        cookie.setHttpOnly(true);
        logger.info("added a new session.id {} for DSS user {}.", sessionId, user);
        return new HttpServletRequestWrapper(req) {
            @Override
            public Cookie[] getCookies() {
                return (Cookie[]) ArrayUtils.add(super.getCookies(), cookie);
            }
        };
    }

    @Override
    public boolean isUserExistInSession(HttpServletRequest req) {
        return getUser(req) != null;
    }

    @Override
    public String getUser(HttpServletRequest req) {
        String sessionId = getSessionId(req);
        if(sessionId == null) {
            return null;
        }
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            return null;
        }
        User user = userMapper.selectById(session.getUserId());
        if(user != null) {
            return user.getUserName();
        }
        return null;
    }

    private String getSessionId(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if(cookies == null) {
            return null;
        }
        for(Cookie cookie : cookies) {
            if(Constants.SESSION_ID.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
