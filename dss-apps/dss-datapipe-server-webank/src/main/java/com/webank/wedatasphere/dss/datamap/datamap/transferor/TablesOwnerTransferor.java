package com.webank.wedatasphere.dss.datamap.datamap.transferor;

import com.webank.wedatasphere.dss.datamap.domain.TransferTablesOwnerRequest;

public interface TablesOwnerTransferor {

    Integer transferOwner(TransferTablesOwnerRequest transferTablesOwnerRequest, String userName) throws Exception;

}
