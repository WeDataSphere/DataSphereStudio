<template>
  <FDrawer
    v-model:show="drawerShow"
    :title="mode === 'add' ? '新增AppConn' : '编辑AppConn'"
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
      <FFormItem label="AppConn名称" prop="appConnName">
        <FInput
          v-model="formData.appConnName"
          placeholder="请输入AppConn名称"
        />
      </FFormItem>
      <FFormItem label="是否需要初始化" prop="isUserNeedInit">
        <FRadioGroup v-model="formData.isUserNeedInit">
          <FRadio value="1"> 是 </FRadio>
          <FRadio value="0"> 否 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem label="等级" prop="level">
        <FInput
          v-model="formData.level"
          type="number"
          placeholder="请输入等级"
        />
      </FFormItem>
      <FFormItem label="是否支持iframe嵌入" prop="ifIframe">
        <FRadioGroup v-model="formData.ifIframe">
          <FRadio value="1"> 是 </FRadio>
          <FRadio value="0"> 否 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem label="是否微前端" prop="isMicroApp">
        <FRadioGroup v-model="formData.isMicroApp">
          <FRadio value="1"> 是 </FRadio>
          <FRadio value="0"> 否 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem label="是否外部" prop="isExternal">
        <FRadioGroup v-model="formData.isExternal">
          <FRadio value="1"> 是 </FRadio>
          <FRadio value="0"> 否 </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem label="物料包获取方式" prop="resourceFetchMethod">
        <FRadioGroup v-model="formData.resourceFetchMethod">
          <FRadio value="upload"> 手动上传 </FRadio>
          <FRadio value="related"> 引用现有AppConn </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FFormItem
        v-if="formData.resourceFetchMethod == 'upload'"
        label="AppConn物料包"
        prop="resource"
      >
        <FInputFile
          :multiple="false"
          :accept="['.zip']"
          @change="handleUploadChange"
        />
      </FFormItem>
      <FFormItem
        v-if="formData.resourceFetchMethod == 'upload'"
        label="AppConn物料包主类"
        prop="className"
      >
        <FInput
          v-model="formData.className"
          placeholder="请输入AppConn物料包主类"
        />
      </FFormItem>
      <FFormItem
        v-if="formData.resourceFetchMethod == 'related'"
        label="关联AppConn"
        prop="reference"
      >
        <FSelect
          v-model="formData.reference"
          :options="appConnlist"
          placeholder="请关联AppConn"
          filterable
          clearable
        />
      </FFormItem>
    </FForm>
  </FDrawer>
</template>
<script lang="ts" setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import {
  FForm,
  FFormItem,
  FInput,
  FInputNumber,
  FInputFile,
  FSelect,
  FRadio,
  FRadioGroup,
  FDrawer,
  FMessage,
} from '@fesjs/fes-design';
import { request } from '@dataspherestudio/shared';

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
  appConnName: '',
  isUserNeedInit: '1',
  level: '',
  ifIframe: '1',
  isMicroApp: '1',
  isExternal: '1',
  resourceFetchMethod: 'upload',
  className: '',
  reference: '',
  resource: '',
});

watch(
  () => props.appconn,
  (newVal) => {
    initData(newVal);
  }
);

const rules = reactive({
  appConnName: [
    {
      required: true,
      message: '请输入AppConn名称',
      trigger: 'blur',
    },
    {
      type: 'string',
      max: 64,
      message: '最长64个字符',
      trigger: 'blur',
    },
  ],
  className: [
    {
      required: true,
      message: '请输入AppConn物料包主类',
      trigger: 'blur',
    },
  ],
  isUserNeedInit: [
    {
      required: true,
      message: '请选择',
      trigger: 'blur',
    },
  ],
  level: [
    {
      type: 'string',
      required: true,
      message: '请输入等级',
      trigger: 'change',
    },
    {
      type: 'string',
      validator: (rule, value, callback) => {
        if (/^[1-9]\d*$/.test(value) && value >= 1 && value <= 100) {
          return callback();
        }
        callback('等级必须在1-100之间');
      },
    },
  ],
  ifIframe: [
    {
      required: true,
      message: '请选择',
      trigger: 'change',
    },
  ],
  isMicroApp: [
    {
      required: true,
      message: '请选择',
      trigger: 'blur',
    },
  ],
  isExternal: [
    {
      required: true,
      message: '请选择',
      trigger: 'blur',
    },
  ],
  reference: [
    {
      required: true,
      message: '请选择',
      trigger: 'blur',
    },
  ],
  resource: [
    {
      required: true,
      message: '请选择',
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
      .then(async () => {
        await saveAppConn();
        refins.resetFields();
        emit('updateAppcons');
        closeDrawer();
      })
      .catch((error: any) => {
        console.log('表单验证失败: ', error);
      });
  }
}

const saveAppConn = () => {
  const data: any = {
    appConnName: formData.appConnName,
    isUserNeedInit: formData.isUserNeedInit || '1',
    level: formData.level,
    ifIframe: formData.ifIframe == '1',
    isMicroApp: formData.isMicroApp == '1',
    isExternal: formData.isExternal == '1',
    resourceFetchMethod: formData.resourceFetchMethod,
    className: formData.className,
    reference: formData.reference,
    resource: formData.resource,
  };
  if (data.resourceFetchMethod == 'upload') {
    data.reference = '';
  } else {
    data.resource = '';
  }

  let url = '';
  if (props.mode == 'edit') {
    data.id = props.appconn.id;
    url = 'dss/framework/project/appconn/editAppConn';
  } else {
    url = 'dss/framework/project/appconn/addAppConn';
  }
  return request.fetch(url, data, 'post').then((resp: any) => {
    FMessage.success('保存成功');
  });
};

const handleUploadChange = function (file: any) {
  var formdata = new FormData();
  formdata.append('file', file[0]);
  return request
    .fetch('dss/framework/project/appconn/uploadAppConnResource', formdata, {
      method: 'post',
      data: formdata,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((resp: any) => {
      formData.resource = resp.data.resource;
    });
};

const appConnlist = ref([]);
const fetchAppconnList = function () {
  return request
    .fetch('dss/framework/project/appconn/getAppConnsName', { method: 'get' })
    .then((resp: any) => {
      appConnlist.value = (resp.data?.appConnsName || []).map((it) => ({
        label: it,
        value: it,
      }));
    });
};

const closeDrawer = () => {
  const refins = elFormRef.value;
  refins?.clearValidate();
  refins?.resetFields();
  emit('update:show', false);
};
const initData = (appconn) => {
  formData.appConnName = appconn.appConnName;
  formData.isUserNeedInit = appconn.isUserNeedInit;
  formData.level = appconn.level;
  formData.ifIframe = appconn.ifIframe ? '1' : '0';
  formData.isMicroApp = appconn.isMicroApp ? '1' : '0';
  formData.isExternal = appconn.isExternal ? '1' : '0';
  formData.resourceFetchMethod = appconn.resourceFetchMethod || 'upload';
  formData.className = appconn.className;
  formData.reference = appconn.reference;
  formData.resource = appconn.resource;
};
onMounted(() => {
  fetchAppconnList();
  if (props.mode === 'edit') {
    const refins = elFormRef.value;
    refins?.clearValidate();
    refins?.resetFields();
    initData(props.appconn);
  }
});
</script>
