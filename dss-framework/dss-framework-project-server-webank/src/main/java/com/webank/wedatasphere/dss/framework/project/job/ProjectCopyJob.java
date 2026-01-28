package com.webank.wedatasphere.dss.framework.project.job;

import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectCopyTask;
import com.webank.wedatasphere.dss.framework.project.entity.vo.OrchestratorCopyVO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectCopyVO;
import com.webank.wedatasphere.dss.framework.project.entity.response.ExportResult;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.workflow.entity.ProjectOrchestratorWhite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProjectCopyJob implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectCopyJob.class);

    private ProjectCopyEnv projectCopyEnv;
    private ProjectCopyVO projectCopyVO;

    @Override
    public void run() {
        try {
            importOrchestrator();
        } catch (Exception e) {
            LOGGER.error("Copy {} for {} error ", projectCopyVO.getProjectDO().getName(), projectCopyVO.getCopyProjectName(), e);
        }
    }

    public void importOrchestrator() throws Exception {
        LOGGER.info(">>>>>>>>>>>>>>>>>CopyProject Start: projectName:{},copyProjectName:{}<<<<<<<<<<<<<<<<<<",
                projectCopyVO.getProjectDO().getName(), projectCopyVO.getCopyProjectName());
        OrchestratorCopyVO orchestratorCopyVO = null;
        OrchestratorCopyJob orchestratorCopyJob = null;
        List<DSSOrchestratorInfo> orchestratorList = projectCopyVO.getOrchestratorList();
        if(orchestratorList==null){
            orchestratorList= Collections.emptyList();
        }
        DSSProjectCopyTask projectCopyTask = new DSSProjectCopyTask();
        projectCopyTask.setWorkspaceId(projectCopyVO.getProjectDO().getWorkspaceId());
        projectCopyTask.setStatus(0);
        projectCopyTask.setInstanceName(projectCopyVO.getInstanceName());
        projectCopyTask.setSumCount(orchestratorList.size());
        projectCopyTask.setSurplusCount(orchestratorList.size());
        projectCopyTask.setSourceProjectId(projectCopyVO.getProjectDO().getId());
        projectCopyTask.setSourceProjectName(projectCopyVO.getProjectDO().getName());
        projectCopyTask.setCopyProjectId(projectCopyVO.getCopyProjectId());
        projectCopyTask.setCopyProjectName(projectCopyVO.getCopyProjectName());
        projectCopyTask.setCreateBy(projectCopyVO.getUsername());
        projectCopyTask.setCreateTime(new Date());
        projectCopyTask.setErrorMsg("");
        //保存复制工程任务信息
        projectCopyEnv.getDssProjectCopyTaskMapper().insert(projectCopyTask);

        for (int i = 0; i < orchestratorList.size(); i++) {
            orchestratorCopyJob = new OrchestratorCopyJob();
            DSSOrchestratorInfo orc = orchestratorList.get(i);
            orchestratorCopyVO = new OrchestratorCopyVO();
            BeanUtils.copyProperties(projectCopyVO, orchestratorCopyVO);
            orchestratorCopyVO.setOrchestrator(orc);
            orchestratorCopyVO.setProjectDO(projectCopyVO.getProjectDO());
            orchestratorCopyVO.setCurrentNum(i + 1);
            orchestratorCopyVO.setSumCount(orchestratorList.size());
            orchestratorCopyVO.setCopyTaskId(projectCopyTask.getId());
            orchestratorCopyVO.setSourceWorkspace(projectCopyVO.getSourceWorkspace());
            orchestratorCopyVO.setTargetWorkspace(projectCopyVO.getTargetWorkspace());
            orchestratorCopyVO.setCopyProjectName(projectCopyVO.getCopyProjectName());
            orchestratorCopyVO.setCopyProjectId(projectCopyVO.getCopyProjectId());
            orchestratorCopyJob.setOrchestratorCopyVO(orchestratorCopyVO);
            orchestratorCopyJob.setProjectCopyEnv(projectCopyEnv);
            //先导出
            ExportResult exportResult = orchestratorCopyJob.exportOrc();
            if (exportResult != null) {
                //后导入
                Long targetOrchestratorId = orchestratorCopyJob.importOrc(exportResult);

                addWhiteProjectOrchestrator(targetOrchestratorId, orc);

            }
            //状态默认为导入中
            int status = 1;
            if (i == orchestratorList.size() - 1) {
                status = 2;
            }
            projectCopyEnv.getDssProjectCopyTaskMapper().updateCopyStatus(status, projectCopyTask.getId());
        }
        LOGGER.info(">>>>>>>>>>>>>>>>>CopyProject End: projectName:{},copyProjectName:{}<<<<<<<<<<<<<<<<<<",
                projectCopyVO.getProjectDO().getName(), projectCopyVO.getCopyProjectName());
    }

    public void setProjectCopyEnv(ProjectCopyEnv projectCopyEnv) {
        this.projectCopyEnv = projectCopyEnv;
    }

    public void setProjectCopyVO(ProjectCopyVO projectCopyVO) {
        this.projectCopyVO = projectCopyVO;
    }

    public void addWhiteProjectOrchestrator(Long targetOrchestratorId, DSSOrchestratorInfo orc){

        boolean isWhite = projectCopyEnv.getProjectOrchestratorWhiteService().checkProjectAndOrchestratorIsWhite(orc.getProjectId(),
                orc.getId());
        LOGGER.info("copy project addWhiteProjectOrchestrator orc info is [{},{},{}], targetOrc is [{},{},{}], isWhite is {}",
                orc.getProjectId(), orc.getId(), orc.getName(), targetOrchestratorId,
                projectCopyVO.getCopyProjectId(),projectCopyVO.getCopyProjectName(), isWhite);
        // 复制白名单项目,项目下的白名单编排复制后 添加到白名单中
        if (targetOrchestratorId != null &&  isWhite) {
            ProjectOrchestratorWhite projectOrchestratorWhite = new ProjectOrchestratorWhite();
            projectOrchestratorWhite.setOrchestratorId(targetOrchestratorId);
            projectOrchestratorWhite.setOrchestratorName(orc.getName());
            projectOrchestratorWhite.setProjectId(projectCopyVO.getCopyProjectId());
            projectOrchestratorWhite.setProjectName(projectCopyVO.getCopyProjectName());
            projectOrchestratorWhite.setCreateBy(projectCopyVO.getUsername());
            projectOrchestratorWhite.setUpdateBy(projectCopyVO.getUsername());
            projectCopyEnv.getProjectOrchestratorWhiteService().addProjectOrchestratorWhite(projectOrchestratorWhite);
        }

    }
}
