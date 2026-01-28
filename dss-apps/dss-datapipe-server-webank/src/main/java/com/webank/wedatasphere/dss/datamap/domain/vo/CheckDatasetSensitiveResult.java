package com.webank.wedatasphere.dss.datamap.domain.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.datamap.dao.entity.DatasetScanRecordDO;

import java.util.Map;

/**
 * Author: xlinliu
 * Date: 2024/10/30
 */
public class CheckDatasetSensitiveResult {
    private String resultPath;
    private Boolean hasSensitiveData;
    Map<String,Boolean> metadata;
    private Long total;

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public Boolean getHasSensitiveData() {
        return hasSensitiveData;
    }

    public void setHasSensitiveData(Boolean hasSensitiveData) {
        this.hasSensitiveData = hasSensitiveData;
    }

    public Map<String, Boolean> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Boolean> metadata) {
        this.metadata = metadata;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public static CheckDatasetSensitiveResult fromDO(DatasetScanRecordDO datasetScanRecordDO){
        CheckDatasetSensitiveResult checkDatasetSensitiveResult=new CheckDatasetSensitiveResult();
        checkDatasetSensitiveResult.setHasSensitiveData(datasetScanRecordDO.getHasSensitiveInfo()==1);
        checkDatasetSensitiveResult.setResultPath(datasetScanRecordDO.getPath());
        checkDatasetSensitiveResult.setMetadata(new Gson().fromJson( datasetScanRecordDO.getScanInfo(),
                new TypeToken< Map<String,Boolean>>() {
        }.getType()));
        checkDatasetSensitiveResult.setTotal(datasetScanRecordDO.getRowSize());
        return checkDatasetSensitiveResult;
    }
    public DatasetScanRecordDO toDO(){
        DatasetScanRecordDO datasetScanRecordDO=new DatasetScanRecordDO();
        datasetScanRecordDO.setHasSensitiveInfo(this.hasSensitiveData?1:0);
        datasetScanRecordDO.setPath(this.resultPath);
        datasetScanRecordDO.setScanInfo(new Gson().toJson(this.metadata));
        datasetScanRecordDO.setRowSize(this.total);
        return datasetScanRecordDO;
    }
}
