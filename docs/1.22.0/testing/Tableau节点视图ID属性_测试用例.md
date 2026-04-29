# Tableau节点视图ID属性_测试用例

> **文档版本**：v1.0
> **关联设计文档**：Tableau节点视图ID属性_设计-v2.5.md
> **创建日期**：2026-04-27

---

## 一、测试用例概述

### 1.1 测试目的

验证Tableau相关节点的ID属性编辑功能，包括：
- **tableau节点**：viewId（视图ID）的显示、编辑、校验功能
- **tableauDataRefre节点**：datasourceId（数据源ID）的显示、编辑、校验功能

### 1.2 测试范围

| 测试模块 | 测试内容 |
|---------|---------|
| 后端接口 | EditFlowRequest字段扩展、modifyJson逻辑、batchEditFlow校验逻辑 |
| 数据可视化页面 | 节点列表显示、编辑弹窗、批量编辑 |
| 工作流页面 | 节点参数展示、保存逻辑、失败处理 |

### 1.3 测试策略

| 测试类型 | 说明 |
|---------|------|
| 功能测试 | 验证ID属性的增删改查功能 |
| 校验测试 | 验证必填校验和真实性校验 |
| 边界测试 | 验证空值、特殊字符、长度限制等边界场景 |
| 异常测试 | 验证接口失败时的处理逻辑 |
| 集成测试 | 验证前后端数据流转 |

---

## 二、测试环境要求

### 2.1 软件环境

| 软件 | 版本要求 |
|-----|---------|
| DSS | 1.22.0-SNAPSHOT |
| Linkis | 1.18.3-wds |
| Java | 8 |
| MySQL | 5.7+ |
| 浏览器 | Chrome 90+ / Firefox 88+ |

### 2.2 数据准备

1. 已创建工作空间和项目
2. 已创建包含tableau和tableauDataRefre节点的工作流
3. 已配置Tableau AppConn连接
4. 数据库中已存在 `dss_workflow_node_content_to_ui` 表配置

### 2.3 测试账号

| 角色 | 用途 |
|-----|-----|
| 管理员账号 | 全功能测试 |
| 普通用户账号 | 权限测试 |

---

## 三、详细测试用例

### 3.1 后端接口测试

#### 3.1.1 EditFlowRequest字段扩展

| 用例编号 | TC-BACKEND-001 |
|---------|----------------|
| 用例名称 | EditFlowRequest支持viewId字段 |
| 前置条件 | 后端服务已启动 |
| 测试步骤 | 1. 构造包含viewId的EditFlowRequest对象<br>2. 调用getter方法获取viewId<br>3. 调用setter方法设置viewId |
| 测试数据 | viewId = "view-test-001" |
| 预期结果 | 1. getter能正确获取设置的viewId值<br>2. setter能正确设置viewId值 |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-002 |
|---------|----------------|
| 用例名称 | EditFlowRequest支持datasourceId字段 |
| 前置条件 | 后端服务已启动 |
| 测试步骤 | 1. 构造包含datasourceId的EditFlowRequest对象<br>2. 调用getter方法获取datasourceId<br>3. 调用setter方法设置datasourceId |
| 测试数据 | datasourceId = "ds-test-001" |
| 预期结果 | 1. getter能正确获取设置的datasourceId值<br>2. setter能正确设置datasourceId值 |
| 优先级 | P0 |

#### 3.1.2 modifyJson方法测试

