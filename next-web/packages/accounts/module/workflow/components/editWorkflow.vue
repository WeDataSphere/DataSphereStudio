<template>
  <FModal
    v-model:show="showModal"
    :title="$t('_.编辑')"
    display-directive="if"
    :ok-text="$t('_.确定')"
    :cancel-text="$t('_.取消')"
    :mask-closable="false"
    @ok="handleOk"
  >
    <FForm
      ref="formRef"
      label-position="top"
      :model="formData"
      :rules="formRules"
    >
      <FFormItem :label="$t('_.工作流ID')">
        <FInput
          v-model="formData.orchestratorId"
          :placeholder="$t('_.工作流ID')"
          disabled
        />
      </FFormItem>
      <FFormItem :label="$t('_.工作流名称')">
        <FInput
          v-model="formData.orchestratorName"
          :placeholder="$t('_.工作流名称')"
        />
      </FFormItem>
      <FFormItem :label="$t('_.工作流描述')">
        <FInput
          v-model="formData.description"
          :placeholder="$t('_.工作流描述')"
          type="textarea"
        />
      </FFormItem>
      <FFormItem :label="$t('_.默认参数模板')">
        <FSelect
          v-model="formData.templateIds"
          filterable
          multiple
          @change="handleChange"
        >
          <FSelectGroupOption
            v-for="(group, index) in templateList"
            :key="group.enginType + index"
            :label="group.enginType"
            :disabled="
              group.child.some((template) =>
                formData.templateIds.includes(template.templateId)
              )
            "
          >
            <FOption
              v-for="item in group.child"
              :key="item.templateId"
              :value="item.templateId"
            >
              {{ item.templateName }}
            </FOption>
          </FSelectGroupOption>
        </FSelect>
      </FFormItem>
      <FFormItem
        v-if="formData.templateIds && formData.templateIds.length"
        :label="$t('_.新增节点是否默认引用资源参数模板')"
        prop="isDefaultReference"
      >
        <FRadioGroup v-model="formData.isDefaultReference">
          <FRadio value="1">
            {{ $t('_.是') }}
          </FRadio>
          <FRadio value="0">
            {{ $t('_.否') }}
          </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem :label="$t('_.代理用户')" prop="proxyUser">
        <FSelect
          v-model="formData.proxyUser"
          :options="fullUserList"
          :placeholder="$t('_.请选择')"
          filterable
          clearable
          tag
        />
      </FFormItem>
    </FForm>
  </FModal>
</template>

<script lang="ts" setup name="editWorkflow">
import { useI18n } from 'vue-i18n';

import { ref, computed, watch, onMounted } from 'vue';
import { FForm, FMessage } from '@fesjs/fes-design';
import { useDataList } from '../hooks/useDataList';
import type { BaseType, WorkflowEiditFormType } from '../types/index';
import api from '../api';

const { t: $t } = useI18n();

const emits = defineEmits<{
  (e: 'update:show', val: { editWorkflow: boolean }): void;
  (e: 'success'): void;
}>();
const props = defineProps({
  show: {
    type: Object,
    default: () => ({}),
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

const {
  userList,
  templateList,
  fetchUserList,
  fetchTemplateList,
  fetchFlowTemplateList,
} = useDataList();

const formData = ref<WorkflowEiditFormType>({
  projectId: '',
  orchestratorId: '',
  orchestratorName: '',
  templateIds: [],
  isDefaultReference: '',
  proxyUser: '',
  description: '',
});

const formRules = {
  isDefaultReference: [{ required: true, message: $t('_.请选择') }],
  proxyUser: [
    {
      pattern: new RegExp(/^[a-zA-Z0-9_]+$/),
      message: $t('_.只能由于字母、数字、下划线组成'),
      trigger: ['blur', 'change'],
    },
  ],
};

const fullUserList = computed(() => {
  let current: BaseType[] = [];
  if (
    props.form.proxyUser &&
    !userList.value.some(
      (item: BaseType) => item.value === props.form.proxyUser
    )
  ) {
    current = [{ value: props.form.proxyUser, label: props.form.proxyUser }];
  }
  return [...current, ...userList.value];
});

const showModal = computed({
  get() {
    return !!props.show?.editWorkflow;
  },
  set(val) {
    emits('update:show', { editWorkflow: val });
  },
});

watch(
  () => showModal.value,
  (show) => {
    if (show) {
      formData.value = {
        projectId: props.form.projectId,
        orchestratorId: props.form.orchestratorId,
        orchestratorName: props.form.orchestratorName,
        description: props.form.description,
        templateIds: [],
        isDefaultReference: props.form.isDefaultReference,
        proxyUser: props.form.proxyUser || '',
      };
      fetchTemplateList({ projectId: formData.value.projectId });
      fetchFlowTemplateList(
        { orchestratorId: formData.value.orchestratorId },
        (templates) => {
          formData.value.templateIds = templates.map((item) => item.templateId);
        }
      );
    }
  },
  { immediate: true }
);

const formRef = ref<InstanceType<typeof FForm> | null>(null);
const handleChange = (val: any[]) => {
  if (!val || val.length < 1) {
    formData.value.isDefaultReference = '';
  }
};

const handleOk = async () => {
  try {
    await formRef.value?.validate();
    const param = {
      workspaceId: props.config.workspaceId,
      projectId: formData.value.projectId,
      orchestratorId: formData.value.orchestratorId,
      orchestratorName: formData.value.orchestratorName,
      description: formData.value.description,
      isDefaultReference: formData.value.isDefaultReference,
      proxyUser: formData.value.proxyUser,
    };
    await api.WorkflowModifyApi(param);
    const templateApiParam: Pick<
      WorkflowEiditFormType,
      'projectId' | 'orchestratorId' | 'templateIds'
    > = {
      projectId: formData.value.projectId,
      orchestratorId: formData.value.orchestratorId,
      templateIds: formData.value.templateIds,
    };
    await api.SaveWorkflowTemplateApi(templateApiParam);
    FMessage.success($t('_.编辑成功'));
    showModal.value = false;
    emits('success');
  } catch (error: any) {
    console.log('edit error', error);
  }
};

onMounted(() => {
  fetchUserList();
});
</script>

<style lang="less" scoped></style>
