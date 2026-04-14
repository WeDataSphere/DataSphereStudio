package com.webank.wedatasphere.dss.datamap.datamap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.datamap.conf.DSSDataPipeConfiguration;
import com.webank.wedatasphere.dss.datamap.conf.DataMapConnConf;
import com.webank.wedatasphere.dss.datamap.datamap.transferor.TablesOwnerTransferor;
import com.webank.wedatasphere.dss.datamap.datamap.transferor.TransferContext;
import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;
import com.webank.wedatasphere.dss.datamap.domain.vo.*;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 库表信息来源
 * tbOwner : fsimage
 * tableSize: fsimage
 * viewTime(访问时间): report/portal
 * 其他信息：datashapis
 * @author: jinyangrao on 2020/11/05
 */
@Component
public class DataMapDataSource {

    final static private Logger LOG = LoggerFactory.getLogger(DataMapDataSource.class);
    private static final Pattern datacheckerPattern = Pattern.compile("hive:///.+/(.+)/(.+)");
    private static final String defaultPartition = "ds=${run_date}";
    private String ipAddress = DataMapConnConf.DATAMAP_IP_ADDRESS();
    private Integer port = DataMapConnConf.DATAMAP_IP_PORT();
    private String basePath = DataMapConnConf.DATAMAP_REQUEST_BASE_PATH();

    // uat / prod
    private String clusterEnv = DataMapConnConf.DATAMAP_ENV();
    // BDAP-SIT
    private String clusterType = DataMapConnConf.CLUSTER_TYPE();

    private String systemUsername = DataMapConnConf.DATAMAP_SYSTEM_USERNAME();
    private String appId = DataMapConnConf.DATAMAP_APPID();
    private String token = DataMapConnConf.DATAMAP_TOKEN();

    private String requestSchema = "http";

    private int randomStart = 10000;
    private int randomEnd = 99999;

    private String schemaBaseInfoMethod = DataMapConnConf.DATAMAP_REQUEST_SCHEMAINFO_METHOD();
    private String schemaBaseInfoParam = DataMapConnConf.DATAMAP_REQUEST_SCHEMAINFO_PARAM();
    private String schemaBaseInfoPath = DataMapConnConf.DATAMAP_REQUEST_SCHEMAINFO_PATH();

    private String tablesmetadataMethod = DataMapConnConf.DATAMAP_REQUEST_TABLESMETADATA_METHOD();
    private String tablesmetadataParam = DataMapConnConf.DATAMAP_REQUEST_TABLESMETADATA_PARAM();
    private String tablesmetadataPath = DataMapConnConf.DATAMAP_REQUEST_TABLESMETADATA_PATH();

    private String tablesnameMethod = DataMapConnConf.DATAMAP_REQUEST_TABALESNAME_METHOD();
    private String tablesnameParam = DataMapConnConf.DATAMAP_REQUEST_TABALESNAME_PARAM();
    private String tablesnamePath = DataMapConnConf.DATAMAP_REQUEST_TABALESNAME_PATH();

