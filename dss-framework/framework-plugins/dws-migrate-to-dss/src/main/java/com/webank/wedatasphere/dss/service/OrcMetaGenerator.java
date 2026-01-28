package com.webank.wedatasphere.dss.service;

import com.webank.wedatasphere.dss.common.utils.IoUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.publish.io.export.MetaWriter;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlowRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.List;

/**
 * 编排元数据生成
 * Author: xlinliu
 * Date: 2023/10/7
 */
public class OrcMetaGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final String fileName = "meta.txt";



    public void export(DSSOrchestratorInfo dssOrchestratorInfo, String savePath) throws IOException {

        try (
                OutputStream outputStream = generateOutputStream(savePath)
        ) {
            exportOrchestratorBaseInfo(dssOrchestratorInfo, outputStream);
        }
    }


    public void exportFlowBaseInfo(List<DSSFlow> allDSSFlows, List<DSSFlowRelation> allFlowRelations, String savePath) throws IOException {

        try (
                OutputStream outputStream = generateOutputStream(savePath)
        ) {
            exportFlowBaseInfo(allDSSFlows, outputStream);
            exportFlowRelation(allFlowRelations, outputStream);
        }

    }

    private void exportFlowBaseInfo(List<DSSFlow> DSSFlows, OutputStream outputStream) throws IOException {

        MetaWriter.of("dss_flow", DSSFlow.class).data(DSSFlows).write(outputStream);

    }

    private void exportFlowRelation(List<DSSFlowRelation> flowRelations, OutputStream outputStream) throws IOException {

        MetaWriter.of("dss_workflow_relation", DSSFlowRelation.class).data(flowRelations).write(outputStream);

    }


    private OutputStream generateOutputStream(String basePath) throws IOException {
        return IoUtils.generateExportOutputStream(basePath + File.separator + fileName);
    }


    private void exportOrchestratorBaseInfo(DSSOrchestratorInfo dssOrchestratorInfo,OutputStream outputStream) throws IOException {

        MetaWriter.of("dss_orchestrator", DSSOrchestratorInfo.class).data(dssOrchestratorInfo).write(outputStream);

    }
}
