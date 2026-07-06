---
title: 编码与测试规范
type: guideline
created: 2026-06-09
tags: [编码规范, 命名约定, 测试]
---

# 编码与测试规范

## 代码风格

### recommend: 使用 2 空格缩进

```javascript
// ✅ 推荐
function example() {
  if (condition) {
    doSomething()
  }
}
```

### recommend: 使用单引号

```javascript
// ✅ 推荐
const name = 'datasphere'

// ❌ 避免
const name = "datasphere"
```

### recommend: Vue 组件命名

```javascript
// ✅ 推荐 - PascalCase
import WorkflowForm from './WorkflowForm.vue'

// ✅ 推荐 - kebab-case 在模板中
<workflow-form />
```

## 命名规范

### 文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| Vue 组件 | PascalCase | `WorkflowForm.vue` |
| JS 工具 | camelCase | `formatDate.js` |
| 样式文件 | kebab-case | `workflow-form.scss` |
| 常量 | UPPER_SNAKE_CASE | `API_PATH.js` |

### 变量命名

```javascript
// ✅ 推荐
const workflowList = []
const isLoading = false
const MAX_RETRY_COUNT = 3

// ❌ 避免
const list = []
const flag = false
```

### 方法命名

```javascript
// ✅ 推荐 - 动词开头
function getWorkflowList() {}
function handleSaveClick() {}
function fetchUserData() {}

// ❌ 避免
function list() {}
function click() {}
```

## Vue 组件规范

### recommend: 组件结构

```vue
<template>
  <div class="component-name">
    <!-- 模板内容 -->
  </div>
</template>

<script>
export default {
  name: 'ComponentName',
  components: {},
  props: {},
  data() {
    return {}
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {}
}
</script>

<style lang="scss" scoped>
.component-name {
  /* 样式 */
}
</style>
```

### recommend: Props 定义

```javascript
// ✅ 推荐
props: {
  workflowId: {
    type: String,
    required: true
  },
  isVisible: {
    type: Boolean,
    default: false
  },
  listData: {
    type: Array,
    default: () => []
  }
}

// ❌ 避免
props: ['workflowId', 'isVisible']
```

### recommend: 事件命名

```javascript
// ✅ 推荐 - kebab-case
this.$emit('update-workflow', data)
this.$emit('close-modal')

// ❌ 避免
this.$emit('updateWorkflow', data)
```

## 模块开发规范

### recommend: 模块入口文件

每个模块应有 `index.js` 作为入口：

```javascript
// packages/scriptis/module/workbench/index.js
import container from './container.vue'
import scriptEditor from './script/script.vue'

export default {
  name: 'Workbench',
  components: {
    container,
    scriptEditor
  }
}
```

### recommend: 使用 moduleMixin

```javascript
// packages/scriptis/module/workbench/index.js
const module = {
  name: 'Workbench',
  events: ['onDataLoad', 'onError'],
  dispatchs: ['Workbench:socket', 'Workbench:refresh'],
  methods: {
    init() {
      // 初始化逻辑
    },
    loadData() {
      // 加载数据
    }
  }
}

export default module
```

### avoid: 跨模块直接引用

```javascript
// ❌ 避免
import { loadData } from '../../hdfsSidebar/index.js'

// ✅ 推荐 - 通过 dispatch 调用
this.dispatch('HdfsSidebar:loadData', params)
```

## 国际化规范

### recommend: 使用 i18n

```javascript
// ✅ 推荐
this.$t('message.workflow.title')

// ❌ 避免
'工作流标题'
```

### recommend: 国际化资源位置

```javascript
// packages/shared/common/i18n/zh.json
{
  "message": {
    "workflow": {
      "title": "工作流标题",
      "createSuccess": "创建成功"
    }
  }
}
```

## 错误处理

### recommend: API 调用错误处理

```javascript
// ✅ 推荐
async function fetchData() {
  try {
    const result = await api.fetch('/api/path', data)
    return result
  } catch (error) {
    this.$Message.error(error.message)
    throw error
  }
}

// ❌ 避免
async function fetchData() {
  const result = await api.fetch('/api/path', data)
  return result  // 没有错误处理
}
```

### recommend: 组件错误处理

```javascript
// ✅ 推荐
export default {
  methods: {
    async handleSave() {
      try {
        await this.saveData()
        this.$Message.success('保存成功')
      } catch (error) {
        this.$Message.error(`保存失败: ${error.message}`)
      }
    }
  }
}
```

## 存储规范

### recommend: 使用封装的 storage

```javascript
// ✅ 推荐
import storage from '@dataspherestudio/shared/common/helper/storage'

// sessionStorage
storage.set('key', value, 'session')
storage.get('key', 'session')

// localStorage
storage.set('key', value, 'local')
storage.get('key', 'local')

// cookie
storage.set('token', token, 'cookie', expired)
storage.get('token', 'cookie')
```

### avoid: 直接使用 localStorage

```javascript
// ❌ 避免
localStorage.setItem('key', JSON.stringify(value))
JSON.parse(localStorage.getItem('key'))
```

## 测试规范

### 单元测试

测试文件放置在模块的 `test/` 目录下：

```
packages/scriptis/
└── module/
    └── workbench/
        └── test/
            └── container.spec.js
```

### 测试命名

```javascript
// ✅ 推荐
describe('Workbench Container', () => {
  it('should render correctly', () => {
    // ...
  })
  
  it('should call fetch on mounted', () => {
    // ...
  })
})
```

## 注释规范

### recommend: 函数注释

```javascript
/**
 * 获取工作流列表
 * @param {Object} params - 查询参数
 * @param {string} params.workspaceId - 工作空间 ID
 * @param {string} params.projectId - 项目 ID
 * @returns {Promise<Array>} 工作流列表
 */
async function getWorkflowList(params) {
  // ...
}
```

### recommend: 复杂逻辑注释

```javascript
// 处理工作流版本回滚
// 1. 获取当前版本信息
// 2. 调用回滚接口
// 3. 刷新列表
async function rollbackWorkflow(version) {
  // ...
}
```

## 避免的模式

### avoid: 在 data 中使用 this

```javascript
// ❌ 避免
data() {
  return {
    userName: this.getUserName()  // this 可能未初始化
  }
}

// ✅ 推荐
data() {
  return {
    userName: ''
  }
},
created() {
  this.userName = this.getUserName()
}
```

### avoid: 在 v-for 中使用 index 作为 key

```vue
<!-- ❌ 避免 -->
<div v-for="(item, index) in list" :key="index">

<!-- ✅ 推荐 -->
<div v-for="item in list" :key="item.id">
```

### avoid: 直接修改 props

```javascript
// ❌ 避免
this.propsValue = newValue

// ✅ 推荐
this.$emit('update:propsValue', newValue)
```
