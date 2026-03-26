import { request } from '@dataspherestudio/shared';
import type { DataType } from './types';

const nodeInfoApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/getDataDevelopNodeContent', param, 'get');

const jumpToGitApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/orchestrator/gitUrl', param, 'get');
const dataVisualTableApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/workflow/queryDataDevelopNode', param, 'post');
const editDataVisualApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/project/modifyProjectMeta', param, 'post');
export default {
  nodeInfoApi,
  jumpToGitApi,
  dataVisualTableApi,
  editDataVisualApi,
};
