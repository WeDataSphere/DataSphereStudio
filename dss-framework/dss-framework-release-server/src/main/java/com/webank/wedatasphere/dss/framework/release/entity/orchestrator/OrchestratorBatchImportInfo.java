package com.webank.wedatasphere.dss.framework.release.entity.orchestrator;

import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;

import java.util.List;

/**
 * 批量导入编排的信息记录
 * Author: xlinliu
 * Date: 2022/9/14
 */
public class OrchestratorBatchImportInfo {
    /**
     * 来源编排信息
     */
    List<OrchestratorDetail> from;
    /**
     * 导入后的编排信息
     */
    List<OrchestratorReleaseInfo> to;

    public OrchestratorBatchImportInfo() {
    }

    public OrchestratorBatchImportInfo(List<OrchestratorDetail> from, List<OrchestratorReleaseInfo> to) {
        this.from = from;
        this.to = to;
    }

    public List<OrchestratorDetail> getFrom() {
        return from;
    }

    public void setFrom(List<OrchestratorDetail> from) {
        this.from = from;
    }

    public List<OrchestratorReleaseInfo> getTo() {
        return to;
    }

    public void setTo(List<OrchestratorReleaseInfo> to) {
        this.to = to;
    }
}
