<template>
  <div class="script-container">
    <we-panel ref="panel" diretion="vertical" @on-resize="resize" @on-move-end="resizePanel">
      <we-panel-item
        ref="topPanel"
        :index="1"
        :min="36"
        :height="scriptViewState.topPanelHeight"
        class="editor-panel"
        :class="{'full-screen': scriptViewState.topPanelFull}"
      >
        <editor ref="editor" :script="script" :work="work" :script-type="work.type" :readonly="readonly" :nodeEditable="hasNodeEditable()"
          @on-save="save"
          @on-run="run"
          @on-fix="emitAiFix"
          @on-stop="stop"/>
        <Spin v-if="saveLoading" size="large" class="new-sidebar-spin" fix />
      </we-panel-item>
      <!-- 使用v-if把非当前脚本DOM移除文档防止DOM节点过多影响性能 -->
      <we-panel-item
        v-if="current===work.id"
        :index="2"
        :min="36"
        :height="scriptViewState.bottomContentHeight"
        class="log-panel"
        :class="{'full-screen': scriptViewState.bottomPanelFull}"
      >
        <div class="workbench-tabs">
          <div class="workbench-tab-wrapper">
            <div class="workbench-tab">
              <div v-if="bottomTab.progress" :class="{active: scriptViewState.showPanel == 'progress'}"
                class="workbench-tab-item" @click="showPanelTab('progress')">
                <span>{{ $t('message.scripts.tabs.progress') }}</span>
              </div>
              <div v-if="bottomTab.result" :class="{active: scriptViewState.showPanel == 'result'}"
                class="workbench-tab-item" @click="showPanelTab('result')">
                <span>{{ $t('message.scripts.tabs.result') }}</span>
              </div>
              <div v-if="bottomTab.log" :class="{active: scriptViewState.showPanel == 'log'}" class="workbench-tab-item"
                @click="showPanelTab('log')">
                <span>{{ $t('message.scripts.tabs.log') }}</span>
              </div>
              <div v-if="bottomTab.history" :class="{active: scriptViewState.showPanel == 'history'}"
                class="workbench-tab-item" @click="showPanelTab('history')">
                <span>{{ $t('message.scripts.tabs.history') }}</span>
              </div>
              <div v-if="bottomTab.fieldDetail & isShowFieldDetailTab" :class="{active: scriptViewState.showPanel == 'fieldDetail'}"
                class="workbench-tab-item" style="min-width: 200px" @click="showPanelTab('fieldDetail')">
                <span>{{ codePrecheckTitle }}</span>
              </div>
              <template v-for="(comp, index) in extComponents">
                <component
                  :key="comp.name + '_head' + index"
                  :is="comp.tabHead"
                  :script="script"
                  :scriptViewState="scriptViewState"
                  :comp="comp"
                  :work="work"
                  :class="{active: scriptViewState.showPanel == comp.name}"
                  @click.native="showPanelTab(comp.name)" />
              </template>
            </div>
            <div v-if="handleComputeShowBigTip()" class="column-pagination">
              <span>{{ $t('message.workbench.columnPagination') }} {{ scriptResult.totalColumn }} {{ $t('message.scripts.constants.columns') }}</span>
              <Page
                :total="scriptResult.totalColumn"
                :current="scriptViewState.columnPageNow || 1"
                :page-size="500"
                :simple="true"
                size="small"
                @on-change="onChangeColPage"
              />
            </div>
            <span class="workbench-tab-full-btn" @click="configLogPanel">
              <Icon :type="scriptViewState.bottomPanelFull?'md-contract':'md-expand'" />
              {{ scriptViewState.bottomPanelFull ? $t('message.scripts.constants.logPanelList.releaseFullScreen') : $t('message.scripts.constants.logPanelList.fullScreen') }}
            </span>
          </div>
          <div class="workbench-container">
            <we-progress
              v-if="bottomTab.show_progress"
              ref="progressTab"
              :script="script"
              :script-view-state="scriptViewState"
              :execute="execute"
              :show-diagnosis="showDiagnosis"
              @compareDetail="openCompareDetail"
              @open-panel="openPanel"
              @showDiagnosisModal="handleShowDiagnosis" />
            <result
              v-if="bottomTab.show_result"
              ref="result"
              getResultUrl="filesystem"
              :script="script"
              :work="work"
              :dispatch="dispatch"
              :script-view-state="scriptViewState"
              @on-set-change="changeResultSet"/>
            <log
              v-if="bottomTab.show_log"
              :status='script.status'
              :logs="script.log"
              :log-line="script.logLine"
              :script-view-state="scriptViewState"/>
            <history
              v-if="bottomTab.show_history"
              :history="script.history"
              :script-view-state="scriptViewState"
              :run-type="script.runType"
              :node="node"
              @on-fix="checkAIFix"
              @update-from-history="updateFromHistory"/>
            <field
              v-if="bottomTab.show_fieldDetail"
              :codePrecheckRes="script.codePrecheckRes"
              :script-view-state="scriptViewState"
            />
            <template v-for="(comp, index) in extComponents">
              <component
                v-if="scriptViewState.showPanel == comp.name"
                :key="comp.name + index"
                :is="comp.component"
                :script="script"
                :execute="execute"
                :script-view-state="scriptViewState"
                :work="work" />
            </template>
          </div>
        </div>
      </we-panel-item>
    </we-panel>
    
    <!-- 事中诊断弹窗 -->
    <diagnosisModal
      ref="diagnosisModal"
      :task-id="work.taskID"
      :diagnosis-res="curDiagnosisRes"
    />
  </div>

</template>
<script>
import {
  isEmpty,
  find,
  dropRight,
  toArray,
  values,
  isString,
  findIndex,
  last,
  isUndefined,
  debounce
} from 'lodash';
import api from '@dataspherestudio/shared/common/service/api';
import storage from '@dataspherestudio/shared/common/helper/storage';
import Execute from '@dataspherestudio/shared/common/service/execute';
import util from '@dataspherestudio/shared/common/util';
import {
  Script
} from '../modal.js';

import editor from './editor.vue';
import history from './history.vue';
import result from '@dataspherestudio/shared/components/consoleComponent/result.vue';
import log from '@dataspherestudio/shared/components/consoleComponent/log.vue';
import field from './field.vue';
import weProgress from '@dataspherestudio/shared/components/consoleComponent/progress.vue';
import diagnosisModal from '@dataspherestudio/shared/components/consoleComponent/diagnosisModal.vue';
import mixin from '@dataspherestudio/shared/common/service/mixin';
import plugin from '@dataspherestudio/shared/common/util/plugin';
import eventbus from '@dataspherestudio/shared/common/helper/eventbus';
import { EXECUTE_COMPLETE_TYPE } from '@dataspherestudio/shared/common/config/const';

const extComponents = []
const typeMap ={'.py': 'pyspark','.hql': 'hive sql', '.sql': 'spark sql', '.scala': 'spark scala', '.txt': '文本'};

