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
package com.webank.wedatasphere.dss.git.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class GitServerThreadPool {
    private final Map<String, ExecutorService> taskExecutors = new HashMap<>();
    private final int POOL_SIZE;

    public GitServerThreadPool(int num) {
        this.POOL_SIZE = num;
        // 初始化每个任务的ExecutorService
        for (int i = 0; i < POOL_SIZE; i++) {
            taskExecutors.put("TaskExecutor_" + i, Executors.newSingleThreadExecutor());
        }
    }

    public <T> Future<T> submitTask(String taskName, Callable<T> task) {
        // 根据任务名称选择对应的ExecutorService，这里简化为根据hashCode选取
        int index = Math.abs(taskName.hashCode()) % POOL_SIZE;
        ExecutorService executorService = taskExecutors.get("TaskExecutor_" + index);
        return executorService.submit(task);
    }

    public void shutdown() {
        // 关闭所有ExecutorService
        taskExecutors.values().forEach(ExecutorService::shutdown);
    }

}
