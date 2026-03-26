<template>
  <FForm
    ref="formRef"
    label-position="top"
    :model="formData"
    :rules="formRules"
  >
    <FFormItem :label="$t('_.规则类型')" prop="ruleType">
      <FSelect
        v-model="formData.ruleType"
        :options="ruleTypes"
        :placeholder="$t('_.请选择规则类型')"
        filterable
        clearable
        value-field="value"
        label-field="label"
        @change="handleChange('rule_type')"
      />
    </FFormItem>
    <FFormItem :label="$t('_.关联应用')" prop="application">
      <FSelect
        v-model="formData.application"
        :options="allBindApplications"
        :placeholder="$t('_.请选择关联应用')"
        filterable
        clearable
        value-field="valueField"
        label-field="labelField"
        @focus="handleSelect('bind_application')"
        @change="handleChange('bind_application')"
      />
    </FFormItem>
    <FFormItem
      :label="$t('_.引擎类型')"
      prop="engineName"
      :show-message="formData.application !== '*'"
    >
      <FSelect
        v-model="formData.engineName"
        :options="engineNames"
        :placeholder="$t('_.请选择引擎类型')"
        filterable
        clearable
        :disabled="formData.application === '*'"
        value-field="valueField"
        label-field="labelField"
        @focus="handleSelect('engine_name', formData.application)"
        @change="handleChange('engine_name')"
      />
    </FFormItem>
    <FFormItem :label="$t('_.模板名称')" prop="templateId">
      <FSelect
        v-model="formData.templateId"
        :options="templateNames"
        :placeholder="$t('_.请选择模板名称')"
        filterable
        clearable
        value-field="valueField"
        label-field="labelField"
        @focus="handleSelect('template_name', formData.engineName)"
      />
    </FFormItem>
    <FFormItem :label="$t('_.覆盖范围')" prop="permissionType">
      <FSelect
        v-model="formData.permissionType"
        :options="overlayList"
        :placeholder="$t('_.请选择覆盖范围')"
        filterable
        clearable
        value-field="value"
        label-field="label"
        :disabled="+formData.ruleType === 1"
        @change="handleChange('permission_type')"
      />
    </FFormItem>
    <FFormItem
      v-if="+formData.permissionType === 1"
      ref="permissionUsersRef"
      :label="$t('_.覆盖用户')"
      prop="permissionUsers"
    >
      <FSelectCascader
        v-model="formData.permissionUsers"
        :data="cascadUserList"
        :placeholder="$t('_.请选择覆盖用户')"
        multiple
        :cascade="true"
        check-strictly="child"
        clearable
        expand-trigger="click"
        :show-path="false"
        :filterable="true"
      />
      <UploadOutlined class="upload" :size="20" @click="openBatchUpload" />
    </FFormItem>
    <FFormItem
      v-if="+formData.permissionType === 2"
      :label="$t('_.覆盖部门')"
      prop="permissionDepartments"
    >
      <FSelect
        v-model="formData.permissionDepartments"
        :options="allDeptList"
        :placeholder="
          $t(
            '_.默认为全部，若指定，则只有指定部门的工作空间新用户，才会下发该规则'
          )
        "
        filterable
        clearable
        multiple
        collapse-tags
        :collapse-tags-limit="2"
      />
    </FFormItem>
  </FForm>
  <FModal
    v-model:show="showBatchUpload"
    :title="$t('_.批量导入用户')"
    :mask-closable="false"
    display-directive="if"
    @ok="handleOkUpload"
    @cancel="handleCancelUpload"
  >
    <FInput
      v-model="batchUploadForm.batchUsers"
      type="textarea"
      :placeholder="$t('_.请输入用户名，例如: enjoyyin,owewnxu,leebai')"
    />
  </FModal>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { UploadOutlined } from '@fesjs/fes-design/icon';
import { ref, computed, onMounted } from 'vue';
import { useDataList } from './hooks/useDataList';
import { FForm, FMessage, FSelectCascader } from '@fesjs/fes-design';
import { request } from '@dataspherestudio/shared';
import { useDataList as useOtherDataList } from '../hooks/useDataList';
import api from './api';

const { t: $t } = useI18n();

const props = defineProps({
  workspaceId: {
    type: String,
    required: true,
    default: '',
  },
});
const workspaceId = computed(() => props.workspaceId);
const permissionUsersRef = ref(null);
const {
  allBindApplications, // 关联应用
  engineNames, // 引擎类型
  templateNames, // 模板名称
  ruleTypes, // 规则类型
  overlayAreas, // 覆盖范围,
  handleSelect,
} = useDataList(workspaceId);

const {
  allDeptList,
  loadAllDeptList,
  allWorkSpaceUserDeptsList,
  loadAllWorkSpaceUserDeptsList,
  cascadUserList,
  processUserData,
} = useOtherDataList();

