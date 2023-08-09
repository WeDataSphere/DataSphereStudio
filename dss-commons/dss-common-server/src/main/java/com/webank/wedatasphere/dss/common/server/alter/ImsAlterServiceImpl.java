package com.webank.wedatasphere.dss.common.server.alter;

import com.webank.wedatasphere.dss.common.alter.ExceptionAlterSender;
import com.webank.wedatasphere.dss.common.entity.Alter;

public class ImsAlterServiceImpl implements ExceptionAlterSender {
    @Override
    public void sendAlter(Alter alter) {
        try {
            ImsAlterClient.getInstance().send(alter);
        } catch (Exception e) {
            throw new RuntimeException("调用Ims异常：" + e);
        }

    }
}
