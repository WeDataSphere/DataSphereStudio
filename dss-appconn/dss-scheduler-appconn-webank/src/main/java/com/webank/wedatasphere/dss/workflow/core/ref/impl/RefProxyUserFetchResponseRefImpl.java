package com.webank.wedatasphere.dss.workflow.core.ref.impl;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;
import com.webank.wedatasphere.dss.workflow.core.ref.RefProxyUserFetchResponseRef;
import java.util.List;
import java.util.Map;

/**
 * Created by enjoyyin on 2022/9/8.
 */
public class RefProxyUserFetchResponseRefImpl extends ResponseRefImpl implements RefProxyUserFetchResponseRef {

    /**
     * wtss 下划线开头的运维账号
     */
    private List<String> maintenanceUserList;
    /**
     * hduser开头的代理用户账号
     */
    private List<String> proxyExecuteUserList;

    public RefProxyUserFetchResponseRefImpl(String responseBody, int status,
        String errorMsg, Map<String, Object> responseMap, List<String> maintenanceUserList,List<String> proxyExecuteUserList) {
        super(responseBody, status, errorMsg, responseMap);
        this.maintenanceUserList = maintenanceUserList;
        this.proxyExecuteUserList=proxyExecuteUserList;
    }

    @Override
    public List<String> getMaintenanceUserList() {
        return maintenanceUserList;
    }

    @Override
    public List<String> getProxyExecuteUserList() {
        return proxyExecuteUserList;
    }
}
