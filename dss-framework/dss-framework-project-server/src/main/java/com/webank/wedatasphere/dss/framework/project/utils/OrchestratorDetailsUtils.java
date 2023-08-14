package com.webank.wedatasphere.dss.framework.project.utils;

import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author: jinyangrao on 2021/7/2
 * @description:
 */
public class OrchestratorDetailsUtils {

    public static void sortOrchestratorDetailList( List<OrchestratorDetail> orchestratorDetails){
        Collections.sort(orchestratorDetails, (o1, o2) -> {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date o1Time = null;
            Date o2Time= null;
            if(null ==o1.getScheduleTime() && null ==o2.getScheduleTime()){
                return 0;
            }
            if(null == o1.getScheduleTime() && null != o2.getScheduleTime()){
                return 1;
            }
            if(null !=o1.getScheduleTime() && null ==o2.getScheduleTime()){
                return -1;
            }
            try {
                o1Time = df.parse(o1.getScheduleTime());

                o2Time = df.parse(o2.getScheduleTime());
            } catch (Exception e) {
                //部分調度時間為空，不需要處理
            }

            if (o1Time.getTime() >o2Time.getTime()){
                return -1;
            }
            if (o1Time.getTime() == o2Time.getTime()){
                return 0;
            }
            return 1;
        });
    }
}
