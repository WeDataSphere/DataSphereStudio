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
              v-model="searchForm.projectNameList"
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
            <span class="condition-label">{{ $t('_.节点名') }}</span>
            <FSelect
              v-model="searchForm.nodeNameList"
              :options="nodeList"
              :placeholder="$t('_.请选择')"
              filterable
              clearable
              multiple
              collapse-tags
              :collapse-tags-limit="1"
            />
          </div>
          <template v-if="state.key === 'datachecker'">
            <div class="tag-select">
              <span class="condition-label">source.type</span>
              <FSelect
                v-model="searchForm.sourceType"
                :options="sourceTypeList"
                :placeholder="$t('_.请选择')"
                filterable
                clearable
              />
            </div>
            <div>
              <span class="condition-label">check.object</span>
              <FInput
                v-model="searchForm.checkObject"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">job.desc</span>
              <FInput
                v-model="searchForm.jobDesc"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div class="tag-select">
              <span class="condition-label">{{
                $t('_.使用qualities校验')
              }}</span>
              <FSelect
                v-model="searchForm.qualitisCheck"
                :placeholder="$t('_.请选择')"
                filterable
                clearable
              >
                <FOption value="1">
                  {{ $t('_.是') }}
                </FOption>
                <FOption value="0">
                  {{ $t('_.否') }}
                </FOption>
              </FSelect>
            </div>
          </template>
          <template v-else-if="state.key === 'eventsender'">
            <div>
              <span class="condition-label">msg.sender</span>
              <FInput
                v-model="searchForm.msgSender"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.body</span>
              <FInput
                v-model="searchForm.msgBody"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.topic</span>
              <FInput
                v-model="searchForm.msgTopic"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.name</span>
              <FInput
                v-model="searchForm.msgName"
                :placeholder="$t('_.请输入')"
              />
            </div>
          </template>
          <template v-else-if="state.key === 'eventreceiver'">
            <div>
              <span class="condition-label">msg.receiver</span>
              <FInput
                v-model="searchForm.msgReceiver"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.topic</span>
              <FInput
                v-model="searchForm.msgTopic"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.name</span>
              <FInput
                v-model="searchForm.msgName"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">max.receive.hours</span>
              <FInput
                v-model="searchForm.maxReceiveHours"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div>
              <span class="condition-label">msg.saveKey</span>
              <FInput
                v-model="searchForm.msgSaveKey"
                :placeholder="$t('_.请输入')"
              />
            </div>
            <div class="tag-select">
              <span class="condition-label">only.receive.today</span>
              <FSelect
                v-model="searchForm.onlyReceiveToday"
                :placeholder="$t('_.请选择')"
                filterable
                clearable
              >
                <FOption value="1">
                  {{ $t('_.是') }}
                </FOption>
                <FOption value="0">
                  {{ $t('_.否') }}
                </FOption>
              </FSelect>
            </div>
            <div class="tag-select">
              <span class="condition-label">msg.receive.use.rundate</span>
              <FSelect
                v-model="searchForm.msgReceiveUseRunDate"
                :placeholder="$t('_.请选择')"
                filterable
                clearable
              >
                <FOption value="1">
                  {{ $t('_.是') }}
                </FOption>
                <FOption value="0">
                  {{ $t('_.否') }}
                </FOption>
              </FSelect>
            </div>
          </template>
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
          {{ $t('_.确认编辑') }}
        </FButton>
        <FButton
          :disabled="operStatus === 'pendding'"
          @click="selectionReset('all')"
        >
          {{ $t('_.取消') }}
        </FButton>
      </template>
      <template v-else>
        <FButton type="primary" @click="handleBtnClick('batch')">
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
            v-else-if="item.type === 'action'"
            v-slot="{ row }"
            :key="item.prop"
            :label="item.label"
            :width="item.minWidth"
            align="center"
            fixed="right"
          >
            <span
              :style="setEditStyle(row.editable)"
              @click="handleBtnClick('item', row)"
              >{{ $t('_.编辑') }}</span
            >
            <span class="operate-link" @click="jumpToworkflow(row)">{{
              $t('_.查看工作流')
            }}</span>
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

  <BatchEditSignal
    v-model:show="currentShow"
    :config="currentConfig"
    :form="currentForm"
    @success="handleFormSearch"
    @cancel="resetOperStatus"
  />
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { onMounted, ref, nextTick, inject, computed, watch } from 'vue';
import {
  usePagination,
  type PaginationAndParams,
} from '../hooks/usePagination';
import { FMessage } from '@fesjs/fes-design';
import { useBatchOperations } from '../hooks/useBatchOperations';
import { useDataList } from './hooks/useDataList';
import { useTableColumns } from './hooks/useTableColumns';
import type { BaseType, SignalType, StatusStyleType } from './types/index';
import api from './api';
import BatchEditSignal from './components/batchEditSignal.vue';
import { getUrlParams } from '@fesjs/traction-widget';

const { t: $t } = useI18n();

