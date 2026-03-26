<template>
  <div class="page-container">
    <div>
      <div class="info-title">基础信息</div>
      <div class="form-page">
        <FForm
          ref="nodeFormRef"
          label-width="105px"
          label-position="right"
          :model="nodeForm"
          :rules="nodeFormRules"
        >
          <FFormItem label="节点名称" prop="name">
            <FInput v-model="nodeForm.name" placeholder="请输入" />
          </FFormItem>
          <FFormItem label="节点分类" prop="nodeGroup">
            <FSelect
              v-model="nodeForm.nodeGroup"
              :options="nodeGroupList"
              placeholder="请选择"
            />
          </FFormItem>
          <FFormItem label="节点全路径名" prop="nodeType">
            <FInput v-model="nodeForm.nodeType" placeholder="请输入" />
          </FFormItem>
          <FFormItem label="关联AppConn" prop="appcconName">
            <FSelect
              v-model="nodeForm.appconnName"
              :options="appconnNameList"
              placeholder="请选择"
            />
          </FFormItem>
          <FFormItem label="节点图标" prop="iconPath">
            <FInput v-model="nodeForm.iconPath" placeholder="请输入" />
          </FFormItem>
          <!-- <FFormItem label="用户英文名" prop="userCode">
            <FInput v-model="nodeForm.userCode" placeholder="请输入" />
          </FFormItem> -->
          <FFormItem label="是否支持跳转" prop="supportJump">
            <FRadioGroup v-model="nodeForm.supportJump">
              <FRadio :value="1"> 是 </FRadio>
              <FRadio :value="0"> 否 </FRadio>
            </FRadioGroup>
          </FFormItem>
          <FFormItem label="跳转类型" prop="jumpType">
            <FRadioGroup v-model="nodeForm.jumpType">
              <FRadio :value="2"> 内部节点 </FRadio>
              <FRadio :value="1"> 外部节点 </FRadio>
            </FRadioGroup>
          </FFormItem>
          <FFormItem label="是否支持拷贝" prop="enableCopy">
            <FRadioGroup v-model="nodeForm.enableCopy">
              <FRadio :value="1"> 是 </FRadio>
              <FRadio :value="0"> 否 </FRadio>
            </FRadioGroup>
          </FFormItem>
          <FFormItem label="是否支持发布" prop="submitToScheduler">
            <FRadioGroup v-model="nodeForm.submitToScheduler">
              <FRadio :value="1"> 是 </FRadio>
              <FRadio :value="0"> 否 </FRadio>
            </FRadioGroup>
          </FFormItem>
          <FFormItem label="是否需要弹窗" prop="shouldCreationBeforeNode">
            <FRadioGroup v-model="nodeForm.shouldCreationBeforeNode">
              <FRadio :value="1"> 是 </FRadio>
              <FRadio :value="0"> 否 </FRadio>
            </FRadioGroup>
          </FFormItem>
        </FForm>
        <div style="margin-left: 445px">
          <FButton type="info" @click="submitNodeInfo"> 保存基础信息 </FButton>
        </div>
      </div>
    </div>
    <div>
      <div class="info-title">属性信息</div>
      <div style="margin-bottom: 16px">
        <FButton type="info" @click="addAttributeInfo"> 添加属性信息 </FButton>
      </div>
      <div style="margin-bottom: 16px">
        <f-table :data="pageLists">
          <f-table-column
            :formatter="fillText"
            prop="lableNameEn"
            label="字段英文名"
            align="left"
            :width="180"
            ellipsis
          />
          <f-table-column
            :formatter="fillText"
            prop="descriptionEn"
            label="字段英文描述"
            align="left"
            :width="160"
            ellipsis
          />
          <f-table-column
            :formatter="fillText"
            prop="lableName"
            label="字段中文名"
            align="left"
            :width="180"
            ellipsis
          />
          <f-table-column
            :formatter="fillText"
            prop="description"
            label="字段中文描述"
            align="left"
            :width="160"
            ellipsis
          />
          <f-table-column
            :formatter="fillText"
            prop="uiType"
            label="输入类型"
            align="left"
            :width="140"
            ellipsis
          />
          <f-table-column
            v-slot="{ row = {} }"
            :formatter="fillText"
            prop="required"
            label="是否必填"
            align="left"
            :width="88"
            ellipsis
          >
            {{ row.required === 1 ? '是' : '否' }}
          </f-table-column>
          <f-table-column
            :formatter="fillText"
            prop="defaultValue"
            label="默认值"
            align="left"
            :width="88"
            ellipsis
          />
          <f-table-column
            v-slot="{ row = {} }"
            :formatter="fillText"
            prop="isHidden"
            label="是否隐藏"
            align="left"
            :width="88"
            ellipsis
          >
            {{ row.isHidden === 1 ? '是' : '否' }}
          </f-table-column>
          <f-table-column
            :formatter="fillText"
            prop="condition"
            label="显示条件"
            align="left"
            :width="88"
            ellipsis
          />
          <f-table-column
            v-slot="{ row }"
            prop="condition"
            label="校验条件"
            align="left"
            :width="88"
            ellipsis
          >
            <FPopper placement="bottom" trigger="click" :arrow="true">
              <template #trigger>
                <FButton type="link" @click="() => viewRuleDetail(row)">
                  查看
                </FButton>
              </template>
              <div style="padding: 15px">
                <div
                  v-for="(item, index) in curRuleDetail"
                  :key="index"
                  style="margin-bottom: 10px"
                >
                  <p>校验类型：{{ item.validateType }}</p>
                  <p>校验范围：{{ item.validateRange }}</p>
                  <p>校验提示：{{ item.errorMsg }}</p>
                </div>
              </div>
            </FPopper>
          </f-table-column>
          <f-table-column
            v-slot="{ row }"
            label="操作"
            fixed="right"
            align="left"
            :width="148"
            ellipsis
          >
            <div class="operation">
              <div class="edit" @click="editAttribute(row)">编辑</div>
              <div class="unbind" @click="unbindAttribute(row)">解绑</div>
              <div class="delete" @click="deleteAttribute(row)">删除</div>
            </div>
          </f-table-column>
        </f-table>
      </div>
      <FPagination
        v-model:currentPage="pagination.pageNum"
        v-model:pageSize="pagination.pageSize"
        style="justify-content: end"
        show-size-changer
        show-total
        :total-count="tableShowLists.length"
      />
    </div>
  </div>
  <FDrawer
    v-model:show="showDrawer"
    title="添加属性信息"
    :footer="true"
    content-class="drawer-content"
    display-directive="if"
    :mask-closable="false"
    width="50%"
    @ok="submitAttributeAndRule"
  >
    <FForm
      ref="attributeFormRef"
      label-width="95px"
      label-position="right"
      :model="attributeForm"
      :rules="attributeFormRules"
    >
      <FFormItem label="属性名称" prop="key">
        <FSelect
          v-model="attributeOption"
          :options="attributeOptionList"
          placeholder="请选择"
          style="width: 100px; margin-right: 8px"
        />
        <FInput
          v-if="attributeOption === '自定义'"
          v-model="attributeForm.key"
          placeholder="请输入"
        />
        <FSelect
          v-else
          v-model="attributeForm.key"
          :options="inlineOptionsList"
          placeholder="请选择"
          clearable
          @change="toUpdateuiId"
        />
      </FFormItem>
      <div v-if="attributeOption === '自定义'">
        <FFormItem label="输入类型" prop="uiType">
          <FSelect
            v-model="attributeForm.uiType"
            :options="uiTypeList"
            placeholder="请选择"
            clearable
          />
        </FFormItem>
        <FFormItem label="显示顺序" prop="order">
          <FInputNumber
            v-model="attributeForm.order"
            :min="1"
            :max="100"
            :step="1"
            style="width: 100%"
            placeholder="请输入1-100之间的正整数"
          />
        </FFormItem>
        <FFormItem label="属性类型" prop="nodeMenuType">
          <FSelect
            v-model="attributeForm.nodeMenuType"
            :options="nodeMenuTypeList"
            placeholder="请选择"
          />
        </FFormItem>
        <FFormItem label="属性存储位置" prop="position">
          <FInput v-model="attributeForm.position" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="标签中文名" prop="lableName">
          <FInput v-model="attributeForm.lableName" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="标签英文名" prop="lableNameEn">
          <FInput v-model="attributeForm.lableNameEn" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="属性中文描述" prop="description">
          <FInput v-model="attributeForm.description" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="属性英文描述" prop="descriptionEn">
          <FInput v-model="attributeForm.descriptionEn" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="是否必填" prop="required">
          <FRadioGroup v-model="attributeForm.required">
            <FRadio :value="1"> 是 </FRadio>
            <FRadio :value="0"> 否 </FRadio>
          </FRadioGroup>
        </FFormItem>
        <FFormItem label="是否隐藏" prop="isHidden">
          <FRadioGroup v-model="attributeForm.isHidden">
            <FRadio :value="1"> 是 </FRadio>
            <FRadio :value="0"> 否 </FRadio>
          </FRadioGroup>
        </FFormItem>
        <FFormItem label="是否基础属性" prop="isBaseInfo">
          <FRadioGroup v-model="attributeForm.isBaseInfo">
            <FRadio :value="1"> 是 </FRadio>
            <FRadio :value="0"> 否 </FRadio>
          </FRadioGroup>
        </FFormItem>
        <FFormItem label="是否高级属性" prop="isAdvanced">
          <FRadioGroup v-model="attributeForm.isAdvanced">
            <FRadio :value="1"> 是 </FRadio>
            <FRadio :value="0"> 否 </FRadio>
          </FRadioGroup>
        </FFormItem>
        <FFormItem label="显示条件" prop="condition">
          <FInput v-model="attributeForm.condition" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="值" prop="value">
          <FInput v-model="attributeForm.value" placeholder="请输入" />
        </FFormItem>
        <FFormItem label="默认值" prop="defaultValue">
          <FInput v-model="attributeForm.defaultValue" placeholder="请输入" />
        </FFormItem>
      </div>
      <FFormItem label="是否校验" prop="isVerification">
        <FRadioGroup
          v-model="attributeForm.isVerification"
          :disabled="attributeOption == '内置'"
        >
          <FRadio :value="1"> 是 </FRadio>
          <FRadio :value="0"> 否 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem
        v-if="attributeForm.isVerification === 1"
        label="校验规则"
        prop="verificationRule"
      >
        <div style="width: 100%">
          <div
            v-for="(alarmData, index) in verificationRuleList"
            :key="index"
            class="verification-rule"
          >
            <FForm
              :ref="
                (el) => {
                  if (el) verificationRuleListRefs[index] = el;
                }
              "
              label-width="92px"
              label-position="right"
              :model="verificationRuleList[index]"
              :rules="verificationRuleValidate"
            >
              <div class="sub-title">
                <div class="left">
                  校验规则{{ index < 9 ? `0${index + 1}` : index + 1 }}
                </div>
                <div class="right">
                  <FButton
                    type="link"
                    class="link-button"
                    @click="saveVerificationRule(index)"
                  >
                    保存
                  </FButton>
                  <FButton
                    v-if="verificationRuleList.length > 1"
                    type="link"
                    style="color: #63656f"
                    class="link-button"
                    @click="removeVerificationRule(index)"
                  >
                    删除
                  </FButton>
                </div>
              </div>
              <FFormItem label="触发规则" prop="trigger">
                <FSelect
                  v-model="verificationRuleList[index].trigger"
                  :options="triggerList"
                  placeholder="请选择"
                />
              </FFormItem>
              <FFormItem label="校验类型" prop="validateType">
                <FSelect
                  v-model="verificationRuleList[index].validateType"
                  :options="validateTypeList"
                  placeholder="请选择"
                />
              </FFormItem>
              <FFormItem label="校验表达式" prop="validateRange">
                <FInput
                  v-model="verificationRuleList[index].validateRange"
                  placeholder="请输入"
                />
              </FFormItem>
              <FFormItem label="校验失败中文提示" prop="errorMsg">
                <FInput
                  v-model="verificationRuleList[index].errorMsg"
                  placeholder="请输入中文提示"
                />
              </FFormItem>
              <FFormItem
                label="校验失败英文提示"
                prop="errorMsgEn"
                style="margin-bottom: 0px"
              >
                <FInput
                  v-model="verificationRuleList[index].errorMsgEn"
                  placeholder="请输入英文提示"
                />
              </FFormItem>
            </FForm>
          </div>
          <div>
            <FButton type="link" class="link-add" @click="addVerificationRule">
              <template #icon>
                <PlusCircleOutlined /> </template
              >添加校验规则
            </FButton>
          </div>
        </div>
      </FFormItem>
    </FForm>
  </FDrawer>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { FMessage, FPopper, FInputNumber } from '@fesjs/fes-design';
