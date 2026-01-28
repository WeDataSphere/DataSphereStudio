package com.webank.wedatasphere.dss.common.server.service;

import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteContent;
import com.webank.wedatasphere.dss.common.server.enums.ReleaseTypeEnum;

import java.util.List;

/**
 * 获取releaseNote 内容
 * Author: xlinliu
 * Date: 2022/12/6
 */
public interface ReleaseNoteService {
    List<ReleaseNoteContent> getReleaseNoteContent(ReleaseTypeEnum releaseType);
}
