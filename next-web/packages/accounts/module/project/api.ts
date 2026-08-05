import { request } from '@dataspherestudio/shared';
import type { AxiosResponse } from 'axios';
import type { ProjectDetailVO, SearchFormType } from './types/index';

const ProjectTableApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/project/queryAllProjects', param, 'post');
const ProjectListApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/project/listAllProjectName', param, 'post');
const userListApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/workspace/getAllWorkspaceUsers', param, 'get');
const editProjectApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/project/modifyProjectMeta', param, 'post');
const getUserByRoleApi = <T>(param: T): Promise<T> =>
  request.fetch('dss/framework/workspace/getWorkspaceUserByRole', param, 'get');

/**
 * [新增] 获取项目资产详情（基础信息 + 工作流列表 + 节点类型分布 + 数据源摘要）
 * GET dss/framework/project/asset/detail?projectId=&workspaceId=
 */
const getProjectDetailApi = (param: {
  projectId: number;
  workspaceId: string;
}): Promise<{ data: { detail?: ProjectDetailVO } }> =>
  request.fetch('dss/framework/project/asset/detail', param, 'get');

/**
 * [新增] CSV 导出（流式 blob 下载）
 *
 * 直接使用 axios 实例，绕开 request.fetch 的 JSON success 拦截器
 * （拦截器对 Blob 执行 data.status!==0 判断会误判为错误）。
 * 成功返回 AxiosResponse（data 为 Blob）；
 * 后端超限时返回 application/json 错误体，调用方按 content-type 判别。
 */
const exportProjectsApi = (
  param: Partial<SearchFormType> & Record<string, any>
): Promise<AxiosResponse<Blob>> =>
  request.instance.post('dss/framework/project/asset/export', param, {
    responseType: 'blob',
  });

export default {
  ProjectTableApi,
  ProjectListApi,
  userListApi,
  editProjectApi,
  getUserByRoleApi,
  getProjectDetailApi,
  exportProjectsApi,
};
