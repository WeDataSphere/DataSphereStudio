package com.webank.wedatasphere.dss.common.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.webank.wedatasphere.dss.common.server.beans.UserAccessAuditBean;
import com.webank.wedatasphere.dss.common.server.dao.UserAccessAuditMapper;
import com.webank.wedatasphere.dss.common.server.service.UserAccessAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserAccessAuditServiceImpl implements UserAccessAuditService {
    @Autowired
    UserAccessAuditMapper userAccessAuditMapper;
    @Override
    public void increaseLoginCount(String userName) {
        QueryWrapper<UserAccessAuditBean> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",userName);

        UserAccessAuditBean userAccessAudit = userAccessAuditMapper.selectOne(queryWrapper);
        if(userAccessAudit==null){
            userAccessAudit=new UserAccessAuditBean();
            userAccessAudit.setUserName(userName);
            userAccessAudit.setLoginCount(1L);
            userAccessAudit.setFirstLogin(new Date());
            userAccessAudit.setLastLogin(new Date());
            userAccessAuditMapper.insert(userAccessAudit);
        }else{
            long loginCount= userAccessAudit.getLoginCount()+1;
            userAccessAudit.setLoginCount(loginCount);
            userAccessAudit.setLastLogin(new Date());
            userAccessAuditMapper.update(userAccessAudit,queryWrapper);
        }

    }

    @Override
    public Long getLoginCount(String userName) {
        QueryWrapper<UserAccessAuditBean> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",userName);
        UserAccessAuditBean userAccessAudit = userAccessAuditMapper.selectOne(queryWrapper);
        if(userAccessAudit==null){
            return 0L;
        }else {
            return userAccessAudit.getLoginCount();
        }
    }

    @Override
    public Long getAndIncreaseLoginCount(String userName) {
        long loginCount= getLoginCount(userName);
        increaseLoginCount(userName);
        return loginCount;
    }
}