| 用例编号 | TC-BACKEND-003 |
|---------|----------------|
| 用例名称 | modifyJson正确处理tableau节点的viewId |
| 前置条件 | 1. 后端服务已启动<br>2. 已存在tableau类型节点 |
| 测试步骤 | 1. 调用modifyJson接口，传入viewId参数<br>2. 查询节点的jobContent |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableau"<br>viewId = "view-abc-123" |
| 预期结果 | 1. jobContent中存在viewId属性<br>2. jobContent.viewId = "view-abc-123" |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-004 |
|---------|----------------|
| 用例名称 | modifyJson正确处理tableauDataRefre节点的datasourceId |
| 前置条件 | 1. 后端服务已启动<br>2. 已存在tableauDataRefre类型节点 |
| 测试步骤 | 1. 调用modifyJson接口，传入datasourceId参数<br>2. 查询节点的jobContent |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableauDataRefre"<br>datasourceId = "ds-xyz-456" |
| 预期结果 | 1. jobContent中存在datasourceId属性<br>2. jobContent.datasourceId = "ds-xyz-456" |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-005 |
|---------|----------------|
| 用例名称 | modifyJson无jobContent时自动创建 |
| 前置条件 | 1. 后端服务已启动<br>2. 节点的jobContent为空或不存在 |
| 测试步骤 | 1. 调用modifyJson接口，传入viewId参数<br>2. 查询节点的jobContent |
| 测试数据 | viewId = "view-new-001" |
| 预期结果 | 1. 自动创建jobContent对象<br>2. jobContent.viewId = "view-new-001" |
| 优先级 | P1 |

| 用例编号 | TC-BACKEND-006 |
|---------|----------------|
| 用例名称 | modifyJson更新已存在的viewId |
| 前置条件 | 1. 后端服务已启动<br>2. 节点已存在viewId = "old-view-id" |
| 测试步骤 | 1. 调用modifyJson接口，传入新的viewId<br>2. 查询节点的jobContent |
| 测试数据 | old viewId = "old-view-id"<br>new viewId = "new-view-id" |
| 预期结果 | jobContent.viewId = "new-view-id"（旧值被覆盖） |
| 优先级 | P1 |

| 用例编号 | TC-BACKEND-007 |
|---------|----------------|
| 用例名称 | modifyJson传入空viewId不更新 |
| 前置条件 | 1. 后端服务已启动<br>2. 节点已存在viewId |
| 测试步骤 | 1. 调用modifyJson接口，传入viewId = null或空字符串<br>2. 查询节点的jobContent |
| 测试数据 | viewId = null 或 "" |
| 预期结果 | 1. 不更新jobContent中的viewId<br>2. 或保持原有值不变 |
| 优先级 | P2 |

#### 3.1.3 batchEditFlow校验测试

| 用例编号 | TC-BACKEND-008 |
|---------|----------------|
| 用例名称 | tableau节点viewId必填校验 |
| 前置条件 | 1. 后端服务已启动<br>2. 已存在tableau类型节点 |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 传入viewId为空或null |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableau"<br>viewId = null 或 "" |
| 预期结果 | 1. 返回错误码80001<br>2. 错误信息："视图ID不能为空" |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-009 |
|---------|----------------|
| 用例名称 | tableauDataRefre节点datasourceId必填校验 |
| 前置条件 | 1. 后端服务已启动<br>2. 已存在tableauDataRefre类型节点 |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 传入datasourceId为空或null |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableauDataRefre"<br>datasourceId = null 或 "" |
| 预期结果 | 1. 返回错误码80001<br>2. 错误信息："数据源ID不能为空" |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-010 |
|---------|----------------|
| 用例名称 | tableau节点viewId真实性校验-无效ID |
| 前置条件 | 1. 后端服务已启动<br>2. Tableau服务可连接<br>3. 已存在tableau类型节点 |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 传入不存在的viewId |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableau"<br>viewId = "non-existent-view-id" |
| 预期结果 | 1. 返回错误<br>2. 错误信息包含"视图ID不存在"或类似提示 |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-011 |
|---------|----------------|
| 用例名称 | tableauDataRefre节点datasourceId真实性校验-无效ID |
| 前置条件 | 1. 后端服务已启动<br>2. Tableau服务可连接<br>3. 已存在tableauDataRefre类型节点 |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 传入不存在的datasourceId |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableauDataRefre"<br>datasourceId = "non-existent-ds-id" |
| 预期结果 | 1. 返回错误<br>2. 错误信息包含"数据源ID不存在"或类似提示 |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-012 |
|---------|----------------|
| 用例名称 | tableau节点viewId真实性校验-有效ID |
| 前置条件 | 1. 后端服务已启动<br>2. Tableau服务可连接<br>3. 已存在tableau类型节点<br>4. Tableau中存在有效的viewId |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 传入有效的viewId |
| 测试数据 | nodeType = "linkis.appconn.newVisualis.tableau"<br>viewId = "valid-view-id"（Tableau中存在的ID） |
| 预期结果 | 1. 校验通过<br>2. 成功更新节点数据 |
| 优先级 | P0 |

