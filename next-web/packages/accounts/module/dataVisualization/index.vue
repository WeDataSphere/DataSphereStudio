<template class="project-accounts">
  <BTablePage>
    <template #search>
      <BSearch
        v-model:form="searchForm"
        :is-reset="false"
        @search="fetchTableDataMain"
      >
        <template #form>
          <div class="tag-select-long">
            <span class="condition-label">{{ $t('common.projectName') }}</span>
            <FSelect
              v-model="searchForm.projectNameList"
              :options="projectList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{ $t('common.workflowName') }}</span>
            <FSelect
              v-model="searchForm.orchestratorName"
              :options="orchestratorNameList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{ $t('_.节点小类') }}</span>
            <FSelect
              v-model="searchForm.nodeTypeNameList"
              :options="nodeTypeList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select-long">
            <span class="condition-label">{{ $t('_.节点名称') }}</span>
            <FSelect
              v-model="searchForm.nodeNameList"
              :options="nodeNameList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-input">
            <span class="condition-label">{{
              $t('_.Tableau视图ID或数据源ID')
            }}</span>
            <FSelect
              v-model="searchForm.viewId"
              :options="tableauIdList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
            />
          </div>
        </template>
        <template #exButton>
          <FButton class="reset" @click="handleReset">
            {{ $t('common.reset') }}
          </FButton>
        </template>
      </BSearch>
    </template>
    <template #table>
      <FTable ref="tableRef" :data="tableList">
        <template #empty>
          <BPageLoading :action-type="actionType" />
        </template>
        <f-table-column
          prop="projectName"
          :label="$t('common.projectName')"
          :width="184"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="orchestratorName"
          :label="$t('common.workflowName')"
          :width="184"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="nodeName"
          :label="$t('common.nodeName')"
          :width="318"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="nodeTypeName"
          :label="$t('common.nodeSmallType')"
          :width="164"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="nodeDesc"
          :label="$t('common.nodeDesc')"
          :width="204"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          v-slot="{ row }"
          prop="viewId"
          :label="$t('accounts.bindTableauViewIdOrDatasourceId')"
          :width="language === 'zh-CN' ? 234 : 300"
          ellipsis
        >
          <span
            v-if="row.nodeTypeName === 'tableau'"
            :style="{ cursor: 'pointer', color: '#5384ff' }"
            @click="jumpToTableau(row)"
            >{{ handleShowId(row) }}</span
          >

          <span v-else>{{ handleShowId(row) }}</span>
        </f-table-column>
        <f-table-column
          prop="nodeId"
          :label="$t('common.nodeId')"
          :width="language === 'zh-CN' ? 264 : 300"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          v-slot="{ row }"
          :label="$t('common.action')"
          align="center"
          fixed="right"
          :width="language === 'zh-CN' ? 148 : 166"
          ellipsis
        >
          <span
            v-if="row.editable"
            class="operate-link"
            @click="handleEdit('edit', row)"
            >{{ $t('common.edit') }}</span
          >
          <span v-else class="operate-link-disabled">{{
            $t('common.edit')
          }}</span>
          <span class="operate-link" @click="jumpToworkflow(row)">{{
            $t('_.查看工作流')
          }}</span>
        </f-table-column>
      </FTable>
    </template>
    <template #pagination>
      <FPagination
        v-model:currentPage="pagination.current"
        v-model:pageSize="pagination.size"
        show-size-changer
        show-total
        :total-count="pagination.total"
        @change="fetchTableDataMain"
        @page-size-change="fetchTableDataMain"
      />
    </template>
  </BTablePage>
  <!-- 编辑弹窗 -->
  <EditDataVisualDrawer
    v-if="currentShow"
    v-model:show="currentShow"
    :config="currentConfig"
    :form="currentForm"
    node-parent-type="数据可视化"
    @success="fetchTableDataMain"
  />
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { onMounted, ref, reactive } from 'vue';
import { FTable } from '@fesjs/fes-design';
import { getUrlParams } from '@fesjs/traction-widget';
import { useDataList } from './hooks/useDataList';
import type { DataType, SearchFormType } from './types/index';
import api from './api';
import EditDataVisualDrawer from './components/editDataVisualDrawer.vue';

const { t: $t } = useI18n();
const language = localStorage.getItem('locale');
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});
const {
  projectList,
  nodeTypeList,
  nodeNameList,
  tableauIdList,
  orchestratorNameList,
  fetchMutipleTypeList,
  fetchProjectList,
  fetchNodeTypeList,
  fetchTableauIdList,
  fillText,
} = useDataList();

const query = ref({
  workspaceId: '',
  label: '',
});
const initDataVisualSearchForm = (): SearchFormType => ({
  projectNameList: [],
  orchestratorName: '',
  nodeTypeNameList: [],
  nodeNameList: [],
  viewId: '',
  // sortBy: 'createTime',
  // orderBy: 'descend',
});
const searchForm = ref(initDataVisualSearchForm());
const tableList = ref<DataType[]>([]);
const isLoading = ref(false);
const actionType = ref<string>('loading');
const tableRef = ref<typeof FTable | null>(null);

