import Vue from 'vue'
import i18n from '@dataspherestudio/shared/common/i18n'
import api from '@dataspherestudio/shared/common/service/api'
import storage from "@dataspherestudio/shared/common/helper/storage"

// 项目管理路由
const projectManageRoute = {
  path: '/projectManage',
  name: 'ProjectManage',
  meta: {
    publicPage: true
  },
  component: () =>
    import('./projectManage/index.vue'),
};

/**
 * 插件绑定
 */
export default function () {
  // 增加项目管理操作历史页面
  this.bindHook('app_router_config', function (routes) {
    routes[0].children.push(projectManageRoute)
  })

  // 切换生产中心运维代理用户展示及切换按钮
  this.bindHook('app_router_afterchange', function ({ to }) {
    if (to && to.path === '/scheduleCenter') {
      const baseInfo = storage.get("baseInfo", "local") || {}
      this.proxyUser = baseInfo.proxyUserName || ''
      setTimeout(function () {
        let body = document.querySelector('.bottomRightContainer');
        if (body.querySelector('#set_proxy_modal_btn')) return
        let bindPhone = document.createElement('div')
        bindPhone.setAttribute('id', 'proxy_modal_btn')
        body.appendChild(bindPhone)
        return new Vue({
          render: (h) => {
            return h(
              'div', {
                style: {
                  color: '#2d8cf0',
                  position: 'absolute',
                  display: baseInfo.proxyUserName ? 'block' : 'none',
                  right: '30px'
                },
                attrs: {
                  'id': 'set_proxy_modal_btn'
                },
                on: {
                  click: function () {
                    // createProxyModal()
                  }
                }
              }, [
                h("Icon",
                  {
                    props: {
                      type: 'ios-settings',
                      size: 16,
                      color: '#2d8cf0'
                    }
                  }),
                baseInfo.proxyUserName ? `代理用户${baseInfo.proxyUserName}` : ''
              ]
            )
          }
        }).$mount('#proxy_modal_btn')
      }, 1000)
    }
  })
  // 生产中心左侧项目列表中项目的菜单
  this.bindHook('scheduler_center_project_menu', function () {
    return {
      value: 'manage',
      text: i18n.t('message.ext.bdp.Manage'),
      func: function (context, item) {
        context.$router.push({
          path: '/projectManage',
          query: {
            ...context.$route.query,
            projectID: item.id,
            projectName: item.name
          }
        })
      }
    }
  })
  // 生产中心工作流列表项操作增加删除工作流
  this.bindHook('scheduler_center_workflow_action', function ({ context, cols, h, params }) {
    const deleteWorkflow = function (row) {
      const data = {
        id: row.orchestratorId,
        projectId: row.projectId,
        workspaceId: +context.$route.query.workspaceId,
        labels: { route: 'prod' }
      }
      api
        .fetch(
          `${context.$API_PATH.ORCHESTRATOR_PATH}deleteOrchestrator`,
          data,
          "post"
        )
        .then(() => {
          if (typeof context.search === 'function') context.search()
          context.$Message.success(
            context.$t("message.workflow.deleteSuccessName", {
              name: row.orchestratorName
            })
          );
        })
    }
    cols.splice(2, 0, h(
      "Button",
      {
        props: {
          type: "error",
          size: "small",
        },
        style: {
          marginRight: "5px",
        },
        on: {
          click: () => {
            const content = `<div>
              <p style="margin-bottom:10px">${context.$t("message.orchestratorModes.confirmDeleteOrchestrator")}${params.row.orchestratorName}?</p>
              <p>${context.$t('message.scheduleCenter.Simultaneously')}</p>
            </div>`
            context.$Modal.confirm({
              title: context.$t('message.orchestratorModes.deleteOrchestrator'),
              content,
              okText: context.$t('message.common.ok'),
              cancelText: context.$t('message.common.cancel'),
              onOk: () => {
                deleteWorkflow(params.row)
              },
              onCancel: () => { }
            })
          },
        },
      },
      context.$t("message.workflow.deleteWorkflow")
    ))
  })
}