| 用例编号 | TC-BACKEND-013 |
|---------|----------------|
| 用例名称 | 非Tableau节点跳过校验 |
| 前置条件 | 1. 后端服务已启动<br>2. 已存在非Tableau类型节点（如spark节点） |
| 测试步骤 | 1. 调用batchEditFlow接口<br>2. 不传入viewId/datasourceId |
| 测试数据 | nodeType = "linkis.appconn.scriptis.spark" |
| 预期结果 | 1. 不执行校验<br>2. 正常处理其他字段 |
| 优先级 | P2 |

---

### 3.2 数据可视化页面测试

#### 3.2.1 节点列表显示测试

| 用例编号 | TC-VISUAL-001 |
|---------|---------------|
| 用例名称 | 节点列表正确显示tableau节点的viewId |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点且已配置viewId |
| 测试步骤 | 1. 进入数据可视化页面<br>2. 查看tableau类型节点列表 |
| 测试数据 | tableau节点，viewId = "view-test-001" |
| 预期结果 | 1. 节点列表显示viewId列<br>2. viewId值为"view-test-001" |
| 优先级 | P0 |

| 用例编号 | TC-VISUAL-002 |
|---------|---------------|
| 用例名称 | 节点列表正确显示tableauDataRefre节点的datasourceId |
| 前置条件 | 1. 已登录系统<br>2. 存在tableauDataRefre类型节点且已配置datasourceId |
| 测试步骤 | 1. 进入数据可视化页面<br>2. 查看tableauDataRefre类型节点列表 |
| 测试数据 | tableauDataRefre节点，datasourceId = "ds-test-001" |
| 预期结果 | 1. 节点列表显示datasourceId列<br>2. datasourceId值为"ds-test-001" |
| 优先级 | P0 |

| 用例编号 | TC-VISUAL-003 |
|---------|---------------|
| 用例名称 | 节点列表显示空viewId/datasourceId |
| 前置条件 | 1. 已登录系统<br>2. 存在未配置viewId/datasourceId的节点 |
| 测试步骤 | 1. 进入数据可视化页面<br>2. 查看节点列表 |
| 测试数据 | tableau节点，viewId = null |
| 预期结果 | 1. viewId/datasourceId列显示为空或占位符 |
| 优先级 | P2 |

#### 3.2.2 编辑弹窗测试

| 用例编号 | TC-VISUAL-004 |
|---------|---------------|
| 用例名称 | 编辑弹窗正确显示tableau节点viewId |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 点击tableau节点的编辑按钮<br>2. 查看编辑弹窗 |
| 测试数据 | tableau节点，viewId = "view-edit-001" |
| 预期结果 | 1. 编辑弹窗显示viewId输入框<br>2. 输入框显示当前viewId值"view-edit-001" |
| 优先级 | P0 |

| 用例编号 | TC-VISUAL-005 |
|---------|---------------|
| 用例名称 | 编辑弹窗正确显示tableauDataRefre节点datasourceId |
| 前置条件 | 1. 已登录系统<br>2. 存在tableauDataRefre类型节点 |
| 测试步骤 | 1. 点击tableauDataRefre节点的编辑按钮<br>2. 查看编辑弹窗 |
| 测试数据 | tableauDataRefre节点，datasourceId = "ds-edit-001" |
| 预期结果 | 1. 编辑弹窗显示datasourceId输入框<br>2. 输入框显示当前datasourceId值"ds-edit-001" |
| 优先级 | P0 |

