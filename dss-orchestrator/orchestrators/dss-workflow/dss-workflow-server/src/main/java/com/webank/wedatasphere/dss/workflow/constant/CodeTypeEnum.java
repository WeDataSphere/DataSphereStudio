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
package com.webank.wedatasphere.dss.workflow.constant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CodeTypeEnum {


    // 数据开发
    HIVE("Hive","linkis.hive.hql"),
    SPARK("Spark","linkis.spark.sql"),
    PYSPARK("PySpark","linkis.spark.py"),

    // 信号节点
    DATACHECKER("Datachecker","linkis.appconn.datachecker"),

    EVENTRECEIVER("Eventreceiver","linkis.appconn.eventchecker.eventreceiver"),

    EVENTRECEIVERWTSS("EventreceiverWTSS","wtss.eventchecker.receiver"),

    EVENTSENDER("Eventsender","linkis.appconn.eventchecker.eventsender"),

    EVENTSENDERWTSS("EventsenderWTSS","wtss.eventchecker.sender"),

    ;


    private String name;

    private String engineType;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    CodeTypeEnum(String name,String engineType) {
        this.name = name;
        this.engineType = engineType;
    }



    private static final List<CodeTypeEnum> dataNodeList = Stream.of(
            CodeTypeEnum.HIVE,
            CodeTypeEnum.SPARK,
            CodeTypeEnum.PYSPARK
    ).collect(Collectors.toList());


    private static final List<CodeTypeEnum>  signalNodeList = Stream.of(
            CodeTypeEnum.DATACHECKER,
            CodeTypeEnum.EVENTRECEIVER,
            CodeTypeEnum.EVENTSENDER,
            CodeTypeEnum.EVENTRECEIVERWTSS,
            CodeTypeEnum.EVENTSENDERWTSS
    ).collect(Collectors.toList());


    public static boolean isMatchSignalNode(String engineType){

        return signalNodeList.stream().anyMatch(codeTypeEnum -> codeTypeEnum.getEngineType().equals(engineType));
    }

    public static boolean isMatchDataNode(String engineType){

        return dataNodeList.stream().anyMatch(codeTypeEnum -> codeTypeEnum.getEngineType().equals(engineType));
    }

    public static boolean isMatchDataOrSignalNode(String engineType){
        return isMatchDataNode(engineType) || isMatchSignalNode(engineType);

    }

}
