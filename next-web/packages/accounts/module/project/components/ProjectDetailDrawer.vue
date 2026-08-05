<template>
  <FDrawer
    v-model:show="drawerShow"
    :title="$t('common.projectDetail')"
    display-directive="if"
    dimension="640px"
    @cancel="handleClose"
  >
    <BPageLoading v-if="loading" action-type="loading" />
    <div v-else-if="!detail" class="detail-fetch-failed">
      {{ $t('common.fetchFailed') }}
    </div>
    <div v-else class="project-detail">
      <!-- 1. 基础信息 -->
      <section class="detail-section">
        <h4 class="detail-section__title">{{ $t('common.basicInfo') }}</h4>
        <div class="basic-info-grid">
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.projectName') }}</span>
            <span class="basic-info-value">{{ detail.basicInfo?.name || '--' }}</span>
          </div>
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.inWorkspace') }}</span>
            <span class="basic-info-value">{{ detail.basicInfo?.workspaceName || '--' }}</span>
          </div>
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.projectCreator') }}</span>
            <span class="basic-info-value">{{ detail.basicInfo?.createBy || '--' }}</span>
          </div>
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.projectCreateTime') }}</span>
            <span class="basic-info-value">{{ detail.basicInfo?.createTime || '--' }}</span>
          </div>
          <div class="basic-info-item basic-info-item--full">
            <span class="basic-info-label">{{ $t('common.projectDesc') }}</span>
            <span class="basic-info-value">{{ detail.basicInfo?.description || '--' }}</span>
          </div>
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.workflowCount') }}</span>
            <span class="basic-info-value">{{ formatStat(detail.basicInfo?.workflowCount) }}</span>
          </div>
          <div class="basic-info-item">
            <span class="basic-info-label">{{ $t('common.nodeCount') }}</span>
            <span class="basic-info-value">{{ formatStat(detail.basicInfo?.nodeCount) }}</span>
          </div>
        </div>
      </section>

      <!-- 2. 工作流列表 -->
      <section class="detail-section">
        <h4 class="detail-section__title">
          {{ $t('common.workflowList') }}
          <span class="detail-section__count">
            ({{ detail.workflowList?.length || 0 }})
          </span>
        </h4>
        <div v-if="detail.workflowDegraded" class="detail-fetch-failed">
          {{ $t('common.fetchFailed') }}
        </div>
        <FTable
          v-else-if="detail.workflowList && detail.workflowList.length"
          :data="detail.workflowList"
          size="small"
          max-height="240"
        >
          <f-table-column
            prop="name"
            :label="$t('common.workflowName')"
            ellipsis
          />
          <f-table-column
            prop="updateTime"
            :label="$t('common.updateTime')"
            :width="170"
            ellipsis
          >
            <template #default="{ row }">
              {{ row.updateTime || '--' }}
            </template>
          </f-table-column>
          <f-table-column
            prop="status"
            :label="$t('common.releaseStatus')"
            :width="110"
          >
            <template #default="{ row }">
              {{ row.status || '--' }}
            </template>
          </f-table-column>
        </FTable>
        <div v-else class="detail-empty">{{ $t('common.noData') }}</div>
      </section>

      <!-- 3. 节点类型分布（CSS 条形图） -->
      <section class="detail-section">
        <h4 class="detail-section__title">
          {{ $t('common.nodeTypeDistribution') }}
          <span v-if="nodeTotal > 0" class="detail-section__count">
            ({{ $t('common.totalNodes', { count: nodeTotal }) }})
          </span>
        </h4>
        <div v-if="detail.nodeDegraded" class="detail-fetch-failed">
          {{ $t('common.fetchFailed') }}
        </div>
        <div
          v-else-if="detail.nodeTypeDistribution && detail.nodeTypeDistribution.length"
          class="node-dist-list"
        >
          <div
            v-for="item in detail.nodeTypeDistribution"
            :key="item.jobType"
            class="node-dist-row"
          >
            <span
              class="node-dist-name"
              :title="item.nodeTypeName || item.jobType"
            >
              {{ item.nodeTypeName || item.jobType }}
            </span>
            <div class="node-dist-bar-bg">
              <div
                class="node-dist-bar-fill"
                :style="{ width: barWidth(item.count) }"
              />
            </div>
            <span class="node-dist-count">{{ item.count }}</span>
          </div>
        </div>
        <div v-else class="detail-empty">{{ $t('common.noData') }}</div>
      </section>

      <!-- 4. 数据源摘要 -->
      <section class="detail-section">
        <h4 class="detail-section__title">
          {{ $t('common.dataSourceSummary') }}
          <span class="detail-section__count">
            ({{ detail.dataSourceList?.length || 0 }})
          </span>
        </h4>
        <FTable
          v-if="detail.dataSourceList && detail.dataSourceList.length"
          :data="detail.dataSourceList"
          size="small"
          max-height="200"
        >
          <f-table-column
            prop="dataSourceName"
            :label="$t('common.dataSourceName')"
            ellipsis
          />
          <f-table-column
            prop="dataSourceType"
            :label="$t('common.dataSourceType')"
            :width="160"
            ellipsis
          />
        </FTable>
        <div v-else class="detail-empty">{{ $t('common.noData') }}</div>
      </section>
    </div>
  </FDrawer>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { FDrawer, FTable } from '@fesjs/fes-design';
