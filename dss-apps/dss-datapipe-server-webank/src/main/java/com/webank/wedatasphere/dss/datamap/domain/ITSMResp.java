package com.webank.wedatasphere.dss.datamap.domain;

public class ITSMResp {

    private Integer data;

    private Integer retCode;

    private String retDetail;

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }

    @Override
    public String toString() {
        return "ITSMResp{" +
                "data=" + data +
                ", retCode=" + retCode +
                ", retDetail='" + retDetail + '\'' +
                '}';
    }
}
