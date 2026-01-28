package com.webank.wedatasphere.dss.datamap.datamap.transferor;

import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;

public class TransferContext {

    private final TablesOwnerTransferor tablesOwnerTransferor;

    public TransferContext(TablesOwnerTransferor tablesOwnerTransferor) {
        this.tablesOwnerTransferor = tablesOwnerTransferor;
    }

    public Integer executeStrategy(TransferTablesOwnerRequest transferTablesOwnerRequest, String userName) throws Exception {
        return tablesOwnerTransferor.transferOwner(transferTablesOwnerRequest, userName);
    }
}