interface FormDataType {
  ruleType: string;
  templateId: string;
  engineName: string;
  permissionType: string | number;
  permissionUsers?: string[];
  application: string;
  permissionDepartments?: string[];
}

const init = (): FormDataType => ({
  ruleType: '', // 规则类型
  templateId: '', // 模板id
  engineName: '', // 引擎类型
  permissionType: '', // 覆盖范围
  permissionUsers: [], // 覆盖用户
  application: '', // 应用类型
  permissionDepartments: [], // 覆盖部门 只有工作空间新用户会选择
});
const formData = ref<FormDataType>(init());

onMounted(() => {
  formData.value = init();
});

const formRules = computed(() => ({
  ruleType: [
    {
      required: true,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
  templateId: [
    {
      required: true,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
  engineName: [
    {
      required: true,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
  application: [
    {
      required: true,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
  permissionType: [
    {
      required: true,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
  permissionUsers: [
    {
      required: true,
      type: 'array' as const,
      message: $t('_.请选择'),
      trigger: ['change', 'blur'],
    },
  ],
}));

// 自身校验
const formRef = ref<InstanceType<typeof FForm> | null>(null);
async function submit() {
  await formRef.value?.validate();
  const param: { [index: string]: any } = {
    ruleType: formData.value.ruleType,
    templateId: formData.value.templateId,
    engineName: formData.value.engineName,
    permissionType: +formData.value.permissionType,
    application: formData.value.application,
  };
  if (param.permissionType === 1) {
    param.permissionUsers = formData.value.permissionUsers || [];
  }
  if (
    formData.value.permissionDepartments &&
    formData.value.permissionDepartments.length > 0
  ) {
    param.permissionType = 3;
    param.permissionDepartments = formData.value.permissionDepartments || [];
  }
  await request.fetch(api.saveConfTemplateApplyRule, param, 'put');
  FMessage.success($t('_.新建规则成功!'));
}

// 覆盖范围
const overlayList = ref<Record<string, unknown>[]>([]);
function handleChange(type: string) {
  switch (type) {
    case 'rule_type':
      if (+formData.value.ruleType === 1) {
        formData.value.permissionType = '2';
        formData.value.permissionUsers = [];
        formData.value.permissionDepartments = [];
        overlayList.value = overlayAreas.value.filter(
          (item) => item.value === '2'
        );
        formRef.value?.clearValidate();
      } else {
        formData.value.permissionType = '';
        formData.value.permissionUsers = [];
        formData.value.permissionDepartments = [];
        overlayList.value = overlayAreas.value.filter(
          (item) => item.value !== '2'
        );
      }
      break;
    case 'bind_application':
      engineNames.value =
        formData.value.application === '*'
          ? [{ valueField: '*', labelField: $t('_.全局设置') }]
          : [];
      formData.value.engineName = formData.value.application === '*' ? '*' : '';
      templateNames.value = [];
      formData.value.templateId = '';
      break;
    case 'engine_name':
      templateNames.value = [];
      formData.value.templateId = '';
      break;
    case 'permission_type':
      formData.value.permissionUsers = [];
      break;
    default:
      break;
  }
}
const batchUploadForm = ref({
  batchUsers: '',
  batchUserArray: [],
});
const showBatchUpload = ref(false);
const openBatchUpload = () => {
  showBatchUpload.value = true;
  batchUploadForm.value.batchUsers = '';
};
const handleCancelUpload = () => {
  showBatchUpload.value = false;
};
const handleOkUpload = () => {
  batchUploadForm.value.batchUserArray =
    batchUploadForm.value.batchUsers.split(/[,，]/);
  // 递归函数，用于查找用户
  const findUserInTree = (tree, userName) => {
    for (let node of tree) {
      if (node.type === 'user' && node.name === userName) {
        return node;
      }
      if (node.child && node.child.length > 0) {
        const result = findUserInTree(node.child, userName);
        if (result) return result;
      }
    }
    return null;
  };

  const tempInsertData = batchUploadForm.value.batchUserArray
    .map((element) => findUserInTree(allWorkSpaceUserDeptsList.value, element))
    .filter(Boolean); // 过滤掉 null 值
  tempInsertData.forEach((element) => {
    if (!formData.value.permissionUsers.includes(element.name)) {
      formData.value.permissionUsers.push(element.name);
    }
  });
  showBatchUpload.value = false;
  if (formData.value.permissionUsers.length > 0) {
    permissionUsersRef.value.clearValidate();
  }
};

onMounted(async () => {
  await loadAllDeptList();
  await loadAllWorkSpaceUserDeptsList(workspaceId.value as string);
  processUserData(allWorkSpaceUserDeptsList.value);
});

defineExpose({ submit });
</script>
<style lang="less" scoped>
.upload {
  margin-left: 8px;
}
</style>
