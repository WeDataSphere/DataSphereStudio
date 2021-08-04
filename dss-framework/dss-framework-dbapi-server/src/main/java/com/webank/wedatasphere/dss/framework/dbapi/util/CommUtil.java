package com.webank.wedatasphere.dss.framework.dbapi.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommUtil {
    /**
     * 将object转换成数组
     *
     * @param object
     * @return
     */
    public static String[] objectToArray(Object object) {
        String[] newArray = null;
        if (object != null) {
            List<String> result = new ArrayList<>();
            if (object instanceof ArrayList<?>) {
                for (Object o : (List<?>) object) {
                    result.add(o == null ? null : String.valueOf(o));
                }
            }
            newArray = new String[result.size()];
            for (int i = 0; i < result.size(); i++) {
                newArray[i] = result.get(i);
            }
        }
        return newArray;
    }

    /**
     * 拼接where句柄
     *
     * @param requestFields
     * @return
     * @throws JSONException
     */

    public static String getWhereCause(String requestFields) throws JSONException {
        JSONArray jsonArray = new JSONArray(requestFields);
        StringBuilder whereCauseBuild = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i == 0) {
                whereCauseBuild.append(" where ");
            }
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String columnName = jsonObject.getString("name").trim();
            String columnNameFormat = String.format("%s%s%s","`",columnName,"`");
            String compareType = jsonObject.getString("compare").replaceAll("<","&lt;");
            String whereCause = String.format("%s %s #{%s}", columnNameFormat, compareType, columnName);
            System.out.println(whereCause);
            if (i < jsonArray.length() - 1) {
                whereCauseBuild.append(whereCause).append(" and ");
            } else {
                whereCauseBuild.append(whereCause);
            }
        }
        return whereCauseBuild.toString();
    }

    /**
     * 拼接 order by 句柄
     *
     * @param orderFields
     * @return
     * @throws JSONException
     */
    public static String getOrderCause(String orderFields) throws JSONException {
        JSONArray jsonArray = new JSONArray(orderFields);
        StringBuilder orderCauseBuild = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i == 0) {
                orderCauseBuild.append(" order by ");
            }
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String columnName = jsonObject.getString("name").trim();
            columnName = String.format("%s%s%s","`",columnName,"`");
            String orderType = jsonObject.getString("type");
            String orderCause = String.format("%s %s", columnName, orderType);
            System.out.println(orderCause);
            if (i < jsonArray.length() - 1) {
                orderCauseBuild.append(orderCause).append(",");
            } else {
                orderCauseBuild.append(orderCause);
            }
        }
        return orderCauseBuild.toString();
    }

}
