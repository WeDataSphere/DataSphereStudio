package com.webank.wedatasphere.dss.datamap.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.datamap.constant.EnvTypeEnum;
import com.webank.wedatasphere.dss.datamap.datamap.HttpUtils;
import com.webank.wedatasphere.dss.datamap.domain.ITSMResp;
import com.webank.wedatasphere.dss.datamap.exception.ITSMException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ITSMRequestUtils {

    private static final Logger logger = LoggerFactory.getLogger(ITSMRequestUtils.class);

    /**
     * userId，appId，appKey为一套参数，需要向ITSM助手申请
     * @param env itsm环境信息
     * @param userId 配置的ITSM用户id，为某个员工的英文名
     * @param appId 与该员工对应的应用id
     * @param appKey 与该员工对应的应用key
     * @return 请求头
     */
    private static Map<String, String> generateHeader(String env, String userId, String appId, String appKey) throws Exception {

        //生成random参数
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            logger.error("请求ITSM服务获取SHA1PRNG算法异常", e);
            throw new ITSMException("请求ITSM服务获取SHA1PRNG算法异常");
        }
        int randomMax = 9999;
        int randomMin = 1000;

        //生成sign参数
        // 测试环境使用固有值，生产使用生成的值
        String sign;
        int randomInt;
        //请求头
        Map<String, String> header = new HashMap<>();
        if (EnvTypeEnum.TEST.getEnName().equals(env)) {
            sign = "fe64bed95dbc2e81c843e9af775ad6b4";
            randomInt = 4945;
            header.put("timestamp", "1664332415886");
        } else {
            randomInt = random.nextInt(randomMax) % (randomMax - randomMin + 1) + randomMin;
            sign = MD5(MD5(appKey + randomInt + System.currentTimeMillis()) + userId);
            header.put("timestamp", String.valueOf(System.currentTimeMillis()));
        }

        header.put("Content-Type", "application/json");
        header.put("appId", appId);
        header.put("random", String.valueOf(randomInt));
        header.put("userId", userId);
        header.put("sign", sign);

        logger.info(JSONObject.toJSONString(header));

        return header;

    }

    /**
     *
     * @param title ITSM表单标题
     * @param urgency ITSM表单紧急度
     * @param endDate ITSM表单处理结束日期
     * @param desc ITSM表单描述
     * @param UserIds ITSM表单关注用户，以逗号分割
     * @param Owner ITSM表单请求人
     * @param dataList ITSM表单内容，key为属性，value为属性值
     * @param formId ITSM表单ID，测试环境和生产环境不相同
     * @param formVersion ITSM表单对应的版本号
     * @param requestRatifyChain ITSM审批链信息，有几个审批人该list就有几个元素，按审批顺序往该list中写入元素，每个元素也都是一个list，其中包含四个元素，依次是审批人id，
     *                           审批人类型，审批链类型，审批名称
     */
    private static JSONObject generateBody(String title, String urgency, String endDate, String desc, String UserIds, String Owner,
                                          Map<String, Object> dataList, String formId, String formVersion, ArrayList<ArrayList<String>> requestRatifyChain) {

        JSONObject body = new JSONObject();

        // 表单公共部分
        body.put("requestTitle", title);
        body.put("requestUrgency", urgency);
        body.put("requestEndDate", endDate);
        body.put("requestDesc", desc);
        body.put("notifyUserIds", UserIds);
        body.put("requestOwner", Owner);

        // 表单内容
        Map<String, Object> forms = new HashMap<>();
        Map<String, Object> formDataValue = new HashMap<>();
        formDataValue.put("dataList", Lists.newArrayList(dataList));
        forms.put("formDataValue", formDataValue);
        forms.put("formId", formId);
        forms.put("formVersion", formVersion);
        body.put("forms", Lists.newArrayList(forms));

        // 审批链
        ArrayList<Map<String, Object>> requestRatifyChains = Lists.newArrayList();

        requestRatifyChain.forEach(t -> requestRatifyChains.add(generateChains(t.get(0), t.get(1), t.get(2), t.get(3))));

        body.put("requestRatifyChains", requestRatifyChains);

        logger.info(JSONObject.toJSONString(body));

        return body;

    }

    /**
     *
     * @param env itsm环境信息
     * @param url ITSM接口url
     * @return ITSM单号
     */
    public static Integer doJsonPOST2(String env, String url, String userId, String appId, String appKey, String title, String urgency, String endDate,
                                      String desc, String UserIds, String Owner, Map<String, Object> dataList, String formId, String formVersion, ArrayList<ArrayList<String>> requestRatifyChain) throws Exception{

        Map<String, String> header = generateHeader(env, userId, appId, appKey);
        JSONObject parameters = generateBody(title, urgency, endDate, desc, UserIds, Owner, dataList, formId, formVersion, requestRatifyChain);


        // 生产环境使用http请求，测试环境使用https请求
        CloseableHttpClient client;
        if (EnvTypeEnum.TEST.getEnName().equals(env) || EnvTypeEnum.OA.getEnName().equals(env)) {
            OpenApiClient openApiClient = new OpenApiClient();
            client = openApiClient.getOpenApiHttpClient();
        } else {
            client = HttpUtils.getConnection();
        }
        logger.info("请求的ITSM接口url为{}", url);
        HttpPost request = (HttpPost) HttpUtils.getRequestMethod("post", URI.create(url), header);

        //设置和格式化参数
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(parameters.toJSONString(), charSet);
        request.setEntity(entity);

        CloseableHttpResponse response = null;
        try {
            response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            logger.info("ITSM的响应值为{}", statusLine.getStatusCode());
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                String result = EntityUtils.toString(responseEntity);
                ITSMResp itsmResp = JSONObject.parseObject(result, ITSMResp.class);
                if (itsmResp.getRetCode() == 0) {
                    return itsmResp.getData();
                } else {
                    throw new ITSMException(itsmResp.getRetDetail());
                }
            } else {
                throw new ITSMException("请求返回失败，状态码：" + statusLine.getStatusCode() + "请求URL：" + url);
            }
        } catch (IOException e) {
            logger.error("ITSM服务异常", e);
            throw new ITSMException("ITSM服务异常");
        }finally {
            client.close();
            if (response != null) {
                response.close();
            }
        }
    }

    private static Map<String, Object> generateChains(String user, String userType, String eoaChainStepType, String eoaChainStepName) {

        HashMap<String, Object> eoaChainUserList = new HashMap<>();
        eoaChainUserList.put("baseUserId", user);
        eoaChainUserList.put("userType", Integer.parseInt(userType));

        HashMap<String, Object> requestRatifyChains = new HashMap<>();

        requestRatifyChains.put("eoaChainUsers", user);
        requestRatifyChains.put("eoaChainStepType", Integer.parseInt(eoaChainStepType));
        requestRatifyChains.put("eoaChainStepName", eoaChainStepName);
        requestRatifyChains.put("eoaChainUserList", Lists.newArrayList(eoaChainUserList));

        return requestRatifyChains;

    }

    private static String MD5(String sourceStr) throws Exception {
        StringBuilder buf = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte[] b = md.digest();
            int i;
            for (byte value : b) {
                i = value;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("请求ITSM服务时MD5加密异常", e);
            throw new ITSMException("请求ITSM服务时MD5加密异常");
        }
        return buf.toString();
    }


}
