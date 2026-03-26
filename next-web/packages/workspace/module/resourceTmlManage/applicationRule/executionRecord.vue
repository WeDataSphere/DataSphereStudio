<template>
  <div class="dss-drawer__table">
    <BTablePage :is-loading="isLoading" action-type="loading">
      <template #search>
        <BSearch
          v-model:form="searchForm"
          @search="handleInit"
          @reset="handleReset"
        >
          <template #form>
            <div>
              <FInput
                v-model="searchForm.templateName"
                :placeholder="$t('_.模板名称')"
              />
            </div>
            <div>
              <FSelect
                v-model="searchForm.engineType"
                :options="allEngineTypes"
                :placeholder="$t('_.引擎类型')"
                filterable
                clearable
                value-field="valueField"
                label-field="labelField"
                @focus="handleSelect('engine_type')"
              />
            </div>
            <div>
              <FSelect
                v-model="searchForm.username"
                :options="allWorkSpaceUserList"
                :placeholder="$t('_.覆盖用户')"
                filterable
                clearable
                value-field="value"
                label-field="label"
                @focus="loadAllWorkSpaceUserList(workspaceId as string)"
              />
            </div>
            <div>
              <FSelect
                v-model="searchForm.application"
                :options="allBindApplications"
                :placeholder="$t('_.关联应用')"
                filterable
                clearable
                value-field="valueField"
                label-field="labelField"
                @focus="handleSelect('bind_application')"
              />
            </div>
          </template>
        </BSearch>
      </template>
      <template #table>
        <FTable ref="tableRef" :data="tableList">
          <FTable-column
            prop="templateName"
            :label="$t('_.模板名称')"
            :min-width="180"
            :formatter="fillText"
            ellipsis
          />
          <FTable-column
            prop="engineType"
            :label="$t('_.引擎类型')"
            :min-width="120"
            :formatter="fillText"
            ellipsis
          >
            <template #default="{ row }">
              {{
                row.engineType === '*' ? `${$t('_.全局设置')}` : row.engineType
              }}
            </template>
          </FTable-column>
          <FTable-column
            prop="userName"
            :label="$t('_.覆盖用户人')"
            :min-width="120"
            :formatter="fillText"
            ellipsis
          />
          <FTable-column
            prop="application"
            :label="$t('_.关联应用')"
            :min-width="130"
            :formatter="fillText"
            ellipsis
          >
            <template #default="{ row }">
              {{
                row.application === '*'
                  ? `${$t('_.全局设置')}`
                  : row.application
              }}
            </template>
          </FTable-column>
          <FTable-column
            v-slot="{ row }"
            prop="status"
            :label="$t('_.执行状态')"
            :min-width="88"
            :formatter="fillText"
            ellipsis
          >
            <span v-if="+row.status === 2" style="color: #f75f56">{{
              $t('_.执行失败')
            }}</span>
            <span v-else-if="+row.status === 1" style="color: #00cb91">{{
              $t('_.执行成功')
            }}</span>
            <span v-else style="color: #0f1222">{{ $t('_.未执行') }}</span>
          </FTable-column>
          <FTable-column
            prop="executeUser"
            :label="$t('_.执行人')"
            :min-width="120"
            :formatter="fillText"
            ellipsis
          />
          <FTable-column
            prop="executeTime"
            :label="$t('_.执行时间')"
            :min-width="182"
            :formatter="fillTimeText"
            ellipsis
          />
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
          @change="handleCurrentChange"
        />
      </template>
    </BTablePage>
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { onMounted, ref, computed } from 'vue';
import { usePagination } from '../../hooks/usePagination';
import { useDataList } from './hooks/useDataList';
import { useDataList as useOtherDataList } from '../hooks/useDataList';
import { request } from '@dataspherestudio/shared';
import api from './api';
import type { ComputedRef } from 'vue';
import type { PaginationAndParams } from '../../hooks/usePagination';

const { t: $t } = useI18n();

const props = defineProps({
  workspaceId: {
    type: String,
    required: true,
    default: '',
  },
});

const {
  allBindApplications, // 关联应用
  allEngineTypes, // 引擎类型
  handleSelect,
  jsonFilter,
} = useDataList();
const workspaceId = computed(() => props.workspaceId);
const { allWorkSpaceUserList, loadAllWorkSpaceUserList } = useOtherDataList();

interface FormType {
  templateName: string;
  engineType: string;
  username: string;
  application: string;
}

const init = (): FormType => ({
  templateName: '',
  engineType: '',
  username: '',
  application: '',
});
const searchForm = ref(init());

// 查询参数
const params: ComputedRef<PaginationAndParams> = computed(() => ({
  ...searchForm.value,
}));

/**
 * @description: 获取table数据
 * @return {*}
 */
const tableList = ref([]);
async function loadTable(param: PaginationAndParams) {
  const parametter = jsonFilter({
    ...param,
    workspaceId: workspaceId.value,
  });
  const res = await request.fetch(api.getConfTemplateApplyHistory, parametter);
  tableList.value = res.data?.records;
  return {
    totalPage: Math.ceil(res.data.total / (param.pageSize as number)),
    totalCount: res.data.total,
    pageNow: param.pageNow,
  };
}

/**
 * @description: 分页查询事件
 * @return {*}
 */
const {
  isLoading,
  pagination,
  handleInit,
  handleCurrentChange,
  fillText,
  fillTimeText,
} = usePagination(params, loadTable);

/**
 * @description: 重置
 * @return {*}
 */
function handleReset() {
  searchForm.value = init();
  handleInit();
}

onMounted(() => {
  handleInit();
});
</script>
<style lang="less">
.dss-drawer__table {
  .wd-table-page {
    padding: 0 0;
  }
}
</style>
