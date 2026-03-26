<template>
  <div>
    <FDrawer
      v-model:show="drawerShow"
      :title="config.type === 'edit' ? $t('_.编辑') : $t('_.批量编辑')"
      :mask-closable="false"
      display-directive="if"
      dimension="50%"
      :footer="true"
      content-class="operate-template"
      @cancel="closeDrawer"
    >
      <div>
        <!-- {{ curNodeBaseParamsList }} -->
        <FForm
          ref="baseInfoFormRef"
          label-position="top"
          :model="currentNode"
          class="node-parameter-bar"
        >
          <FFormItem
            v-if="config.type === 'batchEdit'"
            :label="$t('_.已选节点')"
            style="margin-bottom: 6px"
          >
            <FTag
              v-for="node in currentBatchNodeNameList"
              :key="node.nodeId"
              type="info"
              class="node-tag"
              :closable="currentBatchNodeNameList.length > 1"
              @close="handleCloseNodeTag(node.nodeId)"
            >
              <FEllipsis style="max-width: 100%">
                {{ node.nodeName }}
              </FEllipsis>
            </FTag>
          </FFormItem>
          <FFormItem v-if="config.type === 'edit'" :label="$t('_.节点ID')">
            <FInput v-if="currentNode" v-model="currentNode.nodeId" disabled />
          </FFormItem>
          <!-- {{ curNodeBaseParamsList }} -->
          <template v-for="item in curNodeBaseParamsList">
            <div
              v-if="config.type === 'batchEdit' && item.key !== 'title'"
              :key="item.key"
              :class="
                batchShowFormItemData[item.key]
                  ? 'batch-label-selected'
                  : 'batch-label'
              "
            >
              <FCheckbox
                :key="item.key"
                :value="true"
                @change="handleBatchShowFormItemDataChange($event, item.key)"
              >
                {{ $t('_.批量编辑') }}{{ item.lableName }}
              </FCheckbox>
            </div>
            <template
              v-if="config.type == 'edit' || batchShowFormItemData[item.key]"
            >
              <!-- {{ paramsValid(item) }} -->
              <FFormItem
                v-if="['Input', 'Text', 'Disable'].includes(item.uiType)"
                :key="poinToLink(item.key)"
                :rules="paramsValid(item)"
                :label="config.type === 'edit' ? item.lableName : ''"
                :prop="item.key"
              >
                <FInput
                  v-model="currentNode[item.key]"
                  :type="filterFormType(item.uiType)"
                  :rows="6"
                  :placeholder="item.desc"
                  :disabled="item.uiType === 'Disable'"
                />
              </FFormItem>
              <FFormItem
                v-if="item.uiType === 'Tag'"
                :key="poinToLink(item.key)"
                :label="config.type === 'edit' ? item.lableName : ''"
                :prop="item.key"
              >
                <BTagsPanel v-model:tags="currentNode[item.key]" />
              </FFormItem>
              <FFormItem
                v-if="item.uiType === 'Select' || item.uiType === 'MultiSelect'"
                :key="poinToLink(item.key)"
                :rules="paramsValid(item)"
                :label="config.type === 'edit' ? item.lableName : ''"
                :prop="item.key"
              >
                <FSelect
                  v-model="currentNode[item.key]"
                  :placeholder="item.desc"
                  clearable
                  :multiple="item.uiType === 'MultiSelect'"
                >
                  <template v-if="item.value.indexOf('/api/rest_j/') >= 0">
                    <FOption
                      v-for="subItem in dynamicData[item.key]"
                      :key="subItem.name"
                      :value="subItem.name"
                    >
                      {{ subItem.name }}
                    </FOption>
                  </template>
                  <template v-else>
                    <FOption
                      v-for="subItem in JSON.parse(item.value)"
                      :key="subItem"
                      :value="subItem"
                    >
                      {{ subItem }}
                    </FOption>
                  </template>
                </FSelect>
              </FFormItem>
              <FFormItem
                v-if="item.uiType === 'MultiBinding'"
                :key="poinToLink(item.key)"
                :rules="paramsValid(item)"
                :label="config.type === 'edit' ? item.lableName : ''"
                :prop="item.key"
              >
                <FSelect
                  v-model="currentNode[item.key]"
                  :placeholder="item.desc"
                  clearable
                  multiple
                >
                  <FOption
                    v-for="(subItem, index) in conditionBindList(item)"
                    :key="index"
                    :value="subItem.key"
                  >
                    {{ subItem.name }}
                  </FOption>
                </FSelect>
              </FFormItem>
            </template>
          </template>
        </FForm>
        <!-- {{ curNodeParamsList }} -->
        <div
          v-if="curNodeParamsList.length > 0"
          class="node-module-param-modal-header"
        >
          <FForm
            v-if="curNodeParamsList.length > 0"
            ref="parameterFormRef"
            label-position="top"
            class="node-parameter-bar"
            :model="currentNode"
            :rules="ruleValidate"
          >
            <div
              v-if="
                config.type === 'batchEdit' && config?.nodeTypeName !== 'aisql'
              "
              key="ecConfTemplateName"
              :class="
                batchShowFormItemData['ecConfTemplateName']
                  ? 'batch-label-selected'
                  : 'batch-label'
              "
            >
              <FCheckbox
                key="ecConfTemplateName"
                :value="true"
                @change="
                  handleBatchShowFormItemDataChange(
                    $event,
                    'ecConfTemplateName'
                  )
                "
              >
                {{ $t('_.批量编辑引用资源参数模板') }}
              </FCheckbox>
            </div>
            <template
              v-if="
                (config.type == 'edit' ||
                  batchShowFormItemData['ecConfTemplateName']) &&
                config?.nodeTypeName !== 'aisql'
              "
            >
              <FFormItem :label="$t('_.是否引用资源参数模板')">
                <FSelect
                  v-model="isRefTemplate"
                  @change="handleRefTemplateChange"
                >
                  <FOption value="1">
                    {{ $t('_.是') }}
                  </FOption>
                  <FOption value="0">
                    {{ $t('_.否') }}
                  </FOption>
                </FSelect>
              </FFormItem>
              <template v-if="isRefTemplate === '1'">
                <FFormItem
                  ref="paramsTemplateNameRef"
                  :label="$t('_.资源参数模板名称')"
                  prop="ecConfTemplateName"
                >
                  <FInput
                    v-model="currentNode.ecConfTemplateName"
                    :placeholder="$t('_.请选择资源参数模板名称')"
                    readonly
                    @focus="openTemplateDrawer"
                  />
                </FFormItem>
              </template>
            </template>
            <template v-for="item in curNodeParamsList">
              <div
                v-if="config.type !== 'edit' && checkShow(item)"
                :key="item.key"
                :class="
                  batchShowFormItemData[item.key]
                    ? 'batch-label-selected'
                    : 'batch-label'
                "
              >
                <FCheckbox
                  :key="item.key"
                  :value="true"
                  :disabled="
                    item.key === 'sparkVersion' && isSparkVersionDisabled
                  "
                  @change="handleBatchShowFormItemDataChange($event, item.key)"
                >
                  {{ $t('_.批量编辑') }}{{ item.lableName }}
                </FCheckbox>
              </div>
              <template
                v-if="
                  checkShow(item) &&
                  ['runtime', 'startup', 'special'].includes(item.position) &&
                  (config.type == 'edit' || batchShowFormItemData[item.key])
                "
              >
                <FFormItem
                  v-if="['Input', 'Text', 'Disable'].includes(item.uiType)"
                  :key="poinToLink(item.key)"
                  :rules="paramsValid(item)"
                  :label="config.type === 'edit' ? item.lableName : ''"
                  :prop="'jobParams.' + poinToLink(item.key)"
                >
                  <FInput
                    v-model="currentNode.jobParams[poinToLink(item.key)]"
                    :type="filterFormType(item.uiType)"
                    :rows="6"
                    :placeholder="item.desc"
                    :disabled="item.uiType === 'Disable'"
                  />
                </FFormItem>
                <FFormItem
                  v-if="
                    item.uiType === 'Select' || item.uiType === 'MultiSelect'
                  "
                  :key="poinToLink(item.key)"
                  :rules="paramsValid(item)"
                  :label="config.type === 'edit' ? item.lableName : ''"
                  :prop="'jobParams.' + poinToLink(item.key)"
                >
                  <FSelect
                    v-model="currentNode.jobParams[poinToLink(item.key)]"
                    :placeholder="item.desc"
                    clearable
                    :multiple="item.uiType === 'MultiSelect'"
                    :disabled="
                      item.key === 'sparkVersion' && isSparkVersionDisabled
                    "
                    @change="handleSelectChange(item)"
                  >
                    <template v-if="item.value.indexOf('/api/rest_j/') >= 0">
                      <FOption
                        v-for="subItem in dynamicData[item.key]"
                        :key="subItem.name"
                        :value="subItem.name"
                      >
                        {{ subItem.name }}
                      </FOption>
                    </template>
                    <template v-else>
                      <FOption
                        v-for="subItem in JSON.parse(item.value)"
                        :key="subItem"
                        :value="subItem"
                      >
                        {{ subItem }}
                      </FOption>
                    </template>
                  </FSelect>
                </FFormItem>
                <FFormItem
                  v-if="item.uiType === 'MultiBinding'"
                  :key="poinToLink(item.key)"
                  :rules="paramsValid(item)"
                  :label="config.type === 'edit' ? item.lableName : ''"
                  :prop="'jobParams.' + poinToLink(item.key)"
                >
                  <FSelect
                    v-model="currentNode.jobParams[poinToLink(item.key)]"
                    :placeholder="item.desc"
                    clearable
                    multiple
                  >
                    <FOption
                      v-for="(subItem, index) in conditionBindList(item)"
                      :key="index"
                      :value="subItem.key"
                    >
                      {{ subItem.name }}
                    </FOption>
                  </FSelect>
                </FFormItem>
              </template>
              <!-- 无资源上传 -->
              <!-- <template v-if="checkShow(item) && item.uiType === 'Upload'">
              <FFormItem
                :key="poinToLink(item.key)"
                :rules="paramsValid(item)"
                :label="config.type === 'edit' ? item.lableName : ''"
              >
                资源上传 
                <resource
                  :resources="resources"
                  :is-ripetition="true"
                  :node-type="currentNode.nodeType"
                  :readonly="false"
                  :project-name="currentNode.projectName"
                  @update-resources="updateResources"
                />
              </FFormItem>
            </template> -->
            </template>
          </FForm>
        </div>
      </div>
      <template #footer>
        <FSpace justify="end">
          <FButton @click="closeDrawer">
            {{ $t('_.取消') }}
          </FButton>
          <FButton type="primary" :loading="editLoading" @click="handleOk">
            {{ $t('_.确认') }}
          </FButton>
        </FSpace>
      </template>
    </FDrawer>
    <templateSelectDrawer
      v-if="isTemplateDrawerShow"
      ref="templateSelectDrawer"
      v-model:show="isTemplateDrawerShow"
      :templates="templateList"
      :default-template-id="currentNode.ecConfTemplateId"
      @success="handleTemplateSelect"
    />
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import {
  FDrawer,
  FMessage,
  FInput,
  FSelect,
  FForm,
  FCheckboxGroup,
  FCheckbox,
  FTag,
  FEllipsis,
  FModal,
} from '@fesjs/fes-design';
import { ref, computed, defineProps, defineEmits, onMounted, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash-es';
import { useDataList } from '../hooks/useDataList';
import { BTagsPanel } from '@fesjs/traction-widget';
import type { DataType } from '../types/index';
import { request } from '@dataspherestudio/shared';
import api from '../api';
import resource from './resource.vue';
import templateSelectDrawer from './templateSelectDrawer.vue';

const { t: $t } = useI18n();

const {
  fetchNodeTypeDetail,
  getCurrentDsslabels,
  nodeTypeDetailList,
  isWhite,
} = useDataList();
const dynamicData = ref({});
const props = defineProps({
  mode: {
    type: String,
    required: false,
    default: 'edit',
  },
  show: {
    type: Boolean,
    required: true,
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
  nodeParentType: {
    type: String,
    required: true,
  },
});
const emits = defineEmits<{
  (e: 'update:show'): void;
  (e: 'success'): void;
  (e: 'initBatch'): void;
}>();
const drawerShow = computed({
  get: () => props.show,
  set: (value) => {
    emits('update:show', value);
  },
});
const editLoading = ref(false);

// 后面封装到组件
interface Resource {
  fileName: string;
  resourceId: string;
  version: string;
}

interface Template {
  id: string;
  name: string;
}

interface Node {
  params?: {
    configuration?: {
      special: Record<string, string>;
      runtime: Record<string, string>;
      startup: Record<string, string>;
    };
  };
  resources?: Resource[];
  jobParams?: Record<string, string>;
  ecConfTemplateId?: string;
  nodeUiVOS?: NodeUiVO[];
  jobContent?: {
    script: string;
  };
}

interface NodeUiVO {
  baseInfo?: boolean;
  key: string;
  nodeMenuType?: boolean;
  uiType?: string;
  defaultValue?: string;
}

interface Node {
  key: string;
  title: string;
  type: string;
}

interface ConditionItem {
  name: string;
  key: string;
}

interface ValidationResult {
  valid: boolean;
  type: string;
}

interface JobDescDuplicationResult {
  matches: string[];
  divide: string;
}

interface conditionBindListParam {
  defaultValue: string;
  value: string;
  key: string;
}

interface jobDescVaidContext {
  $t: (key: string) => string;
  nodes: Node[];
  currentNode: Node;
  valueSemicolonValidReg: RegExp;
  valueWordwrapValidReg: RegExp;
}

interface Rule {
  required?: boolean;
  message: string;
  trigger: string;
  type?: 'string' | 'array';
  pattern?: RegExp;
  validator?: (
    rule: any,
    value: any,
    callback: (error?: Error) => void
  ) => void;
  range?: number[];
}

interface NodeUiValidateVO {
  validateType: string;
  message: string;
  trigger: string;
  validateRange?: string;
}

interface Param {
  nodeUiValidateVOS?: NodeUiValidateVO[];
  uiType?: string;
  name?: string;
  jobDescVaid?: (value: string) => { valid: boolean };
  getJobDescDuplication?: (value: string) => { matches: string[] };
}
interface NodeParams {
  position: string;
  key: string;
  defaultValue: string;
}

interface Configuration {
  [position: string]: { [key: string]: string };
}

interface JobParams {
  [key: string]: string;
}

interface JobContent {
  script: string;
}

interface ResourcesItem {
  fileName: string;
}

interface Node {
  jobParams?: JobParams;
  params: {
    configuration: Configuration;
  };
  jobContent?: JobContent;
  resources: ResourcesItem[];
}

interface Context {
  currentNode: Node;
  curNodeParamsList: NodeParams[];
  resources: ResourcesItem[];
  $refs: {
    baseInfoForm: any;
    parameterForm: any;
  };
  $emit: (eventName: string, data?: any) => void;
  $Message: {
    warning: (message: string) => void;
  };
  $t: (key: string) => string;
  poinToLink: (key: string) => string;
}
const currentBatchNodeNameList = ref([]);
const batchShowFormItemData = ref({});
const baseInfoFormRef = ref(null);
const parameterFormRef = ref(null);
const paramsTemplateNameRef = ref(null);

const consoleParams = ref<DataType>([]);
const currentNode = ref<Node>({});
const resources = ref<Resource[]>([]);
const isRefTemplate = ref('0');
const ruleValidate = ref({
  ecConfTemplateName: [
    { required: true, message: $t('_.请选择参数模板'), trigger: 'change' },
  ],
});
const valueSemicolonValidReg = ref(
  /^([ \t]*(check\.object|source\.type)\.\w+?=[^;\s]+?;)+$/
);
const valueWordwrapValidReg = ref(
  /^([ \t]*(check\.object|source\.type)\.\w+?=[^;\s]+?\n+)+$/
);
const curNodeTypeDetail = ref({});
const curNodeParamsList = ref([]);
const curNodeBaseParamsList = ref([]);

// 判断 sparkVersion 是否应该被禁用
const isSparkVersionDisabled = computed(() => {
  console.log('isWhite.value', isWhite.value);
  return isWhite.value === false;
});

const getOpions = async (item) => {
  const res = await request.fetch(
    item.value,
    {},
    {
      method: 'get',
      cacheOptions: { time: 3000 },
      baseURL: '',
    }
  );
  const data = res.data.dssWorkspaceStarRocksCluster.map((it) => {
    it.name = it.clusterName;
    return it;
  });
  let defaultData = res.data.dssWorkspaceStarRocksCluster.find(
    (it) => it.defaultCluster
  );
  dynamicData.value = {
    ...dynamicData.value,
    [item.key]: data,
  };
  if (currentNode.value.jobParams && !currentNode.value.jobParams[item.key]) {
    currentNode.value.jobParams[item.key] = defaultData.clusterName;
    currentNode.value.params.configuration[item.position][item.key] =
      defaultData.clusterName;
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.type'
    ] = 'starrocks';
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.params.host'
    ] = defaultData.clusterIp;
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.params.port'
    ] = defaultData.httpPort;
    currentNode.value.jobParams['linkis-datasource-type'] = 'starrocks';
    currentNode.value.jobParams['linkis-datasource-params-host'] =
      defaultData.clusterIp;
    currentNode.value.jobParams['linkis-datasource-params-port'] =
      defaultData.httpPort;
  }
};
const handleSelectChange = (item) => {
  if (item.key === 'executeCluster') {
    let data = dynamicData.value[item.key].find(
      (it) => it.clusterName == currentNode.value.jobParams[item.key]
    );
    currentNode.value.params.configuration[item.position][item.key] =
      data.clusterName;
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.type'
    ] = 'starrocks';
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.params.host'
    ] = data.clusterIp;
    currentNode.value.params.configuration[item.position][
      'linkis.datasource.params.port'
    ] = data.httpPort;
    currentNode.value.jobParams['linkis-datasource-type'] = 'starrocks';
    currentNode.value.jobParams['linkis-datasource-params-host'] =
      data.clusterIp;
    currentNode.value.jobParams['linkis-datasource-params-port'] =
      data.httpPort;
  }
  // sparkVersion 警告提示
  // if (item.key === 'sparkVersion') {
  //   const currentValue = currentNode.value.jobParams[poinToLink(item.key)];
  //   if (currentValue === '2') {
  //     FModal.warning({
  //       title: $t('_.提示'),
  //       content: $t('accounts.spark2Notice'),
  //       closable: true,
  //       width: 500,
  //       okText: $t('_.确定'),
  //     });
  //   }
  // }
};
const getCurNodeParamsList = () => {
  if (curNodeTypeDetail.value && curNodeTypeDetail.value.nodeUiVOS) {
    const arr = curNodeTypeDetail.value.nodeUiVOS
      ? curNodeTypeDetail.value.nodeUiVOS.filter(
          (item) =>
            !item.baseInfo && item.nodeMenuType && item.key !== 'resources'
        )
      : [];
    arr.forEach((item) => {
      if (
        item.key === 'executeCluster' &&
        item.value.indexOf('/api/rest_j/') >= 0
      ) {
        getOpions(item);
      }
    });
    return arr;
  }
  return [];
};
const getCurNodeBaseParamsList = () => {
  if (curNodeTypeDetail.value && curNodeTypeDetail.value.nodeUiVOS) {
    const arr = curNodeTypeDetail.value.nodeUiVOS.filter((item) => {
      const fields = ['appTag', 'businessTag', 'title', 'desc'];
      return item.baseInfo && fields.includes(item.key);
    });
    return arr;
  }
  return [];
};

