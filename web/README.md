# DataSphereStudio Web

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

English | [中文](docs/zh_CN/README.md)

## 简介

DataSphereStudio Web 是一个基于 Vue 2 的数据开发工作台前端项目，采用 Monorepo 架构，包含脚本开发（Scriptis）和工作流编排（Workflow）两大核心模块。

## 快速开始

### 环境要求

- Node.js 14.x - 16.x
- npm 6.x+

### 安装依赖

```bash
npm install
```

### 开发环境启动

```bash
# 主应用
npm run serve

# Scriptis 子应用
npm run serve-scriptis

# 调度中心
npm run serve-scheduleCenter
```

### 构建

```bash
# 生产环境
npm run build

# SIT 环境
npm run build-sit

# UAT 环境
npm run build-uat
```

### 代码检查

```bash
npm run lint    # 检查
npm run fix     # 自动修复
```

## 项目结构

本项目采用 npm workspaces 管理 Monorepo 结构：

| 子包 | 说明 |
|------|------|
| packages/dss | 主应用入口框架 |
| packages/scriptis | 脚本开发与数据分析 |
| packages/workflows | 工作流编排与管理 |
| packages/shared | 公共组件与服务 |

## 核心功能

### Scriptis（脚本开发）

- 多语言脚本编辑（SQL、PySpark、HiveQL）
- 任务提交与执行（Spark、Hive、Python）
- 结果集展示与可视化
- 数据库管理
- UDF 与函数管理

### Workflow（工作流）

- DAG 工作流可视化编排
- 版本管理与回滚
- 流程实例监控
- 调度配置与发布

## 技术栈

| 技术 | 版本 |
|------|------|
| Vue | 2.6.12 |
| Vue Router | 3.4.8 |
| iView | 3.5.4 |
| Vue CLI | 3.12.1 |
| Monaco Editor | 0.30.1 |
| Butterfly DAG | 4.1.23 |

## 文档

- [技术文档](docs/tech-wiki/index.md) - 技术栈、架构设计、编码规范
- [业务文档](docs/biz-wiki/index.md) - 业务领域知识、流程说明
- [API 规范](docs/tech-wiki/api.md) - 接口调用规范

## 许可证

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
