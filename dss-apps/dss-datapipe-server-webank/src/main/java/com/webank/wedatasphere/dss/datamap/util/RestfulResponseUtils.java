package com.webank.wedatasphere.dss.datamap.util;

import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.exception.ITSMException;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.server.Message;

/**
 * @author: jinyangrao on 2020/11/04
 */
public class RestfulResponseUtils {

    public static Message doAndResponse(TryOperation tryOperation, String method, String failMessage) {
        try {
            Message message = tryOperation.operateAndGetMessage();
            return setMethod(message, method);
        }catch (DataMapException e) {
            return setMethod(Message.error(e.getMessage()), method);
        } catch (ITSMException e) {
            return setMethod(Message.error(failMessage + e.getMessage()), method);
        } catch (WarnException e) {
            return setMethod(Message.warn(e.getMessage()), method);
        } catch (Exception e) {
            return setMethod(Message.error(failMessage, e), method);
        }
    }

    private static Message setMethod(Message message, String method) {
        message.setMethod(method);
        return message;
    }

    @FunctionalInterface
    public interface TryOperation {
        Message operateAndGetMessage() throws Exception;
    }
}