package com.webank.wedatasphere.dss.workflow.core.ref;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder.ExternalResponseRefBuilder;
import com.webank.wedatasphere.dss.workflow.core.ref.impl.RefProxyUserFetchResponseRefImpl;
import java.util.List;

/**
 * Created by enjoyyin on 2022/9/8.
 */
public interface RefProxyUserFetchResponseRef extends ResponseRef {
    /**
     * 获取wtss的运维用户
     */
    List<String> getMaintenanceUserList();
    /**
     * 获取wtss的代理用户
     */
    List<String> getProxyExecuteUserList();

    static Builder newBuild() {
        return new Builder();
    }

    class Builder extends ExternalResponseRefBuilder<Builder, RefProxyUserFetchResponseRef> {

        /**
         * wtss 下划线开头的运维账号
         */
        private List<String> maintenanceUserList;
        /**
         * hduser开头的代理用户账号
         */
        private List<String> proxyExecuteUserList;

        public Builder setMaintenanceUserList(List<String> maintenanceUserList) {
            this.maintenanceUserList = maintenanceUserList;
            return this;
        }

        public Builder setProxyExecuteUserList(List<String> proxyExecuteUserList) {
            this.proxyExecuteUserList = proxyExecuteUserList;
            return this;
        }

        @Override
        protected RefProxyUserFetchResponseRef createResponseRef() {
            return new RefProxyUserFetchResponseRefImpl(responseBody, status, errorMsg, responseMap, maintenanceUserList,proxyExecuteUserList);
        }
    }

}
