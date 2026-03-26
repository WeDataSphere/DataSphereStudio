<template>
  <FDrawer
    v-model:show="showModal"
    :title="$t('_.批量提交')"
    width="80%"
    :mask-closable="false"
    display-directive="if"
    footer
    @cancel="handleCancel"
  >
    <div class="compare-wrapper">
      <div class="compare-title">
        <span>{{ $t('_.版本对比') }}</span>
      </div>
      <div class="compare-diff">
        <div class="diff-tabs">
          <FTabs
            v-model="activeTab"
            type="card"
            closable
            @close="handleCloseTab"
            @change="handleToggle(false)"
          >
            <FTabPane
              v-for="(tab, index) in tabs"
              :key="tab.orchestratorId"
              :name="tab.orchestratorName"
              closable
              :value="index"
              :disabled="
                isLoading &&
                activeTabValue.orchestratorId !== tab.orchestratorId
              "
            />
          </FTabs>
        </div>

        <div v-if="!isExpand">
          <FButton
            v-if="activeTabValue.associateGit"
            type="link"
            :loading="isLoading"
            @click="handleToggle(true)"
          >
            {{ $t('_.查看版本对比') }}
          </FButton>
          <div v-else class="diff-btn-tips">
            {{ $t('_.未关联Git项目, 无版本比对') }}
          </div>
        </div>
        <template v-else>
          <div class="diff-tabs">
            <FTabs v-model="diffContentType" @change="pageStateReset('editor')">
              <FTabPane :name="$t('_.代码')" value="code" />
              <FTabPane :name="$t('_.元数据')" value="meta" />
            </FTabs>
          </div>
          <template v-if="currentTree.tree && currentTree.tree.length > 0">
            <!-- 树状菜单 -->
            <div class="diff-tree">
              <FTree
                ref="treeRef"
                :key="diffContentType"
                default-expand-all
                :data="currentTree.tree"
                :cancelable="false"
                @select="handleTreeSelect"
              />
            </div>

            <div v-if="isDiffLoading" class="diff-loading">
              <FSpin stroke="#5384ff" />{{ $t('_.加载中...') }}
            </div>

            <!-- 代码比对 -->
            <div v-if="compareDetail.visible" class="diff-editor">
              <div class="editor-title">
                <template v-if="compareDetail.beforeCommitId">
                  <div class="left">
                    <PasswordOutlined />
                    {{ compareDetail.beforeCommitId }}
                  </div>
                  <div class="middle">VS</div>
                </template>
                <div
                  :class="[
                    'right',
                    !compareDetail.beforeCommitId ? 'left-style' : '',
                  ]"
                >
                  {{ $t('_.当前版本') }}
                </div>
              </div>
              <WeEditorCompare
                :key="diffContentType"
                :style="'height:' + 300"
                :value="compareDetail.after"
                :original="compareDetail.before"
                :diff-editor="!!compareDetail.beforeCommitId"
                :read-only="true"
              />
            </div>
          </template>
          <div v-else-if="currentTree.tips" style="color: #f29360">
            {{ currentTree.tips }}
          </div>
        </template>
      </div>
      <div class="compare-content">
        <FForm
          ref="formRef"
          label-position="top"
          :model="formData"
          :rules="formRules"
        >
          <FFormItem :label="$t('_.注解')" prop="comment">
            <FInput
              v-model="formData.comment"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 5 }"
              :placeholder="$t('_.请输入注解')"
            />
          </FFormItem>
        </FForm>
      </div>
    </div>
    <template #footer>
      <FSpace justify="start">
        <FButton type="primary" :loading="btnLoading" @click="handleOk">
          {{ $t('_.确认') }}
        </FButton>
        <FButton @click="handleCancel">
          {{ $t('_.取消') }}
        </FButton>
      </FSpace>
    </template>
  </FDrawer>
</template>

<script lang="ts" setup name="batchSubmit">
import { useI18n } from 'vue-i18n';

import { ref, computed, watch } from 'vue';
import { FMessage, FTree, FForm } from '@fesjs/fes-design';
import { PasswordOutlined } from '@fesjs/fes-design/icon';
import type { FlowDiffParamType } from '../types/index';
import WeEditorCompare from '../../components/compare.vue';
import type { BaseType } from '../types/index';
import api from '../api';

const { t: $t } = useI18n();

