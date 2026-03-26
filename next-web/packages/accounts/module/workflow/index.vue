<template>
  <BTablePage
    :is-loading="isLoading"
    action-type="loading"
    class="dss-table-page"
  >
    <template #search>
      <BSearch
        v-model:form="searchForm"
        @search="handleFormSearch"
        @reset="handleFormReset"
      >
        <template #form>
          <div class="tag-select-long">
            <span class="condition-label">{{ $t('_.项目名称') }}</span>
            <FSelect
              v-model="searchForm.projectList"
              :options="projectList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
              multiple
              collapse-tags
              :collapse-tags-limit="1"
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{ $t('_.工作流名称') }}</span>
            <FSelect
              v-model="searchForm.orchestratorName"
              :options="flowList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{ $t('_.状态') }}</span>
            <FSelect
              v-model="searchForm.status"
              :options="statusList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
            />
          </div>
        </template>
      </BSearch>
    </template>
    <template #operate>
      <template v-if="['prepare', 'pendding'].includes(operStatus)">
        <FButton
          type="primary"
          :disabled="operStatus === 'pendding'"
          @click="handleBatch"
        >
          {{ currentBatch?.text }}
        </FButton>
        <FButton
          :disabled="operStatus === 'pendding'"
          @click="selectionReset('all')"
        >
          {{ $t('_.取消') }}
        </FButton>
      </template>
      <template v-else>
        <FButton
          type="primary"
          @click="handleBtnClick('submit', 'batchSubmit')"
        >
          {{ $t('_.批量提交') }}
        </FButton>
        <FButton
          type="primary"
          @click="handleBtnClick('publish', 'batchPublish')"
        >
          {{ $t('_.批量发布') }}
        </FButton>
      </template>
    </template>
    <template #table>
      <FTable
        ref="tableRef"
        :data="tableList"
        @selectionChange="selectionChange"
        @sort-change="tableSort"
      >
        <template v-for="(item, index) in tableColumns">
          <FTable-column
            v-if="item.type === 'selection'"
            :key="'selection' + index"
            type="selection"
            :visible="['prepare', 'pendding'].includes(operStatus)"
          />
          <FTable-column
            v-else-if="item.type === 'text'"
            :key="item.prop + 'text' + index"
            :label="item.label"
            :prop="item.prop"
            :ellipsis="item.ellipsis"
            :width="item.minWidth"
            :formatter="item.formatter"
          />
          <FTable-column
            v-else-if="item.type === 'time'"
            :key="item.prop + 'time' + index"
            :label="item.label"
            :prop="item.prop"
            :ellipsis="item.ellipsis"
            :width="item.minWidth"
            :formatter="item.formatter"
            :sortable="item.sortable"
          />
          <FTable-column
            v-else-if="item.type === 'link'"
            v-slot="{ row }"
            :key="'link' + index"
            :label="item.label"
            :prop="item.prop"
            :ellipsis="item.ellipsis"
            :width="item.minWidth"
            :formatter="item.formatter"
          >
            <span :style="setStatusStyle(row[item.prop as string])">{{
              row[item.text as string] || '- -'
            }}</span>
          </FTable-column>
          <FTable-column
            v-else-if="item.type === 'action'"
            v-slot="{ row }"
            :key="item.prop"
            :label="item.label"
            :width="language === 'zh-CN' ? 60 : 96"
            fixed="right"
          >
            <span
              :style="setEditStyle(row.editable)"
              @click="handleBtnClick('edit', 'editWorkflow', row)"
              >{{ $t('_.编辑') }}</span
            >
          </FTable-column>
        </template>
      </FTable>
    </template>
    <template #pagination>
      <FPagination
        show-total
        :page-size="pagination.pageSize"
        :current-page="pagination.pageNow"
        show-size-changer
        :page-size-option="[10, 20, 50, 100]"
        :total-count="pagination.totalCount"
        @change="handlePageChange"
      />
    </template>
  </BTablePage>
  <component
    :is="currentComp"
    v-model:show="currentShow"
    :config="currentConfig"
    :form="currentForm"
    @success="handleFormSearch"
    @cancel="resetOperStatus"
  />
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { onMounted, ref, shallowRef, nextTick, type Component } from 'vue';
import { FTable, FMessage } from '@fesjs/fes-design';
import { getUrlParams } from '@fesjs/traction-widget';
import {
  usePagination,
  type PaginationAndParams,
} from '../hooks/usePagination';
import { useBatchOperations } from '../hooks/useBatchOperations';
import { useDataList } from './hooks/useDataList';
import type {
  BaseType,
  ColumnType,
  SearchFormType,
  StatusStyleType,
} from './types/index';
import api from './api';
import editWorkflow from './components/editWorkflow.vue';
import batchSubmit from './components/batchSubmit.vue';
import batchPublish from './components/batchPublish.vue';

