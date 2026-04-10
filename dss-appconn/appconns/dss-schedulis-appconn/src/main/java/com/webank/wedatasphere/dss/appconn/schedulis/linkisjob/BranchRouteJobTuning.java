package com.webank.wedatasphere.dss.appconn.schedulis.linkisjob;

import com.webank.wedatasphere.dss.appconn.schedulis.constant.BranchSchedulisConstant;

public class BranchRouteJobTuning implements LinkisJobTuning {

    @Override
    public LinkisJob tuningJob(LinkisJob job) {
        job.setType(BranchSchedulisConstant.DECISION_JOB_TYPE);
        job.setLinkistype(BranchSchedulisConstant.BRANCH_ROUTE_LINKIS_TYPE);
        return job;
    }

    @Override
    public boolean ifJobCantuning(String nodeType) {
        return BranchSchedulisConstant.BRANCH_NODE_TYPE.equalsIgnoreCase(nodeType);
    }
}