const updateCurrentNode = () => {
  if (
    !currentNode.value.params ||
    isEmpty(currentNode.value.params.configuration)
  ) {
    currentNode.value.params = {
      configuration: {
        special: {},
        runtime: {},
        startup: {},
      },
    };
  }
  resources.value = [];

  let jobParams: Record<string, string> = {};
  // console.log('curNodeParamsList', curNodeParamsList.value);
  curNodeParamsList.value.forEach((item) => {
    const defaultValue = ['MultiBinding'].includes(item.uiType)
      ? JSON.parse(item.defaultValue)
      : consoleParamsDefault(
          item.defaultValue,
          item.key,
          currentNode.value.consoleParams
        );
    // console.log('defaultValue', item.key, defaultValue)
    if (
      ['runtime', 'startup', 'special'].includes(item.position) &&
      currentNode.value.params
    ) {
      const value = currentNode.value.params.configuration[item.position][
        item.key
      ]
        ? currentNode.value.params.configuration[item.position][item.key]
        : defaultValue;
      jobParams[poinToLink(item.key)] = value;
    } else if (item.uiType === 'Upload') {
      if (currentNode.value.resources && currentNode.value.resources.length) {
        resources.value = [...currentNode.value.resources];
        if (
          currentNode.value.jobContent &&
          currentNode.value.jobContent.script
        ) {
          resources.value = currentNode.value.resources.filter((item) => {
            return item.fileName !== currentNode.value.jobContent.script;
          });
        }
      } else {
        resources.value = [];
      }
    }
  });
  currentNode.value.ecConfTemplateId = currentNode.value.templateId;
  currentNode.value.ecConfTemplateName = currentNode.value.templateName;
  delete currentNode.value.templateId;
  delete currentNode.value.templateName;
  if (currentNode.value.ecConfTemplateId) {
    isRefTemplate.value = '1';
  } else {
    isRefTemplate.value = '0';
  }

  jobParams['ec-conf-templateId'] = currentNode.value.ecConfTemplateId;
  // console.log('jobParams', jobParams);
  currentNode.value.jobParams = jobParams;

  // console.log('updateCurrentNode-currentNode', currentNode.value);
};

