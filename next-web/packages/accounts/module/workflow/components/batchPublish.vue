<template>
  <FDrawer
    v-model:show="showModal"
    :title="$t('_.批量发布')"
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

            <div v-if="compareDetail.visible" class="diff-editor">
              <div class="editor-title">
                <div class="left">
                  <PasswordOutlined />
                  {{ compareDetail.beforeCommitId }}
                </div>
                <div class="middle">VS</div>
                <div class="right">
                  {{ $t('_.当前版本(') }}{{ compareDetail.afterCommitId }})
                </div>
              </div>
              <WeEditorCompare
                :key="
                  diffContentType +
                  compareDetail.beforeCommitId +
                  compareDetail.afterCommitId
                "
                :style="'height:' + 300"
                :value="compareDetail.after"
                :original="compareDetail.before"
                :diff-editor="true"
                :read-only="true"
              />
            </div>
          </template>
          <div v-else-if="currentTree.tips" style="color: #f29360">
            {{ currentTree.tips }}
          </div>
        </template>
        <div v-if="compareDetail.visible" class="diff-commit">
          <div class="commit-left">
            {{ compareDetail.beforeAnnotate }}
          </div>
          <div class="commit-middle" />
          <div class="commit-right">
            {{ compareDetail.afterAnnotate }}
          </div>
        </div>
      </div>
      <div class="compare-content">
        <FForm ref="formRef" label-position="top" :model="formData">
          <FFormItem :label="$t('_.发布描述')" prop="comment">
            <FInput
              v-model="formData.comment"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 5 }"
              :placeholder="$t('_.请输入发布描述')"
            />
          </FFormItem>
        </FForm>
      </div>
      <div class="compare-title">
        <span>{{ $t('_.提交记录') }}</span>
      </div>
      <div class="compare-table">
        <FTable ref="tableRef" :data="tableList">
          <FTable-column
            :label="$t('_.提交ID')"
            prop="commitId"
            ellipsis
            :width="116"
            :formatter="fillText"
          />
          <FTable-column
            :label="$t('_.提交时间')"
            prop="commitTime"
            ellipsis
            :width="116"
            :formatter="fillTimeText"
          />
          <FTable-column
            :label="$t('_.提交人')"
            prop="commitUser"
            ellipsis
            :width="116"
            :formatter="fillText"
          />
          <FTable-column
            :label="$t('_.注释')"
            prop="comment"
            ellipsis
            :width="116"
            :formatter="fillText"
          />
        </FTable>
      </div>
    </div>
    <template #footer>
      <FSpace justify="start">
        <FButton type="primary" :loading="btnLoading" @click="handleKeyValid">
          {{ $t('_.确认') }}
        </FButton>
        <FButton @click="handleCancel">
          {{ $t('_.取消') }}
        </FButton>
      </FSpace>
    </template>
    <FModal
      v-model:show="checkKeyShow"
      :title="$t('_.节点关键字检查')"
      :ok-text="$t('_.继续发布')"
      :cancel-text="$t('_.返回修改')"
      :max-height="500"
      @ok="handleOk"
    >
      <div v-for="(obj, key) in checkKeyData" :key="key">
        <div
          v-if="
            obj.notContainsKeywordsNodeList &&
            obj.notContainsKeywordsNodeList.length > 0
          "
        >
          <p class="ellipse-p">
            {{ $t('_.项目') }}{{ obj.projectName }}{{ $t('_.下的工作流')
            }}{{ obj.orchestratorName }}{{ $t('_.中节点：') }}
          </p>
          <p class="ellipse-p">
            {{ obj.notContainsKeywordsNodeList.join(',') }}
          </p>
        </div>
      </div>
      <div>{{ $t('_.不包含关键字insert或create table') }}</div>
    </FModal>
  </FDrawer>
</template>

<script lang="ts" setup name="batchPublish">
import { useI18n } from 'vue-i18n';

