package com.webank.wedatasphere.dss.detection.server.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @date 2023/3/15 11:53
 */
public abstract class StreamMessage implements DetectionMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamMessage.class);

    private String message = null;

    private String messageType = "Stream";

    private Boolean isDone = false;

    private SubmitInformation submitInformation;

    @Override
    public SubmitInformation getSubmitInformation() {
        return submitInformation;
    }

    @Override
    public void setSubmitInformation(SubmitInformation submitInformation) {
        this.submitInformation = submitInformation;
    }

    @Override
    public String getMessageType() {
        return messageType;
    }

    @Override
    public Boolean clearMessage() throws IOException {
        message = "";
        return true;
    }

    @Override
    public Boolean isDone() {
        return isDone;
    }

    @Override
    public void done() throws IOException {
        isDone = true;
        LOGGER.info("Done! success to detect message!");
    }

    @Override
    public String getMessage() throws IOException {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
