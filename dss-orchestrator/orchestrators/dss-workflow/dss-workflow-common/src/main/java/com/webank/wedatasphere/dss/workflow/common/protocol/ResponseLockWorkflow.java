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
package com.webank.wedatasphere.dss.workflow.common.protocol;


public class ResponseLockWorkflow {
    public static final int LOCK_SUCCESS = 0;
    public static final int LOCK_FAILED = 1;

    private int unlockStatus;
    private String lockOwner;

    public ResponseLockWorkflow(int unlockStatus, String lockOwner) {
        this.unlockStatus = unlockStatus;
        this.lockOwner = lockOwner;
    }

    public ResponseLockWorkflow() {
    }

    public int getUnlockStatus() {
        return unlockStatus;
    }

    public void setUnlockStatus(int unlockStatus) {
        this.unlockStatus = unlockStatus;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }
}
