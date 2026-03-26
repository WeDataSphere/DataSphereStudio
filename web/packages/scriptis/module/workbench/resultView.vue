<template>
  <div style="width:100%;height:100%">
    <div v-if="loading" class="loading-text">
      请耐心等待，数据正在加载中... <Spin />
    </div>
    <div v-if="info" class="info-text">
      <Alert type="info" show-icon>{{ info }}</Alert>
    </div>
    <div v-if="error" class="error-text">
      <Alert type="error" show-icon>{{ error }}</Alert>
    </div>
    <result
      v-if="!loading && !error && !info && (script.result.path || script.resultList.length > 0)"
      ref="result"
      getResultUrl="filesystem"
      :script="script"
      :work="work"
      :dispatch="dispatch"
      :script-view-state="scriptViewState"
      @loadDataDone="loading=false"
      @on-set-change="changeResultSet"
    />
  </div>
</template>

<script>
import api from '@dataspherestudio/shared/common/service/api'
import result from '@dataspherestudio/shared/components/consoleComponent/result.vue'
import SUPPORTED_LANG_MODES from '@dataspherestudio/shared/common/config/scriptis'
import { find, debounce } from 'lodash'
import mixin from '@dataspherestudio/shared/common/service/mixin'
import util from '@dataspherestudio/shared/common/util';

