package com.webank.wedatasphere.dss.common.exception;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.server.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//为aop不能正常捕获的异常兜底
@ControllerAdvice
public class GlobalExceptionHandler {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(ErrorException.class)
    @ResponseBody
    public Message handleErrorException(ErrorException e){
        LOGGER.error("[GlobalExceptionHandler]:捕获到ErrorException", e);
        return Message.error(e.getDesc());
    }

    @ExceptionHandler(WarnException.class)
    @ResponseBody
    public Message handleWarnException(WarnException e){
        LOGGER.error("[GlobalExceptionHandler]:捕获到handlerWarnException", e);
        return Message.error(e.getDesc());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Message handleException(Exception e){
        LOGGER.error("[GlobalExceptionHandler]:捕获到Exception，原因:"+ ExceptionUtils.getRootCauseMessage(e), e);
        return Message.error("服务端异常"+e.getMessage());
    }

}
