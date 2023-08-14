package com.webank.wedatasphere.dss.framework.compute.resource.manager.util;

import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.SimpleHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * restful客户端
 * Author: xlinliu
 * Date: 2022/11/25
 */
public final class HttpClientUtil {
    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    public final static int connectTimeout = 5000;

    private static CloseableHttpClient httpclient ;

    static {
        httpclient = HttpClients.createDefault();
    }


    public static SimpleHttpResponse postForm(String url, int timeout, Map<String, Object> headerMap, List<NameValuePair> paramsList, String encoding){
        HttpPost post = new HttpPost(url);
        String content="";
        int statusCode=502;
        try {
            if(headerMap != null){
                for(Map.Entry<String, Object> entry : headerMap.entrySet()){
                    post.setHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setExpectContinueEnabled(false).build();
            post.setConfig(requestConfig);

            post.setEntity(new UrlEncodedFormEntity(paramsList, encoding));
            try (CloseableHttpResponse response = httpclient.execute(post)) {
                statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        content = EntityUtils.toString(entity, encoding);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("invoke http post error!",e);
        } finally {
            post.releaseConnection();
        }
        return new SimpleHttpResponse(statusCode,content);
    }

    public static SimpleHttpResponse postJsonBody(String url, Map<String, String> headerMap,
                                      String paraData, String encoding) {

        logger.info("successfully  start post Json Body  url{} ", url);
        logger.info("successfully  start post Json Body {} ", paraData);
        HttpPost post = new HttpPost(url);
        int statusCode=502;
        String content="";
        try {
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectTimeout).setExpectContinueEnabled(false).build();
            StringEntity jsonEntity = new StringEntity(paraData, ContentType.APPLICATION_JSON);
            post.setConfig(requestConfig);
            post.setEntity(jsonEntity);
            try (CloseableHttpResponse response = httpclient.execute(post)) {
                statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        content = EntityUtils.toString(entity, encoding);

                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("connect to  http  rest api failed", e);
            throw new RuntimeException("postJsonBody error: "+e.getMessage());
        } finally {
            post.releaseConnection();
        }
        logger.info("successfully  end post Json Body  url{} ", url);
        return new SimpleHttpResponse(statusCode,content) ;
    }

    @SuppressWarnings("deprecation")
    public static SimpleHttpResponse invokeGet(String url,Map<String,String>headerMap, Map<String, String> params, String encode) {
        String responseString = null;
        int statusCode=502;
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();


        StringBuilder sb = new StringBuilder();
        sb.append(url);
        int i = 0;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (i == 0 && !url.contains("?")) {
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey());
                sb.append("=");
                String value = entry.getValue();
                try {
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("encode http get params error, value is " + value, e);
                    sb.append(URLEncoder.encode(value));
                }
                i++;
            }
        }
        String getPath = sb.toString();
        logger.info("successfully  start invoke get url {} ", getPath);
        HttpGet get = new HttpGet(getPath);
        get.setConfig(requestConfig);
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            CloseableHttpResponse response = httpclient.execute(get);
            try {
                statusCode =  response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        responseString = EntityUtils.toString(entity, encode);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (IOException e) {
            logger.error(String.format("connect to  http  rest api failed, url:%s", getPath), e);
            throw new RuntimeException("invokeGet error: "+e.getMessage());
        }  finally {
            get.releaseConnection();
        }
        return new SimpleHttpResponse(statusCode, responseString);
    }

    // 随机4位数
    public static String getRandomValue() {
        String str = "0123456789";
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            char ch = str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        return sb.toString();
    }

    // 当前时间到秒
    public static String getTimestamp() {

        Date date = new Date();
        String timestamp = String.valueOf(date.getTime() / 1000);
        return timestamp;
    }

    // 当前时间到秒
    public static String getNowDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }



    public static String executeGet(String url)
    {
        String rtnStr = "";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            //获得返回的结果
            rtnStr = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return rtnStr;
    }

}