| 用例编号 | TC-VISUAL-006 |
|---------|---------------|
| 用例名称 | 编辑tableau节点viewId并保存 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 点击tableau节点的编辑按钮<br>2. 修改viewId值<br>3. 点击保存 |
| 测试数据 | 原viewId = "old-view"<br>新viewId = "new-view" |
| 预期结果 | 1. 保存成功<br>2. 节点列表显示新的viewId<br>3. 后端jobContent.viewId已更新 |
| 优先级 | P0 |

| 用例编号 | TC-VISUAL-007 |
|---------|---------------|
| 用例名称 | 编辑tableauDataRefre节点datasourceId并保存 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableauDataRefre类型节点 |
| 测试步骤 | 1. 点击tableauDataRefre节点的编辑按钮<br>2. 修改datasourceId值<br>3. 点击保存 |
| 测试数据 | 原datasourceId = "old-ds"<br>新datasourceId = "new-ds" |
| 预期结果 | 1. 保存成功<br>2. 节点列表显示新的datasourceId<br>3. 后端jobContent.datasourceId已更新 |
| 优先级 | P0 |

#### 3.2.3 批量编辑测试

| 用例编号 | TC-VISUAL-008 |
|---------|---------------|
| 用例名称 | 批量编辑多个tableau节点viewId |
| 前置条件 | 1. 已登录系统<br>2. 存在多个tableau类型节点 |
| 测试步骤 | 1. 勾选多个tableau节点<br>2. 点击批量编辑<br>3. 修改viewId<br>4. 点击保存 |
| 测试数据 | 选择2个tableau节点 |
| 预期结果 | 1. 所有选中节点的viewId更新成功<br>2. 节点列表显示更新后的viewId |
| 优先级 | P1 |

| 用例编号 | TC-VISUAL-009 |
|---------|---------------|
| 用例名称 | 批量编辑混合类型节点 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau和tableauDataRefre类型节点 |
| 测试步骤 | 1. 勾选tableau和tableauDataRefre节点<br>2. 点击批量编辑<br>3. 查看编辑弹窗 |
| 测试数据 | 1个tableau节点 + 1个tableauDataRefre节点 |
| 预期结果 | 1. 根据节点类型分别显示viewId和datasourceId字段<br>2. 保存时分别处理对应字段 |
| 优先级 | P1 |

| 用例编号 | TC-VISUAL-010 |
|---------|---------------|
| 用例名称 | 批量编辑时必填校验 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 勾选tableau节点<br>2. 点击批量编辑<br>3. 清空viewId<br>4. 点击保存 |
| 测试数据 | viewId = "" |
| 预期结果 | 1. 显示错误提示"视图ID不能为空"<br>2. 不保存数据 |
| 优先级 | P0 |

---

### 3.3 工作流页面测试

#### 3.3.1 节点参数展示测试

| 用例编号 | TC-WORKFLOW-001 |
|---------|-----------------|
| 用例名称 | 工作流节点参数显示tableau的viewId |
| 前置条件 | 1. 已登录系统<br>2. 存在包含tableau节点的工作流<br>3. tableau节点已配置viewId |
| 测试步骤 | 1. 打开工作流<br>2. 点击tableau节点<br>3. 查看节点参数面板 |
| 测试数据 | tableau节点，viewId = "view-workflow-001" |
| 预期结果 | 1. 节点参数面板显示viewId字段<br>2. 显示值为"view-workflow-001" |
| 优先级 | P0 |

