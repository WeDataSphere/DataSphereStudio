package com.webank.wedatasphere.dss.datamap.datamap.transferor;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.datamap.conf.DataMapConnConf;
import com.webank.wedatasphere.dss.datamap.constant.ClusterTypeEnum;
import com.webank.wedatasphere.dss.datamap.constant.EnvTypeEnum;
import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;
import com.webank.wedatasphere.dss.datamap.util.ITSMRequestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ITSMTransferor implements TablesOwnerTransferor {

    private final String requestUrl = DataMapConnConf.ITSM_REQUEST_URL();
    private final String clusterName = DataMapConnConf.DB_CLUSTER_NAME();
    private final String itsmEnv = DataMapConnConf.ITSM_ENV();
    private final String dbEnv = DataMapConnConf.DB_ENV();
    private final String implManager = DataMapConnConf.ITSM_IMPL_MANAGER();
    private final String appId = DataMapConnConf.ITSM_APP_ID();
    private final String appKey = DataMapConnConf.ITSM_AP_KEY();
    private final String userId = DataMapConnConf.ITSM_USER_ID();
    private final String formId = DataMapConnConf.ITSM_FORM_ID();
    private final String formVersion = DataMapConnConf.ITSM_FORM_VERSION();

    @Override
    public Integer transferOwner(TransferTablesOwnerRequest transferTablesOwnerRequest, String userName) throws Exception {

        // ITSM表单个性化内容
        Map<String, Object> dataList = new HashMap<>();
        dataList.put("cluster_name", ClusterTypeEnum.getCnName(clusterName));
        dataList.put("env", EnvTypeEnum.getCnName(dbEnv));
        dataList.put("db_name", transferTablesOwnerRequest.getDbName());
        dataList.put("table_name", transferTablesOwnerRequest.getTablesName().toString().replaceAll("(?:\\[|null|\\]| +)", ""));
        dataList.put("old_owner", transferTablesOwnerRequest.getOldOwner());
        dataList.put("new_owner", transferTablesOwnerRequest.getNewOwner());

        // ITSM个性化审批链
        ArrayList<ArrayList<String>> requestRatifyChains = Lists.newArrayList(Lists.newArrayList(transferTablesOwnerRequest.getDataGovernanceAdmin(), "0", "0", "业务数据治理管理员"),
                Lists.newArrayList(userName, "0", "2", "表属主转移流程申请人"),
                Lists.newArrayList(transferTablesOwnerRequest.getNewOwner(), "0", "0", "新Owner"),
                Lists.newArrayList(userName, "1", "0", "表属主转移流程申请人上级"),
                Lists.newArrayList(transferTablesOwnerRequest.getNewOwner(), "1", "0", "新Owner上级"),
                Lists.newArrayList(implManager, "0", "0", "实施负责人"));

        //httpClient post请求ITSM
        return ITSMRequestUtils.doJsonPOST2(itsmEnv, requestUrl, userId, appId, appKey, transferTablesOwnerRequest.getApprovalTitle(), "2001", LocalDate.now().plusDays(1).toString(),
                transferTablesOwnerRequest.getDescription(), transferTablesOwnerRequest.getOldOwner() + "," + transferTablesOwnerRequest.getNewOwner(), userName,
                dataList, formId, formVersion, requestRatifyChains);
    }

}