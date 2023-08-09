package com.webank.wedatasphere.dss.common.server.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class HttpClientUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    public final static int connectTimeout = 5000;

    public final static String encoding = "UTF-8";

    private static final CloseableHttpClient httpclient;

    static {
        httpclient = HttpClients.createDefault();
    }

    public static void postJsonBody(String url, Map<String, String> headerMap, String paraData) {
        logger.info("Successfully start post Json Body url {} ", url);
        HttpPost post = new HttpPost(url);
        String content = "";
        int statusCode;
        try {
            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            post.setHeader("Content-type", "application/json");
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectTimeout).setExpectContinueEnabled(false).build();
            StringEntity jsonEntity = new StringEntity(paraData, ContentType.APPLICATION_JSON);
            post.setConfig(requestConfig);
            post.setEntity(jsonEntity);
            logger.info("请求的参数为{}, jsonEntity {}", post, jsonEntity);
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
            logger.error("Connect to http rest api failed", e);
            throw new RuntimeException("postJsonBody error: " + e.getMessage());
        } finally {
            post.releaseConnection();
        }
        logger.info("Successfully end post Json Body url {}, the response status code is {} and content is {}", url, statusCode, content);
    }



    public static void postForm(String url, Map<String, Object> header, List<NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        String content = "";
        int statusCode;
        try {
            if (header != null) {
                for (Map.Entry<String, Object> entry : header.entrySet()) {
                    post.setHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(connectTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectTimeout)
                    .setExpectContinueEnabled(false).build();
            post.setConfig(requestConfig);
            post.setEntity(new UrlEncodedFormEntity(params, encoding));
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
            logger.error("Connect to http rest api failed", e);
            throw new RuntimeException("invoke ims http post error!", e);
        } finally {
            post.releaseConnection();
        }
        logger.info("Successfully end post Json Body url {}, the response status code is {} and content is {}", url, statusCode, content);
    }
}
