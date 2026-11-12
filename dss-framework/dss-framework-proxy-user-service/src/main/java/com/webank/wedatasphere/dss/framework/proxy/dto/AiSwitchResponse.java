package com.webank.wedatasphere.dss.framework.proxy.dto;

import com.google.gson.annotations.SerializedName;

/**
 * 轻量响应 DTO：仅解析 Linkis governanceStationAdmin 接口中本需求消费的字段。
 *
 * 设计依据（docs/1.26.0/design/REQ-01_新增AI用户切换_设计.md §1.1.1 / §1.3.2，OPT-D1 定论）：
 * 不复用 dss-framework-compute-resource-manager-webank 的 GovernanceStationAdminResponse，
 * 避免框架模块间新增编译依赖；Gson 反序列化对未知字段容忍、缺失字段返回 null。
 *
 * Linkis 侧契约：LinkisV2.1.7 REQ-01（release_v2.1.7），data.aiSwitch 为 JSON 原生布尔，
 * 取自 linkis.ai.user.enable（默认 false，热加载）；既有字段（admin/historyAdmin/deptAdmin/
 * canResultSet/errorMsgTip）由 Gson 自动忽略。
 *
 * @since 1.26.0
 */
public class AiSwitchResponse {

    private int status;

    private String message;

    private Data data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        /**
         * 集群级 _ai 用户能力开关（linkis.ai.user.enable）。
         * null = 响应缺失该字段（部署的 Linkis 版本低于 release_v2.1.7），按"未开启"降级处理。
         */
        @SerializedName("aiSwitch")
        private Boolean aiSwitch;

        public Boolean getAiSwitch() {
            return aiSwitch;
        }

        public void setAiSwitch(Boolean aiSwitch) {
            this.aiSwitch = aiSwitch;
        }
    }
}
