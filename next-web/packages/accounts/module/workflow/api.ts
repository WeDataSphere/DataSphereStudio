import { request } from '@dataspherestudio/shared';
import type { BaseType } from './types';

const ProjectListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/project/listAllProjectName', param, 'post');

const FlowListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/getAllOrchestratorName', param);

const StatusListApi = (): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/getOrchestratorGitStatus', {});

const UserListApi = (): Promise<BaseType> =>
  request.fetch('dss/framework/workspace/getWorkspaceUserByRole', {});

const TemplateListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/getProjectTemplates', param);

const WorkflowListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    'dss/framework/orchestrator/getAllOrchestratorMeta',
    param,
    'post'
  );

const GetWorkflowTemplateListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    'dss/framework/orchestrator/getWrokflowDefaultTemplates',
    param
  );

const WorkflowModifyApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    'dss/framework/orchestrator/modifyOrchestratorMeta',
    param,
    'post'
  );

const SaveWorkflowTemplateApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/saveTemplateRef', param, 'put');

// 批量提交
const SubmitInitDiffFlowApi = <T extends { projectName: string }>(
  param: T
): Promise<BaseType> =>
  request.fetch(
    `dss/framework/orchestrator/diffFlowJob?projectName=${param.projectName}`,
    param,
    'post'
  );

const GetDiffFlowSubmitStatusApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/diffStatus', param);

const GetDiffFlowTreeApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/diffContent', param);

const GetDiffFlowCotentApi = <T extends { projectName: string }>(
  param: T
): Promise<BaseType> =>
  request.fetch(
    `dss/framework/orchestrator/diffFlowContent?projectName=${param.projectName}`,
    param,
    'post'
  );

const BatchSubmitDiffFlowCotentApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(`dss/framework/orchestrator/batchSubmitFlow`, param, 'post');

// 批量发布
const PublishInitDiffFlowApi = <T extends { projectName: string }>(
  param: T
): Promise<BaseType> =>
  request.fetch(
    `dss/framework/orchestrator/diffOrchestratorPublish?projectName=${param.projectName}`,
    param,
    'post'
  );

const BatchPublishDiffFlowCotentApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(`dss/workflow/batchPublishWorkflow`, param, 'post');

const ProxyUserDismissedApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(`dss/framework/workspace/isDismissed`, param, 'post');

const GetSubmitHistory = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/publish/history', param);

const BatchPublishFlowCheckApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    `dss/framework/orchestrator/batchPublishFlowCheck`,
    param,
    'post'
  );

export default {
  WorkflowListApi,
  WorkflowModifyApi,
  ProjectListApi,
  FlowListApi,
  StatusListApi,
  UserListApi,
  TemplateListApi,
  GetWorkflowTemplateListApi,
  SaveWorkflowTemplateApi,
  SubmitInitDiffFlowApi,
  GetDiffFlowSubmitStatusApi,
  GetDiffFlowTreeApi,
  GetDiffFlowCotentApi,
  BatchSubmitDiffFlowCotentApi,
  PublishInitDiffFlowApi,
  BatchPublishDiffFlowCotentApi,
  GetSubmitHistory,
  ProxyUserDismissedApi,
  BatchPublishFlowCheckApi,
};
