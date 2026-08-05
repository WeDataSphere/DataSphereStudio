/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu.standard;

import com.webank.wedatasphere.dss.appconn.datagofeishu.service.DataGoFeishuExecutionService;
import com.webank.wedatasphere.dss.standard.app.development.standard.OnlyExecutionDevelopmentStandard;

/**
 * DataGo飞书外发开发流程规范，仅提供执行（RefExecution）能力。
 * <p>
 * 创建 {@link DataGoFeishuExecutionService}，由其构造节点执行编排器
 * {@code DataGoFeishuRefExecutionOperation}。
 */
public class DataGoFeishuDevelopmentStandard extends OnlyExecutionDevelopmentStandard {
    @Override
    protected DataGoFeishuExecutionService createRefExecutionService() {
        return new DataGoFeishuExecutionService();
    }

    @Override
    public void init() {
    }
}