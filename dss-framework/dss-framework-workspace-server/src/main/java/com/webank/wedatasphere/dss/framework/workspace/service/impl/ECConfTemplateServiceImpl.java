package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.github.pagehelper.PageHelper;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.ConfigurationTemplate;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.UpdateKeyMappingRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.CategoryLabel;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfItem;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplate;
import com.webank.wedatasphere.dss.framework.workspace.bean.PermissionUser;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.TemplatePermissionUsersGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateUserMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateUserDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.PermissionUserCountDO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceUserService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/6/15
 */
@Service
public class ECConfTemplateServiceImpl implements ECConfTemplateService {
    @Autowired
    private ECConfigTemplateMapper ecConfigTemplateMapper;
    @Autowired
    private ResourceManageClient resourceManageClient;
    @Autowired
    private ECConfigTemplateUserMapper ecConfigTemplateUserMapper;
    @Autowired
    private DSSWorkspaceUserService dssWorkspaceUserService;


    @Override
    public void checkConfTemplateName(String name) {
        if (ecConfigTemplateMapper.getByName(name) != null) {
            throw new DSSRuntimeException("模板名已存在");
        }
    }

    @Override
    public List<String> getEngineTypeList(String applicationFilter, String operateUser) {
        if (applicationFilter == null) {
            //get all engine type
            return resourceManageClient.getEngineTypes(operateUser);
        } else {
            return
                    resourceManageClient.getApplicationEngineTypeCategory(operateUser)
                            .stream()
                            .filter(e -> applicationFilter.equals(e.getCategoryName()))
                            .findAny()
                            .map(CategoryLabel::getChildCategory)
                            .orElse(new ArrayList<>())
                            .stream()
                            .map(CategoryLabel::getCategoryName)
                            .map(e -> e.substring(0, e.indexOf("-")))
                            .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getEngineNameList(String applicationFilter, String operateUser) {
        return
                resourceManageClient.getApplicationEngineTypeCategory(operateUser)
                        .stream()
                        .filter(e -> applicationFilter.equals(e.getCategoryName()))
                        .findAny()
                        .map(CategoryLabel::getChildCategory)
                        .orElse(new ArrayList<>())
                        .stream()
                        .map(CategoryLabel::getCategoryName)
                        .collect(Collectors.toList());
    }

    @Override
    public List<String> getApplicationList(String operateUser) {
        return
                resourceManageClient.getApplicationEngineTypeCategory(operateUser)
                        .stream()
                        .map(CategoryLabel::getCategoryName)
                        .filter(e -> !"GlobalSettings".equals(e) && !"全局设置".equals(e))
                        .collect(Collectors.toList());
    }

    @Override
    public ECConfTemplate getTemplateParamDetail(String templateId, String operateUser) {
        ECConfigTemplateDO templateDO = ecConfigTemplateMapper.getByTemplateId(templateId);
        if (templateDO == null) {
            throw new DSSRuntimeException("param config template is not exist" + templateId);
        }
        ECConfTemplate template = ECConfTemplate.fromDO(templateDO);
        ConfigurationTemplate configurationTemplate = resourceManageClient.getECConfTemplate(templateId, operateUser);
        if (configurationTemplate == null) {
            throw new DSSRuntimeException("param config template is not exist" + templateId);
        }
        List<ECConfItem> conf= configurationTemplate.getItemList()
                .stream()
                .map(e -> {
                    ECConfItem confItem = new ECConfItem();
                    BeanUtils.copyProperties(e, confItem);
                    return confItem;
                })
                .collect(Collectors.toList());
        template.setParamDetails(conf);
        return template;
    }

    @Override
    public List<ECConfItem> getTemplateParamConf(String engineType, String operateUser) {
        return
                resourceManageClient.getTemplateParamConf(engineType,operateUser).stream()
                        .map(e -> {
                            ECConfItem confItem = new ECConfItem();
                            BeanUtils.copyProperties(e, confItem);
                            return confItem;
                        })
                        .collect(Collectors.toList());
    }

    @Override
    public PageInfo<ECConfTemplate> getTemplateList(ECConfTemplateGetRequest request) {
        String engineType= request.getEngineType();;
        String engineName= request.getEngineName();
        if(engineName!=null&&engineType==null){
            engineType = engineName.substring(0, engineName.indexOf("-"));
            request.setEngineType(engineType);
        }
        PageHelper.startPage(request.getPageNow(), request.getPageSize());
        List<ECConfigTemplateDO> dos = ecConfigTemplateMapper.getTemplatesList(request);
        com.github.pagehelper.PageInfo<ECConfigTemplateDO> doPage = new com.github.pagehelper.PageInfo<>(dos);
        List<ECConfTemplate> recordList=
                doPage.getList().stream().map(ECConfTemplate::fromDO).collect(Collectors.toList());
        List<String> templateIds=
        recordList.stream()
                .filter(e -> e.getPermissionType() == ECConfTemplate.SPECIFIED_USER_PERMISSION_TYPE)
                .map(ECConfTemplate::getTemplateId)
                .collect(Collectors.toList());
        Map<String, PermissionUserCountDO> countMap;
        if(!templateIds.isEmpty()) {
            countMap = ecConfigTemplateUserMapper.getPermissionUserCount(templateIds);
        }else {
            countMap=Collections.EMPTY_MAP;
        }
        long workspaceUserCount= dssWorkspaceUserService.getUserCount(request.getWorkspaceId());
        for (ECConfTemplate template : recordList) {
            long count;
            if(template.getPermissionType()==ECConfTemplate.SPECIFIED_USER_PERMISSION_TYPE){
                count = countMap.getOrDefault(template.getTemplateId(),new PermissionUserCountDO(){ {
                            setGroupId("");setUserCount(0L);}})
                        .getUserCount();
            }else{
                count = workspaceUserCount;
            }
            template.setPermissionUserCount(count);
        }
        return new PageInfo<>(recordList,doPage.getTotal());
    }

    @Override
    public ECConfTemplate getTemplate(String templateId) {
        ECConfigTemplateDO templateDO = ecConfigTemplateMapper.getByTemplateId(templateId);
        return templateDO == null ? null : ECConfTemplate.fromDO(templateDO);
    }

    @Override
    public ECConfTemplate saveTemplate(ECConfTemplate ecConfTemplate, Long workspaceId, String operator) {

        String templateId = ecConfTemplate.getTemplateId();
        if (templateId == null) {
            String name = ecConfTemplate.getName();
            //校验唯一性
            checkConfTemplateName(name);
            templateId = UUID.randomUUID().toString();
            ecConfTemplate.setTemplateId(templateId);
            ecConfTemplate.setCreator(operator);
            ecConfTemplate.setModifier(operator);
            //存到linkis
            UpdateKeyMappingRequest clientBean = convertTemplate(ecConfTemplate);
            resourceManageClient.saveECConfTemplate(clientBean);
            ECConfigTemplateDO ecConfigTemplateDO = ecConfTemplate.toDO(workspaceId);
            ecConfigTemplateMapper.insert(ecConfigTemplateDO);
        } else {
            ecConfTemplate.setModifier(operator);
            //存到linkis
            UpdateKeyMappingRequest clientBean = convertTemplate(ecConfTemplate);
            resourceManageClient.saveECConfTemplate(clientBean);
            ECConfigTemplateDO ecConfigTemplateDO = ecConfTemplate.toDO(workspaceId);
            ecConfigTemplateMapper.update(ecConfigTemplateDO);
        }
        List<PermissionUser> permissionUsers;
        final String finalTemplateId = templateId;
        //保存可见范围用户
        if (ecConfTemplate.getPermissionType() == ECConfTemplate.SPECIFIED_USER_PERMISSION_TYPE
                && (permissionUsers = ecConfTemplate.getPermissionUsers()) != null) {
            //先把老数据删除，在插入新数据
            ecConfigTemplateUserMapper.deleteByTemplateId(templateId);
            List<ECConfigTemplateUserDO> userDOS = permissionUsers.stream().map(e -> {
                        ECConfigTemplateUserDO userDO = new ECConfigTemplateUserDO();
                        userDO.setUserName(e.getName());
                        userDO.setWorkspaceId(workspaceId);
                        userDO.setTemplateId(finalTemplateId);
                        return userDO;
                    })
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(userDOS)) {
                ecConfigTemplateUserMapper.insertBatch(userDOS);
            }
        }
        return ecConfTemplate;
    }

    private UpdateKeyMappingRequest convertTemplate(ECConfTemplate template) {
        UpdateKeyMappingRequest clientBean = new UpdateKeyMappingRequest();
        clientBean.setTemplateName(template.getName());
        clientBean.setTemplateUid(template.getTemplateId());
        clientBean.setOperator(template.getModifier());
        clientBean.setEngineType(template.getEngineType());
        List<UpdateKeyMappingRequest.TemplateConfigKey> items = template.getParamDetails().stream()
                .map(e -> {
                    UpdateKeyMappingRequest.TemplateConfigKey item = new UpdateKeyMappingRequest.TemplateConfigKey();
                    BeanUtils.copyProperties(e, item);
                    return item;
                })
                .collect(Collectors.toList());
        clientBean.setItemList(items);
        return clientBean;
    }

    @Override
    public PageInfo<String> getPermissionUserList(TemplatePermissionUsersGetRequest request) {
        ECConfigTemplateDO templateDO = ecConfigTemplateMapper.getByTemplateId(request.getTemplateId());
        if(templateDO==null){
            throw new DSSRuntimeException("template does not exist,templateId" + request.getTemplateId());
        }
        if (templateDO.getPermissionType() == ECConfTemplate.SPECIFIED_USER_PERMISSION_TYPE) {
            PageHelper.startPage(request.getPageNow(), request.getPageSize());
            List<ECConfigTemplateUserDO> dos = ecConfigTemplateUserMapper.selectByTemplateId(request.getTemplateId());
            com.github.pagehelper.PageInfo<ECConfigTemplateUserDO> doPage = new com.github.pagehelper.PageInfo<>(dos);
            List<String> userNames = doPage.getList().stream().map(ECConfigTemplateUserDO::getUserName).collect(Collectors.toList());
            return new PageInfo<>(userNames, doPage.getTotal());

        } else {
            return dssWorkspaceUserService.getAllWorkspaceUsersPage(templateDO.getWorkspaceId(), request.getPageNow(), request.getPageSize());
        }
    }

    @Override
    public void deleteTemplate(String templateId) {
        ecConfigTemplateUserMapper.deleteByTemplateId(templateId);
        ecConfigTemplateMapper.deleteByTemplateId(templateId);
    }

    @Override
    public boolean checkTemplateUse(String templateId){
        return ecConfigTemplateUserMapper.checkTemplateUse(templateId) > 0;
    }

}
