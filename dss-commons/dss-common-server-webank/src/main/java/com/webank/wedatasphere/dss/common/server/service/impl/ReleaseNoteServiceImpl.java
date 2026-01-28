package com.webank.wedatasphere.dss.common.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.webank.wedatasphere.dss.common.server.beans.ReleaseNoteContent;
import com.webank.wedatasphere.dss.common.server.dao.ReleaseNoteContentMapper;
import com.webank.wedatasphere.dss.common.server.enums.ReleaseTypeEnum;
import com.webank.wedatasphere.dss.common.server.service.ReleaseNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实现类
 * Author: xlinliu
 * Date: 2022/12/6
 */
@Service
public class ReleaseNoteServiceImpl implements ReleaseNoteService {
    @Autowired
    ReleaseNoteContentMapper releaseNoteContentMapper;
    @Override
    public List<ReleaseNoteContent> getReleaseNoteContent(ReleaseTypeEnum releaseType) {
        int releaseTypeCode= releaseType.getCode();
        QueryWrapper<ReleaseNoteContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("release_type",releaseTypeCode);
        queryWrapper.orderByAsc("id");
        return releaseNoteContentMapper.selectList(queryWrapper);
    }
}
