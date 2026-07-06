---
title: 技术栈与目录结构
type: model
created: 2026-06-09
tags: [技术栈, 架构, 目录结构]
---

# 技术栈与目录结构

## 技术栈

### 核心框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 2.6.12 | 前端框架 |
| Vue Router | 3.4.8 | 路由管理 |
| Vuex | - | 状态管理（隐式使用） |
| iView | 3.5.4 | UI 组件库 |
| Vue i18n | 8.22.1 | 国际化 |

### 开发工具

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue CLI | 3.12.1 | 脚手架工具 |
| Webpack | 4.46.0 | 构建打包 |
| Babel | - | 语法转换 |
| ESLint | 6.8.0 | 代码检查 |

### 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| axios | 1.12.0 | HTTP 请求 |
| butterfly-dag | 4.1.23 | DAG 流程图编辑 |
| monaco-editor | 0.30.1 | 代码编辑器 |
| echarts | ^4.1.0 | 图表可视化 |
| lodash | 4.17.21 | 工具函数库 |
| moment | 2.30.1 | 日期处理 |
| dexie | 3.0.4 | IndexedDB 封装 |
| dt-sql-parser | 1.2.1 | SQL 语法解析 |

## Monorepo 结构

本项目采用 **npm workspaces** 管理多包结构。

### 聚焦子包

| 子包 | 路径 | 职责 |
|------|------|------|
| **dss** | `packages/dss/` | 主应用入口框架，路由编排与布局 |
| **scriptis** | `packages/scriptis/` | 脚本开发与交互式数据分析 |
| **workflows** | `packages/workflows/` | 工作流可视化编排与管理 |
| **shared** | `packages/shared/` | 公共组件、工具、服务层 |

### 关联子包

| 子包 | 路径 | 职责 |
|------|------|------|
| editor | `packages/editor/` | 代码编辑器核心 |
| editorLsp | `packages/editorLsp/` | LSP 语言服务客户端 |
| scheduleCenter | `packages/scheduleCenter/` | 调度中心 |
| apiServices | `packages/apiServices/` | API 服务管理 |
| workspace | `packages/workspace/` | 工作空间管理 |
| cyeditor | `packages/cyeditor/` | Cytoscape 图编辑器 |
| exts | `packages/exts/` | 扩展模块 |

## 目录结构

```
packages/
├── dss/                          # 主应用入口
│   ├── src/
│   │   ├── main.js              # 应用启动入口
│   │   ├── router.js            # 路由配置
│   │   ├── common-router.js     # 公共路由
│   │   └── dynamic-apps.js      # 动态模块加载
│   ├── module/
│   │   ├── header/              # 顶部导航
│   │   ├── footer/              # 底部页脚
│   │   ├── indexedDB/           # 本地存储
│   │   └── newGuidance/         # 新手引导
│   └── view/                    # 视图组件
│
├── scriptis/                     # 脚本开发模块
│   ├── module/
│   │   ├── workbench/           # 工作台（核心）
│   │   │   ├── script/          # 脚本编辑
│   │   │   ├── result/          # 结果集
│   │   │   ├── dbDetails/       # 数据库详情
│   │   │   └── tableDetails/    # 表详情
│   │   ├── webSocket/           # WebSocket 通信
│   │   ├── workSidebar/         # 工作目录侧边栏
│   │   ├── hdfsSidebar/         # HDFS 浏览器
│   │   ├── hiveSidebar/         # Hive 浏览器
│   │   └── logView/             # 日志查看
│   ├── service/                 # API 服务
│   └── view/                    # 视图组件
│
├── workflows/                    # 工作流模块
│   ├── module/
│   │   ├── workflow/            # 工作流核心
│   │   ├── process/             # 流程实例管理
│   │   ├── ide/                 # IDE 集成
│   │   └── innerIframe/         # 内嵌 iframe
│   ├── service/                 # API 服务
│   └── view/                    # 视图组件
│
└── shared/                       # 公共模块
    ├── components/              # 公共组件（27 个）
    │   ├── table/               # 表格组件
    │   ├── dynamicForm/         # 动态表单
    │   ├── log/                 # 日志组件
    │   ├── menu/                # 菜单组件
    │   └── svgIcon/             # SVG 图标
    └── common/
        ├── service/             # 公共服务
        │   ├── api.js           # HTTP 请求封装
        │   ├── socket.js        # WebSocket 封装
        │   ├── storage.js       # 存储封装
        │   └── moduleMixin.js   # 模块混入
        ├── util/                # 工具函数
        ├── config/              # 全局配置
        ├── i18n/                # 国际化资源
        ├── helper/              # 辅助工具
        │   ├── eventbus.js      # 事件总线
        │   └── storage.js       # 存储 helper
        └── style/               # 全局样式
```

## 架构特点

### 1. 动态模块加载

通过 `dynamic-apps.js` 实现按需打包：

```bash
# 打包指定模块
npm run build --module=scriptis --micro_module=scriptis
```

### 2. 模块间通信

使用 `eventbus` 实现模块解耦：

```javascript
// 发送事件
eventbus.emit('event:name', payload)

// 监听事件
eventbus.on('event:name', handler)
```

### 3. 公共服务层

所有 API 请求统一通过 `shared/common/service/api.js`：

```javascript
import api from '@dataspherestudio/shared/common/service/api'

api.fetch('/api/path', data, 'post')
```

### 4. 国际化

资源集中在 `shared/common/i18n/`，支持中英文双语。

## 依赖关系图

```
                    ┌─────────┐
                    │   dss   │
                    └────┬────┘
                         │
           ┌─────────────┼─────────────┐
           │             │             │
           ▼             ▼             ▼
     ┌──────────┐  ┌──────────┐  ┌──────────┐
     │ scriptis │  │workflows │  │ workspace│
     └────┬─────┘  └────┬─────┘  └────┬─────┘
          │             │             │
          └─────────────┼─────────────┘
                        │
                        ▼
                  ┌──────────┐
                  │  shared  │
                  └──────────┘
```
