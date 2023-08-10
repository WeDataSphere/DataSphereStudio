package com.webank.wedatasphere.dss.apiservice.core.service;

import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;

public interface ApprovalStatusListener {
    void afterApprovalSuccess(ApprovalVo approvalVo);

    void afterApprovalFailed(ApprovalVo approvalVo);
}
