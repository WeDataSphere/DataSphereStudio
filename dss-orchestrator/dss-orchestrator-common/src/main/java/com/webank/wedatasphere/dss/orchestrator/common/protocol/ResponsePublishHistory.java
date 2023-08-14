package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;

import java.io.Serializable;
import java.util.List;

public class ResponsePublishHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer totalPage;
    private List<ReleaseHistoryDetail> releaseHistorys;

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<ReleaseHistoryDetail> getReleaseHistorys() {
        return releaseHistorys;
    }

    public void setReleaseHistorys(List<ReleaseHistoryDetail> releaseHistorys) {
        this.releaseHistorys = releaseHistorys;
    }
}
