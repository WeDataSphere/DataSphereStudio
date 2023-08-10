package com.webank.wedatasphere.dss.apiservice.core.service.impl;

import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceApprovalDao;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceVersionDao;
import com.webank.wedatasphere.dss.apiservice.core.datamap.DataMapStatus;
import com.webank.wedatasphere.dss.apiservice.core.datamap.DataMapUtils;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceQueryException;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalService;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalStatusListener;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final Logger LOG = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    @Autowired
    private ApiServiceApprovalDao apiServiceApprovalDao;

    @Autowired
    private ApiServiceVersionDao apiServiceVersionDao;

    private final List<ApprovalStatusListener> approvalStatusListeners = new ArrayList<>(1);

    @Override
    public List<ApprovalVo> refreshStatus(String approvalNo) throws Exception {
        List<ApprovalVo> resList = new ArrayList<>();
        DataMapStatus dataMapStatus;
        try {
            //请求审批单状态
            dataMapStatus = DataMapUtils.requestDataMapStatus(approvalNo);
        } catch (Exception e) {
//            LOG.info("请求DataMap获取审批单状态异常", e);
            throw new ApiServiceQueryException(80055, "请求DataMap获取审批单状态异常");
        }
        if (dataMapStatus.getIndex() == DataMapStatus.EMPTY.getIndex()) {
            throw new ApiServiceQueryException(80055, "审批单号不存在，请检查单号正确性");
        }
        List<ApprovalVo> approvalVoList = apiServiceApprovalDao.queryByApprovalNo(approvalNo);
        for (ApprovalVo approvalVo : approvalVoList) {
            if (DataMapStatus.APPROVING.getIndex() == approvalVo.getStatus() ||
                    DataMapStatus.INITED.getIndex() == approvalVo.getStatus() ||
                    DataMapStatus.REJECT.getIndex() == approvalVo.getStatus()) {
                // 更新审批单的状态，驳回还会有后续状态，所以当为驳回状态时，还需要查询datamap
                approvalVo.setStatus(dataMapStatus.getIndex());
                apiServiceApprovalDao.updateApprovalStatus(approvalNo, dataMapStatus.getIndex());
                //审批状态通过
                if (dataMapStatus.getIndex() == DataMapStatus.SUCCESS.getIndex()) {
                    approvalStatusListeners.forEach(l -> {
                        l.afterApprovalSuccess(approvalVo);
                    });
                }//审批不通过、作废
                else if (dataMapStatus.getIndex() == DataMapStatus.FAILED.getIndex()) {
                    approvalStatusListeners.forEach(l -> {
                        l.afterApprovalFailed(approvalVo);
                    });
                }
                LOG.info("更新审批单：{}，状态： {}", approvalNo, dataMapStatus.getValue());
            }
            resList.add(approvalVo);
        }
        return resList;
    }

    public void registerApprovalStatusListener(ApprovalStatusListener approvalStatusListener) {
        approvalStatusListeners.add(approvalStatusListener);
    }

    @Override
    public List<ApprovalVo> query(String approvalNo) {
        List<ApprovalVo> approvalVoList = apiServiceApprovalDao.queryByApprovalNo(approvalNo);

        if (null != approvalVoList) {
            return approvalVoList;
        } else {
            LOG.warn("无此审批单号！" + approvalNo);
            return Collections.emptyList();
        }
    }
}