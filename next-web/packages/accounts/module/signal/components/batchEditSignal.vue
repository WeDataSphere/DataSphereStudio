<template>
  <FDrawer
    v-model:show="showModal"
    :title="editTitle"
    :mask-closable="false"
    display-directive="if"
    footer
    dimension="50%"
    @cancel="handleCancel"
  >
    <FForm ref="baseParamsFormRef" label-position="top" :model="formData">
      <FFormItem v-if="props.config.mode === 'batch'" :label="$t('_.已选节点')">
        <TagEdit
          v-model:tags="nodeList"
          mode="read"
          :label="$t('_.添加标签')"
        />
      </FFormItem>
      <FFormItem v-else :label="$t('_.节点ID')">
        <FInput v-if="currentNode" v-model="currentNode.nodeId" disabled />
      </FFormItem>
      <FFormItem
        v-for="item in baseParamsList"
        :key="poinToLink(item.key)"
        :rules="paramsValid(item)"
        :prop="item.key"
        :class="[
          !item.editable && props.config.mode === 'batch'
            ? 'item-content-hide'
            : '',
        ]"
      >
        <template #label>
          <FCheckbox
            v-if="props.config.mode === 'batch'"
            v-model="item.editable"
            @change="handleCheckBoxChange(item)"
          >
            {{ $t('_.批量编辑') }}{{ item.lableName }}
          </FCheckbox>
          <span v-else>{{ item.lableName }}</span>
        </template>
        <FInput
          v-if="['Input', 'Text', 'Disable'].includes(item.uiType)"
          v-model="formData[item.key]"
          :type="filterFormType(item.uiType)"
          :autosize="{ minRows: 8, maxRows: 8 }"
          :placeholder="item.desc"
          :disabled="item.uiType === 'Disable'"
        />
        <TagEdit
          v-else-if="item.uiType === 'Tag'"
          v-model:tags="formData[item.key]"
          :label="$t('_.添加标签')"
        />
        <FSelect
          v-if="['Select', 'MultiSelect'].includes(item.uiType)"
          v-model="formData[item.key]"
          :placeholder="item.desc"
          clearable
          :multiple="item.uiType === 'MultiSelect'"
        >
          <FOption
            v-for="subItem in JSON.parse(item.value)"
            :key="subItem"
            :value="subItem"
          >
            {{ subItem }}
          </FOption>
        </FSelect>
      </FFormItem>
    </FForm>
    <FForm
      v-if="nodeParamsList.length > 0"
      ref="nodeParamsFormRef"
      label-position="top"
      :model="formData"
    >
      <template v-if="props.config.mode === 'item'">
        <FFormItem :label="$t('_.是否引用资源参数模板')">
          <FSelect v-model="isRefTemplate" @change="handleRefTemplateChange">
            <FOption value="1">
              {{ $t('_.是') }}
            </FOption>
            <FOption value="0">
              {{ $t('_.否') }}
            </FOption>
          </FSelect>
        </FFormItem>
        <FFormItem
          v-if="isRefTemplate === '1'"
          :label="$t('_.资源参数模板名称')"
          prop="ecConfTemplateName"
          :rules="[
            {
              required: true,
              message: $t('_.请选择资源参数模板'),
              trigger: 'change',
              type: 'string',
            },
          ]"
        >
          <FInput
            v-model="formData.ecConfTemplateName"
            :placeholder="$t('_.请选择资源参数模板名称')"
            readonly
            @focus="openTemplateDrawer"
          />
        </FFormItem>
      </template>
      <template v-for="item in nodeParamsList">
        <FFormItem
          v-if="checkShow(item)"
          :key="poinToLink(item.key)"
          :rules="paramsValid(item)"
          :prop="item.key"
          :class="[
            !item.editable && props.config.mode === 'batch'
              ? 'item-content-hide'
              : '',
          ]"
        >
          <template #label>
            <FCheckbox
              v-if="props.config.mode === 'batch'"
              v-model="item.editable"
              @change="handleCheckBoxChange(item)"
            >
              {{ $t('_.批量编辑') }}{{ item.lableName }}
            </FCheckbox>
            <span v-else>{{ item.lableName }}</span>
          </template>
          <FTooltip
            popper-class="form-item-popper"
            :disabled="!['check.object', 'job.desc'].includes(item.lableName)"
            mode="popover"
            :offset="5"
            placement="top-start"
            trigger="focus"
          >
            <FInput
              v-if="['Input', 'Text', 'Disable'].includes(item.uiType)"
              v-model="formData[item.key]"
              :type="filterFormType(item.uiType)"
              :autosize="{ minRows: 8, maxRows: 8 }"
              :placeholder="item.desc"
              :disabled="item.uiType === 'Disable'"
            />
            <TagEdit
              v-else-if="item.uiType === 'Tag'"
              v-model:tags="formData[item.key]"
              :label="$t('_.添加标签')"
            />
            <FSelect
              v-if="['Select', 'MultiSelect'].includes(item.uiType)"
              v-model="formData[item.key]"
              :placeholder="item.desc"
              clearable
              :multiple="item.uiType === 'MultiSelect'"
            >
              <FOption
                v-for="subItem in JSON.parse(item.value)"
                :key="subItem"
                :value="subItem"
              >
                {{ subItem }}
              </FOption>
            </FSelect>
            <template #content>
              <div style="width: 300px">
                {{ item.desc }}
              </div>
            </template>
          </FTooltip>
        </FFormItem>
      </template>
    </FForm>
    <TemplateEidt
      v-model:show="isTemplateDrawerShow"
      :templates="templateList"
      :default-template-id="formData.ecConfTemplateId"
      @success="handleTemplateSelect"
    />
    <template #footer>
      <FSpace justify="start">
        <FButton type="primary" :loading="editLoading" @click="handleOk">
          {{ $t('_.确认') }}
        </FButton>
        <FButton @click="handleCancel">
          {{ $t('_.取消') }}
        </FButton>
      </FSpace>
    </template>
  </FDrawer>
