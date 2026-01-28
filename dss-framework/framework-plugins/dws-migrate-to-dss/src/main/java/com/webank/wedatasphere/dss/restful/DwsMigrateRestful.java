package com.webank.wedatasphere.dss.restful;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.DWSAPIClient;
import com.webank.wedatasphere.dss.bean.DWSProject;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.common.utils.IoUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestImportOrchestrator;
import com.webank.wedatasphere.dss.orchestrator.core.DSSOrchestratorContext;
import com.webank.wedatasphere.dss.orchestrator.publish.ImportDSSOrchestratorPlugin;
import com.webank.wedatasphere.dss.service.DSSFlowExportService;
import com.webank.wedatasphere.dss.service.OrcMetaGenerator;
import com.webank.wedatasphere.dss.service.WorkflowMetaGenerator;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/10/26
 */
@RequestMapping(path = "/dss/framework/orchestrator/dwsmigrate", produces = {"application/json"})
@RestController
public class DwsMigrateRestful {
    private static final Logger LOGGER= LoggerFactory.getLogger(DwsMigrateRestful.class);
    private static final String DWS_URL = CommonVars.apply("wds.dws.http.url", "http://127.0.0.1:20817").getValue();

    @Autowired
    private DSSOrchestratorContext orchestratorContext;
    @Autowired
    @Qualifier("orchestratorBmlService")
    private BMLService bmlService;

    @RequestMapping(path = "migrateProject", method = RequestMethod.GET)
    public Message importOldDSSProject(HttpServletRequest req,
                                       @RequestParam("dwsCookie") String dwsCookie,
                                       @RequestParam(required = false, name = "projectName") String projectName,
                                       @RequestParam(required = false, name = "projectID") Long projectID) throws Exception {

        String userName = SecurityFilter.getLoginUsername(req);
        //调用工具类生产label
        List<DSSLabel> dssLabelList = Lists.newArrayList(new EnvDSSLabel("dev"));
        Workspace workspace = SSOHelper.getWorkspace(req);
        DWSAPIClient client = new DWSAPIClient(DWS_URL, dwsCookie);
        String outputDir = IoUtils.generateIOPath(userName, projectName, "");

        DSSFlowExportService dssFlowExportService = new DSSFlowExportService(new OrcMetaGenerator(), new WorkflowMetaGenerator(), client, outputDir);
        DWSProject dwsProject= client.getAllProject().stream().filter(e -> projectName.equals(e.getProjectName())).findFirst().orElse(null);
        if(dwsProject==null){
           return  Message.error("dws中不存在同名工程：" + projectName + ",无法迁移");
        }
        LOGGER.info("begin to export project from dws.user:{}  projectName {} ",userName,projectName);
        List<String> orcZips = dssFlowExportService.exportWholeProjectFlows(dwsProject.getProjectName(), dwsProject.getProjectTaxonomyID(), dwsProject.getProjectVersionID());
        LOGGER.info("export and convert successfully, now  begin to import. user:{}  projectName {} ",userName,projectName);

        orcZips.forEach( orcZipPath->{

            String fileName = projectName + new File(new File(orcZipPath).getParent()).getName();
            InputStream inputStream = bmlService.readLocalResourceFile(userName, orcZipPath);
            // upload会负责把inputStream关闭
            BmlResource resultMap = bmlService.upload(userName, inputStream, fileName, projectName);
            DSSOrchestratorVersion dssOrchestratorVersion;
            try {
                RequestImportOrchestrator importRequest = new RequestImportOrchestrator(userName, projectName,
                        projectID, resultMap.getResourceId(),
                        resultMap.getVersion(), null, dssLabelList, workspace);
                dssOrchestratorVersion = orchestratorContext.getDSSOrchestratorPlugin(ImportDSSOrchestratorPlugin.class).importOrchestrator(importRequest);
                AuditLogUtils.printLog(userName, workspace.getWorkspaceId(), workspace.getWorkspaceName(), TargetTypeEnum.ORCHESTRATOR,
                        projectID, projectName, OperateTypeEnum.CREATE, dssOrchestratorVersion);
            } catch (Exception e) {
                LOGGER.error("Import orchestrator failed for ", e);
                throw new RuntimeException( "Import orchestrator failed for " + e.getMessage());
            }
        });
        return  Message.ok("迁移完成");
    }
}
