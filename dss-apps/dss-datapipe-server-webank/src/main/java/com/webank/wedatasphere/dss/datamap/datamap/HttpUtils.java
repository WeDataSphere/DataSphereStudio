package com.webank.wedatasphere.dss.datamap.datamap;


import com.webank.wedatasphere.dss.datamap.conf.DataMapConnConf;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: jinyangrao on 2020/11/05
 */
public class HttpUtils {

    private static RequestConfig requestConfig = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    static {
        // set http connection status
        requestConfig = RequestConfig.custom()
                .setSocketTimeout(DataMapConnConf.HTTP_SOCKET_TIMEOUT())
                .setConnectTimeout(DataMapConnConf.HTTP_CONN_TIMEOUT())
                .setConnectionRequestTimeout(DataMapConnConf.HTTP_CONN_REQUEST_TIMEOUT())
                .build();
    }

    public static CloseableHttpClient getConnection() {
        return HttpClientBuilder.create().build();
    }

    // gen uri
    public static URI getUri(Map<String, String> param, String schema, String host, int port, String path) {
        List<NameValuePair> requestParam = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = param.entrySet();
        for(Map.Entry<String, String> item: entries) {
            String key = item.getKey();
            String value = item.getValue();
            NameValuePair pair = new BasicNameValuePair(key, value);
            requestParam.add(pair);
        }
        URI uri = null;
        try {
            uri = new URIBuilder().setParameters(requestParam).setScheme(schema)
                                  .setHost(host).setPort(port)
                                  .setPath(path).build();
        } catch (URISyntaxException e) {
            LOGGER.info("Failed to get url ", e);
        }
        return uri;
    }

    // get request method
    public static HttpRequestBase getRequestMethod(String method, URI uri, Map<String, String> headerParam) {
        HttpRequestBase requestMethod = null;
        if("post".equalsIgnoreCase(method)) {
            requestMethod = new HttpPost();
        } else if("get".equalsIgnoreCase(method)) {
            requestMethod = new HttpGet();
        }
        if (requestMethod != null){
            requestMethod.setURI(uri);
            requestMethod.setConfig(requestConfig);
            for(Map.Entry<String, String> item: headerParam.entrySet()) {
                requestMethod.setHeader(item.getKey(), item.getValue());
            }
        }
        return requestMethod;
    }
}