</template>

<script lang="ts" setup name="batchEditSignal">
import { useI18n } from 'vue-i18n';

import { ref, computed, watch, h } from 'vue';
import { FForm, FFormItem, FMessage, FModal } from '@fesjs/fes-design';
import type { BaseType, UiItemType, ViewItem } from '../types/index';
import api from '../api';
import TagEdit from './tagEdit.vue';
import TemplateEidt from './templateEidt.vue';
import { cloneDeep } from 'lodash-es';

const { t: $t } = useI18n();

const valueSemicolonValidReg =
  /^(\s*(check\.object|source\.type)\.\w+?=[^;\s]+?[ \t]*?;)+$/; // 校验分号分割
const valueWordwrapValidReg =
  /^(\s*(check\.object|source\.type)\.\w+?=[^;\s]+?[ \t]*?\n+)+$/; // 校验换行符分割

const emits = defineEmits<{
  (e: 'update:show', val: boolean): void;
  (e: 'success'): void;
  (e: 'cancel', val: 'fullfield' | 'prepare' | 'pendding'): void;
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

const getJobDescDivide = (
  value: string
): { valid: boolean; divide: string } => {
  let valid = false;
  let divide = '';
  if (
    valueSemicolonValidReg.test(value) ||
    valueSemicolonValidReg.test(value + ';')
  ) {
    valid = true;
    divide = ';';
  } else if (
    valueWordwrapValidReg.test(value) ||
    valueWordwrapValidReg.test(value + '\n')
  ) {
    valid = true;
    divide = '\n';
  }

  return { valid, divide };
};

// 自定义函数
const validatorMap = {
  // 判断节点名称不能和工作流名称一样
  validatorTitle: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    if (
      currentNode.value.orchestratorName &&
      value === `${currentNode.value.orchestratorName}`
    ) {
      callback(new Error(rule.message));
    }
  },
  // 判断输入是否json字符串
  validateJson: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    const isJsonString = (str: string) => {
      try {
        JSON.parse(str);
        return true;
      } catch (err) {
        return false;
      }
    };
    if (isJsonString(value)) {
      callback();
    } else {
      callback(new Error($t('_.请填写正确的json格式!')));
    }
  },
  // 判断job.desc是否符合规则
  validateJobDesc: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    let errMsg = '';
    const trimVal = (value || '').trim();
    if (trimVal) {
      const matches = trimVal.split(/[\n;]+\s*/);
      matches.some((item) => {
        const trimItem = item.trim(); // 去除首尾的空格
        if (!/^(check\.object|source\.type)\.[^\=]+?.*?$/.test(trimItem)) {
          errMsg = $t(
            '_.每行请以check.object.xx或source.type.xx开头，xx为编号，从01开始'
          );
          return true;
        } else if (/\s+/.test(trimItem)) {
          const temp = trimItem.split(/[\s\=]+/);
          errMsg = `${temp[0]}${$t('_.行内包含空格字符')}`;
          return true;
        }
      });
      if (!errMsg && !getJobDescDivide(trimVal).valid) {
        errMsg = $t('_.请正确填写多源配置');
      }
    }
    if (errMsg) {
      callback(new Error(errMsg));
    }
  },
  // 判断job.desc是否重复
  validateJobDescDuplication: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    let tmp: BaseType = {};
    let duplicationKey = '';
    const trimVal = (value || '').trim();
    if (trimVal) {
      const matches = trimVal.split(/[\n;]+\s*/);
      matches.some((it) => {
        const key = it.trim().split('=')[0];
        if (tmp[key]) {
          duplicationKey = key;
          return true;
        } else {
          tmp[key] = 1;
        }
      });
    }
    if (duplicationKey) {
      callback(new Error(`${duplicationKey}${$t('_.重复,请检查')}`));
    }
  },
  // 校验数字类型的填写
  numInterval: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    const reg = /^[0-9]+$/;
    const valueResult =
      reg.test(value) &&
      rule.range[0] <= Number(value) &&
      rule.range[1] >= Number(value);
    if (valueResult || value === '') {
      callback();
    } else {
      callback(new Error());
    }
  },
  numIntervalDot: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    const reg = /^[0-9]+(\.[0-9]{1,2})?$/;
    const valueResult =
      reg.test(value) &&
      rule.range[0] <= Number(value) &&
      rule.range[1] >= Number(value);
    if (valueResult || value === '') {
      callback();
    } else {
      callback(new Error());
    }
  },
  // check.object校验
  validateCheckObject: (
    rule: any,
    value: string,
    callback: (val?: unknown) => void
  ) => {
    let errMsg = '';
    const trimVal = (value || '').trim();
    if (trimVal) {
      if (trimVal.includes('check.object')) {
        errMsg = $t('_.输入参数不能包含check.object字符串');
      } else if (trimVal.match(/\s+/g)) {
        errMsg = $t('_.输入参数中间不能包含空格字符');
      }
    }
    if (errMsg) {
      callback(new Error(errMsg));
    }
  },
};