    private String nebulaBasePath = DataMapConnConf.NEBULA_REQUEST_BASE_PATH();
    private String spaceInfoPath = DataMapConnConf.NEBULA_REQUEST_SPACE_PATH();
    private String spaceInfoMethod = DataMapConnConf.NEBULA_REQUEST_SPACE_METHOD();
    private String spaceInfoParam = DataMapConnConf.NEBULA_REQUEST_SPACE_PARAM();
    private String tagsInfoPath = DataMapConnConf.NEBULA_REQUEST_TAGS_PATH();
    private String tagsInfoMethod = DataMapConnConf.NEBULA_REQUEST_TAGS_METHOD();
    private String tagsInfoParam = DataMapConnConf.NEBULA_REQUEST_TAGS_PARAM();
    private String tagPropInfoPath = DataMapConnConf.NEBULA_REQUEST_TAG_PROP_PATH();
    private String tagPropInfoMethod = DataMapConnConf.NEBULA_REQUEST_TAG_PROP_METHOD();
    private String tagPropInfoParam = DataMapConnConf.NEBULA_REQUEST_TAG_PROP_PARAM();
    private String edgesInfoPath = DataMapConnConf.NEBULA_REQUEST_EDGES_PATH();
    private String edgesInfoMethod = DataMapConnConf.NEBULA_REQUEST_EDGES_METHOD();
    private String edgesInfoParam = DataMapConnConf.NEBULA_REQUEST_EDGES_PARAM();
    private String edgePropInfoPath = DataMapConnConf.NEBULA_REQUEST_EDGE_PROP_PATH();
    private String edgePropInfoMethod = DataMapConnConf.NEBULA_REQUEST_EDGE_PROP_METHOD();
    private String edgePropInfoParam = DataMapConnConf.NEBULA_REQUEST_EDGE_PROP_PARAM();
    private String tagIndexPath = DataMapConnConf.NEBULA_REQUEST_TAG_INDEX_PATH();
    private String tagIndexMethod = DataMapConnConf.NEBULA_REQUEST_TAG_INDEX_METHOD();
    private String tagIndexParam = DataMapConnConf.NEBULA_REQUEST_TAG_INDEX_PARAM();
    private String edgeIndexPath = DataMapConnConf.NEBULA_REQUEST_EDGE_INDEX_PATH();
    private String edgeIndexMethod = DataMapConnConf.NEBULA_REQUEST_EDGE_INDEX_METHOD();
    private String edgeIndexParam = DataMapConnConf.NEBULA_REQUEST_EDGE_INDEX_PARAM();
    private String permissionPath = DataMapConnConf.NEBULA_REQUEST_PERMISSION_PATH();
    private String permissionMethod = DataMapConnConf.NEBULA_REQUEST_PERMISSION_METHOD();
    private String permissionParam = DataMapConnConf.NEBULA_REQUEST_PERMISSION_PARAM();
    private String codeMetaMethod = DataMapConnConf.DMS_REQUEST_CODE_META_METHOD();
    private String codeMetaPath = DataMapConnConf.DMS_REQUEST_CODE_META_PATH();
    private String enterpriseSecureUserListMethod = DataMapConnConf.DMS_REQUEST_ENTERPRISE_SECURE_USER_LIST_METHOD();
    private String enterpriseSecureUserListPath = DataMapConnConf.DMS_REQUEST_ENTERPRISE_SECURE_USER_LIST_PATH();
    private String userNeedScanMethod = DataMapConnConf.DMS_REQUEST_USER_NEED_SCAN_METHOD();
    private String userNeedScanPath = DataMapConnConf.DMS_REQUEST_USER_NEED_SCAN_PATH();
    private String deductSensitiveDataQuotaMethod = DataMapConnConf.DMS_REQUEST_DEDUCT_SENSITIVE_DATA_QUOTA_METHOD();
    private String deductSensitiveDataQuotaPath = DataMapConnConf.DMS_REQUEST_DEDUCT_SENSITIVE_DATA_QUOTA_PATH();
    private String validateTableMethod = DataMapConnConf.DMS_REQUEST_VALIDATE_TABLE_METHOD();
    private String validateTablePath = DataMapConnConf.DMS_REQUEST_VALIDATE_TABLE_PATH();
    private String tableMaskMethod = DataMapConnConf.DMS_REQUEST_TABLE_MASK_METHOD();
    private String detailDatasetMaskInfoPath = DataMapConnConf.DMS_REQUEST_TABLE_MASK_INFO_PATH();
    private String columnMaskInfoListPath = DataMapConnConf.DMS_REQUEST_MASK_COLUMN_LIST_PATH();
    private String bdpTableDetailPath = DataMapConnConf.DMS_REQUEST_BDP_TABLE_DETAIL_PATH();


    /**
     * use http to get db info
     */
    public DMSchemaBaseInfoBean getSchemaBaseInfo(String dbName, String loginUser) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(schemaBaseInfoParam);
        param.put("dbCode", dbName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse(param, schemaBaseInfoMethod, schemaBaseInfoPath, loginUser, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        DMSchemaBaseInfoBean dmSchemaBaseInfoBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmSchemaBaseInfoBean = BDPJettyServerHelper.gson().fromJson(data, DMSchemaBaseInfoBean.class);
        }
        return dmSchemaBaseInfoBean;
    }

    /**
     * use http to get table metadata info
     */
    public DMToTableMetaDataInfoVo getTablesMetaDataInfo(String dbName, Integer isTableOwner,
                                                         String tableName, Integer orderBy,
                                                         int currentPage, int pageSize,
                                                         String loginUser,String tableOwner,
                                                         Long accessStartTime,Long accessEndTime) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(tablesmetadataParam);
        param.put("dbCode", dbName);
        param.put("currentPage", Integer.toString(currentPage));
        param.put("pageSize", Integer.toString(pageSize));
        param.put("order", orderBy.toString());
        param.put("datasetName", tableName);
        //是否实时拉取表信息。如果为true，datashapis会实时拉取元数据，数据实时性高，但是性能较差
        // param.put("realTime",String.valueOf(realTime) );
        // 0表示默认值我有权限的表，1表示我创建的表
        // 需要把他转换为Boolean类型字符串传输
        Boolean create = isTableOwner == 1;
        param.put("create", create.toString());
        // 添加属主查询
        param.put("tableOwner",tableOwner);