const getConsoleParams = async () => {
  try {
    const [sparkRes, commonRes, hiveRes] = await Promise.all([
      request.fetch(
        '/configuration/getFullTreesByAppName',
        {
          engineType: 'spark',
          creator: 'nodeexecution',
        },
        'get'
      ),
      request.fetch(
        '/configuration/getFullTreesByAppName',
        {
          engineType: $t('_.通用设置'),
          creator: $t('_.通用设置'),
        },
        'get'
      ),
      request.fetch(
        '/configuration/getFullTreesByAppName',
        {
          engineType: 'hive',
          creator: 'nodeexecution',
        },
        'get'
      ),
    ]);

    consoleParams.value = [sparkRes.data, commonRes.data, hiveRes.data];
  } catch (error) {
    console.error('Failed to fetch console parameters:', error);
  }
};

const consoleParamsDefault = (originDefalut, key, rst) => {
  let value = originDefalut;
  if (rst.length > 0) {
    rst[0].fullTree.forEach((item) => {
      if (item && item.settings.length > 0) {
        item.settings.forEach((it) => {
          if (it.key === key) {
            value = it.configValue || it.defaultValue;
          }
        });
      }
    });
    // if (rst[1].fullTree[0] && key === 'wds.linkis.rm.yarnqueue') {
    //   value =
    //     rst[1].fullTree[0].settings[0].configValue ||
    //     rst[1].fullTree[0].settings[0].defaultValue;
    // }
    rst[2].fullTree.forEach((item) => {
      if (item && item.settings.length > 0) {
        item.settings.forEach((it) => {
          if (it.key === key) {
            value = it.configValue || it.defaultValue;
          }
        });
      }
    });
  }
  return value;
};
// linkToPoin
const linkToPoin = (key: string) => {
  return key.replace(/-/g, '.');
};
//降点装换成短横杠
const poinToLink = (key: string) => {
  return key.replace(/\./g, '-');
};

