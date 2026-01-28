package com.webank.wedatasphere.dss.workflow.core.ref;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationActiveFlagRequestRef<R extends RefOrchestrationActiveFlagRequestRef<R>>
    extends RefOrchestrationContentRequestRef<R> {

    default String getActiveFlag() {
        return (String) getParameter("activeFlag");
    }

    default R setActiveFlag(String activeFlag) {
        setParameter("activeFlag", activeFlag);
        return (R) this;
    }

}