import { request } from '@dataspherestudio/shared';
import { PlusCircleOutlined } from '@fesjs/fes-design/icon';
import { useRouter, useRoute } from 'vue-router';
import { usePagination } from '@dataspherestudio/workspace/module/hooks/usePagination';
import {
  getNodeGroupList,
  getAllAppconnNameList,
  getNodeInfo,
  saveNodeInfo,
  queryAttribute,
  saveNodeAttribute,
  saveVerificationRuleData,
  ruleBindAttribute,
  unbindVerification,
  nodeBindAttribute,
  nodeDeleteAttribute,
  nodeUnbindAttribute,
  queryValidateRule,
} from '../addNode/api';

const { fillText } = usePagination();

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

const router = useRouter();
const route = useRoute();
const isLoading = ref(false);
const actionType = ref('emptyQueryResult');
const nodeId = ref(null);
const uiId = ref(null);
const validateId = ref(null);
const nodeForm = ref({
  supportJump: 1,
  jumpType: 1,
  enableCopy: 1,
  submitToScheduler: 1,
  shouldCreationBeforeNode: 0,
});
const nodeFormRef = ref(null);
const nodeGroupList = ref([]);
const appconnNameList = ref([]);
const inlineOptionsList = ref([]);
const tableShowLists = ref([]);
const showDrawer = ref(false);
const nodeFormRules = computed(() => ({
  name: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  nodeGroup: [
    {
      required: true,
      message: '不能为空',
      type: 'number',
      trigger: ['change', 'blur'],
    },
  ],
  nodeType: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  appconnName: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  iconPath: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  userCode: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  supportJump: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: 'change',
    },
  ],
  jumpType: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: 'change',
    },
  ],
  enableCopy: [
    {
      required: true,
      message: '不能为空',
      type: 'number',
      trigger: 'change',
    },
  ],
  submitToScheduler: [
    {
      required: true,
      message: '不能为空',
      type: 'number',
      trigger: 'change',
    },
  ],
  shouldCreationBeforeNode: [
    {
      required: true,
      message: '不能为空',
      type: 'number',
      trigger: 'change',
    },
  ],
}));

