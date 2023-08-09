package com.webank.wedatasphere.dss.common.server.service;

import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteContent;
import com.webank.wedatasphere.dss.common.server.enums.ReleaseTypeEnum;

import java.util.List;

public interface ReleaseNoteService {
    List<ReleaseNoteContent> getReleaseNoteContent(ReleaseTypeEnum releaseType);
}