const conditionBindList = (
  context: jobDescVaidContext,
  param: conditionBindListParam
): ConditionItem[] => {
  let temArry: ConditionItem[] = [];

  if (param.defaultValue === 'empty') {
    temArry.push({
      name: context.$t('message.workflow.process.notBinding'),
      key: 'empty',
    });
  }

  const conditionResult = (type: string): boolean => {
    if (param.value && JSON.parse(param.value)) {
      const optionsList = JSON.parse(param.value);
      if (optionsList[0] === '*') {
        return true;
      } else {
        return optionsList.includes(type);
      }
    }
    return false;
  };

  if (context.nodes && context.nodes.length) {
    context.nodes.forEach((node) => {
      if (node.key !== context.currentNode.key && conditionResult(node.type)) {
        // 当 sql 节点里面没内容时, resources 属性值为 [], 这种 sql 节点不放做选项
        if (node.resources.length > 0) {
          const tempObj: ConditionItem = {
            name: node.title,
            key: node.key,
          };
          temArry.push(tempObj);
        }
      }
    });
  }

  return temArry;
};

const jobDescVaid = (value: string): ValidationResult => {
  let valid = false;
  let type = '';

  if (
    valueSemicolonValidReg.value.test(value) ||
    valueSemicolonValidReg.value.test(value + ';')
  ) {
    valid = true;
    type = 'semicolon';
  } else if (
    valueWordwrapValidReg.value.test(value) ||
    valueWordwrapValidReg.value.test(value + '\n')
  ) {
    valid = true;
    type = 'wordwrap';
  }

  return { valid, type };
};

