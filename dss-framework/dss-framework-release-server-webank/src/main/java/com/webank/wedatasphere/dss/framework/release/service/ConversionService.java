package com.webank.wedatasphere.dss.framework.release.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.util.List;

/**
 * Created by enjoyyin on 2021/7/4.
 */
public interface ConversionService {
    /**
     * 讲编排转为schedulis的工作流调度。
     * @param request 转化请求
     * @param dssLabels 标签
     */
    void convert(RequestFrameworkConvertOrchestration request,  List<DSSLabel> dssLabels) throws DSSErrorException;

    /**
     * 向调度系统上报工作流的版本对比信息

     */
    void uploadFlowVersionCompareInfo(String userName, Long projectId, Long orcId , Workspace workspace);

}
