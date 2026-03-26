<template>
  <FModal
    v-model:show="showModal"
    :title="$t('_.编辑')"
    display-directive="if"
    :cancel-text="$t('_.取消')"
    :ok-text="$t('_.确认')"
    :mask-closable="false"
    :ok-loading="editLoading"
    @ok="handleOk"
  >
    <FForm
      ref="formRef"
      label-position="top"
      :model="formData"
      :rules="formRule"
    >
      <FFormItem prop="name" :label="$t('common.projectName')">
        <FInput
          v-model="formData.name"
          :placeholder="$t('common.pleaseInput')"
          disabled
        />
      </FFormItem>
      <FFormItem prop="description" :label="$t('common.projectDesc')">
        <FInput
          v-model="formData.description"
          :placeholder="$t('common.pleaseInput')"
          type="textarea"
          :maxlength="250"
          clearable
        />
      </FFormItem>
      <FFormItem prop="releaseUsers" :label="$t('common.releaseAuth')">
        <FSelect
          v-model="formData.releaseUsers"
          :options="releaseUsers"
          :placeholder="$t('common.pleaseSelect')"
          filterable
          clearable
          multiple
        />
      </FFormItem>
      <FFormItem prop="editUsers" :label="$t('common.editAuth')">
        <FSelect
          v-model="formData.editUsers"
          :options="editUsers"
          :placeholder="$t('common.pleaseSelect')"
          filterable
          clearable
          multiple
        />
      </FFormItem>
      <FFormItem prop="accessUsers" :label="$t('common.accessAuth')">
        <FSelect
          v-model="formData.accessUsers"
          :options="accessUsers"
          :placeholder="$t('common.pleaseSelect')"
          filterable
          clearable
          multiple
        />
      </FFormItem>
    </FForm>
  </FModal>
</template>

<script lang="ts" setup name="editWorkflow">
import { useI18n } from 'vue-i18n';
import { ref, computed, defineProps, defineEmits, onMounted } from 'vue';
import { cloneDeep } from 'lodash-es';
import { useDataList } from '../hooks/useDataList';
import api from '../api';
import { FMessage } from '@fesjs/fes-design';

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
  config: {
    type: Object,
    default: () => ({}),
  },
  form: {
    type: Object,
    default: () => ({}),
  },
});

const { accessUsers, editUsers, releaseUsers, fetchUserList } = useDataList();

const formData = ref({
  id: '',
  name: '',
  description: '',
  releaseUsers: [],
  editUsers: [],
  accessUsers: [],
});
const showModal = computed({
  get() {
    return !!props.show;
  },
  set(val) {
    emits('update:show', val);
  },
});

const formRef = ref(null);

const formRule = ref({
  name: [
    {
      required: true,
      message: $t('_.不能为空'),
      trigger: ['blur'],
    },
  ],
  description: [
    {
      required: true,
      message: $t('_.不能为空'),
      trigger: ['blur'],
    },
  ],
});
const editLoading = ref(false);
const handleOk = async () => {
  try {
    await formRef.value?.validate();
    if (!editLoading.value) {
      editLoading.value = true;
      const param = {
        id: formData.value.id,
        workspaceId: props.config.workspaceId,
        name: formData.value.name,
        description: formData.value.description,
        releaseUsers: formData.value.releaseUsers,
        editUsers: formData.value.editUsers,
        accessUsers: formData.value.accessUsers,
      };
      await api.editProjectApi(param);
      FMessage.success($t('_.编辑成功'));
      showModal.value = false;
      editLoading.value = false;
      emits('success');
    }
  } catch (errMsg) {
    console.error(errMsg);
    editLoading.value = false;
  }
};

onMounted(async () => {
  fetchUserList({ workspaceId: props.config.workspaceId });
  formData.value = cloneDeep(props.form);
});
</script>

<style lang="less" scoped></style>
