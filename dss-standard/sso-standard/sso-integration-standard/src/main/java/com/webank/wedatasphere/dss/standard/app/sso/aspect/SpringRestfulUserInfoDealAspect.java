package com.webank.wedatasphere.dss.standard.app.sso.aspect;

import com.webank.wedatasphere.dss.common.utils.UserUtils;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
//@Order(2)
public class SpringRestfulUserInfoDealAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringRestfulUserInfoDealAspect.class);
    //定义切入点
    @Pointcut(
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)"
                    +"|| @annotation(org.springframework.web.bind.annotation.GetMapping)"
                    +"|| @annotation(org.springframework.web.bind.annotation.PostMapping)"
                    +"|| @annotation(org.springframework.web.bind.annotation.PutMapping)"
                    +"|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public void userInfoGet() {}
    @Around("userInfoGet()")
    public Object userInfoHold(ProceedingJoinPoint joinPoint) throws Throwable {
        try{
            //获取request对象
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            //获取用户名
            String userName = UserUtils.getLoginUserName(request);
            Workspace workspace = null;
            try {
                workspace = SSOHelper.getWorkspace(request);
            }catch (Throwable e){
                LOGGER.info("UserInfoHolder 未取到workspace");
                workspace = null;
            }
            //设置threadlocal
            UserInfoHolder.setUserName(userName);
            UserInfoHolder.setWorkspace(workspace);
            Object ret = joinPoint.proceed();
            return ret;
        }catch (Exception e){
            LOGGER.error("UserInfoHolder获取异常",e);
            throw e;
        }finally{
            UserInfoHolder.removeUserName();
            UserInfoHolder.removeWorkspace();
        }
    }
}
