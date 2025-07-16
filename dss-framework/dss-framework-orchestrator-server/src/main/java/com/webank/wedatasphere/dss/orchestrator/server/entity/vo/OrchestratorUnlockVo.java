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
package com.webank.wedatasphere.dss.orchestrator.server.entity.vo;

public class OrchestratorUnlockVo extends CommonOrchestratorVo {
    /**
     * 已锁定用户
     */
    private String lockOwner;
    /**
     * 提示信息
     */
    private String confirmMessage;
    /**
     * 0：解锁成功，1：需用户二次确认解锁
     */
    private int status;

    public OrchestratorUnlockVo(String lockOwner, String confirmMessage, int status) {
        this.lockOwner = lockOwner;
        this.confirmMessage = confirmMessage;
        this.status = status;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