const paramsValid = (param: BaseType) => {
  let temRule: BaseType[] = [];
  if (param.nodeUiValidateVOS) {
    param.nodeUiValidateVOS.map((item: BaseType) => {
      if (item.validateType === 'Required' && props.config.mode === 'item') {
        temRule.push({
          required: true,
          message: item.message,
          trigger: item.trigger,
          type: ['MultiSelect'].includes(param.uiType) ? 'array' : 'string',
        });
      } else if (item.validateType === 'Regex') {
        temRule.push({
          type: 'string',
          pattern: new RegExp(item.validateRange),
          message: item.message,
          trigger: item.trigger,
        });
      } else if (item.validateType === 'Function') {
        temRule.push({
          validator:
            validatorMap[item.validateRange as keyof typeof validatorMap] ||
            ((rule: any, value: string, callback: (val?: unknown) => void) => {
              callback();
            }),
          message: [
            'validateJobDesc',
            'validateCheckObject',
            'validateJobDescDuplication',
          ].includes(item.validateRange)
            ? undefined
            : item.message,
          trigger: item.trigger || 'blur',
        });
      } else if (item.validateType === 'NumInterval') {
        if (param.key === 'max.check.hours') {
          temRule.push({
            validator: validatorMap['numIntervalDot'],
            range: JSON.parse(item.validateRange),
            trigger: 'change',
            message: item.message,
          });
        } else {
          temRule.push({
            validator: validatorMap['numInterval'],
            range: JSON.parse(item.validateRange),
            trigger: 'change',
            message: item.message,
          });
        }
      }
    });
  }
  return temRule;
};

