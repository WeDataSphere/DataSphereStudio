package com.webank.wedatasphere.dss.common.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.common.server.beans.UserAccessAuditBean;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为操作mapper
 * Author: xlinliu
 * Date: 2022/12/6
 */
@Mapper
public interface UserAccessAuditMapper extends BaseMapper<UserAccessAuditBean> {
}
