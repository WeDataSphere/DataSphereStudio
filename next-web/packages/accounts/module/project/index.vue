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
        </template>
        <template #exButton>
          <FButton class="reset" @click="handleReset">
            {{ $t('common.reset') }}
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
          ellipsis
        />
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
            <FEllipsis v-if="row.releaseUsers.length > 0">
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
            <FEllipsis v-if="row.editUsers.length > 0">
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
            <FEllipsis v-if="row.accessUsers.length > 0">
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
</template>
<script lang="ts" setup>
import { onMounted, ref, nextTick, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { FTable } from '@fesjs/fes-design';
import { getUrlParams } from '@fesjs/traction-widget';
import { useDataList } from './hooks/useDataList';
import type {
  DataType,
  SearchFormType,
  WorkspaceInfoType,
} from './types/index';
import api from './api';
import EditProject from './components/editProject.vue';

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

// 获取表格数据
const fetchTableData = async () => {
  const params = {
    pageNow: pagination.current,
    pageSize: pagination.size,
    ...searchForm.value,
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
