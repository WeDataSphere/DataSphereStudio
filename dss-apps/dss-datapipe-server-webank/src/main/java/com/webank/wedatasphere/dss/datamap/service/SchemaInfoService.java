package com.webank.wedatasphere.dss.datamap.service;

import com.webank.wedatasphere.dss.datamap.datamap.*;
import com.webank.wedatasphere.dss.datamap.domain.ExplainCodeMetaRequest;
import com.webank.wedatasphere.dss.datamap.domain.MetaDataQuery;
import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;
import com.webank.wedatasphere.dss.datamap.domain.vo.*;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.util.ThreadPoolUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * @author: jinyangrao on 2020/11/05
 */
public interface SchemaInfoService {

    ExecutorService cachedThreadPool = ThreadPoolUtils.newCachedThreadPool(10,"dss-datapipe-thread-",true);
    Random random = new Random();

    SchemaBaseInfoVo getSchemaBaseInfo(String dbName, String loginUser) throws DataMapException;

    DMToTableMetaDataInfoVo getTableMetaDataInfo(MetaDataQuery metaDataQuery) throws DataMapException;

    List<String> getTablesName(String dbName, String tableName, Integer isTableOwner, Integer orderBy, String loginUser,String tableOwner,String usageHeat) throws DataMapException;

    Integer transferTablesOwner(TransferTablesOwnerRequest transferTablesOwnerRequest, String userName) throws Exception;

    DMToTableMetaDataInfoVo getTableMetaDataInfo(MetaDataQuery metaDataQuery, Integer currentPage) throws DataMapException;

    List<DMSpaceInfoBean> getSpace(String clusterCode, String nebulaAccount, String spaceName, Integer pageNum, Integer pageSize) throws DataMapException;

    List<DMSTagBean> getTags(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException;

    List<DMSTagPropBean> getTagProps(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException;

    List<DMSEdgeBean> getEdges(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException;

    List<DMSEdgePropBean> getEdgesProp(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException;

    List<DMSTagIndexBean> getTagIndex(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String tagName) throws DataMapException;

    List<DMSEdgeIndexBean> getEdgeIndex(String clusterCode, String userName, String spaceName, Integer pageNum, Integer pageSize, String edgeTypeName) throws DataMapException;

    DMSPermissionVos getPermission(String urn, String clusterCode, String userName, String roleType, String spaceName, String opType, String approvalNo, String executor, String executeTime, Integer pageNum, Integer pageSize) throws DataMapException;

    List<CodeMeta> getCodeMeta(ExplainCodeMetaRequest explainCodeMetaRequest,String loginUser)throws DataMapException ;
    List<TableValidateResult> validateTables(List<CodeMeta> tables, String loginUser)throws DataMapException ;
    List<String> getAllEnterpriseSecureUserList() throws DataMapException;
    boolean userNeedScan(String username)throws DataMapException;

    DMSDatasetMaskInfoResult getDetailDatasetMaskInfo(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException;

    List<DMSColumnMaskInfo> getColumnMaskInfoList(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException;

    DMSBdpTableInfo getBdpTableDetail(DMSDatasetMaskRequest datasetMaskRequest) throws DataMapException;

    DeductSensitiveDatasetQuotaResult deductSensitiveDatasetQuota(String taskId, long amount, String username);
}
