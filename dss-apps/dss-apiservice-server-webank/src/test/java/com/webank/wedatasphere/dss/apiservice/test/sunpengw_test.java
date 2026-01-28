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
package com.webank.wedatasphere.dss.apiservice.test;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import com.sun.jersey.core.util.Base64;



public class sunpengw_test {

    public static final Pattern WRITABLE_PATTERN = Pattern.compile("^\\s*(insert|update|delete|drop|alter|create).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static void main(String[] args) {

        // String content = "select * from a where a = 'a;create table';create table test0515 (a string);";
//        String content = "select * from duosheet11 where a='aaa;create table';\ncreate table";
////        for (String c : content.split(";")){
////            boolean flag = WRITABLE_PATTERN.matcher(c).matches();
////            System.out.println(flag);
////        }
//        String regex_sql = content.replaceAll("=\\s*'.*?'", "='#'");
//        System.out.println(regex_sql);
//        for (String sql : regex_sql.split(";")) {
//
//            System.out.println(WRITABLE_PATTERN.matcher(sql.trim()).matches());
//        }

//        System.out.println(content.replaceAll("'.*?'","'#'"));
//        System.out.println(WRITABLE_PATTERN.matcher(content).matches());
//        String a = "1";
//        System.out.println(StrUtil.equalsAny(a, "2", "111","13"));


//        List<String> a = new ArrayList<>();
//        a.add("bbb");
////        System.out.println(CollectionUtils.isEmpty(a));
////        a.add("bbb");
//        a.add("aaa");
//        List <String> b = new ArrayList<>();
//        b.add("aaa");
////        b.add("bbb");
//
//
//        List<String> c = new ArrayList<>();
//        c.add("bbb");
////        System.out.println(CollectionUtils.isEmpty(a));
////        a.add("bbb");
////        c.add("aaa");
////
////        System.out.println(CollectionUtils.isEqualCollection(a, b));
//
//        boolean flag = !CollectionUtils.isEqualCollection(a,b) && !CollectionUtils.isEqualCollection(a,c);
//        System.out.println(flag);

//        String uuid = "05e6ad08-3a01-4441-914d-a1b1bcfce644";
//        System.out.println(StringUtils.isNotBlank(uuid));
//        System.out.println(Base64.encode("LINKISDSSSECRET"));
//
//        System.out.println(org.apache.commons.codec.binary.Base64.encodeBase64("LINKISDSSSECRET".getBytes()));


        String entityString = "{\n" +
                "  \"error\" : null,\n" +
                "  \"message\" : \"testhuigui.test0001 开始定时调度.\",\n" +
                "  \"scheduleId\" : 1886,\n" +
                "  \"status\" : \"success\"\n" +
                "}";

        if(StringUtils.isNotBlank(entityString)){
            if(entityString.startsWith("{") && entityString.endsWith("}")){
                Map resMap = DSSCommonUtils.COMMON_GSON.fromJson(entityString, Map.class);
                //
                if(resMap.containsKey("error") && !Objects.isNull(resMap.get("error")) ){
                    System.out.println("===========");
                    System.out.println((String)resMap.get("error"));
                }
            }
            if(entityString.contains("<div class=\"container-full\">") && entityString.contains("<div class=\"login\">")){
                System.out.println("The SSO login status is invalid.Please refresh the browser and log in again!");
            }
        }


        Object object = DSSCommonUtils.COMMON_GSON.fromJson(entityString, Object.class);
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
                System.out.println("2222222222222");
                System.out.println(message);
            }
        }
        System.out.println("333333333 success");


        String timestamp = String.valueOf(System.currentTimeMillis());
        String key = "350965f1d6dfc38757cba3c34478163176aafcb2ed5ff2478d94a43b40d3ae42";
        String sign = DigestUtil.sha256Hex(key + timestamp);
        System.out.println(sign);
        System.out.println(timestamp);


    }


}
