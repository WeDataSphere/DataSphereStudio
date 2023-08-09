package com.webank.wedatasphere.dss.common.server.beans;

import com.webank.wedatasphere.dss.common.entity.Alter;

public class ImsAlter extends Alter {

    /**
     * alterLevel 告警级别
     * 【数字】>=0，<=5,可选	0:clear，1:critical，2:major，3:minor，4:warning， 5:info
     * 如不传入，则使用默认值5:info
     * 告警级别 critical > major > minor > warning＞info
     */


    /**
     * alterReceiver 告警接收人
     * 【字符】最多15个接收人
     * 可选
     * 必须是RTX名称，输入错误时不会报错，但不能正常收到（单个接收人直接填写，如有多个接收人则通过半角逗号分隔）
     */


    public ImsAlter() {
    }

    public ImsAlter(String alterTitle, String alterInfo, String alterLevel, String alterReceiver) {
        super(alterTitle, alterInfo, alterLevel, alterReceiver);

    }

}
