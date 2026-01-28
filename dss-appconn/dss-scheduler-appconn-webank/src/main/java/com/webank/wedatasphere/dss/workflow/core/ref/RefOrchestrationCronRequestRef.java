package com.webank.wedatasphere.dss.workflow.core.ref;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationCronRequestRef<R extends RefOrchestrationCronRequestRef<R>> extends RefOrchestrationContentRequestRef<R> {

    default String getScheduleTime() {
        return (String) getParameter("scheduleTime");
    }

    default R setScheduleTime(String scheduleTime) {
        setParameter("scheduleTime", scheduleTime);
        return (R) this;
    }

    default String getAlarmEmails() {
        return (String) getParameter("alarmEmails");
    }

    default R setAlarmEmails(String alarmEmails) {
        setParameter("alarmEmails", alarmEmails);
        return (R) this;
    }

    default String getAlarmLevel() {
        return (String) getParameter("alarmLevel");
    }

    default R setAlarmLevel(String alarmLevel) {
        setParameter("alarmLevel", alarmLevel);
        return (R) this;
    }

}
