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
              v-model="searchForm.projectNames"
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
            <span class="condition-label">{{
              $t('common.projectCreator')
            }}</span>
            <FSelect
              v-model="searchForm.createUsers"
              :options="createUsers"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{
              $t('common.releaseUserList')
            }}</span>
            <FSelect
              v-model="searchForm.releaseUsers"
              :options="releaseUsers"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{ $t('common.editUserList') }}</span>
            <FSelect
              v-model="searchForm.editUsers"
              :options="editUsers"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <div class="tag-select">
            <span class="condition-label">{{
              $t('common.accessUserList')
            }}</span>
            <FSelect
              v-model="searchForm.accessUsers"
              :options="accessUsers"
              :placeholder="$t('common.pleaseSelect')"
              filterable
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
          <!-- [新增] 更新时间范围-开始 -->
          <div class="tag-select">
            <span class="condition-label">{{
              $t('common.updateTimeRange')
            }}</span>
            <FDatePicker
              v-model="searchForm.updateStartTime"
              type="date"
              :placeholder="$t('common.updateTimeRange')"
              :max-date="updateMaxDate"
            />
          </div>
          <!-- [新增] 更新时间范围-结束 -->
          <div class="tag-select">
            <span class="condition-label">~</span>
            <FDatePicker
              v-model="searchForm.updateEndTime"
              type="date"
              :placeholder="$t('common.updateTimeRange')"
              :min-date="updateMinDate"
            />
          </div>
          <!-- [新增] 健康状态多选 -->
          <div class="tag-select-long">
            <span class="condition-label">{{ $t('common.healthStatus') }}</span>
            <FSelect
              v-model="searchForm.healthStatus"
              :options="healthStatusOptions"
              :placeholder="$t('common.pleaseSelect')"
              clearable
              collapse-tags
              :collapse-tags-limit="1"
              multiple
            />
          </div>
        </template>
        <template #exButton>
          <FButton class="reset" @click="handleReset">
            {{ $t('common.reset') }}
          </FButton>
          <!-- [新增] 导出按钮 -->
          <FButton
            type="primary"
            :loading="exportLoading"
            @click="handleExport"
          >
            {{ exportLoading ? $t('common.exporting') : $t('common.export') }}
          </FButton>
        </template>
      </BSearch>
    </template>
    <template #operate>
      {{ workspaceInfo.workspaceName }}{{ $t('common.workspaceAdmin') }}:{{
        workspaceInfo.roles
      }}
    </template>
    <template #table>
      <FTable ref="tableRef" :data="tableList" @sort-change="tableSort">
        <template #empty>
          <BPageLoading :action-type="actionType" />
        </template>
        <f-table-column
          prop="name"
          :label="$t('common.projectName')"
          :width="174"
          fixed="left"
          ellipsis
        >
          <template #default="{ row }">
            <span
              class="project-name-link"
              :title="row.name"
              @click="handleOpenDetail(row)"
              >{{ row.name }}</span
            >
          </template>
        </f-table-column>
        <f-table-column
          prop="description"
          :label="$t('common.projectDesc')"
          :width="264"
          ellipsis
        >
          <template #default="{ row }">
            <FEllipsis v-if="row.description">
              {{ row.description }}
              <template #tooltip>
                <div style="max-width: 500px; word-wrap: break-word">
                  {{ row.description }}
                </div>
              </template>
            </FEllipsis>
            <div v-else>--</div>
          </template>
        </f-table-column>
        <f-table-column
          prop="createBy"
          :label="$t('common.projectCreator')"
          :width="language === 'zh-CN' ? 122 : 130"
          ellipsis
        />
        <f-table-column
          prop="workspaceName"
          :label="$t('common.inWorkspace')"
          :width="164"
          ellipsis
        />
        <f-table-column
          prop="releaseUsers"
          :label="$t('common.releaseUserList')"
          :width="194"
          ellipsis
        >
          <template #default="{ row }">
            <FEllipsis v-if="row.releaseUsers && row.releaseUsers.length > 0">
              {{ row.releaseUsers.join(',') }}
              <template #tooltip>
                <div style="max-width: 500px; word-wrap: break-word">
                  <p v-for="(obj, key) in row?.releaseUsers" :key="key">
                    {{ obj }}
                  </p>
                </div>
              </template>
            </FEllipsis>
            <div v-else>--</div>
          </template>
        </f-table-column>
        <f-table-column
          prop="editUsers"
          :label="$t('common.editUserList')"
          :width="194"
          ellipsis
        >
          <template #default="{ row }">
            <FEllipsis v-if="row.editUsers && row.editUsers.length > 0">
              {{ row.editUsers.join(',') }}
              <template #tooltip>
                <div style="max-width: 500px; word-wrap: break-word">
                  <p v-for="(obj, key) in row?.editUsers" :key="key">
                    {{ obj }}
                  </p>
                </div>
              </template>
            </FEllipsis>
            <div v-else>--</div>
          </template>
        </f-table-column>
        <f-table-column
          prop="accessUsers"
          :label="$t('common.accessUserList')"
          :width="194"
          ellipsis
        >
          <template #default="{ row }">
            <FEllipsis v-if="row.accessUsers && row.accessUsers.length > 0">
              {{ row.accessUsers.join(',') }}
              <template #tooltip>
                <div style="max-width: 500px; word-wrap: break-word">
                  <p v-for="(obj, key) in row?.accessUsers" :key="key">
                    {{ obj }}
                  </p>
                </div>
              </template>
            </FEllipsis>
            <div v-else>--</div>
          </template>
        </f-table-column>
        <f-table-column
          prop="createTime"
          :label="$t('common.projectCreateTime')"
          :width="language === 'zh-CN' ? 160 : 182"
          ellipsis
          sortable
        />
        <f-table-column
          prop="updateTime"
          :label="$t('common.projectUpdateTime')"
          :width="language === 'zh-CN' ? 160 : 182"
          ellipsis
          sortable
        />
        <!-- ===== [新增] 资产统计列（位置：更新时间后、操作前） ===== -->
        <f-table-column
          prop="workflowCount"
          :label="$t('common.workflowCount')"
          :width="100"
          align="center"
        >
          <template #default="{ row }">
            {{ formatStatCell(row, 'workflowCount') }}
          </template>
        </f-table-column>
        <f-table-column
          prop="nodeCount"
          :label="$t('common.nodeCount')"
          :width="90"
          align="center"
        >
          <template #default="{ row }">
            {{ formatStatCell(row, 'nodeCount') }}
          </template>
        </f-table-column>
        <f-table-column
          prop="dataSourceCount"
          :label="$t('common.dataSourceCount')"
          :width="100"
          align="center"
        >
          <template #default="{ row }">
            {{ formatStatCell(row, 'dataSourceCount') }}
          </template>
        </f-table-column>
        <f-table-column
          prop="memberCount"
          :label="$t('common.memberCount')"
          :width="90"
          align="center"
        >
          <template #default="{ row }">
            {{ formatStatCell(row, 'memberCount') }}
          </template>
        </f-table-column>
        <f-table-column
          prop="latestWorkflowUpdateTime"
          :label="$t('common.latestWorkflowUpdateTime')"
          :width="180"
          ellipsis
        >
          <template #default="{ row }">
            {{ formatTimeCell(row) }}
          </template>
        </f-table-column>
        <f-table-column
          prop="healthLabels"
          :label="$t('common.healthStatus')"
          :width="200"
        >
          <template #default="{ row }">
            <HealthBadge :labels="row.healthLabels" />
          </template>
        </f-table-column>
        <f-table-column
          v-slot="{ row }"
          :label="$t('_.操作')"
          align="center"
          fixed="right"
          :width="language === 'zh-CN' ? 60 : 96"
          ellipsis
        >
          <span
            v-if="row.editable"
            :style="{ cursor: 'pointer', color: '#5384ff' }"
            @click="handleEdit('edit', row)"
            >{{ $t('common.edit') }}</span
          >
          <span v-else> -- </span>
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
  <EditProject
    v-if="currentShow"
    v-model:show="currentShow"
    :config="currentConfig"
    :form="currentForm"
    @success="fetchTableDataMain"
  />
  <!-- [新增] 项目详情抽屉 -->
  <ProjectDetailDrawer
    v-model:show="detailVisible"
    :project-id="detailProjectId"
    :workspace-id="query.workspaceId"
  />
