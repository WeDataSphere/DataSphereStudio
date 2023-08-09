package com.webank.wedatasphere.dss.common.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.common.server.beans.UserAccessAuditBean;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccessAuditMapper extends BaseMapper<UserAccessAuditBean> {
}
