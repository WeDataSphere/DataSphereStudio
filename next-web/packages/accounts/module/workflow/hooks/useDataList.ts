import { ref } from 'vue';
import api from '../api';
import type { TemplateNodeItemType, TemplateItemType } from '../types/index';

export const useDataList = () => {
  const projectList = ref([]);
  const flowList = ref([]);
  const statusList = ref([]);
  const userList = ref([]);
  const templateList = ref<TemplateItemType[]>([]);
  const flowTemplateList = ref([]);

  async function fetchProjectList(param: { workspaceId: string }) {
    try {
      const apiParam = { workspaceId: param.workspaceId };
      const res = await api.ProjectListApi<{ workspaceId: string }>(apiParam);
      projectList.value = (res.data.data || []).map((item: string) => ({
        label: item,
        value: item,
      }));
    } catch (error) {
      console.log('fetch project error', error);
    }
  }

  async function fetchFlowList(param: { workspaceId: string }) {
    try {
      const apiParam = { workspaceId: param.workspaceId };
      const res = await api.FlowListApi<{ workspaceId: string }>(apiParam);
      flowList.value = (res.data.data || []).map((item: string) => ({
        label: item,
        value: item,
      }));
    } catch (error) {
      console.log('fetch flow error', error);
    }
  }

  async function fetchStatusList() {
    try {
      const res = await api.StatusListApi();
      statusList.value = (res.data?.status || []).map(
        (item: { status: string; name: string }) => ({
          label: item.name,
          value: item.status,
        })
      );
    } catch (error) {
      console.log('fetch status error', error);
    }
  }

  async function fetchUserList() {
    try {
      const res = await api.UserListApi();
      userList.value = (res.data?.users || []).map((item: string) => ({
        label: item,
        value: item,
      }));
    } catch (error) {
      console.log('fetch user error', error);
    }
  }

  async function fetchTemplateList(param: { projectId: string }) {
    try {
      const res = await api.TemplateListApi<{ projectId: string }>(param);
      templateList.value = res.data?.templates || [];
    } catch (error) {
      console.log('fetch template error', error);
    }
  }

  async function fetchFlowTemplateList(
    param: { orchestratorId: string },
    cb: (result: TemplateNodeItemType[]) => void
  ) {
    try {
      const res = await api.GetWorkflowTemplateListApi<{
        orchestratorId: string;
      }>(param);
      flowTemplateList.value = res.data?.wrokflowDefaultTemplates || [];
      cb && cb(flowTemplateList.value);
    } catch (error) {
      console.log('fetch flow template error', error);
    }
  }

  return {
    projectList,
    flowList,
    statusList,
    userList,
    templateList,
    flowTemplateList,
    fetchProjectList,
    fetchFlowList,
    fetchStatusList,
    fetchUserList,
    fetchTemplateList,
    fetchFlowTemplateList,
  };
};