const {
  projectList,
  flowList,
  nodeList,
  sourceTypeList,
  fetchProjectList,
  fetchFlowAndNodeList,
  fetchSourceTypeList,
} = useDataList();
const query = ref({
  workspaceId: '',
  label: '',
});
const init = (key: string) => {
  switch (key) {
    case 'datachecker':
      return {
        orchestratorName: '',
        projectNameList: [],
        nodeNameList: [],
        sourceType: '',
        checkObject: '',
        jobDesc: '',
        qualitisCheck: '',
      };
    case 'eventsender':
      return {
        orchestratorName: '',
        projectNameList: [],
        nodeNameList: [],
        msgSender: '',
        msgBody: '',
        msgTopic: '',
        msgName: '',
      };
    case 'eventreceiver':
      return {
        orchestratorName: '',
        projectNameList: [],
        nodeNameList: [],
        msgReceiver: '',
        msgTopic: '',
        msgName: '',
        maxReceiveHours: '',
        msgSaveKey: '',
        onlyReceiveToday: '',
        msgReceiveUseRunDate: '',
      };
  }
};
const state = inject<BaseType>('pageState') || {};
const searchForm = ref(init(state.value.key) as BaseType);
const tableList = ref<BaseType[]>([]);
const actionType = ref<string>('loading');

const apiMap = {
  datachecker: api.GetDataCheckerListApi,
  eventsender: api.GetEventSenderListApi,
  eventreceiver: api.GetEventReciverListApi,
};

const loadTable = async (param: PaginationAndParams) => {
  actionType.value = 'loading';
  ['qualitisCheck', 'msgReceiveUseRunDate', 'onlyReceiveToday'].forEach(
    (key) => {
      if (param[key]) {
        param[key] = Number(param[key]);
      }
    }
  );
  const res: BaseType = await apiMap[state.value.key as SignalType](param);
  tableList.value = (res.data?.data || []).map((item: BaseType) => {
    ['qualitisCheck', 'onlyReceiveToday', 'msgReceiveUseRunDate'].forEach(
      (key: string) => {
        item[`${key}Text`] = ['null', 'undefined', ''].includes(
          String(item[key])
        )
          ? ''
          : item[key]
          ? $t('_.是')
          : $t('_.否');
      }
    );
    return item;
  });
  if (res.data.total === 0) {
    actionType.value = 'emptyQueryResult';
  }
  return {
    totalPage: Math.ceil(res.data.total / (param.pageSize as number)),
    totalCount: res.data.total,
    pageNow: param.pageNow,
  };
};

const { isLoading, pagination, handleInit, handleCurrentChange, fillText } =
  usePagination(searchForm, loadTable);

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
};
const handleFormReset = async () => {
  await nextTick();
  searchForm.value = init(state.value.key) as BaseType;
  await handleFormSearch();
};
const handlePageChange = async (currentPage: number, pageSize: number) => {
  selectionReset('row');
  await handleCurrentChange(currentPage, pageSize);
};

const setEditStyle = (isActive: boolean): StatusStyleType => {
  const editStyle: StatusStyleType = {
    color: isActive ? '#5384ff' : '#b7b7bc',
    'margin-right': '6px',
  };
  if (isActive) {
    editStyle['cursor'] = 'pointer';
  } else {
    editStyle['pointer-events'] = 'none';
  }
  return editStyle;
};

watch(
  () => state.value.key,
  (curKey: string) => {
    searchForm.value = init(curKey) as BaseType;
    fetchFlowAndNodeList({ groupNameEn: 'Signal node', nodeTypeName: curKey });
    handleFormSearch();
  },
  { immediate: true }
);

onMounted(() => {
  query.value = getUrlParams() || {};
  fetchProjectList();
  fetchSourceTypeList();
});

// 编辑和批量编辑
const currentShow = ref<boolean>(false);
const currentForm = ref<BaseType>({});
const currentConfig = ref<BaseType>({ type: 'edit' });

const handleBtnClick = (mode: string, row: BaseType | null = null): void => {
  if (mode === 'batch') {
    operStatus.value = 'prepare';
    currentShow.value = false;
  } else {
    currentShow.value = true;
    currentForm.value = { row };
    currentConfig.value = {
      mode: 'item',
      uiList: state.value.uiMap[state.value.key],
    };
    console.log('currentConfig', currentConfig);
  }
};

const handleBatch = async () => {
  try {
    const rows = await selectionValidate((rows) => {
      const types = rows.map((node) => node.nodeTypeName);
      if ([...new Set(types)].length > 1) {
        FMessage.warn($t('_.批量编辑时节点类型必须相同'));
        return false;
      } else if (rows.some((node) => !node.editable)) {
        FMessage.warn($t('_.批量编辑存在无编辑权限的节点，请重新选择'));
        return false;
      }
      return true;
    });
    currentShow.value = true;
    currentForm.value = { rows };
    currentConfig.value = {
      mode: 'batch',
      uiList: state.value.uiMap[state.value.key],
    };
  } catch (error) {
    console.log('bacth select error', error);
  }
};
const jumpToworkflow = (row: any) => {
  const localUrl = window.location.href;
  const baseUrl = localUrl.split('next-web')[0] + '#/workflow?';
  const workspaceId = query.value.workspaceId;
  const params = `workspaceId=${workspaceId}&projectID=${row.projectId}&projectName=${row.projectName}&flowId=${row.orchestratorId}&appId=${row.flowId}&nodeName=${row.nodeName}`;
  const url = baseUrl + params;
  window.open(url, '_blank');
};
const { columnMap } = useTableColumns(fillText);
const tableColumns = computed(() => columnMap.value[state.value.key]);
</script>
<style lang="less" scoped>
.operate-link {
  cursor: pointer;
  color: #5384ff;
  margin-right: 6px;
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
</style>
