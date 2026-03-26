import { useI18n } from 'vue-i18n';
import { ref, computed } from 'vue';
import {
  fetchTemplateUser,
  fetchRelateUsers,
  fetchRelateUsersDepts,
  fetchEngineTypes,
  fetchworkFlowProjectNames,
  fetchworkFlowNames,
  fetchDepartments,
} from '../api';
import { utils } from '../hooks/utils';
import { useDateFormat } from '@vueuse/core';

export const useDataList = () => {
  const { t: $t } = useI18n();
  const templateUserList = ref([]);
  const allWorkSpaceUserList = ref([]);
  const allWorkSpaceUserDeptsList = ref([]);
  const allDeptList = ref([]);
  const engineList = ref([]);
  const projectNameList = ref([]);
  const workflowNameList = ref([]);
  const permissionTypeList = ref([
    {
      value: 0,
      label: $t('_.全部用户'),
    },
    {
      value: 1,
      label: $t('_.指定用户'),
    },
  ]);
  const { paramsObjectToString } = utils();

  const loadworkFlowNames = async (templateId: string) => {
    try {
      const data = await fetchworkFlowNames(templateId);
      workflowNameList.value = data?.orchestratorNames.map((v: string) => ({
        value: v,
        label: v,
      }));
    } catch (error) {
      console.log(error);
    }
  };

  const loadworkFlowProjectNames = async (templateId: string) => {
    try {
      const data = await fetchworkFlowProjectNames(templateId);
      projectNameList.value = data?.projectNames.map((v: string) => ({
        value: v,
        label: v,
      }));
    } catch (error) {
      console.log(error);
    }
  };

  const loadEngineList = async () => {
    try {
      const data = await fetchEngineTypes();
      engineList.value = data?.engineTypes.map((v: string) => ({
        value: v,
        label: v,
      }));
    } catch (error) {
      console.log(error);
    }
  };

  const loadAllWorkSpaceUserList = async (workspaceId: string) => {
    try {
      const data = await fetchRelateUsers(workspaceId);
      allWorkSpaceUserList.value = data?.users.accessUsers.map((v: string) => ({
        value: v,
        label: v,
      }));
    } catch (error) {
      console.log(error);
    }
  };

  const loadAllWorkSpaceUserDeptsList = async (workspaceId: string) => {
    try {
      const data = await fetchRelateUsersDepts(workspaceId);
      if (data && data.users && data.users.accessUsers) {
        allWorkSpaceUserDeptsList.value = data.users.accessUsers.map(
          (v: any) => v
        );
      } else {
        allWorkSpaceUserDeptsList.value = [];
      }
    } catch (error) {
      console.log(error);
    }
  };

  const loadAllDeptList = async () => {
    try {
      const data = await fetchDepartments();
      allDeptList.value = data?.departments.map((v: string) => ({
        value: v,
        label: v,
      }));
    } catch (error) {
      console.log(error);
    }
  };

  const loadTemplateUserList = async (templateId: string, name: string) => {
    const params = {
      pageNow: 1,
      pageSize: 50,
      templateId: templateId,
      username: name,
    };
    const strParams = paramsObjectToString(params);
    try {
      const res = await fetchTemplateUser(strParams);
      templateUserList.value = res.data.users.map((v: { name: string }) => ({
        value: v.name,
        label: v.name,
      }));
    } catch (error) {
      console.log(error);
    }
  };
  const allEngineList = computed(() => [
    { value: '*', label: $t('_.全局设置') },
    ...engineList.value,
  ]);

  interface ObjectType {
    [key: string]: any;
  }
  // 表格内容filter
  const fillText = (row: ObjectType) =>
    ['null', 'undefined', ''].includes(String(row.cellValue))
      ? '- -'
      : row.cellValue;

  // 表格内容filter
  const fillTimeText = (row: ObjectType) =>
    ['null', 'undefined', ''].includes(String(row.cellValue))
      ? '- -'
      : useDateFormat(row.cellValue as string, 'YYYY-MM-DD HH:mm:ss').value;

  const cascadUserList = ref([]);

  function processUserData(data: any[]) {
    // 定义一个递归处理函数
    function processItem(
      item: {
        child: any[];
        label: any;
        name: any;
        value: any;
        id: any;
        children: any;
      },
      level: number
    ) {
      // 如果当前项有child属性，说明它是一个父节点，需要递归处理子节点
      if (item.child && item.child.length > 0) {
        item.child.forEach((child) => {
          processItem(child, level + 1); // 递归处理子节点
        });
      }
      // 给当前项添加label和value字段，值为name字段的值
      item.label = item.name;
      if (level === 2) {
        item.value = item.name;
      } else {
        item.value = item.id;
      }
      item.children = item.child;
    }

    // 遍历并处理每一项
    data.forEach((item) => {
      processItem(item, 0);
    });
    cascadUserList.value = data;
  }

  return {
    templateUserList,
    allWorkSpaceUserList,
    engineList,
    allEngineList,
    permissionTypeList,
    projectNameList,
    workflowNameList,
    allDeptList,
    allWorkSpaceUserDeptsList,
    cascadUserList,
    processUserData,
    loadAllWorkSpaceUserDeptsList,
    loadAllDeptList,
    loadTemplateUserList,
    loadAllWorkSpaceUserList,
    loadEngineList,
    loadworkFlowProjectNames,
    loadworkFlowNames,
    fillText,
    fillTimeText,
  };
};
