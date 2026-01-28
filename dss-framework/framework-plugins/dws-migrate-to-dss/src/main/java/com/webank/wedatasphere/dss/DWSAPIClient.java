package com.webank.wedatasphere.dss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.bean.DWSFlow;
import com.webank.wedatasphere.dss.bean.DWSProject;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.IoUtils;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.SimpleHttpResponse;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.util.HttpClientUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static  com.webank.wedatasphere.dss.GsonUtils.getString;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Author: xlinliu
 * Date: 2023/10/8
 */
public class DWSAPIClient {
    private static final Logger logger = LoggerFactory.getLogger(DWSAPIClient.class);
    public final static int connectTimeout = 5000;
    public final static int socketTimeout = 60 * 1000;

    private static CloseableHttpClient httpclient = HttpClients.createDefault();

    private static final String TREE_API_URI = "/api/rest_j/v1/wtss/tree";
    private static final String GET_API_URI = "/api/rest_j/v1/wtss/get";
    private static final String OPEN_SCRIPT_API_URI = "/api/rest_j/v1/filesystem/openScriptFromBML";
    private static final String DOWNLOAD_BML_API_URI = "/api/rest_j/v1/bml/download";
    private static final String ENCODING = "utf-8";

    private final String host;
    private final String cookie;

    public DWSAPIClient(String host, String cookie) {
        this.host = host;
        this.cookie = cookie;
    }

    public List<DWSProject> getAllProject() {
        String url = host + TREE_API_URI;
        Map<String, String> header = Collections.singletonMap("Cookie", cookie);

        SimpleHttpResponse reponse = HttpClientUtil.invokeGet(url, header, null, ENCODING);
        if (reponse.getStatusCode() != 200) {
            logger.error("get dws projects  failed. message:{}", reponse.getBody());
            throw new DSSRuntimeException("get dws projects  failed：" + reponse.getBody());
        }
        List<DWSProject> projects = new ArrayList<>();
        new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("data")
                .forEach(e -> {
                    Long projectTaxonomyID = e.getAsJsonObject().get("id").getAsLong();
                    e.getAsJsonObject().getAsJsonArray("dwsProjectList").forEach(
                            p -> {
                                JsonObject projectJson = p.getAsJsonObject();
                                Long projectVersionID = projectJson.getAsJsonObject("latestVersion").get("id").getAsLong();
                                String name = getString(projectJson,"name");
                                projects.add(new DWSProject(name, projectTaxonomyID, projectVersionID));
                            }
                    );
                });
        return projects;
    }

    public List<DWSFlow> getAllFlowMetaInProject(String projectName, Long projectTaxonomyID, Long projectVersionID, boolean isRootFlow) {
        String url = host + TREE_API_URI;
        Map<String, String> header = Collections.singletonMap("Cookie", cookie);
        Map<String, String> params = new HashMap<>(2);
        params.put("projectTaxonomyID", projectTaxonomyID.toString());
        params.put("projectVersionID", projectVersionID.toString());
        params.put("isRootFlow", isRootFlow ? "true" : "false");
        SimpleHttpResponse reponse = HttpClientUtil.invokeGet(url, header, params, ENCODING);
        if (reponse.getStatusCode() != 200) {
            logger.error("get dws flows meta in project {} failed. message:{}", projectName, reponse.getBody());
            throw new DSSRuntimeException("get dws flows meta in project" + projectName + "  failed：" + reponse.getBody());
        }
        List<DWSFlow> flows = new ArrayList<>();
        new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("data")
                .forEach(e -> {
                    e.getAsJsonObject().getAsJsonArray("dwsFlowList").forEach(
                            p -> {
                                JsonObject flowJson = p.getAsJsonObject();
                                Long id = flowJson.get("id").getAsLong();
                                String name =getString( flowJson,"name");
                                boolean rootFlow = flowJson.get("rootFlow").getAsBoolean();
                                String updator=getString(flowJson.getAsJsonObject("latestVersion"),"updator");

                                flows.add(new DWSFlow(id, projectVersionID, name, rootFlow,updator));
                            }
                    );
                });
        return flows;
    }

    public DWSFlow getFlowContent(DWSFlow flowMeta) {
        String url = host + GET_API_URI;
        Map<String, String> header = Collections.singletonMap("Cookie", cookie);
        Map<String, String> params = new HashMap<>(2);
        params.put("id", flowMeta.getId().toString());
        params.put("projectVersionID", flowMeta.getProjectVersionID().toString());
        SimpleHttpResponse reponse = HttpClientUtil.invokeGet(url, header, params, ENCODING);
        if (reponse.getStatusCode() != 200) {
            logger.error("get content of dws flow {} failed. message:{}", flowMeta.getName(), reponse.getBody());
            throw new DSSRuntimeException("get content of dws flow" + flowMeta.getName() + "  failed：" + reponse.getBody());
        }
        JsonObject jsonObject=new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("flow");
        String flowJson =getString(jsonObject.getAsJsonObject("latestVersion"),"json");
        DWSFlow flow = new DWSFlow(flowMeta.getId(), flowMeta.getProjectVersionID(), flowMeta.getName(), flowMeta.isRootFlow(),flowMeta.getUpdator());
        flow.setJsonObject(jsonObject);
        flow.setJson(flowJson);
        return flow;
    }

    public String openScriptFromBML(String fileName, String resourceId, String version) {
        String url = host + OPEN_SCRIPT_API_URI;
        Map<String, String> header = Collections.singletonMap("Cookie", cookie);
        Map<String, String> params = new HashMap<>(2);
        params.put("fileName", fileName);
        params.put("resourceId", resourceId);
        params.put("version", version);
        SimpleHttpResponse reponse = HttpClientUtil.invokeGet(url, header, params, ENCODING);
        if (reponse.getStatusCode() != 200) {
            logger.error("get script file  {} failed. message:{}", fileName, reponse.getBody());
            throw new DSSRuntimeException("get script file" + fileName + "  failed：" + reponse.getBody());
        }
        String scriptContent = getString( new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data"),"scriptContent");
        return scriptContent;
    }

    public String downloadBmlToLocalPath( String resourceId, String version, String path) {
        String url = host + DOWNLOAD_BML_API_URI;
        Map<String, String> header = Collections.singletonMap("Cookie", cookie);
        Map<String, String> params = new HashMap<>(2);
        params.put("resourceId", resourceId);
        params.put("version", version);
        return invokeGetAndWriteToLocalPath(url,header,params,path);
    }

    /**
     * 调用get请求并把返回结果写到本地磁盘的制定位置。一般用于请求二进制资源如文件。
     */
    private static String invokeGetAndWriteToLocalPath(String url, Map<String, String> headerMap, Map<String, String> params, String path) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout)
                .setConnectTimeout(socketTimeout).setConnectionRequestTimeout(connectTimeout).build();

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

        try (CloseableHttpResponse response = httpclient.execute(get)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                logger.error("get entity failed,entity is null, url:{}", getPath);
                throw new RuntimeException("get entity failed,entity is null, url"+getPath);
            }
            try (InputStream is = entity.getContent(); OutputStream os = IoUtils.generateExportOutputStream(path)) {
                IOUtils.copy(is, os);
                return path;
            }
        } catch (IOException e) {
            logger.error(String.format("connect to  http  rest api failed, url:%s", getPath), e);
            throw new RuntimeException("invokeGet error: " + e.getMessage());
        } finally {
            get.releaseConnection();
        }
    }
}
