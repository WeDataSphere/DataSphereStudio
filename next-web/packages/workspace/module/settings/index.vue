<template>
  <div class="container">
    <h4 class="title">
      {{ $t('_.工作流可选配置') }}
    </h4>
    <FForm ref="formRef" label-position="top" :model="formData">
      <FFormItem prop="enabledFlowKeywordsCheck">
        <template #label>
          <span
            >{{ $t('_.是否开启工作流关键字校验')
            }}<span style="position: relative">
              <FTooltip placement="right">
                <QuestionCircleOutlined />
                <template #content>
                  <div style="width: 490px">
                    <p>{{ $t('_.工作流节点配置提示') }}</p>
                  </div>
                </template>
              </FTooltip>
            </span>
          </span>
        </template>
        <FRadioGroup
          v-model="formData.enabledFlowKeywordsCheck"
          :cancelable="false"
          @change="saveEnableConfigData('enabledFlowKeywordsCheck')"
        >
          <FRadio value="1">
            {{ $t('_.是') }}
          </FRadio>
          <FRadio value="0">
            {{ $t('_.否') }}
          </FRadio>
        </FRadioGroup>
      </FFormItem>
      <FForm
        layout="inline"
        :inline-item-width="500"
        :inline-item-gap="50"
        :model="formData"
        label-position="top"
        class="inline-form"
      >
        <FFormItem prop="templateIds">
          <template #label>
            <span style="margin-right: 8px"
              >{{ $t('_.创建工作流默认配置')
              }}<span style="position: relative">
                <FTooltip placement="right">
                  <QuestionCircleOutlined />
                  <template #content>
                    <div style="width: 490px">
                      <p>
                        {{
                          $t(
                            '_.选择该配置后，在该工作空间下面的所有项目创建工作流均会默认带上这些配置'
                          )
                        }}
                      </p>
                    </div>
                  </template>
                </FTooltip>
              </span>
            </span>
          </template>
          <FSelect
            v-model="formData.templateIds"
            filterable
            multiple
            :placeholder="$t('_.默认资源参数模板')"
            clearable
            @change="saveTemplateIds"
          >
            <FSelectGroupOption
              v-for="(group, index) in templateList"
              :key="group.enginType + index"
              :label="group.enginType"
            >
              <FOption
                v-for="item in group.child"
                :key="item.templateId"
                :value="item.templateId"
                :disabled="handleTemplateDisabled(group, item)"
              >
                {{ item.templateName }}
              </FOption>
            </FSelectGroupOption>
          </FSelect>
        </FFormItem>
        <FFormItem
          v-if="formData.templateIds && formData.templateIds.length"
          prop="isDefaultReference"
        >
          <template #label>
            <span
              class="more-label-text"
              :title="
                $t(
                  '_.选择该配置后，在该工作空间下面的所有项目创建工作流均会默认带上这些配置'
                )
              "
              >{{ $t('_.新增节点默认引用资源参数模板')
              }}<QuestionCircleFilled />
            </span>
          </template>
          <FRadioGroup
            v-model="formData.isDefaultReference"
            :cancelable="false"
            @change="saveEnableConfigData('isDefaultReference')"
          >
            <FRadio value="1">
              {{ $t('_.是') }}
            </FRadio>
            <FRadio value="0">
              {{ $t('_.否') }}
            </FRadio>
          </FRadioGroup>
        </FFormItem>
      </FForm>
      <FFormItem prop="starrocksconfig">
        <template #label>
          <span style="margin-right: 8px">{{ $t('_.StarRocks集群配置') }}</span>
        </template>
        <FTable ref="tableRef" :data="formData.starrocksconfig">
          <template #empty>
            <FButton @click="handleBtnClick('add')">
              {{ $t('_.新增') }}
            </FButton>
          </template>
          <FTable-column
            :label="$t('_.集群名')"
            prop="clusterName"
            ellipsis
            :width="120"
          />
          <FTable-column label="IP" prop="clusterIp" :width="100" />
          <FTable-column
            :label="$t('_.HTTP端口')"
            prop="httpPort"
            :width="80"
          />
          <FTable-column :label="$t('_.TCP端口')" prop="tcpPort" :width="80" />
          <FTable-column
            v-slot="{ row }"
            :label="$t('_.是否默认')"
            prop="defaultCluster"
            :width="80"
          >
            {{ row.defaultCluster == '1' ? `${$t('_.是')}` : `${$t('_.否')}` }}
          </FTable-column>
          <FTable-column
            v-slot="{ row, rowIndex }"
            :label="$t('_.操作')"
            fixed="right"
            :width="200"
          >
            <FButton
              type="info"
              style="border: none"
              @click="handleBtnClick('edit', row)"
            >
              {{ $t('_.编辑') }}
            </FButton>
            <FButton
              type="danger"
              style="border: none"
              @click="handleBtnClick('del', row)"
            >
              {{ $t('_.删除') }}
            </FButton>
            <FButton
              v-if="rowIndex == formData.starrocksconfig.length - 1"
              type="info"
              style="border: none"
              @click="handleBtnClick('add', row)"
            >
              {{ $t('_.新增') }}
            </FButton>
          </FTable-column>
        </FTable>
      </FFormItem>
    </FForm>
    <FModal
      v-model:show="showModal"
      :title="$t('_.集群信息')"
      display-directive="if"
      :ok-text="$t('_.确认')"
      :cancel-text="$t('_.取消')"
      :mask-closable="false"
      @ok="handleOk"
    >
      <FForm
        ref="configRef"
        label-position="right"
        :model="configItem"
        :rules="formRule"
        :label-width="100"
      >
        <FFormItem prop="clusterName" :label="$t('_.集群名')">
          <FInput
            v-model="configItem.clusterName"
            :disabled="formType === 'edit'"
          />
        </FFormItem>
        <FFormItem prop="clusterIp" label="IP">
          <FInput v-model="configItem.clusterIp" />
        </FFormItem>
        <FFormItem prop="httpPort" :label="$t('_.HTTP端口')">
          <FInputNumber v-model="configItem.httpPort" :precision="0" />
        </FFormItem>
        <FFormItem prop="tcpPort" :label="$t('_.TCP端口')">
          <FInputNumber v-model="configItem.tcpPort" :precision="0" />
        </FFormItem>
        <FFormItem prop="defaultCluster" :label="$t('_.是否默认')">
          <FRadioGroup v-model="configItem.defaultCluster" :cancelable="false">
            <FRadio value="1">
              {{ $t('_.是') }}
            </FRadio>
            <FRadio value="0">
              {{ $t('_.否') }}
            </FRadio>
          </FRadioGroup>
        </FFormItem>
      </FForm>
    </FModal>
  </div>
