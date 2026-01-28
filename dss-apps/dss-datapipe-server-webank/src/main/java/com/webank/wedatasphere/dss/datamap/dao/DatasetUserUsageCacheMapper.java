package com.webank.wedatasphere.dss.datamap.dao;

import org.apache.ibatis.annotations.*;

/**
 * Author: xlinliu
 * Date: 2024/11/4
 */
@Mapper
public interface DatasetUserUsageCacheMapper {

    @Insert("INSERT INTO dss_dataset_user_usage_cache (username, usage_quota, create_time, update_time) " +
            "VALUES (#{username}, #{usageQuota}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE usage_quota = VALUES(usage_quota), update_time = VALUES(update_time)")
    int insertOrUpdate(@Param("username") String username,
                       @Param("usageQuota") Long usageQuota);

    @Select("SELECT usage_quota FROM dss_dataset_user_usage_cache WHERE username = #{username}")
    Long findUsageByUsername(@Param("username") String username);
}