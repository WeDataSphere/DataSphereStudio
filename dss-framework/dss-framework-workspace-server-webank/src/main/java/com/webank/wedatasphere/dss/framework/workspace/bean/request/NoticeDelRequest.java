package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import javax.validation.constraints.NotNull;

public class NoticeDelRequest {
    @NotNull
    private Integer noticeId;

    public Integer getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
    }
}
