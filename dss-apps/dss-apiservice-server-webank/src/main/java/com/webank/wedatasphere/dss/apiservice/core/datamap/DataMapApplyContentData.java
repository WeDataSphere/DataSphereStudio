/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.datamap;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.linkis.common.conf.CommonVars;

import java.util.Date;

/**
 * created by cooperyang on 2020/8/28
 * Description:
 */
public class DataMapApplyContentData {

    private static final String CLUSTER_TYPE = CommonVars.apply("wds.dss.datamap.cluster.type", "BDAP").getValue();
    private static final String ENV_VAL = CommonVars.apply("wds.dss.datamap.env", "1").getValue();
    private static final String SYSTEM_VAL = CommonVars.apply("wds.dss.datamap.system", "DWS-IDE").getValue();
    private static final String DEV_PRIN = CommonVars.apply("wds.dss.datamap.dev.principles", "raoqin(覃娆)").getValue();
    private static final String TIME_VAL = CommonVars.apply("wds.dss.datamap.time", "365").getValue();
    private static final String REASON_VAL = CommonVars.apply("wds.dss.datamap.reason", "api service apply for resource").getValue();
    private static final String IPNAME_VAL = CommonVars.apply("wds.dss.datamap.ip.name", "127.0.0.1").getValue();
    private static final String IMPORTANCE_VAL = CommonVars.apply("wds.dss.datamap.importance", "1").getValue();
    private static final String DATAMAP_STATUS = CommonVars.apply("wds.dss.datamap.status", "").getValue();


    @SerializedName("bdap_job_apply-4-P1-clusterType")
    private String clusterType = CLUSTER_TYPE;
    @SerializedName("bdap_job_apply-4-P1-env")
    private String env = ENV_VAL;
    @SerializedName("bdap_job_apply-4-P1-apiName")
    private String apiName;
    @SerializedName("bdap_job_apply-4-P1-apiCreator")
    private String apiCreator;
    @SerializedName("bdap_job_apply-4-P1-apiDescription")
    private String apiDescription;
    @SerializedName("bdap_job_apply-4-P1-user")
    private String user;
    @SerializedName("bdap_job_apply-4-P1-system")
    private String system = SYSTEM_VAL;
    @SerializedName("bdap_job_apply-4-P1-time")
    private String time = TIME_VAL;
    @SerializedName("bdap_job_apply-4-P1-begainTime")
    private Date beganTime;
    @SerializedName("bdap_job_apply-4-P1-reason")
    private String reason = REASON_VAL;
    @SerializedName("bdap_job_apply-4-P1-ipName")
    private String ipName = IPNAME_VAL;
    @SerializedName("bdap_job_apply-4-P1-importance")
    private String importance = IMPORTANCE_VAL;
    @SerializedName("bdap_job_apply-4-P2-dbName")
    private String dbName;
    @SerializedName("bdap_job_apply-4-P2-tableName")
    private String tableName;
    @SerializedName("bdap_job_apply-4-P2-devPrincipals")
    private String devPrincipals = DEV_PRIN;

    @SerializedName("bdap_job_apply-4-P2-status")
    private String status = DATAMAP_STATUS;

    @SerializedName("bdap_job_apply-4-P1-proxyUser")
    private String proxyUser;

    @SerializedName("bdap_job_apply-4-P2-isSure")
    private String isSure = "0";

    @SerializedName("bdap_job_apply-4-P2-dataPrincipals")
    private String dataPrincipals = "";

    //关联的产品信息
    @SerializedName("bdap_job_apply-4-P2-productInfo")
    private String productInfo = "";
    //数据所属子系统
    @SerializedName("bdap_job_apply-4-P2-subSystemInfo")
    private String subSystemInfo = "";
    //是否涉及一级数据
    @SerializedName("bdap_job_apply-4-P1-sensitiveLevel")
    private String sensitiveLevel;

    public DataMapApplyContentData() {
        // no args constructor
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiCreator() {
        return apiCreator;
    }

    public void setApiCreator(String apiCreator) {
        this.apiCreator = apiCreator;
    }

    public String getApiDescription() {
        return apiDescription;
    }

    public void setApiDescription(String apiDescription) {
        this.apiDescription = apiDescription;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Date getBeganTime() {
        return beganTime;
    }

    public void setBeganTime(Date beganTime) {
        this.beganTime = beganTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIpName() {
        return ipName;
    }

    public void setIpName(String ipName) {
        this.ipName = ipName;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDevPrincipals() {
        return devPrincipals;
    }

    public void setDevPrincipals(String devPrincipals) {
        this.devPrincipals = devPrincipals;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }


    public String getIsSure() {
        return isSure;
    }

    public void setIsSure(String isSure) {
        this.isSure = isSure;
    }

    public String getDataPrincipals() {
        return dataPrincipals;
    }

    public void setDataPrincipals(String dataPrincipals) {
        this.dataPrincipals = dataPrincipals;
    }

    public String getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public String getSubSystemInfo() {
        return subSystemInfo;
    }

    public void setSubSystemInfo(String subSystemInfo) {
        this.subSystemInfo = subSystemInfo;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).replaceAll("\"", "'");
    }
}
