package com.webank.wedatasphere.dss.flow.execution.entrance.dao;

import com.webank.wedatasphere.dss.flow.execution.entrance.entity.SendImsTaskInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SendImsTaskMapper {

    List<SendImsTaskInfo> selectByJobId(@Param("jobId") String jobId);

    void insert(SendImsTaskInfo sendImsTaskInfo);

}
