package com.webank.wedatasphere.dss.apiservice.core.vo;

import java.util.List;

public class CheckSubmitApiResponse {

    private List<ApiVersionIdVo> submitApiInfos;

    private Integer status;

    private ApiServiceVo apiServiceVo;

    public List<ApiVersionIdVo> getSubmitApiInfos() {
        return submitApiInfos;
    }

    public void setSubmitApiInfos(List<ApiVersionIdVo> submitApiInfos) {
        this.submitApiInfos = submitApiInfos;
    }

    public ApiServiceVo getApiServiceVo() {
        return apiServiceVo;
    }

    public void setApiServiceVo(ApiServiceVo apiServiceVo) {
        this.apiServiceVo = apiServiceVo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