export default {
  name: 'editor-script',
  components: {
    result,
    editor,
    log,
    history,
    weProgress,
    field,
    diagnosisModal,
  },
  props: {
    work: {
      type: Object,
      required: true,
    },
    current: {
      type: String,
      default: ''
    },
    node: Object,
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    let showPanel = this.work.type === 'historyScript' ? 'log' : 'progress'
    return {
      script: {
        data: '',
        oldData: '',
        result: {},
        steps: [],
        progress: {},
        resultList: null,
        resultSet: 0,
        params: {},
        readOnly: false,
        diagnosisRes: {}, // 缓存事中诊断结果
        diagnosisProblemTypes: [], // 存储去重后的异常类型
      },
      scriptViewState: {
        topPanelHeight: this.node ? 300 : 450,
        bottomContentHeight: '250',
        topPanelFull: false,
        showPanel,
        bottomPanelFull: false,
        cacheLogScroll: 0,
        columnPageNow: 1,
      },
      execute: null,
      postType: 'http',
      saveLoading: false,
      extComponents,
      isShowFieldDetailTab: false,
      codePrecheckTitle: this.$t('message.workbench.fieldTypeMismatch'),
      showDiagnosis: false,
      curDiagnosisRes: {},
      diagnosisModalVisible: false,
    };
  },
  mixins: [mixin],
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
      if (this.script.resultList) {
        res = this.script.resultList[this.script.resultSet || 0].result || res
      }
      return res
    },
    bottomTab() {
      let { showPanel } = this.scriptViewState;
      return {
        progress: this.work.type !== 'historyScript',
        result: this.scriptResult.total || (this.script.resultList && this.script.resultList.length),
        log: this.isLogShow || this.work.type === 'historyScript',
        history: this.work.type !== 'historyScript',
        fieldDetail: (this.script.codePrecheckRes && this.script.codePrecheckRes.checkData &&this.script.codePrecheckRes.checkData.length > 0 )|| false,
        show_progress: showPanel === 'progress',
        show_result: showPanel === 'result',
        show_log: showPanel === 'log',
        show_history: showPanel === 'history',
        show_fieldDetail: showPanel === 'fieldDetail',
      }
    }
  },
  watch: {
    'script.data': function () {
      if (!this.work.ismodifyByOldTab) {
        this.work.unsave = this.script.data != this.script.oldData;
        if (this.node) {
          this.node.isChange = this.work.unsave;
        }
      }
    },
    'script.oldData': function () {
      if (!this.work.ismodifyByOldTab) {
        this.work.unsave = this.script.data != this.script.oldData;
      }
    },
    'execute.run': function (val) {
      this.work.data.running = this.script.running = val;
      // 加入用户名来区分不同账户下的tab
      this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName });
    },
    'execute.status': function (val) {
      this.work.data.status = val;
      this.script.status = val;
      // 加入用户名来区分不同账户下的tab
      this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName });
    },
    current(){
      if (this.node) {
        this.resizePanel()
      }
      const needFix = storage.get(`cache_needfix_${this.script.id}`);
      if (needFix) {
        this.checkAIFix(
          needFix.taskId,
          needFix.code,
          needFix.errCode,
          needFix.errMessage,
          needFix.status
        )
      }
    },
    'work.unsave': function(v) {
      if (this.node && v) {
        this.$set(this.node, 'isChange', true)
      }
    }
  },
  async created() {
    this.userName = this.getUserName();
    // 如果当前work存在data，表示工作已经被打开
    if (this.work.data) {
      delete this.work.data.oldData;
      this.script = this.work.data;
    } else if (this.work.type === 'historyScript') {
      this.initHistory();
      return;
    } else {
      let supportedMode = find(this.getSupportModes(), (p) => p.rule.test(this.work.filename));
      if (this.work.specialSetting) {
        supportedMode = find(this.getSupportModes(), (p) => p.runType === this.work.specialSetting.runType);
      }
      // 判断特殊字符的文件, 后台编译可能会因为文件名存在特殊字符报错，所以无法运行
      const regLeal = /^[.\w\u4e00-\u9fa5-]{1,200}\.[A-Za-z0-9]+$/;
      const islegal = regLeal.test(this.work.filename);
      this.work.data = this.script = new Script(Object.assign({}, supportedMode, {
        id: this.work.id,
        fileName: this.work.filename,
        // supportedMode的executable优先级高于islegal
        executable: !supportedMode.executable ? supportedMode.executable : islegal,
        data: this.work.code,
        params: this.work.params,
        readOnly: this.readonly || this.work.readOnly
      }));
      delete this.work.specialSetting;
    }

    // 删掉无用的code和params，因为已经存储在script对象中
    delete this.work.code;
    delete this.work.params;
    this.script.oldData = this.script.data;
    if (!this.work.noLoadCache) {
      this.dispatch('IndexedDB:getResult', {
        tabId: this.script.id,
        cb: (resultList) => {
          if (resultList) {
            this.script.resultList = dropRight(toArray(resultList), 3);
            this.script.resultSet = resultList.resultSet;
            this.showPanelTab(resultList.showPanel);
          }
        }
      });
      this.dispatch('IndexedDB:getLog', {
        tabId: this.script.id,
        cb: (logList) => {
          this.script.log = Object.freeze(logList);
        }
      });
      this.dispatch('IndexedDB:getProgress', {
        tabId: this.script.id,
        cb: ({
          content
        }) => {
          this.script.progress = {
            current: content.current,
            progressInfo: content.progressInfo,
            costTime: content.costTime,
          };
          this.script.steps = content.steps || [];
        }
      });
    }
    let cacheWork = await this.getCacheWork(this.work);
    this._running_scripts_key = 'running_scripts_' + this.userName;
    if (cacheWork) { // 点击运行后脚本正在执行中未关掉Tab刷新页面时执行进度恢复
      let {
        data,
        taskID,
        execID
      } = cacheWork;
        // cacheWork.taskID && .execID && cacheWork.data && cacheWork.data.running
      if (taskID && execID && data && data.running) {
        let dbProgress = JSON.parse(JSON.stringify(this.script.progress));
        delete cacheWork.data.history;
        this.script = cacheWork.data;
        this.script.progress = dbProgress;
        this.run({
          taskID,
          execID,
          isRestore: true,
          id: this.script.id,
        });
        this.work.execID = execID;
        this.work.taskID = taskID;
      }
    } else { // 脚本正在执行中关掉了Tab，之后再打开页面，执行进度恢复
      let runningScripts = storage.get(this._running_scripts_key, 'local') || {};
      if (runningScripts[this.script.id]) {
        // 存在execID表示任务已经在执行，否则任务已提交或排尚未真正执行
        if (runningScripts[this.script.id].execID) {
          this.script.steps = runningScripts[this.script.id].steps;
          this.script.progress = runningScripts[this.script.id].progress;
          this.run({
            taskID: runningScripts[this.script.id].taskID,
            execID: runningScripts[this.script.id].execID,
            isRestore: true,
            id: this.script.id,
          });
          this.work.execID = runningScripts[this.script.id].execID;
          this.work.taskID = runningScripts[this.script.id].taskID;
        }
      } else {
        this.script.status = 'Inited';
      }
    }
    this.dispatch('IndexedDB:getHistory', {
      tabId: this.script.id,
      cb: (historyList) => {
        // console.log('[DEBUG] script.vue created - IndexedDB:getHistory 回调');
        // console.log('[DEBUG] tabId:', this.script.id);
        // console.log('[DEBUG] historyList:', historyList);
        this.script.history = dropRight(values(historyList));
        // console.log('[DEBUG] this.script.history:', this.script.history);
      }
    });
    // 加入用户名来区分不同账户下的tab
    this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName });
    
    // 恢复缓存的诊断结果
    if (this.script.diagnosisRes && this.script.diagnosisRes.diagnosisMsg) {
      this.curDiagnosisRes = this.script.diagnosisRes;
      const diagnosisMsgJson = typeof this.curDiagnosisRes.diagnosisMsg === 'string' 
        ? JSON.parse(this.curDiagnosisRes.diagnosisMsg) 
        : this.curDiagnosisRes.diagnosisMsg;
      // 只要有problems就显示提示区域，按钮的显示由progress.vue中的hasSuggestions控制
      if (this.script.status === 'Running' && diagnosisMsgJson.data && diagnosisMsgJson.data.problems && diagnosisMsgJson.data.problems.length > 0) {
        // 提取并去重异常类型
        const problemTypes = diagnosisMsgJson.data.problems.map(p => p.type);
        this.script.diagnosisProblemTypes = [...new Set(problemTypes)];
        this.showDiagnosis = true;
      }
    }
    
    // 刷新页面时重新查询接口获取最新诊断数据
    if (this.work.taskID && this.script.application === 'spark') {
      this.refreshDiagnosisData();
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.dispatch('WebSocket:init');
      this.resizePanel();
      this.initListenerCopilotEvent()
    });
    window.onbeforeunload = () => {
      if (this.work.unsave || this.script.running) {
        return 'need confirm';
      }
      return null;
    };
    this.autoSave();
    if (this.scriptResult && this.scriptResult.columnPageNow && this.scriptResult.columnPageNow != this.scriptViewState.columnPageNow) {
      this.scriptViewState.columnPageNow = this.scriptResult.columnPageNow;
    }
  },
  beforeDestroy() {
    // 关闭tab页面不再继续轮询接口
    if (this.execute && this.execute.run !== undefined) {
      this.execute.run = false;
    }
    clearTimeout(this.autoSaveTimer);
    if(this.canceledTimeout) {
      clearTimeout(this.canceledTimeout);
      this.canceledTimeout = null;
    }
    // 清理事中诊断轮询定时器
    if (this.diagnosisPollingTimer) {
      clearTimeout(this.diagnosisPollingTimer);
      this.diagnosisPollingTimer = null;
    }
    let runningScripts = storage.get(this._running_scripts_key, 'local') || {};
    if (this.script.running && this.execute && this.execute.taskID && this.execute.id) {
      runningScripts[this.script.id] = {
        execID: this.execute.id,
        taskID: this.execute.taskID,
        progress: this.script.progress,
        steps: this.script.steps,
      };
    } else {
      delete runningScripts[this.script.id];
    }
    storage.set(this._running_scripts_key, runningScripts, 'local');
    if (this.execute) {
      this.execute.off();
      this.execute = null;
    }
    window.onbeforeunload = null;
    plugin.off('copilot_web_listener_inster', this.insterCode)
  },
  methods: {
    handleComputeShowBigTip() {
      let curVShow = 'table'
      if(this.$refs.result) {
       curVShow = this.$refs.result.getVisualShow();
      }
      return this.bottomTab.show_result && curVShow==='table' && this.scriptResult.totalColumn <= 10000 && this.scriptResult.totalColumn > 500
    },
    hasNodeEditable() {
      let flag = true;
      if(this.node && this.node.params && this.node.params.configuration && this.node.params.configuration.special 
            && this.node.params.configuration.special.disableEdit 
            && (this.node.params.configuration.special.disableEdit === "true" || this.node.params.configuration.special.disableEdit === true)) {
        flag = false;
      }
      return flag
    },
    /**
      events: [{
          eventType?: any,
          arg?: {}
      }]
     * @param {*} param0 
     */
    initListenerCopilotEvent() {
      plugin.on('copilot_web_listener_inster', this.insterCode)
    },
    onChangeColPage(v) {
      console.log('script-onChangeColPage', v)
      this.scriptViewState = {...this.scriptViewState, columnPageNow: v}
      console.log('scriptViewState', this.scriptViewState)
    },
    insterCode({ code }) {
      if (this.current !== this.work.id) return
      if (this.script.data) {
        this.script.data += `\n${code}`
      } else {
        this.script.data += code
      }
      this.$Message.success("code inster success");
    },
    // panel 分割线拖动调整大小
    resizePanel: debounce(function() {
      // 兼容方案：优先使用 panel ref，如果不存在则使用 $el
      const containerEl = this.$refs.panel ? this.$refs.panel.$el : this.$el;
      if (containerEl && this.$refs.topPanel && (containerEl.clientHeight - this.$refs.topPanel.$el.clientHeight > 0)) {
        this.scriptViewState.topPanelHeight = this.$refs.topPanel.$el.clientHeight
        this.scriptViewState.bottomContentHeight = containerEl.clientHeight - this.$refs.topPanel.$el.clientHeight;
      }
    }, 700),
    // 浏览器窗口缩放
    resize: debounce(function() {
      // 兼容方案：优先使用 panel ref，如果不存在则使用 $el
      const containerEl = this.$refs.panel ? this.$refs.panel.$el : this.$el;
      if (this.scriptViewState.bottomPanelFull) {
        this.scriptViewState.bottomContentHeight = containerEl.clientHeight + 30
        return
      }
      if (containerEl && this.$refs.topPanel && (containerEl.clientHeight - this.$refs.topPanel.$el.clientHeight > 0)) {
        this.scriptViewState.topPanelHeight = containerEl.clientHeight * 0.5
        this.scriptViewState.bottomContentHeight = containerEl.clientHeight * 0.5;
      }
    }, 700),
    'Workbench:save'(work) {
      if (work.id == this.script.id) {
        this.save();
      }
    },
    'Workbench:socket'({
      type,
      ...args
    }) {
      if (type === 'downgrade') {
        this.postType = 'http';
      }
      if (this.execute) {
        this.execute.trigger(type, Object.assign(args, {
          execute: this.execute,
        }));
      }
    },
    'Workbench:run'(option, cb) {
      if (option.id === this.script.id) {
        this.run(option, (status) => {
          if (status !== 'start' && status !== 'downgrade') {
            return cb && cb(true);
          }
        });
      }
    },
    'Workbench:setResultCache'(option) {
      if (option.id === this.script.id) {
        if (this.$refs.result) {
          this.script.resultList = this.script.resultList || [this.scriptResult];
          const tmpResult = this.$refs.result.getResult();
          const resultSet = this.script.resultSet || 0;
          this.$set(this.script.resultList[resultSet], 'result', tmpResult);
          this.updateResult({
            tabId: this.script.id,
            resultSet: resultSet,
            showPanel: this.scriptViewState.showPanel,
            ...this.script.resultList,
          })
        }
      }
    },
    'Workbench:setEditorPanelSize'(option) {
      if (option.id === this.script.id) {
        if (option.status == 'fullScreen') {
          this.scriptViewState.topPanelFull = true
        } else if(option.status == 'releaseFullScreen') {
          this.scriptViewState.topPanelFull = false
        }
        this.$nextTick(()=>{
          this.scriptViewState = {...this.scriptViewState}
          this.$refs.editor.layout()
        })
      }
    },
    'Workbench:removeTab'() {
      // fix http://dpms.weoa.com/#/product/100199/bug/detail/199545
      if (this.node) {
        this.resizePanel()
      }
    },
    getCacheWork(work) {
      return new Promise((resolve) => {
        this.dispatch('IndexedDB:getTabs', (tabs) => {
          let currentTab = null;
          for (let tab of tabs) {
            if (tab.id === work.id) {
              currentTab = tab;
              break;
            }
          }
          resolve(currentTab);
        });
      });
    },
    async getDefaultTplForAiSql() {
      const { templateList } = await api.fetch('dss/framework/workspace/engineconf/getConfTemplateList', {
        workspaceId: this.$route.query.workspaceId,
        pageNow: 1,
        pageSize: 1000
      }, 'get')
      return templateList.find(it => {
        return it.defaultForAISQL
      })
    },
    async getExecuteData(option) {
      const executionCode = isString(option.code) ? option.code : this.script.data;
      const isStorage = option && option.type === 'storage';
      this.script.executionCode = isStorage ? option.executionCode : executionCode;
      let params = {};
      if (this.node && Object.keys(this.node).length > 0) {
        params = this.nodeConvertSettingParams(this.script.params);
      } else {
        params = this.convertSettingParams(this.script.params)
      }
      if (params.configuration && option.dataSetValue) {
        if (params.configuration.runtime) {
          params.configuration.runtime = {
            ...params.configuration.runtime,
            'wds.linkis.engine.runtime.datasource': option.dataSetValue,
          }
        } else {
          params.configuration = {
            ...params.configuration,
            runtime: {
              'wds.linkis.engine.runtime.datasource': option.dataSetValue,
            },
          }
        }
      }
      if (this.script.ext === '.aisql') {
       let defaultTpl = await this.getDefaultTplForAiSql()
       if (defaultTpl && !this.work.queryTableCreated) {
         params.configuration.runtime = {
          ...params.configuration.runtime,
          'ec.resource.name': defaultTpl.name
         }
       }
       if(this.work.queryTableCreated) {
        params.configuration.runtime['ec.engine.type'] = 'starrocks'
       }
      }
      if (this.script.ext === '.py3') {
       params.variable = {
        ...params.variable,
        sparkVersion: "3"
       }
      }
      // 对于 spark 类型的脚本，在 Home 路径下，如果没有 sparkVersion 变量则自动添加
      if (this.$route.name == 'Home' && this.script.application === 'spark') {
        if (!params.variable || !params.variable.hasOwnProperty('sparkVersion')) {
          params.variable = {
            ...params.variable,
            sparkVersion: "3"
          }
        }
      } 
      let initData = {
        method: '/api/rest_j/v1/entrance/execute',
        websocketTag: this.work.id,
        data: {
          executeApplicationName: this.script.application,
          executionCode: this.script.executionCode,
          runType: this.script.runType,
          params,
          postType: this.postType,
          source: {
            scriptPath: this.work.filepath || this.work.filename,
          },
        },
      };
      if(this.$route.name == 'Workflow') {
        // 如果params.configuration.runtime中有参数sparkVersion,
        if(params && params.configuration && params.configuration.runtime && params.configuration.runtime.sparkVersion) {
            if(params.variable && !params.variable.sparkVersion ) {
              params.variable.sparkVersion = params.configuration.runtime.sparkVersion
            }
        }
      }
      if (isStorage) {
        initData.method = 'dss/scriptis/backgroundservice';
        initData.data.background = option.backgroundType;
      }

      return initData;
    },
    openCompareDetail() {
      this.isShowFieldDetailTab = true;
      this.showPanelTab('fieldDetail');
    },
    showDiyParamsInfo(executionCode, variableData) {
      // 步骤 1: 提取代码中的变量表达式
      const regex = /\$\{([^}]*)\}/g;
      let match;
      const variablesInCode = new Set();

      while ((match = regex.exec(executionCode)) !== null) {
        const expression = match[1].trim(); // 提取变量表达式并去除前后空白字符

        // 步骤 2: 处理变量表达式
        if (expression.includes('+') || expression.includes('-')) {
          // 表达式中有加减符号，按加减符号分隔
          const parts = expression.split(/\s*[\+\-]\s*/);
          parts.forEach(part => {
            if (part && !/^\d+$/.test(part.trim())) { // 过滤掉纯数字
              variablesInCode.add(part.trim());
            }
          });
        } else {
          // 表达式中没有加减符号，直接视作一个变量
          variablesInCode.add(expression);
        }
      }
      // console.log('variablesInCode', variablesInCode);
      // 执行代码中无变量,返回 false
      if(variablesInCode.size === 0) {
        return false
      }
      // console.log('validDiyParams-variablesInCode', variablesInCode)
      // 步骤 2: 检查变量是否存在
      for (let variable of variablesInCode) {
        const found = variableData.some(data => data.key === variable);
        if (!found) {
        // 如果执行代码中存在某变量，但在参数列表中不存在，则返回 false
          // console.log('不存在的变量', variable)
          return false; 
        }
      }

      return true; // 所有变量都存在，则返回 true
    },
    filterComments(code) {
        // 拆分成行
        const lines = code.split('\n');
        
        // 过滤每一行
        const filteredLines = lines.map(line => {
            // 找到注释符的位置
            const commentIndex = line.indexOf('--');
            
            if (commentIndex === 0) {
                // 注释符在句首，过滤整行
                return '';
            } else if (commentIndex > 0) {
                // 注释符在句中，过滤注释部分
                return line.substring(0, commentIndex).trim();
            } else {
                // 没有注释符，保留原行
                return line;
            }
        });
        
        // 合并过滤后的行
        return filteredLines.join('\n').trim();
    },
    async run(option, cb) {
      this.scriptViewState.columnPageNow = 1;
      this.handleLines = {}
      this.script.validParamsInfoRes = {};
      this.isShowFieldDetailTab = false;
      if (option && option.id === this.script.id) {
        if (this.scriptViewState.topPanelFull) {
          this.dispatch('Workbench:setTabPanelSize');
          this.scriptViewState.topPanelFull = false;
        }
        const data = await this.getExecuteData(option);
        if (this.execute) {
          this.execute.off();
          this.execute = null;
        }
        // 执行
        this.execute = new Execute(data);
        this.isLogShow = false;
        this.localLog = {
          log: {
            all: '',
            error: '',
            warning: '',
            info: ''
          },
          logLine: 1,
        };
        // code-preckcheck执行依赖progress页先展示，延时有关联影响
        if (this.scriptViewState.showPanel != 'progress') {
            setTimeout(()=>{
              this.showPanelTab('progress');
            }, 100)
        }
         
        let runPrecheck = false;
        let runLinkisProcessDiagnosis = false;
        this.showDiagnosis = false;
        if (option && option.isRestore) {
          this.execute.restore(option);
        } else {
          if (this.$refs.progressTab) {
            this.$refs.progressTab.updateErrorMsg({})
            this.$refs.progressTab.updateCodePreCheck({});
          }
          this.dispatch('IndexedDB:clearLog', this.script.id);
          this.dispatch('IndexedDB:clearResult', this.script.id);
          this.dispatch('IndexedDB:clearProgress', this.script.id);
          this.resetProgress();
          this.resetData();
          this.execute.start();
          runPrecheck = true;
          runLinkisProcessDiagnosis = true;
        }
        // 运行时，如果是临时脚本且未保存状态时，弹出一个警告的提醒，否则直接保存。
        if (!this.work.filepath && this.work.unsave && !this.node) {
          this.$Notice.warning({
            title: this.$t('message.scripts.notice.unsave.title'),
            desc: this.$t('message.scripts.notice.unsave.desc'),
            duration: 3,
          });
        } else {
          this.save('', option.dataSetValue, 'run');
        }
        this.execute.once('sendStart', (code) => {
          const name = this.work.filepath || this.work.filename;
          this.$Notice.close(name);
          this.$Notice.info({
            title: this.$t('message.scripts.notice.sendStart.title'),
            desc: '',
            render: (h) => {
              return h('span', {
                style: {
                  'word-break': 'break-all',
                  'line-height': '20px',
                },
              },
              `${this.$t('message.scripts.notice.sendStart.render')} ${this.work.filename} ！`
              );
            },
            name,
            duration: 3,
          });
          this.work.execID = this.execute.id;
          this.work.taskID = this.execute.taskID;
          if (code) {
            this.script.executionCode = this.script.data = code;
          }
          // 加入用户名来区分不同账户下的tab
          this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName });
          cb && cb('start');
        });

        this.execute.on('log', (logs) => {
          let hasLogInfo = Array.isArray(logs) ? logs.some((it) => it.length > 0) : logs;
          if (!hasLogInfo) {
            return;
          }
          const convertLogs = util.convertLog(logs);
          Object.keys(convertLogs).forEach((key) => {
            const convertLog = convertLogs[key];
            if (convertLog) {
              this.localLog.log[key] += convertLog + '\n';
            }
            if (key === 'all') {
              this.localLog.logLine += convertLog.split('\n').length;
            }
          });

          if (this.scriptViewState.showPanel === 'log') {
            this.localLogShow();
          }
        });

        this.execute.on('history', (ret) => {
          // console.log('[DEBUG] history事件触发');
          // console.log('[DEBUG] ret:', ret);
          // console.log('[DEBUG] this.script.history:', this.script.history);
          // console.log('[DEBUG] this.execute.taskID:', this.execute ? this.execute.taskID : 'undefined');
          
          this.script.history = this.script.history || []
          const index = findIndex(this.script.history, (o) => o.taskID == ret.taskID);
          // console.log('[DEBUG] index:', index);
          const findHis = index > -1 ? this.script.history[index] : undefined;
          // console.log('[DEBUG] findHis:', findHis);
          let newItem = null;
          // 这里针对的是导入导出脚本，executionCode为object的情况
          const code = typeof (this.script.executionCode) === 'string' && this.script.executionCode ? this.script
            .executionCode : this.script.data;
          if (findHis) {
            if (ret.isPartialUpdate) {
              ret.runningTime = findHis.runningTime || 0;
              delete ret.isPartialUpdate;
              newItem = Object.assign(findHis, ret);
            } else if (Object.prototype.hasOwnProperty.call(ret, 'logPath')) {
              newItem = {
                subscribed: findHis.subscribed,
                taskID: ret.taskID,
                createDate: findHis.createDate,
                execID: ret.execID || findHis.execID,
                runningTime: findHis.runningTime,
                // executionCode代表是选中某段代码进行执行的
                data: code,
                status: ret.status,
                fileName: this.script.fileName,
                errDesc: ret.errDesc,
                errCode: ret.errCode,
                failedReason: ret.failedReason
              };
            } else {
              newItem = {
                subscribed: findHis.subscribed,
                taskID: ret.taskID,
                createDate: ret.createDate,
                execID: ret.execID || findHis.execID,
                runningTime: ret.runningTime,
                data: code,
                status: ret.status,
                fileName: this.script.fileName,
                errDesc: ret.errDesc,
                errCode: ret.errCode,
                failedReason: ret.failedReason
              };
            }
          } else {
            newItem = {
              taskID: ret.taskID,
              createDate: ret.createDate,
              execID: ret.execID,
              runningTime: ret.runningTime,
              data: code,
              status: ret.status,
              fileName: this.script.fileName,
              failedReason: ret.failedReason,
              errDesc: ret.errDesc,
              errCode: ret.errCode,
            };
          }
          newItem.solution = ret.solution
          if (index === -1) {
            this.script.history.unshift(newItem);
            this.dispatch('IndexedDB:appendHistory', {
              tabId: this.script.id,
              ...this.script.history,
            });
          } else {
            this.script.history.splice(index, 1, newItem);
            this.dispatch('IndexedDB:updateHistory', {
              tabId: this.script.id,
              ...this.script.history,
            });
          }
          // 有绑定解决方案停留进度tab，否则打开日志定位第一行错误
          if (this.$refs.progressTab) {
            this.$refs.progressTab.updateErrorMsg({
              solution: ret.solution,
              errDesc: ret.errDesc,
              errCode: ret.errCode,
              status: ret.status,
              taskId: ret.taskID,
              failedReason: ret.errCode && ret.errDesc ? ret.errCode + ret.errDesc : ''
            })
          }
          if (ret.solution && ret.solution.solutionUrl) {
            //
          } else if (ret.status == 'Failed') {
            this.showPanelTab('log')
          }
          this.checkAIFix(ret.taskID, code, ret.errCode, ret.errDesc, ret.status)
        });
        this.execute.on('result', (ret) => {
          this.showPanelTab('result');
          // const storeResult = {
          //   'headRows': ret.metadata,
          //   'bodyRows': ret.fileContent,
          //   'total': ret.totalLine,
          //   'type': ret.type,
          //   'totalColumn': ret.totalColumn,
          //   'path': this.execute.currentResultPath,
          //   'current': 1,
          //   'size': 20,
          //   tipMsg: ret.tipMsg,
          //   hugeData: !!ret.hugeData
          // };
          // this.$set(this.execute.resultList[0], 'result', storeResult);
          this.$set(this.script, 'resultSet', 0);
          this.script.resultList = this.execute.resultList;
          this.dispatch('IndexedDB:getResult', {
            tabId: this.script.id,
            cb: (resultList) => {
              if (resultList) {
                this.updateResult({
                  tabId: this.script.id,
                  resultSet: 0,
                  showPanel: 'result',
                  ...this.script.resultList,
                });
              } else {
                this.dispatch('IndexedDB:appendResult', {
                  tabId: this.script.id,
                  resultSet: 0,
                  showPanel: 'result',
                  ...this.script.resultList,
                });
              }
            }
          });
        });
        this.execute.on('progress', ({
          progress,
          progressInfo,
          waitingSize,
          yarnMetrics
        }) => {
          this.$set(this.script, 'yarnMetrics', yarnMetrics)
          if (this._execute_last_progress === progress) {
            return;
          }
          this._execute_last_progress = progress;
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
            delete runningScripts[this.script.id];
            storage.set(this._running_scripts_key, runningScripts, 'local');
          }

          this.script.progress = {
            ...this.script.progress,
            current: progress
          }

          if (waitingSize !== null && waitingSize >= 0) {
            this.script.progress.waitingSize = waitingSize;
          }
          this.dispatch('IndexedDB:updateProgress', {
            tabId: this.script.id,
            rst: this.script.progress,
          });
        });
        this.execute.on('updateExpireHistory', (data) => {
          const index = findIndex(this.script.history, (o) => o.taskID === data.taskID);
          const findHis = find(this.script.history, (o) => o.execID === data.execID);
          if (findHis) {
            let newItem = null;
            api.fetch(`/jobhistory/${findHis.taskID}/get`, 'get')
              .then((res) => {
                newItem = {
                  solution: res.solution,
                  taskID: res.task.taskID,
                  createDate: res.task.createdTime,
                  execID: res.task.strongerExecId,
                  runningTime: res.task.costTime,
                  data: res.task.executionCode,
                  status: res.task.status,
                  fileName: res.task.fileName,
                  failedReason: res.task.errCode && res.task.errDesc ? res.task.errCode + res.task.errDesc :
                    res.task.errCode || res.task.errDesc || '',
                };
                this.script.history.splice(index, 1, newItem);
                this.dispatch('IndexedDB:updateHistory', {
                  tabId: this.script.id,
                  ...this.script.history,
                });
              });
          }
        });
        this.execute.on('steps', (status) => {
          if (this._execute_last_status === status) {
            return;
          }
          // Linkis在自动重试任务时，DSS前端不需要展示重试中，默认后台重试，用户无感知
          if (this.script.steps.indexOf('Running') > -1 && ['Scheduled', 'WaitForRetry','Inited'].indexOf(status) > -1) {
            return
          }
          if (status == 'WaitForRetry') status = 'Running'
          this._execute_last_status = status;
          if (status === 'Inited') {
            this.script.steps = ['Submitted', 'Inited'];
          } else {
            if (this.script.steps.indexOf(status) === -1) {
              this.script.steps.push(status);
            }
            
            this.dispatch('IndexedDB:updateProgress', {
              tabId: this.script.id,
              rst: Object.assign(this.script.progress, {
                steps: this.script.steps
              }),
            });
          }
        });
        this.execute.on('WebSocket:send', (data) => {
          this.dispatch('WebSocket:send', data);
        });
        this.execute.on('error', (type) => {
          // 执行错误的时候resolve，用于改变modal框中的loading状态
          cb && cb(type || 'error');
          this.dispatch('IndexedDB:appendLog', {
            tabId: this.script.id,
            rst: this.script.log,
          });
        });
        this.execute.on('stateEnd', () => {
          // 执行成功的时候resolve，用于改变modal框中的loading状态
          cb && cb('end');
          this.dispatch('IndexedDB:appendLog', {
            tabId: this.script.id,
            rst: this.localLog.log,
          });
        });
        this.execute.on('querySuccess', ({
          type,
          task
        }) => {
          const costTime = util.convertTimestamp(task.costTime);
          this.script.progress = {
            ...this.script.progress,
            costTime
          };
          const name = this.work.filepath || this.work.filename;
          this.$Notice.close(name);
          this.$Notice.success({
            title: this.$root.$t('message.scripts.notice.querySuccess.title'),
            desc: '',
            render: (h) => {
              return h('span', {
                style: {
                  'word-break': 'break-all',
                  'line-height': '20px',
                },
              },
              `${this.work.filename} ${type}${this.$root.$t('message.scripts.notice.querySuccess.render')}：${costTime}！`
              );
            },
            name,
            duration: 3,
          });
        });
        this.execute.on('queryError',()=>{
          this.$set(this.script, 'running', false)
        })
        this.execute.on('notice', ({
          type,
          msg,
          autoJoin
        }) => {
          const name = this.work.filepath || this.work.filename;
          const label = autoJoin ?
            `${this.$root.$t('message.scripts.script')}${this.work.filename} ${msg}` : msg;
          this.$Notice.close(name);
          this.$Notice[type]({
            title: this.$root.$t('message.scripts.notice.notice.title'),
            name,
            desc: '',
            duration: 5,
            render: (h) => {
              return h('div', {
                style: {
                  position: 'relative',
                  'padding-bottom': '15px'
                }
              }, [h('span', {
                style: {
                  'word-break': 'break-all',
                  'line-height': '20px',
                },
              }, label)])
            },
          });
        });
        this.execute.on('costTime', (time) => {
          this.script.progress.costTime = util.convertTimestamp(time);
          this.dispatch('IndexedDB:updateProgress', {
            tabId: this.script.id,
            rst: this.script.progress,
          });
        });
        // 只有hive或者spark引擎类型才会调用代码关联审查 置于末尾，不影响其余事件
        if (runPrecheck && (
          (this.script.application === 'spark' && this.script.runType === 'sql') 
          || (this.script.application === 'hive' && this.script.runType === 'hql') 
          || this.script.ext === '.aisql')) {
          const variable = isEmpty(this.script.params.variable) ? {} : util.convertArrayToObject(this.script.params.variable);
          let params = {
            'variable': variable,
            'configuration': {}
          };
          const codePrecheckRes = await api.fetch('/validator/code-precheck', {
            params: JSON.stringify(params),
            code: this.script.executionCode,
            engineType: this.script.application,
            type: 'union'
          }, {
            method: 'post',
            useFormQuery: true,
          });
          setTimeout(()=>{
            // 如果拿到代码审查结果时，代码已运行完成，则不进行操作
            if (this.$refs.progressTab && (!this.script.progress || !this.script.progress.costTime)) {
              this.script.codePrecheckRes = codePrecheckRes;
              this.$refs.progressTab.updateCodePreCheck(codePrecheckRes);
              if (this.script.codePrecheckRes.checkData && this.script.codePrecheckRes.checkData.length > 0 && this.script.codePrecheckRes.checkData[0].detailTitle) {
                this.codePrecheckTitle = this.script.codePrecheckRes.checkData[0].detailTitle
              } else {
                this.codePrecheckTitle = this.$t('message.workbench.fieldTypeMismatch')
              }
            }
          }, 150)
        setTimeout(()=>{
          // 如果当前执行代码中配置的参数都在配置参数中,做提示
          const filterCode = this.filterComments(this.script.executionCode)
          const diyParamsInfoValidRes = this.showDiyParamsInfo(filterCode, this.script.params.variable)
          let paramsRes = {
              content: '',
              isShow: false
            }
          if(diyParamsInfoValidRes) {
            paramsRes = {
              content: '脚本存在已配置的自定义参数,请知悉',
              isShow:true
            }
          }
          this.script.validParamsInfoRes = paramsRes;
          this.$refs.progressTab.updateParamsInfo(paramsRes);
        } , 150)
        }
        if(runLinkisProcessDiagnosis &&  this.script.application === 'spark') {
          setTimeout(async ()=>{
          // 延时后发起事中诊断轮询，如果查询时代码已运行完成，则不进行操作
            if (!this.script.progress || !this.script.progress.costTime) {
              this.startDiagnosisPolling();
            }
          },  3 * 60 * 1000)
        }
      }
    },
    resetData() {
      // upgrade only one time
      this.script.log = {
        all: '',
        error: '',
        warning: '',
        info: ''
      };
      this.script.logLine = 1;
      this.script.steps = ['Submitted'];
      this.script.resultList = null;
      this._execute_last_progress = null;
      this.scriptViewState.columnPageNow = 1;
    },
    resetProgress() {
      this.script.progress = {
        current: null,
        progressInfo: [],
        waitingSize: null,
        costTime: null,
      };
    },
    stop(cb) {
      clearTimeout(this.killTimer)
      const killAct = () => {
        if (this.execute && this.execute.id) {
          api.fetch(`/entrance/${this.execute.id}/kill`, {taskID: this.execute.taskID}, 'get').then(() => {
            this.execute.trigger('stop');
            this.execute.trigger('error');
            this.execute.trigger('kill');
            // 停止执行
            const name = this.work.filepath || this.work.filename;
            this.$Notice.close(name);
            this.$Notice.warning({
              title: this.$t('message.scripts.notice.kill.title'),
              desc: `${this.$t('message.scripts.notice.kill.desc')} ${this.work.filename} ！`,
              name: name,
              duration: 3,
            });
            // kill请求发送成功后status还没有变成Canceled，会有状态请求的轮询，此时应当等待canceled之后才能进行下次执行 dpms 400007
            const fn = () => {
              if (this.script.steps.indexOf('Cancelled') > -1) {
                cb()
              } else {
                this.canceledTimeout = setTimeout(() => {
                  fn()
                }, 1000)
              }
            }
            fn()
          }).catch(() => {
            this.execute.queryStatusaAfterKill = 0
            cb();
          });
          this.script.running = false;
        } else {
          this.killTimer = setTimeout(() => {
            killAct()
          }, 500)
          cb();
        }
      }
      killAct()
    },
    autoSave() {
      clearTimeout(this.autoSaveTimer);
      if (!this.work.saveAs && this.work && this.work.data) {
        this.autoSaveTimer = setTimeout(() => {
          this.save(true);
        }, 1000 * 60 * 5);
      }
    },
    async getCurWorkspaceValidRes () {
      try {
        const id = this.$route.query.workspaceId;
        const res = await api.fetch(
          `dss/framework/workspace/workspaces/${id}`,
          {},
          'get'
        );
        const result = res.workspace.enabledFlowKeywordsCheck || '0';
        return result === '1' ? true: false;
      } catch (err) {
        console.warn(err);
      }
    },
    containsKeywords(keywords, scriptCode) {
      // 将脚本代码转换为小写，以便忽略大小写
      const lowerCaseScript = scriptCode.toLowerCase();
      for (let keyword of keywords) {
      // 对关键字中的正则表达式特殊字符进行转义
      const escapedKeyword = this.escapeRegExp(keyword);
      // 创建正则表达式，确保关键字作为一个完整的单词出现（使用 \b 匹配单词边界）
      const regex = new RegExp(`\\b${escapedKeyword}\\b`, 'i');
      // 如果当前关键字作为一个完整的单词存在于脚本代码中，立即返回 true
      if (regex.test(lowerCaseScript)) {
        return true;
      }
      }
      // 如果所有关键字都不作为一个完整的单词存在于脚本代码中，返回 false
      return false;
    },
    escapeRegExp(string) {
      return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& 表示匹配到的整个字符串
    },
    async workspaceKeyValid() {
      const validType = ['sql','hql','py'];
      if (this.script.runType && validType.includes(this.script.runType)) {
        const curWorkspaceValidRes = await this.getCurWorkspaceValidRes();
        if(curWorkspaceValidRes) {
          const keyWords = ['insert', 'create table']
          const containsKeywordsResult = this.containsKeywords(keyWords, this.script.data)
          return containsKeywordsResult;
        }
      }
      return true
    },
    async handleNodeSave(auto, type) {
      if (this.readonly) return this.$Message.warning(this.$t('message.scripts.notSave'));
      const r = await this.workspaceKeyValid();
      // 校验通过、自动保存时或者运行时不弹窗
      if (r || auto || type === 'run') {
        this.nodeSave();
      } else {
        const content = `<p class="ellipse-p">${this.node.title}${this.$t('message.workbench.nokeywords')}insert / create table</p>`;
        this.$Modal.confirm({
            title: this.$t('message.workbench.nodeKeywordCheck'),
            content: content,
            okText: this.$t('message.workbench.confirm'),
            cancelText: this.$t('message.workbench.cancel'),
            onOk: () => {
                this.nodeSave();
            },
        });
      }
    },
    nodeSave() {
      let params = {
        fileName: this.script.fileName,
        scriptContent: this.script.data,
        creator: this.node.creator,
        projectName: this.$route.query.projectName || '',
        metadata: this.nodeConvertSettingParams(this.script.params),
      };
      const resources = this.node.resources;
      if (resources && resources.length && this.node.jobContent && this.node.jobContent.script) {
        params.resourceId = resources[0].resourceId;
      }
      this.saveLoading = true;
      this.node.params = JSON.parse(JSON.stringify(this.nodeConvertSettingParams(this.script.params)));
      // 删除节点json里的contextId
      delete this.node.params.configuration.runtime.contextID;
      delete this.node.params.configuration.runtime.nodeName;
      // 除了执行，其他的都不需要contextID
      let tempParams = JSON.parse(JSON.stringify(params));
      delete tempParams.metadata.configuration.runtime.contextID;
      delete tempParams.metadata.configuration.runtime.nodeName;
      delete tempParams.metadata.configuration.startup;
      return api.fetch('/filesystem/saveScriptToBML', tempParams, 'post')
        .then((res) => {
          this.$Message.success(this.$t('message.scripts.saveSuccess'));
          this.node.isChange = false;
          this.dispatch('IDE:saveNode', {
            resource: {
              fileName: this.script.fileName,
              resourceId: res.resourceId,
              version: res.version,
            },
            node: this.node,
            data: {
              content: this.script.data,
              params: this.script.params
            }

          }, false);
          this.work.unsave = false;
          this.work.data.data = this.script.data;
          // 提交最新的内容，更新script.data和script.oldData
          this.script.oldData = this.script.data;
          // 保存时更新下缓存。
          // 加入用户名来区分不同账户下的tab
          this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName });
          this.dispatch('IndexedDB:updateGlobalCache', {
            id: this.userName,
            work: this.work,
          });
          this.saveLoading = false;
          // 和后台有确认次值只是用在资源管理器的运行任务名字展示上，对其他无影响
          return this.node.title;
        }).catch(() => {
          // this.$Message.error(this.$t('message.scripts.saveErr'));
          this.saveLoading = false;
          this.work.unsave = true;
        });
    },
    async save(auto, dataSetValue, type) {
      if (this.node && Object.keys(this.node).length > 0) {
        this.handleNodeSave(auto, type);
      } else {
        const params = {
          path: this.work.filepath,
          scriptContent: this.script.data,
          params: this.convertSettingParams(this.script.params),
        };
        if (dataSetValue) {
          this.work.dataSetValue = dataSetValue;
          if (isEmpty(params.params.configuration)) {
            params.params.configuration.runtime = {
              'wds.linkis.engine.runtime.datasource': dataSetValue,
            }
          } else {
            params.params.configuration.runtime = {
              ...params.params.configuration.runtime,
              'wds.linkis.engine.runtime.datasource': dataSetValue,
            }
          }
          if (this.work.data.params.configuration && this.work.data.params.configuration.runtime) {
            this.work.data.params.configuration.runtime[
              'wds.linkis.engine.runtime.datasource'
            ] = dataSetValue;
          } else { 
            this.work.data.params.configuration.runtime = {
              'wds.linkis.engine.runtime.datasource': dataSetValue
            }
          }
          this.work.data.params.configuration.runtime[
            'wds.linkis.engine.runtime.datasource'
          ] = dataSetValue;
        } else if(!this.work.dataSetValue && params.params.configuration && params.params.configuration.runtime) {
          delete params.params.configuration.runtime['wds.linkis.engine.runtime.datasource']
        }
          // this.work.code = this.script.data;
        const isHdfs = this.work.filepath.indexOf('hdfs') === 0;
        if (this.script.data) {
          if (this.work.unsave && !isHdfs) {
            if (this.work.filepath) {
              this.work.unsave = false;
              const timeout = setTimeout(() => {
                this.saveLoading = true;
              }, 2000);
                // 保存时更新下缓存。
              if (this.script.data !== this.work.data.data) {
                this.work.data.data = this.script.data;
              }
              delete this.work.data.oldData;
              // 加入用户名来区分不同账户下的tab
              this.dispatch('IndexedDB:recordTab', { ...this.work, userName: this.userName }, () => {
                api.fetch('/filesystem/saveScript', params).then(() => {
                  clearTimeout(timeout);
                  this.saveLoading = false;
                  this.$Message.success(this.$t('message.scripts.constants.success.save'));
                  // 提交最新的内容，更新script.data和script.oldData
                  this.script.oldData = this.script.data;
                  // 保存完后，需要去设置参数的原始值，用于在子模块判断unsave的值
                  if (this.$refs.editor.$refs.setting) {
                    this.$refs.editor.$refs.setting.origin = JSON.stringify(this.script.params);
                  }

                  if (this.work.ismodifyByOldTab) {
                    this.work.ismodifyByOldTab = false;
                  }
                }).catch(() => {
                  clearTimeout(timeout);
                  this.saveLoading = false;
                  this.work.unsave = true;
                });
              });
            } else {
              // 保存时候判断是否为临时脚本
              this.$Modal.confirm({
                title: this.$t('message.scripts.confirmModal.title'),
                content: this.$t('message.scripts.confirmModal.content'),
                okText: this.$t('message.scripts.confirmModal.okText'),
                cancelText: this.$t('message.scripts.confirmModal.cancelText'),
                onOk: () => {
                  this.dispatch('Workbench:saveAs', this.work);
                },
              });
            }
          }
        } else if(auto !== true) {
          this.$Message.warning(this.$t('message.scripts.warning.empty'));
        } else {
          return
        }
        this.autoSave();
      }
    },
    showPanelTab(type) {
      // console.log('[DEBUG] showPanelTab 被调用');
      // console.log('[DEBUG] 切换到tab:', type);
      // console.log('[DEBUG] this.script.status:', this.script.status);
      // console.log('[DEBUG] this.script.steps:', this.script.steps);
      // console.log('[DEBUG] this.script.history:', this.script.history);
      // console.log('[DEBUG] this.execute:', this.execute);
      // console.log('[DEBUG] this.execute.taskID:', this.execute ? this.execute.taskID : 'undefined');
      
      const scriptViewState = {
        ...this.scriptViewState,
        showPanel: type,
      }
      this.scriptViewState = scriptViewState;
      if (type === 'progress') {
        // console.log('[DEBUG] 切换到progress tab');
        if (EXECUTE_COMPLETE_TYPE.indexOf(this.script.status) === -1 && this.script.history && this.script.steps) {
          // console.log('[DEBUG] 当前状态不是终态，检查历史记录同步');
          // 当前步骤不是终态，历史步骤是终态
          let notSync = false;
          if (this.script.history.length > 0 && this.script.steps.length > 0) {
            // console.log('[DEBUG] history[0].status:', this.script.history[0].status);
            // console.log('[DEBUG] history[0].taskID:', this.script.history[0].taskID);
            notSync = EXECUTE_COMPLETE_TYPE.indexOf(this.script.history[0].status) !== -1 && this.execute && this.script.history[0].taskID == this.execute.taskID;
            // console.log('[DEBUG] notSync:', notSync);
            if (notSync) {
              // console.log('[DEBUG] 历史记录已更新，触发queryStatus查询');
              // 进度轮询5秒钟一次，如果历史已经更新了则直接查询
              clearTimeout(this.execute.statusTimeout);
              this.execute.queryStatus({ isKill: false });
            } else if(!this.execute) {
              console.error('no execute')
            }
          }
        }
      }
      if (type === 'log') {
        const taskID = this.work.taskID || (this.script.history[0] && this.script.history[0].taskID)
        // dpms: /#/product/100199/bug/detail/222584
        // dpms: /#/product/100199/bug/detail/398827
        if (EXECUTE_COMPLETE_TYPE.indexOf(this.script.status) > -1) {
          if (this.execute && this.execute.resultsetInfo) {
            this.getLogs(this.execute.resultsetInfo)
          } else if(taskID){
            api.fetch(`/jobhistory/${taskID}/get`, 'get').then(rst => {
              this.getLogs(rst.task)
            })
          }
        } else {
          this.localLogShow();
        }
      }
    },
    updateFromHistory(task) {
      if (task && this.execute && this.execute.taskID == task.taskID) {
        let notSync = false;
        if (task && this.script.steps.length > 0) {
          // 当前步骤不是终态，历史步骤是终态
          notSync = EXECUTE_COMPLETE_TYPE.indexOf(task.status) !== -1 && EXECUTE_COMPLETE_TYPE.indexOf(this.script.status) === -1;
        }
        if (notSync && this.execute && this.execute.taskID) {
          // 进度轮询5秒钟一次，如果历史已经更新了则直接查询
          clearTimeout(this.execute.statusTimeout);
          this.execute.queryStatus({ isKill: false });
        } else if(!this.execute) {
          console.error('no execute')
        }
      }
    },
    localLogShow() {
      if (!this.debounceLocalLogShow) {
        this.debounceLocalLogShow = debounce(() => {
          if (this.localLog) {
            this.script.log = Object.freeze({
              ...this.localLog.log
            });
            this.script.logLine = this.localLog.logLine;
          }
        }, 1500);
      }
      this.debounceLocalLogShow();
    },
    configLogPanel() {
      // 兼容方案：优先使用 panel ref，如果不存在则使用 $el
      const containerEl = this.$refs.panel ? this.$refs.panel.$el : this.$el;
      let bottomContentHeight
      if (this.scriptViewState.bottomPanelFull) {
        bottomContentHeight = this._last_bottom_panel_height
      } else {
        this._last_bottom_panel_height = this.scriptViewState.bottomContentHeight
        bottomContentHeight = containerEl.clientHeight + 30
      }
      this.scriptViewState = {
        ...this.scriptViewState,
        bottomContentHeight,
        bottomPanelFull: !this.scriptViewState.bottomPanelFull
      }
    },
    changeResultSet(data, cb) {
      const resultSet = isUndefined(data.currentSet) ? this.script.resultSet : data.currentSet;
      this.script = {
        ...this.script,
        resultSet
      };
       // 同步列分页状态：当切换结果集时，重置列分页到第一页
      if (this.scriptViewState.columnPageNow && this.scriptViewState.columnPageNow !== 1) {
        this.scriptViewState.columnPageNow = 1;
      }
      this.updateResult({
        tabId: this.script.id,
        resultSet: resultSet,
        showPanel: 'result',
        ...this.script.resultList,
      });
      if (cb) {
        cb();
      }
    },
    async initHistory() {
      try {
        const rst = await api.fetch(`/jobhistory/list`, { taskID: this.work.taskID }, 'get')
        if (rst) {
          const option = rst.tasks[0];
          const supportList = this.getSupportModes();
          const supportedMode = find(supportList, (p) => p.rule.test(this.work.filename));
          try {
            if (typeof option.paramsJson === 'string') {
              option.paramsJson = JSON.parse(option.paramsJson);
            }
          } catch (error) {
            //
          }
          if (option.paramsJson && !Array.isArray(option.paramsJson) && typeof option.paramsJson.variable === 'object') {
            option.paramsJson.variable = util.convertObjectToArray(option.paramsJson.variable)
          }

          this.work.data = this.script = new Script(Object.assign({}, supportedMode, option, {
            fileName: this.work.filename,
            filepath: this.work.filepath,
            id: this.work.id,
            executionCode: option.executionCode,
            data: option.executionCode,
            params: option.paramsJson,
            readOnly: true,
            executable: false,
            configurable: false,
          }));
          this.getLogs(option);
          if (option.resultLocation) {
            this.getResult(option,'initHistory');
          }
        }
      } catch (errorMsg) {
        console.error(errorMsg)
      }
    },
    async getLogs(option) {
      if (!option.logPath) {
        this.script.log = {
          all: '',
          error: '',
          warning: '',
          info: ''
        };
        this.script.logLine = 1;
        this.script.status = option.status;
        return;
      }
      let params;
      let url;
      if (EXECUTE_COMPLETE_TYPE.indexOf(this.script.status) === -1) {
        url = `/entrance/${this.work.execID}/log`
        params = {
          fromLine: 1,
          size: -1,
        }
      } else {
        url = '/filesystem/openLog';
        params = {
          path: option.logPath,
        }
      }
      try {
        const rst = await api.fetch(url, params, 'get');
        if (rst) {
          const log = {
            all: '',
            error: '',
            warning: '',
            info: ''
          };
          const convertLogs = util.convertLog(rst.log);
          Object.keys(convertLogs).forEach((key) => {
            if (convertLogs[key]) {
              log[key] += convertLogs[key] + '\n';
            }
          });
          this.script.status = option.status;
          this.script.log = log;
          this.script.logLine = rst.fromLine;
        }
      } catch (error) {
        console.error(error)
      }
    },
    async getResult(option, parentType = '') {
      const url1 = `/filesystem/getDirFileTrees`;
      if (['Succeed','Failed','Cancelled'].indexOf(this.script.status) < 0) {
        return
      }
      const rst = await api.fetch(url1, {
        path: option.resultLocation,
      }, 'get');
      if (rst.dirFileTrees) {
        // 后台的结果集顺序是根据结果集名称按字符串排序的，展示时会出现结果集对应不上的问题，所以加上排序
        this.script.resultSet = 0
        const slice = (name) => {
          return Number(name.slice(1, name.lastIndexOf('.')));
        }
        this.script.resultList = rst.dirFileTrees.children.sort((a, b) => {
          return slice(a.name) - slice(b.name);
        });
        if (this.script.resultList.length) {
          const currentResultPath = rst.dirFileTrees.children[0].path;
          const url2 = `/filesystem/openFile`;
          if (!currentResultPath) return
          const truncateColumn = storage.get('truncateColumn_'+currentResultPath)
          const param = {
            path: currentResultPath,
            enableLimit: true,
            page: 1,
            pageSize: 5000,
          }
          if (truncateColumn !== null) {
            param.truncateColumn = truncateColumn == 1
          }
          api.fetch(url2, param, 'get').then((ret) => {
            let tmpResult
            if (ret.oversizedFields && ret.oversizedFields.length) {
              const msg = localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg;
              const fileds = ret.oversizedFields.map(it => it.fieldName)
              const content = `<p class="ellipse-p">${msg} : </br> ${fileds.join('、')}</p>`;
              if (truncateColumn === null) {
                this.$Modal.confirm({
                  title: this.$t('message.workbench.prompt'),
                  content: content,
                  okText: this.$t('message.workbench.confirm'),
                  cancelText: this.$t('message.workbench.cancel'),
                  onOk: () => {
                    storage.set('truncateColumn_'+currentResultPath, 1)
                    this.getResult(option, parentType)
                  },
                  onCancel: () => {
                    storage.set('truncateColumn_'+currentResultPath, 0)
                    this.getResult(option, parentType)
                  }
                });
              }
              tmpResult = {
                'headRows': [],
                'bodyRows': [],
                'total': 0,
                'type': '2',
                'path': currentResultPath,
                hugeData: true,
                tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg
              };
            } else if (ret.display_prohibited) {
              tmpResult = {
                'headRows': [],
                'bodyRows': [],
                'total': ret.totalLine,
                'type': ret.type,
                'path': currentResultPath,
                hugeData: true,
                tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg
              };
            } else if (ret.column_limit_display) {
              tmpResult = {
                tipMsg: localStorage.getItem("locale") === "en" ? ret.en_msg : ret.zh_msg,
                'headRows': ret.metadata,
                'bodyRows': ret.fileContent,
                'total': ret.totalLine,
                'type': ret.type,
                'totalColumn': ret.totalColumn,
                'path': currentResultPath,
              };
            } else {
              tmpResult = {
                'headRows': ret.metadata,
                'bodyRows': ret.fileContent,
                'total': ret.totalLine,
                'type': ret.type,
                'totalColumn': ret.totalColumn,
                'path': currentResultPath,
              };
            }
            this.$set(this.script.resultList[0], 'result', tmpResult);
            if(parentType !== 'initHistory') {
              this.scriptViewState.showPanel = 'result';
            }
          });
        }
      }
    },
    openPanel(type) {
      if (type === 'log') {
        this.isLogShow = true;
      }
      this.showPanelTab(type);
    },
    nodeConvertSettingParams(params) {
      const variable = isEmpty(params.variable) ? {} : util.convertArrayToObject(params.variable);
      const configuration = isEmpty(params.configuration) ? {
        special: {},
        runtime: {},
        startup: {},
      } : {
        special: params.configuration.special || {},
        runtime: params.configuration.runtime || {},
        startup: params.configuration.startup || {},
      };
      return {
        variable,
        configuration,
      };
    },
    convertSettingParams(params) {
      const variable = isEmpty(params.variable) ? {} : util.convertArrayToObject(params.variable);
      const configuration = isEmpty(params.configuration) ? {} : {
        special: {},
        runtime: {},
        startup: {},
        ...params.configuration
      };
      return {
        ...params,
        variable,
        configuration,
      };
    },
    updateResult(params) {
      this.dispatch('IndexedDB:updateResult', params);
    },
    checkAIFix: debounce(function(taskId, code, errCode, errMessage, status) {
      const baseinfo = storage.get('baseInfo', 'local')
      if (this.node) return
      if (baseinfo && !baseinfo.copilotEnable) return
      let codefixnotice
      const vm = this
      const noticeFix = (res) => {
        if (window.codefixnotice) {
          window.codefixnotice.remove('codefixnotice')
        }
        window.codefixnotice = codefixnotice = this.$Notice.warning({
          name: 'codefixnotice',
          title: this.$t('message.workbench.aiCorrection'),
          render: h => {
            return h("div", {
              style: {
                'word-wrap': 'break-word'
              }
            }, [
              res.message,
              h("div", {
                style: {
                  marginTop: "20px",
                  display: 'flex',
                  justifyContent: 'end'
                },
              }, [
              h(
                "Button",
                {
                  style: {
                    marginRight: "20px",
                  },
                  on: {
                    'click': function () {
                      codefixnotice.remove('codefixnotice')
                    }
                  }
                },
                "取消"
              ),
              h(
                "Button",
                {
                  props: {
                    type: "primary",
                  },
                  on: {
                    'click': function () {
                      vm.emitAiFix(taskId, code, errCode, errMessage)
                      codefixnotice.remove('codefixnotice')
                    }
                  }
                },
                "确认"
              )

              ])
            ]);
          },
          right: '30%',
          duration: 0
        })
      }
      if (this.script.id == this.current) {
        // 当前激活脚本
        if (taskId && EXECUTE_COMPLETE_TYPE.includes(status)) {
          storage.remove(`cache_needfix_${this.script.id}`)
          api.fetch('/copilot/executeCode', { 
            fileName: this.script.fileName,
            taskId,
            errCode
          }, 'get').then(res => {
            if (res.chatId) {
              plugin.emit('copilot_web_open_change', { 
                type: 'CodeOptimize', 
                message: '',
                params: {
                  type: this.script ? typeMap[this.script.ext] || this.script.application : '',
                  code: code,
                  scriptName: this.script.fileName,
                  taskId: taskId,
                  badjobAnalyzeIds: this.badjobAnalyzeIds,
                  chatId: res.chatId
                } 
              })
            } else if(res.report || res.badsqlAnalyze) {
              eventbus.emit('check.scriptis.analysis', {
                badjobAnalyzeIds: res.badjobAnalyzeIds,
                code,
                taskId,
                scriptName: this.script.fileName,
                type: this.script ? typeMap[this.script.ext] || this.script.application : '',
                report: res.report
              })
              this.script.history.find(item => {
                if (item.taskID == taskId) {
                  item.badsql = true
                }
              })
              this.script.history = [...this.script.history]
            } else if (res.popup) {
              noticeFix(res)
            }
            if (res.status == "diagnosed_no_report") {
              this.$Message.success('已诊断无需优化')
            }
            if (['diagnosed', 'analyzed'].indexOf(res.status) > -1) {
              storage.set(`${taskId}_analysis`, 1)
            }
          })
        }
      } else {
        if (taskId) {
          // 缓存错误信息
          storage.set(`cache_needfix_${this.script.id}`, {
            taskId, code, errCode, errMessage, status
          });
        }
      }
    }, 1000),
    emitAiFix(taskId, code, errCode, errMessage) {
      if (taskId) {
        const message = `请检查并修改如下代码中的语法错误: ${code}，执行错误信息如下: ${errMessage}`;
        plugin.emit('copilot_web_open_change', { 
          type: 'CodeCorrection', 
          message, 
          params: {
            type: this.script ? typeMap[this.script.ext] || this.script.application : '',
            code,
            taskId,
            errMessage
          } 
        })
      } else {
        const message = `请检查并修改如下代码中的语法错误: ${code}`;
        plugin.emit('copilot_web_open_change', { 
          type: 'CodeCorrection', 
          message, 
          params: {
            type: this.script ? typeMap[this.script.ext] || this.script.application : '',
            code: code || '',
            taskId: this.work.taskID || (this.script.history && this.script.history[0] && this.script.history[0].taskID)
          } 
        })
      }
    },
    // 刷新页面时重新查询诊断数据
    async refreshDiagnosisData() {
      try {
        const diagnosisRes = await api.fetch('/jobhistory/diagnosis-query', {
          taskID: this.work.taskID,
          diagnosisSource: 'doctoris'
        }, {
          method: 'get',
        });
        
        if (diagnosisRes && diagnosisRes.diagnosisMsg) {
          this.curDiagnosisRes = diagnosisRes;
          this.script.diagnosisRes = diagnosisRes;
          // 只要有problems就显示提示区域，按钮的显示由progress.vue中的hasSuggestions控制
          const diagnosisMsgJson = typeof diagnosisRes.diagnosisMsg === 'string' 
            ? JSON.parse(diagnosisRes.diagnosisMsg) 
            : diagnosisRes.diagnosisMsg;
          if (this.script.status === 'Running' && diagnosisMsgJson.data && diagnosisMsgJson.data.problems && diagnosisMsgJson.data.problems.length > 0) {
            // 提取并去重异常类型
            const problemTypes = diagnosisMsgJson.data.problems.map(p => p.type);
            this.script.diagnosisProblemTypes = [...new Set(problemTypes)];
            this.showDiagnosis = true;
          }
        } else {
          // 如果diagnosisMsg为空且脚本还在运行状态，启动轮询
          if (this.script.status === 'Running') {
            this.startDiagnosisPolling();
          }
        }
      } catch (error) {
        console.error('刷新诊断数据失败:', error);
        // 查询失败且脚本还在运行状态，也启动轮询
        if (this.script.status === 'Running') {
          this.startDiagnosisPolling();
        }
      }
    },
    // 开始事中诊断轮询
    startDiagnosisPolling() {
      // 清除之前的轮询定时器
      if (this.diagnosisPollingTimer) {
        clearTimeout(this.diagnosisPollingTimer);
      }
      
      const pollDiagnosis = async () => {
        try {
          console.log('pollDiagnosis')
          // 如果代码已运行完成，停止轮询
          if (this.script.progress && this.script.progress.costTime) {
            return;
          }
           // 如果任务状态不是Running，停止轮询
          if (this.script.status !== 'Running') {
            return;
          }
          let diagnosisRes = await api.fetch('/jobhistory/diagnosis-query', {
            taskID: this.work.taskID,
            diagnosisSource: 'doctoris'
          }, {
            method: 'get',
          });
          console.log('diagnosisRes', diagnosisRes)
          this.curDiagnosisRes = diagnosisRes;
          // 只要有problems就显示提示区域，按钮的显示由progress.vue中的hasSuggestions控制
          if (diagnosisRes && diagnosisRes.diagnosisMsg && this.script.status === 'Running') {
            const diagnosisMsgJson = JSON.parse(diagnosisRes.diagnosisMsg)
            console.log('diagnosisMsgJson', diagnosisMsgJson)
            if(diagnosisMsgJson.data.problems && diagnosisMsgJson.data.problems.length > 0){
              // 提取并去重异常类型
              const problemTypes = diagnosisMsgJson.data.problems.map(p => p.type);
              this.script.diagnosisProblemTypes = [...new Set(problemTypes)];
              this.showDiagnosis = true;
              // 缓存诊断结果到script对象
              this.script.diagnosisRes = diagnosisRes;
            }
            // 只要diagnosisMsg有数据了就停止轮询
            return;
          }
          
          // 如果diagnosisMsg为空，继续轮询
          this.diagnosisPollingTimer = setTimeout(pollDiagnosis, 30000);
        } catch (error) {
          console.error('事中诊断查询失败:', error);
          // 查询失败也继续轮询
          this.diagnosisPollingTimer = setTimeout(pollDiagnosis, 30000);
        }
      };
      
      // 开始轮询
      this.diagnosisPollingTimer = setTimeout(pollDiagnosis, 30000);
    },
    // 显示事中诊断弹窗
    showDiagnosisModal() {
      this.$refs.diagnosisModal.show();
    },
    // 处理AI诊断按钮点击事件
    handleShowDiagnosis(diagnosisRes) {
      if (!diagnosisRes || !diagnosisRes.diagnosisMsg) {
        this.showDiagnosisModal();
        return;
      }
      
      try {
        const diagnosisMsgJson = typeof diagnosisRes.diagnosisMsg === 'string' 
          ? JSON.parse(diagnosisRes.diagnosisMsg) 
          : diagnosisRes.diagnosisMsg;
        
        // 检查是否包含copilotChatId字段
        if (diagnosisMsgJson.data && 
            diagnosisMsgJson.data.suggestions && 
            diagnosisMsgJson.data.suggestions.length > 0 &&
            diagnosisMsgJson.data.suggestions[0].copilotChatId) {
              console.log('diagnosisMsgJson', diagnosisMsgJson)
              // console.log('diagnosisMsgJson.data.suggestions[0].copilotChatId', diagnosisMsgJson.data.suggestions[0].copilotChatId)
          // 包含copilotChatId，唤起copilot
          const suggestion = diagnosisMsgJson.data.suggestions[0];
          const copilotChatId = suggestion.copilotChatId;
          
          // 等效于checkAIFix弹窗点击了确认，唤起copilot
          plugin.emit('copilot_web_open_change', {
            type: 'AiSql',
            message: '',
            params: {
              type: this.script ? typeMap[this.script.ext] || this.script.application : '',
              code: this.script.data,
              scriptName: this.script.fileName,
              taskId: this.work.taskID,
              chatId: copilotChatId,
              editable: false
            }
          });
        } else {
          // 不包含copilotChatId，打开弹窗报告
          this.curDiagnosisRes = diagnosisRes;
          this.showDiagnosisModal();
        }
      } catch (error) {
        console.error('解析诊断数据失败:', error);
        // 解析失败时，打开弹窗报告
        this.curDiagnosisRes = diagnosisRes;
        this.showDiagnosisModal();
      }
    },
  },
};
</script>
<style lang="scss" scoped>
  @import '@dataspherestudio/shared/common/style/variables.scss';
  .ellipse-p {
  word-break: break-word;
  overflow-wrap: break-word;
}
  .script-container {
    height: 100%;
    width: 100%;
    overflow: hidden;
    position: relative;
  }
  .editor-panel {
    position: relative;
    .script-line {
      position: absolute;
      width: 100%;
      height: 6px;
      left: 0;
      bottom: -2px;
      z-index: 3;
      border-bottom: 1px solid #dcdee2;
      @include border-color($border-color-base, $dark-base-color);
      @include bg-color($light-base-color, $dark-base-color);
      cursor: ns-resize;
    }
    &.full-screen {
      top: 54px !important;
      right: 0 !important;
      bottom: 0 !important;
      left: 0 !important;
      position: fixed;
      z-index: 1050;
      height: calc(100% - 54px) !important;
    }
    .new-sidebar-spin {
      @include bg-color(rgba(255, 255, 255, .1), rgba(0, 0, 0, 0.5));
    }
  }
  .log-panel {
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
    }
    .workbench-tabs {
      position: $relative;
      height: 100%;
      overflow: hidden;
      box-sizing: border-box;
      z-index: 3;
      .workbench-tab-wrapper {
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
          align-items: center;
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
            @include bg-color($light-base-color, $dark-submenu-color);
            @include font-color($workspace-title-color, $dark-workspace-title-color);
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
              @include font-color($primary-color, $dark-primary-color);
              border-radius: 4px 4px 0 0;
              border-left: 1px solid $border-color-base;
              border-right: 1px solid $border-color-base;
              border-top: 1px solid $border-color-base;
              @include border-color($border-color-base, $dark-border-color-base);
            }
          }
        }
        .workbench-tab-full-btn {
          display: flex;
          justify-content: center;
          align-items: center;
          padding-right: 8px;
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
    }
  }
</style>
