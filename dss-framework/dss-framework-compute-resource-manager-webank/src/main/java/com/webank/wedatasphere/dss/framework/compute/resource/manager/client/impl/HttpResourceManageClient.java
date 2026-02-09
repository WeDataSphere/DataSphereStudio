package com.webank.wedatasphere.dss.framework.compute.resource.manager.client.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.conf.LinkisConnConf;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.ConfigurationTemplate;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ApplyECConfTemplateRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.BatchQueueInfoRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceKillRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.UpdateKeyMappingRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.*;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.util.HttpClientUtil;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.vo.QueueInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;


/**
 * 资源管理http实现
 * Author: xlinliu
 * Date: 2022/11/25
 */
@Service
public class HttpResourceManageClient implements ResourceManageClient {
    private final static Logger logger = LoggerFactory.getLogger(HttpResourceManageClient.class);
    @Override
    public List<ECInstance> fetchECInstance(ECInstanceRequest ecInstanceRequest,String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY, operateUser);
        List<String> ecInstanceNames=ecInstanceRequest.getEcInstanceNames();
        List<String> creators= ecInstanceRequest.getCreateUser();
        List<String> engineTypes=ecInstanceRequest.getEngineType();
        List<String> statuses=ecInstanceRequest.getStatus();
        String queueName = ecInstanceRequest.getYarnQueue();
        String isCrossCluster = String.valueOf(ecInstanceRequest.getCrossCluster());
        Map<String, Object> body = new HashMap<>();
        body.put("ecInstances", ecInstanceNames);
        body.put("creators",creators);
        body.put("engineTypes",engineTypes);
        body.put("statuss",statuses);
        body.put("queueName", queueName);
        body.put("crossCluster", isCrossCluster);
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(body);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(FETCH_ECINSTANCE_PATH,headers,bodyParam,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("get ec instance list failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get get ec instance list failed：" + reponse.getBody());
        }
        ECInstanceHttpResponse message=DSSCommonUtils.COMMON_GSON.fromJson(reponse.getBody(),
                ECInstanceHttpResponse.class);
        return Optional.ofNullable(message)
                .map(ECInstanceHttpResponse::getData)
                .map(ECInstanceHttpResponse.Data::getEcList)
                .orElse(Collections.emptyList());

    }

    @Override
    public void batchKillECInstance(List<ECInstanceKillRequest> killRequest,String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operateUser);
        List<String> instanceNames=killRequest.stream().map(ECInstanceKillRequest::getEngineInstance).collect(Collectors.toList());
        Map<String, List<String>> map = Collections.singletonMap("instances", instanceNames);
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(map);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(KILL_ECINSTANCE_PATH, headers,bodyParam,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("kill ec instance failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("kill instance failed：" + reponse.getBody());
        }
    }

    @Override
    public boolean testUserInitiated(String userName) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,userName);
        Map<String,String> req=Collections.singletonMap("pathType","file");
        SimpleHttpResponse reponse= HttpClientUtil.invokeGet(TEST_USER_INITIATED_PATH,headers,req,"utf-8");

        return reponse.getStatusCode()==200;
    }

    @Override
    public QueueInfo getQueueInfo(String queueName,boolean isCrossCluster,String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operateUser);
        Map<String, String> map = new HashMap<>(2);
        map.put("clustername", "default");
        map.put("queuename",queueName);
        map.put("crossCluster",  String.valueOf(isCrossCluster));
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(map);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(QUERY_QUEUE_INFO,headers,bodyParam,"utf-8");

        if(reponse.getStatusCode()!=200){
            logger.error("get queue info failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get queue info failed：" + reponse.getBody());
        }
        JsonObject obj= new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("queueInfo");
        return DSSCommonUtils.COMMON_GSON.fromJson(obj, QueueInfo.class);
    }

    @Override
    public Map<String, QueueInfo> batchGetQueueInfo(BatchQueueInfoRequest request, String operateUser) {
        Map<String, QueueInfo> result = new HashMap<>();
        if (CollectionUtils.isEmpty(request.getQueueNames())) {
            return result;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY, operateUser);
        Map<String, Object> map = new HashMap<>();
        map.put("clustername", "default");
        map.put("queueNames", request.getQueueNames());
        map.put("crossCluster", String.valueOf(request.getCrossCluster()));
        String bodyParam = DSSCommonUtils.COMMON_GSON.toJson(map);
        SimpleHttpResponse response = HttpClientUtil.postJsonBody(BATCH_QUERY_QUEUE_INFO, headers, bodyParam, "utf-8");

        if (response.getStatusCode() != 200) {
            logger.error("batch get queue info failed. message:{}", response.getBody());
            throw new DSSRuntimeException("batch get queue info failed：" + response.getBody());
        }
        JsonObject dataObj = new JsonParser().parse(response.getBody()).getAsJsonObject()
                .getAsJsonObject("data");
        if (dataObj != null && dataObj.has("queueInfos")) {
            JsonArray queueInfoArray = dataObj.getAsJsonArray("queueInfos");
            for (JsonElement element : queueInfoArray) {
                QueueInfo queueInfo = DSSCommonUtils.COMMON_GSON.fromJson(element, QueueInfo.class);
                if (queueInfo != null && queueInfo.getQueuename() != null) {
                    String queueName = queueInfo.getQueuename().getQueueName();
                    result.put(queueName, queueInfo);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getEngineTypes(String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operateUser);
        SimpleHttpResponse reponse= HttpClientUtil.invokeGet(CONF_ALL_ENGINE_TYPE,headers,null,"utf-8");

        if(reponse.getStatusCode()!=200){
            logger.error("get engine type info failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get engine type info failed：" + reponse.getBody());
        }
        JsonArray obj= new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("engineType");
        return DSSCommonUtils.COMMON_GSON.fromJson(obj,new TypeToken<List<String>>(){}.getType());
    }

    @Override
    public List<CategoryLabel> getApplicationEngineTypeCategory(String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operateUser);
        SimpleHttpResponse reponse= HttpClientUtil.invokeGet(CONF_CATEGORY_PATH,headers,null,"utf-8");

        if(reponse.getStatusCode()!=200){
            logger.error("get application engine type category info failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get application engine type info failed：" + reponse.getBody());
        }
        JsonArray obj= new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("Category");
        return DSSCommonUtils.COMMON_GSON.fromJson(obj, new TypeToken<List<CategoryLabel>>(){}.getType());
    }

    @Override
    public void saveECConfTemplate(UpdateKeyMappingRequest template) {
        if (!CollectionUtils.isEmpty(template.getItemList())) {
            //把空值过滤掉，避免空值清空linkis管理台已有的值。
            List<UpdateKeyMappingRequest.TemplateConfigKey> keyList = template.getItemList().stream().
                    filter(e -> !StringUtils.isEmpty(e.getConfigValue()))
                    .collect(Collectors.toList());
            template.setItemList(keyList);
        }

        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,template.getOperator());
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(template);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(CONF_TEMPLATE_ADD_PATH,headers,bodyParam,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("add configuration template  failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("add configuration template failed：" + reponse.getBody());
        }
    }

    @Override
    public ConfigurationTemplate getECConfTemplate(String templateId,String operator) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operator);
        Map<String, List<String>> bodyMap = Collections.singletonMap("templateUidList", Collections.singletonList(templateId));
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(bodyMap);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(CONF_TEMPLATE_QUERY_PATH,headers,bodyParam,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("get configuration template  failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get configuration template failed：" + reponse.getBody());
        }
        JsonArray jsonArray = new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("list");
        if (jsonArray == null || jsonArray.size() < 1) {
            return null;
        }
        return DSSCommonUtils.COMMON_GSON.fromJson(jsonArray.get(0), new TypeToken<ConfigurationTemplate>(){}.getType());
    }

    @Override
    public List<ConfigurationTemplate.Item> getTemplateParamConf(String engineType, String operateUser) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,operateUser);
        Map<String,String> req=Collections.singletonMap("engineType",engineType);
        SimpleHttpResponse reponse= HttpClientUtil.invokeGet(CONF_UI_QUERY_PATH,headers,req,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("get configuration item list  failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get configuration item list failed：" + reponse.getBody());
        }
        JsonArray jsonArray = new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonArray("itemList");
        if (jsonArray == null || jsonArray.size() < 1) {
            return Collections.emptyList();
        }
        return DSSCommonUtils.COMMON_GSON.fromJson(jsonArray, new TypeToken<List<ConfigurationTemplate.Item>>(){}.getType());
    }

    @Override
    public ApplyECConfTemplateResponse applyECConfTemplate(ApplyECConfTemplateRequest request) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,request.getOperator());
        String bodyParam= DSSCommonUtils.COMMON_GSON.toJson(request);
        SimpleHttpResponse reponse= HttpClientUtil.postJsonBody(CONF_APPLY_TEMPLATE_PATH,headers,bodyParam,"utf-8");
        if(reponse.getStatusCode()!=200){
            logger.error("get configuration template  failed. message:{}",reponse.getBody());
            throw new DSSRuntimeException("get configuration template failed：" + reponse.getBody());
        }
        JsonObject obj = new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                .getAsJsonObject("data");
        return DSSCommonUtils.COMMON_GSON.fromJson(obj, ApplyECConfTemplateResponse.class);

    }

    @Override
    public GovernanceStationAdminResponse governanceStationAdmin(String userName) {
        Map<String,String> headers=new HashMap<>();
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_KEY, LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_VALUE);
        headers.put(LinkisConnConf.LINKIS_RESOURCE_ADMIN_TOKEN_USER_KEY,userName);
        SimpleHttpResponse response = HttpClientUtil.invokeGet(CONF_GOVERNANCE_ADMIN_PATH,headers,null,"utf-8");
        if(response.getStatusCode()!=200){
            logger.error("check admin user failed. message:{}",response.getBody());
            throw new DSSRuntimeException("check admin user failed：" + response.getBody());
        }
        return DSSCommonUtils.COMMON_GSON.fromJson(response.getBody(), GovernanceStationAdminResponse.class);
    }

    @Override
    public boolean checkSuperAdmin(String userName) {
        GovernanceStationAdminResponse governanceStationAdminResponse = governanceStationAdmin(userName);
        Boolean admin = governanceStationAdminResponse.getData().getAdmin();
        if(admin != null && admin){
            return true;
        }
        return false;
    }
}
