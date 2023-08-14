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

package com.webank.wedatasphere.dss.framework.release.conf;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public enum ReleaseCodeEnum {
    export_orchestrator_error("60099","导出编排模式报错"),
    export_orchestrator_error2("60015","导出编排模式报错"),
    export_orchestrator_error3("63323","导出编排模式报错"),
    post_orchestrator_server_out_time("63323","请求DEV编排模式服务超时"),
    create_third_node_eport_ref("60089","创建第三方节点导出REF报错"),
    export_orchestratorc_version_error("90038","导出编排模式版本号不存在"),
    export_qualitis_error("90156","导出qualitis报错"),
    import_orchestrator_error("60016","导入编排模式报错"),
    import_qualitis_error("90157","导入qualitis报错"),
    export_visualis_error("90176","导出visualis报错"),
    current_user_no_exist_wtss("100323","当前设置用户在Azkaban为空"),
    current_user_no_exist_wtss2("100323","当前设置用户在Azkaban对应工程没有权限"),
//    import_visualis_error("90157","导入visualis报错"),
    ERROR_FIRST("8888801","第一步(一共五步)失败: 从开发中心导出工作流,建议重新发布"),
    ERROR_SECOND("8888802","第二步(一共五步)失败: 从开发中心导入到生产中心,建议重新发布"),
    ERROR_THIRD("8888803","第三步(一共五步)失败: 从生产中心导入到WTSS（调度中心）：1.请检查当前发布人是否存在WTSS，2.WTSS对应工程下有没有发布人的相关权限，3.工程是否存在WTSS"),
    ERROR_FOURTH("8888804","第四步(一共五步)失败: 开发中心，新增编排模式版本,建议重新发布"),
    ERROR_FIFTH("8888805","第五步(一共五步)失败: 开发中心，将最新的编排模式版本ID更新工程模块的编排模式")
    ;

    ReleaseCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static String getErrorMsgByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        return Arrays.stream(ReleaseCodeEnum.values())
                .filter(a -> a.getCode().equals(code))
                .map(ReleaseCodeEnum::getMsg)
                .findFirst()
                .orElse(null);
    }
}