</template>
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

import { onMounted, ref, reactive } from 'vue';
import { FMessage } from '@fesjs/fes-design';
import { useRoute } from 'vue-router';
import { request } from '@dataspherestudio/shared';
import { QuestionCircleOutlined } from '@fesjs/fes-design/icon';

const { t: $t } = useI18n();

export type WorkflowEiditFormType = {
  templateIds: string[];
  isDefaultReference: string | null;
  enabledFlowKeywordsCheck: string | null;
  starrocksconfig: any;
};
const route = useRoute();
const formData = ref<WorkflowEiditFormType>({
  templateIds: [],
  isDefaultReference: '0',
  enabledFlowKeywordsCheck: '0',
  starrocksconfig: [],
});
const curWorkPlaceId = ref('');

const saveEnableConfigData = async (key: string) => {
  const url = 'dss/framework/workspace/updateWorkspaceInfo';
  try {
    const params = {
      workspaceId: curWorkPlaceId.value,
      isDefaultReference: formData.value.isDefaultReference,
      enabledFlowKeywordsCheck: formData.value.enabledFlowKeywordsCheck,
    };
    await request.fetch(url, params, 'post');
    // if (key === 'isDefaultReference') {
    //   FMessage.success('新增节点是否默认引用资源参数模板保存成功');
    // }
    // if (key === 'enabledFlowKeywordsCheck') {
    //   FMessage.success('是否开启工作流关键字校验保存成功');
    // }
  } catch (err) {
    console.warn(err);
  }
};

const templateList = ref<any>([]);
const getTemplateData = async () => {
  try {
    const res = await request.fetch(
      'dss/framework/orchestrator/getWorkspaceTemplates',
      {
        workspaceId: curWorkPlaceId.value,
        pageNow: 1,
        pageSize: 10000,
      }
    );
    templateList.value = res.data.templates || [];
  } catch (err) {
    console.warn(err);
  }
};
const handleTemplateDisabled = (group: any, item: any) => {
  if (formData.value.templateIds.includes(item.templateId)) {
    return false;
  }
  return group.child.some((template) =>
    formData.value.templateIds.includes(template.templateId)
  );
};
const saveTemplateIds = async () => {
  try {
    const url = 'dss/framework/orchestrator/saveWorkspaceTemplateRef';
    const params = {
      workspaceId: curWorkPlaceId.value,
      templateIds: formData.value.templateIds,
    };
    await request.fetch(url, params, 'post');
    // FMessage.success('默认资源参数模板保存成功');
  } catch (err) {
    console.warn(err);
  }
};
const getDefaultTemplateData = async () => {
  try {
    const res: any = await request.fetch(
      'dss/framework/orchestrator/getWorkspaceDefaultTemplates',
      {},
      'get'
    );
    formData.value.templateIds =
      res.data.workspaceDefaultTemplates.map((item) => item.templateId) || [];
  } catch (err) {
    console.warn(err);
  }
};

