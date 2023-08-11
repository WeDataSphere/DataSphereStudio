package com.webank.wedatasphere.dss.detection.server.message;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @date 2023/3/15 12:10
 */
public class InputStreamMessage extends StreamMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamMessage.class);

    private InputStream inputStream;

    private String messageType = "inputStream";

    private BufferedReader reader;


    @Override
    public String getMessageType() {
        return this.messageType;
    }

    public void setMessage(InputStream message) {
        this.inputStream = message;
        InputStreamReader inputStreamReader = new InputStreamReader(this.inputStream);
        reader = new BufferedReader(inputStreamReader);
    }


    @Override
    public void done() throws IOException {
        super.done();
        inputStream.close();
        reader.close();
    }

    @Override
    public String getMessage() throws IOException {
        try {
            String line = reader.readLine();
            if(line != null) {
                return line;
            } else {
                done();
            }
        } catch (IOException e) {
            LOGGER.error("readline from inputStream failed, failed to detect message!", e.getMessage());
            done();
            return null;
        }
        return null;
    }
}
