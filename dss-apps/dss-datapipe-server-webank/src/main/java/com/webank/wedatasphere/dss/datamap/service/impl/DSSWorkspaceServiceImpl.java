package com.webank.wedatasphere.dss.datamap.service.impl;

import com.webank.wedatasphere.dss.datamap.dao.DSSWorkspaceUserMapper;
import com.webank.wedatasphere.dss.datamap.service.DSSWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DSSWorkspaceServiceImpl implements DSSWorkspaceService {

    @Autowired
    DSSWorkspaceUserMapper dssWorkspaceUserMapper;

    /**
     * 检测用户是否工作空间管理员
     * **/
    @Override
    public boolean isAdminUser(Long workspaceId, String username) {

        List<Integer> roles = dssWorkspaceUserMapper.getRoleInWorkspace(workspaceId.intValue(), username);
        if (roles != null && roles.size() > 0) {
            for (Integer role : roles) {
                if (role == 1) {
                    return true;
                }
            }
        }

        return false;
    }
}
