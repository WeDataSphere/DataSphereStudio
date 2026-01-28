package com.webank.wedatasphere.dss.service;


import com.webank.wedatasphere.dss.common.utils.IoUtils;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlowRelation;
import com.webank.wedatasphere.dss.workflow.io.export.MetaWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.List;

/**
 * 工作流元数据生成
 * Author: xlinliu
 * Date: 2023/10/7
 */
public class WorkflowMetaGenerator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final String fileName = "meta.txt";


    public void exportFlowBaseInfo(List<DSSFlow> allDSSFlows, List<DSSFlowRelation> allFlowRelations, String savePath) throws IOException {

        try (
                OutputStream outputStream = generateOutputStream(savePath)
        ) {
            exportFlowBaseInfo(allDSSFlows, outputStream);
            exportFlowRelation(allFlowRelations, outputStream);
        }
    }

    private OutputStream generateOutputStream(String basePath) throws IOException {
        return IoUtils.generateExportOutputStream(basePath + File.separator + fileName);
    }

    private void exportFlowBaseInfo(List<DSSFlow> DSSFlows, OutputStream outputStream) throws IOException {

        MetaWriter.of("dss_flow", DSSFlow.class).data(DSSFlows).write(outputStream);

    }

    private InputStream exportFlowBaseInfo(List<DSSFlow> DSSFlows) throws IOException {

        return MetaWriter.of("dss_flow", DSSFlow.class).data(DSSFlows).write();

    }

    private void exportFlowRelation(List<DSSFlowRelation> flowRelations, OutputStream outputStream) throws IOException {

        MetaWriter.of("dss_workflow_relation", DSSFlowRelation.class).data(flowRelations).write(outputStream);

    }

    private InputStream exportFlowRelation(List<DSSFlowRelation> flowRelations) throws IOException {

        return MetaWriter.of("dss_workflow_relation", DSSFlowRelation.class).data(flowRelations).write();

    }
}
