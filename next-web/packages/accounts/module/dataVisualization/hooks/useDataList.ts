import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import api from '../api';
import type { DataType } from '../types/index';
import { getUrlParams } from '@fesjs/traction-widget';

export const useDataList = () => {
  const { t: $t } = useI18n();
  const projectList = ref([]);
  const nodeTypeList = ref([]);
  const nodeNameList = ref([]);
  const tableauIdList = ref([]);
  const orchestratorNameList = ref([]);
  const templateNameList = ref([]);
  const nodeTypeDetailList = ref<DataType>([]);
  const isWhite = ref<boolean>(true);

  async function fetchProjectList() {
    try {
      const res = await api.ProjectListApi<DataType>();
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
  async function fetchMutipleTypeList(param: { groupNameEn: string }) {
    try {
      const apiParam = { groupNameEn: param.groupNameEn };
      const res = await api.MutipleTypeListApi<DataType>(apiParam);
      nodeNameList.value = (res.data.nodeNameList || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
      orchestratorNameList.value = (res.data.orchestratorNameList || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
      templateNameList.value = (res.data.templateNameList || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
    } catch (error) {
      console.log('fetch node type error', error);
    }
  }

  async function fetchNodeTypeList(param: { groupNameEn: string }) {
    try {
      const apiParam = { groupNameEn: param.groupNameEn };
      const res = await api.NodeTypeListApi<DataType>(apiParam);
      nodeTypeList.value = (res.data.data || [])
        .filter(Boolean)
        .map((item: string) => ({
          label: item,
          value: item,
        }));
    } catch (error) {
      console.log('fetch node type error', error);
    }
  }

  async function fetchTableauIdList() {
    try {
      const res = await api.TableauIdListApi<DataType>();
      tableauIdList.value = (res.data.data || []).map((item: string) => ({
        label: item,
        value: item,
      }));
    } catch (error) {
      console.log('fetch tableau id error', error);
    }
  }

  async function fetchNodeTypeDetail<T>(param: T, type = 'single') {
    try {
      if (type === 'single') {
        const res: any = await api.NodeTypeDetailApi(param);
        nodeTypeDetailList.value = res.data.nodeTypes || [];
        isWhite.value = res.data.isWhite || false;
      } else if (type === 'batch') {
        const res: any = await api.NodeTypeDetailBatchApi(param);
        nodeTypeDetailList.value = res.data.nodeTypes || [];
        isWhite.value = res.data.isWhite || false;
      }
    } catch (error) {
      console.log('fetch tableau id error', error);
    }
  }

  // 表格内容filter
  const fillText = (row: { [index: string]: unknown }) =>
    ['null', 'undefined', ''].includes(String(row.cellValue))
      ? '--'
      : row.cellValue;
  const fillBoolText = (row: { [index: string]: unknown }) =>
    ['null', 'undefined', ''].includes(String(row.cellValue))
      ? '--'
      : row.cellValue
      ? $t('_.是')
      : $t('_.否');
  // 获取当前工作流的环境
  const getCurrentDsslabels = () => {
    const queryParam = getUrlParams();
    return queryParam.labels
      ? queryParam.labels
      : sessionStorage.getItem('currentDssLabels')
      ? sessionStorage.getItem('currentDssLabels')
      : 'dev';
  };
  return {
    projectList,
    nodeTypeList,
    nodeNameList,
    tableauIdList,
    orchestratorNameList,
    templateNameList,
    nodeTypeDetailList,
    isWhite,
    fetchMutipleTypeList,
    fetchProjectList,
    fetchNodeTypeList,
    fetchTableauIdList,
    fillText,
    fillBoolText,
    fetchNodeTypeDetail,
    getCurrentDsslabels,
  };
};
