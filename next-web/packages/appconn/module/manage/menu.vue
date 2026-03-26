<template>
  <FDrawer
    v-model:show="drawerShow"
    title="配置菜单"
    :mask-closable="false"
    display-directive="if"
    width="50%"
    :footer="true"
    content-class="operate-template"
    @cancel="closeDrawer"
    @ok="handelSubmit"
  >
    <FForm
      ref="elFormRef"
      :model="formData"
      :rules="rules"
      label-position="top"
      size="small"
    >
      <FFormItem label="所属菜单分类" prop="menuId">
        <FSelect
          v-model="formData.menuId"
          :options="menuOptions"
          placeholder="请选择所属菜单分类"
          filterable
          clearable
          value-field="id"
          label-field="name"
        />
      </FFormItem>
      <FFormItem label="中文标题" prop="titleCn">
        <FInput v-model="formData.titleCn" placeholder="请输入中文标题" />
      </FFormItem>
      <FFormItem label="英文标题" prop="titleEn">
        <FInput v-model="formData.titleEn" placeholder="请输入英文标题" />
      </FFormItem>

      <FFormItem label="状态" prop="isActive">
        <FRadioGroup v-model="formData.isActive">
          <FRadio value="1"> 可访问 </FRadio>
          <FRadio value="0"> 不可访问 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem label="AppConn图标" prop="icon">
        <FInput
          v-model="formData.icon"
          placeholder="请输入图标名称（必须为前端已有svg）"
        />
      </FFormItem>
      <FFormItem label="顺序" prop="order">
        <FInputNumber
          v-model="formData.order"
          type="number"
          placeholder="请输入顺序"
        />
      </FFormItem>
      <FFormItem label="图片" prop="image">
        <FInput
          v-model="formData.image"
          placeholder="请输入图标名称（必须为前端已有svg）"
        />
      </FFormItem>
      <FFormItem label="中文描述" prop="descCn">
        <FInput v-model="formData.descCn" placeholder="请输入中文描述" />
      </FFormItem>
      <FFormItem label="英文描述" prop="descEn">
        <FInput v-model="formData.descEn" placeholder="请输入英文描述" />
      </FFormItem>
      <FFormItem label="中文标签" prop="labelsCn">
        <FInput v-model="formData.labelsCn" placeholder="请输入中文标签" />
      </FFormItem>
      <FFormItem label="英文标签" prop="labelsEn">
        <FInput v-model="formData.labelsEn" placeholder="请输入英文标签" />
      </FFormItem>
      <FFormItem label="中文访问按钮" prop="accessButtonCn">
        <FInput
          v-model="formData.accessButtonCn"
          placeholder="请输入中文访问按钮"
        />
      </FFormItem>
      <FFormItem label="英文访问按钮" prop="accessButtonEn">
        <FInput
          v-model="formData.accessButtonEn"
          placeholder="请输入英文访问按钮"
        />
      </FFormItem>
      <FFormItem label="中文使用文档按钮" prop="manualButtonCn">
        <FInput
          v-model="formData.manualButtonCn"
          placeholder="请输入中文使用文档按钮"
        />
      </FFormItem>
      <FFormItem label="英文使用文档按钮" prop="manualButtonEn">
        <FInput
          v-model="formData.manualButtonEn"
          placeholder="请输入英文使用文档按钮"
        />
      </FFormItem>
    </FForm>
  </FDrawer>
</template>
<script lang="ts" setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { request } from '@dataspherestudio/shared';
import {
  FForm,
  FFormItem,
  FInput,
  FInputNumber,
  FRadio,
  FSelect,
  FRadioGroup,
  FDrawer,
  FMessage,
} from '@fesjs/fes-design';

