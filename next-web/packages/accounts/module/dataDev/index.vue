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
          <div class="tag-select-small">
            <span class="condition-label">{{ $t('_.引用参数模板') }}</span>
            <FSelect
              v-model="searchForm.refTemplate"
              :options="booleanList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              @change="handleTemplateDataChange"
            />
          </div>
          <div
            v-if="!(searchForm.refTemplate === false)"
            class="tag-select-long"
          >
            <span class="condition-label">{{ $t('_.参数模板名称') }}</span>
            <FSelect
              v-model="searchForm.templateNameList"
              :options="templateNameList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select-small">
            <span class="condition-label">{{ $t('_.复用引擎') }}</span>
            <FSelect
              v-model="searchForm.reuseEngine"
              :options="booleanList"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
            />
          </div>
        </template>
        <template #exButton>
          <FButton
            v-if="querySelectedCount"
            type="info"
            @click="toggleAdvanceQuery"
          >
            {{ $t('common.advanceSearch')
            }}{{
              querySelectedCount > 0
                ? `（${$t('common.selected_blank')}${querySelectedCount}${t(
                    'common.blank_items'
                  )}）`
                : ''
            }}
          </FButton>
          <FButton v-else @click="toggleAdvanceQuery">
            {{ $t('common.advanceSearch') }}
          </FButton>
          <FButton class="reset" @click="handleReset">
            {{ $t('common.reset') }}
          </FButton>
        </template>
      </BSearch>
    </template>
    <template #operate>
      <template v-if="['prepare', 'pendding'].includes(operStatus)">
        <FButton
          type="primary"
          :disabled="operStatus === 'pendding'"
          @click="handleBatchConfirm"
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
          @click="handleBatchBtnClick('edit', 'batchEdit')"
        >
          {{ $t('_.批量编辑') }}
        </FButton>
      </template>
    </template>
    <template #table>
      <FTable
        ref="tableRef"
        :data="tableList"
        @selectionChange="selectionChange"
      >
        <template #empty>
          <BPageLoading :action-type="actionType" />
        </template>
        <f-table-column
          :key="'selection'"
          type="selection"
          :visible="['prepare', 'pendding'].includes(operStatus)"
        />
        <f-table-column
          prop="projectName"
          :label="$t('common.projectName')"
          :width="144"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="orchestratorName"
          :label="$t('common.workflowName')"
          :width="144"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="nodeName"
          :label="$t('common.nodeName')"
          :width="216"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="nodeTypeName"
          :label="$t('common.nodeSmallType')"
          :width="96"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="refTemplate"
          :label="$t('_.是否引用参数模板')"
          :width="language === 'zh-CN' ? 146 : 228"
          ellipsis
          :formatter="fillBoolText"
        />
        <f-table-column
          prop="templateName"
          :label="$t('_.参数模板名称')"
          :width="124"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="sparkExecutorMemory"
          label="spark.executor.memory"
          :width="168"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="sparkDriverMemory"
          label="spark.driver.memory"
          :width="156"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="sparkConf"
          label="spark.conf"
          :width="86"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="sparkExecutorCore"
          label="spark.executor.core"
          :width="140"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="sparkExecutorInstances"
          label="spark.executor.instances"
          :width="172"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          prop="reuseEngine"
          :label="$t('_.是否复用引擎')"
          :width="118"
          ellipsis
          :formatter="fillBoolText"
        />
        <f-table-column
          v-slot="{ row }"
          :label="$t('_.资源文件')"
          :width="156"
          ellipsis
          :formatter="fillText"
        >
          <span>{{ handleShowResource(row) }}</span>
        </f-table-column>
        <f-table-column
          prop="nodeId"
          :label="$t('_.节点ID')"
          :width="181"
          ellipsis
          :formatter="fillText"
        />
        <f-table-column
          v-slot="{ row }"
          :label="$t('common.action')"
          align="center"
          fixed="right"
          :width="language === 'zh-CN' ? 204 : 308"
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
          <span
            v-if="row.associateGit"
            class="operate-link"
            @click="jumpToGit(row)"
            >{{ $t('_.git地址') }}</span
          >
          <span class="operate-link" @click="jumpToworkflow(row)">{{
            $t('_.查看工作流')
          }}</span>
          <span class="operate-link" @click="showInfo(row)">{{
            $t('_.参数信息')
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
  <!-- 高级筛选 -->
  <AdvanceSearch
    v-model:show="showAdvanceQuery"
    v-model:advance-search-form-data="advanceSearchForm"
    @search="handleAdvanceSearch"
    @cancel="advanceCancel"
  />
  <!-- 编辑弹窗 -->
  <EditDataVisualDrawer
    v-if="currentEditShow"
    v-model:show="currentEditShow"
    :config="currentConfig"
    :form="currentForm"
    node-parent-type="数据开发"
    @success="fetchTableDataMain"
    @initBatch="handleInitBatch"
  />
  <!-- 信息查看弹窗 -->
  <InfoDataDev
    v-if="currentInfoShow"
    v-model:show="currentInfoShow"
    :form="currentForm"
  />
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { onMounted, ref, reactive, type Component, computed } from 'vue';
import { FTable, FMessage } from '@fesjs/fes-design';
import { getUrlParams } from '@fesjs/traction-widget';
import { useDataList } from './hooks/useDataList';
import { useDataList as useDataVisualDataList } from '@/packages/accounts/module/dataVisualization/hooks/useDataList.ts';
import { useBatchOperations } from '../hooks/useBatchOperations';
import type {
  DataType,
  SearchFormType,
  advanceSearchFormType,
} from './types/index';
import api from './api';
import InfoDataDev from './components/infoDataDev.vue';
import EditDataVisualDrawer from '../dataVisualization/components/editDataVisualDrawer.vue';
import { cloneDeep } from 'lodash-es';
import AdvanceSearch from './components/advanceSearch.vue';

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
  templateNameList,
  orchestratorNameList,
  fetchMutipleTypeList,
  fetchProjectList,
  fetchNodeTypeList,
  fillText,
  fillBoolText,
} = useDataVisualDataList();
const { booleanList } = useDataList();

// 高级筛选
const initAdvanceSearchForm = (): advanceSearchFormType => ({
  sparkExecutorMemory: '',
  sparkDriverMemory: '',
  sparkConf: '',
  sparkExecutorCore: '',
  sparkExecutorInstances: '',
});
const advanceSearchForm = ref(initAdvanceSearchForm());
const advanceQueryDataBak = ref(initAdvanceSearchForm());
const showAdvanceQuery = ref(false);
const handleAdvanceSearch = (advanceData) => {
  pagination.current = 1;
  advanceSearchForm.value = cloneDeep(advanceData);
  advanceQueryDataBak.value = cloneDeep(advanceData);
  fetchAllTableData();
};
const toggleAdvanceQuery = () => {
  showAdvanceQuery.value = true;
};
const advanceCancel = () => {
  advanceSearchForm.value = cloneDeep(advanceQueryDataBak.value);
  showAdvanceQuery.value = false;
};
const querySelectedCount = computed(() => {
  const count = Object.values(advanceSearchForm.value).filter((item) => {
    if (Array.isArray(item) && item.length === 0) return false;
    return !!item;
  }).length;
  return count;
});
// 批量操作
const {
  tableRef,
  operStatus,
  selectedRows,
  selectionReset,
  resetOperStatus,
  selectionChange,
  selectionValidate,
} = useBatchOperations();
const query = ref({
  workspaceId: '',
});
const initDataDevSearchForm = (): SearchFormType => ({
  projectNameList: [],
  orchestratorName: '',
  nodeTypeNameList: [],
  nodeNameList: [],
  refTemplate: null,
  templateNameList: [],
  reuseEngine: null,
  // sortBy: 'createTime',
  // orderBy: 'descend',
});
const searchForm = ref(initDataDevSearchForm());
const tableList = ref<DataType[]>([]);
const isLoading = ref(false);
const actionType = ref<string>('loading');

// 获取表格数据
const fetchAllTableData = async () => {
  const params = {
    pageNow: pagination.current,
    pageSize: pagination.size,
    ...searchForm.value,
    ...advanceSearchForm.value,
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
  searchForm.value = initDataDevSearchForm();
  advanceSearchForm.value = initAdvanceSearchForm();
  advanceQueryDataBak.value = initAdvanceSearchForm();
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

const currentEditShow = ref<boolean | null>(null);
const currentInfoShow = ref<boolean | null>(null);
const currentForm = ref<DataType | null>(null);
const currentConfig = ref<DataType>({ type: 'edit' });
const currentBatch = ref<DataType | null>(null);

// 编辑弹窗
const handleEdit = (type: string, row: DataType) => {
  currentForm.value = { ...row };
  currentEditShow.value = true;
  currentConfig.value = {
    type: type,
    nodeTypeName: currentForm.value.nodeTypeName,
    workspaceId: query.value.workspaceId,
  };
};

// 批量编辑
const btnMap: DataType = {
  edit: { text: $t('_.确认编辑'), name: 'batchEdit' },
};

const handleBatchBtnClick = (type: string, name: string): void => {
  currentConfig.value = {
    workspaceId: query.value.workspaceId,
  };
  if (['edit'].includes(type)) {
    operStatus.value = 'prepare';
    currentBatch.value = btnMap[type];
  }
};

const handleBatchConfirm = async () => {
  try {
    const compnentName = currentBatch.value?.name;
    const rows = await selectionValidate((rows) => {
      if (compnentName === 'batchEdit') {
        const types = rows.map((node) => node.nodeTypeName);
        if ([...new Set(types)].length > 1) {
          FMessage.warn($t('_.批量编辑时节点类型必须相同'));
          operStatus.value = 'prepare';
          return false;
        } else if (rows.some((node) => !node.editable)) {
          FMessage.warn($t('_.批量编辑存在无编辑权限的节点，请重新选择'));
          operStatus.value = 'prepare';
          return false;
        }
      }
      currentEditShow.value = true;
      currentConfig.value = {
        type: 'batchEdit',
        nodeTypeName: selectedRows.value[0].nodeTypeName,
        workspaceId: query.value.workspaceId,
        selectedData: selectedRows.value,
      };
      return true;
    });
    currentForm.value = { rows };
  } catch (error) {
    console.log('bacth select error', error);
  }
};

const handleInitBatch = () => {
  operStatus.value = '';
  selectionReset('all');
};

// 查看信息
const showInfo = (row: DataType) => {
  currentForm.value = { ...row };
  currentInfoShow.value = true;
};

const jumpToGit = async (row: DataType) => {
  const params = {
    workflowId: row.flowId,
    projectName: row.projectName,
    workflowName: row.orchestratorName,
    workflowNodeName: row.nodeName,
  };
  try {
    const res = await api.jumpToGitApi(params);
    if (res && res.data && res.data.gitUrl) {
      window.open(res.data.gitUrl, '_blank');
    }
  } catch (err) {
    console.error(err);
  }
};

function getParameterFromUrl(url, parameterName) {
  try {
    const urlParams = new URLSearchParams(new URL(url).search);
    return urlParams.get(parameterName);
  } catch (error) {
    console.error('无法解析 URL 或获取参数:', error);
    return null;
  }
}

const jumpToworkflow = (row: any) => {
  const localUrl = window.location.href;
  const baseUrl = localUrl.split('next-web')[0] + '#/workflow?';
  console.log('jumpToworkflow-baseUrl', baseUrl);
  const workspaceId = query.value.workspaceId;
  const params = `workspaceId=${workspaceId}&projectID=${row.projectId}&projectName=${row.projectName}&flowId=${row.orchestratorId}&appId=${row.flowId}&nodeName=${row.nodeName}`;
  const url = baseUrl + params;
  window.open(url, '_blank');
};

// 模板查询项联动
const handleTemplateDataChange = (val: any[]) => {
  if (searchForm.value.refTemplate) {
    searchForm.value.templateNameList = [];
  }
};

// 资源文件展示
const handleShowResource = (row: any) => {
  if (row.resource) {
    const resArray = row.resource
      .split(';')
      .filter((item) => item !== row.script);
    return resArray.join(';');
  }
  return '--';
};

onMounted(async () => {
  query.value = getUrlParams() || {};
  fetchMutipleTypeList({ groupNameEn: 'Data development' });
  fetchProjectList();
  fetchNodeTypeList({ groupNameEn: 'Data development' });
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
.tag-select-small {
  :deep(.fes-select) {
    width: 160px;
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
