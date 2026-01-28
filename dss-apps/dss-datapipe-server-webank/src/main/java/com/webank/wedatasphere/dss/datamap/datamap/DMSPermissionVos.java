package com.webank.wedatasphere.dss.datamap.datamap;

import java.util.List;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSPermissionVos {
    private List<DMSPermissionVo> dmsPermissionVos;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalCount;
    private Integer totalPage;

    public List<DMSPermissionVo> getDmsPermissionVos() {
        return dmsPermissionVos;
    }

    public void setDmsPermissionVos(List<DMSPermissionVo> dmsPermissionVos) {
        this.dmsPermissionVos = dmsPermissionVos;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}
