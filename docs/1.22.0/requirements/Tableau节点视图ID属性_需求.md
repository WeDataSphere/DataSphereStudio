# Tableau节点视图ID属性_需求

## 一、功能概述

### 1.1 需求类型
功能增强（ENHANCE）

### 1.2 需求描述
为Tableau相关节点添加视图ID和数据源ID的查看和编辑功能：
- **tableau节点**：支持查看和编辑视图ID（viewId）
- **tableauDataRefre节点**：支持查看和编辑数据源ID（datasourceId）

支持用户在创建节点后查看和修改ID，更新后的ID在执行节点时生效。

### 1.3 基础模块
- 模块名称：Tableau相关节点
- 节点类型：
  - tableau（属于数据可视化节点-Tableau视图）
  - tableauDataRefre（属于数据可视化节点-Tableau数据源）

### 1.4 当前痛点
创建Tableau节点时会绑定相应的ID，但创建后无法在界面上查看节点实际绑定的ID，无法确认配置是否正确，也无法进行修改。

## 二、现有功能描述

### 2.1 Tableau相关节点现状
- **tableau节点**：DSS系统中用于集成Tableau数据可视化功能的工作流节点，工作流执行时通过链接的方式跳转到Tableau查看数据可视化结果
- **tableauDataRefre节点**：用于处理Tableau数据源刷新的节点

### 2.2 当前数据存储
- **tableau节点**：视图ID（viewId）存储在节点配置的 `nodeContent.viewId` 字段中
- **tableauDataRefre节点**：数据源ID（datasourceId）存储在节点配置的 `nodeContent.datasourceId` 字段中

### 2.3 现有界面
系统已有数据可视化节点管理页面（`next-web/packages/accounts/module/dataVisualization/`），支持：
- 查询数据可视化节点列表
- 显示节点基本信息（项目、工作流、节点名称、节点类型等）
- 跳转Tableau查看功能（点击表格中的ID可跳转）
- 编辑节点功能（调用 `editDataVisualApi`）

## 三、详细功能描述

### 3.1 功能范围

#### 3.1.1 核心功能（P0）
| 序号 | 功能名称 | 功能描述 | 节点类型 |
|-----|---------|---------|---------|
| 1 | 视图ID展示 | 在数据可视化节点管理页面表格中，为tableau节点显示绑定的视图ID | tableau |
| 2 | 视图ID编辑 | 在节点编辑弹窗中，支持编辑tableau节点的视图ID字段 | tableau |
| 3 | 视图ID保存 | 支持保存编辑后的视图ID到节点配置 | tableau |
| 4 | 执行使用更新ID | 工作流执行时使用最新保存的视图ID跳转Tableau | tableau |
| 5 | 数据源ID展示 | 在数据可视化节点管理页面表格中，为tableauDataRefre节点显示绑定的数据源ID | tableauDataRefre |
| 6 | 数据源ID编辑 | 在节点编辑弹窗中，支持编辑tableauDataRefre节点的数据源ID字段 | tableauDataRefre |
| 7 | 数据源ID保存 | 支持保存编辑后的数据源ID到节点配置 | tableauDataRefre |
| 8 | 执行使用更新ID | 工作流执行时使用最新保存的数据源ID | tableauDataRefre |

#### 3.1.2 扩展功能（P1）
| 序号 | 功能名称 | 功能描述 |
|-----|---------|---------|
| 1 | ID校验 | 对用户输入的ID进行必填校验 |
| 2 | 错误提示 | ID校验失败时给出明确的错误提示信息 |

### 3.2 功能详情

#### 3.2.1 ID展示规则
- 在数据可视化节点列表的"绑定Tableau视图ID或数据源ID"列显示对应ID
- **tableau节点**：显示 `nodeContent.viewId`
- **tableauDataRefre节点**：显示 `nodeContent.datasourceId`
- ID显示为蓝色可点击链接，点击后跳转到Tableau查看（现有功能保持不变）
- ID为必填项，未填写时不允许保存节点

#### 3.2.2 ID编辑规则
- 在数据可视化节点管理页面，点击"编辑"按钮打开编辑弹窗
- **tableau节点**：编辑弹窗中包含视图ID输入框（属于基础参数配置部分，必填项）
- **tableauDataRefre节点**：编辑弹窗中包含数据源ID输入框（属于基础参数配置部分，必填项）
- 用户需手动输入ID值，不能为空
- 仅更新当前节点的ID，不影响其他节点

#### 3.2.3 权限控制
- 查看和编辑权限由项目权限管控
- 具有项目编辑权限的用户可以编辑ID
- 权限与节点本身无关，遵循现有的项目权限体系

## 四、验收标准

### 4.1 用户查询功能验收

#### 4.1.1 输入验证
无特殊要求

#### 4.1.2 处理验证
- 系统能够正确查询并显示数据可视化节点列表
- tableau节点能正确显示 `nodeContent.viewId` 字段值
- tableauDataRefre节点能正确显示 `nodeContent.datasourceId` 字段值

#### 4.1.3 输出验证
- 表格中"绑定Tableau视图ID或数据源ID"列正确显示对应ID值
- 空值或null值显示为 `--`

### 4.2 ID编辑功能验收（tableau节点）

