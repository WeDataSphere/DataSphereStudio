package com.webank.wedatasphere.dss;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * gson的辅助类
 * Author: xlinliu
 * Date: 2023/12/6
 */
public class GsonUtils {
    public static String getString(JsonObject jsonObject,String fieldName){
        if (!jsonObject.has(fieldName)) {
            return null;
        }
        JsonElement element = jsonObject.get(fieldName);
        return element.isJsonNull() ? null : element.getAsString();
    }

}
