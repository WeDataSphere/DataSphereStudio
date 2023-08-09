package com.webank.wedatasphere.dss.common.server.alter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webank.wedatasphere.dss.common.entity.Alter;
import com.webank.wedatasphere.dss.common.entity.CustomAlter;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.server.beans.ImsAlter;
import com.webank.wedatasphere.dss.common.server.conf.CommonServerConfiguration;
import com.webank.wedatasphere.dss.common.server.utils.HttpClientUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ImsAlterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsAlterClient.class);

    private static ImsAlterClient INSTANCE = null;

    private ImsAlterClient() {
    }

    public static ImsAlterClient getInstance() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }

    private static synchronized void init() {
        if (INSTANCE == null) {
            INSTANCE = new ImsAlterClient();
        }
    }



    public void send(Alter imsAlter) throws Exception {

        if (imsAlter == null || imsAlter.getAlterTitle() == null) {
            LOGGER.warn("Alter Information is empty!");
            throw new DSSErrorException(100788, "Alter Information is empty!");
        }

        List<NameValuePair> paramsList = new ArrayList<>();
        paramsList.add(new BasicNameValuePair("userAuthKey", CommonServerConfiguration.IMS_USER_AUTH_KEY.getValue()));
        paramsList.add(new BasicNameValuePair("alert_title", imsAlter.getAlterTitle()));
        if (imsAlter.getAlterLevel() != null) {
            paramsList.add(new BasicNameValuePair("alert_level", imsAlter.getAlterLevel()));
        }
        if (imsAlter.getAlterReceiver() != null) {
            paramsList.add(new BasicNameValuePair("alert_reciver", imsAlter.getAlterReceiver()));
        }
        paramsList.add(new BasicNameValuePair("alert_way", "1,2"));
        paramsList.add(new BasicNameValuePair("alert_obj", CommonServerConfiguration.DSS_DEPLOY_ENV.getValue()));
        paramsList.add(new BasicNameValuePair("sub_system_id", CommonServerConfiguration.DSS_SYSTEM_ID.getValue()));

        if (imsAlter.getAlterInfo() != null) {
            paramsList.add(new BasicNameValuePair("alert_info", imsAlter.getAlterInfo()));
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String bodyString = gson.toJson(paramsList);

        String imsUrl = CommonServerConfiguration.IMS_ALERT_API_URL.getValue();

        LOGGER.info("Ims alter params: {} , url: {}", bodyString, imsUrl);

        HttpClientUtil.postForm(imsUrl, null, paramsList);
    }



}
