package com.webank.wedatasphere.dss.apiservice.core.exception;

import org.apache.linkis.common.exception.ErrorException;

public class ApiServiceParamsCheckException extends ErrorException {

    public ApiServiceParamsCheckException(int errCode, String desc) {
        super(errCode, desc);
    }

    public ApiServiceParamsCheckException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }
}