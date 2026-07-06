---
title: 开发流程与发布规范
type: process
created: 2026-06-09
tags: [开发流程, 发布, 协作]
---

# 开发流程与发布规范

## 分支规范

### 分支类型

| 分支类型 | 命名规范 | 用途 | 生命周期 |
|---------|---------|------|---------|
| master | `master` | 生产环境分支 | 永久 |
| Release | `Release1.XX.0` | 发布分支 | 永久 |
| Feature | `feature/xxx` | 功能开发 | 合并后删除 |
| Hotfix | `hotfix/xxx` | 紧急修复 | 合并后删除 |

### 分支流程

```
master (生产)
    │
    └── Release1.23.0 (发布)
            │
            ├── feature/workflow-edit (功能开发)
            │
            └── hotfix/login-fix (紧急修复)
```

## Commit 规范

### Commit Message 格式

```
<type>: <subject>

<body>
```

### Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | `feat: 新增工作流导入功能` |
| fix | Bug 修复 | `fix: 修复登录跳转问题` |
| docs | 文档更新 | `docs: 更新 README` |
| style | 代码格式 | `style: 修复缩进` |
| refactor | 重构 | `refactor: 重构 API 调用层` |
| test | 测试 | `test: 添加单元测试` |
| chore | 构建/工具 | `chore: 更新构建配置` |

### Commit 示例

```bash
# 功能开发
git commit -m "feat: 新增工作流导入功能"

# Bug 修复
git commit -m "fix: 修复登录跳转问题"

# 多行提交
git commit -m "feat: 新增工作流导入功能" -m "- 支持 JSON 格式导入
- 支持 XML 格式导入
- 添加导入进度提示"
```

## 开发流程

### 1. 创建功能分支

```bash
# 从 Release 分支创建功能分支
git checkout Release1.23.0
git pull origin Release1.23.0
git checkout -b feature/workflow-import
```

### 2. 开发与提交

```bash
# 开发代码
npm run serve

# 代码检查
npm run lint

# 自动修复
npm run fix

# 提交代码
git add .
git commit -m "feat: 新增工作流导入功能"
```

### 3. 推送与合并

```bash
# 推送到远程
git push origin feature/workflow-import

# 创建 Merge Request
# 在 GitLab/GitHub 上创建 MR，目标分支为 Release1.23.0
```

### 4. 代码评审

- 至少 1 人 Review 通过
- 通过 CI 检查
- 无冲突

### 5. 合并与部署

```bash
# 合并到 Release 分支
# 由项目负责人操作
```

## 发布流程

### 1. 发布前准备

```bash
# 切换到 Release 分支
git checkout Release1.23.0
git pull origin Release1.23.0

# 运行测试
npm run lint

# 构建测试
npm run build-sit
```

### 2. 构建

```bash
# SIT 环境构建
npm run build-sit

# UAT 环境构建
npm run build-uat

# 生产环境构建
npm run build
```

### 3. 部署

构建产物位于 `dist/` 目录，部署到对应环境服务器。

## 环境说明

| 环境 | 分支 | 构建命令 | 说明 |
|------|------|---------|------|
| SIT | Release | `npm run build-sit` | 系统集成测试 |
| UAT | Release | `npm run build-uat` | 用户验收测试 |
| PRD | master | `npm run build` | 生产环境 |

## 子应用独立发布

### 发布 Scriptis 子应用

```bash
# 开发
npm run serve-scriptis

# 构建
npm run build-scriptis
```

### 发布调度中心

```bash
# 开发
npm run serve-scheduleCenter

# 构建
npm run build-scheduleCenter
```

## 版本号规范

### 版本格式

```
主版本号.次版本号.修订号
如：1.23.0
```

### 版本规则

| 变更类型 | 版本变化 | 示例 |
|---------|---------|------|
| 大版本更新 | 主版本号 +1 | 1.0.0 → 2.0.0 |
| 功能新增 | 次版本号 +1 | 1.22.0 → 1.23.0 |
| Bug 修复 | 修订号 +1 | 1.23.0 → 1.23.1 |

## 常用命令速查

```bash
# 查看状态
git status

# 拉取最新
git pull origin Release1.23.0

# 创建分支
git checkout -b feature/xxx

# 切换分支
git checkout Release1.23.0

# 合并分支
git merge feature/xxx

# 查看日志
git log --oneline -10

# 撤销修改
git checkout -- <file>

# 撤销提交
git reset --soft HEAD~1
```

## CI/CD 流程

### Pipeline 阶段

1. **Lint** - 代码检查
2. **Build** - 构建打包
3. **Test** - 单元测试（如有）
4. **Deploy** - 部署到环境

### 触发条件

- Push 到 Release 分支：触发 SIT 部署
- Merge Request：触发 Lint 和 Build
- Tag 创建：触发生产部署

## 检查清单

### 提交前检查

- [ ] 代码通过 ESLint 检查
- [ ] 无 console.log 调试代码
- [ ] 国际化文本已添加
- [ ] 无硬编码配置
- [ ] 敏感信息已移除

### 发布前检查

- [ ] 功能测试通过
- [ ] 兼容性测试通过
- [ ] 性能测试通过（如有要求）
- [ ] 文档已更新
- [ ] 版本号已更新