const filterFormType = (val: string) => {
  switch (val) {
    case 'Text':
      return 'textarea';
    default:
      return 'text';
  }
};

//降点装换成短横杠
const poinToLink = (key: string) => {
  return key.replace(/\./g, '-');
};

const showModal = computed({
  get() {
    return !!props.show;
  },
  set(val) {
    emits('update:show', val);
  },
});

const formData = ref<BaseType>({
  ecConfTemplateName: '',
  ecConfTemplateId: '',
});
const templateList = ref<BaseType[]>([]);
const isTemplateDrawerShow = ref(false);
// 获取当前节点对应的模板信息
const getTemplateDataByProject = async () => {
  const params = {
    projectId: props.form.row.projectId,
    orchestratorId: props.form.row.orchestratorId,
    jobType: props.form.row.nodeType,
  };
  const res = await api.GetNodeTemplateListApi(params);
  templateList.value = [];
  (res.data.templates || []).forEach((e: BaseType) => {
    e.child.forEach((d: BaseType) => {
      templateList.value.push(Object.assign(d));
    });
  });
};

// 引用模板选择是
const handleRefTemplateChange = async (v: string, templateId?: string) => {
  if (v === '1' && !formData.value['ecConfTemplateName']) {
    await getTemplateDataByProject();
    templateList.value.forEach((v) => {
      if ((v.workflowDefault && !templateId) || v.templateId === templateId) {
        formData.value['ecConfTemplateName'] = v.templateName;
        formData.value['ecConfTemplateId'] = v.templateId;
      }
    });
  } else if (v === '0') {
    formData.value['ecConfTemplateName'] = '';
    formData.value['ecConfTemplateId'] = '';
  }
};

const openTemplateDrawer = async () => {
  await getTemplateDataByProject();
  isTemplateDrawerShow.value = true;
};

// 选择参数模板
const handleTemplateSelect = (templateObj: BaseType) => {
  formData.value['ecConfTemplateName'] = templateObj.templateName || '';
  formData.value['ecConfTemplateId'] = templateObj.templateId || '';
};

const baseParamKeys = ['appTag', 'businessTag', 'title', 'desc'];
const isRefTemplate = ref('0');
const currentNode = ref<BaseType>({});
const nodeList = ref<BaseType[]>([]);
const baseParamsList = ref<UiItemType[]>([]);
const nodeParamsList = ref<UiItemType[]>([]);
const allParamsList = ref<UiItemType[]>([]);

const initData = () => {
  isRefTemplate.value = '0';
  nodeList.value = [];
  currentNode.value = {};
  formData.value = {
    ecConfTemplateName: '',
    ecConfTemplateId: '',
  };
  baseParamsList.value = [];
  nodeParamsList.value = [];
  allParamsList.value = [];
};

const editTitle = ref($t('_.编辑'));
const initForm = () => {
  const { mode, uiList } = props.config;
  let nodeUi = cloneDeep(uiList);
  if (mode === 'batch') {
    editTitle.value = $t('_.批量编辑');
    nodeList.value = props.form.rows.map((node: BaseType) => ({
      value: node.nodeId,
      name: node.nodeName,
      ...node,
    }));
    nodeUi = nodeUi.filter(
      (ui: BaseType) => ui.uiType !== 'Disable' && !['title'].includes(ui.key)
    );
  } else {
    editTitle.value = $t('_.编辑');
    currentNode.value = cloneDeep(props.form.row);
  }
  const nodeContent = currentNode.value?.nodeContent || {};
  nodeUi.forEach((ui: BaseType) => {
    ui.editable = false;
    formData.value[ui.key] = '';
    // 非批量编辑初始赋值，批量编辑则不用
    if (mode === 'item') {
      const uiItemValue = nodeContent[ui.key] || ui.defaultValue || '';
      formData.value[ui.key] = ['appTag', 'businessTag'].includes(ui.key)
        ? uiItemValue.split(',').filter(Boolean)
        : uiItemValue;
    }
  });
  if (nodeContent['ec.conf.templateId']) {
    isRefTemplate.value = '1';
    handleRefTemplateChange('1', nodeContent['ec.conf.templateId']);
  }
  baseParamsList.value = nodeUi.filter((item: BaseType) => {
    const fields = baseParamKeys.indexOf(item.key) > -1;
    return item.baseInfo && fields;
  });
  nodeParamsList.value = nodeUi.filter(
    (item: BaseType) =>
      !item.baseInfo &&
      item.nodeMenuType &&
      ['runtime', 'startup', 'special'].includes(item.position)
  );
  allParamsList.value = [...baseParamsList.value, ...nodeParamsList.value];
};

