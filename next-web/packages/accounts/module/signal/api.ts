import { request } from '@dataspherestudio/shared';
import type { BaseType } from './types';

const GetListNodeTypeApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/listNodeType', param);

const ProjectListApi = (): Promise<BaseType> =>
  request.fetch('dss/framework/project/listAllProjectName', {}, 'post');

const FlowAndNodeListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/queryFlowNameList', param);

const SourceTypeListApi = (): Promise<BaseType> =>
  request.fetch('dss/workflow/querySourceType', {});

const GetNodeTemplateListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/framework/orchestrator/getProjectTemplates', param);

const GetNodeTemplateDetail = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    'dss/framework/workspace/engineconf/getConfTemplateParamDetail',
    param
  );

const GetDataCheckerListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/queryDataCheckerNode', param, 'post');

const GetEventSenderListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/queryEventSenderNode', param, 'post');

const GetEventReciverListApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/queryEventReceiveNode', param, 'post');

const SaveSignalNodeApi = <T>(param: T): Promise<BaseType> =>
  request.fetch('dss/workflow/batchEditFlowNode', param, 'post');

const CheckIsViewApi = <T>(param: T): Promise<BaseType> =>
  request.fetch(
    'dss/datapipe/datasource/validateDataCheckerHasView',
    param,
    'post'
  );

export default {
  ProjectListApi,
  FlowAndNodeListApi,
  SourceTypeListApi,
  GetListNodeTypeApi,
  GetDataCheckerListApi,
  GetEventSenderListApi,
  GetEventReciverListApi,
  GetNodeTemplateListApi,
  GetNodeTemplateDetail,
  SaveSignalNodeApi,
  CheckIsViewApi,
};
