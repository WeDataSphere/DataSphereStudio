package com.webank.wedatasphere.dss.detection.server.utils;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @date 2023/3/30 13:35
 */
public class ConstantUtils {

    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String HOME = "home";
    public static final String ID = "id";
    public static final String CAR = "car";
    public static final String CN = "cn";
    public static final String OPENID = "openid";
    public static final String BANK = "bank";

    public static String maskAll(String senseInfo) {
        if (StringUtils.isBlank(senseInfo)) {
            return senseInfo;
        }
        Map<String , String> map = new LinkedHashMap<>();
        String[] split = senseInfo.split(";");
        for (String str : split) {
            String[] senseKeyValue = str.split(":");
            map.put(senseKeyValue[0], senseKeyValue[1]);
        }
        // 对各个类型的校验规则脱敏
        for (String key : map.keySet()) {
            String idMask = mask(key, map.get(key));
            // 更新
            map.put(key, idMask);
        }
        // 获取脱敏后的所有敏感信息
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        String auditSenseInfo = sb.toString();
        return auditSenseInfo;
    }

    private static String mask(String key, String line) {
        if (line == null || line.equals("")) {
            return "";
        }
        List<String> list= Arrays.asList(line.split(","));
        List<String> result = new ArrayList<>();
        list.forEach(str -> {
            result.add(maskLine(key, str));
        });

        return String.join(",", result);
    }

    private static String maskLine(String key, String line) {
        if (line == null || line.equals("")) {
            return "";
        }
        String maskLine = "";
        switch (key) {
            case ConstantUtils.ID:
                maskLine = line.substring(0, 1) + generateMask(16) + line.substring(line.length() - 1, line.length());
                return maskLine;
            case ConstantUtils.BANK:
                maskLine = line.substring(0, 6) + generateMask(line.length() - 10) + line.substring(line.length() - 4, line.length());
                return maskLine;
            case ConstantUtils.PHONE:
                maskLine = line.substring(0, 3) + generateMask(6) + line.substring(line.length() - 2, line.length());
                return maskLine;
            case ConstantUtils.EMAIL:
                String[] emailArray = line.split("@");
                maskLine = line.substring(0, 1) + generateMask(emailArray[0].length() - 1) + line.substring(emailArray[0].length(), line.length());
                return maskLine;
            case ConstantUtils.HOME:
            case ConstantUtils.CAR:
                maskLine = line.substring(0, 2) + generateMask(line.length() - 2) + line.substring(line.length() - 2, line.length());
                return maskLine;
            case ConstantUtils.CN:
                maskLine = line.substring(0, 1) + generateMask(line.length() - 1);
                return maskLine;
            case ConstantUtils.OPENID:
                maskLine = line.substring(0, 5) + generateMask(10) + line.substring(line.length() - 7, line.length());
                return maskLine;
            default:
                return maskLine;
        }
    }
    private static String generateMask(int len) {
        StringBuilder pudding = new StringBuilder();

        for(; len > 0; --len) {
            pudding.append("*");
        }

        return pudding.toString();
    }

}
