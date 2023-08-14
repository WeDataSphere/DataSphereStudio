package com.webank.wedatasphere.dss.framework.release.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OptionalAppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.common.entity.DSSWorkspace;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUser;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUserImpl;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.framework.release.dao.DssProxyUserForStreamisMapper;
import com.webank.wedatasphere.dss.framework.release.dao.entity.StreamisProxyUserDo;
import com.webank.wedatasphere.dss.framework.release.utils.ReleaseConf;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import com.webank.wedatasphere.dss.workflow.core.operation.RefProxyUserFetchOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefProxyUserFetchOperation.RefProxyUserFetchRequestRefImpl;
import com.webank.wedatasphere.dss.workflow.core.ref.RefProxyUserFetchResponseRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author enjoyyin
 * @date 2022-09-06
 * @since 0.5.0
 */
@Service
public class DssProxyUserServiceImpl implements DssProxyUserService {
    @Autowired
    DssProxyUserForStreamisMapper dssProxyUserForStreamisMapper;
    private static final boolean IS_STREAMIS_PROXY= ReleaseConf.IS_STREAMIS_PROXY.getValue();

    @Override
    public List<DssProxyUser> selectProxyUserList(String userName, DSSWorkspace workspace) {
        return IS_STREAMIS_PROXY ? proxyUserListForStreamis(userName) : proxyUserListFromSchedulis(userName, workspace);
    }
    private List<DssProxyUser> proxyUserListFromSchedulis(String userName, DSSWorkspace workspace){
        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue());
        if(appConn == null) {
            throw new DSSRuntimeException("Not exists " + ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue() + " AppConn.");
        }
        if(!(appConn instanceof OptionalAppConn)) {
            throw new DSSRuntimeException("AppConn " + ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue() + " doesn't support to get proxy user list.");
        }
        AppInstance appInstance = appConn.getAppDesc().getAppInstances().get(0);
        RefProxyUserFetchOperation operation = (RefProxyUserFetchOperation) ((OptionalAppConn) appConn).getOrCreateOptionalStandard().getOptionalService(appInstance)
                .getOptionalOperation(RefProxyUserFetchOperation.OPERATION_NAME);
        RefProxyUserFetchRequestRefImpl requestRef = new RefProxyUserFetchRequestRefImpl();
        requestRef.setUserName(userName);
        requestRef.setWorkspace((Workspace) workspace);
        RefProxyUserFetchResponseRef responseRef = operation.apply(requestRef);
        return responseRef.getProxyUserList().stream()
                //只要WTSS下划线开头的，确保为运维用户
                .filter(proxyUser-> StringUtils.startsWithIgnoreCase(proxyUser,"WTSS_"))
                .map(proxyUser -> new DssProxyUserImpl(userName, proxyUser)).collect(Collectors.toList());
    }

    /**
     * 查询streamis的代理用户列表
     * @param userName 实名用户
     * @return 代理用户列表
     */
    private List<DssProxyUser> proxyUserListForStreamis(String userName){
        QueryWrapper<StreamisProxyUserDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",userName);
        return new ArrayList<>(dssProxyUserForStreamisMapper.selectList(queryWrapper));
    }

}
