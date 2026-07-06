---
title: 架构决策与技术选型
type: decision
created: 2026-06-09
tags: [架构, 技术选型, 决策]
---

# 架构决策与技术选型

## 1. Monorepo 架构

### 决策

采用 **npm workspaces** 管理 Monorepo 结构。

### 理由

- 统一依赖版本管理，避免版本冲突
- 公共代码抽取到 shared 包，减少重复
- 支持按需打包子应用，减少构建产物体积
- 模块边界清晰，职责分明

### 否决方案

- **单包应用**：代码耦合严重，难以维护
- **pnpm workspaces**：团队对 npm 更熟悉，迁移成本高

## 2. 模块间通信机制

### 决策

采用 **EventBus + Dispatch** 模式实现模块间解耦通信。

### 理由

- 模块间松耦合，可独立开发测试
- 支持一对多事件订阅
- 通过 `moduleMixin.js` 统一管理模块生命周期

### 实现方式

```javascript
// 模块定义
const module = {
  name: 'Workbench',
  events: ['onDataLoad'],
  dispatchs: ['Workbench:socket'],
  methods: {
    init() { /* ... */ }
  }
}

// 事件监听
eventbus.on('Workbench:socket', handler)

// 事件触发
module.dispatch('Workbench:socket', data)
```

## 3. HTTP 请求封装

### 决策

基于 **axios** 封装统一的 API 请求层。

### 核心特性

| 特性 | 说明 |
|------|------|
| 请求拦截 | 自动添加 token、语言参数 |
| 响应拦截 | 统一错误处理、状态码判断 |
| 重复请求取消 | 通过 AbortController 实现 |
| 请求缓存 | 支持 cacheOptions 配置 |
| 超时控制 | 默认 600s 超时 |

### 否决方案

- **fetch API**：需要手动封装太多功能
- **umi-request**：与现有 Vue 生态整合不如 axios

## 4. 代码编辑器选型

### 决策

采用 **Monaco Editor** 作为代码编辑器核心。

### 理由

- VS Code 同款编辑器，功能强大
- 原生支持 LSP 协议
- 支持多语言语法高亮
- 集成 dt-sql-parser 实现 SQL 语法解析

### 配套方案

- `monaco-languageclient`：LSP 客户端
- `dt-sql-parser`：SQL 语法解析
- `editorLsp`：语言服务客户端封装

## 5. DAG 流程图编辑

### 决策

采用 **butterfly-dag** 作为流程图编辑器。

### 理由

- 专为数据开发场景设计
- 支持拖拽、缩放、连线
- 丰富的节点类型
- 蚂蚁集团开源，稳定可靠

## 6. 国际化方案

### 决策

采用 **vue-i18n** + 集中式资源管理。

### 理由

- Vue 官方推荐方案
- 支持动态语言切换
- 资源集中在 shared 包，便于维护

### 实现方式

```javascript
// shared/common/i18n/zh.json
{
  "message": {
    "scripts": {
      "title": "脚本开发"
    }
  }
}

// 使用
this.$t('message.scripts.title')
```

## 7. UI 组件库

### 决策

采用 **iView 3.x** 作为 UI 组件库。

### 理由

- Vue 2 生态成熟方案
- 组件丰富，文档完善
- 支持主题定制

### 注意事项

- 版本锁定为 3.5.4，避免升级到 4.x 的破坏性变更
- 主题定制通过 `shared/common/style/theme/` 管理

## 8. 状态管理

### 决策

不强制使用 Vuex，采用 **EventBus + 组件状态** 的轻量级方案。

### 理由

- 大部分状态为组件局部状态
- 跨组件状态通过 EventBus 同步
- 减少样板代码

### 适用场景

- 跨模块状态：使用 EventBus
- 组件内状态：使用 data/computed
- 持久化状态：使用 storage 或 IndexedDB

## 9. 构建工具

### 决策

采用 **Vue CLI 3.x** + Webpack 4。

### 理由

- Vue 官方脚手架，生态完善
- 配置简单，支持自定义
- 已有项目积累

### 否决方案

- **Vite**：Vue 2 支持有限，迁移成本高
- **Webpack 5**：升级收益不明显

## 10. 动态模块加载

### 决策

通过 `dynamic-apps.js` + webpack virtual modules 实现按需打包。

### 理由

- 支持子应用独立部署
- 减少构建产物体积
- 灵活的模块组合

### 使用方式

```bash
# 打包 Scriptis 子应用
npm run build --module=scriptis --micro_module=scriptis

# 打包调度中心
npm run build --module=scheduleCenter,workflows --micro_module=scheduleCenter
```
