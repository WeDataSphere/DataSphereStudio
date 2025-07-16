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
package com.webank.wedatasphere.dss.common.utils;

/**
 * get cron parameter
 */
public class AssembleCronUtils {

    /**
     * 该方法可以根据传入的数据，将其转换成cron表达式，最小周期间隔为1s，最大为每天零点执行一次
     * @param value cron表达式的执行周期
     * @return cron表达式
     */
    public static String getCron(Integer value) {

        if (value < 60) {
            // 每隔多少秒执行一次，小于60s
            return "0/" + value + " * * * * ?";
        } else if (value < 3600) {
            // 每隔多少分钟执行一次，小于60min
            return "0 0/" + value / 60 + " * * * ?";
        } else if (value < 86400){
            // 每隔多少小时执行一次，小于24h
            return "0 0 0/" + value / 60 + " * * ?";
        } else {
            // 每天零点执行一次
            return "0 0 0 * * ?";
        }
    }

}
