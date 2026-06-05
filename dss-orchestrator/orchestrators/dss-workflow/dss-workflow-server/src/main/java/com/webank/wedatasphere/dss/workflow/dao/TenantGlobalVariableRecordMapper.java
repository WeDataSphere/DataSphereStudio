package com.webank.wedatasphere.dss.workflow.dao;

import com.webank.wedatasphere.dss.workflow.entity.TenantGlobalVariableRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TenantGlobalVariableRecordMapper {

    void batchInsert(List<TenantGlobalVariableRecord> records);
}
