package com.webank.wedatasphere.dss.detection.server.message;

import java.io.IOException;

/**
 * @date 2023/3/15 11:44
 */
public interface DetectionMessage {

    SubmitInformation getSubmitInformation();

    void setSubmitInformation(SubmitInformation submitInformation);

    String getMessageType();

    Boolean clearMessage() throws IOException;

    Boolean isDone();

    void done() throws IOException;

    String getMessage() throws IOException;


}
