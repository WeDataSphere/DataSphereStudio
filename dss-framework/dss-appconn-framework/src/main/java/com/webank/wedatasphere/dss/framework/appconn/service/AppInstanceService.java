package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.entity.AppInstanceBean;

import java.util.List;

public interface AppInstanceService {

    List<AppInstanceBean> getAppInstancesByAppConnId(Long appConnId);

    AppInstanceBean addAppInstance(AppInstanceBean appInstanceBean);

    void deleteAppInstance(Long appInstanceId);

    AppInstanceBean updateAppInstance(AppInstanceBean appInstanceBean);

}
