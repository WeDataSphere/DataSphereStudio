package com.webank.wedatasphere.dss.framework.workspace.esb.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.workspace.bean.StaffInfo;
import com.webank.wedatasphere.dss.framework.workspace.esb.conf.EsbConf;
import com.webank.wedatasphere.dss.framework.workspace.service.StaffInfoGetter;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceDeptService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.linkis.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * created by cooperyang on 2020/4/2
 * Description:
 */
@Component
public class HttpStaffInfoGetter implements StaffInfoGetter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStaffInfoGetter.class);

    private static final Map<String, StaffInfo> STAFF_INFO_MAP = new HashMap<>();
    private static final Object LOCK = new Object();

    private static DSSWorkspaceDeptService DSSWorkspaceDeptService;
    @Autowired
    public void setWebankDSSWorkspaceDeptService(DSSWorkspaceDeptService DSSWorkspaceDeptService) {
        HttpStaffInfoGetter.DSSWorkspaceDeptService = DSSWorkspaceDeptService;
    }

    private static class StaffThread implements Runnable {
        private static final String APP_ID = EsbConf.ESB_APPID.getValue();
        private static final String TOKEN = EsbConf.ESB_TOKEN.getValue();
        private static final String DEFAULT_NONCE = "12345";
        private static final String RESULT_STR = "Result";
        private static final String CODE_STR = "Code";
        private static final String MESSAGE_STR = "Message";

        private String generateEsbUrl() {
            String urlPrefix = EsbConf.ESB_HTTP_URL.getValue() + EsbConf.ESB_HR_STAFF_URL.getValue();
            long timeStamp = System.currentTimeMillis() / 1000;
            String signature = generateSignature(timeStamp);
            return urlPrefix + "?" + "appid=" + APP_ID + "&" + "nonce=" + DEFAULT_NONCE +
                    "&" + "timestamp=" + timeStamp + "&" + "signature=" + signature;
        }

        private String generateSignature(long timeStamp) {
            String firstStr = APP_ID + DEFAULT_NONCE + timeStamp;
            String md5Str1 = md5Encrypt(firstStr).toLowerCase();
            String secondStr = md5Str1 + TOKEN;
            return md5Encrypt(secondStr).toLowerCase();
        }


        private String md5Encrypt(String data) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(data.getBytes(StandardCharsets.UTF_8));
                byte[] s = messageDigest.digest();
                StringBuilder result = new StringBuilder();
                for (byte b : s) {
                    result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
                }
                return result.toString();
            } catch (Exception e) {
                LOGGER.error("for {} to md5 failed", data, e);
                return "";
            }
        }

        @Override
        public void run() {
            try {
                HttpClient httpClient = HttpClients.custom().build();
                String esbUrl = generateEsbUrl();
                LOGGER.info("esb url is {} ", esbUrl);
                HttpGet httpGet = new HttpGet(esbUrl);
                HttpResponse response = httpClient.execute(httpGet);
                String content = IOUtils.toString(response.getEntity().getContent());
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(content).getAsJsonObject();
                JsonElement jsonArray = jsonObject.getAsJsonObject(RESULT_STR).getAsJsonArray("Data");
                if (jsonArray.isJsonArray()) {
                    ((JsonArray) jsonArray).forEach(node -> {
                        String nodeStr = node.toString();
                        try {
                            StaffInfo staffInfo = DSSCommonUtils.COMMON_GSON.fromJson(nodeStr, StaffInfo.class);
                            STAFF_INFO_MAP.put(staffInfo.getEnglishName(), staffInfo);
                        } catch (Exception e) {
                            LOGGER.error("failed to serialize a json {} ", nodeStr, e);
                        }
                    });
                    DSSWorkspaceDeptService.insertOrUpdateDept(STAFF_INFO_MAP);
                }
            } catch (Exception e) {
                LOGGER.error("fail to get esb response, reason is ", e);
            }
        }
    }

    static {
        Utils.defaultScheduler().scheduleAtFixedRate(new StaffThread(), 0, 1, TimeUnit.HOURS);
    }

    @Override
    public String getFullOrgNameByUsername(String username) {
        StaffInfo staffInfo = STAFF_INFO_MAP.get(username);
        if (staffInfo == null) {
            return "TCTP";
        } else {
            return staffInfo.getOrgFullName();
        }
    }

    @Override
    public List<String> getAllDepartments() {
        Set<String> orgNames = new HashSet<>();
        for (StaffInfo staffInfo : STAFF_INFO_MAP.values()) {
            String orgFullName = staffInfo.getOrgFullName();
            if (StringUtils.isNotEmpty(orgFullName)) {
                orgNames.add(orgFullName);
            }
        }
        return new ArrayList<>(orgNames);
    }

    @Override
    public List<StaffInfo> getAllUsers() {
        return new ArrayList<>(STAFF_INFO_MAP.values());
    }

}
