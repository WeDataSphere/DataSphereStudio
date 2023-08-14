package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.project.dao.DssProjectOperateRecordMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ProjectOperateRecordDO;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectOperateService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目操作实现类
 * Author: xlinliu
 * Date: 2022/9/6
 */
@Service
public class DSSProjectOperateServiceImpl implements DSSProjectOperateService {
    @Resource
    private DssProjectOperateRecordMapper dssProjectOperateRecordMapper;

    @Override
    public void addOneRecord(ProjectOperateRecordBO recordVO) {
        ProjectOperateRecordDO recordDO=recordVO.toDO();
        dssProjectOperateRecordMapper.insert(recordDO);
    }

    @Override
    public String getRecordProjectName(String recordId) {
        return dssProjectOperateRecordMapper.getRecordProjectName(recordId);
    }

    @Override
    public void updateRecord(String recordId, ProjectOperateRecordStatusEnum statusEnum, String content) {
        ProjectOperateRecordDO recordDO= dssProjectOperateRecordMapper.getByRecordId(recordId);
        if(statusEnum!=null){
            recordDO.setStatus(statusEnum.getCode());
        }
        if(content!=null){
            recordDO.setContent(content);
        }
        recordDO.setCreateTime(new Date());
        dssProjectOperateRecordMapper.updateByRecordId(recordDO);
    }

    @Override
    public void attachExportResource(String recordId, BmlResource resource) {
        ProjectOperateRecordDO recordDO= dssProjectOperateRecordMapper.getByRecordId(recordId);
        recordDO.setResultResourceUri(new Gson().toJson(resource));
        recordDO.setCreateTime(new Date());
        dssProjectOperateRecordMapper.updateByRecordId(recordDO);
    }

    @Override
    public PageInfo<ProjectOperateRecordBO> queryRecords(Long projectId, int pageSize, int currentPage,
                                                         @Nullable  String recordId,
                                                         @Nullable ProjectOperateRecordStatusEnum statusFilter,
                                                         @Nullable ProjectOperateTypeEnum operateTypeFilter) {
        long total;
        List<ProjectOperateRecordDO> recordDOs;
        if(StringUtils.isNotBlank(recordId)){
            ProjectOperateRecordDO oneDo = dssProjectOperateRecordMapper.getByRecordId(recordId);
            recordDOs = Collections.singletonList(oneDo);
            total=1;
        }else {
            int offset = (currentPage - 1) * pageSize;
            int limit = pageSize;
            Integer status = Optional.ofNullable(statusFilter).map(ProjectOperateRecordStatusEnum::getCode).orElse(null);
            Integer operateType = Optional.ofNullable(operateTypeFilter).map(ProjectOperateTypeEnum::getCode).orElse(null);
            total = dssProjectOperateRecordMapper.getCount(projectId, operateType, status);
            recordDOs = dssProjectOperateRecordMapper.getRecords(projectId, limit, offset, operateType, status);
        }
        List<ProjectOperateRecordBO> records = recordDOs.stream()
                .map(ProjectOperateRecordBO::fromDO)
                .collect(Collectors.toList());
        for (ProjectOperateRecordBO record : records) {
            //只有导出任务并且已完成，采可以有下载操作。
            boolean canBeDownload = record.getOperateType() == ProjectOperateTypeEnum.EXPORT &&
                    record.getStatus() == ProjectOperateRecordStatusEnum.SUCCESS;
            record.setCanDownload(canBeDownload);
            if(canBeDownload){
                String content=record.getContent();
               String checkCode= new JsonParser().parse(content).getAsJsonObject().get("checkCode").getAsString();
               record.setCheckCode(checkCode);
            }
        }
        return new PageInfo<>(records,total);
    }

    @Override
    public BmlResource getExportResultByRecordId(String recordId) {
        ProjectOperateRecordDO recordDO= dssProjectOperateRecordMapper.getByRecordId(recordId);
        if(recordDO==null|| recordDO.getResultResourceUri()==null){
            throw new DSSRuntimeException("this operate record has no result,recordId："+recordId);
        }
        return new Gson().fromJson(recordDO.getResultResourceUri(), BmlResource.class);
    }

    @Override
    public int queryRunningTotal(ProjectOperateTypeEnum operateTypeEnum, Long workspaceId) {
        Integer operateType= Optional.ofNullable(operateTypeEnum).map(ProjectOperateTypeEnum::getCode).orElse(null);
        int status= ProjectOperateRecordStatusEnum.RUNNING.getCode();
        return dssProjectOperateRecordMapper.getCount(workspaceId,operateType,status);
    }

}
