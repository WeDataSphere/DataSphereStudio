import { request } from '@dataspherestudio/shared';

// 项目名称
const ProjectListApi = <T>(): Promise<T> =>
  request.fetch('dss/framework/project/listAllProjectName', {}, 'post');

// 工作流、节点名称、模板名称
const MutipleTypeListApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/queryFlowNameList', param, 'get');

// 节点小类
const NodeTypeListApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/queryNodeTypeNameList', param, 'get');
// tableauId
const TableauIdListApi = <T>(): Promise<T> =>
  request.fetch('dss/workflow/queryViewId', {}, 'get');

// 数据可视化表格数据查询
const dataVisualTableApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/queryDataViewNode', param, 'post');
// 编辑节点 - 待完善
const editDataVisualApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/batchEditFlowNode', param, 'post');

// 跳转tableauUrl
const jumpToTableau = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/getAppConnNodeUrl', param, 'post');

// 获取节点类型
const NodeTypeDetailApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/listNodeType', param, 'get');
// 获取批量节点类型
const NodeTypeDetailBatchApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/listNodeType', param, 'post');
// 获取参数模板
const templateDatasApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/orchestrator/getProjectTemplates', param, 'get');
// 获取指定用户对应节点参数模板
const templateDatasByUserApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/orchestrator/getProjectTemplatesByUser', param);
// 获取指定参数模板详情
const GetNodeTemplateDetail = <T>(param: T): Promise<T> =>
  request.fetch(
    'dss/framework/workspace/engineconf/getConfTemplateParamDetail',
    param
  );
export default {
  ProjectListApi,
  MutipleTypeListApi,
  NodeTypeListApi,
  TableauIdListApi,
  dataVisualTableApi,
  editDataVisualApi,
  jumpToTableau,
  NodeTypeDetailApi,
  NodeTypeDetailBatchApi,
  templateDatasApi,
  templateDatasByUserApi,
  GetNodeTemplateDetail,
};
