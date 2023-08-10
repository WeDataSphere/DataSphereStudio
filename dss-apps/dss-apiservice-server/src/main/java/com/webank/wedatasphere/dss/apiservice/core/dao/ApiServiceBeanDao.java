package com.webank.wedatasphere.dss.apiservice.core.dao;

import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceBean;
import org.apache.ibatis.annotations.Param;

public interface ApiServiceBeanDao {

    void insert(ApiServiceBean apiServiceBean);

    ApiServiceBean selectByTaskId(@Param("taskId") String taskId);
}