const getJobDescDuplication = (value: string): JobDescDuplicationResult => {
  const trimVal = (value || '').trim();
  const result = jobDescVaid(trimVal);
  const type = result.type;
  let divide = type === 'wordwrap' ? /\s/ : ';';

  const matches = trimVal
    .split(divide)
    .map((item) => item.trim())
    .filter((item) => item);

  if (type === 'wordwrap') {
    divide = '\n';
  }

  return { matches, divide };
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
      const matches = trimVal.split(/[\n;]+/);
      matches.some((item) => {
        const trimItem = item.replace(/^\s+/, ''); // 去除开头的空格
        if (!/^(check\.object|source\.type)\.[^\=]+?\=.+?$/.test(trimItem)) {
          errMsg = $t(
            '_.每行请以check.object.xx或source.type.xx开头，xx为编号，从01开始'
          );
          return true;
        } else if (
          trimItem.startsWith('check.object.') &&
          /\s+/.test(trimItem)
        ) {
          errMsg = $t('_.check.object行内包含空格字符');
          return true;
        } else if (
          trimItem.startsWith('source.type.') &&
          /\s+/.test(trimItem)
        ) {
          errMsg = $t('_.source.type行内包含空格字符');
          return true;
        }
      });
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
    let tmp: DataType = {};
    let hasDuplication = false;
    const trimVal = (value || '').trim();
    if (trimVal) {
      const matches = trimVal.split(/[\n;]+/);
      matches.some((it) => {
        const key = it.split('=')[0];
        if (tmp[key]) {
          hasDuplication = true;
          return true;
        } else {
          tmp[key] = 1;
        }
      });
    }
    if (hasDuplication) {
      callback(new Error(rule.message));
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

const paramsValid = (param: DataType) => {
  let temRule: DataType[] = [];
  if (param.nodeUiValidateVOS) {
    param.nodeUiValidateVOS.map((item: DataType) => {
      if (item.validateType === 'Required' && props.config.type === 'edit') {
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
          message: ['validateJobDesc', 'validateCheckObject'].includes(
            item.validateRange
          )
            ? undefined
            : item.message,
          trigger: item.trigger || 'blur',
        });
      } else if (item.validateType === 'NumInterval') {
        temRule.push({
          validator: validatorMap['numInterval'],
          range: JSON.parse(item.validateRange),
          trigger: 'change',
          message: item.message,
        });
      }
    });
  }
  return temRule;
};