</template>
<script lang="ts" setup>
import { onMounted, ref, computed, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { FTable, FMessage } from '@fesjs/fes-design';
import { getUrlParams } from '@fesjs/traction-widget';
import { useDataList } from './hooks/useDataList';
import type {
  DataType,
  ProjectRowType,
  SearchFormType,
  WorkspaceInfoType,
} from './types/index';
import api from './api';
import EditProject from './components/editProject.vue';
import HealthBadge from './components/HealthBadge.vue';
import ProjectDetailDrawer from './components/ProjectDetailDrawer.vue';

const { t } = useI18n();
const language = localStorage.getItem('locale');
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});
const {
  projectList,
  fetchProjectList,
  accessUsers,
  createUsers,
  editUsers,
  releaseUsers,
  fetchUserList,
} = useDataList();

const query = ref({
  workspaceId: '',
});
const init = (): SearchFormType => ({
  workspaceId: query.value.workspaceId,
  projectNames: [],
  createUsers: [],
  releaseUsers: [],
  editUsers: [],
  accessUsers: [],
  updateStartTime: '',
  updateEndTime: '',
  healthStatus: [],
  sortBy: 'createTime',
  orderBy: 'descend',
});
const searchForm = ref(init());
const tableList = ref<DataType[]>([]);
const workspaceInfo = ref<WorkspaceInfoType>({
  workspaceName: '',
  roles: [],
});
const isLoading = ref(false);
const actionType = ref<string>('loading');
const tableRef = ref<typeof FTable | null>(null);

