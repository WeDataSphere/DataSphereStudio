package com.webank.wedatasphere.dss.framework.release.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.framework.release.dao.entity.StreamisProxyUserDo;
import org.apache.ibatis.annotations.Mapper;


/**
 * dss_streamis_proxy_user 配置表，用户streamis代理用户选择
 * Author: xlinliu
 * Date: 2022/11/9
 */
@Mapper
public interface DssProxyUserForStreamisMapper extends BaseMapper<StreamisProxyUserDo> {
}
