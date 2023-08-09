package com.webank.wedatasphere.dss.common.server.service;

import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import com.webank.wedatasphere.dss.common.server.beans.NoticeVo;

import java.util.List;

public interface NoticeService {
    List<NoticeContent> getNoticeContent();

    List<NoticeVo> getAllNotice(String sortBy,String orderBy);

    void createNotice(NoticeVo notice);

    void deleteNotice(Integer noticeId);

    //boolean checkInWhiteList(String userName);
}
