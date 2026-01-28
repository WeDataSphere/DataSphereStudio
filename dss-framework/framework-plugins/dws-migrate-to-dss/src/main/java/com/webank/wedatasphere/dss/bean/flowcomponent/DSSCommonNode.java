package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dss 通用工作流属性
 * Author: xlinliu
 * Date: 2023/10/12
 */
public class DSSCommonNode {
    private String key;
    private String title;
    private String desc;
    private DSSNodeLayout layout;
    private Map<String,Object> params=new HashMap<>();
    private List<DSSNodeResource> resources=new ArrayList<>();
    private boolean selected=false;

    private String bindViewKey = "";
    private Long createTime;
    private Long modifyTime;
    private String modifyUser;
    private String id;
    private String jobType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public DSSNodeLayout getLayout() {
        return layout;
    }

    public void setLayout(DSSNodeLayout layout) {
        this.layout = layout;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public List<DSSNodeResource> getResources() {
        return resources;
    }

    public void setResources(List<DSSNodeResource> resources) {
        this.resources = resources;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getBindViewKey() {
        return bindViewKey;
    }

    public void setBindViewKey(String bindViewKey) {
        this.bindViewKey = bindViewKey;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public static String convertType(String dwsType){
        switch (dwsType){
            case "ujes.shell": return "linkis.shell.sh";
            case "wtss.connector": return "linkis.control.empty";
            case "flow": return "workflow.subflow";
            case "wtss.eventcheckerf": return "linkis.appconn.eventchecker.eventsender";
            case "wtss.eventcheckerw": return "linkis.appconn.eventchecker.eventreceiver";
            case "wtss.datachecker": return "linkis.appconn.datachecker";
            default:
                throw new DSSRuntimeException("unknown node type"+dwsType);
        }
    }
}
