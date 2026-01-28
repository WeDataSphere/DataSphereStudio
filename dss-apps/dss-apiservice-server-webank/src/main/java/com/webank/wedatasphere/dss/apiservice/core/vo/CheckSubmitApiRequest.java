package com.webank.wedatasphere.dss.apiservice.core.vo;

import java.util.List;

public class CheckSubmitApiRequest {

    private List<ApiVersionIdVo> submitApiInfos;


    public List<ApiVersionIdVo> getSubmitApiInfos() {
        return submitApiInfos;
    }

    public void setSubmitApiInfos(List<ApiVersionIdVo> submitApiInfos) {
        this.submitApiInfos = submitApiInfos;
    }
}