import { ref, computed, watch } from 'vue';
import { FForm, FMessage, FTree, FModal } from '@fesjs/fes-design';
import { PasswordOutlined } from '@fesjs/fes-design/icon';
import type { FlowDiffParamType } from '../types/index';
import WeEditorCompare from '../../components/compare.vue';
import { usePagination } from '../../hooks/usePagination';
import type { BaseType } from '../types/index';
import api from '../api';

const { t: $t } = useI18n();

const emits = defineEmits<{
  (e: 'update:show', val: { batchPublish: boolean }): void;
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
const tabs = ref<Array<BaseType>>([]);
const activeTab = ref(0);
const activeTabValue = computed(() => tabs.value[activeTab.value]);

const handleCloseTab = (key: number) => {
  if (tabs.value.length <= 1) {
    FMessage.warning($t('_.至少需要发布一个工作流'));
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

const isLoading = ref(false);
const flowFileTree = ref<BaseType>({
  commitId: '',
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

const fetchDiffTree = async (cb?: () => void) => {
  try {
    isLoading.value = true;
    const param = {
      projectName: activeTabValue.value.projectName,
      orchestratorId: activeTabValue.value.orchestratorId,
      labels: { route: 'dev' },
    };
    const res = await api.PublishInitDiffFlowApi<FetchDiffTreeType>(param);
    const { codeTree, metaTree } = res.data.tree || {};
    flowFileTree.value = {
      code: buildTree(codeTree || []) as BaseType[],
      meta: buildTree(metaTree || []) as BaseType[],
      codeEmptyTips:
        !codeTree &&
        $t('_.当前工作流代码发布后的版本与上一版完全相同，没有任何变化'),
      metaEmptyTips:
        !metaTree &&
        $t('_.当前工作流元数据发布后的版本与上一版完全相同，没有任何变化'),
    };
    isLoading.value = false;
    cb && cb();
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
  afterCommitId: '',
  beforeAnnotate: '',
  afterAnnotate: '',
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
      publish: true,
    };
    const res = await api.GetDiffFlowCotentApi<FetchDiffContentType>(param);
    compareDetail.value = {
      visible: true,
      before: res.data.content.before || '',
      after: res.data.content.after || '',
      beforeCommitId: res.data.content.beforeCommitId || '',
      afterCommitId: res.data.content.afterCommitId || '',
      beforeAnnotate: res.data.content.beforeAnnotate || '',
      afterAnnotate: res.data.content.afterAnnotate || '',
    };
    isDiffLoading.value = false;
  } catch (error) {
    isDiffLoading.value = false;
    console.log('get content error', error);
  }
};

// 点击树形结构
const treeRef = ref<typeof FTree | null>(null);
function handleTreeSelect({ node }: BaseType) {
  if (!node.hasChildren) {
    fetchDiffContent(node.origin.absolutePath);
  } else {
    treeRef.value?.expandNode(node.value);
  }
}

const tableList = ref([]);
const loadTable = async () => {
  try {
    if (!activeTabValue.value.associateGit) return;
    const param = {
      projectName: activeTabValue.value.projectName,
      orchestratorId: activeTabValue.value.orchestratorId,
      workspaceId: props.config.workspaceId,
    };
    const rst = await api.GetSubmitHistory<
      Pick<FlowDiffParamType, 'projectName' | 'orchestratorId' | 'workspaceId'>
    >(param);
    tableList.value = rst.data.history.responses || [];
  } catch (error) {
    console.log('get submit history error', error);
  }
};

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
      commitId: '',
      code: [],
      meta: [],
      codeEmptyTips: '',
      metaEmptyTips: '',
    };
    isExpand.value = false;
    tableList.value = [];
  }
  compareDetail.value = {
    visible: false,
    before: '',
    after: '',
    beforeCommitId: '',
    afterCommitId: '',
    beforeAnnotate: '',
    afterAnnotate: '',
  };
  isDiffLoading.value = false;
};

const handleToggle = (expand = false) => {
  if (expand) {
    fetchDiffTree(() => {
      isExpand.value = true;
    });
  } else {
    pageStateReset('tab');
    loadTable();
  }
};

type BatchPublishDiffFlowCotentType = Pick<
  FlowDiffParamType,
  'labels' | 'comment' | 'orchestratorList' | 'dssLabel'
>;

const btnLoading = ref(false);
const checkKeyShow = ref(false);
const checkKeyData = ref([]);
const handleKeyValid = async (key: string) => {
  try {
    const params = {
      orchestratorIdList: tabs.value.map((item) => item.orchestratorId),
    };
    const rst = await api.BatchPublishFlowCheckApi(params);
    if (rst && rst.data && rst.data.data && rst.data.data.length > 0) {
      const result = !rst.data.data.every((item) => {
        return (
          item.notContainsKeywordsNodeList === null ||
          (Array.isArray(item.notContainsKeywordsNodeList) &&
            item.notContainsKeywordsNodeList.length === 0)
        );
      });
      if (result) {
        checkKeyData.value = rst.data.data;
        checkKeyShow.value = true;
        return;
      }
    }
    handleOk();
  } catch (error) {
    console.log('handleKeyValid error', error);
  }
};
const handleOk = async () => {
  try {
    const proxyUsers = tabs.value.map((item) => item.proxyUser).filter(Boolean);
    if (proxyUsers && proxyUsers.length) {
      const rst = await api.ProxyUserDismissedApi({ usernames: proxyUsers });
      const disMissedProxyUsers: string[] = [];
      (rst.data.isDismissed || []).forEach((item: BaseType) => {
        const [user, disMissed] = Object.entries(item)[0];
        if (disMissed) {
          disMissedProxyUsers.push(user);
        }
      });
      if (disMissedProxyUsers.length) {
        const disMissedFlows = tabs.value
          .filter(
            (item) =>
              item.proxyUser && disMissedProxyUsers.includes(item.proxyUser)
          )
          .map((item) => item.orchestratorName);
        FMessage.warning(
          `${disMissedFlows.join('、')}${$t(
            '_.工作流的代理用户已离职或不存在，请确认是否修改代理用户'
          )}`
        );
        return;
      }
    }
    const param = {
      orchestratorList: tabs.value.map((item) => item.orchestratorId),
      labels: { route: 'dev' },
      comment: formData.value.comment,
      dssLabel: 'dev',
    };
    btnLoading.value = true;
    await api.BatchPublishDiffFlowCotentApi<BatchPublishDiffFlowCotentType>(
      param
    );
    btnLoading.value = false;
    FMessage.success($t('_.批量发布提交成功!'));
    emits('success');
    showModal.value = false;
    checkKeyShow.value = false;
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
    return !!props.show?.batchPublish;
  },
  set(val) {
    emits('update:show', { batchPublish: val });
  },
});

watch(
  () => showModal.value,
  (show) => {
    if (show) {
      pageStateReset('page');
      tabs.value = [...props.form.rows];
      loadTable();
    }
  },
  { immediate: true }
);

const { fillText, fillTimeText } = usePagination();
</script>

<style lang="less" scoped>
.ellipse-p {
  word-break: break-word;
  overflow-wrap: break-word;
}
.compare-wrapper {
  .compare-title {
    margin-bottom: 16px;
    color: #0f1222;
  }

  .compare-diff {
    border: 1px solid #e7e7e9;
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

    .diff-commit {
      background: #f8f8f8;
      margin-top: 16px;
      display: flex;

      .commit-left,
      .commit-right {
        padding: 0 16px;
        min-height: 32px;
        line-height: 32px;
        word-break: break-all;
        flex: 1;
      }

      .commit-middle {
        flex: 0 0 8px;
        background: #fff;
      }
    }
  }

  .compare-content {
    margin-bottom: 16px;
  }
}
</style>
