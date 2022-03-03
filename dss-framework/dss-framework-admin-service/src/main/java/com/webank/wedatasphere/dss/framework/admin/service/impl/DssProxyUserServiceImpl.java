package com.webank.wedatasphere.dss.framework.admin.service.impl;

import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.admin.service.DssProxyUserService;
import com.webank.wedatasphere.dss.framework.admin.xml.DSSProxyUserMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class DssProxyUserServiceImpl implements DssProxyUserService {
    @Resource
    DSSProxyUserMapper dssProxyUserMapper;

    @Override
    public List<DssProxyUser> selectProxyUserList(String userName) {
        return dssProxyUserMapper.selectProxyUserList(userName);
    }

    @Override
    public int insertProxyUser(DssProxyUser dssProxyUser) {
        int rows = dssProxyUserMapper.insertUser(dssProxyUser);
        return rows;
    }

    @Override
    public boolean isExists(String userName, String proxyUserName) {
        List<DssProxyUser> res= dssProxyUserMapper.getProxyUserList(userName,proxyUserName);
        if(res.size()==0){
            return false;
        }else {
            return true;
        }
    }
}
