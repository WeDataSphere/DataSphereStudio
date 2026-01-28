package com.webank.wedatasphere.dss.datamap.domain;

import com.webank.wedatasphere.dss.datamap.domain.vo.CodeMeta;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2025/2/10
 */
public class ValidateTablesStringRequest {
    private String checkObject;
    private String jobDesc;

    public String getCheckObject() {
        return checkObject;
    }

    public void setCheckObject(String checkObject) {
        this.checkObject = checkObject;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }
}
