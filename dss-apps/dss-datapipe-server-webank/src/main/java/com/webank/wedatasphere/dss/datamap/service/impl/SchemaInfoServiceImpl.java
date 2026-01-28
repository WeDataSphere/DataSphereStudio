package com.webank.wedatasphere.dss.datamap.service.impl;

import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.server.esb.http.HttpStaffInfoGetter;
import com.webank.wedatasphere.dss.datamap.constant.UsageHeatTypeEnum;
import com.webank.wedatasphere.dss.datamap.dao.DatasetUserUsageCacheMapper;
import com.webank.wedatasphere.dss.datamap.datamap.*;
import com.webank.wedatasphere.dss.datamap.domain.ExplainCodeMetaRequest;
import com.webank.wedatasphere.dss.datamap.domain.MetaDataQuery;
import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;
import com.webank.wedatasphere.dss.datamap.domain.vo.*;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.service.DSSWorkspaceService;
import com.webank.wedatasphere.dss.datamap.service.SchemaInfoService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: jinyangrao on 2020/11/05
 */
@Service
public class SchemaInfoServiceImpl implements SchemaInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaInfoServiceImpl.class);

    @Autowired
    DataMapDataSource dataMapDataSource;

    @Autowired
    DSSWorkspaceService dssWorkspaceService;
    @Autowired
    DatasetUserUsageCacheMapper usageCacheMapper;


    @Autowired
    HttpStaffInfoGetter httpStaffInfoGetter;

    @Override
    public SchemaBaseInfoVo getSchemaBaseInfo(String dbName, String loginUser) throws DataMapException {

        // 登录用户为非实名用户返回空
        if(!isRealNameUser(loginUser)){
            return new SchemaBaseInfoVo();
        }

        DMSchemaBaseInfoBean dmSchemaBaseInfoBean = dataMapDataSource.getSchemaBaseInfo(dbName, loginUser);
        SchemaBaseInfoVo schemaBaseInfoVo = new SchemaBaseInfoVo();
        if (null != dmSchemaBaseInfoBean) {
            schemaBaseInfoVo.setDbName(dmSchemaBaseInfoBean.getDbCode());
            String DbSize = DMUtils.sizeTranslator(dmSchemaBaseInfoBean.getUsedSpace());
            schemaBaseInfoVo.setDbSize(DbSize);
            schemaBaseInfoVo.setTableQuantity(Integer.parseInt(dmSchemaBaseInfoBean.getTableNum()));
            String DbCapacity = DMUtils.sizeTranslator(dmSchemaBaseInfoBean.getSpaceQuota());
            schemaBaseInfoVo.setDbCapacity(DbCapacity);
            schemaBaseInfoVo.setDescription(dmSchemaBaseInfoBean.getDescription());
        }
        return schemaBaseInfoVo;
    }

    @Override
    public DMToTableMetaDataInfoVo getTableMetaDataInfo(MetaDataQuery metaDataQuery) throws DataMapException {

        // 登录用户为非实名用户返回空
        if(!isRealNameUser(metaDataQuery.getLoginUser())){
            return new DMToTableMetaDataInfoVo();
        }

        String dbName = metaDataQuery.getDbName();
        Integer isTableOwner = metaDataQuery.getIsTableOwner();
        String tableName = metaDataQuery.getTableName();
        Integer orderBy = Integer.parseInt(metaDataQuery.getOrderBy()) - 1;
        int currentPage = metaDataQuery.getCurrentPage();
        int pageSize = metaDataQuery.getPageSize();
        String loginUser = metaDataQuery.getLoginUser();
        Boolean exactTableName = metaDataQuery.getExactTableName();
        String tableOwner = metaDataQuery.getTableOwner();
        UsageHeatTypeEnum usageHeatTypeEnum = getUsageHeatTypeEnum(metaDataQuery.getUsageHeat());
        Long accessStartTime = null;
        Long accessEndTime = null;

        if (usageHeatTypeEnum != null) {
            accessStartTime= usageHeatTypeEnum.getAccessStartTime();
            accessEndTime = usageHeatTypeEnum.getAccessEndTime();
        }

        DMToTableMetaDataInfoVo dmToTableMetaDataInfoVo = dataMapDataSource.getTablesMetaDataInfo(dbName, isTableOwner,
                tableName, orderBy, currentPage, pageSize, loginUser, tableOwner,accessStartTime,accessEndTime);

        List<TableMetaDataInfoVo> tableMetaDataInfoVos = dmToTableMetaDataInfoVo.getTableMetaDataInfoVos();
        for (TableMetaDataInfoVo item : tableMetaDataInfoVos) {

            if ("Y".equalsIgnoreCase(item.getIsNew())) {
                item.setTableSize("数据待更新");
            } else {
                String translatedSize = DMUtils.sizeTranslator(item.getTableSize());
                item.setTableSize(translatedSize);
            }
        }
        List<TableMetaDataInfoVo> tableMetaDataInfoVoList =
                Optional.ofNullable(dmToTableMetaDataInfoVo.getTableMetaDataInfoVos())
                        .orElse(Collections.emptyList());
        Integer tablesTotalSize = dmToTableMetaDataInfoVo.getTotalCount();
        //模糊匹配改成精确匹配
        if (Boolean.TRUE.equals(exactTableName) && StringUtils.isNotBlank(tableName)) {
            tableMetaDataInfoVoList = tableMetaDataInfoVoList.stream().filter(e -> tableName.equals(e.getTableName())).collect(Collectors.toList());
            tablesTotalSize = 1;
        }
        //压缩格式不为空就表示有压缩
        tableMetaDataInfoVoList.forEach(e -> e.setCompressed(StringUtils.isNotEmpty(e.getCompressedFormat())));
        dmToTableMetaDataInfoVo.setTotalCount(tablesTotalSize);
        dmToTableMetaDataInfoVo.setTableMetaDataInfoVos(tableMetaDataInfoVoList);
        // 判断是否工作空间管理员
        boolean isAdmin = false;
        if (null != metaDataQuery.getWorkspaceId()) {
            isAdmin = dssWorkspaceService.isAdminUser(metaDataQuery.getWorkspaceId(), loginUser);
        }
        dmToTableMetaDataInfoVo.setWorkspaceAdmin(isAdmin);
        return dmToTableMetaDataInfoVo;
    }

    @Override
    public List<String> getTablesName(String dbName, String tableName, Integer isTableOwner, Integer orderBy, String loginUser, String tableOwner,String usageHeat) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(loginUser)){
            return new ArrayList<>();
        }

        UsageHeatTypeEnum usageHeatTypeEnum = getUsageHeatTypeEnum(usageHeat);
        Long accessStartTime = null;
        Long accessEndTime = null;

        if (usageHeatTypeEnum != null) {
            accessStartTime= usageHeatTypeEnum.getAccessStartTime();
            accessEndTime = usageHeatTypeEnum.getAccessEndTime();
        }
        return dataMapDataSource.getTablesName(dbName, tableName, isTableOwner, orderBy - 1, loginUser, tableOwner,accessStartTime,accessEndTime);
    }

    @Override
    public Integer transferTablesOwner(TransferTablesOwnerRequest transferTablesOwnerRequest, String userName) throws Exception {

        return dataMapDataSource.transferTablesOwner(transferTablesOwnerRequest, userName);

    }

    public DMToTableMetaDataInfoVo getTableMetaDataInfo(MetaDataQuery metaDataQuery, Integer currentPage) throws DataMapException {
        metaDataQuery.setCurrentPage(currentPage);
        return getTableMetaDataInfo(metaDataQuery);
    }

    @Override
    public List<DMSpaceInfoBean> getSpace(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getSpace(clusterCode, nebulaAccount, spaceName, pageNum, pageSize);
    }

    @Override
    public List<DMSTagBean> getTags(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getTags(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, tagName);
    }

    @Override
    public List<DMSTagPropBean> getTagProps(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getTagProps(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, tagName);
    }

    @Override
    public List<DMSEdgeBean> getEdges(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }

        return dataMapDataSource.getEdges(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, edgeTypeName);
    }

    @Override
    public List<DMSEdgePropBean> getEdgesProp(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getEdgesProp(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, edgeTypeName);
    }

    @Override
    public List<DMSTagIndexBean> getTagIndex(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getTagIndex(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, tagName);
    }

    @Override
    public List<DMSEdgeIndexBean> getEdgeIndex(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException {
        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new ArrayList<>();
        }
        return dataMapDataSource.getEdgeIndex(clusterCode, nebulaAccount, spaceName, pageNum, pageSize, edgeTypeName);
    }

    @Override
    public DMSPermissionVos getPermission(String urn, String clusterCode, String nebulaAccount, String roleType, String spaceName, String opType, String approvalNo, String executor, String executeTime, Integer pageNum, Integer pageSize) throws DataMapException {

        // 登录用户为非实名用户返回空
        if(!isRealNameUser(nebulaAccount)){
            return new DMSPermissionVos();
        }
        return dataMapDataSource.getPermission(urn, clusterCode, nebulaAccount, roleType, spaceName, opType, approvalNo, executor, executeTime, pageNum, pageSize);
    }


    public UsageHeatTypeEnum getUsageHeatTypeEnum(String usageHeat) {
        UsageHeatTypeEnum usageHeatTypeEnum = null;

        if (StringUtils.isEmpty(usageHeat)) {
            return usageHeatTypeEnum;
        }

        try {
            usageHeatTypeEnum = UsageHeatTypeEnum.getUsageHeatTypeEnumByType(usageHeat);
        } catch (ParseException e) {
            LOG.error("getUsageHeatTypeEnum func parse time exception, usageHeat is {}", usageHeat);
            LOG.error(e.getMessage());
        }

        if (usageHeatTypeEnum != null) {
            LOG.info("usageHeat is {},desc is {}, accessStartTime timestamp is {} , accessStartTime time str is  {}",
                    usageHeatTypeEnum.getType(), usageHeatTypeEnum.getDesc()
                    , usageHeatTypeEnum.getAccessStartTime(), usageHeatTypeEnum.getStartTime());

            LOG.info("usageHeat is {}, desc is {}, accessEndTime timestamp is {} , accessEndTime time str is  {}",
                    usageHeatTypeEnum.getType(), usageHeatTypeEnum.getDesc()
                    , usageHeatTypeEnum.getAccessEndTime(), usageHeatTypeEnum.getEndTime());
        } else {
            LOG.warn("usageHeat is {} ,usageHeatTypeEnum is NULL, not find usageHeatTypeEnum info", usageHeat);
        }

        return usageHeatTypeEnum;
    }

    @Override
    public List<CodeMeta> getCodeMeta(ExplainCodeMetaRequest explainCodeMetaRequest, String loginUser)throws DataMapException  {
        String codeType;
        String nodeType = explainCodeMetaRequest.getNodeType();
        if (nodeType.contains("spark")) {
            codeType = "Spark";
        } else if (nodeType.contains("hive")) {
            codeType = "Hive";
        } else if (nodeType.contains("mysql")) {
            codeType = "Mysql";
        }else if (nodeType.contains("oracle")) {
            codeType = "Oracle";
        }else{
            throw new DataMapException("不支持的sql类型：" + nodeType);
        }
        return dataMapDataSource.getCodeMeta(codeType,
                explainCodeMetaRequest.getScriptContent()).stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<TableValidateResult> validateTables(List<CodeMeta> tables, String loginUser) throws DataMapException {
        List<TableValidateResult> collect = new ArrayList<>();
        for (CodeMeta table : tables) {
            TableValidateResult tableValidateResult = dataMapDataSource.validateTable(table);
            collect.add(tableValidateResult);
        }
        return collect;
    }

    @Override
    public List<String> getAllEnterpriseSecureUserList() throws DataMapException {
        List<String> result = new ArrayList<>();
        int currentPageNum=1;
        int pageSize=500;
        PageInfo<String> userList= dataMapDataSource.getEnterpriseSecureUserList(currentPageNum,pageSize);
        result.addAll(userList.getData());
        long total = userList.getTotal();
        int i=0;
        while (i++ < 20 && result.size() < total) {
            currentPageNum++;
            userList = dataMapDataSource.getEnterpriseSecureUserList(currentPageNum, pageSize);
            result.addAll(userList.getData());
        }
        return result;
    }

    @Override
    public boolean userNeedScan(String username)throws DataMapException {
        return dataMapDataSource.isUserNeedScanMethod(username);
    }

    @Override
    public DMSDatasetMaskInfoResult getDetailDatasetMaskInfo(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException {
        return dataMapDataSource.getDetailDatasetMaskInfo(datasetMaskRequest);
    }

    @Override
    public List<DMSColumnMaskInfo> getColumnMaskInfoList(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException {
        return dataMapDataSource.getColumnMaskInfoList(datasetMaskRequest);
    }

    @Override
    public DMSBdpTableInfo getBdpTableDetail(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException {
        return dataMapDataSource.getBdpTableDetail(datasetMaskRequest);
    }

    @Override
    public DeductSensitiveDatasetQuotaResult deductSensitiveDatasetQuota(
            String taskId,long amount, String username) {
        DeductSensitiveDatasetQuotaResult result;
        try {
            result = dataMapDataSource.deductSensitiveDatasetQuota(taskId
                    , amount, username);
            usageCacheMapper.insertOrUpdate(username, result.getQuota());
        } catch (DataMapException e) {
            result = new DeductSensitiveDatasetQuotaResult();
            Long quota_cache = usageCacheMapper.findUsageByUsername(username);
            if (quota_cache != null) {
                result.setNeedDeduct(true);
                if( quota_cache < amount) {
                    result.setQuota(quota_cache);
                    result.setIsSuccess(false);
                }else{
                    long new_quota = quota_cache - amount;
                    result.setQuota(new_quota);
                    result.setIsSuccess(true);
                    usageCacheMapper.insertOrUpdate(username, new_quota);
                }
            }else{
                //没有缓存，也不阻塞查看，扣减成功
                result.setNeedDeduct(true);
                result.setQuota(0L);
                result.setIsSuccess(true);
            }

        }
        return result;
    }



    private boolean isRealNameUser(String username){

        // 获取实名用户信息
        Set<String> userNames = httpStaffInfoGetter.getAllUsernames();

        return  userNames.contains(username);

    }
}
