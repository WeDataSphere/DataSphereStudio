import { ref } from 'vue';
import api from '../api';
import type { DataType } from '../types/index';

export const useDataList = () => {
  const projectList = ref([]);
  const createUsers = ref([]);
  const editUsers = ref([]);
  const accessUsers = ref([]);
  const releaseUsers = ref([]);
  async function fetchProjectList(param: { workspaceId: string }) {
    try {
      const apiParam = { workspaceId: param.workspaceId };
      const res = await api.ProjectListApi<DataType>(apiParam);
      projectList.value = (res.data.data || []).map((item: string) => ({
        label: item,
        value: item,
      }));
    } catch (error) {
      console.log('fetch project error', error);
    }
  }

  async function fetchUserList(param: { workspaceId: string }) {
    try {
      const apiParam = { workspaceId: param.workspaceId };
      const res = await api.userListApi<DataType>(apiParam);

      const mapUsers = <T>(users: T[]) =>
        users.map((item: T) => ({
          value: item as string,
          label: item as string,
        }));

      accessUsers.value = mapUsers<string>(res.data.users.accessUsers) || [];
      createUsers.value = mapUsers<string>(res.data.users.createUsers) || [];
      editUsers.value = mapUsers<string>(res.data.users.editUsers) || [];
      releaseUsers.value = mapUsers<string>(res.data.users.releaseUsers) || [];
    } catch (error) {
      console.log('fetch user error', error);
    }
  }
  return {
    projectList,
    fetchProjectList,
    createUsers,
    releaseUsers,
    editUsers,
    accessUsers,
    fetchUserList,
  };
};
