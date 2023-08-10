/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.datamap;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.webank.wedatasphere.dss.apiservice.core.util.HttpClientUtil;
import org.apache.linkis.common.conf.CommonVars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Description: 用于datamap 提单、查询单 等操作
 */
public class DataMapUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataMapUtils.class);

    private static final String DATAMAP_URL = CommonVars.apply("wds.dss.api.datamap.url",
            "http://127.0.0.1:9001").getValue();
    private static final String DATAMAP_SAVE_URL = CommonVars.apply("wds.dss.api.datamap.saveurl",
            "http://127.0.0.1:9001").getValue();
    private static final String DATAMAP_APPLY_SUFFIX = CommonVars.apply("wds.dss.api.datamap.apply.suffix",
            "/front/dgsa/openapi/form/request/saveNew").getValue();
    private static final String DATAMAP_REQUEST_SUFFIX = CommonVars.apply("wds.dss.api.datamap.request.suffix",
            "/datamap/dgsa/request/queryStepType").getValue();
    //    private static final String DATAMAP_REQUEST_SUFFIX = CommonVars.apply("wds.dss.api.datamap.request.suffix",
//            "/front/dgsa/openapi/form/request/queryStepType").getValue();
    private static final String DATAMAP_REQUEST_DEVPRINS_SUFFIX = CommonVars.apply("wds.dss.api.datamap.request.devprins",
            "/datamap/api/external/getDevPrincipals").getValue();
    private static final String DATAMAP_APPID = CommonVars.apply("wds.dss.api.datamap.appid",
            "12345678").getValue();
    private static final String DATAMP_APPTOKEN = CommonVars.apply("wds.dss.api.datamap.apptoken",
            "DATAMP-AUTH").getValue();
    private static final String DEFAULT_NONCE = CommonVars.apply("wds.dss.api.datamap.nonce", "56689").getValue();
    private static final String DATAMAP_USER = CommonVars.apply("wds.dss.api.datamap.user", "hadoop").getValue();
    private static final String DATAMAP_NOTIFY_USER = CommonVars.apply("wds.dss.api.datamap.notify.user", "hadoop,test-user").getValue();
    private static final String DATAMAP_OTTHID = CommonVars.apply("wds.dss.api.datamap.otthid", "DWS-IDE-APISERVICE").getValue();
    private static final String DATAMAP_FORM_TYPE = CommonVars.apply("wds.dss.api.datamap.formtype", "bdap_job_apply").getValue();

    // bdap/bdp/qdy
    public static final String DATAMAP_DEVPRINS_CLUSTERTYPE = CommonVars.apply("wds.dss.api.datamap.devprins.clustertype", "bdap").getValue();
    // uat/prod
    public static final String DATAMAP_DEVPRINS_ENV = CommonVars.apply("wds.dss.api.datamap.devprins.env", "uat").getValue();


    private static final String OTTH_ID_STR = "otthId";
    private static final String FORM_TYPE_STR = "formType";
    private static final String REQUEST_TITLE = "requestTitle";
    private static final String REQUEST_OWNER_ID = "requestOwnerId";
    private static final String CONTENT_DATA = "contentData";

    private static final String REQUEST_DESC = "requestDesc";
    private static final String NOTIFY_USERS = "notifyUsers";
    private static final String REQUEST_STATUS_TEXT = "requestStatusText";

    // for datamap test
    public static void main(String[] args) throws Exception {
        String otthId = UUIDGenerator.genUUID();
        DataMapApplyContentData dataMapApplyContentData = new DataMapApplyContentData();

        boolean isSuccess = applyDataMap(otthId, "tom", "for test", dataMapApplyContentData);
        System.out.println(isSuccess);
        DataMapStatus dataMapStatus = requestDataMapStatus(DATAMAP_OTTHID);
        System.out.println(dataMapStatus.getValue());
    }

    public static boolean applyDataMap(String otthId,
                                       String requestUser,
                                       String requestTitle,
                                       DataMapApplyContentData dataMapApplyContentData) {
        return applyDataMap(otthId, requestUser, requestTitle, "test sql", "test user", Arrays.asList(dataMapApplyContentData));
    }

    /**
     * 向datamap进行提单
     * otthId 全局唯一
     * requestUser 提单人
     * requestTitle 提单标题
     * 提单成功返回true 否则 false
     */
    public static boolean applyDataMap(String otthId,
                                       String requestUser,
                                       String requestTitle,
                                       String requestDesc,
                                       String notifyUsers,
                                       List<DataMapApplyContentData> dataMapApplyContentDatas) {
        String applyUrl = null;
        try {
            applyUrl = generateDataMapApplyUrl(System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            LOGGER.error("can not get apply url", e);
            return false;
        }
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put(OTTH_ID_STR, otthId);
        reqParams.put(FORM_TYPE_STR, DATAMAP_FORM_TYPE);
        reqParams.put(REQUEST_TITLE, requestTitle);
        reqParams.put(REQUEST_OWNER_ID, requestUser);
        reqParams.put(REQUEST_DESC, requestDesc);
        //关注人
        reqParams.put(NOTIFY_USERS, notifyUsers);
        reqParams.put(REQUEST_STATUS_TEXT, "");
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (DataMapApplyContentData data : dataMapApplyContentDatas) {
            sb.append(data.toString()).append(",");
        }
        String s = sb.toString().substring(0, sb.length() - 1);
        String finalS = s + "]";
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        reqParams.put(CONTENT_DATA, finalS);
        String reqJson = gson.toJson(reqParams);
        LOGGER.info("json is {}", reqJson);
        LOGGER.info("url is {}", applyUrl);
        String response = HttpClientUtil.postJsonBody(applyUrl, 10000, new HashMap<>(), reqJson, "UTF-8");
        LOGGER.info("response is {}", response);
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();
            int retCode = jsonObject.get("retCode").getAsInt();
            if (retCode != 0) {
                LOGGER.error("failed to save to datamap");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("fail to do with {} , e is ", response, e);
            return false;
        }
    }

    /**
     * @param dbName
     * @param tableName
     * @param env         操作环境: prod/uat
     * @param clusterType 集群：bdp/bdap/qdy
     * @return
     */
    public static Map<String, String> getDevPrincipalsAndStatus(String dbName, String tableName, String env, String clusterType) throws Exception {
        String requestUrl = generateDataMapGetDevPrinsUrl(System.currentTimeMillis() / 1000);
        Map<String, Object> reqParams = new HashMap<>();
        List<Object> reqList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("clusterType", clusterType);
        params.put("env", env);
        params.put("dbName", dbName);
        params.put("tableName", tableName);
        reqList.add(params);
        reqParams.put("tableList", reqList);
        LOGGER.info("requestUrl for getDevPrincipals is: {}", requestUrl);
        String reqJson = new Gson().toJson(reqParams);
        LOGGER.info("requestJson is {}", reqJson);
        String response = HttpClientUtil.postJsonBody(requestUrl, 10000, new HashMap<>(), reqJson, "UTF-8");
        LOGGER.info("response is {}", response);
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();
        int retCode = jsonObject.get("retCode").getAsInt();
        Map<String, String> retMap = new HashMap<>();
        if (retCode != 0) {
            LOGGER.error("failed to get datamap dev principals");
            return retMap;
        } else {
            JsonArray data = jsonObject.get("data").getAsJsonArray();
            JsonElement devPrinEle = data.get(0).getAsJsonObject().get("devPrincipals");
            JsonElement statusEle = data.get(0).getAsJsonObject().get("status");
            retMap.put("devPrincipals", devPrinEle == null ? null : devPrinEle.getAsString());
            retMap.put("status", devPrinEle == null ? null : statusEle.getAsString());
            return retMap;
        }
    }

    /**
     * 向datamap进行获取datamap的状态
     * 现在默认通过otthId来查询单号状态
     *
     * @param otthId 不能为null
     * @return
     */
    public static DataMapStatus requestDataMapStatus(String otthId) throws Exception {
        List<String> otthIds = Arrays.asList(otthId);
        Map<String, Object> reqParams = new HashMap<>();
//        reqParams.put(FORM_TYPE_STR, DATAMAP_FORM_TYPE);
        reqParams.put("otthIds", otthIds);
        String requestUrl = generateDataMapRequestUrl(System.currentTimeMillis() / 1000);
        LOGGER.info("requestUrl is {}", requestUrl);
        String reqJson = new Gson().toJson(reqParams);
        LOGGER.info("requestJson is {}", reqJson);
        String response = HttpClientUtil.postJsonBody(requestUrl, 10000, new HashMap<>(), reqJson, "UTF-8");
        LOGGER.info("response is {}", response);
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();
        try {
            int retCode = jsonObject.get("retCode").getAsInt();
            if (0 != retCode) {
                LOGGER.error("failed to get datamap status");
                return DataMapStatus.APPROVING;
            } else {
                String data = jsonObject.get("data").getAsString();
//                LOGGER.info(data);
                List<Map<String, Object>> mapList = new Gson().fromJson(data, new TypeToken<List<Map<String, Object>>>() {
                }.getType());

                if (mapList.size() == 0) {
                    LOGGER.error(" get datamap status failed for data is empty,please check approval number");
                    return DataMapStatus.EMPTY;
                }
                for (Map<String, Object> m : mapList) {
//                    int stepType = e.getAsJsonObject().get("stepType").getAsInt();
                    int stepType = ((Double) m.get("stepType")).intValue();
                    LOGGER.info("接受到DataMap的返回状态码为：{}", stepType);
                    switch (stepType) {
                        case 93:
                        case 7:
                            return DataMapStatus.SUCCESS;
                        case 92:
                        case 91:
                            return DataMapStatus.FAILED;
                        case 90:
                            return DataMapStatus.REJECT;
                        case 0:
                            return DataMapStatus.INITED;
                        default:
                            return DataMapStatus.APPROVING;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("can not resolve datamap status", e);
            throw e;
        }
        return DataMapStatus.APPROVING;
    }

    private static String generateDataMapApplyUrl(long timeStamp) throws Exception {
        String signature = generateSignature(timeStamp);
        return DATAMAP_SAVE_URL + DATAMAP_APPLY_SUFFIX + "?" + "appid=" + DATAMAP_APPID + "&nonce=" + DEFAULT_NONCE + "&timestamp=" + timeStamp +
                "&signature=" + signature + "&loginUser=" + DATAMAP_USER;
    }

    private static String generateDataMapRequestUrl(long timeStamp) throws Exception {
        String signature = generateSignature(timeStamp);
        return DATAMAP_URL + DATAMAP_REQUEST_SUFFIX + "?" + "appid=" + DATAMAP_APPID + "&nonce=" + DEFAULT_NONCE + "&timestamp=" + timeStamp +
                "&signature=" + signature + "&loginUser=" + DATAMAP_USER;
    }

    private static String generateDataMapGetDevPrinsUrl(long timeStamp) throws Exception {
        String signature = generateSignature(timeStamp);
        return DATAMAP_URL + DATAMAP_REQUEST_DEVPRINS_SUFFIX + "?" + "appid=" + DATAMAP_APPID + "&nonce=" + DEFAULT_NONCE + "&timestamp=" + timeStamp +
                "&signature=" + signature + "&loginUser=" + DATAMAP_USER;
    }


    private static String md5Encrypt(String data) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] s = messageDigest.digest();
        StringBuilder result = new StringBuilder();
        for (byte b : s) {
            result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
        }
        return result.toString();
    }

    private static String generateSignature(long timeStamp) throws Exception {
        String firstStr = DATAMAP_APPID + DEFAULT_NONCE + DATAMAP_USER + timeStamp;
        String md5Str1 = md5Encrypt(firstStr).toLowerCase();
        String secondStr = md5Str1 + DATAMP_APPTOKEN;
        return md5Encrypt(secondStr).toLowerCase();
    }


}
