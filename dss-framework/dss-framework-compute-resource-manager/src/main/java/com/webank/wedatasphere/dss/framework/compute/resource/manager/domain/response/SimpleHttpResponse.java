package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

/**
 * http 外部请求结果
 * Author: xlinliu
 * Date: 2022/11/28
 */
public class SimpleHttpResponse {
    private  int statusCode;
    private String body;


    public SimpleHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
