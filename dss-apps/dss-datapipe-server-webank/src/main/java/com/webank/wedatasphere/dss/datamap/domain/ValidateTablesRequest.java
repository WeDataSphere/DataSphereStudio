package com.webank.wedatasphere.dss.datamap.domain;

import com.webank.wedatasphere.dss.datamap.domain.vo.CodeMeta;
import java.util.List;

/**
 * Author: xlinliu
 * Date: 2025/2/10
 */
public class ValidateTablesRequest {
List<CodeMeta> tables;

    public List<CodeMeta> getTables() {
        return tables;
    }

    public void setTables(List<CodeMeta> tables) {
        this.tables = tables;
    }
}
