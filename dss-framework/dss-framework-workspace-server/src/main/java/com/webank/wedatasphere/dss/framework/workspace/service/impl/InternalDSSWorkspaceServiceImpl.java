package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.framework.workspace.bean.dto.response.WorkspaceFavoriteVo;
import com.webank.wedatasphere.dss.framework.workspace.dao.InternalWorkspaceMapper;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalDSSWorkspaceServiceImpl extends DSSWorkspaceServiceImpl {
    @Autowired
    InternalWorkspaceMapper internalWorkspaceMapper;

    @Override
    public String getAppInstanceTitle(AppConn appConn, AppInstance appInstance, boolean isChinese) {
        String envStr = ((EnvDSSLabel) appInstance.getLabels().get(0)).getEnv();
        if (isChinese) {
            return "进入 " + (envStr.equalsIgnoreCase("dev") ? "开发中心" : "生产中心");
        } else {
            return "Enter " + (envStr.equalsIgnoreCase("dev") ? "Development Center" : "Production Center");
        }
    }

    @Override
    public List<WorkspaceFavoriteVo> getWorkspaceFavorites(Long workspaceId, String username, boolean isChinese, String type) {
        checkScriptis(workspaceId, username, "dingyiding");
        return super.getWorkspaceFavorites(workspaceId, username, isChinese, type);
    }


    public void checkScriptis(Long workspaceId, String username, String type) {
        int exists = internalWorkspaceMapper.getByMenuAppIdAndUser(4L, workspaceId, username, type);
        if (exists < 1) {
            super.addFavorite(username, workspaceId, 4L, type);
        }
    }
}
