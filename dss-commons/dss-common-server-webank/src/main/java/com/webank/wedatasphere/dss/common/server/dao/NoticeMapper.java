package com.webank.wedatasphere.dss.common.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页公告mapper
 * Author: xlinliu
 * Date: 2023/3/13
 */
@Mapper
public interface NoticeMapper extends BaseMapper<NoticeContent> {
}
