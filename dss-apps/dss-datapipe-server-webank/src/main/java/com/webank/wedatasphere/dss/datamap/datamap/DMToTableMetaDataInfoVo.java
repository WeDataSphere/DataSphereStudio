package com.webank.wedatasphere.dss.datamap.datamap;

import com.webank.wedatasphere.dss.datamap.domain.vo.TableMetaDataInfoVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMToTableMetaDataInfoVo {
    private Integer totalCount = 0;
    private List<TableMetaDataInfoVo> tableMetaDataInfoVos = new ArrayList<>();

    // 是否空间管理员
    private boolean isWorkspaceAdmin;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<TableMetaDataInfoVo> getTableMetaDataInfoVos() {
        return tableMetaDataInfoVos;
    }

    public void setTableMetaDataInfoVos(List<TableMetaDataInfoVo> tableMetaDataInfoVos) {
        this.tableMetaDataInfoVos = tableMetaDataInfoVos;
    }

    public boolean isWorkspaceAdmin() {
        return isWorkspaceAdmin;
    }

    public void setWorkspaceAdmin(boolean workspaceAdmin) {
        isWorkspaceAdmin = workspaceAdmin;
    }
}
