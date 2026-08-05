<template>
  <Modal
    v-model="innerVisible"
    :title="modalTitle"
    :width="600"
    :mask-closable="false"
    :closable="true"
    class-name="validation-result-modal"
    @on-cancel="handleCancel"
  >
    <div class="validation-desc">
      {{ $t('message.workflow.process.validation.errorDesc') }}
    </div>
    <div class="validation-issue-list">
      <div
        v-for="(issue, index) in issues"
        :key="(issue.nodeId || issue.edgeRef || '') + index"
        class="validation-issue-item"
      >
        <span class="issue-level-icon level-error">
          <Icon
            type="md-close-circle"
            :size="18"
          />
        </span>
        <div class="issue-detail">
          <div class="issue-header">
            <span class="issue-check-name">{{ issue.checkName || issue.ruleId }}</span>
            <span
              v-if="issueLocation(issue)"
              class="issue-location"
              :title="$t('message.workflow.process.validation.locateIssue')"
              @click="handleLocate(issue)"
            >
              <Icon type="md-locate" :size="12" />
              <span class="location-text">{{ issueLocation(issue) }}</span>
            </span>
          </div>
          <div class="issue-message" v-if="issue.message">{{ issue.message }}</div>
          <div class="issue-suggestion" v-if="issue.suggestion">
            <Icon type="md-bulb" :size="12" />
            <span>{{ issue.suggestion }}</span>
          </div>
        </div>
      </div>
      <div v-if="!issues || issues.length === 0" class="no-issues">
        {{ $t('message.workflow.process.validation.noIssues') }}
      </div>
    </div>
    <div slot="footer">
      <Button type="primary" @click="handleConfirm">
        {{ $t('message.workflow.process.validation.known') }}
      </Button>
    </div>
  </Modal>
</template>

<script>
export default {
  name: 'ValidationResultDialog',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    issues: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        if (!val) {
          this.$emit('update:visible', false);
          this.$emit('on-close');
        }
      },
    },
    modalTitle() {
      return this.$t('message.workflow.process.validation.errorTitle');
    },
  },
  methods: {
    issueLocation(issue) {
      if (issue.nodeId) return issue.nodeId;
      if (issue.edgeRef) return issue.edgeRef;
      return '';
    },
    handleLocate(issue) {
      this.$emit('on-locate', issue);
    },
    handleConfirm() {
      this.innerVisible = false;
    },
    handleCancel() {
      this.innerVisible = false;
      this.$emit('on-close');
    },
  },
};
</script>

<style lang="scss" scoped>
.validation-result-modal {
  .validation-desc {
    color: #ed4014;
    font-size: 13px;
    margin-bottom: 12px;
    padding: 8px 12px;
    background-color: #fef0f0;
    border-radius: 4px;
    border: 1px solid #fde2e2;
  }
  .validation-issue-list {
    max-height: 360px;
    overflow-y: auto;
    .validation-issue-item {
      display: flex;
      align-items: flex-start;
      padding: 10px 12px;
      margin-bottom: 8px;
      border-radius: 4px;
      border: 1px solid #e8eaec;
      background-color: #fafafa;
      transition: background-color 0.2s;
      &:hover {
        background-color: #f0f0f0;
      }
      .issue-level-icon {
        flex-shrink: 0;
        margin-right: 10px;
        margin-top: 1px;
        &.level-error {
          color: #ff4d4f;
        }
      }
      .issue-detail {
        flex: 1;
        min-width: 0;
        .issue-header {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          margin-bottom: 4px;
          .issue-check-name {
            font-weight: 600;
            font-size: 13px;
            color: #17233d;
            margin-right: 8px;
          }
          .issue-location {
            display: inline-flex;
            align-items: center;
            gap: 2px;
            font-size: 12px;
            color: #2d8cf0;
            cursor: pointer;
            padding: 2px 6px;
            border-radius: 3px;
            background-color: #e8f4ff;
            transition: background-color 0.2s;
            .location-text {
              font-family: 'Courier New', monospace;
            }
            &:hover {
              background-color: #d0e8ff;
            }
          }
        }
        .issue-message {
          font-size: 12px;
          color: #515a6e;
          margin-bottom: 4px;
          line-height: 1.5;
          word-break: break-all;
        }
        .issue-suggestion {
          display: flex;
          align-items: flex-start;
          gap: 4px;
          font-size: 12px;
          color: #808695;
          line-height: 1.5;
          word-break: break-all;
        }
      }
    }
    .no-issues {
      text-align: center;
      color: #808695;
      padding: 40px 0;
    }
  }
}
</style>
