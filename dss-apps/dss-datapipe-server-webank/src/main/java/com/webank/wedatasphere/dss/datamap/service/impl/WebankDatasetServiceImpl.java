package com.webank.wedatasphere.dss.datamap.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.common.StaffInfo;
import com.webank.wedatasphere.dss.common.StaffInfoGetter;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.server.beans.SimpleHttpResponse;
import com.webank.wedatasphere.dss.common.server.utils.HttpClientUtil;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.datamap.conf.DSSDataPipeConfiguration;
import com.webank.wedatasphere.dss.datamap.dao.DatasetScanRecordMapper;
import com.webank.wedatasphere.dss.datamap.dao.entity.DatasetScanRecordDO;
import com.webank.wedatasphere.dss.datamap.domain.CheckDatasertSensitiveRequest;
import com.webank.wedatasphere.dss.datamap.domain.DatasetReadHistory;
import com.webank.wedatasphere.dss.datamap.domain.DeductSensitiveDatasetQuotaRequest;
import com.webank.wedatasphere.dss.datamap.domain.vo.CheckDatasetSensitiveResult;
import com.webank.wedatasphere.dss.datamap.domain.vo.DeductSensitiveDatasetQuotaResult;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.service.SchemaInfoService;
import com.webank.wedatasphere.dss.errorcode.client.ClientConfiguration;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2024/10/30
 */
@Service
public class WebankDatasetServiceImpl {
    private final static Logger logger = LoggerFactory.getLogger(WebankDatasetServiceImpl.class);

    private static final String RESUlT_SET_SCAN_PATH= Configuration.getGateWayURL()
            +"/api/rest_j/v1/filesystem/resultSetScan";
    /**
     * 超时时间，1个小时
     */
    private static final int connectTimeOut = 3600000;
    private static volatile Set<String> needScanUserCache=new HashSet<>(0) ;

    @Autowired
    DatasetScanRecordMapper datasetScanRecordMapper;
    @Autowired
    private StaffInfoGetter staffInfoGetter;
    @Autowired
    SchemaInfoService schemaInfoService;



