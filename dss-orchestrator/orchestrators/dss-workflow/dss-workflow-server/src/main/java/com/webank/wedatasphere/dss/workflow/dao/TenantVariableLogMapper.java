package com.webank.wedatasphere.dss.workflow.dao;

import com.webank.wedatasphere.dss.workflow.entity.TenantVariableLogEntry;

public interface TenantVariableLogMapper {

    void insert(TenantVariableLogEntry logEntry);
}