watch(
  () => showModal.value,
  (show) => {
    if (show) {
      initData();
      initForm();
    }
  },
  { immediate: true }
);

const handleCheckBoxChange = (item: BaseType) => {
  if (!item.editable) {
    formData.value[item.key] = ['appTag', 'businessTag'].includes(item.key)
      ? []
      : '';
  }
};

const checkShow = (item: BaseType) => {
  const data = formData.value;
  let show = true;
  if (typeof item.condition === 'string' && item.condition) {
    let condition = item.condition;
    // condition 示例：
    // ${params.configuration.runtime['only.receive.today']}
    // !${params.configuration.startup['ec.conf.templateId']}
    if (condition.indexOf('params.configuration') > 0) {
      condition = condition
        .replace(/params\.configuration\.runtime/g, '')
        .replace(/params\.configuration\.startup/g, '')
        .replace(/params\.configuration\.special/g, '');
    }
    let Fn = Function;
    let fn = condition.replace(/\${([^}]+)}/g, function (a: string, b: string) {
      return `__node__data${b}`;
    });
    try {
      show = new Fn('__node__data', 'return ' + fn)(data);
    } catch (e) {
      console.log(e);
    }
  }
  if (typeof item.condition === 'boolean') {
    show = item.condition;
  }
  // 为false则批量编辑里为空
  if (!show && props.config.mode === 'batch') {
    item.editable = false;
    handleCheckBoxChange(item);
  }
  return show;
};

// 批量编辑时将以前值整合到一起
const formatValueToForm = (original?: BaseType) => {
  const result: BaseType = { configuration: {} };
  props.config.uiList.forEach((ui: BaseType) => {
    let fieldValue;
    const curUi = allParamsList.value.find((u) => u.key === ui.key);
    if (props.config.mode === 'batch' && !curUi?.editable) {
      fieldValue = (original?.nodeContent || {})[ui.key] || '';
      if (
        !fieldValue &&
        (ui.uiType === 'Disable' || !checkShow(curUi as BaseType)) &&
        ui.defaultValue
      ) {
        fieldValue = ui.defaultValue;
      }
    } else {
      fieldValue = formData.value[ui.key];
      if (!fieldValue && ui.defaultValue) {
        fieldValue = ui.defaultValue;
      }
    }
    // 标签转化为字符串
    if (fieldValue && Array.isArray(fieldValue)) {
      fieldValue = fieldValue.join(',');
    }
    // 去除字符串前后空格
    if (fieldValue && typeof fieldValue === 'string') {
      fieldValue = fieldValue.trim();
    }
    // 针对job.desc去除每行首空格
    if (ui.key === 'job.desc' && fieldValue) {
      const { divide } = getJobDescDivide(fieldValue);
      fieldValue = fieldValue
        .split(/[\n;]+\s*/)
        .map((item: string) => item.trim())
        .join(divide);
    }
    if (baseParamKeys.includes(ui.key) && ui.baseInfo) {
      result[ui.key] = fieldValue;
    } else if (['runtime', 'startup', 'special'].includes(ui.position)) {
      if (!result['configuration'][ui.position]) {
        result['configuration'][ui.position] = {};
      }
      result['configuration'][ui.position][ui.key] = fieldValue;
    }
  });
  const { configuration, ...baseParams } = result;
  if (!configuration['startup']) {
    configuration['startup'] = {};
  }
  if (props.config.mode === 'batch') {
    baseParams['ecConfTemplateName'] =
      (original?.nodeContent || {})['ecConfTemplateName'] || '';
    baseParams['ecConfTemplateId'] =
      (original?.nodeContent || {})['ecConfTemplateId'] || '';
    configuration['startup']['ec.conf.templateId'] =
      (original?.nodeContent || {})['ecConfTemplateId'] || '';
  } else {
    baseParams['ecConfTemplateName'] =
      formData.value['ecConfTemplateName'] || '';
    baseParams['ecConfTemplateId'] = formData.value['ecConfTemplateId'] || '';
    configuration['startup']['ec.conf.templateId'] =
      formData.value['ecConfTemplateId'] || '';
  }
  return { ...baseParams, params: JSON.stringify({ configuration }) };
};