/**
 * [新增] 健康状态多选可选项
 */
const healthStatusOptions = computed(() => [
  { label: t('common.healthEmptyProject'), value: 'EMPTY_PROJECT' },
  { label: t('common.healthStale'), value: 'STALE' },
  { label: t('common.healthNoDescription'), value: 'NO_DESCRIPTION' },
]);

/**
 * [新增] 日期联动约束：开始≤结束
 */
const updateMinDate = computed(() => searchForm.value.updateStartTime || undefined);
const updateMaxDate = computed(() => searchForm.value.updateEndTime || undefined);

/**
 * [新增] 将 FDatePicker 的绑定值（Date/string/number）格式化为 yyyy-MM-dd
 */
const toDateString = (val: unknown): string => {
  if (!val) return '';
  if (typeof val === 'string') {
    return val.length >= 10 ? val.slice(0, 10) : val;
  }
  if (val instanceof Date) {
    const y = val.getFullYear();
    const m = String(val.getMonth() + 1).padStart(2, '0');
    const d = String(val.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
  if (typeof val === 'number') return toDateString(new Date(val));
  return '';
};

/**
 * [新增] 统计列单元格显示
 * - statsDegraded=true → 无法评估
 * - value null/undefined → --
 * - 否则原值
 */
const formatStatCell = (row: ProjectRowType, key: string): string => {
  if (row.statsDegraded) return t('common.unavailable');
  const v = (row as any)[key];
  if (v === null || v === undefined) return '--';
  return String(v);
};

/**
 * [新增] 最近工作流更新时间单元格显示
 */
const formatTimeCell = (row: ProjectRowType): string => {
  if (row.statsDegraded) return t('common.unavailable');
  if (!row.latestWorkflowUpdateTime) return '--';
  return row.latestWorkflowUpdateTime;
};

// 获取表格数据
const fetchTableData = async () => {
  const params = {
    pageNow: pagination.current,
    pageSize: pagination.size,
    ...searchForm.value,
    updateStartTime: toDateString(searchForm.value.updateStartTime),
    updateEndTime: toDateString(searchForm.value.updateEndTime),
  };
  actionType.value = 'loading';
  tableList.value = [];
  try {
    if (!isLoading.value) {
      isLoading.value = true;
      const res: DataType = await api.ProjectTableApi(params);
      tableList.value = res.data?.projects || [];
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
  await fetchTableData();
  if (
    tableList.value.length > 0 &&
    tableRef.value &&
    searchForm.value.sortBy &&
    searchForm.value.orderBy
  ) {
    tableRef.value?.sort(searchForm.value.sortBy, searchForm.value.orderBy);
  }
};

// 重置操作
const handleReset = () => {
  pagination.current = 1;
  searchForm.value.projectNames = [];
  searchForm.value.createUsers = [];
  searchForm.value.releaseUsers = [];
  searchForm.value.editUsers = [];
  searchForm.value.accessUsers = [];
  searchForm.value.updateStartTime = '';
  searchForm.value.updateEndTime = '';
  searchForm.value.healthStatus = [];
  fetchTableDataMain();
};

// 判断排序参数是否和上次相同
const compareSortParams = (prop: string, order: string) => {
  if (prop !== searchForm.value.sortBy || order !== searchForm.value.orderBy) {
    return false;
  }
  return true;
};

// 表格排序
const tableSort = async (sortParam: { prop: string; order: string }) => {
  const tempOrder = sortParam.order ? sortParam.order : 'descend';
  if (!compareSortParams(sortParam.prop, tempOrder)) {
    searchForm.value.sortBy = sortParam.prop;
    searchForm.value.orderBy = tempOrder;
    await fetchTableDataMain();
  }
};

const currentShow = ref<boolean | null>(null);
const currentForm = ref<DataType | null>(null);
const currentConfig = ref<DataType>({ type: 'edit' });

const handleEdit = (type: string, row: DataType) => {
  currentForm.value = { ...row };
  currentShow.value = true;
  currentConfig.value = { type, workspaceId: query.value.workspaceId };
};

/**
 * [新增] 项目详情抽屉
 */
const detailVisible = ref(false);
const detailProjectId = ref<number | null>(null);
const handleOpenDetail = (row: DataType) => {
  detailProjectId.value = row.id ?? null;
  detailVisible.value = true;
};

/**
 * [新增] CSV 导出（blob 下载）
 */
const exportLoading = ref(false);
const parseExportFileName = (disposition: string): string => {
  // content-disposition: attachment; filename="project_ledger_xxx.csv"
  const match = /filename="?([^";]+)"?/i.exec(disposition);
  return match ? match[1] : `project_ledger_${Date.now()}.csv`;
};
const handleExport = async () => {
  if (exportLoading.value) return;
  exportLoading.value = true;
  try {
    const params = {
      ...searchForm.value,
      updateStartTime: toDateString(searchForm.value.updateStartTime),
      updateEndTime: toDateString(searchForm.value.updateEndTime),
    };
    const res = await api.exportProjectsApi(params);
    const blob: Blob = res.data;
    const contentType = res.headers['content-type'] || blob.type || '';
    // 后端超限时返回 application/json 错误体，需解析提示
    if (contentType.includes('application/json')) {
      const text = await blob.text();
      let msg = t('common.fetchFailed');
      try {
        msg = JSON.parse(text)?.message || msg;
      } catch (e) {
        // ignore parse error
      }
      FMessage.error(msg);
      return;
    }
    // 正常 CSV 文件流，触发下载
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = parseExportFileName(
      res.headers['content-disposition'] || ''
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    FMessage.success(t('common.exportSuccess'));
  } catch (err) {
    console.error('export failed', err);
    FMessage.error(t('common.fetchFailed'));
  } finally {
    exportLoading.value = false;
  }
};

onMounted(async () => {
  query.value = getUrlParams() || {};
  fetchProjectList(query.value);
  fetchUserList(query.value);
  searchForm.value.workspaceId = query.value.workspaceId;
  const workspaceRes = await api.getUserByRoleApi<DataType>({
    workspaceId: query.value.workspaceId,
  });
  workspaceInfo.value.roles = workspaceRes.data.users.join(',') || '';
  workspaceInfo.value.workspaceName = workspaceRes.data.workspaceName || '';
  fetchTableDataMain();
});
</script>
<style lang="less" scoped>
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
  :deep(.fes-date-picker) {
    width: 215px;
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
.project-name-link {
  color: #5384ff;
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
}
</style>
