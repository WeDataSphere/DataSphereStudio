---
title: 领域词典
type: glossary
created: 2026-06-09
tags: [术语, 词典, 业务概念]
---

# 领域词典

## 通用术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| Workspace | 工作空间 | 数据开发工作的顶层容器，包含项目、资源等 | 通用 |
| Project | 项目 | 工作空间下的业务项目，包含工作流、脚本等 | 通用 |
| Orchestrator | 编排器 | 工作流的编排容器，定义工作流的类型和模式 | Workflow |
| Workflow | 工作流 | 由多个节点组成的有向无环图（DAG），定义数据处理流程 | Workflow |
| Node | 节点 | 工作流中的单个任务单元 | Workflow |
| DSS Label | DSS 标签 | 环境标识，用于区分开发、测试、生产环境 | 通用 |

## Scriptis 术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| Script | 脚本 | 用户编写的代码文件，如 SQL、Python 等 | Scriptis |
| Workbench | 工作台 | 脚本编辑和执行的主界面 | Scriptis |
| Result Set | 结果集 | 脚本执行后返回的数据结果 | Scriptis |
| UDF | 用户定义函数 | User Defined Function，用户自定义的函数 | Scriptis |
| Engine | 引擎 | 执行任务的计算引擎，如 Spark、Hive | Scriptis |
| Job | 作业 | 一次任务执行的实例 | Scriptis |
| Log | 日志 | 任务执行过程中的输出信息 | Scriptis |
| Progress | 进度 | 任务执行的完成百分比 | Scriptis |
| HDFS | HDFS | Hadoop 分布式文件系统 | Scriptis |
| Hive | Hive | 基于 Hadoop 的数据仓库工具 | Scriptis |
| Spark | Spark | 大数据处理引擎 | Scriptis |

## Workflow 术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| DAG | 有向无环图 | Directed Acyclic Graph，工作流的拓扑结构 | Workflow |
| Process | 流程实例 | 工作流的一次运行实例 | Workflow |
| Version | 版本 | 工作流的版本记录，支持回滚 | Workflow |
| Publish | 发布 | 将工作流发布到生产环境 | Workflow |
| Schedule | 调度 | 工作流的定时执行配置 | Workflow |
| Orchestrator Mode | 编排模式 | 工作流的类型，如生产调度、实时计算等 | Workflow |
| Orchestrator Ways | 编排方式 | 工作流的实现方式 | Workflow |
| Release | 发布版本 | 已发布的工作流版本 | Workflow |
| Rollback | 回滚 | 将工作流恢复到历史版本 | Workflow |
| Template | 模板 | 工作流模板，用于快速创建工作流 | Workflow |

## 技术相关术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| EventBus | 事件总线 | 模块间通信的事件发布订阅机制 | 技术 |
| Dispatch | 派发 | 模块间的事件触发机制 | 技术 |
| Module | 模块 | 功能单元，包含组件、服务、状态等 | 技术 |
| Mixin | 混入 | Vue 组件复用机制 | 技术 |
| IndexedDB | IndexedDB | 浏览器端 NoSQL 数据库 | 技术 |
| WebSocket | WebSocket | 双向通信协议，用于实时推送 | 技术 |
| Monaco Editor | Monaco 编辑器 | VS Code 同款代码编辑器 | 技术 |
| Butterfly DAG | Butterfly DAG | 阿里开源的 DAG 流程图编辑器 | 技术 |

## 权限相关术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| Admin | 管理员 | 系统管理员角色 | 权限 |
| Edit User | 编辑用户 | 有编辑权限的用户 | 权限 |
| View User | 查看用户 | 只有查看权限的用户 | 权限 |
| Priv | 权限 | Permission，操作权限标识 | 权限 |
| Releasable | 可发布 | 是否有发布权限 | 权限 |
| Editable | 可编辑 | 是否有编辑权限 | 权限 |

## 数据源相关术语

| 英文名 | 中文名 | 定义 | 所属领域 |
|-------|-------|------|---------|
| Datasource | 数据源 | 数据库连接配置 | 数据 |
| Database | 数据库 | 数据库实例 | 数据 |
| Table | 表 | 数据库表 | 数据 |
| Schema | 模式 | 数据库模式/命名空间 | 数据 |
| Partition | 分区 | 表分区 | 数据 |
