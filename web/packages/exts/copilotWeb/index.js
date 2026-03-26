import CopilotEntry from './copilotEntry'
import CopilotContainer from './copilotContainer.vue'
import EventTable from './event/table'
/**
 * 插件绑定
 */
export default function () {
  // 插件钩子type: component, 提供组件
  // workflows: 工作流开发底部TAB面板复制历史
  this.bindHook('copilot_web_component', function () {
    return {
      copilotContainerComponent: {
        key: 'CopilotContainer',
        component: CopilotContainer
      },
      copilotEntryComponent: {
        key: 'CopilotEntry',
        component: CopilotEntry
      }
    }
  })
  this.bindHook('copilot_web_listener_event_remove', function () {
    ['copilot_web_open_change', 'copilot_web_listener_inster', 'copilot_web_listener_create', 'copilot_web_listener_createAndInster'].forEach(item => {
      this.clear(item)
    })
  })
  this.bindHook('get_copilot_web_listener_event_class', function (type) {
    switch (type) {
      case 'table':
        return EventTable
      default:
        break;
    }
  })
  /**
   * events: [{ eventType: '', args: {} }]
   * eventType: "queryStructure"\"viewTableData"\"createAndInster"\"inster"
   */
  this.on('copilot_web_listener_event', (data) => {
    try {
      console.warn('----copilot_web_listener_even---', data)
      const { params } = data || {}
      if (params && params.events) {
        params.events.forEach(item => {
          console.log('emit-'+item.eventType)
          this.emit(`copilot_web_listener_${item.eventType}`, item.args)
        });
      }
    } catch (error) {
      console.error('----copilot_web_listener_event---error-', error)
    }
    
  })
}
