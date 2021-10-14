/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.appconn.schedulis.utils;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;

public class AzkabanUtils {
    public static String handleAzkabanEntity(String entityString) throws IOException {
        Gson gson = new Gson();
        Object object = gson.fromJson(entityString, Object.class);
        String status = null;
        String message = null;
        if (object instanceof Map) {
            Map map = (Map) object;
            if (map.get("status") != null) {
                status = map.get("status").toString();
            }
            if (StringUtils.isNotEmpty(status)) {
                if (null != map.get("message")) {
                    message = map.get("message").toString();
                }
            }
            if ("error".equalsIgnoreCase(status)) {
                return message;
            }
        }
        return "success";
    }

    public static String getValueFromEntity(String entityString, String searchKey) throws IOException {
        Gson gson = new Gson();
        Object object = gson.fromJson(entityString, Object.class);
        String status = null;
        String valueStr = null;
        if (object instanceof Map) {
            Map map = (Map) object;
            if (map.get("status") != null) {
                status = map.get("status").toString();
            }
            if (StringUtils.isNotEmpty(status) && status.equals("success")) {
                if (null != map.get(searchKey)) {
                    valueStr = map.get(searchKey).toString();
                }
            }
        }
        return valueStr;
    }
}
