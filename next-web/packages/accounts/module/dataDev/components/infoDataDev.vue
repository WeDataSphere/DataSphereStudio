<template>
  <FModal
    v-model:show="showModal"
    :title="$t('_.参数信息')"
    display-directive="if"
    :ok-text="$t('_.确认')"
    :cancel-text="$t('_.取消')"
    :mask-closable="false"
    @ok="handleOk"
  >
    <div class="info-data">
      <div class="node-title">
        {{ $t('_.节点名') }}
      </div>
      <div class="node-desc">
        {{ formData.nodeName }}
      </div>
      <div class="node-title">
        {{ $t('_.参数详情') }}
      </div>
      <div class="node-desc">
        <div v-for="(value, key) in nodeDetail" :key="key">
          <!-- <span style="color: #63656f">{{ obj.name }}[{{ obj.key }}]：</span> -->
          <span style="color: #63656f">{{ key }}：</span>
          <span>{{ value }}</span
          >&nbsp;&nbsp;
        </div>
      </div>
    </div>
  </FModal>
</template>

<script lang="ts" setup name="editWorkflow">
import { useI18n } from 'vue-i18n';

import { ref, computed, defineProps, defineEmits, onMounted } from 'vue';
import { cloneDeep } from 'lodash-es';
import api from '../api';
import apiVisual from '@/packages/accounts/module/dataVisualization/api';
import { FMessage } from '@fesjs/fes-design';
import type { DataType } from '../types/index';

const { t: $t } = useI18n();

const emits = defineEmits<{
  (e: 'update:show'): void;
  (e: 'success'): void;
}>();
const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  form: {
    type: Object,
    default: () => ({}),
  },
});

const formData = ref<DataType>(null);
const nodeDetail = ref<DataType>(null);
const showModal = computed({
  get() {
    return !!props.show;
  },
  set(val) {
    emits('update:show', val);
  },
});

const handleOk = async () => {
  showModal.value = false;
};

// 获取表格数据
const fetchNodeInfo = async () => {
  const params = {
    nodeId: formData.value.nodeId,
    contentId: formData.value.contentId,
  };
  try {
    const res: DataType = await api.nodeInfoApi(params);
    nodeDetail.value = res.data.data;
    // console.log('nodeDetail', nodeDetail.value);
  } catch (err) {
    console.error(err);
  }
};
const fetchNodeTemplateInfo = async () => {
  try {
    const res = await apiVisual.GetNodeTemplateDetail({
      templateId: formData.value.templateId,
    });
    const descRes = res.data || {};
    nodeDetail.value = {};
    descRes.conf.forEach((item: { key: string | number; configValue: any }) => {
      nodeDetail.value[item.key] = item.configValue;
    });
  } catch (err) {
    console.error(err);
  }
};
onMounted(async () => {
  formData.value = cloneDeep(props.form);
  if (formData.value.templateId) {
    await fetchNodeTemplateInfo();
  } else {
    await fetchNodeInfo();
  }
});
</script>

<style lang="less" scoped>
.info-data {
  .node-title {
    margin-bottom: 8px;
  }
  .node-desc {
    background: #f8f8f8;
    padding: 8px;
    border-radius: 4px;
    margin-bottom: 24px;
  }
}
</style>
