<template>
  <div
    ref="bottomPanel"
    class="log-panel"
    :class="{'full-screen': scriptViewState.bottomPanelFull}"
  >
    <div class="workbench-tabs">
      <div class="workbench-tab-wrapper">
        <div class="workbench-tab">
          <div
            :class="{active: scriptViewState.showPanel == 'progress'}"
            class="workbench-tab-item"
            @click="showPanelTab('progress')">
            <span>{{ $t('message.workflow.tabs.progress') }}</span>
          </div>
          <div
            v-if="script.result"
            :class="{active: scriptViewState.showPanel == 'result'}"
            class="workbench-tab-item"
            @click="showPanelTab('result')">
            <span>{{ $t('message.workflow.tabs.result') }}</span>
          </div>
          <div
            v-if="isLogShow"
            :class="{active: scriptViewState.showPanel == 'log'}"
            class="workbench-tab-item"
            @click="showPanelTab('log')">
            <span>{{ $t('message.workflow.tabs.log') }}</span>
          </div>
        </div>
        <div v-if="scriptViewState.showPanel == 'result' && scriptResult.totalColumn <= 10000 && scriptResult.totalColumn > 500" class="column-pagination">
          <span>列分页：500列 / 页，共{{ scriptResult.totalColumn }}列</span>
          <Page
            :total="scriptResult.totalColumn"
            :current="scriptViewState.columnPageNow || 1"
            :page-size="500"
            :simple="true"
            size="small"
            @on-change="onChangeColPage"
          />
        </div>
        <span class="workbench-tab-full-btn" @click="toggleFull">
          <Icon :type="scriptViewState.bottomPanelFull?'md-contract':'md-expand'" />
          {{ scriptViewState.bottomPanelFull ? $t('message.scripts.constants.logPanelList.releaseFullScreen') : $t('message.scripts.constants.logPanelList.fullScreen') }}
        </span>
        <Icon
          style="cursor:pointer;padding-top: 4px;margin-right: 4px;"
          type="ios-close"
          size="20"
          @click="closeConsole"></Icon>
      </div>
      <div
        class="workbench-container">
        <we-progress
          v-if="scriptViewState.showPanel == 'progress'"
          :script="script"
          :execute="execute"
          @open-panel="openPanel"
          :script-view-state="scriptViewState"/>
        <result
          v-if="scriptViewState.showPanel == 'result'"
          ref="result"
          getResultUrl="filesystem"
          :result="script.result"
          :script="script"
          :dispatch="dispatch"
          :script-view-state="scriptViewState"
          @on-set-change="changeResultSet"/>
        <log
          v-if="scriptViewState.showPanel == 'log'"
          :logs="script.log"
          :log-line="script.logLine"
          :script-view-state="scriptViewState"/>
      </div>
    </div>
  </div>
</template>
<script>
import api from '@dataspherestudio/shared/common/service/api';
import util from '@dataspherestudio/shared/common/util';
import storage from '@dataspherestudio/shared/common/helper/storage';
import {debounce, isUndefined, isEmpty, last} from 'lodash';
import {Script} from '../modal.js';
import result from '@dataspherestudio/shared/components/consoleComponent/result.vue';
import log from '@dataspherestudio/shared/components/consoleComponent/log.vue';
import weProgress from '@dataspherestudio/shared/components/consoleComponent/progress.vue';
import Execute from '@dataspherestudio/shared/common/service/execute';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import { EXECUTE_COMPLETE_TYPE } from '@dataspherestudio/shared/common/config/const';