const getCurrentNode = () => {
  const param = cloneDeep(currentNode.value);
  if (param.jobParams && param.params.configuration) {
    curNodeParamsList.value.forEach((item) => {
      if (
        ['runtime', 'startup', 'special'].includes(item.position) &&
        param.params
      ) {
        const value =
          param.jobParams[poinToLink(item.key)] ?? item.defaultValue;
        if (item.key && ['job.desc'].includes(item.key)) {
          const { matches, divide } = getJobDescDuplication(value);
          param.params.configuration[item.position][item.key] =
            matches.join(divide);
        }
      }
    });
  }
  return param;
};

// 资源上传
const resourcesAction = () => {
  let resources: ResourcesItem[] = [];
  const mapFlag: { [fileName: string]: number } = {};

  if (currentNode.value.jobContent && currentNode.value.jobContent.script) {
    currentNode.value.resources.forEach((item) => {
      if (
        item.fileName === currentNode.value.jobContent.script &&
        !mapFlag[item.fileName]
      ) {
        resources.push(item);
        mapFlag[item.fileName] = 1;
      }
    });
  }

  resources.forEach((item) => {
    if (!mapFlag[item.fileName]) {
      resources.push(item);
    }
  });

  currentNode.value.resources = resources;
};

const updateResources = (res: Resource[]): void => {
  resources.value = res.map((item) => ({
    fileName: item.fileName,
    resourceId: item.resourceId,
    version: item.version,
  }));
  resourcesAction();
  // const tempNode = getCurrentNode();
  // context.$emit('saveNode', tempNode);
};

const filterFormType = (val: string): string => {
  switch (val) {
    case 'Text':
      return 'textarea';
    default:
      return 'text';
  }
};

/**
 * 检查数据是否符合条件，仅支持 runtime startup 参数配置时控制
 * ${params.configuration.runtime['only.receive.today']}=='true'
 * !${params.configuration.startup['ec.conf.templateId']}
 * @param item
 * @param nodeData
 * @returns
 */
const checkShow = (item: Item, nodeData?: Node): boolean => {
  let data = nodeData || currentNode.value;
  if (
    item &&
    item.condition &&
    typeof item.condition === 'string' &&
    item.condition
  ) {
    let condition = item.condition;

    // condition 示例：
    // ${params.configuration.runtime['only.receive.today']}
    // !${params.configuration.startup['ec.conf.templateId']}
    // 后端配置根据params路径位置写规则
    // 前端编辑参数存在jobParams，需要转换，以上示例转换如下
    // ${jobParams['only-receive-today']}
    // !${jobParams['ec-conf-templateId']}
    if (nodeData) {
      // module.vue click 触发，条件检查使用params.configuration.runtime这种路径
    } else {
      // 参数面板点击触发，条件检查使用jobParams这种路径
      if (condition.indexOf('params.configuration') > 0) {
        condition = condition.replace(
          /params\.configuration\.runtime/g,
          'jobParams'
        );
        condition = condition.replace(
          /params\.configuration\.startup/g,
          'jobParams'
        );
        condition = poinToLink(condition);
      }
    }

    let Fn = Function;
    let fn = condition.replace(/\${([^}]+)}/g, function (a, b) {
      return `__node__data.${b}`;
    });

    try {
      return new Fn('__node__data', `return ${fn}`)(data);
    } catch (e) {
      console.log(e);
      return false; // 在发生错误时返回 false
    }
  }

  if (item && item.condition && typeof item.condition === 'boolean') {
    return item.condition;
  }
  return true;
};

// 参数模板相关
const isTemplateDrawerShow = ref(false);
const templateList = ref<Template[]>([]);
const handleRefTemplateChange = (value: string, templateId?: string) => {
  if (
    value === '1' &&
    !currentNode.value.ecConfTemplateName &&
    props.config.type === 'edit'
  ) {
    getTemplateDataByProject().then(() => {
      templateList.value.forEach((template: { [key: string]: any }) => {
        if (
          (template.workflowDefault && !templateId) ||
          templateId === template.templateId
        ) {
          currentNode.value.ecConfTemplateName = template.templateName;
          currentNode.value.ecConfTemplateId = template.templateId;
          currentNode.value.jobParams['ec-conf-templateId'] =
            template.templateId;
          currentNode.value.params.configuration.startup['ec.conf.templateId'] =
            template.templateId;
        }
      });
    });
  } else if (currentNode.value.ecConfTemplateName) {
    currentNode.value.jobParams['ec-conf-templateId'] =
      currentNode.value.ecConfTemplateId;
  }
  if (value === '0') {
    delete currentNode.value.jobParams['ec-conf-templateId'];
  }
};

const handleTemplateSelect = (templateObj: {
  templateName: string;
  templateId: string;
}) => {
  currentNode.value.ecConfTemplateName = templateObj.templateName;
  currentNode.value.ecConfTemplateId = templateObj.templateId;
  currentNode.value.jobParams['ec-conf-templateId'] = templateObj.templateId;
  currentNode.value.params.configuration.startup['ec.conf.templateId'] =
    templateObj.templateId;
  paramsTemplateNameRef.value.clearValidate();
};

const openTemplateDrawer = async () => {
  if (props.config.type === 'edit') {
    await getTemplateDataByProject();
  } else {
    await getTemplateDataByUser();
  }
  isTemplateDrawerShow.value = true;
  (document.querySelector('#templateSelectDrawer') as any)?.initRadioData?.();
};

const handleTemplateShow = () => {
  isTemplateDrawerShow.value = false;
};

