<template>
  <div class="health-badge-wrap">
    <span
      v-for="code in displayLabels"
      :key="code"
      class="health-badge"
      :class="`health-badge--${colorOf(code)}`"
    >
      {{ textOf(code) }}
    </span>
    <span v-if="!displayLabels.length" class="health-badge-empty">--</span>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { HealthLabelCode } from '../types/index';

const props = defineProps<{
  /** 后端返回的健康标签列表 */
  labels?: HealthLabelCode[];
}>();

const { t } = useI18n();

/**
 * 过滤掉重复 / 空值。UNAVAILABLE 优先于 EMPTY_PROJECT/STALE（降级时不展示可能误判的标签）。
 */
const displayLabels = computed<HealthLabelCode[]>(() => {
  const list = props.labels || [];
  if (list.includes('UNAVAILABLE')) {
    // 降级时仅展示"无法评估"，避免展示依赖聚合数据的 EMPTY_PROJECT/STALE 造成假阳性
    return ['UNAVAILABLE'];
  }
  return list.filter(Boolean);
});

/**
 * 标签文案
 */
const textOf = (code: HealthLabelCode): string => {
  switch (code) {
    case 'EMPTY_PROJECT':
      return t('common.healthEmptyProject');
    case 'STALE':
      return t('common.healthStale');
    case 'NO_DESCRIPTION':
      return t('common.healthNoDescription');
    case 'UNAVAILABLE':
      return t('common.healthUnavailable');
    default:
      return code;
  }
};

/**
 * 标签颜色：空项目=红、长期未更新=橙、描述缺失=灰、无法评估=灰
 */
const colorOf = (code: HealthLabelCode): string => {
  switch (code) {
    case 'EMPTY_PROJECT':
      return 'red';
    case 'STALE':
      return 'orange';
    case 'NO_DESCRIPTION':
    case 'UNAVAILABLE':
    default:
      return 'gray';
  }
};
</script>

<style lang="less" scoped>
.health-badge-wrap {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}
.health-badge {
  display: inline-block;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
  font-size: 12px;
  border-radius: 11px;
  border: 1px solid transparent;
  white-space: nowrap;
}
.health-badge--red {
  color: #e54040;
  background-color: #ffeceb;
  border-color: #ffd6d6;
}
.health-badge--orange {
  color: #d9800b;
  background-color: #fff3e0;
  border-color: #ffe2b8;
}
.health-badge--gray {
  color: #86909c;
  background-color: #f2f3f5;
  border-color: #e5e6eb;
}
.health-badge-empty {
  color: #86909c;
}
</style>
