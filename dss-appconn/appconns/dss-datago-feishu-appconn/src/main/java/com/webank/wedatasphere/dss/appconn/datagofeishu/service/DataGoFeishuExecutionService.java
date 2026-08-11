/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.service;

import com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuRefExecutionOperation;
import com.webank.wedatasphere.dss.standard.app.development.service.AbstractRefExecutionService;

/**
 * DataGo飞书外发节点执行服务，构造 {@code DataGoFeishuRefExecutionOperation}
 * 负责节点 submit/state/result/kill/progress/log 全生命周期编排。
 */
public class DataGoFeishuExecutionService extends AbstractRefExecutionService {
    @Override
    protected DataGoFeishuRefExecutionOperation createRefExecutionOperation() {
        return new DataGoFeishuRefExecutionOperation();
    }
}