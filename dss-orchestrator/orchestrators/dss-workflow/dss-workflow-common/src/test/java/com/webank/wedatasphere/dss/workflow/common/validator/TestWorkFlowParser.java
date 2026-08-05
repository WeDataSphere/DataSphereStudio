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

package com.webank.wedatasphere.dss.workflow.common.validator;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.common.entity.Resource;
import com.webank.wedatasphere.dss.common.entity.node.DSSEdge;
import com.webank.wedatasphere.dss.common.entity.node.DSSEdgeDefault;
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.common.entity.node.DSSNodeDefault;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.workflow.common.parser.WorkFlowParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 单元测试用 WorkFlowParser 桩。镜像 server 中 {@code DefaultWorkFlowParser} 的 nodes/edges 解析逻辑
 * （同样的 Gson 实例 + TypeToken→DSSNodeDefault/DSSEdgeDefault），保证测试与运行时解析行为一致。
 * 其余方法返回空/null（校验器只用 getWorkFlowNodes/getWorkFlowEdges）。
 */
public class TestWorkFlowParser implements WorkFlowParser {

    @Override
    public List<DSSNode> getWorkFlowNodes(String workFlowJson) {
        JsonObject jsonObject = new JsonParser().parse(workFlowJson).getAsJsonObject();
        JsonArray nodeJsonArray = jsonObject.getAsJsonArray("nodes");
        return DSSCommonUtils.COMMON_GSON.fromJson(nodeJsonArray, new TypeToken<List<DSSNodeDefault>>() {
        }.getType());
    }

    @Override
    public List<DSSEdge> getWorkFlowEdges(String workFlowJson) {
        JsonObject jsonObject = new JsonParser().parse(workFlowJson).getAsJsonObject();
        JsonArray edgeJsonArray = jsonObject.getAsJsonArray("edges");
        return DSSCommonUtils.COMMON_GSON.fromJson(edgeJsonArray, new TypeToken<List<DSSEdgeDefault>>() {
        }.getType());
    }

    @Override
    public List<Resource> getWorkFlowResources(String workFlowJson) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getParamConfTemplate(String workFlowJson) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getWorkFlowNodesJson(String workFlowJson) {
        return Collections.emptyList();
    }

    @Override
    public String updateFlowJsonWithKey(String workFlowJson, String key, Object value) {
        return workFlowJson;
    }

    @Override
    public String updateFlowJsonWithMap(String workFlowJson, Map<String, Object> props) {
        return workFlowJson;
    }

    @Override
    public String getValueWithKey(String workFlowJson, String key) {
        return null;
    }
}
