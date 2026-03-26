import { ref } from 'vue';
import api from '../api';

export const useDataList = () => {
  const projectList = ref([]);
  const flowList = ref([]);
  const nodeList = ref([]);
  const sourceTypeList = ref([]);

  async function fetchProjectList() {
    try {
      const res = await api.ProjectListApi();
      projectList.value = (res.data.data || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
    } catch (error) {
      console.log('fetch project error', error);
    }
  }

  async function fetchFlowAndNodeList(param: {
    groupNameEn: string;
    nodeTypeName: string;
  }) {
    try {
      const res = await api.FlowAndNodeListApi(param);
      flowList.value = (res.data?.orchestratorNameList || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
      nodeList.value = (res.data?.nodeNameList || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
    } catch (error) {
      console.log('fetch node error', error);
    }
  }

  async function fetchSourceTypeList() {
    try {
      const res = await api.SourceTypeListApi();
      sourceTypeList.value = (res.data?.data || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
    } catch (error) {
      console.log('fetch source type error', error);
    }
  }

  return {
    sourceTypeList,
    projectList,
    flowList,
    nodeList,
    fetchProjectList,
    fetchFlowAndNodeList,
    fetchSourceTypeList,
  };
};
