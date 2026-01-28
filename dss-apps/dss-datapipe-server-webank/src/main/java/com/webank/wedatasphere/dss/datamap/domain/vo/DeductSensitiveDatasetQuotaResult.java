package com.webank.wedatasphere.dss.datamap.domain.vo;

import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2024/10/30
 */
public class DeductSensitiveDatasetQuotaResult {
    private Boolean needDeduct;
    private Boolean isSuccess;
    private Long dataSetSize;
    private Long quota;
    Map<String,Boolean> metadata;

    public Boolean getNeedDeduct() {
        return needDeduct;
    }

    public void setNeedDeduct(Boolean needDeduct) {
        this.needDeduct = needDeduct;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Long getDataSetSize() {
        return dataSetSize;
    }

    public void setDataSetSize(Long dataSetSize) {
        this.dataSetSize = dataSetSize;
    }

    public Long getQuota() {
        return quota;
    }

    public void setQuota(Long quota) {
        this.quota = quota;
    }

    public Map<String, Boolean> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Boolean> metadata) {
        this.metadata = metadata;
    }
}
