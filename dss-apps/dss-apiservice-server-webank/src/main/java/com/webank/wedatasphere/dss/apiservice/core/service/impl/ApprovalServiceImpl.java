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
import java.util.Comparator;
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

        List<ApprovalVo> approvalVoList = apiServiceApprovalDao.queryByApprovalNo(approvalNo);
        for (ApprovalVo approvalVo : approvalVoList) {
            if (dataMapStatus.getIndex() == DataMapStatus.EMPTY.getIndex()) {
                approvalVo.setStatus(DataMapStatus.EMPTY.getIndex());
                resList.add(approvalVo);
                return resList;
            }

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


    @Override
    public List<ApprovalVo> queryByApiIdAndStatus(long apiId,int status) {
        return  apiServiceApprovalDao.queryByApiIdAndStatus(apiId,status);
    }


    /**
     * 获取上一个通过的审批单（用于增量授权）
     * 
     * 业务场景：
     * 当 API V2 提交审批时，如果需要保留 V1 的授权用户，就需要获取 V1 的审批单信息。
     * 通过查询所有审批通过的记录并排序，返回倒数第二个审批单（最新的前一个）。
     * 
     * 使用示例：
     * API V1 审批通过（2026-01-01），授权用户：user1, user2
     * API V2 提交审批（2026-02-01），申请用户：user1, user3
     * 调用此方法获取 V1 的审批单，查询 user1, user2 的 Token 配置
     * 将 user2 的配置复制到 V2，实现增量授权（user1, user2, user3 都有权限）
     * 
     * @param apiId API ID
     * @return 上一个通过的审批单，如果不存在返回 null
     */
    @Override
    public ApprovalVo getSecondApproval(long apiId) {
        // 查询该 API 所有审批通过的审批单（status=3）
        List<ApprovalVo> approvalVoList = queryByApiIdAndStatus(apiId,DataMapStatus.SUCCESS.getIndex());

        // 如果审批通过的记录少于等于 1 条，说明没有上一个审批单（首次提单或只有一次审批）
        if(approvalVoList.size() <= 1){
            return  null;
        }

        // 按创建时间升序排序，最新的审批单在列表最后
        approvalVoList.sort(Comparator.comparing(ApprovalVo::getCreateTime));
        
        // 获取倒数第二个审批单（最新的前一个），即上一次审批通过的记录
        ApprovalVo approvalVo = approvalVoList.get(approvalVoList.size() - 2);
        LOG.info("获取上一个通过的审批单 - API ID: {}, 审批单ID: {}, 审批单号: {}", 
                 apiId, approvalVo.getId(), approvalVo.getApprovalNo());
        return approvalVoList.get(approvalVoList.size() - 2);
    }

}