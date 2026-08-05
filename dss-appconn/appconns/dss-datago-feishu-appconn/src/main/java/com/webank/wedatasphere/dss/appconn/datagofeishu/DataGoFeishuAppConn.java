/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.webank.wedatasphere.dss.appconn.datagofeishu;

import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.appconn.core.impl.AbstractAppConn;
import com.webank.wedatasphere.dss.appconn.datagofeishu.standard.DataGoFeishuDevelopmentStandard;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;

/**
 * DataGo飞书多维表格外发 AppConn 入口类。
 * <p>
 * 注册为 OnlyDevelopmentAppConn（仅开发流程规范，无 SSO/组织结构规范），
 * 通过 {@link DataGoFeishuDevelopmentStandard} 提供节点执行能力。
 * <p>
 * appconn_name=datagofeishu，node_type=linkis.appconn.datagofeishu。
 */
public class DataGoFeishuAppConn extends AbstractAppConn implements OnlyDevelopmentAppConn {
    private DataGoFeishuDevelopmentStandard standard;

    @Override
    protected void initialize() {
        standard = new DataGoFeishuDevelopmentStandard();
    }

    @Override
    public DevelopmentIntegrationStandard getOrCreateDevelopmentStandard() {
        return standard;
    }
}