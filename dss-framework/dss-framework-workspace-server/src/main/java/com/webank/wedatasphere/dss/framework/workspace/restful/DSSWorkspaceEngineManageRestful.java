package com.webank.wedatasphere.dss.framework.workspace.restful;

import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceKillRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ECInstance;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.vo.EngineConnItemVO;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECReleaseStrategyStatusChangeRequest;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.KillECInstanceRequest;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECKillHistoryRecordVO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceECKillHistoryService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceECService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient.ENGINE_TYPE_IN_YARN_QUEUE;

/**
 * 工作空间内与引擎信息相关的接口
 * Author: xlinliu
 * Date: 2023/4/17
 */

@RequestMapping(path = "/dss/framework/workspace", produces = {"application/json"})
@RestController
public class DSSWorkspaceEngineManageRestful {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSSWorkspaceEngineManageRestful.class);

    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    DSSWorkspaceService dssWorkspaceService;
    @Autowired
    ResourceManageClient resourceManageClient;
    @Autowired
    DSSWorkspaceECService DSSWorkspaceECService;

    @Autowired
    DSSWorkspaceECKillHistoryService DSSWorkspaceECKillHistoryService;


    /**
     * 获取引擎列表
     */
    @PostMapping("listEngineConnInstances")
    public Message listEngineConnInstances(@RequestBody ECInstanceRequest ecInstanceRequest){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        long workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin workspace!(您好，您不是管理员，没有权限管理空间)");
        }

        if(ecInstanceRequest.getPageSize()==null){
            ecInstanceRequest.setPageSize(15);
        } else if (ecInstanceRequest.getPageSize()>50) {
            ecInstanceRequest.setPageSize(50);
        }
        if(ecInstanceRequest.getPageNow()==null){
            ecInstanceRequest.setPageNow(1);
        }
        //限制引擎类型
        if(CollectionUtils.isEmpty(ecInstanceRequest.getEngineType())){
            ecInstanceRequest.setEngineType(new ArrayList<>(ENGINE_TYPE_IN_YARN_QUEUE));
        }
        //创建者限制，只查本空间内的成员的引擎。
        Set<String> workspaceUsers=new HashSet<>(
                dssWorkspaceService.getWorkspaceUsers(ecInstanceRequest.getWorkspaceId().toString())
        );
        if(CollectionUtils.isEmpty(ecInstanceRequest.getCreateUser())){
            ecInstanceRequest.setCreateUser(new ArrayList<>(workspaceUsers));
        }else {
            List<String> userInWorkspace=ecInstanceRequest.getCreateUser().stream().filter(workspaceUsers::contains)
                    .collect(Collectors.toList());
            ecInstanceRequest.setCreateUser(userInWorkspace);
        }
        //状态过滤
        if(!CollectionUtils.isEmpty(ecInstanceRequest.getStatus())){
            ecInstanceRequest.setStatus(flatEngineStatus(ecInstanceRequest.getStatus()));
        }
        List<ECInstance> ecInstanceList;
        try {
            ecInstanceList = resourceManageClient.fetchECInstance(ecInstanceRequest,userName);
            assert ecInstanceList!=null;
            //内存排序，对全量数据排序
            if(!StringUtils.isEmpty(ecInstanceRequest.getSortBy())){
                ecInstanceList = sort(ecInstanceList, ecInstanceRequest.getSortBy(), ecInstanceRequest.getOrderBy());
            }
            //yarn队列过滤
            String yarnNameFilter=ecInstanceRequest.getYarnQueue();
            if(!StringUtils.isEmpty(yarnNameFilter)){
                ecInstanceList=ecInstanceList.stream()
                        .filter(e->e.getUseResource()!=null
                                &&e.getUseResource().getYarn()!=null
                                && yarnNameFilter.equals(e.getUseResource().getYarn().getQueueName()))
                        .collect(Collectors.toList());
            }
            //状态转化
            ecInstanceList.forEach(e-> e.setInstanceStatus(mergeEngineStatus(e.getInstanceStatus())));
        }catch (DSSRuntimeException e){
            return Message.error("获取引擎列表失败",e);
        }
        int total=ecInstanceList.size();
        int offset=(ecInstanceRequest.getPageNow()-1)*ecInstanceRequest.getPageSize();
        int length=ecInstanceRequest.getPageSize();
        List<EngineConnItemVO> engineConnList=
                ecInstanceList.stream()
                        .skip(offset).limit(length)
                        .map(EngineConnItemVO::fromECInstannce).collect(Collectors.toList());
        return Message.ok("成功获取引擎列表！").data("engineList",engineConnList).data("total",total);
    }

    /**
     * 手动释放引擎
     */
    @PostMapping("killEngineConnInstances")
    public Message killEngineConnInstances(@RequestBody KillECInstanceRequest killECInstanceRequest){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        List<ECInstanceKillRequest> instances=killECInstanceRequest.getInstances();
        if(instances==null){
            return Message.error("at lease one instance");
        }
        long workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin workspace!(您好，您不是管理员，没有权限管理空间)");
        }
        Workspace workspace = SSOHelper.getWorkspace(httpServletRequest);
        //审计
        instances.stream().map(ECInstanceKillRequest::getEngineInstance).forEach(instanceName ->
                AuditLogUtils.printLog(userName,workspace.getWorkspaceId(),workspace.getWorkspaceName(), TargetTypeEnum.EC_INSTANCE,
                        -1,instanceName, OperateTypeEnum.KILL,null)
        );
        try {
            List<String> ecInstanceNames=instances.stream().map(ECInstanceKillRequest::getEngineInstance).collect(Collectors.toList());
            ECInstanceRequest ecInstanceRequest=new ECInstanceRequest();
            ecInstanceRequest.setEcInstanceNames(ecInstanceNames);
            //必须在kill之前获取ec的信息，否则获取的信息是不准确的。
            List<ECKillHistoryRecord> killHistoryRecords = resourceManageClient.fetchECInstance(ecInstanceRequest, userName).stream()
                    .map(e -> ECKillHistoryRecord.convertECInstance2ECKillHistoryRecord(e, workspaceId, "0", userName))
                    .collect(Collectors.toList());
            //第一步，发起kill
            resourceManageClient.batchKillECInstance(instances,userName);
            //第二步，记录kill历史
            if (!killHistoryRecords.isEmpty()) {
                String thisInstanceName = Sender.getThisInstance();
                killHistoryRecords.forEach(e -> e.setExecuteInstance(thisInstanceName));
                DSSWorkspaceECKillHistoryService.batchAddEcKillRecord(killHistoryRecords);
            }
        }catch (DSSRuntimeException e){
            LOGGER.error("kill ec instance action failed", e);
            return Message.error("部分引擎kill失败，请稍后通过搜索查询引擎状态，确认kill结果",e);
        }
        return Message.ok("引擎停止请求发送成功，可稍后通过搜索查询引擎状态！");
    }

    @GetMapping("getQueueList")
    public Message getQueueList(@RequestParam ("workspaceId")Long workspaceId){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        if (!Objects.equals(workspaceId, SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId())
                ||
                !dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            List<String> queueList = DSSWorkspaceECService.getQueueList(workspaceId);
            return Message.ok().data("queueList", queueList);
        }catch (DSSRuntimeException e){
            return Message.error("获取队列失败。"+e.getMessage());
        }
    }

    @GetMapping("getEcReleaseStrategyList")
    public Message getEcReleaseStrategyList(@RequestParam ("workspaceId")Long workspaceId){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        if (!Objects.equals(workspaceId, SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId())
                ||
                !dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            List<ECReleaseStrategy> strategies = DSSWorkspaceECService.listEcReleaseStrategy(workspaceId);
            return Message.ok()
                    .data("strategyList", strategies)
                    .data("total", strategies.size());
        }catch (DSSRuntimeException e){
            return Message.error("获取规则失败。"+e.getMessage());
        }
    }

    @PutMapping("saveEcReleaseStrategy")
    public Message saveEcReleaseStrategy(@RequestBody ECReleaseStrategy strategy){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace=SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECReleaseStrategy savedStrategy = DSSWorkspaceECService.saveEcReleaseStrategy(strategy, workspaceId, userName);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_KILL_STRATEGY,
                    savedStrategy.getStrategyId(),
                    savedStrategy.getName(),
                    strategy.getStrategyId()==null?OperateTypeEnum.CREATE: OperateTypeEnum.UPDATE,
                    strategy);
            return Message.ok().data("strategy", savedStrategy);
        }catch (DSSRuntimeException e){
            return Message.error("保存规则失败。"+e.getMessage());
        }
    }

    @PostMapping("ecReleaseStrategy/{strategyId}")
    public  Message deleteECReleaseStrategy(@PathVariable("strategyId")String strategyId){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace=SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECReleaseStrategy deleteResult= DSSWorkspaceECService.deleteEcReleaseStrategy(strategyId);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_KILL_STRATEGY,
                    strategyId,
                    deleteResult.getName(),
                    OperateTypeEnum.DELETE,
                    deleteResult);
            return Message.ok().setMessage("删除成功");
        }catch (DSSRuntimeException e){
            return Message.error("删除规则失败。"+e.getMessage());
        }
    }

    @PostMapping("changeEcReleaseStrategyStatus")
    public Message changeEcReleaseStrategyStatus(@RequestBody ECReleaseStrategyStatusChangeRequest changeRequest){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace=SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECReleaseStrategy strategy= DSSWorkspaceECService.changeEcReleaseStrategyStatus(changeRequest);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_KILL_STRATEGY,
                    strategy.getStrategyId(),
                    strategy.getName(),
                    "turnOn".equals(changeRequest.getAction())?OperateTypeEnum.ENABLE: OperateTypeEnum.DISABLE,
                    changeRequest);
            return Message.ok();
        }catch (DSSRuntimeException e){
            return Message.error("操作规则失败。"+e.getMessage());
        }
    }

    @GetMapping("listEcKillHistory")
    public Message listEcKillHistory(@RequestParam("workspaceId")Long workspaceId,@RequestParam("pageNow") int pageNow,
                                     @RequestParam("pageSize") int pageSize){
        if(pageNow<1 || pageSize<1){
            return Message.error("page param error!(分页参数错误)");
        }
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        if (!Objects.equals(workspaceId, SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId())
                ||
                !dssWorkspaceService.isAdminUser(workspaceId, userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            PageInfo<ECKillHistoryRecord> pageInfo = DSSWorkspaceECKillHistoryService.listEcKillHistory(workspaceId, pageNow, pageSize);
            List<ECKillHistoryRecordVO> recordVOList=pageInfo.getData().stream().map(ECKillHistoryRecord::toVO).collect(Collectors.toList());

            return Message.ok()
                    .data("engineList", recordVOList)
                    .data("total", pageInfo.getTotal());
        }catch (DSSRuntimeException e){
            return Message.error("查询失败。"+e.getMessage());
        }
    }

    /**
     * 引擎状态flat
     * @param dssStatus
     * @return
     */
    private List<String> flatEngineStatus(List<String> dssStatus){
        Set<String> flatResult=new HashSet<>();

        dssStatus.forEach(e->{
            switch (e){
                case "Starting":
                    flatResult.add("Starting");
                    break;
                case "Idle":
                    flatResult.addAll(Arrays.asList("Unlock"));
                    break;
                case "Busy":
                    flatResult.addAll(Arrays.asList(  "Idle","Locked", "Busy","Running","Failed","Success"));
                    break;
                default:
                    //do nothing
                    break;


            }
        });
        return  new ArrayList<>(flatResult);
    }

    /**
     * 根据排序参数排序引擎
     * @param ecInstances
     * @return
     */
    private List<ECInstance> sort(List<ECInstance> ecInstances,String sortBy,String orderBy){
        if(StringUtils.isEmpty(sortBy)){
            return ecInstances;
        }
        Comparator<ECInstance> comparator;
        if("queueMemory".equals(sortBy)){
            comparator=(engine1,engine2)->{
                String memory1=Optional.of(engine1).map(ECInstance::getUseResource).map(ECInstance.Resource::getYarn)
                        .map(ECInstance.YarnInfo::getQueueMemory).orElse(null);
                String memory2=Optional.of(engine2).map(ECInstance::getUseResource).map(ECInstance.Resource::getYarn)
                        .map(ECInstance.YarnInfo::getQueueMemory).orElse(null);
                if(memory1!=null&&memory2!=null){
                    try {
                        Double m1 = Double.valueOf(memory1.substring(0, memory1.indexOf('G')).trim());
                        Double m2 = Double.valueOf(memory2.substring(0, memory2.indexOf('G')).trim());
                        return m1.compareTo(m2);
                    }catch (NumberFormatException ne){
                        return memory1.compareTo(memory2);
                    }
                } else if (memory1!=null) {
                    return  1;
                } else if (memory2!=null) {
                    return -1;
                } else  {
                    return  0;
                }
            };
        } else if ("queueCpu".equals(sortBy)) {
            comparator=(engine1,engine2)->{
                Integer memory1=Optional.of(engine1).map(ECInstance::getUseResource).map(ECInstance.Resource::getYarn)
                        .map(ECInstance.YarnInfo::getQueueCpu).orElse(null);
                Integer memory2=Optional.of(engine2).map(ECInstance::getUseResource).map(ECInstance.Resource::getYarn)
                        .map(ECInstance.YarnInfo::getQueueCpu).orElse(null);
                if(memory1!=null&&memory2!=null){
                    return memory1.compareTo(memory2);
                } else if (memory1!=null) {
                    return  1;
                } else if (memory2!=null) {
                    return -1;
                } else  {
                    return  0;
                }
            };
        }else {
            comparator= (e1,e2)->0;
        }
        //默认降序，除非显示指定升序
        if(!"asc".equals(orderBy)){
            comparator=comparator.reversed();
        }

        return
                ecInstances.stream().sorted(comparator).collect(Collectors.toList());

    }

    /**
     * 归并引擎状态
     * @param status
     * @return
     */
    private String mergeEngineStatus(String status){
        if(status==null){
            return null;
        }

        switch (status) {
            case "Starting":
                return "Starting";
            case "Unlock":
                return "Idle";
            case "ShuttingDown":
                return "ShuttingDown";
            case "Locked":
            case "Idle":
            case "Busy":
            case "Running":
            case "Failed":
            case "Success":
            default:
                return "Busy";
        }

    }
}