export default {
  name: 'ResultView',
  components: {
    result,
  },
  mixins: [mixin],
  data() {
    return {
      scriptViewState: {
        topPanelHeight: 0,
        bottomContentHeight: '600',
        topPanelFull: false,
        showPanel: 'result',
        bottomPanelFull: false,
        cacheLogScroll: 0,
        columnPageNow: 1,
      },
      script: {
        id: '',
        fileName: '',
        runType: 'hql',
        scriptType: 'hive',
        data: '',
        oldData: '',
        result: {
          headRows: [],
          bodyRows: [],
          type: 'RES_EMPTY',
          total: 0,
          cache: {},
        },
        resultList: [],
        resultSet: 0,
        steps: [],
        progress: {},
        params: {},
        readOnly: true,
        executable: false,
        configurable: false,
      },
      work: {
        taskID: '',
        data: {
          history: []
        }
      },
      loading: false,
      error: null,
      info: null,
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.getHeight)
  },
  mounted() {
    // 监听窗口变化，获取浏览器宽高
    window.addEventListener('resize', this.getHeight)
    this.scriptViewState.bottomContentHeight = this.$el.clientHeight
    this.initData()
  },
  methods: {
    getHeight: debounce(function() {
      this.scriptViewState.bottomContentHeight = this.$el.clientHeight
    }, 300),

    // 初始化数据
    async initData() {
      try {
        // 1. 参数验证
        const taskId = this.$route.query.taskId
        if (!taskId) {
          this.error = '缺少必要参数 taskId'
          this.info = null
          return
        }

        this.loading = true
        this.work.taskID = taskId
        this.script.id = taskId

        // 2. 判断是否提供了 resultPath
        const resultPath = this.$route.query.resultPath
        if (resultPath) {
          // 直接使用 resultPath
          this.script.result.path = resultPath
          this.script.resultList = [{
            path: resultPath,
          }]
          this.loading = false
        } else {
          // 3. 通过 taskId 获取任务信息
          await this.getTaskInfo(taskId)
        }
      } catch (error) {
        console.error('初始化数据失败:', error)
        this.error = `加载数据失败: ${error.message || '未知错误'}`
        this.loading = false
      }
    },

    // 通过 taskId 获取任务信息
    async getTaskInfo(taskId) {
      try {
        const rst = await api.fetch(`/jobhistory/list`, { taskID: taskId }, 'get')
        
        if (!rst || !rst.tasks || rst.tasks.length === 0) {
          this.error = '任务不存在或已被删除'
          this.loading = false
          return
        }

        const option = rst.tasks[0]
        
        if (option.status === 'Running') {
          this.info = `任务执行中，请稍后。`
          this.error = null
          this.loading = false
          return
        }

        // 检查任务状态
        if (option.status !== 'Succeed') {
          this.error = `任务执行失败，状态: ${option.status}`
          this.info = null
          this.loading = false
          return
        }

        // 检查是否有结果位置
        if (!option.resultLocation) {
          this.error = '任务没有生成结果集'
          this.loading = false
          return
        }

        // 获取支持的脚本模式
        const supportList = SUPPORTED_LANG_MODES
        const supportedMode = find(supportList, (p) => p.rule.test(option.fileName)) || {}

        // 解析参数
        let paramsJson = option.paramsJson
        if (typeof paramsJson === 'string') {
          try {
            paramsJson = JSON.parse(paramsJson)
          } catch (e) {
            console.error('解析参数失败:', e)
            paramsJson = {}
          }
        }

        // 转换变量参数格式
        if (paramsJson && !Array.isArray(paramsJson) && typeof paramsJson.variable === 'object') {
          paramsJson.variable = util.convertObjectToArray(paramsJson.variable)
        }

        // 设置脚本信息
        this.script = {
          ...this.script,
          fileName: option.fileName,
          executionCode: option.executionCode,
          data: option.executionCode,
          params: paramsJson,
          status: option.status,
          readOnly: true,
          executable: false,
          configurable: false,
          runType: supportedMode.runType || 'hql',
          scriptType: supportedMode.scriptType || 'hive',
        }

        // 获取结果数据
        await this.getResult(option)
      } catch (error) {
        console.error('获取任务信息失败:', error)
        this.error = `获取任务信息失败: ${error.message || '未知错误'}`
        this.info = null
        this.loading = false
      }
    },

    // 获取结果数据
    async getResult(option) {
      try {
        const url1 = `/filesystem/getDirFileTrees`
        const rst = await api.fetch(url1, {
          path: option.resultLocation,
        }, 'get')

        if (!rst || !rst.dirFileTrees || !rst.dirFileTrees.children) {
          this.error = '结果集目录不存在'
          this.info = null
          this.loading = false
          return
        }

        // 后台的结果集顺序是根据结果集名称按字符串排序的，展示时会出现结果集对应不上的问题，所以加上排序
        this.script.resultSet = 0
        const slice = (name) => {
          return Number(name.slice(1, name.lastIndexOf('.')))
        }
        this.script.resultList = rst.dirFileTrees.children.sort((a, b) => {
          return slice(a.name) - slice(b.name)
        })

        if (this.script.resultList.length === 0) {
          this.error = '结果集为空'
          this.info = null
          this.loading = false
          return
        }

        // 设置第一个结果集的路径
        const currentResultPath = rst.dirFileTrees.children[0].path
        this.script.result.path = currentResultPath

        this.loading = false
      } catch (error) {
        console.error('获取结果数据失败:', error)
        this.error = `获取结果数据失败: ${error.message || '未知错误'}`
        this.info = null
        this.loading = false
      }
    },

    // 切换结果集
    changeResultSet(data, cb) {
      const resultSet = data.currentSet || 0
      this.script = {
        ...this.script,
        resultSet
      }
      // 同步列分页状态：当切换结果集时，重置列分页到第一页
      if (this.scriptViewState.columnPageNow && this.scriptViewState.columnPageNow !== 1) {
        this.scriptViewState.columnPageNow = 1
      }
      if (cb) {
        cb()
      }
    },
  }
}
</script>

<style scoped>
.loading-text {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  height: 100px;
  padding: 20px;
  width: 100%;
  z-index: 2;
  font-size: 14px;
  color: #666;
}

.error-text {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  height: 100px;
  padding: 20px;
  width: 100%;
  z-index: 2;
}

.info-text {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  height: 100px;
  padding: 20px;
  width: 100%;
  z-index: 2;
}
</style>
