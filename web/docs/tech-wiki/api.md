---
title: API 与集成规范
type: model
created: 2026-06-09
tags: [API, HTTP, 接口规范]
---

# API 与集成规范

## HTTP 请求封装

### 基础配置

位于 `packages/shared/common/service/api.js`。

```javascript
// axios 实例配置
const instance = axios.create({
  baseURL: '/api/rest_j/v1/',
  timeout: 600000,        // 10 分钟超时
  withCredentials: true,  // 携带 cookie
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})
```

### 请求拦截

```javascript
// 自动添加的 headers
config.headers['Content-language'] = 'zh-CN'  // 国际化
config.headers['token'] = api.getToken()      // DolphinScheduler token
```

### 响应处理

```javascript
// 响应结构约定
{
  status: 0,          // 状态码，0 表示成功
  message: 'success', // 消息
  data: {}            // 数据
}

// 特殊情况：DolphinScheduler
{
  code: 0,
  msg: 'success',
  data: {}
}
```

## API 调用方式

### 基本调用

```javascript
import api from '@dataspherestudio/shared/common/service/api'

// GET 请求
api.fetch('/api/path', data, 'get')

// POST 请求
api.fetch('/api/path', data, 'post')

// 带配置
api.fetch('/api/path', data, {
  method: 'post',
  removeCache: true,  // 取消重复请求
  cacheOptions: {     // 启用缓存
    time: 60000
  }
})
```

### 错误处理

```javascript
try {
  const result = await api.fetch('/api/path', data)
} catch (error) {
  // error.message 包含错误信息
  // error.solution 包含解决方案（如有）
  console.error(error.message)
}
```

## API 路径常量

位于 `packages/shared/common/config/apiPath.js`。

```javascript
import API_PATH from '@dataspherestudio/shared/common/config/apiPath.js'

API_PATH.WORKSPACE_PATH     // '/dss/framework/workspace/'
API_PATH.PROJECT_PATH       // '/dss/framework/project/'
API_PATH.ORCHESTRATOR_PATH  // '/dss/framework/orchestrator/'
API_PATH.WORKFLOW_PATH      // '/dss/workflow/'
API_PATH.PUBLISH_PATH       // '/dss/framework/release/'
API_PATH.DATASOURCE_PATH    // '/dss/data/api/datasource/'
```

## 推荐做法

### recommend: 统一使用 api.fetch

```javascript
// ✅ 推荐
import api from '@dataspherestudio/shared/common/service/api'
const result = await api.fetch('/api/path', data)
```

### avoid: 直接使用 axios

```javascript
// ❌ 避免
import axios from 'axios'
const result = await axios.post('/api/path', data)
```

### recommend: 使用 API_PATH 常量

```javascript
// ✅ 推荐
api.fetch(`${this.$API_PATH.WORKFLOW_PATH}getAllOrchestrator`, params)

// ❌ 避免
api.fetch('/dss/workflow/getAllOrchestrator', params)
```

### recommend: 错误处理

```javascript
// ✅ 推荐
try {
  const result = await api.fetch('/api/path', data)
  // 处理结果
} catch (error) {
  this.$Message.error(error.message)
}

// ❌ 避免
api.fetch('/api/path', data).then(result => {
  // 没有错误处理
})
```

## 重复请求处理

### 启用请求取消

```javascript
api.fetch('/api/path', data, {
  method: 'post',
  removeCache: true  // 相同请求自动取消上一个
})
```

### 适用场景

- 搜索框实时查询
- 表单重复提交
- 列表频繁刷新

## 请求缓存

### 启用缓存

```javascript
api.fetch('/api/path', data, {
  cacheOptions: {
    time: 60000  // 缓存 1 分钟
  }
})
```

### 适用场景

- 字典数据
- 配置信息
- 不常变化的数据

## WebSocket 通信

### 基本使用

```javascript
import Socket from '@dataspherestudio/shared/common/service/socket'

const socket = new Socket({
  url: `ws://${location.host}/ws/path`,
  errHandler: (error) => {
    console.error(error)
  }
})

// 监听消息
socket.on('data', (data) => {
  console.log(data)
})

// 发送消息
socket.send({ method: 'execute', data: {} })

// 关闭连接
socket.close()
```

### 自动降级

当 WebSocket 连接失败时，自动降级为 HTTP 轮询。

## 模块间通信

### EventBus

```javascript
import eventbus from '@dataspherestudio/shared/common/helper/eventbus'

// 监听事件
eventbus.on('event:name', (payload) => {
  console.log(payload)
})

// 触发事件
eventbus.emit('event:name', { data: 'value' })

// 取消监听
eventbus.off('event:name', handler)
```

### Dispatch 模式

```javascript
// 模块配置
const module = {
  name: 'Workbench',
  dispatchs: ['Workbench:socket', 'Workbench:refresh'],
  methods: {
    init() { /* ... */ }
  }
}

// 触发 dispatch
this.dispatch('Workbench:refresh', data)
```

## 外部系统集成

### iframe 通信

```javascript
// 发送消息
window.parent.postMessage({
  type: 'event_name',
  data: payload
 }, '*')

// 接收消息
window.addEventListener('message', (event) => {
  if (event.data.type === 'event_name') {
    // 处理消息
  }
})
```

### 内嵌 iframe

使用 `packages/workflows/module/innerIframe/` 组件处理 iframe 嵌入。