const getTemplateDataByProject = async () => {
  const params = {
    projectId: currentNode.value.projectId,
    orchestratorId: currentNode.value.orchestratorId,
    jobType: currentNode.value.nodeType,
  };
  const res = await api.templateDatasApi(params);
  templateList.value.splice(0); // 清空数组
  res.data.templates.forEach((element) => {
    element.child.forEach((data) => {
      templateList.value.push({ ...data });
    });
  });
};
const getTemplateDataByUser = async () => {
  const params = {
    jobType: currentNode.value?.rows[0].nodeType,
  };
  const res = await api.templateDatasByUserApi(params);
  templateList.value.splice(0); // 清空数组
  res.data.templates.forEach((element) => {
    element.child.forEach((data) => {
      templateList.value.push({ ...data });
    });
  });
};

// 编辑参数回显
const handleEditData = (nodeData, curNodeBaseParamsList, curNodeParamsList) => {
  // console.log(
  //   'handleEditData',
  //   nodeData,
  //   curNodeBaseParamsList,
  //   curNodeParamsList
  // );
  // 遍历 curNodeBaseParamsList，查找匹配的键值对
  curNodeBaseParamsList.forEach((param) => {
    const { key } = param;
    if (key in nodeData.nodeContent) {
      nodeData[key] = nodeData.nodeContent[key];
      if (key === 'appTag' || key === 'businessTag') {
        nodeData[key] = nodeData.nodeContent[key].split(',');
      }
    }
  });
  // 初始反显模板
  if (nodeData.nodeContent['ec.conf.templateId']) {
    isRefTemplate.value = '1';
    handleRefTemplateChange('1', nodeData.nodeContent['ec.conf.templateId']);
  }
  curNodeParamsList.forEach((param) => {
    const { key } = param;
    if (key in nodeData.nodeContent) {
      nodeData.jobParams[poinToLink(key)] = nodeData.nodeContent[key];
    }
  });
};

// 批量编辑-对于未勾选的参数，放入原来的值
const originParamsAssigned = (nodeData, params) => {
  // console.log('originParamsAssigned', nodeData, params)
  const curParams = cloneDeep(params);
  curNodeParamsList.value.map((item) => {
    if (!batchShowFormItemData.value[item.key]) {
      const oldValue = nodeData.nodeContent[item.key] || item.defaultValue;
      curParams.configuration[item.position][item.key] = oldValue;
    }
  });
  return curParams;
};
const handleOk = async () => {
  try {
    await Promise.all([
      baseInfoFormRef.value.validate(),
      parameterFormRef.value.validate(),
    ]);
    if (!editLoading.value) {
      editLoading.value = true;
      if (
        currentNode.value.jobParams &&
        currentNode.value.params.configuration
      ) {
        curNodeParamsList.value.map((item) => {
          if (
            ['runtime', 'startup', 'special'].includes(item.position) &&
            currentNode.value.params &&
            (props.config.type === 'edit' ||
              batchShowFormItemData.value[item.key])
          ) {
            const value = currentNode.value.jobParams[poinToLink(item.key)]
              ? currentNode.value.jobParams[poinToLink(item.key)]
              : item.defaultValue;
            // 编辑-用户填值了取用户填写的，否则走默认值
            if (item.key && ['job.desc'].includes(item.key)) {
              const { matches, divide } = getJobDescDuplication(value);
              currentNode.value.params.configuration[item.position][item.key] =
                matches.join(divide);
            } else {
              currentNode.value.params.configuration[item.position][item.key] =
                value;
            }
          }
        });
      }
      // 单一节点编辑
      if (props.config.type === 'edit') {
        if (isRefTemplate.value === '0') {
          delete currentNode.value.ecConfTemplateId;
          delete currentNode.value.ecConfTemplateName;
          delete currentNode.value.jobParams['ec-conf-templateId'];
          delete currentNode.value.params.configuration['startup'][
            'ec.conf.templateId'
          ];
        } else {
          currentNode.value.jobParams['ec-conf-templateId'] =
            currentNode.value.ecConfTemplateId;
          currentNode.value.params.configuration['startup'][
            'ec.conf.templateId'
          ] = currentNode.value.ecConfTemplateId;
        }
        const nodeParams = {
          id: currentNode.value.contentId,
          nodeKey: currentNode.value.nodeKey,
          orchestratorId: currentNode.value.orchestratorId,
          title: currentNode.value.title,
          desc: currentNode.value.desc,
          appTag: currentNode.value.appTag?.join(',') || '',
          businessTag: currentNode.value.businessTag?.join(',') || '',
          params: JSON.stringify(currentNode.value.params),
        };
        if (isRefTemplate.value === '1') {
          nodeParams.ecConfTemplateName = currentNode.value.ecConfTemplateName;
          nodeParams.ecConfTemplateId = currentNode.value.ecConfTemplateId;
        }
        await api.editDataVisualApi({ editNodeList: [nodeParams] });
        FMessage.success($t('_.编辑成功'));
      }
      // 批量节点编辑
      if (props.config.type === 'batchEdit') {
        const batchShowFormItemDataValues = Object.values(
          batchShowFormItemData.value
        );
        // 检查所有值是否都是 false
        const allFalse = batchShowFormItemDataValues.every(
          (value) => value === false
        );
        if (allFalse) {
          FMessage.warn($t('_.请至少选择一个要编辑的数据项'));
          editLoading.value = false;
          return;
        }
        const editNodeList = [];
        currentBatchNodeNameList.value.forEach((item) => {
          const curEditNodeParams = originParamsAssigned(
            item,
            currentNode.value.params
          );
          const nodeParams = {
            id: item.contentId,
            nodeKey: item.nodeKey,
            orchestratorId: item.orchestratorId,
            title: item.nodeName,
          };
          ['desc', 'appTag', 'businessTag'].forEach((key) => {
            if (batchShowFormItemData.value[key]) {
              if (key === 'desc') {
                nodeParams[key] = currentNode.value[key] || '';
              } else {
                nodeParams[key] = currentNode.value[key]?.join(',') || '';
              }
            } else {
              nodeParams[key] = item.nodeContent[key];
            }
          });
          if (batchShowFormItemData.value['ecConfTemplateName']) {
            // 批量删除模板，不用赋值即可
            // 批量添加模板
            if (isRefTemplate.value === '1') {
              nodeParams.ecConfTemplateName =
                currentNode.value.ecConfTemplateName;
              nodeParams.ecConfTemplateId = currentNode.value.ecConfTemplateId;
              curEditNodeParams.configuration['startup']['ec.conf.templateId'] =
                currentNode.value.ecConfTemplateId;
            }
          } else if (item.templateId) {
            // 批量编辑时没操作模板，若原有模板数据则赋值
            nodeParams.ecConfTemplateName = item.templateName;
            nodeParams.ecConfTemplateId = item.templateId;
            curEditNodeParams.configuration['startup']['ec.conf.templateId'] =
              item.templateId;
          }
          nodeParams.params = JSON.stringify(curEditNodeParams);
          editNodeList.push(nodeParams);
        });
        await api.editDataVisualApi({ editNodeList: editNodeList });
        FMessage.success($t('_.批量编辑成功'));
        emits('initBatch');
      }
      drawerShow.value = false;
      editLoading.value = false;
      emits('success');
    }
  } catch (errMsg) {
    console.error(errMsg);
    editLoading.value = false;
  }
};
const closeDrawer = () => {
  emits('update:show', false);
  if (props.config.type === 'batchEdit') {
    emits('initBatch');
  }
};

