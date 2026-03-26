<template>
  <FModal
    v-model:show="showModal"
    :title="$t('_.高级筛选')"
    display-directive="if"
    :ok-text="$t('_.确定')"
    :cancel-text="$t('_.取消')"
    :mask-closable="false"
    @ok="handleOk"
  >
    <FForm ref="filterForm" label-position="top">
      <FFormItem :label="$t('_.节点名称')">
        <FSelect
          v-model="formData.nodeName"
          :options="allNodeList"
          :placeholder="$t('_.请选择')"
          filterable
          clearable
          value-field="title"
          label-field="title"
        />
      </FFormItem>
      <FFormItem :label="$t('_.节点类型')">
        <FSelect
          v-model="formData.nodeType"
          :options="nodeTypes"
          :placeholder="$t('_.请选择')"
          filterable
          clearable
          multiple
          collapse-tags
          :collapse-tags-limit="2"
          value-field="value"
          label-field="label"
        />
      </FFormItem>
      <FFormItem :label="$t('_.资源参数模板')">
        <FSelectCascader
          v-model="formData.templateId"
          class="select-cascader"
          :placeholder="$t('_.请选择')"
          :data="templateData"
          clearable
          remote
          :emit-path="false"
          expand-trigger="click"
          :show-path="false"
          :load-data="loadTemplates"
        />
      </FFormItem>
      <FFormItem :label="$t('_.更新人')">
        <FSelect
          v-model="formData.modifyUser"
          :options="userList"
          :placeholder="$t('_.请选择')"
          filterable
          clearable
          value-field="value"
          label-field="label"
        />
      </FFormItem>
      <FFormItem :label="$t('_.修改时间')">
        <FDatePicker
          v-model="formData.updateTimes"
          type="datetimerange"
          format="yyyy/MM/dd HH:mm:ss"
          clearable
          :placeholder="[$t('common.startDateTime'), $t('common.endDateTime')]"
        />
      </FFormItem>
    </FForm>
  </FModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n';

import { ref, computed, defineProps, defineEmits, watch, inject } from 'vue';
// import { useI18n } from 'vue-i18n';
import { cloneDeep } from 'lodash-es';
import { useTemplateList } from './hooks/useTemplateList';

const { t: $t } = useI18n();

const emits = defineEmits(['success', 'update:show']);
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

// 默认筛选参数
const formData = ref({
  nodeName: '',
  nodeType: [],
  modifyUser: '',
  templateId: '',
  updateTimes: [],
});

// 控制弹窗显示
const showModal = computed({
  get: () => {
    return props.show;
  },
  set: (val) => {
    emits('update:show', val);
  },
});

watch(
  () => showModal.value,
  (show) => {
    if (show) {
      formData.value = cloneDeep(props.form);
    }
  }
);

/**
 * @description: 确认筛选
 * @return {*}
 */
const handleOk = () => {
  showModal.value = false;
  emits('success', formData.value);
};

const nodeTypes = inject('nodeTypes');
const templateList = inject('templateList');
const userList = inject('userList');
const allNodeList = inject('allNodeList');

const { templateData, createData, loadTemplates } =
  useTemplateList(templateList);

watch(
  () => templateList.value,
  (list) => {
    const arr = Array.from([
      ...new Set(list.map((item) => item.engineType)),
    ]).map((v) => ({ value: v, label: v }));
    templateData.value = createData(arr);
  },
  { immediate: true, deep: true }
);
</script>

<style lang="less" scoped></style>
