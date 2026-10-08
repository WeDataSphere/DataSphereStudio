package com.webank.wedatasphere.dss.workflow.service.impl;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.dss.common.entity.Resource;
import com.webank.wedatasphere.dss.common.entity.node.DSSEdge;
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.CompareWorkflowResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.CompareWorkflowAndSubFlowResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppAndSubFlowCompare;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAppCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestAppWithSubFlowCompare;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.constant.CompareWorkflow;
import com.webank.wedatasphere.dss.workflow.constant.CompareWorkflowEnum;
import com.webank.wedatasphere.dss.workflow.core.WorkflowFactory;
import com.webank.wedatasphere.dss.workflow.core.entity.Workflow;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNodeEdge;
import com.webank.wedatasphere.dss.workflow.dao.WebankDSSFlowMapper;
import com.webank.wedatasphere.dss.workflow.service.DSSFlowService;
import com.webank.wedatasphere.dss.workflow.service.WebankCompareWorkflowService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebankCompareWorkflowServiceImpl implements WebankCompareWorkflowService {
    private final static Logger log = LoggerFactory.getLogger(WebankCompareWorkflowServiceImpl.class);
    private static final String LINKIS_CONTROL_EMPTY_NODE ="linkis.control.empty" ;

    @Autowired
    WebankDSSFlowMapper webankDSSFlowMapper;
    @Autowired
    DSSFlowService flowService;

    @Override
    public ResponseAppCompare compareWorkflow(RequestAppCompare requestAppCompare) {
        List<Long> ids = Arrays.asList(requestAppCompare.getFirstFlowId(), requestAppCompare.getSecondFlowId());
        List<Workflow> workflows= ids.stream().map(flowService::getFlowWithJsonAndSubFlowsByID)
                .map(flow -> WorkflowFactory.INSTANCE.getJsonToFlowParser().parse( flow))
                .collect(Collectors.toList());
        Workflow firstWorkflow = workflows.get(0);
        Workflow secondWorkflow = workflows.get(1);
        //比较工作流
        List<CompareWorkflowResult> resultList= compareWorkflow(firstWorkflow, secondWorkflow,false);
        ResponseAppCompare responseAppCompare = new ResponseAppCompare();

        responseAppCompare.setList(resultList);
        return responseAppCompare;
    }

    /**
     * 把有变化的节点组装成结果集，包括确定节点的上下游节点信息、填充节点属性等等
     */
    private List<CompareWorkflowResult> getResultList(List<Workflow> workflows) {
        List<CompareWorkflowResult> retList = new ArrayList<>();
        Set<String> nodeIdSet = new HashSet<>();
        Map<String, String> idHistoryNameMap = new HashMap<>();
        for (int i = 0; i < workflows.size(); i++) {
            Workflow workflow = workflows.get(i);
            Map<String, List<DSSEdge>> dssEdgeListMap = new HashMap<>();
            Map<String, CompareWorkflowResult> compareWorkflowResultMap = new HashMap<>();
            List<WorkflowNodeEdge> edges = workflow.getWorkflowNodeEdges();
            for (WorkflowNodeEdge workflowNodeEdge : edges) {
                DSSEdge dssEdge = workflowNodeEdge.getDSSEdge();
                String source = dssEdge.getSource();
                String target = dssEdge.getTarget();
                if (!dssEdgeListMap.containsKey(source)) {
                    dssEdgeListMap.put(source, new ArrayList<>());
                }
                if (!dssEdgeListMap.containsKey(target)) {
                    dssEdgeListMap.put(target, new ArrayList<>());
                }
                dssEdgeListMap.get(source).add(dssEdge);
                dssEdgeListMap.get(target).add(dssEdge);
            }

            for (WorkflowNode wkn : workflow.getWorkflowNodes()) {
                CompareWorkflowResult compareWorkflowResult = new CompareWorkflowResult();
                compareWorkflowResult.setAppId(workflow.getId());
                compareWorkflowResult.setAppName(workflow.getName());

                DSSNode dssNode = wkn.getDSSNode();
                String nodeFlag = (String) dssNode.getJobContent().get(CompareWorkflow.nodeFlag);
                //same状态的节点，不需要记录
                if (nodeFlag == null ||CompareWorkflow.nodeFlag_same.equals(nodeFlag)){
                    continue;
                }
                //只有modify状态的节点才可能重复在2个工作流同时出现，只需要first workflow(旧版本）里node的名字作为history，其他全部用second workflow（新版本）的信息
                if( CompareWorkflow.nodeFlag_modify.equals(nodeFlag)&& !nodeIdSet.contains(dssNode.getId() + nodeFlag)) {
                    nodeIdSet.add(dssNode.getId() + nodeFlag);
                    idHistoryNameMap.put(dssNode.getId(), dssNode.getName());
                    continue;
                }
                Long updateTime;
                String updateUser;
                if(CompareWorkflow.nodeFlag_delete.equals(nodeFlag)){
                    //删除的节点，修改时间和修改人，只能取新工作流的修改时间和修改人
                    updateTime=workflows.get(1).getUpdateTime();
                    updateUser=workflows.get(1).getUpdateUser();
                }else{
                    //老版本的dssnode没有修改时间和修改人信息，都是用工作流信息代替。此处兜底逻辑保留这个设计。
                    updateTime =dssNode.getModifyTime()!=null?dssNode.getModifyTime():workflows.get(1).getUpdateTime();
                    updateUser = dssNode.getModifyUser() != null ? dssNode.getModifyUser() : workflows.get(1).getUpdateUser();
                }
                compareWorkflowResult.setUpdateTime(updateTime);
                compareWorkflowResult.setUpdateUser(updateUser);
                compareWorkflowResult.setNodeId(dssNode.getId());
                compareWorkflowResult.setNodeType(dssNode.getNodeType());
                compareWorkflowResult.setNodeName(dssNode.getName());
                compareWorkflowResult.setNodeHistoryName(idHistoryNameMap.get(dssNode.getId()));
                compareWorkflowResult.setStatus(nodeFlag);
                compareWorkflowResult.setStatusCn(CompareWorkflowEnum.getNameByType(nodeFlag));

                CompareWorkflowResult compareWorkflowResult2 = new CompareWorkflowResult();
                BeanUtils.copyProperties(compareWorkflowResult, compareWorkflowResult2);
                compareWorkflowResultMap.put(dssNode.getId(), compareWorkflowResult2);
                Map<String,Object> thisVersionNodeContent=new HashMap<>(3);
                thisVersionNodeContent.put("name",dssNode.getName());
                thisVersionNodeContent.put("desc", dssNode.getDesc());
                thisVersionNodeContent.put("params", dssNode.getParams());
                String thisVersionNodeContentStr= DSSCommonUtils.COMMON_GSON.toJson(thisVersionNodeContent);

                Map<String,Object> anotherVersionNodeContent=new HashMap<>(3);
                anotherVersionNodeContent.put("name",dssNode.getJobContent().get(CompareWorkflow.nodeFlag_name));
                anotherVersionNodeContent.put("desc", dssNode.getJobContent().get(CompareWorkflow.nodeFlag_desc));
                anotherVersionNodeContent.put("params", dssNode.getJobContent().get(CompareWorkflow.nodeFlag_params));
                String anotherVersionNodeContentStr= DSSCommonUtils.COMMON_GSON.toJson(anotherVersionNodeContent);
                //分别应对 add 、 modify 、 delete 三种可能
                if(CompareWorkflow.nodeFlag_add.equals(nodeFlag)){
                    compareWorkflowResult.setResourceList(null);
                    compareWorkflowResult.setCompareResourceList(dssNode.getResources());
                    compareWorkflowResult.setNodeContent(null);
                    compareWorkflowResult.setCompareNodeContent(thisVersionNodeContentStr);
                }else if(CompareWorkflow.nodeFlag_modify.equals(nodeFlag)) {
                    compareWorkflowResult.setResourceList((List<Resource>) dssNode.getJobContent().get(CompareWorkflow.nodeFlag_resource));
                    compareWorkflowResult.setCompareResourceList(dssNode.getResources());
                    compareWorkflowResult.setNodeContent(anotherVersionNodeContentStr);
                    compareWorkflowResult.setCompareNodeContent(thisVersionNodeContentStr);
                }else{
                    //delete 时，dssNode只可能是另一个版本的。所以这里的this和another都和add与modify不一样，得对调一下。
                    compareWorkflowResult.setResourceList(dssNode.getResources());
                    compareWorkflowResult.setCompareResourceList(null);
                    compareWorkflowResult.setNodeContent(thisVersionNodeContentStr);;
                    compareWorkflowResult.setCompareNodeContent(null);
                }
                retList.add(compareWorkflowResult);
            }
            //设定上下游节点值
            for (CompareWorkflowResult compareWorkflowResult : retList) {
                String nodeId = compareWorkflowResult.getNodeId();
                if (!dssEdgeListMap.containsKey(nodeId)) {
                    continue;
                }
                List<DSSEdge> tempEdgeList = dssEdgeListMap.get(nodeId);
                List<CompareWorkflowResult> targetList = new ArrayList<>();
                List<CompareWorkflowResult> sourceList = new ArrayList<>();
                tempEdgeList.forEach(a -> {
                    if (StringUtils.isNotBlank(a.getSource()) && !nodeId.equals(a.getSource())) {
                        sourceList.add(compareWorkflowResultMap.get(a.getSource()));
                    }
                    if (StringUtils.isNotBlank(a.getTarget()) && !nodeId.equals(a.getTarget())) {
                        targetList.add(compareWorkflowResultMap.get(a.getTarget()));
                    }
                });
                compareWorkflowResult.setChildNodeList(targetList);
                compareWorkflowResult.setParentNodeList(sourceList);
            }
        }
        return retList;
    }


    /**
     * 比对2个工作流
     * @param firstWorkflow 老版本工作流
     * @param secondWorkflow 新版本工作流
     * @param onlyCompareName 是否只对比节点名称，即只关注节点改名、新增、删除，不关注节点内容的变化
     */
    private List<CompareWorkflowResult> compareWorkflow(Workflow firstWorkflow, Workflow secondWorkflow,boolean onlyCompareName) {
        if (firstWorkflow == null && secondWorkflow == null) {
            return Collections.emptyList();
        }else if (firstWorkflow != null && secondWorkflow == null) {
            //逐个考察老版本节点的jobContent，如果jobContent为空，则标记问not（不比对），否则标记为已删除。
            for (WorkflowNode wkn : firstWorkflow.getWorkflowNodes()) {
                DSSNode dssNode = wkn.getDSSNode();
                Map<String, Object> jobContent = dssNode.getJobContent();
                addJobContent(dssNode);
                jobContent.put(CompareWorkflow.nodeFlag, CompareWorkflow.nodeFlag_delete);
            }
        }else if (firstWorkflow == null) {
            //逐个考察新版本节点的jobContent，如果jobContent为空，则标记问not（不比对），否则标记为新增。
            for (WorkflowNode wkn : secondWorkflow.getWorkflowNodes()) {
                DSSNode dssNode = wkn.getDSSNode();
                Map<String, Object> jobContent = dssNode.getJobContent();
                addJobContent(dssNode);
                jobContent.put(CompareWorkflow.nodeFlag, CompareWorkflow.nodeFlag_add);
            }

        }else {

            log.info("============== first =====================");
            Map<String, List<DSSNode>> firstNodeTypeMap = new HashMap<>();
            List<WorkflowNode> firstWorkflowNodes = firstWorkflow.getWorkflowNodes();
            combineData(firstWorkflowNodes, firstNodeTypeMap);

            log.info("============== second =====================");
            Map<String, List<DSSNode>> secondNodeTypeMap = new HashMap<String, List<DSSNode>>();
            List<WorkflowNode> secondWorkflowNodes = secondWorkflow.getWorkflowNodes();
            combineData(secondWorkflowNodes, secondNodeTypeMap);

            List<String> linkisNodeTypeList = webankDSSFlowMapper.getLinkisNodeTypeWorkflow();
            compareWorkflowNodeDetail(firstWorkflowNodes, secondNodeTypeMap, CompareWorkflow.nodeFlag_delete, linkisNodeTypeList,onlyCompareName);
            compareWorkflowNodeDetail(secondWorkflowNodes, firstNodeTypeMap, CompareWorkflow.nodeFlag_add, linkisNodeTypeList,onlyCompareName);
        }
        //组装结果，并确定节点的上下游节点信息
        List<Workflow> workflows= Arrays.asList(firstWorkflow,secondWorkflow);
        return getResultList(workflows);
    }

    /**
     * DSSNode没有jobContent，则添加一个默认值，返回jobcontent,否则返回空
     * @param dssNode
     * @return
     */
    private Map<String, Object> addJobContent(DSSNode dssNode){
        Map<String, Object> jobContent = dssNode.getJobContent();
        if (jobContent == null){
            jobContent = new HashMap<>();
            dssNode.setJobContent(jobContent);
            return jobContent;
        }
        return null;
    }

    //比较单个工作流详情
    private void compareWorkflowNodeDetail(List<WorkflowNode> workflowNodes, Map<String, List<DSSNode>> compareNodeTypeMap,
                                           String delete_or_add,List<String> linkisNodeTypeList,boolean onlyCompareName) {
        /**
         * 首先按照第一个的工作流的节点进行比较
         */
        for (WorkflowNode wkn : workflowNodes) {
            DSSNode dssNode = wkn.getDSSNode();
            String nodeType = dssNode.getNodeType();
            //如果为空，則添加多一個
            addJobContent(dssNode);
            Map<String, Object> jobContent = dssNode.getJobContent();

            //如果第二节点不存在相同的节点类型，证明已经删除
            if (!compareNodeTypeMap.containsKey(nodeType)) {
                jobContent.put(CompareWorkflow.nodeFlag, delete_or_add);
                continue;
            }
            //找出第二个同id的节点进行比较
            List<DSSNode> compareNodeList = compareNodeTypeMap.get(nodeType);
            DSSNode tempCompareDSSNode = null;
            for (DSSNode tempNode : compareNodeList) {
                if (tempNode.getId().trim().equalsIgnoreCase(dssNode.getId().trim())) {
                    tempCompareDSSNode = tempNode;
                    break;
                }
            }

            //如果第二个工作流不含有相同的id节点，说明已经删除
            if (tempCompareDSSNode == null) {
                jobContent.put(CompareWorkflow.nodeFlag, delete_or_add);
                continue;
            }

            /******************非删、非增、那只可能是修改了***********************/
            boolean notModify= dssNode.getName().equals(tempCompareDSSNode.getName());
            if(!onlyCompareName) {
                //如果不仅仅是比较节点名，那就还要对比其他内容
                notModify = notModify
                        &&StringUtils.equals(dssNode.getDesc(), tempCompareDSSNode.getDesc())
                        &&
                        DSSCommonUtils.COMMON_GSON.toJson(dssNode.getParams()).equals(DSSCommonUtils.COMMON_GSON.toJson(tempCompareDSSNode.getParams()))
                        &&
                        !compareDssNodeResource(dssNode, tempCompareDSSNode, delete_or_add, linkisNodeTypeList);
            }
            if(!notModify) {
                //首先标记flag为modify
                jobContent.put(CompareWorkflow.nodeFlag, CompareWorkflow.nodeFlag_modify);
                //把对比内容保存到content中
                jobContent.put(CompareWorkflow.nodeFlag_resource, tempCompareDSSNode.getResources());
                jobContent.put(CompareWorkflow.nodeFlag_name, tempCompareDSSNode.getName());
                jobContent.put(CompareWorkflow.nodeFlag_desc, tempCompareDSSNode.getDesc());
                jobContent.put(CompareWorkflow.nodeFlag_params,tempCompareDSSNode.getParams());
            }else{
                jobContent.put(CompareWorkflow.nodeFlag, CompareWorkflow.nodeFlag_same);
            }
        }
    }

    /**
     * 比较单个节点的resource。
     * @param dssNode 当前节点
     * @param compareDSSNode 同id节点
     * @param linkisNodeTypeList linkis类的节点类型集合
     * @return 是否变化，变化为true，不变为false
     */
    private boolean compareDssNodeResource(DSSNode dssNode, DSSNode compareDSSNode, String delete_or_add, List<String> linkisNodeTypeList) {
        if(dssNode.getResources().size() != compareDSSNode.getResources().size()){
            //如果节点的resources的数量不一样，则就视为已修改
            return true;
        }
        //判断是否修改
        for (int i = 0; i < dssNode.getResources().size(); i++) {
            Resource resource = dssNode.getResources().get(i);
            Resource compareResource = compareDSSNode.getResources().get(i);
            String fileName = StringUtils.stripToEmpty(resource.getFileName());
            String version = StringUtils.stripToEmpty(resource.getVersion());

            String secondFileName = StringUtils.stripToEmpty(compareResource.getFileName());
            String secondVersion = StringUtils.stripToEmpty(compareResource.getVersion());
            if (fileName.equals(secondFileName)) {
                String nodeType = dssNode.getNodeType();
                boolean ischange = false;
                if (linkisNodeTypeList.contains(nodeType)) {
                    //由于发布的时候，linkisNodeType节点都会重新上传一份，所以发布之后的版本号，大于1，则证明是已经修改
                    int executeVersion = 0;
                    if (CompareWorkflow.nodeFlag_delete.equals(delete_or_add)) {
                        executeVersion = Integer.parseInt(secondVersion.substring(1));
                    } else {
                        executeVersion = Integer.parseInt(version.substring(1));
                    }
                    ischange = executeVersion > 1 ? true : false;
                } else if (!version.equals(secondVersion)) {
                    ischange = true;
                }
                if (ischange) {
                    return true;
                }
            }
        }
        return false;
    }

    //组合工作流
    private void combineData(List<WorkflowNode> workflowNodes,Map<String,List<DSSNode>> nodeTypeMap){
        workflowNodes.forEach(wkn -> {
            DSSNode dssNode = wkn.getDSSNode();
            String nodeType = dssNode.getNodeType();
            nodeTypeMap.putIfAbsent(nodeType,new ArrayList<>());
            nodeTypeMap.get(nodeType).add(dssNode);
        });
    }

    @Override
    public ResponseAppAndSubFlowCompare compareWorkflowAndSubFlow(RequestAppWithSubFlowCompare requestAppWithSubFlowCompare) {
        List<CompareWorkflowAndSubFlowResult> compareInfos = new ArrayList<>();
        DSSFlow oldDSSFlow = flowService.getFlowWithJsonAndSubFlowsByID(requestAppWithSubFlowCompare.getOldFlowId());
        Workflow oldFlow = WorkflowFactory.INSTANCE.getJsonToFlowParser().parse(oldDSSFlow);

        DSSFlow newDSSFlow = flowService.getFlowWithJsonAndSubFlowsByID(requestAppWithSubFlowCompare.getNewFlowId());
        Workflow newFlow = WorkflowFactory.INSTANCE.getJsonToFlowParser().parse(newDSSFlow);
        Map<String, Workflow> oldSubFlows = parseFlowRecurse(oldFlow);
        Map<String, Workflow> newSubFLows = parseFlowRecurse(newFlow);
        MapDifference<String, Workflow> difference = Maps.difference(oldSubFlows, newSubFLows);
        Map<String, MapDifference.ValueDifference<Workflow>> modify = difference.entriesDiffering();
        Map<String,Workflow> deleteSubFlow=difference.entriesOnlyOnLeft();
        Map<String,Workflow> newAddSubFlow=difference.entriesOnlyOnRight();
        //被删的子工作流
        for (Workflow value : deleteSubFlow.values()) {
            List<CompareWorkflowAndSubFlowResult.NodeCompareInfo> nodeList = Collections.singletonList(
                    new CompareWorkflowAndSubFlowResult.NodeCompareInfo(CompareWorkflow.nodeFlag_delete,
                            value.getName() + "_", null, LINKIS_CONTROL_EMPTY_NODE));
            compareInfos.add(new CompareWorkflowAndSubFlowResult(nodeList, value.getName(), null,CompareWorkflow.nodeFlag_delete));
        }
        //新增的子工作流
        for (Workflow value : newAddSubFlow.values()) {
            compareInfos.add(new CompareWorkflowAndSubFlowResult(null, null, value.getName(),CompareWorkflow.nodeFlag_add));
        }

        List<CompareWorkflowAndSubFlowResult.NodeCompareInfo> parentNodeCompareInfos=compareWorkflow( oldFlow,  newFlow,true).stream()
                .map(WebankCompareWorkflowServiceImpl::convert).collect(Collectors.toList());
        if(!oldFlow.getName().equals(newFlow.getName())){
            //因为工作流在转成wtss格式时，一定会在里面增加一个同名节点，所以如果工作流名被修改，那么这个同名节点也要改名
            parentNodeCompareInfos.add(new CompareWorkflowAndSubFlowResult.NodeCompareInfo(CompareWorkflow.nodeFlag_modify, oldFlow.getName(), newFlow.getName(),LINKIS_CONTROL_EMPTY_NODE));
        }
        //工作流节点或工作流名称修改
        if(!CollectionUtils.isEmpty(parentNodeCompareInfos) ) {
            compareInfos.add(new CompareWorkflowAndSubFlowResult(parentNodeCompareInfos, oldFlow.getName(), newFlow.getName(), CompareWorkflow.nodeFlag_modify));
        }
        //被修改的子工作流
        for (Map.Entry<String, MapDifference.ValueDifference<Workflow>> entry : modify.entrySet()) {
            Workflow oldSubFlow = entry.getValue().leftValue();
            Workflow newSubFlow = entry.getValue().rightValue();
            List<CompareWorkflowAndSubFlowResult.NodeCompareInfo> nodeCompareInfos = compareWorkflow(oldSubFlow, newSubFlow, true).stream()
                    .map(WebankCompareWorkflowServiceImpl::convert).collect(Collectors.toList());
            if(!oldSubFlow.getName().equals(newSubFlow.getName())){
                //因为工作流在转成wtss格式时，一定会在里面增加一个同名节点，所以如果工作流名被修改，那么这个同名节点也要改名
                nodeCompareInfos.add(
                        new CompareWorkflowAndSubFlowResult.NodeCompareInfo(CompareWorkflow.nodeFlag_modify,
                                oldSubFlow.getName() + "_",
                                newSubFlow.getName() + "_",LINKIS_CONTROL_EMPTY_NODE));
            }
            if(!CollectionUtils.isEmpty(nodeCompareInfos)) {
                compareInfos.add(new CompareWorkflowAndSubFlowResult(nodeCompareInfos, oldSubFlow.getName(), newSubFlow.getName(), CompareWorkflow.nodeFlag_modify));
            }
        }
        ResponseAppAndSubFlowCompare responseAppAndSubFlowCompare = new ResponseAppAndSubFlowCompare();
        responseAppAndSubFlowCompare.setList(compareInfos);
        log.info("compareInfos size is:{}",compareInfos.size());
        return responseAppAndSubFlowCompare;
    }
    private static CompareWorkflowAndSubFlowResult.NodeCompareInfo convert(CompareWorkflowResult e) {
        CompareWorkflowAndSubFlowResult.NodeCompareInfo nodeCompareInfo = new CompareWorkflowAndSubFlowResult.NodeCompareInfo();
        BeanUtils.copyProperties(e, nodeCompareInfo);
        if (CompareWorkflow.nodeFlag_delete.equals(e.getStatus())) {
            nodeCompareInfo.setNodeHistoryName(e.getNodeName());
        } else {
            nodeCompareInfo.setNodeNewName(e.getNodeName());
            nodeCompareInfo.setNodeHistoryName(e.getNodeHistoryName());
        }
        return nodeCompareInfo;
    }

    private Map<String,Workflow> parseFlowRecurse(Workflow workflow){
        Map<String, Workflow> nodeIdFlowMap = new HashMap<>();
        List<Workflow> subflows=workflow.getChildren();
        if(CollectionUtils.isEmpty(subflows)){
            return Collections.emptyMap();
        }
        Map<Long, String> subFlowAppIdNodeId = new HashMap<>();
        workflow.getWorkflowNodes().stream().filter(node-> "workflow.subflow".equals(node.getNodeType())
                        &&node.getDSSNode()!=null
                        &&node.getDSSNode().getJobContent()!=null
                        &&node.getDSSNode().getJobContent().containsKey("embeddedFlowId"))
                .forEach(e-> subFlowAppIdNodeId.put( Double.valueOf(e.getDSSNode().getJobContent().get("embeddedFlowId").toString()).longValue(),e.getId()));

        for (Workflow subflow : subflows) {
            nodeIdFlowMap.putAll(parseFlowRecurse(subflow));
            Long appId = subflow.getId();
            if(!subFlowAppIdNodeId.containsKey(appId)){
                log.error("subFlow is not in parent Node. subFLowId:{},parentFlowId:{}", appId, workflow.getId());
                continue;
            }
            nodeIdFlowMap.put(subFlowAppIdNodeId.get(appId),subflow);
        }
        return nodeIdFlowMap;
    }

}
