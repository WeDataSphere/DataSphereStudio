package com.webank.wedatasphere.dss.framework.appconn.service.impl;

import com.webank.wedatasphere.dss.framework.appconn.dao.AppInstanceMapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppInstanceBean;
import com.webank.wedatasphere.dss.framework.appconn.service.AppInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AppInstanceServiceImpl implements AppInstanceService {

    @Autowired
    AppInstanceMapper appInstanceMapper;
    @Override
    public List<AppInstanceBean> getAppInstancesByAppConnId(Long appConnId) {
        return appInstanceMapper.getAppInstancesByAppConnId(appConnId);
    }

    @Override
    public AppInstanceBean addAppInstance(AppInstanceBean appInstanceBean) {
        appInstanceMapper.addAppInstance(appInstanceBean);
        return appInstanceMapper.getAppInstanceById(appInstanceBean.getId());
    }

    @Override
    public void deleteAppInstance(Long appInstanceId) {
        appInstanceMapper.deleteAppInstance(appInstanceId);
    }

    @Override
    public AppInstanceBean updateAppInstance(AppInstanceBean appInstanceBean) {
        appInstanceMapper.updateAppInstance(appInstanceBean);
        return appInstanceBean;
    }
}