import { BPageLoading } from '@fesjs/traction-widget';
import api from '../api';
import type { ProjectDetailVO } from '../types/index';

const props = defineProps<{
  show: boolean;
  projectId: number | null;
  workspaceId: string;
}>();
const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
}>();

const { t: $t } = useI18n();

const drawerShow = computed({
  get: () => props.show,
  set: (value: boolean) => emit('update:show', value),
});

const loading = ref(false);
const detail = ref<ProjectDetailVO | null>(null);

/** 节点总数 */
const nodeTotal = computed(() => {
  const list = detail.value?.nodeTypeDistribution || [];
  return list.reduce((sum, item) => sum + (item.count || 0), 0);
});

/** 求最大计数（用于条形宽度），最少为 1 避免除零 */
const maxCount = computed(() => {
  const list = detail.value?.nodeTypeDistribution || [];
  const max = list.reduce((m, item) => Math.max(m, item.count || 0), 0);
  return Math.max(max, 1);
});

/** 条形宽度百分比，最小 5% 保证可见 */
const barWidth = (count: number): string =>
  `${Math.max(((count || 0) / maxCount.value) * 100, 5)}%`;

/** 统计列降级显示 */
const formatStat = (value: any): string => {
  if (value === null || value === undefined) return '--';
  return value;
};

/** 拉取详情 */
const fetchDetail = async (projectId: number) => {
  loading.value = true;
  detail.value = null;
  try {
    const res = await api.getProjectDetailApi({
      projectId,
      workspaceId: props.workspaceId,
    });
    detail.value = res?.data?.detail || null;
  } catch (err) {
    console.error('getProjectDetail failed', err);
    detail.value = null;
  } finally {
    loading.value = false;
  }
};

// projectId 变化或抽屉打开时拉取
watch(
  () => [props.projectId, props.show],
  ([id, show]) => {
    if (show && id) {
      fetchDetail(id as number);
    }
  },
  { immediate: true }
);

const handleClose = () => {
  emit('update:show', false);
};
</script>

<style lang="less" scoped>
.project-detail {
  padding-bottom: 24px;
}
.detail-section {
  margin-bottom: 24px;
  &__title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #0f1222;
  }
  &__count {
    margin-left: 4px;
    font-size: 12px;
    font-weight: 400;
    color: #86909c;
  }
}
.basic-info-grid {
  display: flex;
  flex-wrap: wrap;
}
.basic-info-item {
  width: 50%;
  margin-bottom: 12px;
  padding-right: 12px;
  font-size: 13px;
  &--full {
    width: 100%;
  }
}
.basic-info-label {
  display: block;
  margin-bottom: 4px;
  color: #86909c;
}
.basic-info-value {
  display: block;
  color: #0f1222;
  word-break: break-all;
}
.detail-fetch-failed {
  color: #e54040;
  font-size: 13px;
  padding: 8px 0;
}
.detail-empty {
  color: #86909c;
  font-size: 13px;
  padding: 16px 0;
  text-align: center;
}
/* 节点类型分布 CSS 条形图 */
.node-dist-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.node-dist-row {
  display: flex;
  align-items: center;
  font-size: 13px;
}
.node-dist-name {
  flex: 0 0 140px;
  width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #0f1222;
}
.node-dist-bar-bg {
  flex: 1;
  height: 14px;
  margin: 0 12px;
  background-color: #f2f3f5;
  border-radius: 7px;
  overflow: hidden;
}
.node-dist-bar-fill {
  height: 100%;
  min-width: 6px;
  background: linear-gradient(90deg, #5384ff, #6ea2ff);
  border-radius: 7px;
  transition: width 0.3s ease;
}
.node-dist-count {
  flex: 0 0 40px;
  width: 40px;
  text-align: right;
  color: #0f1222;
  font-weight: 600;
}
</style>