const { t: $t } = useI18n();
const language = localStorage.getItem('locale');
const {
  projectList,
  flowList,
  statusList,
  fetchProjectList,
  fetchFlowList,
  fetchStatusList,
} = useDataList();

const query = ref({
  workspaceId: '',
  labels: '',
});
const init = (): SearchFormType => ({
  orchestratorName: '',
  projectList: [],
  status: '',
  sortBy: 'updateTime',
  orderBy: 'descend',
});
const searchForm = ref(init());
const tableList = ref<BaseType[]>([]);

const loadTable = async (param: PaginationAndParams) => {
  const { sortBy, ...apiParam } = param;
  apiParam.workspaceId = query.value.workspaceId;
  const res: BaseType = await api.WorkflowListApi(apiParam);
  tableList.value = res.data?.data || [];
  return {
    totalPage: Math.ceil(res.data.total / (param.pageSize as number)),
    totalCount: res.data.total,
    pageNow: param.pageNow,
  };
};

const {
  isLoading,
  pagination,
  handleInit,
  handleCurrent,
  handleCurrentChange,
  fillText,
  fillTimeText,
} = usePagination(searchForm, loadTable);

// 批量操作
const {
  tableRef,
  operStatus,
  selectionReset,
  resetOperStatus,
  selectionChange,
  selectionValidate,
} = useBatchOperations();

const handleFormSearch = async () => {
  selectionReset('all');
  await handleInit();
  tableRef.value?.sort(searchForm.value.sortBy, searchForm.value.orderBy);
};
const handleFormReset = async () => {
  await nextTick();
  searchForm.value = init();
  await handleFormSearch();
};
const handlePageChange = async (currentPage: number, pageSize: number) => {
  selectionReset('row');
  await handleCurrentChange(currentPage, pageSize);
  tableRef.value?.sort(searchForm.value.sortBy, searchForm.value.orderBy);
};
const compareSortParams = (prop: string, order: string) => {
  return prop === searchForm.value.sortBy && order === searchForm.value.orderBy;
};
const tableSort = async (sortParam: {
  prop: string;
  order: 'ascend' | 'descend';
}) => {
  const tempOrder = sortParam.order ? sortParam.order : 'descend';
  if (!compareSortParams(sortParam.prop, tempOrder)) {
    searchForm.value.sortBy = sortParam.prop;
    searchForm.value.orderBy = tempOrder;
    selectionReset('row');
    await handleCurrent();
    tableRef.value?.sort(searchForm.value.sortBy, searchForm.value.orderBy);
  }
};

onMounted(() => {
  query.value = getUrlParams() || {};
  fetchProjectList(query.value);
  fetchFlowList(query.value);
  fetchStatusList();
  handleFormSearch();
});

// 编辑和批量提交、发布
const componentMap: { [key: string]: Component } = {
  editWorkflow,
  batchSubmit,
  batchPublish,
};

const btnMap: BaseType = {
  submit: { text: $t('_.确认提交'), name: 'batchSubmit' },
  publish: { text: $t('_.确认发布'), name: 'batchPublish' },
};

const currentBatch = ref<BaseType | null>(null);
const currentShow = ref<{ [key: string]: boolean } | null>(null);
const currentForm = ref<BaseType | null>(null);
const currentConfig = ref<BaseType>({ type: 'edit' });
const currentComp = shallowRef<Component | null>(null);

const handleBtnClick = (
  type: string,
  name: string,
  row: BaseType | null = null
): void => {
  currentConfig.value = {
    workspaceId: query.value.workspaceId,
    labels: query.value.labels,
  };
  if (['submit', 'publish'].includes(type)) {
    operStatus.value = 'prepare';
    currentShow.value = { [name]: false };
    currentBatch.value = btnMap[type];
  } else {
    currentShow.value = { [name]: true };
    currentForm.value = { ...row };
    currentComp.value = componentMap[name];
  }
};

