<template>
  <FModal
    :mask-closable="false"
    :title="$t('_.高级筛选')"
    :show="showAdvanceQuery"
    :ok-text="$t('_.确定')"
    :cancel-text="$t('_.取消')"
    @cancel="advanceCancel"
    @ok="advanceSearch"
  >
    <FForm
      ref="advanceQueryForm"
      :label-width="152"
      :model="curAdvanceSearchFormData"
      label-position="top"
    >
      <FFormItem label="spark.executor.memory">
        <FInput
          v-model="curAdvanceSearchFormData.sparkExecutorMemory"
          :placeholder="$t('_.请输入')"
        />
      </FFormItem>
      <FFormItem label="spark.driver.memory">
        <FInput
          v-model="curAdvanceSearchFormData.sparkDriverMemory"
          :placeholder="$t('_.请输入')"
        />
      </FFormItem>
      <FFormItem label="spark.conf">
        <FInput
          v-model="curAdvanceSearchFormData.sparkConf"
          :placeholder="$t('_.请输入')"
        />
      </FFormItem>
      <FFormItem label="spark.executor.core">
        <FInput
          v-model="curAdvanceSearchFormData.sparkExecutorCore"
          :placeholder="$t('_.请输入')"
        />
      </FFormItem>
      <FFormItem label="spark.executor.instances">
        <FInput
          v-model="curAdvanceSearchFormData.sparkExecutorInstances"
          :placeholder="$t('_.请输入')"
        />
      </FFormItem>
    </FForm>
  </FModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n';

import { defineProps, computed, defineEmits } from 'vue';

const { t: $t } = useI18n();

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  advanceSearchFormData: {
    type: Object,
    default: () => {
      return {};
    },
  },
});

const emit = defineEmits([
  'update:show',
  'search',
  'cancel',
  'update:advanceSearchFormData',
]);

const curAdvanceSearchFormData = computed({
  get: () => props.advanceSearchFormData,
  set: (value) => {
    emit('update:advanceSearchFormData', value);
  },
});
const showAdvanceQuery = computed({
  get: () => props.show,
  set: (value) => {
    emit('update:show', value);
  },
});

const advanceSearch = () => {
  emit('search', curAdvanceSearchFormData.value);
  showAdvanceQuery.value = false;
};

const advanceCancel = () => {
  emit('cancel');
  showAdvanceQuery.value = false;
};
</script>