const getWorkspaceInfoById = async (id) => {
  try {
    const res: any = await request.fetch(
      `dss/framework/workspace/workspaces/${id}`,
      {},
      'get'
    );
    formData.value.isDefaultReference =
      res?.data?.workspace?.isDefaultReference || '0';
    formData.value.enabledFlowKeywordsCheck =
      res?.data?.workspace?.enabledFlowKeywordsCheck || '0';
  } catch (err) {
    console.warn(err);
  }
};
const configItem = reactive({
  id: '',
  clusterName: '',
  httpPort: 0,
  clusterIp: '',
  tcpPort: 0,
  defaultCluster: '1',
});
const showModal = ref(false);
const configRef = ref(null);
const handleOk = async () => {
  await configRef.value?.validate();
  setStarrocksConf();
};

const formRule = ref({
  clusterName: [
    {
      required: true,
      message: $t('_.不能为空,最长256字符'),
      max: 256,
      trigger: ['blur'],
    },
  ],
  httpPort: [
    {
      type: 'number',
      required: true,
      message: $t('_.不能为空'),
      trigger: ['blur'],
    },
  ],
  clusterIp: [
    {
      type: 'string',
      required: true,
      message: $t('_.不能为空'),
      trigger: ['blur'],
    },
    {
      pattern:
        /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
      message: $t('_.IP地址格式不正确'),
    },
  ],
  tcpPort: [
    {
      type: 'number',
      required: true,
      message: $t('_.不能为空'),
      trigger: ['blur'],
    },
  ],
});
const formType = ref('add');
const handleBtnClick = (type: string, row: any) => {
  formType.value = type;
  if (type === 'add') {
    showModal.value = true;
    configItem.id = '';
    configItem.clusterName = '';
    configItem.clusterIp = '';
    configItem.httpPort = 0;
    configItem.tcpPort = 0;
    configItem.defaultCluster = '0';
  }
  if (type === 'del') {
    configItem.id = row.id;
    setStarrocksConf(type);
  }
  if (type === 'edit') {
    configItem.id = row.id;
    configItem.clusterName = row.clusterName;
    configItem.clusterIp = row.clusterIp;
    configItem.httpPort = +row.httpPort;
    configItem.tcpPort = +row.tcpPort;
    configItem.defaultCluster = row.defaultCluster;

    showModal.value = true;
  }
};
const getStarrocksConf = async () => {
  try {
    const url = 'dss/framework/workspace/getWorkspaceStarRocksCluster';
    const params = {
      workspaceId: curWorkPlaceId.value,
    };
    const { data } = await request.fetch(url, params, 'get');
    formData.value.starrocksconfig = data.dssWorkspaceStarRocksCluster.map(
      (item: any) => {
        return {
          id: item.id,
          clusterName: item.clusterName,
          httpPort: item.httpPort,
          clusterIp: item.clusterIp,
          tcpPort: item.tcpPort,
          defaultCluster: item.defaultCluster ? '1' : '0',
        };
      }
    );
  } catch (err) {
    console.warn(err);
  }
};

const setStarrocksConf = async (type?: string) => {
  try {
    const url = 'dss/framework/workspace/updateWorkspaceStarRocksCluster';
    let starRocksUpdateRequest: any[] = [];
    if (type === 'del') {
      starRocksUpdateRequest = [...formData.value.starrocksconfig].filter(
        (item) => item.id !== configItem.id
      );
    } else if (configItem.id) {
      // edit
      starRocksUpdateRequest = [...formData.value.starrocksconfig].map(
        (item) => {
          if (item.id === configItem.id) {
            return {
              clusterName: configItem.clusterName,
              httpPort: configItem.httpPort,
              clusterIp: configItem.clusterIp,
              tcpPort: configItem.tcpPort,
              defaultCluster: configItem.defaultCluster,
              workspaceId: curWorkPlaceId.value,
            };
          }
          return item;
        }
      );
    } else {
      // add
      starRocksUpdateRequest = [
        ...formData.value.starrocksconfig,
        { ...configItem },
      ];
    }
    starRocksUpdateRequest = starRocksUpdateRequest.map((item) => {
      return {
        clusterName: item.clusterName,
        httpPort: item.httpPort,
        clusterIp: item.clusterIp,
        tcpPort: item.tcpPort,
        defaultCluster: item.defaultCluster == '1',
        workspaceId: curWorkPlaceId.value,
      };
    });
    await request.fetch(
      url,
      {
        starRocksUpdateRequest,
      },
      'post'
    );
    showModal.value = false;
    getStarrocksConf();
  } catch (err) {
    console.warn(err);
  }
};
onMounted(async () => {
  curWorkPlaceId.value = route.query.workspaceId as string;
  getTemplateData();
  getDefaultTemplateData();
  getWorkspaceInfoById(curWorkPlaceId.value);
  getStarrocksConf();
});
</script>
<style scoped>
.inline-form {
  /* margin-left: 6px; */
}

.container {
  padding: 20px;
}

.title {
  margin-bottom: 20px;
}
</style>