const handleBatch = async () => {
  try {
    const compnentName = currentBatch.value?.name;
    const rows = await selectionValidate((rows) => {
      if (
        compnentName === 'batchSubmit' &&
        rows.some((item) => item.status !== 'save')
      ) {
        FMessage.warn($t('_.批量提交只能选择待提交的工作流'));
        return false;
      } else if (
        compnentName === 'batchPublish' &&
        rows.some(
          (item) =>
            item.status &&
            !['stateless', 'push', 'publish'].includes(item.status)
        )
      ) {
        FMessage.warn($t('_.批量发布只能选择待发布、已发布或无状态的工作流'));
        return false;
      } else if (rows.some((flow) => !flow.editable)) {
        FMessage.warn(
          `${
            compnentName === 'batchPublish'
              ? $t('_.批量发布')
              : $t('_.批量提交')
          }${$t('_.存在无编辑权限的工作流，请重新选择')}`
        );
        return false;
      }
      return true;
    });
    currentForm.value = { rows };
    if (compnentName) {
      currentShow.value = { [compnentName]: true };
      currentComp.value = componentMap[compnentName];
    }
  } catch (error) {
    console.log('bacth select error', error);
  }
};

const setStatusStyle = (status: string): StatusStyleType => {
  let color: '#b7b7bc' | '#5384ff' | '#ff4d4f' = '#b7b7bc';
  if (['push', 'save', 'success'].includes(status)) {
    color = '#5384ff';
  } else if (['failed'].includes(status)) {
    color = '#ff4d4f';
  }
  return { color };
};

const setEditStyle = (isActive: boolean): StatusStyleType => {
  const editStyle: StatusStyleType = {
    color: isActive ? '#5384ff' : '#b7b7bc',
  };
  if (isActive) {
    editStyle['cursor'] = 'pointer';
  } else {
    editStyle['pointer-events'] = 'none';
  }
  return editStyle;
};

const tableColumns = ref<ColumnType[]>([
  {
    type: 'selection',
    minWidth: 30,
  },
  {
    type: 'text',
    prop: 'projectName',
    label: $t('_.项目名称'),
    ellipsis: true,
    minWidth: language === 'zh-CN' ? 116 : 128,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'orchestratorName',
    label: $t('_.工作流名称'),
    ellipsis: true,
    minWidth: 180,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'description',
    label: $t('_.工作流描述'),
    ellipsis: true,
    minWidth: 180,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'orchestratorId',
    label: $t('_.工作流ID'),
    ellipsis: true,
    minWidth: 116,
    formatter: fillText,
  },
  {
    type: 'link',
    prop: 'status',
    text: 'statusName',
    label: $t('_.状态'),
    ellipsis: true,
    minWidth: 116,
    formatter: fillText,
  },
  {
    type: 'link',
    prop: 'newStatus',
    text: 'newStatusName',
    label: $t('_.最近一次发布状态'),
    ellipsis: true,
    minWidth: language === 'zh-CN' ? 170 : 198,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'version',
    label: $t('_.已发布版本'),
    ellipsis: true,
    minWidth: language === 'zh-CN' ? 116 : 148,
    formatter: fillText,
  },
  {
    type: 'time',
    prop: 'updateTime',
    label: $t('_.已发布版本发布时间'),
    ellipsis: true,
    minWidth: 182,
    sortable: true,
    formatter: fillTimeText,
  },
  {
    type: 'text',
    prop: 'updateUser',
    label: $t('_.已发布版本发布者'),
    ellipsis: true,
    minWidth: 160,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'templateName',
    label: $t('_.默认参数模板'),
    ellipsis: true,
    minWidth: language === 'zh-CN' ? 116 : 156,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'globalVar',
    label: $t('_.全局变量'),
    ellipsis: true,
    minWidth: 116,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'metaResource',
    label: $t('_.资源文件'),
    ellipsis: true,
    minWidth: 116,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'proxyUser',
    label: $t('_.代理用户'),
    ellipsis: true,
    minWidth: 116,
    formatter: fillText,
  },
  {
    type: 'text',
    prop: 'errorMsg',
    label: $t('_.错误信息'),
    ellipsis: true,
    minWidth: 200,
    formatter: fillText,
  },
  {
    type: 'action',
    label: $t('_.操作'),
    minWidth: 60,
  },
]);
</script>
<style lang="less" scoped>
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
</style>
