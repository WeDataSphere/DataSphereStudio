import { request } from '@dataspherestudio/shared';

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

export default {
  ProjectTableApi,
  ProjectListApi,
  userListApi,
  editProjectApi,
  getUserByRoleApi,
};