    @PostConstruct
    public void syncKnowledge() {
        Utils.defaultScheduler().scheduleAtFixedRate(()->{
            try {
                List<String> userList = schemaInfoService.getAllEnterpriseSecureUserList();
                needScanUserCache = new HashSet<>(userList);
            }catch (DataMapException e){
                logger.error("定时同步dms企业明文白名单用户列表失败！！！");
            }
                    logger.info("定时同步dms企业明文白名单用户列表任务执行结束！！！");
                }
                , 0, 2, TimeUnit.HOURS
        );
    }
    public List<CheckDatasetSensitiveResult> checkDatasetSensitive(CheckDatasertSensitiveRequest checkDatasertSensitiveRequest
            , String operateUser,String proxyUser) {
        String taskId = checkDatasertSensitiveRequest.getTaskId();
        List<CheckDatasetSensitiveResult> result =new ArrayList<>(checkDatasertSensitiveRequest.getPaths().size());
        List<DatasetScanRecordDO> scanRecordDOS = datasetScanRecordMapper.findByTaskId(taskId);
        //之前没扫描过的结果集路径
        List<String> newPath=new ArrayList<>();
        Map<String, DatasetScanRecordDO> scanRecordDOMap =
                scanRecordDOS.stream().collect(Collectors.toMap(DatasetScanRecordDO::getPath,
                        Function.identity(),
                        (e1, e2) -> e1));
        for (String path : checkDatasertSensitiveRequest.getPaths()) {
            if(scanRecordDOMap.containsKey(path)){
                //数据库里有扫描结果，直接从数据库里取扫描结果
                result.add(CheckDatasetSensitiveResult.fromDO(scanRecordDOMap.get(path)));
            }else{
                newPath.add(path);
            }
        }
        if(!newPath.isEmpty()) {
            List<CheckDatasetSensitiveResult> newScanResult;
            boolean needScan = isNeedScan(operateUser);
            if (needScan) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Token-Code", ClientConfiguration.AUTH_TOKEN_VALUE.getValue());
                headers.put("Token-User", proxyUser);
                Map<String, String> map = new HashMap<>(2);
                String paths = newPath.stream().collect(Collectors.joining(","));
                map.put("paths", paths);
                map.put("codeType", checkDatasertSensitiveRequest.getScriptType());
                SimpleHttpResponse reponse = HttpClientUtil.invokeGet(RESUlT_SET_SCAN_PATH, headers, map, "utf-8",
                        connectTimeOut);
                if (reponse.getStatusCode() != 200) {
                    logger.error("scan data set sensitive info  failed. message:{}", reponse.getBody());
                    throw new DSSRuntimeException("scan data set sensitive info  failed：" + reponse.getBody());
                }
                JsonArray obj = new JsonParser().parse(reponse.getBody()).getAsJsonObject()
                        .getAsJsonObject("data")
                        .getAsJsonArray("result");
                newScanResult = new Gson().fromJson(obj,
                        new TypeToken<List<CheckDatasetSensitiveResult>>() {
                        }.getType());
                if (!newScanResult.isEmpty()) {
                    for (CheckDatasetSensitiveResult checkDatasetSensitiveResult : newScanResult) {
                        Map<String, Boolean> metaInfos = checkDatasetSensitiveResult.getMetadata();
                        if(metaInfos!=null&&!metaInfos.isEmpty()){
                            Map<String, Boolean> metaInfosOnlyTrue=
                                    metaInfos.entrySet().stream().filter(Map.Entry::getValue)
                                            .collect(Collectors.toMap(Map.Entry::getKey, v -> true));
                            checkDatasetSensitiveResult.setMetadata(metaInfosOnlyTrue);
                        }

                    }

                    List<DatasetScanRecordDO> newScanDOs = newScanResult.stream().map(CheckDatasetSensitiveResult::toDO)
                            .map(e -> {
                                e.setTaskId(taskId);
                                return e;
                            }).collect(Collectors.toList());
                    datasetScanRecordMapper.batchInsert(newScanDOs);
                }
            } else {
                newScanResult = new ArrayList<>(newPath.size());
                for (String path : newPath) {
                    CheckDatasetSensitiveResult checkDatasetSensitiveResult = new CheckDatasetSensitiveResult();
                    checkDatasetSensitiveResult.setHasSensitiveData(false);
                    checkDatasetSensitiveResult.setResultPath(path);
                }
            }
            result.addAll(newScanResult);
        }
        return  result;
    }

    public DeductSensitiveDatasetQuotaResult deductSensitiveDatasetQuota(
            DeductSensitiveDatasetQuotaRequest deductSensitiveDatasetQuotaRequest, String username) {
        DeductSensitiveDatasetQuotaResult result;

        //从数据库里查询结果集扫描记录。
        String taskId = deductSensitiveDatasetQuotaRequest.getTaskId();
        List<DatasetScanRecordDO> scanRecordDOS = datasetScanRecordMapper.findByTaskId(taskId);
        //之前没扫描过的结果集路径
        List<String> newPath=new ArrayList<>();
        Map<String, DatasetScanRecordDO> scanRecordDOMap =
                scanRecordDOS.stream().collect(Collectors.toMap(DatasetScanRecordDO::getPath,
                        Function.identity(),
                        (e1, e2) -> e1));
        //只需要处理有敏感信息且没有阅读过的结果集
        List<DatasetScanRecordDO> recordFirstRead = new ArrayList<>();
        for (String path : deductSensitiveDatasetQuotaRequest.getPaths()) {
            if(scanRecordDOMap.containsKey(path)){
                DatasetScanRecordDO scanRecordDO = scanRecordDOMap.get(path);
                if(scanRecordDO.getHasSensitiveInfo()==0){
                    //无敏感信息，直接不收集
                    continue;
                }
                List<DatasetReadHistory> readHistory= new Gson().fromJson(scanRecordDO.getReadHistory(),
                        new TypeToken<List<DatasetReadHistory>>() {
                        }.getType());
                if(readHistory==null ||
                        readHistory.stream().map(DatasetReadHistory::getUsername).noneMatch(username::equals)){
                    recordFirstRead.add(scanRecordDO);
                }
            }
        }
        if(recordFirstRead.isEmpty()){
            result = new DeductSensitiveDatasetQuotaResult();
            result.setNeedDeduct(false);
        }else {
            long amount=0L;
            Map<String, Boolean> sensitiveColumn = new HashMap<>();
            for (DatasetScanRecordDO scanRecordDO : recordFirstRead) {
                amount += scanRecordDO.getRowSize();
                Map<String, Boolean> metaInfo = new Gson().fromJson(scanRecordDO.getScanInfo(),
                        new TypeToken<Map<String,Boolean>>() {}.getType());
                if(metaInfo!=null){
                    sensitiveColumn.putAll(metaInfo);
                }
            }

            DeductSensitiveDatasetQuotaResult deductInDMS= schemaInfoService.deductSensitiveDatasetQuota(taskId,amount,
                    username);
            result = new DeductSensitiveDatasetQuotaResult();
            if(deductInDMS.getNeedDeduct()){
                result.setNeedDeduct(true);
                result.setQuota(deductInDMS.getQuota());
                result.setIsSuccess(deductInDMS.getIsSuccess());
                result.setMetadata(sensitiveColumn);
                result.setDataSetSize(amount);
            }else {
                result.setNeedDeduct(false);
            }
        }
        //扣减成功，则记录扣减记录
        if(result.getIsSuccess()!=null &&result.getIsSuccess()){
            for (DatasetScanRecordDO scanRecordDO : recordFirstRead) {
                List<DatasetReadHistory> readHistory= new Gson().fromJson(scanRecordDO.getReadHistory(),
                        new TypeToken<List<DatasetReadHistory>>() {
                        }.getType());
                if(readHistory==null){
                    readHistory = new ArrayList<>();
                }
                readHistory.add(new DatasetReadHistory(username, deductSensitiveDatasetQuotaRequest.getReadType(), new Date()));
                String newHistory=new Gson().toJson(readHistory);
                datasetScanRecordMapper.updateReadHistory(scanRecordDO.getId(),newHistory);
            }
        }

        return result;
    }

    private boolean isNeedScan(String operateUser) {
        boolean needScan;
        StaffInfo staffInfo=staffInfoGetter.getStaffInfoByUsername(operateUser);
        //是否白名单部门
        boolean isWhiteListBg = staffInfo != null
                && DSSDataPipeConfiguration.DATASET_SENSITIVE_SCAN_BG_LIST.contains(staffInfo.getBgName());
        if(isWhiteListBg) {
            try {
                needScan = schemaInfoService.userNeedScan(operateUser);
            } catch (DataMapException e) {
                logger.warn("dms error.we use cache.",e);
                needScan = needScanUserCache.contains(operateUser);
            }
        }else{
            needScan=false;
        }
        return needScan;
    }
}