        if(accessStartTime != null){
            param.put("accessStartTime", Long.toString(accessStartTime));
        }

        if(accessEndTime != null){
            param.put("accessEndTime", Long.toString(accessEndTime));
        }

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse(param, tablesmetadataMethod, tablesmetadataPath, loginUser, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        Integer totalCount = null;
        List<TableMetaDataInfoVo> tableMetaDataInfoVos = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
            tableMetaDataInfoVos = new ArrayList<>();
        } else {
            JsonElement totalCountJsonElement = data.getAsJsonObject().get("totalCount");

            if (!totalCountJsonElement.isJsonNull()) {
                totalCount = totalCountJsonElement.getAsInt();
                JsonArray tableMetadataArray = data.getAsJsonObject().getAsJsonArray("content");
                if (!tableMetadataArray.isJsonNull()) {
                    tableMetaDataInfoVos = BDPJettyServerHelper.gson().fromJson(tableMetadataArray,
                            new TypeToken<List<TableMetaDataInfoVo>>() {
                            }.getType());
                }
            } else {
                tableMetaDataInfoVos = new ArrayList<>();
            }
        }
        DMToTableMetaDataInfoVo dmToTableMetaDataInfoVo = new DMToTableMetaDataInfoVo();
        dmToTableMetaDataInfoVo.setTotalCount(totalCount);
        dmToTableMetaDataInfoVo.setTableMetaDataInfoVos(tableMetaDataInfoVos);
        return dmToTableMetaDataInfoVo;
    }

    /**
     * get tables name
     * order:
     * 0 sort by default
     * 1 sort by size fo table
     * 2 sort by create time
     * 3 sort by visit time
     */
    public List<String> getTablesName(String dbName, String tableName, Integer isTableOwner, Integer orderBy,
                                      String loginUser,String tableOwner,Long accessStartTime,Long accessEndTime) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(tablesnameParam);
        param.put("dbCode", dbName);
        param.put("order", Integer.toString(orderBy));
        param.put("datasetName", tableName);
        // param.put("realTime",String.valueOf(isRealTime) );

        // Because DM implements paging by default, set the page size to 0 to cancel paging
        param.put("currentPage", "1");
        param.put("pageSize", String.valueOf(Integer.MAX_VALUE));

        // 0表示默认值我有权限的表，1表示我创建的表
        // 需要把他转换为Boolean类型字符串传输
        Boolean create = isTableOwner == 1;
        param.put("create", create.toString());
        // 根据属主查询
        param.put("tableOwner",tableOwner);

        if(accessStartTime != null){
            param.put("accessStartTime", Long.toString(accessStartTime));
        }

        if(accessEndTime != null){
            param.put("accessEndTime", Long.toString(accessEndTime));
        }

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse(param, tablesnameMethod, tablesnamePath, loginUser, headerParam);
        List<String> tablesName = null;

        JsonElement data = preGetJsonData(contentStr);
        if (null == data || data.isJsonNull() || data.getAsJsonObject().keySet().isEmpty()){
            LOG.warn("The data in the datamap return message body is null.");
            tablesName = new ArrayList<>();
        } else {
            JsonArray tablesNameJson = data.getAsJsonObject().getAsJsonArray("content");
            if (!((null == tablesNameJson) || (tablesNameJson.isJsonNull()))) {
                tablesName = BDPJettyServerHelper.gson().fromJson(tablesNameJson,
                        new TypeToken<List<String>>() {
                        }.getType());
            } else {
                tablesName = new ArrayList<String>();
            }
        }
        return tablesName;
    }

    public Integer transferTablesOwner(TransferTablesOwnerRequest transferTablesOwnerRequest, String username) throws Exception {

        TablesOwnerTransferor transferor = DSSDataPipeConfiguration.getTransferor();
        TransferContext transferContext = new TransferContext(transferor);
        return transferContext.executeStrategy(transferTablesOwnerRequest, username);

    }

    private String requestAndResponse(Map<String, String> param, String requestMethod,
                                      String path, String loginUser, Map<String, String> headerParam) throws DataMapException{
        return requestAndResponse(param, requestMethod, path, loginUser, headerParam, null);
    }
    private String requestAndResponse(Map<String, String> param, String requestMethod,
                                      String path, String loginUser, Map<String, String> headerParam,String json) throws DataMapException {
        prepareParam(param, loginUser);
        URI uri = HttpUtils.getUri(param, requestSchema, ipAddress, port, basePath + path);
        HttpRequestBase request = HttpUtils.getRequestMethod(requestMethod, uri, headerParam);
        if(json!=null&&request instanceof HttpPost){
            StringEntity entity = new StringEntity(json,ContentType.APPLICATION_JSON);
            ((HttpPost)request).setEntity(entity);
        }
        CloseableHttpClient connection = HttpUtils.getConnection();
        CloseableHttpResponse response = null;
        HttpEntity content = null;
        String contentStr = null;
        try {
            LOG.info("datashapis request url:{}",request.getURI());
            response = connection.execute(request);
            LOG.info("datashapis Http Connection response code is: {}", response.getStatusLine().getStatusCode());
            content = response.getEntity();
            contentStr = EntityUtils.toString(content);
            int code = response.getStatusLine().getStatusCode();
            if (200 != code) {
                LOG.error("datashapis Http Connection response error. code: {},content:{}",
                        code,contentStr);
                throw new DataMapException("datashapis Http Connection response error code. [Http访问datashapis异常]",
                        code);
            }
            connection.close();
        } catch (IOException e) {
            LOG.error("There is an error in the connection with DMS." +
                    " It is possible that the DMS service is abnormal.[DataShapis服务异常]",e);
            throw new DataMapException("There is an error in the connection with DMS." +
                    " It is possible that the DMS service is abnormal.[DataShapis服务异常]");
        }
        return contentStr;
    }

    private String requestAndResponse4Nebula(Map<String, String> param, String requestMethod,
                                      String path, String loginUser, Map<String, String> headerParam) throws DataMapException {
        prepareParam(param, loginUser);
        URI uri = HttpUtils.getUri(param, requestSchema, ipAddress, port, nebulaBasePath + path);
        HttpRequestBase request = HttpUtils.getRequestMethod(requestMethod, uri, headerParam);
        CloseableHttpClient connection = HttpUtils.getConnection();
        CloseableHttpResponse response = null;
        HttpEntity content = null;
        String contentStr = null;
        try {
            LOG.info("datashapis request url:{}",request.getURI());
            response = connection.execute(request);
            LOG.info("datashapis Http Connection response code is: {}", response.getStatusLine().getStatusCode());
            if (200 == response.getStatusLine().getStatusCode()) {
                content = response.getEntity();
            } else {
                LOG.error("datashapis Http Connection response error code: {}", response.getStatusLine().getStatusCode());
                throw new DataMapException("datashapis Http Connection response error code. [Http访问datashapis异常]",
                        response.getStatusLine().getStatusCode());
            }
            contentStr = EntityUtils.toString(content);
            connection.close();
        } catch (IOException e) {
            LOG.error("There is an error in the connection with DMS." +
                    " It is possible that the DMS service is abnormal.[DataShapis服务异常]",e);
            throw new DataMapException("There is an error in the connection with DMS." +
                    " It is possible that the DMS service is abnormal.[DataShapis服务异常]");
        }
        return contentStr;
    }

    private JsonElement preGetJsonData(String contentStr) throws DataMapException {
        if (StringUtils.isEmpty(contentStr)) {
            LOG.warn("The message body returned by DM is empty.[DataMap返回数据为空]");
            throw new DataMapException("[DataShapis服务异常] The message body returned by DM is empty.[DataMap返回数据为空]");
        }
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(contentStr).getAsJsonObject();
        int rtnCode = jsonObject.get("code").getAsInt();

        String rtnMsg = jsonObject.get("msg").getAsString();
        JsonElement data;
        if (rtnCode == 200) {
            data = jsonObject.get("data");
        } else {
            String logId=jsonObject.has("loggerId")?jsonObject.get("loggerId").getAsString():"0";
            LOG.warn("DM corresponding to abnormal status code: {}, logId:[{}], and msg: {}", rtnCode, logId,rtnMsg);
            throw new DataMapException(String.format("[DataShapis服务异常] loggerId is [%s]. error message is %s ", logId,
                    rtnMsg));
        }
        return data;
    }


    /**
     * generate request param for datamap
     */
    private void prepareParam(Map<String, String> param, String loginUser) {
        param.put("appid", appId);
        String randomNum = genRandomNum(randomStart, randomEnd);
        param.put("nonce", randomNum);
        String timestamp = Long.toString(System.currentTimeMillis());
        param.put("timestamp", timestamp);
        param.put("loginUser", loginUser);
        String signature = genSignature(appId, token, randomNum, timestamp, loginUser);
        param.put("signature", signature);
        param.put("isolateEnvFlag", clusterEnv);
        param.put("clusterType", clusterType);
    }

    /**
     * random utils
     */
    private String genRandomNum(int min, int max) {
        int randomNum = new Random().nextInt(max) % (max - min + 1) + min;
        return Integer.toString(randomNum);
    }

    /**
     * parse user config to map
     */
    private Map<String, String> getConfigParam(String paramJson) {
        Map<String, String> configParam = null;
        configParam = BDPJettyServerHelper.gson().fromJson(paramJson,
                new TypeToken<Map<String, String>>() {
                }.getType());
        if (null == configParam) {
            configParam = new HashMap<>();
        }
        return configParam;
    }

    /**
     * datamap usesignature for authentication
     */
    private String genSignature(String appId, String appToKen, String randomNum, String timeStamp, String loginUser) {
        String signatrue = md5(md5(appId + randomNum + loginUser + timeStamp) + appToKen);
        return signatrue;
    }

    /**
     * md5 utils
     */
    private String md5(String originStr) {
        String result = null;
        try {
            result = encrypt(originStr, null);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return result;
    }

    private String encrypt(String strSrc, String encName) throws UnsupportedEncodingException {
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes("utf-8");
        try {
            if (encName == null || encName.equals("")) {
                encName = "SHA-256";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    private String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    public List<DMSpaceInfoBean> getSpace(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(spaceInfoParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, spaceInfoMethod, spaceInfoPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSpaceInfoBean> dMSpaceInfoBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dMSpaceInfoBean= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSpaceInfoBean>>() {
            }.getType());
        }
        return dMSpaceInfoBean;
    }

    public List<DMSTagBean> getTags(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(tagsInfoParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("tagName", tagName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, tagsInfoMethod, tagsInfoPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSTagBean> dmsTagBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsTagBean= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSTagBean>>() {
            }.getType());
        }
        return dmsTagBean.stream().sorted(Comparator.comparing(DMSTagBean::getTagName)).collect(Collectors.toList());
    }

    public List<DMSTagPropBean> getTagProps(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(tagPropInfoParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("tagName", tagName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, tagPropInfoMethod, tagPropInfoPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSTagPropBean> dmsTagPropBeans = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsTagPropBeans= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSTagPropBean>>() {
            }.getType());
        }
        return dmsTagPropBeans;
    }

    public List<DMSEdgeBean> getEdges(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(edgesInfoParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("edgeTypeName", edgeTypeName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, edgesInfoMethod, edgesInfoPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSEdgeBean> dmsEdgeBeans = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsEdgeBeans= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSEdgeBean>>() {
            }.getType());
        }
        return dmsEdgeBeans.stream().sorted(Comparator.comparing(DMSEdgeBean::getEdgeTypeName)).collect(Collectors.toList());
    }

    public List<DMSEdgePropBean> getEdgesProp(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(edgePropInfoParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("edgeTypeName", edgeTypeName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, edgePropInfoMethod, edgePropInfoPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSEdgePropBean> dmsEdgePropBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsEdgePropBean= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSEdgePropBean>>() {
            }.getType());
        }
        return dmsEdgePropBean;
    }

    public List<DMSTagIndexBean> getTagIndex(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(tagIndexParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("tagName", tagName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, tagIndexMethod, tagIndexPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSTagIndexBean> dmsIndexBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsIndexBean= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSTagIndexBean>>() {
            }.getType());
        }
        return dmsIndexBean.stream().sorted(Comparator.comparing(DMSTagIndexBean::getIndexName)).collect(Collectors.toList());
    }

    public List<DMSEdgeIndexBean> getEdgeIndex(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(edgeIndexParam);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("spaceName", spaceName);
        if(pageNum!=null){
            param.put("pageNum", Integer.toString(pageNum));
        }
        if(pageSize!=null) {
            param.put("pageSize", Integer.toString(pageSize));
        }
        param.put("edgeTypeName", edgeTypeName);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        String contentStr = requestAndResponse4Nebula(param, edgeIndexMethod, edgeIndexPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSEdgeIndexBean> dmsIndexBean = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            dmsIndexBean= BDPJettyServerHelper.gson().fromJson(data, new TypeToken<List<DMSEdgeIndexBean>>() {
            }.getType());
        }
        return dmsIndexBean.stream().sorted(Comparator.comparing(DMSEdgeIndexBean::getIndexName)).collect(Collectors.toList());
    }

    public DMSPermissionVos getPermission(String urn, String clusterCode, String nebulaAccount, String roleType, String spaceName, String opType, String approvalNo, String executor, String executeTime, Integer pageNum, Integer pageSize) throws DataMapException {
        // param
        Map<String, String> param = getConfigParam(permissionParam);
        param.put("urn", urn);
        param.put("clusterCode", clusterCode);
        param.put("nebulaAccount", nebulaAccount);
        param.put("roleType", roleType);
        param.put("spaceName", spaceName);
        param.put("opType", opType);
        param.put("approvalNo", approvalNo);
        param.put("executor", executor);
        param.put("executeTime", executeTime);

        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");

        DMSPermissionVos dmsPermissionVos = new DMSPermissionVos();

        if(pageNum==null||pageSize==null){
            param.put("pageNum", "1");
            param.put("pageSize", "500");
            getPermissionFromDMS(param, nebulaAccount, headerParam, dmsPermissionVos);
        }else{
            param.put("pageNum", Integer.toString(pageNum));
            param.put("pageSize", Integer.toString(pageSize));
            getPermissionFromDMSByPages(param, nebulaAccount, headerParam, dmsPermissionVos);
            dmsPermissionVos.setPageNum(pageNum);
            dmsPermissionVos.setPageSize(pageSize);
        }

        //图空间名排序
        List<DMSPermissionVo> collect = dmsPermissionVos.getDmsPermissionVos().stream().sorted(Comparator.comparing(DMSPermissionVo::getSpaceName)).collect(Collectors.toList());
        dmsPermissionVos.setDmsPermissionVos(collect);

        return dmsPermissionVos;
    }

    private void getPermissionFromDMS(Map<String, String> param, String nebulaAccount, Map<String, String> headerParam, DMSPermissionVos dmsPermissionVos) throws DataMapException {
        String contentStr = requestAndResponse4Nebula(param, permissionMethod, permissionPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);

        if (null == data || data.isJsonNull() || data.getAsJsonObject().keySet().isEmpty()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            JsonElement content = data.getAsJsonObject().get("content");
            if (null == content || content.isJsonNull()) {
                LOG.warn("The data in the datamap return message body is null");
            } else {
                List<DMSPermissionVo> dmsPermissionVoList = dmsPermissionVos.getDmsPermissionVos();
                if(CollectionUtils.isEmpty(dmsPermissionVoList)){
                    dmsPermissionVoList = new ArrayList<>();
                }
                dmsPermissionVoList.addAll(BDPJettyServerHelper.gson().fromJson(content, new TypeToken<List<DMSPermissionVo>>() {
                }.getType()));
                dmsPermissionVos.setDmsPermissionVos(dmsPermissionVoList);
            }
            if (data.getAsJsonObject().get("totalPage") != null) {
                Integer totalPage = data.getAsJsonObject().get("totalPage").getAsInt();
                if (Integer.parseInt(param.get("pageNum")) < totalPage) {
                    param.put("pageNum", Integer.toString(Integer.parseInt(param.get("pageNum"))+ 1));
                    getPermissionFromDMS(param, nebulaAccount, headerParam, dmsPermissionVos);
                }
            }
            if (data.getAsJsonObject().get("totalCount") != null) {
                Integer totalCount = data.getAsJsonObject().get("totalCount").getAsInt();
                dmsPermissionVos.setTotalCount(totalCount);
            }
            dmsPermissionVos.setTotalPage(1);
        }
    }
    private void getPermissionFromDMSByPages(Map < String, String > param, String nebulaAccount, Map < String, String > headerParam, DMSPermissionVos dmsPermissionVos) throws DataMapException {
        String contentStr = requestAndResponse4Nebula(param, permissionMethod, permissionPath, nebulaAccount, headerParam);
        JsonElement data = preGetJsonData(contentStr);

        if (null == data || data.isJsonNull() || data.getAsJsonObject().keySet().isEmpty()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            JsonElement content = data.getAsJsonObject().get("content");
            if (null == content || content.isJsonNull()) {
                LOG.warn("The data in the datamap return message body is null");
            } else {
                dmsPermissionVos.setDmsPermissionVos(BDPJettyServerHelper.gson().fromJson(content, new TypeToken<List<DMSPermissionVo>>() {
                }.getType()));
            }
            if (data.getAsJsonObject().get("totalCount") != null) {
                Integer totalCount = data.getAsJsonObject().get("totalCount").getAsInt();
                dmsPermissionVos.setTotalCount(totalCount);
            }
            if (data.getAsJsonObject().get("totalPage") != null) {
                Integer totalPage = data.getAsJsonObject().get("totalPage").getAsInt();
                dmsPermissionVos.setTotalPage(totalPage);
            }
        }
    }


    public List<CodeMeta> getCodeMeta(String sqlType,String scriptContent) throws DataMapException {
        // param
        Map<String, String> param = new HashMap<>();
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        //body
        Map<String, String> body = new HashMap<>(2);
        body.put("sqlParseType", sqlType);
        body.put("sql", scriptContent);
        String bodyJson = new Gson().toJson(body);
        String contentStr = requestAndResponse(param, codeMetaMethod, codeMetaPath, systemUsername, headerParam,bodyJson);
        List<CodeMeta> metas ;

        JsonElement data = preGetJsonData(contentStr);
        if (null == data || data.isJsonNull() || data.getAsJsonObject().keySet().isEmpty()) {
            LOG.warn("The data in the datamap return message body is null.");
            metas = new ArrayList<>();
        } else {
            JsonArray edgeSet = data.getAsJsonObject().getAsJsonArray("edgeSet");
            if (!((null == edgeSet) || (edgeSet.isJsonNull()))) {
                metas = new ArrayList<>(edgeSet.size());
                for (JsonElement jsonElement : edgeSet) {
                    String source=jsonElement.getAsJsonObject().get("source"). getAsString();
                    Matcher matcher = datacheckerPattern.matcher(source);
                    if (matcher.find()) {
                        String dbName = matcher.group(1);
                        String tableName = matcher.group(2);
                        metas.add(new CodeMeta(dbName, tableName, defaultPartition));
                    }else{
                        LOG.warn("fail to get code meta from datashapis. source:{}",source);
                    }
                }
            } else {
                metas = new ArrayList<>();
            }
        }
        return metas;
    }

    public TableValidateResult validateTable(CodeMeta table) throws DataMapException {
        TableValidateResult result = new TableValidateResult(table.getDb(), table.getTable(), table.getPartition(),false);
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        //body
        Map<String, String> param = new HashMap<>();
        param.put("sourceType", "Hive");
        param.put("dbCode", table.getDb());
        param.put("datasetName", table.getTable());
        param.put("realMetaDataFlag", "false");
        String contentStr = requestAndResponse(param, validateTableMethod, validateTablePath,
                systemUsername, headerParam);
        if(contentStr.contains("库表不存在")){
            //如果库表不存在，也直接返回非视图。
            result.setView(false);
            return result;
        }
        JsonElement data = preGetJsonData(contentStr);
        if (null == data || data.isJsonNull() || data.getAsJsonObject().keySet().isEmpty()) {
            LOG.warn("The data in the datamap return message body is null.");
            result.setView(false);

        } else {
            LOG.info("The data in the datamap key is {}",data.getAsJsonObject().keySet());
            String storageType = data.getAsJsonObject().get("storageType").getAsString();
            result.setView("View".equalsIgnoreCase(storageType));
        }
        return result;
    }

    public PageInfo<String> getEnterpriseSecureUserList(int pageNum, int pageSize) throws DataMapException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 今天开始时间戳
        Long startOfToday;
        try {
            calendar.setTime(dayFormat.parse(dayFormat.format(calendar.getTime())));
             startOfToday = calendar.getTimeInMillis();
        } catch (ParseException e) {
            startOfToday = 1730736000000L;
        }

        // 今天结束时间戳
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Long endOfToday = calendar.getTimeInMillis();

        Map<String, String> param = new HashMap<>();
        param.put("pageNum", Integer.toString(pageNum));
        param.put("pageSize", Integer.toString(pageSize));
        param.put("todayStartTime", startOfToday.toString());
        param.put("todayEndTime", endOfToday.toString());
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        String contentStr = requestAndResponse(param, enterpriseSecureUserListMethod, enterpriseSecureUserListPath,
                systemUsername, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        long total=data.getAsJsonObject().get("totalCount").getAsLong();
        JsonArray content = data.getAsJsonObject().getAsJsonArray("content");
        List<String> userList = new ArrayList<>( content.size());
        for (JsonElement jsonElement : content) {
            String username = jsonElement.getAsJsonObject().get("userCode").getAsString();
            userList.add(username);
        }
        return new PageInfo(userList, total);
    }

    public boolean isUserNeedScanMethod(String loginUser) throws DataMapException {
        // param
        Map<String, String> param = new HashMap<>();
        param.put("userCode", loginUser);
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        String contentStr = requestAndResponse(param, userNeedScanMethod, userNeedScanPath, systemUsername, headerParam);
        JsonElement data = preGetJsonData(contentStr);
        return data.getAsBoolean();
    }

    public DMSDatasetMaskInfoResult getDetailDatasetMaskInfo(DMSDatasetMaskRequest request) throws DataMapException {
        String json = BDPJettyServerHelper.gson().toJson(request);
        Map<String, String> param = new HashMap<>();
        
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        
        String contentStr = requestAndResponse(param, tableMaskMethod, detailDatasetMaskInfoPath, request.getLoginUser(), headerParam, json);
        JsonElement data = preGetJsonData(contentStr);
        DMSDatasetMaskInfoResult datasetMaskInfoResult = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            datasetMaskInfoResult = BDPJettyServerHelper.gson().fromJson(data, DMSDatasetMaskInfoResult.class);
        }
        return datasetMaskInfoResult;
    }

    public List<DMSColumnMaskInfo> getColumnMaskInfoList(DMSDatasetMaskRequest request) throws DataMapException {
        String json = BDPJettyServerHelper.gson().toJson(request);
        Map<String, String> param = new HashMap<>();
        
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        
        String contentStr = requestAndResponse(param, tableMaskMethod, columnMaskInfoListPath, request.getLoginUser(), headerParam, json);
        JsonElement data = preGetJsonData(contentStr);
        List<DMSColumnMaskInfo> columnMaskInfoList = new ArrayList<>();
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            columnMaskInfoList = BDPJettyServerHelper.gson().fromJson(data,
                    new TypeToken<List<DMSColumnMaskInfo>>() {
                    }.getType());
        }

        return columnMaskInfoList;
    }

    public DMSBdpTableInfo getBdpTableDetail(DMSDatasetMaskRequest request) throws DataMapException {
        String json = BDPJettyServerHelper.gson().toJson(request);
        Map<String, String> param = new HashMap<>();
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        
        String contentStr = requestAndResponse(param, tableMaskMethod, bdpTableDetailPath, request.getLoginUser(), headerParam, json);
        JsonElement data = preGetJsonData(contentStr);
        DMSBdpTableInfo tableInfo = null;
        if (null == data || data.isJsonNull()) {
            LOG.warn("The data in the datamap return message body is null");
        } else {
            tableInfo = BDPJettyServerHelper.gson().fromJson(data, DMSBdpTableInfo.class);
        }
        return tableInfo;
    }

    public DeductSensitiveDatasetQuotaResult deductSensitiveDatasetQuota(String taskId, long amount,String loginUser)
            throws DataMapException {
        // param
        Map<String, String> param = new HashMap<>();
        // header param
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("isAuth", "false");
        //body
        Map<String, Object> body = new HashMap<>(2);
        body.put("taskId", taskId);
        body.put("amount", -1*amount);
        body.put("operationType", "CONSUME");
        body.put("userCode", loginUser);
        String bodyJson = new Gson().toJson(body);
        String contentStr = requestAndResponse(param, deductSensitiveDataQuotaMethod, deductSensitiveDataQuotaPath,
                systemUsername, headerParam,bodyJson);
        JsonElement data = preGetJsonData(contentStr);
        String reduceResult= data.getAsJsonObject().get("reduceResult").getAsString();
        Long remainAmount= data.getAsJsonObject().get("remainAmount").getAsLong();
        DeductSensitiveDatasetQuotaResult result = new DeductSensitiveDatasetQuotaResult();
        result.setQuota(remainAmount);
        if("REDUCE_DUPLICATE".equals(reduceResult)){
            result.setNeedDeduct(false);
        }else if ("REDUCE_SUCCESS".equals(reduceResult)){
            result.setNeedDeduct(true);
            result.setIsSuccess(true);
        }else {
            result.setNeedDeduct(true);
            result.setIsSuccess(false);
        }
        return result;
    }


}
