<template>
  <div class="wd-page">
    <BHorizontalLayout
      v-model:cur-path="route.path"
      :menus="menus"
      @menu-change="onMenuClick"
    >
      <template #container>
        <router-view />
      </template>
    </BHorizontalLayout>
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUrlParams } from '@fesjs/traction-widget';

const { t: $t } = useI18n();

const router = useRouter();
const route = useRoute();

const menus = ref([
  {
    label: $t('_.项目台账明细'),
    value: '/accounts/project',
  },
  {
    label: $t('_.工作流台账明细'),
    value: '/accounts/workflow',
  },
  {
    label: $t('_.节点台账明细'),
    value: '/accounts/dataVisualization',
    children: [
      {
        label: $t('_.数据开发节点'),
        value: '/accounts/dataDev',
      },
      {
        label: $t('_.数据可视化节点'),
        value: '/accounts/dataVisualization',
      },
      {
        label: $t('_.信号节点'),
        value: '/accounts/signal/datachecker',
      },
    ],
  },
]);

const onMenuClick = (e: { [key: string]: any }) => {
  const { workspaceId, timestamp = '' } = getUrlParams() || {};
  const path = `${e.value}?workspaceId=${workspaceId}`;
  if (/^https?:\/\//.test(path)) {
    window.open(path, '_blank');
  } else if (/^\//.test(path)) {
    if (top !== self) {
      sessionStorage.setItem(
        'meta_workflow_current_path',
        JSON.stringify({ path: e.value, key: timestamp })
      );
    }
    router.push(path);
  } else {
    console.warn(
      '[plugin-layout]: 菜单的path只能使以http(s)开头的网址或者路由地址'
    );
  }
};
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