// 保存节点基本信息
const submitNodeInfo = async () => {
  try {
    await nodeFormRef.value.validate();
    const data = await saveNodeInfo(nodeForm.value);
    nodeId.value = data.id;
    console.log(nodeId.value, 111);
    FMessage.success('节点基础信息保存成功');
  } catch (err) {
    console.error(err);
  }
};

const initAttributeForm = () => ({
  value: '',
  defaultValue: '',
  condition: '',
  required: 1,
  isHidden: 0,
  isAdvanced: 0,
  isBaseInfo: 1,
  isVerification: 1,
});

const attributeForm = ref(initAttributeForm());
const attributeFormRef = ref(null);
const validateIdList = ref([]);
const attributeOption = ref('自定义');
const attributeOptionList = [
  { label: '自定义', value: '自定义' },
  { label: '内置', value: '内置' },
];
const nodeMenuTypeList = [
  { label: '0', value: 0 },
  { label: '1', value: 1 },
];
const uiTypeList = ref([
  { label: 'Input', value: 'Input' },
  { label: 'Text', value: 'Text' },
  { label: 'Tag', value: 'Tag' },
  { label: 'Upload', value: 'Upload' },
  { label: 'Select', value: 'Select' },
  { label: 'MultiBinding', value: 'MultiBinding' },
  { label: 'Disable', value: 'Disable' },
  { label: 'Binding', value: 'Binding' },
]);
const attributeFormRules = computed(() => ({
  key: [
    {
      required: true,
      max: 64,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  uiType: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  order: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  nodeMenuType: [
    {
      required: true,
      message: '不能为空',
      type: 'number',
      trigger: ['change', 'blur'],
    },
  ],
  position: [
    {
      required: true,
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  lableName: [
    {
      required: true,
      message: '不能为空',
      mxa: 64,
      trigger: ['change', 'blur'],
    },
  ],
  lableNameEn: [
    {
      required: true,
      message: '不能为空',
      max: 64,
      trigger: ['change', 'blur'],
    },
  ],
  required: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  isHidden: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  isBaseInfo: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  isAdvanced: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
  isVerification: [
    {
      required: true,
      type: 'number',
      message: '不能为空',
      trigger: ['change', 'blur'],
    },
  ],
}));

const pageLists = computed(() => {
  return tableShowLists.value.slice(
    (pagination.pageNum - 1) * pagination.pageSize,
    pagination.pageNum * pagination.pageSize
  );
});

const curRuleDetail = ref([]);
const viewRuleDetail = async (row) => {
  curRuleDetail.value = await queryValidateRule({ uiId: row.id });
};

const fetchTableData = async (params?): Promise<void> => {
  if (isLoading.value) {
    return;
  }
  isLoading.value = true;
  actionType.value = 'loading';
  tableShowLists.value = [];
  try {
    const data = await queryAttribute(params);
    tableShowLists.value = data.uiList;
    isLoading.value = false;
    pagination.total = data.total;
    if (pagination.total === 0) {
      actionType.value = 'emptyQueryResult';
    }
  } catch (err) {
    isLoading.value = false;
    console.log(err);
  }
};

const getVerificationRuleItem = (rule) => {
  const item = {
    trigger: '',
    validateType: '',
    validateRange: '',
    errorMsg: '',
    errorMsgEn: '',
  };
  return item;
};
const verificationRuleListRefs = ref([]);
const verificationRuleList = ref([getVerificationRuleItem()]);
const triggerList = [
  { label: 'blur', value: 'blur' },
  { label: 'change', value: 'change' },
];
const validateTypeList = [
  { label: 'None', value: 'None' },
  { label: 'NumInterval', value: 'NumInterval' },
  { label: 'FloatInterval', value: 'FloatInteval' },
  { label: 'Include', value: 'Include' },
  { label: 'Regex', value: 'Regex' },
  { label: 'OPF', value: 'OPF' },
  { label: 'OFT', value: 'OFT' },
  { label: 'Required', value: 'Required' },
  { label: 'Function', value: 'Function' },
];

const verificationRuleValidate = computed(() => ({
  trigger: [
    { required: true, message: '不能为空', trigger: ['change', 'blur'] },
  ],
  validateType: [
    { required: true, message: '不能为空', trigger: ['change', 'blur'] },
  ],
}));

const toUpdateuiId = async (val) => {
  uiId.value = inlineOptionsList.value.find((item) => item.value === val)?.id;
  verificationRuleList.value = await queryValidateRule({ uiId: uiId.value });
  attributeForm.value.isVerification =
    verificationRuleList.value.length > 0 ? 1 : 0;
};

// 添加属性
const addAttributeInfo = () => {
  attributeForm.value = initAttributeForm();
  verificationRuleList.value = [{}];
  showDrawer.value = true;
};

// 保存属性基本信息
const saveAttributeInfo = async () => {
  try {
    await attributeFormRef.value.validate();
    const params = { ...attributeForm.value };
    delete params.isVerification;
    await saveNodeAttribute(params);
  } catch (error) {
    console.error(error);
  }
};

// 保存校验规则
const saveVerificationRule = async (index) => {
  try {
    await verificationRuleListRefs.value[index].validate();
    const res = await saveVerificationRuleData(
      verificationRuleList.value[index]
    );
    if (res && res.id) {
      FMessage.success('规则保存成功');
    }
    verificationRuleList.value[index].id = res.id;
  } catch (err) {
    console.warn(err);
  }
};

// 删除校验规则
const removeVerificationRule = (index) => {
  verificationRuleList.value.splice(index, 1);
};

// 新建校验规则
const addVerificationRule = () => {
  verificationRuleList.value.push(getVerificationRuleItem());
};

// 提交属性和规则
const submitAttributeAndRule = async () => {
  try {
    if (attributeOption.value === '内置') {
      await nodeBindAttribute({
        nodeId: route.query.id,
        uiId: uiId.value || attributeForm.value.id,
      });

      showDrawer.value = false;
    } else {
      await attributeFormRef.value.validate();
      const params = { ...attributeForm.value };
      delete params.isVerification;
      if (!attributeForm.value.id) {
        attributeForm.value.id = (await saveNodeAttribute(params)).id;
        await nodeBindAttribute({
          nodeId: route.query.id,
          uiId: attributeForm.value.id,
        });
      } else {
        await saveNodeAttribute(params);
      }
      if (attributeForm.value.isVerification) {
        verificationRuleList.value.forEach(async (item) => {
          if (item.id) {
            await ruleBindAttribute({
              uiId: attributeForm.value.id,
              validateId: item.id,
            });
          }
        });
      }
      showDrawer.value = false;
    }
    FMessage.success('成功编辑属性');
    await fetchTableData({ nodeId: route.query.id });
  } catch (error) {
    console.error(error);
  }
};

const editAttribute = async (row) => {
  showDrawer.value = true;
  attributeForm.value = {
    ...row,
    isVerification: verificationRuleList.value.length > 0 ? 1 : 0,
  };
  verificationRuleList.value = await queryValidateRule({ uiId: row.id });
};

const unbindAttribute = async (row) => {
  try {
    await nodeUnbindAttribute({ nodeId: route.query.id, uiId: row.id });
    FMessage.success('成功解绑属性信息');
    fetchTableData({ nodeId: route.query.id });
  } catch (error) {
    console.error(error);
  }
};

const deleteAttribute = async (row) => {
  try {
    await nodeDeleteAttribute({ nodeId: route.query.id, uiId: row.id });
    FMessage.success('成功删除属性信息');
    fetchTableData();
  } catch (error) {
    console.error(error);
  }
};

onMounted(async () => {
  nodeGroupList.value = (await getNodeGroupList()).map((item) => ({
    label: item.name,
    value: item.id,
  }));
  appconnNameList.value = (await getAllAppconnNameList()).map((item) => ({
    label: item,
    value: item,
  }));
  inlineOptionsList.value = (await queryAttribute()).uiList.map((item) => ({
    label: item.lableName,
    value: item.key,
    id: item.id,
  }));
  nodeForm.value = reactive(
    (await getNodeInfo({ nodeId: route.query.id })).nodeList[0]
  );
  fetchTableData({ nodeId: route.query.id });
});
</script>
<style lang="less">
.drawer-content {
  .fes-form-item-label {
    text-align: right;
  }
}
</style>
<style lang="less" scoped>
.page-container {
  padding: 24px;
  background: #ffffff;
  border-radius: 4px;
  .info-title {
    font-family: PingFangSC-Medium;
    font-size: 14px;
    color: #0f1222;
    line-height: 22px;
    font-weight: 500;
    margin-bottom: 16px;
  }
  .form-page {
    border: 1px solid #cfd0d3;
    border-radius: 4px;
    padding: 16px;
    width: 600px;
    margin-bottom: 16px;
  }
}
.verification-rule {
  background: #ffffff;
  border: 1px solid #cfd0d3;
  border-radius: 4px;
  padding: 16px 16px 24px;
  margin-bottom: 16px;
  .sub-title {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
    .left {
      font-family: PingFangSC-Regular;
      font-size: 14px;
      color: #0f1222;
      letter-spacing: 0;
      line-height: 22px;
      font-weight: 400;
      width: 73px;
    }
    .right {
      .link-button {
        height: 22px;
        line-height: 22px;
        padding: 0px;
        font-family: PingFangSC-Regular;
        font-weight: 400;
        --f-btn-min-width: 44px;
      }
    }
  }
}
.link-add {
  height: 20px;
  line-height: 20px;
  padding: 0px;
}

.operation {
  display: flex;
  .edit {
    cursor: pointer;
    margin-right: 16px;
    color: blue;
  }
  .unbind {
    cursor: pointer;
    margin-right: 16px;
    color: blue;
  }
  .delete {
    cursor: pointer;
    color: red;
  }
}
</style>