// 区分编辑和批量编辑初始化
const initEditData = async () => {
  handleEditData(
    currentNode.value,
    curNodeBaseParamsList.value,
    curNodeParamsList.value
  );
};
const initBatchEditData = async () => {
  curNodeParamsList.value.forEach((item) => {
    batchShowFormItemData.value[item.key] = false;
  });
  curNodeBaseParamsList.value.forEach((item) => {
    batchShowFormItemData.value[item.key] = false;
  });
  currentBatchNodeNameList.value = props.config.selectedData.map((item) => ({
    ...item,
  }));
};

const handleBatchShowFormItemDataChange = (v, key) => {
  // console.log('handleBatchShowFormItemDataChange', v, key);
  batchShowFormItemData.value[key] = v;

  // 批量编辑时，勾选 sparkVersion 检查默认值是否为 2
  // if (
  //   props.config.type === 'batchEdit' &&
  //   key === 'sparkVersion' &&
  //   v === true &&
  //   currentNode.value.jobParams &&
  //   currentNode.value.jobParams[poinToLink(key)] === '2'
  // ) {
  //   FModal.warning({
  //     title: $t('_.提示'),
  //     content: $t('accounts.spark2Notice'),
  //     closable: true,
  //     width: 500,
  //     okText: $t('_.确定'),
  //   });
  // }
};

const handleCloseNodeTag = (v) => {
  const delIndex = currentBatchNodeNameList.value.findIndex(
    (item) => item.nodeId === v.nodeId
  );
  currentBatchNodeNameList.value.splice(delIndex, 1);
};
const handleNodeType = (item, nodetype) => {
  if (nodetype === '数据开发') {
    return item.title === '数据开发' || item.title === 'Data development';
  }
  if (nodetype === '数据可视化') {
    return item.title === '数据可视化' || item.title === 'Data visualization';
  }
  return item.title === nodetype;
};

onMounted(async () => {
  await getConsoleParams();
  if (props.config.type === 'edit') {
    const params = {
      labels: getCurrentDsslabels(),
      projectId: props.form?.projectId,
      orchestratorId: props.form?.orchestratorId,
    };
    await fetchNodeTypeDetail(params);
  } else if (props.config.type === 'batchEdit') {
    const batchOrchestratorInfo = [];
    props.form.rows.forEach((item) => {
      batchOrchestratorInfo.push({
        orchestratorId: item.orchestratorId,
        projectId: item.projectId,
      });
    });
    const batchParams = {
      batchOrchestratorInfo,
    };
    await fetchNodeTypeDetail(batchParams, 'batch');
  }
  console.log('nodeTypeDetailList', nodeTypeDetailList.value);
  curNodeTypeDetail.value = nodeTypeDetailList.value
    .find((item) => handleNodeType(item, props.nodeParentType))
    .children.find((subItem) => subItem.title === props.config.nodeTypeName);
  curNodeParamsList.value = getCurNodeParamsList();
  curNodeBaseParamsList.value = getCurNodeBaseParamsList();
  currentNode.value = cloneDeep(props.form);
  currentNode.value.consoleParams = cloneDeep(consoleParams.value);
  updateCurrentNode();
  if (props.config.type === 'edit') {
    initEditData();
  }
  if (props.config.type === 'batchEdit') {
    initBatchEditData();
  }
});
</script>
<style lang="less" scoped>
.batch-label {
  display: block;
  height: auto;
  margin-right: 0;
  margin-bottom: 30px;
}
.batch-label-selected {
  display: block;
  height: auto;
  margin-right: 0;
  margin-bottom: 16px;
}
.node-tag {
  margin-right: 8px;
  margin-bottom: 16px;
  max-width: 300px;
}
</style>
