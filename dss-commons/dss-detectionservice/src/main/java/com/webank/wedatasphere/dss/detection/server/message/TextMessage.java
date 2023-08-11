package com.webank.wedatasphere.dss.detection.server.message;

/**
 *
 * 单文本信息，单次输入，单次输出
 * @date 2023/3/15 11:46
 */
public class TextMessage implements DetectionMessage {

    private String message = null;

    private String messageType;

    private SubmitInformation submitInformation;

    /**
     * 判断是否结束的唯一标志，如果isDone为false，检测会一直进行
     */
    private Boolean isDone = false;

    public TextMessage(String text) {
        this.message = text;
    }

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
    public String getMessage() {
        String tmp = message;
        clearMessage();
        return tmp;
    }

    /**
     * 对于text类型的message，clear数据之后默认结束
     * @return
     */
    @Override
    public Boolean clearMessage() {
        message = null;
        isDone = true;
        return true;
    }

    @Override
    public Boolean isDone() {
        return isDone;
    }

    @Override
    public void done() {
        isDone = true;
    }


}