#### 4.2.1 输入验证
- **必填校验**：视图ID为必填项，不能为空

#### 4.2.2 处理验证
- 点击编辑按钮打开弹窗时，正确回显当前视图ID值
- 修改视图ID后点击确认，调用 `editDataVisualApi` 接口保存
- 接口参数中 `viewId` 字段正确传递新值
- 保存成功后弹出"编辑成功"提示

#### 4.2.3 输出验证
- **保存成功**：
  - 弹出"编辑成功"提示消息
  - 编辑弹窗自动关闭
  - 节点列表自动刷新，显示更新后的视图ID
- **保存失败**：
  - 弹出错误提示信息
  - 编辑弹窗保持打开状态
  - 用户输入内容保持不变

### 4.3 ID编辑功能验收（tableauDataRefre节点）

#### 4.3.1 输入验证
- **必填校验**：数据源ID为必填项，不能为空

#### 4.3.2 处理验证
- 点击编辑按钮打开弹窗时，正确回显当前数据源ID值
- 修改数据源ID后点击确认，调用 `editDataVisualApi` 接口保存
- 接口参数中 `datasourceId` 字段正确传递新值
- 保存成功后弹出"编辑成功"提示

#### 4.3.3 输出验证
- **保存成功**：
  - 弹出"编辑成功"提示消息
  - 编辑弹窗自动关闭
  - 节点列表自动刷新，显示更新后的数据源ID
- **保存失败**：
  - 弹出错误提示信息
  - 编辑弹窗保持打开状态
  - 用户输入内容保持不变

### 4.4 执行生效验收

#### 4.4.1 验证流程（tableau节点）
1. 编辑tableau节点的视图ID为新值并保存
2. 执行包含该节点的工作流
3. 在执行过程中点击跳转Tableau连接
4. 在Tableau服务中验证显示的是更新后的视图ID

#### 4.4.2 输出验证（tableau节点）
- Tableau跳转URL参数中包含正确的视图ID
- Tableau服务显示对应ID的视图内容
- 未跳转时URL参数与节点配置一致

#### 4.4.3 验证流程（tableauDataRefre节点）
1. 编辑tableauDataRefre节点的数据源ID为新值并保存
2. 执行包含该节点的工作流
3. 验证数据源刷新使用更新后的数据源ID

## 五、非功能需求

### 5.1 性能要求
- 节点列表查询响应时间 < 3秒
- ID编辑保存响应时间 < 1秒
- 编辑弹窗打开时间 < 1秒

### 5.2 兼容性要求
- 支持主流浏览器
- 与现有项目权限体系兼容
- 不影响其他类型节点的正常使用

### 5.3 安全要求
- 严格遵循项目权限控制
- 防止XSS攻击（对用户输入进行转义）
- 记录ID修改审计日志

## 六、数据模型变更

### 6.1 字段说明

#### tableau节点配置数据结构
```json
{
  "nodeContent": {
    "viewId": "string",        // Tableau视图ID，新增编辑功能
    "title": "string",         // 节点标题
    "desc": "string",          // 节点描述
    ...
  }
}
```

#### tableauDataRefre节点配置数据结构
```json
{
  "nodeContent": {
    "datasourceId": "string",  // Tableau数据源ID，新增编辑功能
    "title": "string",         // 节点标题
    "desc": "string",          // 节点描述
    ...
  }
}
```

### 6.2 接口变更

#### batchEditFlowNode 接口
- **接口路径**：`/dss/workflow/batchEditFlowNode`
- **请求方法**：POST
- **请求参数**：支持传递 `viewId`（tableau节点）和 `datasourceId`（tableauDataRefre节点）

## 七、用户体验要求

### 7.1 界面展示
- ID列宽度适配中英文环境（中文234px，英文300px）
- 长ID自动省略显示，鼠标悬停显示完整内容
- ID列与操作列固定，避免水平滚动时丢失

### 7.2 交互反馈
- 编辑操作有明确的点击态和禁用态
- 保存成功有明确的成功提示
- 保存失败有具体的错误原因描述
- 加载状态显示loading动画

### 7.3 错误提示
表单校验失败时提示信息：
| 错误类型 | 节点类型 | 提示信息 |
|---------|---------|---------|
| 必填校验 | tableau节点 | 视图ID不能为空 |
| 必填校验 | tableauDataRefre节点 | 数据源ID不能为空 |

## 八、附录

### 8.1 参考资料
- 前端代码位置：`next-web/packages/accounts/module/dataVisualization/`
- 编辑弹窗组件：`next-web/packages/accounts/module/dataVisualization/components/editDataVisualDrawer.vue`
- 国际化配置：`next-web/packages/accounts/i18n/zh.js`、`en.js`

### 8.2 相关接口
| 接口名称 | 接口路径 | 说明 |
|---------|---------|------|
| 查询数据可视化节点 | `/dss/workflow/queryDataViewNode` | POST，查询节点列表 |
| 批量编辑节点 | `/dss/workflow/batchEditFlowNode` | POST，编辑节点信息 |
| 跳转Tableau | `/dss/workflow/getAppConnNodeUrl` | POST，获取跳转URL |

---

**文档版本**：v2.0
**创建日期**：2026-04-22
**更新日期**：2026-04-22
**需求负责人**：待定