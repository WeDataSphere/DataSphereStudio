package com.webank.wedatasphere.dss.bean;

import com.google.gson.JsonObject;

/**
 * Author: xlinliu
 * Date: 2023/10/8
 */
public class DWSFlow {
    private Long id;
    private Long projectVersionID;
    private String name;

    private boolean rootFlow;
    private String json;

    private String updator;
    /**
     * 本对象的jsonObject
     */
    private JsonObject jsonObject;

    public DWSFlow(Long id, Long projectVersionID, String name, boolean rootFlow,String updator) {
        this.id = id;
        this.projectVersionID = projectVersionID;
        this.name = name;
        this.rootFlow = rootFlow;
        this.updator = updator;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectVersionID() {
        return projectVersionID;
    }

    public void setProjectVersionID(Long projectVersionID) {
        this.projectVersionID = projectVersionID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRootFlow() {
        return rootFlow;
    }

    public void setRootFlow(boolean rootFlow) {
        this.rootFlow = rootFlow;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
