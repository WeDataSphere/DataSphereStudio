<template>
  <NavBar
    v-model:modelValue="activeBar"
    :data="bars"
    type="change"
    @change="changeBar"
  />
  <router-view />
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { ref, watch, provide, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getUrlParams } from '@fesjs/traction-widget';
import NavBar from '../components/NavBar/NavBar.vue';
import api from './api';
import type { BaseType, SignalType } from './types/index';

const { t: $t } = useI18n();

const route = useRoute();
const router = useRouter();
const activeBar = ref('datachecker');
const bars = [
  { label: 'datachecker', value: 'datachecker' },
  { label: 'eventsender', value: 'eventsender' },
  { label: 'eventreceiver', value: 'eventreceiver' },
];

watch(
  [() => route.path, () => route.params.type],
  ([path, type]) => {
    if (path.includes('/accounts/signal') && type) {
      activeBar.value = type as string;
    }
  },
  { immediate: true }
);

const uiMap = ref({
  datachecker: [],
  eventsender: [],
  eventreceiver: [],
});

provide(
  'pageState',
  computed(() => {
    const query = getUrlParams() || {};
    return {
      key: activeBar.value,
      uiMap: uiMap.value,
      ...query,
    };
  })
);

const changeBar = (bar: BaseType) => {
  const { workspaceId, timestamp = '' } = getUrlParams() || {};
  const param = {
    name: 'signalNode',
    params: { type: bar.value },
    query: { workspaceId },
  };
  const routeParm = router.resolve(param);
  if (top !== self) {
    sessionStorage.setItem(
      'meta_workflow_current_path',
      JSON.stringify({ path: routeParm.path, key: timestamp })
    );
  }
  router.push(param);
};

api.GetListNodeTypeApi({ labels: 'dev' }).then((res) => {
  const signalNodeTypes = (res.data?.nodeTypes || []).find(
    (item: BaseType) =>
      item.title === '信号节点' || item.title === 'Signal node'
  );
  (signalNodeTypes?.children || []).forEach((item: BaseType) => {
    uiMap.value[item.title as SignalType] = item.nodeUiVOS || [];
  });
});
</script>
<style lang="less" scoped>
.wd-page {
  height: 100%;

  :deep(.wd-side-menus) {
    .wd-logo {
      display: none;
    }
  }
}
</style>