const props = defineProps({
  mode: {
    type: String,
    required: true,
  },
  show: {
    type: Boolean,
    required: true,
    default: false,
  },
  appconn: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['update:show', 'updateAppcons']);

const drawerShow = computed({
  get: () => props.show,
  set: (value) => {
    emit('update:show', value);
  },
});

const formData = reactive({
  id: '',
  appconnId: '',
  menuId: '',
  titleEn: '',
  titleCn: '',
  descEn: '',
  descCn: '',
  labelsEn: '',
  labelsCn: '',
  isActive: '1',
  accessButtonEn: '',
  accessButtonCn: '',
  manualButtonEn: '',
  manualButtonCn: '',
  manualButtonUrl: '',
  icon: '',
  order: '',
  image: '',
});
const rules = reactive({
  menuId: [
    {
      type: 'number',
      required: true,
      message: '请选择所属菜单分类',
      trigger: 'blur',
    },
  ],
  titleEn: [
    {
      required: true,
      message: '请输入英文标题',
      trigger: 'blur',
    },
    {
      type: 'string',
      max: 128,
      message: '最长128个字符',
      trigger: 'blur',
    },
  ],
  titleCn: [
    {
      required: true,
      message: '请输入中文标题',
      trigger: 'blur',
    },
    {
      type: 'string',
      max: 64,
      message: '最长64个字符',
      trigger: 'blur',
    },
  ],
  isActive: [
    {
      required: true,
      message: '请选择状态',
      trigger: 'blur',
    },
  ],
  icon: [
    {
      required: true,
      message: '请输入图标',
      trigger: 'blur',
    },
  ],
  order: [
    {
      required: true,
      message: '请输入顺序',
      trigger: 'blur',
    },
    {
      validator: (rule, value, callback) => {
        if (/^[1-9]\d*$/.test(value) && value >= 1 && value <= 100) {
          return callback();
        }
        callback('顺序应为1-100之间的整数');
      },
    },
  ],
  iamge: [
    {
      required: true,
      message: '请输入图标',
      trigger: 'blur',
    },
  ],
});

type FormRefType = typeof FForm | null;
const elFormRef = ref<FormRefType>(null);

function handelSubmit() {
  const refins = elFormRef.value;
  if (refins) {
    refins
      .validate()
      .then(() => {
        saveForm();
        emit('updateAppcons');
        closeDrawer();
        refins.resetFields();
      })
      .catch((error: any) => {
        console.log('表单验证失败: ', error);
      });
  }
}

const closeDrawer = () => {
  const refins = elFormRef.value;
  refins?.clearValidate();
  refins?.resetFields();
  emit('update:show', false);
};
const menuOptions = <any[]>reactive([]);
const getMenuOptions = async () => {
  return request
    .fetch('dss/framework/appconnmanager/getmenutype', {}, 'get')
    .then((resp) => {
      let { data } = resp;
      menuOptions.length = 0;
      menuOptions.push(...data.menuType);
    });
};
const getMenuConf = async (appconnId) => {
  return request
    .fetch('dss/framework/appconnmanager/getmenuappconn', { appconnId }, 'get')
    .then((resp) => {
      let { data } = resp;
      if (data.menu) {
        formData.appconnId = data.menu.appconnId;
        formData.menuId = data.menu.menuId;
        formData.id = data.menu.id;
        formData.titleEn = data.menu.titleEn;
        formData.titleCn = data.menu.titleCn;
        formData.descEn = data.menu.descEn;
        formData.descCn = data.menu.descCn;
        formData.labelsEn = data.menu.labelsEn;
        formData.labelsCn = data.menu.labelsCn;
        formData.isActive = `${data.menu.isActive}`;
        formData.accessButtonEn = data.menu.accessButtonEn;
        formData.accessButtonCn = data.menu.accessButtonCn;
        formData.manualButtonEn = data.menu.manualButtonEn;
        formData.manualButtonCn = data.menu.manualButtonCn;
        formData.manualButtonUrl = data.menu.manualButtonUrl;
        formData.icon = data.menu.icon;
        formData.order = data.menu.order;
        formData.image = data.menu.image;
      }
    });
};
// 保存表单 /api/rest_j/v1/dss/framework/appconnmanager/savemenuappconn
const saveForm = () => {
  const data = {
    ...formData,
    appconnId: props.appconn.id,
    isActive: formData.isActive - 0,
  };
  return request
    .fetch('dss/framework/appconnmanager/savemenuappconn', { ...data }, 'post')
    .then((resp) => {
      FMessage.success('保存成功');
    });
};
watch(
  () => props.appconn,
  (newVal) => {
    getMenuConf(newVal.id);
  }
);
onMounted(() => {
  getMenuOptions();
  if (props.appconn.id) {
    getMenuConf(props.appconn.id);
  }
});
</script>