// 获取表格数据
const fetchAllTableData = async () => {
  const params = {
    pageNow: pagination.current,
    pageSize: pagination.size,
    ...searchForm.value,
  };
  // console.log('fetchAllTableData-params', searchForm.value);
  actionType.value = 'loading';
  tableList.value = [];
  try {
    if (!isLoading.value) {
      isLoading.value = true;
      const res: DataType = await api.dataVisualTableApi(params);
      tableList.value = res.data?.data || [];
      if (res.data.total === 0) {
        actionType.value = 'emptyQueryResult';
      }
      pagination.total = res.data.total;
      isLoading.value = false;
    }
  } catch (err) {
    console.error(err);
    isLoading.value = false;
  }
};
// 获取表格后刷新排序状态
const fetchTableDataMain = async () => {
  await fetchAllTableData();
  // if (
  //   tableList.value.length > 0 &&
  //   tableRef.value &&
  //   searchForm.value.sortBy &&
  //   searchForm.value.orderBy
  // ) {
  //   tableRef.value?.sort(searchForm.value.sortBy, searchForm.value.orderBy);
  // }
};

// 重置操作
const handleReset = () => {
  pagination.current = 1;
  searchForm.value = initDataVisualSearchForm();
  fetchTableDataMain();
};

// 判断排序参数是否和上次相同
// const compareSortParams = (prop: string, order: string) => {
//   if (prop !== searchForm.value.sortBy || order !== searchForm.value.orderBy) {
//     return false;
//   }
//   return true;
// };

// 表格排序
// const tableSort = async (sortParam: { prop: string; order: string }) => {
//   const tempOrder = sortParam.order ? sortParam.order : 'descend';
//   if (!compareSortParams(sortParam.prop, tempOrder)) {
//     searchForm.value.sortBy = sortParam.prop;
//     searchForm.value.orderBy = tempOrder;
//     await fetchTableDataMain();
//   }
// };

// 编辑弹窗
const currentShow = ref<boolean | null>(null);
const currentForm = ref<DataType | null>(null);
const currentConfig = ref<DataType>({ type: 'edit' });

const handleEdit = (type: string, row: DataType) => {
  currentForm.value = { ...row };
  currentShow.value = true;
  currentConfig.value = {
    type: type,
    nodeTypeName: currentForm.value.nodeTypeName,
    workspaceId: query.value.workspaceId,
    tableauIdList,
  };
};

const jumpToTableau = async (row: DataType) => {
  // console.log('jumpToTableau-row', row);
  const params = {
    flowId: row.flowId,
    labels: {
      route: 'dev',
    },
    params: {
      desc: row.nodeDesc,
      title: row.nodeName,
      // refProjectId: row.nodeContent.refProjectId,
      // viewId: row.viewId,
      ...row.nodeContent,
    },
    nodeType: row.nodeType,
    projectID: row.projectId,
  };
  console.log('jumpToTableau-params', params);
  try {
    const res = await api.jumpToTableau(params);
    if (res && res.data && res.data.jumpUrl) {
      window.open(res.data.jumpUrl, '_blank');
    }
  } catch (err) {
    console.error(err);
  }
};
const handleShowId = (row) => {
  if (row.nodeTypeName === 'tableauDataRefre') {
    return row?.nodeContent?.datasourceId || '--';
  }
  if (row.nodeTypeName === 'tableau') {
    return row?.nodeContent?.viewId || '--';
  }
  return '--';
};

const jumpToworkflow = (row: any) => {
  const localUrl = window.location.href;
  const baseUrl = localUrl.split('next-web')[0] + '#/workflow?';
  const workspaceId = query.value.workspaceId;
  const params = `workspaceId=${workspaceId}&projectID=${row.projectId}&projectName=${row.projectName}&flowId=${row.orchestratorId}&appId=${row.flowId}&nodeName=${row.nodeName}`;
  const url = baseUrl + params;
  window.open(url, '_blank');
};

onMounted(async () => {
  query.value = getUrlParams() || {};
  fetchMutipleTypeList({ groupNameEn: 'Data visualization' });
  fetchProjectList();
  fetchNodeTypeList({ groupNameEn: 'Data visualization' });
  fetchTableauIdList();
  fetchTableDataMain();
});
</script>
<style lang="less" scoped>
.operate-link {
  cursor: pointer;
  color: #5384ff;
  margin-right: 6px;
}
.operate-link-disabled {
  color: #b7b7bc;
  margin-right: 6px;
}
.project-accounts {
  .fes-popper-wrapper {
    background: red;
  }
}
.tag-select-long {
  :deep(.fes-select) {
    width: 265px;
  }
  :deep(.fes-tag) {
    max-width: 160px;
  }
}
.tag-select {
  :deep(.fes-select) {
    width: 215px;
  }
  :deep(.fes-tag) {
    max-width: 110px;
  }
}
.tag-input {
  :deep(.fes-input) {
    width: 180px;
  }
}
.res-tooltip-style {
  color: #0f1222;
  background-color: #ffff;
}
.card-wrapper {
  min-width: 541px;
  :deep(.fes-card__header) {
    padding: 0;
  }
}
</style>
