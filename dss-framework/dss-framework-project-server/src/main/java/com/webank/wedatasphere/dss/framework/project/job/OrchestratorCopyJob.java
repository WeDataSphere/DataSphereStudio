package com.webank.wedatasphere.dss.framework.project.job;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.protocol.ResponseImportOrchestrator;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.common.exception.DSSFrameworkErrorException;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.OrchestratorCopyVO;
import com.webank.wedatasphere.dss.framework.release.entity.export.ExportResult;
import com.webank.wedatasphere.dss.framework.release.utils.ReleaseConf;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestCreateOrchestrator;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestImportOrchestrator;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestWorkflowValidNode;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseWorkflowValidNode;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class OrchestratorCopyJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorCopyJob.class);

    private OrchestratorCopyVO orchestratorCopyVO;
    protected ProjectCopyEnv projectCopyEnv;
    private static final String ORC_FRAMEWORK_NAME = ReleaseConf.ORCHESTRATOR_APPCONN_NAME.getValue();
    private final Sender workflowSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getWorkflowSender();

    /**
     * 导出编排模式
     *
     * @return
     * @throws ErrorException
     */
    public ExportResult exportOrc() throws Exception {
        Long projectId = orchestratorCopyVO.getProjectDO().getId();
        Long orchestratorId = orchestratorCopyVO.getOrchestrator().getId();
        String projectName = orchestratorCopyVO.getProjectDO().getName();
        String username = orchestratorCopyVO.getUsername();
        //先判断工作流是否含有节点
        RequestWorkflowValidNode requestWorkflowValidNode = new RequestWorkflowValidNode();
        requestWorkflowValidNode.setOrcId(orchestratorId);
        ResponseWorkflowValidNode responseWorkflowValidNode = RpcAskUtils.processAskException(workflowSender.ask(requestWorkflowValidNode), ResponseWorkflowValidNode.class, RequestWorkflowValidNode.class);
        //如果没有节点直接调用接口创建即可
        if (responseWorkflowValidNode.getNodeCount() == 0) {
            DSSOrchestratorInfo orchestratorInfo = orchestratorCopyVO.getOrchestrator();
            orchestratorInfo.setProjectId(orchestratorCopyVO.getCopyProjectId());
            orchestratorInfo.setId(null);
            RequestCreateOrchestrator requestCreateOrchestrator = new RequestCreateOrchestrator(username, orchestratorCopyVO.getWorkspace(),
                    orchestratorCopyVO.getCopyProjectName(), orchestratorCopyVO.getCopyProjectId(), orchestratorInfo.getDescription(), orchestratorInfo,
                    Collections.singletonList(new EnvDSSLabel(DSSCommonUtils.ENV_LABEL_VALUE_DEV)));
            try {
                DSSSenderServiceFactory.getOrCreateServiceInstance().getOrcSender().ask(requestCreateOrchestrator);
            }catch (Exception e){
                LOGGER.error("create orchestrator failed when copy project with empty workflow ",e);
                // ignore error,do nothing
            }
            return null;
        }

        //如果含有节点的工作流
        ExportResult exportResult = null;
        try {
            exportResult = this.projectCopyEnv.getExportService().export(orchestratorCopyVO.getUsername(), projectId,
                    new OrchestratorInfo(orchestratorId, responseWorkflowValidNode.getOrcVersionId()),
                    projectName,
                    orchestratorCopyVO.getDssLabel(),
                    orchestratorCopyVO.getWorkspace());
        } catch (Exception e) {
            //保存错误信息
            String errorMsg = "Export Orchestrator " + orchestratorCopyVO.getOrchestrator().getName() + " error, the reason is " + e.getMessage();
            if (errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 999);
            }
            projectCopyEnv.getDssProjectCopyTaskMapper().updateErrorMsgById(errorMsg, orchestratorCopyVO.getOrchestrator().getName(),1, orchestratorCopyVO.getCopyTaskId());

            LOGGER.error("ExportOrcError: projectName:{},copyProjectName:{},orcName:{},currentNum:{},sumCount:{}, Exception:",
                    orchestratorCopyVO.getProjectDO().getName(), orchestratorCopyVO.getCopyProjectName(), orchestratorCopyVO.getOrchestrator().getName(),
                    orchestratorCopyVO.getCurrentNum(), orchestratorCopyVO.getSumCount(), e);
        }
        return exportResult;
    }

    /**
     * 导入编排模式
     *
     * @param exportResult
     * @throws ErrorException
     */
    public void importOrc(ExportResult exportResult) throws ErrorException {
        LOGGER.info("Begin to import orc for project {} and orc resource is {}", orchestratorCopyVO.getProjectDO().getName(), exportResult.getBmlResource());
        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(ORC_FRAMEWORK_NAME);
        DevelopmentIntegrationStandard standard = ((OnlyDevelopmentAppConn) appConn).getOrCreateDevelopmentStandard();
        if (standard == null) {
            LOGGER.error("developStandard is null, can not do import operation");
            DSSExceptionUtils.dealErrorException(60096, "developStandard is null, can not do import operation", DSSFrameworkErrorException.class);
        }
        List<DSSLabel> dssLabelList = Collections.singletonList(orchestratorCopyVO.getDssLabel());
        DSSOrchestratorInfo orchestrator = orchestratorCopyVO.getOrchestrator();
        DSSProjectDO projectDO = orchestratorCopyVO.getProjectDO();
        Workspace workspace = orchestratorCopyVO.getWorkspace();
        BmlResource bmlResource = exportResult.getBmlResource();
            try {
                LOGGER.info("CopyOrc Start: orcName:{},currentNum:{},count:{}",
                        orchestrator.getName(), orchestratorCopyVO.getCurrentNum(), orchestratorCopyVO.getSumCount());
                RequestImportOrchestrator importRequest = new RequestImportOrchestrator(orchestratorCopyVO.getUsername(),
                        projectDO.getName(), projectDO.getId(), bmlResource.getResourceId(),
                        bmlResource.getVersion(), orchestrator.getName(), dssLabelList, workspace);
                importRequest.setCopyProjectId(orchestratorCopyVO.getCopyProjectId());
                importRequest.setCopyProjectName(orchestratorCopyVO.getCopyProjectName());
                Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getOrcSender(dssLabelList);
                ResponseImportOrchestrator importResponse = RpcAskUtils.processAskException(sender.ask(importRequest),
                        ResponseImportOrchestrator.class, RequestImportOrchestrator.class);
                LOGGER.info("CopyOrc Success: orcName:{},orcId:{},currentNum:{},count:{}",
                        orchestrator.getName(), importResponse.orcId(), orchestratorCopyVO.getCurrentNum(), orchestratorCopyVO.getSumCount());
            } catch (Exception e) {
                //保存错误信息
                String errorMsg = "Import Orchestrator " + orchestrator.getName() + " error, the reason is " + e.getMessage();
                if (errorMsg.length() > 1000) {
                    errorMsg = errorMsg.substring(0, 999);
                }
                projectCopyEnv.getDssProjectCopyTaskMapper().updateErrorMsgById(errorMsg, orchestratorCopyVO.getOrchestrator().getName(), 1, orchestratorCopyVO.getCopyTaskId());

                LOGGER.error("importOrcError: projectName:{},copyProjectName:{},orcName:{},currentNum:{},sumCount:{}, Exception:",
                        projectDO.getName(), orchestratorCopyVO.getCopyProjectName(), orchestrator.getName(),
                        orchestratorCopyVO.getCurrentNum(), orchestratorCopyVO.getSumCount(), e);

            }

        LOGGER.info("End to import orc for project {} and orc resource is {}", orchestratorCopyVO.getProjectDO().getName(), exportResult.getBmlResource());
    }

    public OrchestratorCopyVO getOrchestratorCopyVO() {
        return orchestratorCopyVO;
    }

    public void setOrchestratorCopyVO(OrchestratorCopyVO orchestratorCopyVO) {
        this.orchestratorCopyVO = orchestratorCopyVO;
    }

    public ProjectCopyEnv getProjectCopyEnv() {
        return projectCopyEnv;
    }

    public void setProjectCopyEnv(ProjectCopyEnv projectCopyEnv) {
        this.projectCopyEnv = projectCopyEnv;
    }
}
