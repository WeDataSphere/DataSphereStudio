package com.webank.wedatasphere.dss.datamap.datamap;

public class MultiChanelInfo {

    //当前环境的值
    private String currentEnv;

    //映射环境的值
    private String otherEnv;

    //推断的值
    private String inference;

    public String getCurrentEnv() {
        return currentEnv;
    }

    public void setCurrentEnv(String currentEnv) {
        this.currentEnv = currentEnv;
    }

    public String getOtherEnv() {
        return otherEnv;
    }

    public void setOtherEnv(String otherEnv) {
        this.otherEnv = otherEnv;
    }

    public String getInference() {
        return inference;
    }

    public void setInference(String inference) {
        this.inference = inference;
    }
}
