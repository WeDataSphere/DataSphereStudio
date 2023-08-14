package com.webank.wedatasphere.dss.framework.release.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorReleaseInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
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

}