| 用例编号 | TC-WORKFLOW-002 |
|---------|-----------------|
| 用例名称 | 工作流节点参数显示tableauDataRefre的datasourceId |
| 前置条件 | 1. 已登录系统<br>2. 存在包含tableauDataRefre节点的工作流<br>3. 节点已配置datasourceId |
| 测试步骤 | 1. 打开工作流<br>2. 点击tableauDataRefre节点<br>3. 查看节点参数面板 |
| 测试数据 | tableauDataRefre节点，datasourceId = "ds-workflow-001" |
| 预期结果 | 1. 节点参数面板显示datasourceId字段<br>2. 显示值为"ds-workflow-001" |
| 优先级 | P0 |

#### 3.3.2 节点参数保存测试

| 用例编号 | TC-WORKFLOW-003 |
|---------|-----------------|
| 用例名称 | 修改tableau节点viewId并保存 |
| 前置条件 | 1. 已登录系统<br>2. 打开包含tableau节点的工作流 |
| 测试步骤 | 1. 点击tableau节点<br>2. 修改viewId值<br>3. 点击保存节点参数 |
| 测试数据 | 原viewId = "old-view"<br>新viewId = "new-view-workflow" |
| 预期结果 | 1. 保存成功<br>2. jobContent.viewId更新为"new-view-workflow"<br>3. 下次打开显示新值 |
| 优先级 | P0 |

| 用例编号 | TC-WORKFLOW-004 |
|---------|-----------------|
| 用例名称 | 修改tableauDataRefre节点datasourceId并保存 |
| 前置条件 | 1. 已登录系统<br>2. 打开包含tableauDataRefre节点的工作流 |
| 测试步骤 | 1. 点击tableauDataRefre节点<br>2. 修改datasourceId值<br>3. 点击保存节点参数 |
| 测试数据 | 原datasourceId = "old-ds"<br>新datasourceId = "new-ds-workflow" |
| 预期结果 | 1. 保存成功<br>2. jobContent.datasourceId更新为"new-ds-workflow"<br>3. 下次打开显示新值 |
| 优先级 | P0 |

#### 3.3.3 失败处理测试

| 用例编号 | TC-WORKFLOW-005 |
|---------|-----------------|
| 用例名称 | updateAppConnNode失败时不更新数据 |
| 前置条件 | 1. 已登录系统<br>2. 打开包含tableau节点的工作流<br>3. Tableau服务不可用或ID无效 |
| 测试步骤 | 1. 点击tableau节点<br>2. 修改viewId为无效值<br>3. 点击保存节点参数 |
| 测试数据 | viewId = "invalid-view-id" |
| 预期结果 | 1. 显示错误提示<br>2. this.json.nodes数据不变<br>3. 不调用saveFlow接口<br>4. 页面数据保持原值 |
| 优先级 | P0 |

| 用例编号 | TC-WORKFLOW-006 |
|---------|-----------------|
| 用例名称 | updateAppConnNode失败后loading状态重置 |
| 前置条件 | 1. 已登录系统<br>2. 打开包含tableau节点的工作流 |
| 测试步骤 | 1. 点击tableau节点<br>2. 修改viewId为无效值<br>3. 点击保存<br>4. 观察loading状态 |
| 测试数据 | viewId = "invalid-view-id" |
| 预期结果 | 1. 保存失败后loading状态变为false<br>2. 按钮恢复可点击状态 |
| 优先级 | P1 |

#### 3.3.4 数据回显测试

| 用例编号 | TC-WORKFLOW-007 |
|---------|-----------------|
| 用例名称 | jobContent中viewId正确回显到表单 |
| 前置条件 | 1. 已登录系统<br>2. 工作流节点的jobContent中存在viewId |
| 测试步骤 | 1. 打开工作流<br>2. 切换到tableau节点<br>3. 查看节点参数面板 |
| 测试数据 | jobContent.viewId = "view-from-db" |
| 预期结果 | 1. currentNode.viewId = "view-from-db"<br>2. 表单显示viewId值"view-from-db" |
| 优先级 | P0 |

