package com.webank.wedatasphere.dss.common.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.common.server.beans.NoticeContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<NoticeContent> {
}
