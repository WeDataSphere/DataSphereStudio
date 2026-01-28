package com.webank.wedatasphere.dss.datamap.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.util.Objects;

@Component
public class OpenApiClient {

    private String sslKeyFileName = "/appcom/config/dss-config/dev/webank/webank_keystore.jks";

    private String sslTrustFileName = "/appcom/config/dss-config/dev/webank/webank_truststore.jks";

    private String keyStorePass = "Abcd1234";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public CloseableHttpClient getOpenApiHttpClient() {
        CloseableHttpClient httpClient = null;
        int connMaxTotal = 200;
        int connDefaultMaxPerRoute = 5;
        int validateInactivityMillSeconds = 1000;
        int connEvictIdleConnectionsTimeoutMillSeconds = 3000;
        File f;
        try {
            f = getResourceAsFile(sslKeyFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long size = f.length();
        FileInputStream fin = null;
        ByteBuffer kbb = ByteBuffer.allocate((int) size);
        populateByteBuffer(f, kbb);
        try {
            f = getResourceAsFile(sslTrustFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        size = f.length();
        ByteBuffer tbb = ByteBuffer.allocate((int) size);
        populateByteBuffer(f, tbb);
        byte[] keyStoreBytes = kbb.array();
        byte[] trustStoreBytes = tbb.array();

        try (InputStream keyStoreInput = new ByteArrayInputStream(keyStoreBytes); InputStream trustStoreInput = new ByteArrayInputStream(trustStoreBytes)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInput, keyStorePass.toCharArray());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStoreInput, null);

            httpClient = HttpClientUtils.createHttpClientWithCert(keyStore, keyStorePass, trustStore, connMaxTotal, connDefaultMaxPerRoute, validateInactivityMillSeconds, connEvictIdleConnectionsTimeoutMillSeconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return httpClient;
    }

    private static void populateByteBuffer(File f, ByteBuffer bb) {

        try (FileInputStream fin = new FileInputStream(f)) {
            FileChannel channel = fin.getChannel();
            channel.read(bb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private File getResourceAsFile(String name) {

//        ApplicationHome applicationHome = new ApplicationHome(OpenApiClient.class);
//        String rootPath = applicationHome.getSource().getParentFile().toString();
//        String filePath = rootPath + "/" +  name;
        File file = new File(name);

        if (!file.exists()) {
            try {
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(name);
                FileUtils.copyInputStreamToFile(Objects.requireNonNull(in, "文件找不到"), file);
            } catch (IOException e) {

                throw new IllegalArgumentException("获取失败" + ExceptionUtils.getStackTrace(e));

            }
        }
        URI uri = file.toURI();
        return new File(uri);

    }
}