const emits = defineEmits<{
  (e: 'update:show', val: { batchSubmit: boolean }): void;
  (e: 'success'): void;
  (e: 'cancel', val: string): void;
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

const formData = ref({
  comment: '',
});

const formRules = {
  comment: [
    {
      required: true,
      message: $t('_.请输入注解'),
    },
  ],
};

const tabs = ref<Array<BaseType>>([]);
const activeTab = ref(0);
const activeTabValue = computed(() => tabs.value[activeTab.value]);

const handleCloseTab = (key: number) => {
  if (tabs.value.length <= 1) {
    FMessage.warning($t('_.至少需要提交一个工作流'));
    return;
  }
  tabs.value.splice(key, 1);
  if (activeTab.value === key) {
    activeTab.value =
      key >= tabs.value.length - 1 ? tabs.value.length - 1 : key;
    handleToggle(false);
  } else if (activeTab.value > key) {
    activeTab.value = activeTab.value - 1;
  }
};

const buildTree = (data: BaseType[], level = 1): BaseType[] | undefined => {
  if (!data || ['[]', '{}'].includes(JSON.stringify(data))) return undefined;
  let values = data;
  if (data && String(data) === '[object Object]') {
    values = Object.values(data);
  }
  return values.map((item, index) => {
    return {
      label: item.name,
      value: item.name + '#' + level + '#' + index,
      absolutePath: item.absolutePath,
      children: buildTree(item.children, level + 1),
      prefix: null,
      suffix: null,
    };
  });
};

let timeout = ref<number | null>(null);
const delay = (time: number) => {
  if (timeout.value) {
    window.clearTimeout(timeout.value as number);
  }
  return new Promise((resolve) => {
    setTimeout(resolve, time);
  });
};

const isLoading = ref(false);
const flowFileTree = ref<BaseType>({
  code: [],
  meta: [],
  codeEmptyTips: '',
  metaEmptyTips: '',
});
const diffContentType = ref('code');
const currentTree = computed(() => ({
  tree: flowFileTree.value[diffContentType.value],
  tips: flowFileTree.value[diffContentType.value + 'EmptyTips'],
}));

type FetchDiffTreeType = Pick<
  FlowDiffParamType,
  'projectName' | 'orchestratorId' | 'labels'
>;
const fetchDiffTree = async (taskId?: string | null, cb?: () => void) => {
  try {
    isLoading.value = true;
    let status = 'init';
    if (taskId) {
      const rst = await api.GetDiffFlowSubmitStatusApi({ taskId });
      status = rst.data.status.toLowerCase();
      if (status === 'running') {
        await delay(2000);
        fetchDiffTree(taskId, cb);
      } else if (status === 'failed') {
        isLoading.value = false;
        FMessage.warning(rst.data.errMsg || $t('_.获取差异化目录失败'));
        return;
      }
    }
    if (status === 'init') {
      const param = {
        projectName: activeTabValue.value.projectName,
        orchestratorId: activeTabValue.value.orchestratorId,
        labels: { route: 'dev' },
      };
      const task = await api.SubmitInitDiffFlowApi<FetchDiffTreeType>(param);
      fetchDiffTree(task.data.taskId, cb);
    } else if (status === 'success') {
      const res = await api.GetDiffFlowTreeApi<{ taskId: string }>({
        taskId: taskId as string,
      });
      const { codeTree, metaTree } = res.data.tree || {};
      flowFileTree.value = {
        code: buildTree(codeTree || []) as BaseType[],
        meta: buildTree(metaTree || []) as BaseType[],
        codeEmptyTips:
          !codeTree && $t('_.当前工作流代码未发生变化，请保存改动后重试'),
        metaEmptyTips:
          !metaTree && $t('_.当前工作流元数据未发生变化，请保存改动后重试'),
      };
      isLoading.value = false;
      cb && cb();
    }
  } catch (error) {
    isLoading.value = false;
    console.log('get tree error', error);
  }
};

const compareDetail = ref({
  visible: false,
  before: '',
  after: '',
  beforeCommitId: '',
});

type FetchDiffContentType = Pick<
  FlowDiffParamType,
  'projectName' | 'orchestratorId' | 'labels' | 'filePath' | 'publish'
>;
const isDiffLoading = ref(false);
const fetchDiffContent = async (filePath: string) => {
  try {
    isDiffLoading.value = true;
    compareDetail.value.visible = false;
    const param = {
      projectName: activeTabValue.value.projectName,
      orchestratorId: activeTabValue.value.orchestratorId,
      labels: { route: 'dev' },
      filePath,
      publish: false,
    };
    const res = await api.GetDiffFlowCotentApi<FetchDiffContentType>(param);
    compareDetail.value = {
      visible: true,
      before: res.data.content.before || '',
      after: res.data.content.after || '',
      beforeCommitId: res.data.content.beforeCommitId || '',
    };
    isDiffLoading.value = false;
  } catch (error) {
    isDiffLoading.value = false;
    console.log('get content error', error);
  }
};

// 点击树形结构
const treeRef = ref<typeof FTree | null>(null);
const absolutePath = ref<string>('');
function handleTreeSelect({ node }: BaseType) {
  if (!node.hasChildren) {
    absolutePath.value = node.origin.absolutePath;
    fetchDiffContent(node.origin.absolutePath);
  } else {
    treeRef.value?.expandNode(node.value);
  }
}

const isExpand = ref(false);
const pageStateReset = (type: string) => {
  if (type === 'page') {
    activeTab.value = 0;
    tabs.value = [];
    formData.value = {
      comment: '',
    };
  }
  if (['page', 'tab'].includes(type)) {
    diffContentType.value = 'code';
    flowFileTree.value = {
      code: [],
      meta: [],
      codeEmptyTips: '',
      metaEmptyTips: '',
    };
    isExpand.value = false;
  }
  compareDetail.value = {
    visible: false,
    before: '',
    after: '',
    beforeCommitId: '',
  };
  isDiffLoading.value = false;
};

const handleToggle = (expand = false) => {
  if (expand) {
    fetchDiffTree(null, () => {
      isExpand.value = true;
    });
  } else {
    pageStateReset('tab');
  }
};

type BatchSubmitDiffFlowCotentType = Pick<
  FlowDiffParamType,
  'labels' | 'comment' | 'submitRequestList'
>;
const btnLoading = ref(false);
const formRef = ref<typeof FForm | null>(null);
const handleOk = async () => {
  try {
    await formRef.value?.validate();
    const param = {
      labels: { route: 'dev' },
      comment: formData.value.comment,
      submitRequestList: tabs.value.map((item) => ({
        orchestratorId: item.orchestratorId,
        projectName: activeTabValue.value.projectName,
        labels: { route: 'dev' },
        comment: formData.value.comment,
      })),
    };
    btnLoading.value = true;
    await api.BatchSubmitDiffFlowCotentApi<BatchSubmitDiffFlowCotentType>(
      param
    );
    btnLoading.value = false;
    FMessage.success($t('_.批量提交成功!'));
    emits('success');
    showModal.value = false;
  } catch (error) {
    btnLoading.value = false;
    console.log('batch submit error', error);
  }
};

const handleCancel = () => {
  showModal.value = false;
  emits('cancel', 'prepare');
};

const showModal = computed({
  get() {
    return !!props.show?.batchSubmit;
  },
  set(val) {
    emits('update:show', { batchSubmit: val });
  },
});

watch(
  () => showModal.value,
  (show) => {
    if (show) {
      pageStateReset('page');
      tabs.value = [...props.form.rows];
    }
  },
  { immediate: true }
);
</script>

<style lang="less" scoped>
.compare-wrapper {
  .compare-title {
    margin-bottom: 16px;
    color: #0f1222;
  }

  .compare-diff {
    border: 1px solid #e7e7e9;
    border-radius: 4px;
    padding: 16px 16px;
    margin-bottom: 16px;

    .diff-btn-tips {
      color: #f29360;
      line-height: 32px;
      padding: 0 16px;
    }

    .diff-tabs {
      margin-bottom: 16px;

      :deep(.fes-tabs-tab-pane-wrapper) {
        display: none;
      }

      :deep(.fes-tabs-nav-scroll-content) {
        margin-bottom: 2px;
      }
    }

    .diff-tree {
      background: #f8f8f8;
      padding: 8px 16px;
      margin-top: 8px;
      max-height: 300px;
      overflow-y: auto;
    }

    .diff-loading {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #5384ff;
      margin-top: 16px;
    }

    .diff-editor {
      border: 1px solid #e7e7e9;
      padding-top: 16px;
      margin-top: 16px;
      border-radius: 4px;

      .editor-title {
        display: flex;
        margin-bottom: 16px;
        text-align: center;

        .left {
          flex: 1;
          text-align: right;
          :deep(.fes-design-icon) {
            margin-top: 4px;
            vertical-align: top;
          }
        }

        .right {
          flex: 1;
          text-align: left;
          &.left-style {
            margin-left: 16px;
          }
        }

        .middle {
          width: 32px;
          flex: 0 0 32px;
          background: #5384ff;
          color: #fff;
          text-align: center;
          border-radius: 4px;
          margin: 0 32px;
        }
      }
    }
  }

  .compare-content {
    margin-bottom: 16px;
  }
}
</style>
