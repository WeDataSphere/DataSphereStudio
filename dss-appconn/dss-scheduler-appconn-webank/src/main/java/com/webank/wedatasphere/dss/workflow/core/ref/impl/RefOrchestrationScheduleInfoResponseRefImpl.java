package com.webank.wedatasphere.dss.workflow.core.ref.impl;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationScheduleInfoResponseRef;
import java.util.Map;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public class RefOrchestrationScheduleInfoResponseRefImpl extends ResponseRefImpl implements RefOrchestrationScheduleInfoResponseRef {

    private String scheduleId;

    public RefOrchestrationScheduleInfoResponseRefImpl(String responseBody, int status,
        String errorMsg, Map<String, Object> responseMap, String scheduleId) {
        super(responseBody, status, errorMsg, responseMap);
        this.scheduleId = scheduleId;
    }


    @Override
    public String getScheduleId() {
        return scheduleId;
    }

}