export default {
  components: {
    result,
    log,
    weProgress,
  },
  mixins: [mixin],
  props: {
    node: {
      type: Object,
    },
    stop: {
      type: Boolean,
    },
    height: Number
  },
  data() {
    return {
      execute: null,
      scriptViewState: {
        showPanel: 'progress',
        cacheLogScroll: 0,
        bottomContentHeight: this.height || '250',
        bottomPanelFull: false,
        columnPageNow: 1,
      },
      isBottomPanelFull: false,
      isLogShow: false,
      localLog: {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      },
      script: {
        result: {},
        steps: [],
        progress: {},
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
        resultList: null,
      },
    }
  },
  beforeDestroy() {
    if (this.execute) {
      this.killExecute();
    }
  },
  computed: {
    scriptResult() {
      let res = {
        headRows: [],
        bodyRows: [],
        type: 'EMPTY',
        total: 0,
        totalColumn: 0,
        columnPageNow: 1,
        path: ''
      };
      if (this.script.resultList && this.script.resultList.length) {
        res = this.script.resultList[this.script.resultSet || 0].result || res
      }
      return res
    },
  },
  watch: {
    stop(val, oldval) {
      if (oldval && this.execute) {
        this.killExecute();
      }
    },
    height(val) {
      this.scriptViewState.bottomContentHeight = val - 5
    }
  },
  mounted() {
  },
  methods: {
    onChangeColPage(v) {
      this.scriptViewState = {...this.scriptViewState, columnPageNow: v}
    },
    killExecute() {
      this.execute.trigger('stop');
      this.execute.trigger('kill');
    },
    createScript() {
      const node = this.node;
      this.script = new Script({
        nodeId: node.key,
        title: node.title,
        scriptViewState: this.scriptViewState,
      });
      this.dispatch('workflowIndexedDB:addNodeCache', {
        nodeId: node.key,
        value: this.script,
      });
    },
    createExecute(needQuery) {
      const data = {
        method: '/api/rest_j/v1/entrance/execute',
        websocketTag: this.node.id,
        data: {
          executeApplicationName: null,
          executionCode: null,
          runType: 'node',
          params: null,
          postType: 'http',
          source: {
            scriptPath: null,
          },
          fromLine: this.script.logLine,
        },
      }
      this.execute = new Execute(data);
      if (needQuery) {
        this.queryState();
      }
    },
    queryState(monitor = true) {
      if (this.node.runState.execID) {
        const option = {
          taskID: this.node.runState.taskID,
          execID: this.node.runState.execID,
          isRestore: true,
          nodeId: this.node.key,
          openLog: true
        }
        if (monitor) {
          this.monitoringData();
        } else {
          if (this.script.progress.current >= 1) {
            return
          }
        }
        this.execute.halfExecute(option);
      }
    },
    monitoringData() {
      this.execute.on('log', (logs) => {
        this.localLog = this.convertLogs(logs);
        this.script.log = this.localLog.log;
        if (this.scriptViewState.showPanel === 'log') {
          this.localLogShow();
        }
        this.updateNodeCache(['log']);
      });
      this.execute.on('result', (ret) => {
        this.showPanelTab('result');
        const storeResult = {
          'headRows': ret.metadata,
          'bodyRows': ret.fileContent,
          'total': ret.totalLine,
          'type': ret.type,
          'totalColumn': ret.totalColumn,
          'cache': {
            offsetX: 0,
            offsetY: 0,
          },
          'path': this.execute.currentResultPath,
          'current': 1,
          'size': 20,
          hugeData: !!ret.hugeData,
          tipMsg: ret.tipMsg
        };
        if (this.execute.resultList[0]) {
          this.$set(this.execute.resultList[0], 'result', storeResult);
        }
        this.$set(this.script, 'resultSet', 0);
        this.script.result = storeResult;
        this.script.resultList = this.execute.resultList;
        this.script.resultSet = 0;
        this.updateNodeCache(['result', 'resultList', 'resultSet']);
        // 获取过progress的情况下设置为1
        if (this.script.progress.current) {
          this.script.progress.current = 1;
          this.updateNodeCache('progress');
        }
      });
      this.execute.on('progress', ({ progress, progressInfo, waitingSize }) => {
        // 这里progressInfo可能只是个空数组，或者数据第一个数据是一个空对象
        if (progressInfo.length && !isEmpty(progressInfo[0])) {
          progressInfo.forEach((newProgress) => {
            let newId = newProgress.id;
            let index = this.script.progress.progressInfo.findIndex((progress) => {
              return progress.id === newId;
            });
            if (index !== -1) {
              this.script.progress.progressInfo.splice(index, 1, newProgress);
            } else {
              this.script.progress.progressInfo.push(newProgress);
            }
          });
        } else {
          this.script.progress.progressInfo = [];
        }

        if (progress == 1) {
          let runningScripts = storage.get(this._running_scripts_key, 'local') || {};
          delete runningScripts[this.script.nodeId];
          storage.set(this._running_scripts_key, runningScripts, 'local');
        }

        this.script.progress.current = progress;

        if (waitingSize !== null && waitingSize >= 0) {
          this.script.progress.waitingSize = waitingSize;
        }
        this.updateNodeCache('progress');
      });
      this.execute.on('steps', (status) => {
        if (this.executeLastStatus && this.executeLastStatus[this.node.runState.taskID] === status) {
          return;
        }
        this.executeLastStatus = {
          [this.node.runState.taskID]: status
        };
        if (status === 'Inited') {
          this.script.steps = ['Submitted', 'Inited'];
        } else {
          const lastStep = last(this.script.steps);
          if (this.script.steps.indexOf(status) === -1) {
            this.script.steps.push(status);
            // 针对可能有WaitForRetry状态后，后台会重新推送Scheduled或running状态的时候
          } else if (lastStep !== status) {
            this.script.steps.push(status);
          }
          this.updateNodeCache(['steps']);
        }
        if (status !== 'Inited' && this.script.steps.length === 1) {
          this.script.steps.unshift('ellipsis');
        }
      });
      this.execute.on('status', (status) => {
        this.script.status = status;
        this.updateNodeCache('status');
      });
      this.execute.on('stateEnd', () => {
        this.updateNodeCache('log');
      });
      this.execute.on('error', () => {
        this.updateNodeCache('log');
      });
      this.execute.on('querySuccess', ({ type, task }) => {
        const costTime = util.convertTimestamp(task.costTime);
        this.script.progress = {
          ...this.script.progress,
          costTime
        };
        const name = this.script.title;
        this.$Notice.close(name);
        this.$Notice.success({
          title: this.$root.$t('message.workflow.notice.querySuccess.title'),
          desc: '',
          render: (h) => {
            return h('span', {
              style: {
                'word-break': 'break-all',
                'line-height': '20px',
              },
            }, `${this.script.title} ${type}${this.$root.$t('message.workflow.notice.querySuccess.render')}：${costTime}！`);
          },
          name,
          duration: 3,
        });
      });
      this.execute.on('notice', ({ type, msg, autoJoin }) => {
        const name = this.script.title;
        const label = autoJoin ? `${this.$root.$t('message.workflow.script')}${this.script.title} ${msg}` : msg;
        this.$Notice.close(name);
        this.$Notice[type]({
          title: this.$root.$t('message.workflow.notice.notice.title'),
          name,
          desc: '',
          duration: 5,
          render: (h) => {
            return h('span', {
              style: {
                'word-break': 'break-all',
                'line-height': '20px',
              },
            }, label);
          },
        });
      });
      this.execute.on('costTime', (time) => {
        this.script.progress.costTime = util.convertTimestamp(time);
        this.updateNodeCache('progress');
      });
    },
    resetQuery() {
      this.showPanelTab('progress');
      this.isLogShow = false;
      this.localLog = {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      };
      this.script.progress = {
        current: null,
        progressInfo: [],
        waitingSize: null,
        costTime: null,
      };
      this.script.log = { all: '', error: '', warning: '', info: '' };
      this.script.logLine = 1;
      this.script.steps = ['Submitted'];
      this.script.diagnosis = null;
      this.script.result = {
        headRows: [],
        bodyRows: [],
        type: 'EMPTY',
        total: 0,
        path: '',
        cache: {},
      };
      this.script.resultList = null;
    },
    showPanelTab(type) {
      this.scriptViewState.showPanel = type;
      this.script.showPanel = type;
      this.updateNodeCache(type);
      if (type === 'log') {
        this.localLogShow();
      }
    },
    localLogShow() {
      if (!this.debounceLocalLogShow) {
        this.debounceLocalLogShow = debounce(() => {
          if (this.localLog) {
            this.script.log = Object.freeze({ ...this.localLog.log });
            this.script.logLine = this.localLog.logLine;
            this.updateNodeCache('log');
          }
        }, 1500);
      }
      this.debounceLocalLogShow();
    },

    changeResultSet(data, cb) {
      const resultSet = isUndefined(data.currentSet) ? this.script.resultSet : data.currentSet;
      this.script = {
        ...this.script,
        resultSet
      };
      this.updateNodeCache(['result', 'resultList', 'resultSet', 'showPanel']);
      if(cb) {
        cb();
      }
    },
    closeConsole() {
      this.$emit('close-console');
    },
    openPanel(type) {
      if (type === 'log') {
        this.isLogShow = true;
        this.showPanelTab(type);
      }
    },
    checkFromCache() {
      // 每次右键控制台，都会创建一个新实例，所以把上一个执行的实例先停掉
      if (this.execute) {
        clearTimeout(this.execute.executeTimout);
        clearTimeout(this.execute.statusTimeout);
        this.execute = null;
      }
      this.resetQuery();
      const nodeId = this.node.key;
      this.dispatch('workflowIndexedDB:getNodeCache', {
        nodeId,
        cb: (cache) => {
          let needQuery = true;
          if (cache) {
            this.script = Object.assign({}, new Script({
              nodeId,
              title: this.node.title,
              scriptViewState: this.scriptViewState,
            }), cache);
            this.fromLine = cache.logLine;
            this.localLog = {
              log: cache.log,
              logLine: cache.logLine,
            };
            if (EXECUTE_COMPLETE_TYPE.indexOf(this.script.status) === -1) {
              if (!cache.log.all) {
                this.getLogs();
              }
            } else {
              needQuery = false;
            }
          } else {
            this.createScript();
          }
          if (needQuery) {
            this.$nextTick(() => {
              this.createExecute(needQuery);
            })
          }
        }
      })
    },
    updateNodeCache(key) {
      this.dispatch('workflowIndexedDB:updateNodeCache', {
        nodeId: this.script.nodeId,
        key,
        value: this.script,
      });
    },
    getLogs() {
      api.fetch(`/entrance/${this.node.runState.execID}/log`, {
        fromLine: this.fromLine,
        size: -1,
      }, 'get')
        .then((rst) => {
          this.localLog = this.convertLogs(rst.log);
          this.script.log = this.localLog.log;
          this.script.logLine = this.fromLine = rst.fromLine;
          if (this.scriptViewState.showPanel === 'log') {
            this.localLogShow();
          }
          this.updateNodeCache(['log']);
        });
    },
    convertLogs(logs) {
      const tmpLogs = this.localLog || {
        log: { all: '', error: '', warning: '', info: '' },
        logLine: 1,
      };
      let hasLogInfo = Array.isArray(logs) ? logs.some((it) => it.length > 0) : logs;
      if (!hasLogInfo) {
        return tmpLogs;
      }
      const convertLogs = util.convertLog(logs);
      Object.keys(convertLogs).forEach((key) => {
        const convertLog = convertLogs[key];
        if (convertLog) {
          tmpLogs.log[key] += convertLog + '\n';
        }
        if (key === 'all') {
          tmpLogs.logLine += convertLog.split('\n').length;
        }
      });
      return tmpLogs;
    },
    toggleFull() {
      let bottomContentHeight
      if (this.scriptViewState.bottomPanelFull) {
        bottomContentHeight = this._last_bottom_panel_height
      } else {
        this._last_bottom_panel_height = this.scriptViewState.bottomContentHeight
        bottomContentHeight = this.$parent.$el.clientHeight + 50
      }
      this.scriptViewState = {
        ...this.scriptViewState,
        bottomContentHeight,
        bottomPanelFull: !this.scriptViewState.bottomPanelFull
      }
    },
  }
}
</script>
<style lang="scss" scoped>
@import '@dataspherestudio/shared/common/style/variables.scss';
  .log-panel {
    margin-top: 1px;
    border-top: $border-width-base $border-style-base $border-color-base;
    @include border-color($border-color-base, $dark-border-color-base);
    @include bg-color($light-base-color, $dark-base-color);
    &.full-screen {
      top: 54px !important;
      right: 0 !important;
      bottom: 0 !important;
      left: 0 !important;
      position: fixed;
      z-index: 1050;
      height: calc(100% - 54px) !important;
      width: 100% !important;
    }
    .workbench-tabs {
      position: $relative;
      height: 100%;
      overflow: hidden;
      box-sizing: border-box;
      z-index: 3;
      display: flex;
      flex-flow: column;
      .workbench-tab-wrapper {
        height: 34px;
        display: flex;
        border-top: $border-width-base $border-style-base #dcdcdc;
        border-bottom: $border-width-base $border-style-base #dcdcdc;
        @include border-color($border-color-base, $dark-menu-base-color);
        .workbench-tab {
          flex: 1;
          display: flex;
          flex-direction: row;
          flex-wrap: nowrap;
          justify-content: flex-start;
          align-items: flex-start;
          height: 32px;
          @include bg-color($light-base-color, $dark-base-color);
          width: calc(100% - 45px);
          overflow: hidden;
          .workbench-tab-item {
            text-align: center;
            border-top: none;
            display: inline-block;
            height: 32px;
            line-height: 32px;
            @include bg-color($background-color-base, $dark-workspace-body-bg-color);
            @include font-color(
              $title-color,
              $dark-workspace-title-color
            );
            cursor: pointer;
            min-width: 100px;
            max-width: 200px;
            overflow: hidden;
            margin-right: 2px;
            border: 1px solid #eee;
            @include border-color($border-color-base, $dark-border-color-base);
            &.active {
              margin-top: 1px;
              @include bg-color($light-base-color, $dark-base-color);
              color: $primary-color;
              border-radius: 4px 4px 0 0;
              border: 1px solid $border-color-base;
              border-bottom: 2px solid $primary-color;
              @include border-color($border-color-base, $dark-border-color-base);
            }
          }
        }
      }
      .workbench-container {
        height: calc(100% - 36px);
        flex: 1;
        @keyframes ivu-progress-active {
            0% {
                opacity: .3;
                width: 0;
            }
            100% {
                opacity: 0;
                width: 100%;
            }
        }
      }
    }
    .workbench-tab-full-btn {
      display: inline-block;
      line-height: 32px;
      align-items: center;
      padding-right: 8px;
      height: 32px;
      cursor: pointer;
      &:hover {
        @include font-color($primary-color, $dark-primary-color);
      }
    }
    .column-pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      padding-right: 8px;
    }
  }
</style>
