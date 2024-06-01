package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnBean;

import java.util.List;

public interface AppConnService {

    AppConnBean addAppConn(AppConnBean appConnBean);

    AppConnBean updateAppConn(AppConnBean appConnBean);

    void deleteAppConn(Long appConnId);
    List<AppConnBean> getAppConns(String appConnName, String className, int page, int size);

    List<String> getAppConnsName();

}
