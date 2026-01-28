package com.webank.wedatasphere.dss.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.DWSAPIClient;
import com.webank.wedatasphere.dss.bean.DWSFlow;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSCommonNode;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSFlowJson;
import com.webank.wedatasphere.dss.bean.flowcomponent.DSSNodeResource;
import com.webank.wedatasphere.dss.common.entity.IOType;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.IoUtils;
import com.webank.wedatasphere.dss.common.utils.ZipHelper;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.orchestrator.core.exception.DSSOrchestratorErrorException;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlowRelation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.common.utils.ZipHelper.zip;

/**
 * Author: xlinliu
 * Date: 2023/10/24
 */
public class DSSFlowExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSSFlowExportService.class);
    private OrcMetaGenerator orcMetaGenerator;
    private WorkflowMetaGenerator workflowMetaGenerator;
    private DWSAPIClient dwsapiClient;
    private String exportDir;

    public DSSFlowExportService(OrcMetaGenerator orcMetaGenerator, WorkflowMetaGenerator workflowMetaGenerator, DWSAPIClient dwsapiClient, String exportDir) {
        this.orcMetaGenerator = orcMetaGenerator;
        this.workflowMetaGenerator = workflowMetaGenerator;
        this.dwsapiClient = dwsapiClient;
        this.exportDir = exportDir;
    }

    public List<String> exportWholeProjectFlows(String projectName, Long projectTaxonomyID, Long projectVersionID) throws Exception {

        List<DWSFlow> flowMetas = dwsapiClient.getAllFlowMetaInProject(projectName, projectTaxonomyID, projectVersionID, false);
        Map<Long, DSSFlow> dssFlows = new HashMap<>();
        Map<Long, DSSFlowJson> dssFlowJsons = new HashMap<>();
        Map<Long, DSSOrchestratorInfo> dssOrchestratorInfos = new HashMap<>();

        for (DWSFlow flowMeta : flowMetas) {
            DWSFlow flow = dwsapiClient.getFlowContent(flowMeta);
            DSSFlowJson dssFlowJson;
            if(StringUtils.isBlank(flow.getJson())){
                dssFlowJson = new DSSFlowJson();
            }else {
                JsonObject flowObj = new JsonParser().parse(flow.getJson()).getAsJsonObject();
                dssFlowJson = DSSFlowJson.fromDwsFlowJson(flowObj, flowMeta.getUpdator());
            }
            DSSFlow dssFlow = DSSFlowParser.fromDwsFlow(flow.getJsonObject());
            dssFlowJsons.put(dssFlow.getId(), dssFlowJson);
            DSSOrchestratorInfo orchestratorInfo = DSSFlowParser.fromDwsOrc(flow.getJsonObject());
            dssOrchestratorInfos.put(dssFlow.getId(), orchestratorInfo);
            dssFlows.put(dssFlow.getId(), dssFlow);
        }
        List<DSSFlowRelation> relations = DSSFlowParser.fromDwsRelation(dssFlowJsons);

        List<DSSFlow> rootFlows = dssFlows.values().stream().filter(DSSFlow::getRootFlow).collect(Collectors.toList());
        List<String> orcPaths = new ArrayList<>(rootFlows.size());
        for (DSSFlow rootFlow : rootFlows) {
            List<DSSFlow> singleTreeDssFlowList = getSingleFlowTree(rootFlow, relations, dssFlows);
            Set<Long> singleTreeFlowIds=singleTreeDssFlowList.stream().map(DSSFlow::getId).collect(Collectors.toSet());
            DSSOrchestratorInfo dssOrchestratorInfo = dssOrchestratorInfos.get(rootFlow.getId());
            Map<Long, DSSFlowJson> singleTreeFlowIdFlowJsonMap = singleTreeFlowIds.stream()
                    .collect(Collectors.toMap(Function.identity(), dssFlowJsons::get));
            List<DSSFlowRelation> singleTreeFlowRelations = relations.stream().filter(e->singleTreeFlowIds.contains(e.getFlowID())).collect(Collectors.toList());
            //挨个儿导出编排
            String orcZipPath= exportOrchestrator(exportDir, dssOrchestratorInfo,projectName, rootFlow,
                    singleTreeFlowIdFlowJsonMap, singleTreeDssFlowList, singleTreeFlowRelations);
            orcPaths.add(orcZipPath);
            LOGGER.info("dws orc zip path:{}", orcZipPath);
        }
        return orcPaths;
    }

    private List<DSSFlow> getSingleFlowTree(DSSFlow rootFlow, List<DSSFlowRelation> relations, Map<Long, DSSFlow> dssFlows) {
        List<DSSFlow> result = new ArrayList<>();
        result.add(rootFlow);
        List<DSSFlow> subFlows = relations.stream()
                .filter(e -> rootFlow.getId().equals(e.getParentFlowID())).map(DSSFlowRelation::getFlowID)
                .map(dssFlows::get).collect(Collectors.toList());
        for (DSSFlow subFlow : subFlows) {
            result.addAll(getSingleFlowTree(subFlow, relations, dssFlows));
        }
        return result;
    }


    /**
     * 导出单个编排
     */
    public String exportOrchestrator(String basePath, DSSOrchestratorInfo dssOrchestratorInfo, String projectName,
                                     DSSFlow rootFlow, Map<Long, DSSFlowJson> flowIdFlowJsonMap,
                                     List<DSSFlow> dssFlowList, List<DSSFlowRelation> flowRelations) throws Exception {

        String orcSavedPath = basePath +File.separator+ dssOrchestratorInfo.getName()+File.separator+"default_orc";
        try {
            Files.createDirectories(Paths.get(orcSavedPath).toAbsolutePath().normalize());
            //标记当前导出为project导出
            IoUtils.generateIOType(IOType.ORCHESTRATOR, orcSavedPath);
            //标记当前导出环境env
            IoUtils.generateIOEnv(orcSavedPath);
            orcMetaGenerator.export(dssOrchestratorInfo, orcSavedPath);
        } catch (IOException e) {
            LOGGER.error("Failed to export metaInfo in orchestrator server for orc({}) .", dssOrchestratorInfo.getName(), e);
            DSSExceptionUtils.dealErrorException(60099, "Failed to export metaInfo in orchestrator server.", e, DSSOrchestratorErrorException.class);
        }
        LOGGER.info("开始导出Orchestrator: {}.", dssOrchestratorInfo.getName());
        String flowExportSaveBasePath = orcSavedPath + File.separator + projectName;
        String flowZipPathStr = exportFlowInfo(flowExportSaveBasePath, rootFlow, flowIdFlowJsonMap, dssFlowList, flowRelations);
        String targetFlowFileName = orcSavedPath + File.separator + "orc_flow.zip";
        Path flowZipPath = Paths.get(flowZipPathStr);
        Path targetFlowPath = Paths.get(targetFlowFileName);
        try {
            Files.move(flowZipPath, targetFlowPath);
        } catch (IOException e) {
            LOGGER.error("move flow zip file failed,path:{}", flowZipPath);
            //do nothing
        }
        //打包导出工程
        return zip(orcSavedPath);

    }

    /**
     * 导出单个工作流
     */
    public String exportFlowInfo(String flowExportSaveBasePath, DSSFlow rootFlow, Map<Long, DSSFlowJson> flowIdFlowJsonMap,
                                 List<DSSFlow> dssFlowList, List<DSSFlowRelation> flowRelations) throws Exception {
        Map<Long, List<Long>> flowIdDependencyMap = flowRelations.stream()
                .collect(
                        Collectors.groupingBy(DSSFlowRelation::getParentFlowID,
                                Collectors.mapping(DSSFlowRelation::getFlowID, Collectors.toList())));
        Map<Long, DSSFlow> flowIdDSSFlowMap = dssFlowList.stream().collect(Collectors.toMap(DSSFlow::getId, Function.identity()));

        fillChildren(rootFlow, flowIdDependencyMap, flowIdDSSFlowMap);
        //标记当前导出为project导出
        IoUtils.generateIOType(IOType.FLOW, flowExportSaveBasePath);
        //标记当前导出环境
        IoUtils.generateIOEnv(flowExportSaveBasePath);
        workflowMetaGenerator.exportFlowBaseInfo(dssFlowList, flowRelations, flowExportSaveBasePath);
        LOGGER.info( "开始导出Flow：" + rootFlow.getName());
        for (DSSFlow dssFlow : dssFlowList) {
            if (dssFlow.getRootFlow()) {
                String savePath = flowExportSaveBasePath + File.separator + dssFlow.getName() + File.separator + dssFlow.getName() + ".json";
                //导出工作流json文件
                DSSFlowJson flowJson = flowIdFlowJsonMap.get(dssFlow.getId());
                String flowJsonStr = DSSCommonUtils.COMMON_GSON.toJson(flowJson);
                writeContentToPath(flowJsonStr, savePath);
                exportFlowResources( flowExportSaveBasePath, flowJson, dssFlow.getName());
                exportAllSubFlows( dssFlow, flowExportSaveBasePath, flowIdFlowJsonMap);
            }
        }

        //打包导出工程
        return ZipHelper.zip(flowExportSaveBasePath);
    }

    private void fillChildren(DSSFlow parentFlow, Map<Long, List<Long>> flowIdDependencyMap, Map<Long, DSSFlow> flowIdDSSFlowMap) {
        if(!flowIdDependencyMap.containsKey(parentFlow.getId())){
            //没有子节点
            return;
        }
        List<Long> subFlowIds = flowIdDependencyMap.get(parentFlow.getId());
        for (Long subFlowId : subFlowIds) {
            DSSFlow subDssFlow = flowIdDSSFlowMap.get(subFlowId);
            parentFlow.addChildren(subDssFlow);
            fillChildren(subDssFlow, flowIdDependencyMap, flowIdDSSFlowMap);
        }
    }

    private void writeContentToPath(String content, String path) {
        try (OutputStream outStream = IoUtils.generateExportOutputStream(path)) {
            outStream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("write flow json failed", e);
            throw new RuntimeException(e);
        }
    }

    private void exportAllSubFlows(DSSFlow dssFlowParent,
                                   String projectExportBasePath, Map<Long, DSSFlowJson> flowIdFlowJsonMap) {
        List<? extends DSSFlow> subFlows = dssFlowParent.getChildren();
        if (subFlows != null) {
            for (DSSFlow subFlow : subFlows) {
                String savePath = projectExportBasePath + File.separator + subFlow.getName() + File.separator + subFlow.getName() + ".json";
                //导出子flow的json文件
                DSSFlowJson flowJson = flowIdFlowJsonMap.get(subFlow.getId());
                String flowJsonStr = DSSCommonUtils.COMMON_GSON.toJson(flowJson);
                writeContentToPath(flowJsonStr, savePath);
                exportFlowResources( projectExportBasePath, flowJson, subFlow.getName());
                exportAllSubFlows( subFlow, projectExportBasePath, flowIdFlowJsonMap);

            }
        }
    }

    /**
     * 下载节点
     *
     * @param dssNode
     * @param savePath
     */
    public void downloadNodeResourceToLocal(DSSCommonNode dssNode, String savePath) {
        List<DSSNodeResource> resources = dssNode.getResources();
        if (resources != null) {
            resources.forEach(x -> {
                // TODO: 2020/6/9 防止前台传的 resources：{{}],后期要去掉
                if (x.getResourceId() != null && x.getFileName() != null && x.getVersion() != null) {
                    String nodeResourcePath = savePath + File.separator + x.getResourceId() + "_" + x.getVersion() + ".re";
                    dwsapiClient.downloadBmlToLocalPath(x.getResourceId(), x.getVersion(), nodeResourcePath);
                } else {
                    LOGGER.warn("Illegal resource information");
                    LOGGER.warn(",nodeId:{},nodeName:{},fileName:{},version:{},resourceId:{}", dssNode.getId(), dssNode.getTitle(), x.getFileName(), x.getVersion(), x.getResourceId());
                }
            });
        }
    }

    private String genWorkFlowExportDir(String projectExportPath, String flowName) {
        return projectExportPath + File.separator + flowName;
    }

    private String downloadFlowResourceFromBml(String resourceId, String version, String savePath) {
        String flowResourcePath = savePath + File.separator + resourceId + ".re";
        return dwsapiClient.downloadBmlToLocalPath(resourceId, version, flowResourcePath);
    }

    public void exportFlowResources(String projectSavePath, DSSFlowJson flowJson, String flowName) {
        String workFlowExportPath = genWorkFlowExportDir(projectSavePath, flowName);
        String workFlowResourceSavePath = workFlowExportPath + File.separator + "resource";

        //导出工作流资源文件
        for (Map<String, Object> resource : flowJson.getResources()) {
            downloadFlowResourceFromBml( (String) resource.get("resourceId"), (String) resource.get("version"), workFlowResourceSavePath);
        }

        //导出工作流节点资源文件,工作流节点appconn文件
        List<DSSCommonNode> nodes = flowJson.getNodes();
        if (nodes != null) {
            for (DSSCommonNode node : nodes) {
                downloadNodeResourceToLocal(node, workFlowResourceSavePath);
            }
        }


    }
}
