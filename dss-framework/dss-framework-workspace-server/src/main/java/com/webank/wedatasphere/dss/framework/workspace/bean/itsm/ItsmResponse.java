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
package com.webank.wedatasphere.dss.framework.workspace.bean.itsm;

public class ItsmResponse {

    private String data;
    private int retCode;
    private String retDetail;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ItsmResponse data(String data) {
        this.data = data;
        return this;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public ItsmResponse retCode(int retCode) {
        this.retCode = retCode;
        return this;
    }

    public String getRetDetail() {
        return retDetail;
    }

    public void setRetDetail(String retDetail) {
        this.retDetail = retDetail;
    }

    public ItsmResponse retDetail(String retDetail) {
        this.retDetail = retDetail;
        return this;
    }

    public static ItsmResponse ok(){
        ItsmResponse itsmResponse = new ItsmResponse();
        return itsmResponse.retCode(0);
    }
    public static ItsmResponse error(){
        ItsmResponse itsmResponse = new ItsmResponse();
        return itsmResponse.retCode(-1);
    }


}
