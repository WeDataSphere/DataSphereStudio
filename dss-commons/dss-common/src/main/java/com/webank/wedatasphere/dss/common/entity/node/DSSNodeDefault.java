/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.common.entity.node;

import com.webank.wedatasphere.dss.common.entity.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DSSNodeDefault implements DSSNode {
    private Layout layout;
    private String id;
    private String key;
    private String jobType;
    private String lastUpdateTime;
    private Map<String, Object> params;
    private List<Resource> resources;
    private String title;
    private String desc;
    private Long createTime;
    private String userProxy;
    private String modifyUser;
    private Long modifyTime;
    private String appTag;
    private String businessTag;
    private String ecConfTemplateId;
    private String ecConfTemplateName;
    /**
     * dependencys 是该Node的依赖节点
     */
    private List<String> dependencys = new ArrayList<String>();
    /**
     * jobContent 是node的内容
     */
    private Map<String, Object> jobContent;


    @Override
    public Layout getLayout() {
        return layout;
    }


    @Override
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public Map<String, Object> getJobContent() {
        return jobContent;
    }

    @Override
    public void setJobContent(Map<String, Object> jobContent) {
        this.jobContent = jobContent;
    }

    @Override
    public String getUserProxy() {
        return userProxy;
    }

    @Override
    public void setUserProxy(String userProxy) {
        this.userProxy = userProxy;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getNodeType() {
        return jobType;
    }

    @Override
    public void setNodeType(String nodeType) {
        this.jobType = nodeType;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = name;
    }

    @Override
    public void addDependency(String nodeName) {
        dependencys.add(nodeName);
    }

    @Override
    public void setDependency(List<String> dependency) {
        this.dependencys = dependency;
    }

    @Override
    public void removeDependency(String nodeName) {
        dependencys.remove(nodeName);
    }

    @Override
    public List<String> getDependencys() {
        return dependencys;
    }

    @Override
    public String getModifyUser() {
        return modifyUser;
    }

    @Override
    public Long getModifyTime() {
        return modifyTime;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "dwsNode{" +
                "layout=" + layout +
                ", id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", jobType='" + jobType + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", params=" + params +
                ", resources=" + resources +
                ", jobType=" + jobType +
                ", title=" + title +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", modifyUser=" + modifyUser +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getAppTag() {
        return appTag;
    }

    public void setAppTag(String appTag) {
        this.appTag = appTag;
    }

    public String getBusinessTag() {
        return businessTag;
    }

    public void setBusinessTag(String businessTag) {
        this.businessTag = businessTag;
    }

    public String getEcConfTemplateId() {
        return ecConfTemplateId;
    }

    public void setEcConfTemplateId(String ecConfTemplateId) {
        this.ecConfTemplateId = ecConfTemplateId;
    }

    public String getEcConfTemplateName() {
        return ecConfTemplateName;
    }

    public void setEcConfTemplateName(String ecConfTemplateName) {
        this.ecConfTemplateName = ecConfTemplateName;
    }

    public void setDependencys(List<String> dependencys) {
        this.dependencys = dependencys;
    }
}
