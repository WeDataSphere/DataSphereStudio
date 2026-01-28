package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

import java.util.List;

/**
 * linkis接口的引擎返回值
 * Author: xlinliu
 * Date: 2022/11/28
 */
public class ECInstanceHttpResponse {
    private int status;
    private String message;
    private Data data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data  getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    public static class Data{
        private List<ECInstance> ecList;

        public List<ECInstance> getEcList() {
            return ecList;
        }

        public void setEcList(List<ECInstance> ecList) {
            this.ecList = ecList;
        }
    }
}
