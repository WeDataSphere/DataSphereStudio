import DataWrangler from './scriptis/analysis/dataWrangler.vue'
import VisualAnalysis from './scriptis/analysis/visualAnalysis.vue'
import ResultToolbar from './scriptis/resultToolBar/index.vue'
import ExecHistory from './workflows/bottomTab/execHistory.vue'
import ReleaseHistory from './workflows/bottomTab/releaseHistory.vue'
import Version from './workflows/bottomTab/version.vue'
import i18n from '@dataspherestudio/shared/common/i18n'
import loginPage from './login/index.vue'
// import WA from './plugins/wa'

/**
 * 行内应用特有部分
 * https://docs.qq.com/sheet/DT1Jad2hUbFRwSVVX?tab=BB08J2&version=4.0.0.6007&platform=win
 */
export default function () {
  // // 插件通过on方法绑定事件，响应应用通过plugin.emit触发的事件
  // this.on('event-app', function(arg) {
  //   console.log('event from app', arg)
  // })
  // 插件钩子type: component, 提供组件

  // WA()
  // 替换登录页面
  this.bindHook('app_router_config', function (routes) {
    routes.forEach(item => {
      if (item.path === '/login') {
        item.component = loginPage
      }
    });
  })


  // workflows: 工作流开发底部TAB面板包括版本比对，历史版本，执行历史
  this.bindHook('workflow_bottom_panel', function () {
    return [
      {
        name: i18n.t('message.ext.webank.pubHistory'),
        icon: 'md-paper-plane',
        key: 'releaseHistory',
        component: ReleaseHistory
      }, {
        name: i18n.t('message.ext.webank.execHistory'),
        icon: 'md-reorder',
        key: 'execHistory',
        component: ExecHistory
      }, {
        name: i18n.t('message.ext.webank.Versioncomparison'),
        icon: 'md-reorder',
        key: 'version',
        component: Version
      }
    ]
  })

  this.bindHook('workflow_bottom_panel_mounted', function (context) {
    context.$parent.checkLastPublish && context.$parent.checkLastPublish(() => {
      context.minSize = false
      context.showContent = true
      const tab = context.tabs.find(item => item.key === 'releaseHistory')
      context.curTab = tab || context.tabs[0]
    })
  })

  // scriptis 结果集类型 数据分析 一键可视化
  this.bindHook('script_result_type_component', function () {
    return [{
      name: 'VisualAnalysis',
      component: VisualAnalysis
    }, {
      name: 'DataWrangler',
      component: DataWrangler
    }]
  })

  // scriptis 结果集左侧工具条菜单
  this.bindHook('script_result_toolbar', function () {
    // 添加数据可视化操作
    return [{
      name: 'ResultToolbar',
      component: ResultToolbar
    }]
  })


  // 数据库表详情
  // this.bindHook('script_dbtb_details', function({context, params}) {
  //   let url = '/dss/mide/'
  //   const workspaceId = context.$route.query.workspaceId
  //   if (params.type == 'dbDetails') {
  //     url += `#/dataBaseDetail?name=${context.currentAcitved.name}&workspaceId=${workspaceId}&noHeader=1`
  //   } else if (params.type == 'tableDetails') {
  //     url += `#/tableInfo?name=${context.currentAcitved.dbName}.${context.currentAcitved.name}&workspaceId=${workspaceId}&noHeader=1`
  //   }
  //   context.dispatch('Workbench:add', {
  //     id: params.md5,
  //     filename: params.filename,
  //     filepath: '',
  //     data: context.currentAcitved,
  //     type: 'iframe',
  //     url
  //   });
  //   return true
  // })
}