| 用例编号 | TC-WORKFLOW-008 |
|---------|-----------------|
| 用例名称 | jobContent中datasourceId正确回显到表单 |
| 前置条件 | 1. 已登录系统<br>2. 工作流节点的jobContent中存在datasourceId |
| 测试步骤 | 1. 打开工作流<br>2. 切换到tableauDataRefre节点<br>3. 查看节点参数面板 |
| 测试数据 | jobContent.datasourceId = "ds-from-db" |
| 预期结果 | 1. currentNode.datasourceId = "ds-from-db"<br>2. 表单显示datasourceId值"ds-from-db" |
| 优先级 | P0 |

---

### 3.4 边界场景测试

| 用例编号 | TC-EDGE-001 |
|---------|-------------|
| 用例名称 | viewId超长字符串处理 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入超长viewId（如1000字符）<br>3. 保存 |
| 测试数据 | viewId = "a" * 1000 |
| 预期结果 | 1. 系统正常处理或提示长度限制<br>2. 不出现系统异常 |
| 优先级 | P2 |

| 用例编号 | TC-EDGE-002 |
|---------|-------------|
| 用例名称 | viewId包含特殊字符 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入包含特殊字符的viewId<br>3. 保存 |
| 测试数据 | viewId = "view-<script>alert(1)</script>" |
| 预期结果 | 1. 特殊字符被正确转义或提示非法字符<br>2. 不存在XSS漏洞 |
| 优先级 | P1 |

| 用例编号 | TC-EDGE-003 |
|---------|-------------|
| 用例名称 | viewId包含空格处理 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入包含前后空格的viewId<br>3. 保存 |
| 测试数据 | viewId = "  view-with-space  " |
| 预期结果 | 1. 系统自动trim空格或保留原值<br>2. 保存成功 |
| 优先级 | P2 |

| 用例编号 | TC-EDGE-004 |
|---------|-------------|
| 用例名称 | viewId包含中文字符 |
| 前置条件 | 1. 已登录系统<br>2. 存在tableau类型节点 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入包含中文的viewId<br>3. 保存 |
| 测试数据 | viewId = "视图-中文测试" |
| 预期结果 | 1. 中文正常显示和保存<br>2. 无乱码 |
| 优先级 | P2 |

| 用例编号 | TC-EDGE-005 |
|---------|-------------|
| 用例名称 | 节点无jobContent对象时创建 |
| 前置条件 | 1. 已登录系统<br>2. 节点的jobContent为null |
| 测试步骤 | 1. 编辑节点<br>2. 输入viewId<br>3. 保存 |
| 测试数据 | jobContent = null, viewId = "new-view" |
| 预期结果 | 1. 自动创建jobContent对象<br>2. jobContent.viewId = "new-view" |
| 优先级 | P1 |

---

### 3.5 异常场景测试

| 用例编号 | TC-EXCEPTION-001 |
|---------|------------------|
| 用例名称 | Tableau服务不可用时的校验处理 |
| 前置条件 | 1. 已登录系统<br>2. Tableau服务不可用 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入viewId<br>3. 保存 |
| 测试数据 | viewId = "any-view-id" |
| 预期结果 | 1. 显示友好的错误提示<br>2. 不出现系统异常堆栈 |
| 优先级 | P1 |

| 用例编号 | TC-EXCEPTION-002 |
|---------|------------------|
| 用例名称 | 网络超时处理 |
| 前置条件 | 1. 已登录系统<br>2. 模拟网络延迟或超时 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入viewId<br>3. 保存<br>4. 模拟网络超时 |
| 测试数据 | viewId = "timeout-test" |
| 预期结果 | 1. 显示网络超时提示<br>2. 系统不崩溃 |
| 优先级 | P2 |

