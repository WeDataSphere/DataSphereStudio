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
package com.webank.wedatasphere.dss.workflow.entity.response;

import com.webank.wedatasphere.dss.workflow.entity.DataDevelopNodeInfo;
import com.webank.wedatasphere.dss.workflow.entity.DataViewNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class DataViewNodeResponse {


    private Long total = 0L;

    private List<DataViewNodeInfo> dataDevelopNodeInfoList = new ArrayList<>();


    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<DataViewNodeInfo> getDataDevelopNodeInfoList() {
        return dataDevelopNodeInfoList;
    }

    public void setDataDevelopNodeInfoList(List<DataViewNodeInfo> dataDevelopNodeInfoList) {
        this.dataDevelopNodeInfoList = dataDevelopNodeInfoList;
    }
}
