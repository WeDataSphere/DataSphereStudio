package com.webank.wedatasphere.dss.workflow.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAppCompare;

public interface CompareWorkflowService {

    /**
     * compare two workflow
     * @param requestAppCompare
     * @return
     * @throws DSSErrorException
     */
    ResponseAppCompare compareWorkflow(RequestAppCompare requestAppCompare) ;
}
