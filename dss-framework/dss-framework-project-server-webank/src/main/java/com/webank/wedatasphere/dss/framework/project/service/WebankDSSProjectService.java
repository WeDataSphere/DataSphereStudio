package com.webank.wedatasphere.dss.framework.project.service;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.framework.project.entity.request.BatchExportOrchestratorsRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.entity.vo.DSSProjectVo;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectCopyRequest;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectInfoVo;
import com.webank.wedatasphere.dss.framework.project.entity.OrchestratorBatchImportInfo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import java.util.function.Consumer;

import java.util.List;
import java.util.Map;

/**
 * @author: zorezeng on 2021/8/24
 * @description:
 */
public interface WebankDSSProjectService {
    /**
     * 查询已删除工程list
     * @param projectRequest
     * @return
     */
    List<ProjectResponse> getDeletedProjects(ProjectQueryRequest projectRequest);

    /**
     * 复制工程
     * @param projectCopyRequest
     * @param username

     * @throws Exception
     */
    DSSProjectVo copyProject(ProjectCopyRequest projectCopyRequest, String username,String proxyUser,
                             Workspace sourceWorkspace,Workspace targetWorkspace) throws Exception;

    /**
     * 批量导出工程内的指定编排
     * @return 导出记录的id
     */
    String  exportOrchestrators(BatchExportOrchestratorsRequest batchExportOrchestratorsRequest,String username,String proxyUser,Workspace workspace) throws Exception;

    /**
     * 批量导入编排到指定工程
     * @param  projectInfo 要导入的目标工程
     * @param importResource 导入的编排资源
     * @param username 导入人
     * @param checkCode  校验码
     * @param packageInfo 导入包路径或者文件名
     * @param envLabel 环境标签
     * @param workspace 导入工作空间
     * @param async 是否异步
     */
    void importOrchestrators(ProjectInfoVo projectInfo, BmlResource importResource, String username, String proxyUser,
        String checkCode,String packageInfo, EnvDSSLabel envLabel, Workspace workspace,
                             List<Consumer<OrchestratorBatchImportInfo>> importHook,
                             boolean async,String comment) throws Exception;

    /**
     * 从生产中心发布整个项目到调度系统
     */
    void publishOrchestratorsDirectly(RequestFrameworkConvertOrchestration convertOrchestrationReques,String proxyUser);
    /**
     * @Author: bradyli
     * @Date: 2021/11/2
     * @Description: 基于用户级别的修改
     * @Param:
     * @return:
     **/
    Map<String, String> handoverWorkflowsOnUserLevel(String transferor, String recipient, Workspace workspace) throws Exception;

    /**
     * @Author: bradyli
     * @Date: 2021/10/27
     * @Description:  修改用户指定的工作空间下的工作流的username，将transferor修改成recipient--基于工作空间级别的修改
     * @Param:
     * @return:
     **/
    Map<String, String> handoverWorkflowsOnWorkspaceLevel(JsonObject handInfo, String transferor, String recipient, Workspace workspace) throws Exception;

    /**
     * @Author: bradyli
     * @Date: 2021/10/27
     * @Description:  修改用户指定的工程下的工作流的username，将transferor修改成recipient--基于工程级别的修改
     * @Param:
     * @return:
     **/
    Map<String, String> handoverWorkflowsOnProjectLevel(String transferor, String recipient, JsonObject handInfo, Workspace workspace) throws Exception;
}