| 用例编号 | TC-EXCEPTION-003 |
|---------|------------------|
| 用例名称 | 并发编辑同一节点 |
| 前置条件 | 1. 已登录系统<br>2. 两个用户同时编辑同一节点 |
| 测试步骤 | 1. 用户A打开节点编辑<br>2. 用户B同时打开节点编辑<br>3. A保存viewId = "view-a"<br>4. B保存viewId = "view-b" |
| 测试数据 | 两个不同viewId |
| 预期结果 | 1. 后提交者覆盖前者或提示冲突<br>2. 数据一致性有保障 |
| 优先级 | P2 |

| 用例编号 | TC-EXCEPTION-004 |
|---------|------------------|
| 用例名称 | 数据库连接异常 |
| 前置条件 | 1. 已登录系统<br>2. 模拟数据库连接异常 |
| 测试步骤 | 1. 编辑tableau节点<br>2. 输入viewId<br>3. 保存 |
| 测试数据 | viewId = "db-error-test" |
| 预期结果 | 1. 显示数据库错误提示<br>2. 不出现系统异常 |
| 优先级 | P2 |

---

## 四、测试数据准备

### 4.1 数据库数据

```sql
-- 确认dss_workflow_node_content_to_ui表配置存在
SELECT * FROM dss_workflow_node_content_to_ui
WHERE node_type IN ('linkis.appconn.newVisualis.tableau', 'linkis.appconn.newVisualis.tableauDataRefre');

-- 测试用tableau节点数据
INSERT INTO dss_workflow_node (id, flow_id, name, job_type, content)
VALUES (1001, 100, 'test-tableau-node', 'linkis.appconn.newVisualis.tableau',
        '{"jobContent":{"viewId":"test-view-001"},"key":"tableau-001"}');

-- 测试用tableauDataRefre节点数据
INSERT INTO dss_workflow_node (id, flow_id, name, job_type, content)
VALUES (1002, 100, 'test-tableauDataRefre-node', 'linkis.appconn.newVisualis.tableauDataRefre',
        '{"jobContent":{"datasourceId":"test-ds-001"},"key":"ds-refre-001"}');
```

### 4.2 Tableau测试数据

需要准备以下Tableau测试数据：
1. 有效的viewId列表：`valid-view-001`, `valid-view-002`
2. 有效的datasourceId列表：`valid-ds-001`, `valid-ds-002`
3. 确保Tableau服务可访问

### 4.3 测试工作流

创建测试工作流：
1. 工作流名称：`test-tableau-workflow`
2. 包含节点：
   - 1个tableau节点
   - 1个tableauDataRefre节点
   - 1个其他类型节点（用于对比测试）

---

## 五、预期结果汇总

### 5.1 功能测试预期结果

| 功能项 | 预期结果 |
|-------|---------|
| 节点列表显示 | 正确显示viewId/datasourceId字段 |
| 编辑弹窗 | 正确显示和编辑viewId/datasourceId |
| 批量编辑 | 根据节点类型正确处理字段 |
| 工作流参数展示 | 正确显示viewId/datasourceId |
| 工作流保存 | 数据正确保存到jobContent |
| 失败处理 | updateAppConnNode失败时数据不更新 |

### 5.2 校验测试预期结果

| 校验项 | 预期结果 |
|-------|---------|
| tableau节点viewId必填 | 空值时提示"视图ID不能为空" |
| tableauDataRefre节点datasourceId必填 | 空值时提示"数据源ID不能为空" |
| viewId真实性校验 | 无效ID时提示错误 |
| datasourceId真实性校验 | 无效ID时提示错误 |

### 5.3 数据存储预期结果

| 存储位置 | 预期结果 |
|---------|---------|
| jobContent.viewId | tableau节点viewId存储在jobContent层级 |
| jobContent.datasourceId | tableauDataRefre节点datasourceId存储在jobContent层级 |

---

## 六、测试执行记录

| 用例编号 | 执行人 | 执行日期 | 执行结果 | 备注 |
|---------|-------|---------|---------|------|
| | | | | |

---

**文档版本**：v1.0
**创建日期**：2026-04-27
**测试负责人**：待定