const buildParams = () => {
  const result: BaseType[] = [];
  if (props.config.mode === 'batch') {
    nodeList.value.forEach((node: BaseType) => {
      const param = {
        orchestratorId: node.orchestratorId,
        nodeKey: node.nodeKey,
        id: node.contentId,
        ...formatValueToForm(node),
      };
      result.push(param);
    });
  } else {
    result.push({
      orchestratorId: props.form.row.orchestratorId,
      nodeKey: props.form.row.nodeKey,
      id: props.form.row.contentId,
      ...formatValueToForm(),
    });
  }
  return result;
};

const editLoading = ref(false);
const baseParamsFormRef = ref<InstanceType<typeof FForm> | null>(null);
const nodeParamsFormRef = ref<InstanceType<typeof FForm> | null>(null);
const handleOk = async () => {
  try {
    await Promise.all([
      baseParamsFormRef.value?.validate(),
      nodeParamsFormRef.value?.validate(),
    ]);
    if (
      props.config.mode === 'batch' &&
      !allParamsList.value.some((item: UiItemType) => item.editable)
    ) {
      FMessage.warning($t('_.请至少选择一个要编辑的数据项'));
      return;
    }
    const param = {
      editNodeList: buildParams(),
    };
    const paramsObj = JSON.parse(param.editNodeList[0].params);
    const hasView = await checkIsView(
      paramsObj.configuration.runtime['check.object'],
      paramsObj.configuration.runtime['job.desc']
    );
    if (hasView) {
      return;
    }
    editLoading.value = true;
    const msg =
      props.config.mode === 'batch' ? $t('_.批量编辑成功') : $t('_.编辑成功');
    await api.SaveSignalNodeApi(param);
    editLoading.value = false;
    FMessage.success(msg);
    showModal.value = false;
    emits('success');
  } catch (error: any) {
    editLoading.value = false;
    console.log('edit error', error);
  }
};

const checkIsView = async (checkObject: string, jobDesc: string) => {
  if (!checkObject && !jobDesc) {
    return false;
  }
  let hasView: ViewItem[] = [];
  try {
    const { data: res } = await api.CheckIsViewApi({
      checkObject,
      jobDesc,
    });
    hasView = res && res.result && res.result.filter((it: ViewItem) => it.view);
    if (hasView.length) {
      let strs = (jobDesc || '').split('\n');
      const tbs = [checkObject, ...strs].filter((str) => {
        return hasView.some((it) =>
          new RegExp(`${it.db}\\.${it.table}\\b`).test(str)
        );
      });
      FModal.info({
        title: $t('_.提示'),
        showCancel: false,
        showOk: false,
        content: () => {
          return h(
            'div',
            {
              style: {
                width: '400px',
                'tex-align': 'left',
                'word-break': 'break-all',
                'max-height': '470px',
                'overflow-y': 'auto',
              },
            },
            [
              $t('_.check.objeck或job.desc存在视图表:'),
              tbs.map((it) => h('p', { key: it }, it)),
              $t('_.请先删除视图表！'),
            ]
          );
        },
        closable: true,
      });
    }
  } catch (error) {
    console.error(error);
    //
  }
  return hasView.length > 0;
};

const handleCancel = () => {
  showModal.value = false;
  if (props.config.mode === 'batch') {
    emits('cancel', 'prepare');
  }
};
</script>

<style lang="less" scoped>
.item-content-hide {
  :deep(.fes-form-item-content) {
    display: none;
  }
}
</style>
<style lang="less">
.form-item-popper {
  .fes-popper-arrow {
    left: 5px !important;
  }
}
</style>
