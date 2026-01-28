package com.webank.wedatasphere.dss.workflow.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.CompareWorkflowAndSubFlowResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppAndSubFlowCompare;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAppWithSubFlowCompare;

import java.util.List;

public interface WebankCompareWorkflowService {

    /**
     * compare two workflow
     * @param requestAppCompare
     * @return
     * @throws DSSErrorException
     */
    ResponseAppCompare compareWorkflow(RequestAppCompare requestAppCompare) ;
    ResponseAppAndSubFlowCompare compareWorkflowAndSubFlow(RequestAppWithSubFlowCompare requestAppWithSubFlowCompare);
}
