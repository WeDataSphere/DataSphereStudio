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
package com.webank.wedatasphere.dss.orchestrator.core.type;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationWarnException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author enjoyyin
 * @date 2022-03-20
 * @since 0.5.0
 */
public class OrchestratorJsonRelation implements DSSOrchestratorRelation {

    private String relationString;
    private Map<String, Object> relationMap;
    private String mode;
    private String name;
    private String nameCN;
    private String schedulerAppConnName;
    private String bindingAppConnName;
    private Predicate<AppConn> predicate;

    public OrchestratorJsonRelation(String relationString) {
        this.relationString = relationString;
    }

    public void init() {
        relationMap = DSSCommonUtils.COMMON_GSON.fromJson(relationString, Map.class);
        Consumer<String> checkConsumer = key -> {
            if(!relationMap.containsKey(key)) {
                throw new ExternalOperationWarnException(50035, "the relation does not contains ['" + key + "'], relation string is " + relationString);
            }
        };
        checkConsumer.accept("mode");
        checkConsumer.accept("name");
        checkConsumer.accept("name_cn");
        checkConsumer.accept("bindingAppConnName");
        mode = (String) relationMap.get("mode");
        name = (String) relationMap.get("name");
        nameCN = (String) relationMap.get("nameCN");
        bindingAppConnName = (String) relationMap.get("bindingAppConnName");
        schedulerAppConnName = (String) relationMap.get("schedulerAppConnName");
        if(StringUtils.isBlank(schedulerAppConnName)) {
            schedulerAppConnName = null;
        }
        String linkedAppConn = (String) relationMap.get("linkedAppConn");
        if(StringUtils.isNotBlank(linkedAppConn)) {
            String[] appConns = linkedAppConn.split(",");
            predicate = appConn -> ArrayUtils.contains(appConns, appConn.getAppDesc().getAppName());
        } else {
            predicate = appConn -> appConn instanceof OnlyDevelopmentAppConn;
        }
    }

    @Override
    public String getDSSOrchestratorMode() {
        return mode;
    }

    @Override
    public String getDSSOrchestratorName() {
        return name;
    }

    @Override
    public String getDSSOrchestratorName_CN() {
        return nameCN;
    }

    @Override
    public String getBindingAppConnName() {
        return bindingAppConnName;
    }

    @Override
    public String getBindingSchedulerAppConnName() {
        return schedulerAppConnName;
    }

    @Override
    public Predicate<AppConn> isLinkedAppConn() {
        return predicate;
    }

}
