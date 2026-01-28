package com.webank.wedatasphere.dss.errorcode.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.linkis.server.BDPJettyServerHelper;

import java.util.HashMap;
import java.util.Map;

public class GsonHelper {

    public static Map<String, Object> getMapFromJson(String paramJson) {
        Map<String, Object> configParam = null;
        configParam = BDPJettyServerHelper.gson().fromJson(paramJson,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        if (null == configParam) {
            configParam = new HashMap<>();
        }
        return configParam;
    }

    public static String toJson(Object val) {
        return BDPJettyServerHelper.gson().toJson(val);
    }

    public static Gson getOwnGson() {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().create();
        return gson;
    }
}
