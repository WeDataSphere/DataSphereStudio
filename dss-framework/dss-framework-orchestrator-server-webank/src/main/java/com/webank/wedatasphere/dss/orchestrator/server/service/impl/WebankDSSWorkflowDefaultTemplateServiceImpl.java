package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkspaceDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkspaceDefaultDO;
import com.webank.wedatasphere.dss.framework.project.request.WorkflowDefaultTemplateRequest;
import com.webank.wedatasphere.dss.framework.project.request.WorkspaceDefaultTemplateRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflowDefault;
import com.webank.wedatasphere.dss.orchestrator.server.service.WebankDSSWorkflowDefaultTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebankDSSWorkflowDefaultTemplateServiceImpl implements WebankDSSWorkflowDefaultTemplateService {

    @Autowired
    ECTemplateWorkflowDefaultMapper templateWorkflowDefaultMapper;

    @Autowired
    ECTemplateWorkspaceDefaultMapper templateWorkspaceDefaultMapper;

    @Autowired
    DSSWorkspaceService dssWorkspaceService;

    @Autowired
    ECConfigTemplateMapper ecConfigTemplateMapper;

    @Override
    public void saveTemplateRef(WorkflowDefaultTemplateRequest request) {
        //如果request.templates为空，则表示移除了所有的工作流默认模板
        if(CollectionUtils.isEmpty(request.getTemplateIds())){
            templateWorkflowDefaultMapper.deleteWorkflowTemplateRef(request.getOrchestratorId());
            return;
        }
        templateWorkflowDefaultMapper.deleteWorkflowTemplateRef(request.getOrchestratorId());
        List<ECTemplateWorkflowDefaultDO> list = new ArrayList<>();
        request.getTemplateIds().forEach(item ->{
            ECTemplateWorkflowDefaultDO defaultDO = new ECTemplateWorkflowDefaultDO();
            defaultDO.setTemplateId(item);
            defaultDO.setOrchestratorId(request.getOrchestratorId());
            defaultDO.setProjectId(request.getProjectId());
            defaultDO.setCreateUser(request.getCreateUser());
            defaultDO.setUpdateUser(request.getUpdateUser());
            list.add(defaultDO);
        });
        //新增默认
        templateWorkflowDefaultMapper.batchInsert(list);
    }

    @Override
    public List<ECTemplateWorkflowDefault> getWrokflowDefaultTemplates(Long orchestratorId) {
        return templateWorkflowDefaultMapper.getWorkflowDefaultTemplates(orchestratorId).stream().map(ECTemplateWorkflowDefault::convertToBean).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveWorkspaceTemplateRef(WorkspaceDefaultTemplateRequest request,String username) throws DSSErrorException {

        // 判断用户是否具有管理员权限
        if(!dssWorkspaceService.isAdminUser(request.getWorkspaceId(),username)){
            throw new DSSErrorException(90053,String.format("%s user not permission setting default template",username));
        }

        if(CollectionUtils.isEmpty(request.getTemplateIds())){
            request.setTemplateIds(new ArrayList<>());
        }else{
            // 检查如果cookie中的工作空间与模板的不一致的情况
            List<ECConfigTemplateDO> templateDOList = ecConfigTemplateMapper.getTemplatesListByUids(request.getTemplateIds());
            Set<Long> workspaceIdSet = templateDOList.stream().map(ECConfigTemplateDO::getWorkspaceId).collect(Collectors.toSet());
            if(workspaceIdSet.size() != 1 || !workspaceIdSet.contains(request.getWorkspaceId())){
                throw new DSSErrorException(90053,"模板对应的工作空间与cookie中不一致,请重新刷新后保存");
            }
        }

        List<ECTemplateWorkspaceDefaultDO> templateList = templateWorkspaceDefaultMapper.getWorkspaceDefaultTemplates(request.getWorkspaceId());

        List<String> templateIdList = templateList.stream().map(ECTemplateWorkspaceDefaultDO::getTemplateId).collect(Collectors.toList());

        // 取出需要删除的模板
        List<String> deleteTemplateIds = templateIdList.stream().filter(templateId ->
                !request.getTemplateIds().contains(templateId)).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(deleteTemplateIds)){
            templateWorkspaceDefaultMapper.deleteWorkspaceTemplateRef(request.getWorkspaceId(),deleteTemplateIds);
        }

        // 取出需要添加的模板
        List<ECTemplateWorkspaceDefaultDO> insertTemplateIds = getEcTemplateWorkspaceDefaultDO(request, username, templateIdList);

        if(!CollectionUtils.isEmpty(insertTemplateIds)){

            templateWorkspaceDefaultMapper.batchInsert(insertTemplateIds);
        }



    }

    @Override
    public List<ECTemplateWorkspaceDefaultDO> getWorkspaceDefaultTemplates(Long workspaceId) {
        return templateWorkspaceDefaultMapper.getWorkspaceDefaultTemplates(workspaceId);
    }


    private static List<ECTemplateWorkspaceDefaultDO> getEcTemplateWorkspaceDefaultDO(WorkspaceDefaultTemplateRequest request, String username, List<String> templateIdList) {
        List<ECTemplateWorkspaceDefaultDO> insertTemplateIds = new ArrayList<>();
        for(String templateId: request.getTemplateIds()){

            if(templateIdList.contains(templateId)){
                continue;
            }

            ECTemplateWorkspaceDefaultDO ecTemplateWorkspaceDefaultDO = new ECTemplateWorkspaceDefaultDO();
            ecTemplateWorkspaceDefaultDO.setWorkspaceId(request.getWorkspaceId());
            ecTemplateWorkspaceDefaultDO.setTemplateId(templateId);
            ecTemplateWorkspaceDefaultDO.setCreateUser(username);
            ecTemplateWorkspaceDefaultDO.setUpdateUser(username);
            insertTemplateIds.add(ecTemplateWorkspaceDefaultDO);

        }
        return insertTemplateIds;
    }
}
