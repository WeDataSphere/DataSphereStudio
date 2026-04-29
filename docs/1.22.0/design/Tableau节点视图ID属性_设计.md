# Tableau节点视图ID属性_设计

> **文档版本**：v2.5

## 一、设计概述

### 1.1 设计目标
为Tableau相关节点添加ID属性的编辑功能：
- **tableau节点**：支持编辑视图ID（viewId）
- **tableauDataRefre节点**：支持编辑数据源ID（datasourceId）

支持用户在以下页面查看和修改ID：
1. 数据可视化节点管理页面
2. 工作流编辑页面（节点参数配置）

更新后在节点执行时生效。

### 1.2 设计范围
- 后端：修改EditFlowRequest添加字段，修改modifyJson处理逻辑，在jobContent中更新ID
- 前端数据可视化页面：添加viewId和datasourceId字段过滤，更新保存逻辑（需判断节点类型）
- 前端工作流页面：添加viewId和datasourceId字段展示和编辑，支持jobContent层级的读写

### 1.3 设计说明
- 数据库属性配置已存在，无需新增SQL
- 前端编辑时需根据 `nodeTypeName` 区分处理不同节点类型
- 后端只在 `jobContent` 层级存储 viewId 和 datasourceId
- updateAppConnNode 接口失败时，不更新节点数据，不调用 saveFlow 接口

## 二、数据库设计

### 2.1 现有配置说明

| 属性 key | 节点类型 | 状态 | 存储位置 |
|---------|---------|------|---------|
| `viewId` | tableau | 已存在 | `nodeContent.viewId` |
| `datasourceId` | tableauDataRefre | 已存在 | `nodeContent.datasourceId` |

**注意**：两个属性均已存在于数据库中，作为基础字段（`is_base_info=1`）。

### 2.2 数据存储方式

#### 2.2.1 数据库表结构
```sql
-- dss_workflow_node_content_to_ui 表
content_id | node_ui_key    | node_ui_value          | node_type
----------|---------------|------------------------|----------------------------
123        | viewId        | view-xxx               | linkis.appconn.newVisualis.tableau
456        | datasourceId   | datasource-xxx          | linkis.appconn.newVisualis.tableauDataRefre
```

#### 2.2.2 节点JSON结构

**tableau节点样例**：
```json
{
  "jobContent": {
    "viewId": "view-xxx",
    "refProjectId": null
  },
  "key": "xxx",
  "nodeTypeName": "linkis.appconn.newVisualis.tableau",
  "params": {...}
}
```

**tableauDataRefre节点样例**：
```json
{
  "jobContent": {
    "datasourceId": "datasource-xxx",
    "refProjectId": null
  },
  "key": "yyy",
  "nodeTypeName": "linkis.appconn.newVisualis.tableauDataRefre",
  "params": {...}
}
```

## 三、后端设计

### 3.1 后端代码变更

#### 3.1.1 EditFlowRequest.java - 添加viewId和datasourceId字段

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/entity/request/EditFlowRequest.java`

添加 field、getter 和 setter：
- `viewId` (String) - tableau节点的视图ID
- `datasourceId` (String) - tableauDataRefre节点的数据源ID

#### 3.1.2 DSSFlowServiceImpl.modifyJson - 处理viewId和datasourceId（含jobContent）

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java`

**修改内容**：
1. 接收 `viewId` 和 `datasourceId` 参数
2. 当 `viewId` 不为空时，在 `jobContent` 对象中设置 `viewId` 属性（不存在则创建）
3. 当 `datasourceId` 不为空时，在 `jobContent` 对象中设置 `datasourceId` 属性（不存在则创建）

##### 关键逻辑说明

1. **jobContent 层级存储**：
   - 检查是否存在 `jobContent` 对象，不存在则创建
   - 在 `jobContent` 对象中设置 `viewId` 或 `datasourceId`
   - 属性只在 `jobContent` 层级存储

2. **节点类型处理**：
   - 后端无需明确判断节点类型（viewId和datasourceId分别传递）
   - 前端已根据 `nodeTypeName` 传递对应字段

## 四、前端设计 - 数据可视化页面

### 4.1 节点列表页面

#### 4.1.1 文件位置
- 主页面：`next-web/packages/accounts/module/dataVisualization/index.vue`
- 后端接口调用：`next-web/packages/accounts/module/dataVisualization/api.ts`

#### 4.1.2 显示逻辑（已实现）
当前代码已正确实现ID显示：
- `tableau` 类型：显示 `nodeContent.viewId`
- `tableauDataRefre` 类型：显示 `nodeContent.datasourceId`

### 4.2 编辑弹窗组件

#### 4.2.1 文件位置
主组件：`next-web/packages/accounts/module/dataVisualization/components/editDataVisualDrawer.vue`

#### 4.2.2 变更说明

**变更1：getCurNodeBaseParamsList - 添加字段过滤**
在 fields 数组中添加 `'viewId'` 和 `'datasourceId'`

**变更2：handleOk - 单一节点编辑，根据节点类型传递对应字段**
- `tableau` 类型：添加 `nodeParams.viewId`
- `tableauDataRefre` 类型：添加 `nodeParams.datasourceId`

**变更3：handleOk - 批量节点编辑，根据节点类型处理对应字段**
- 使用 `nodeIdField` 动态判断字段名
- 编辑时使用 `currentNode.value[key]`
- 非编辑时保留 `item.nodeContent[key]`

## 五、前端设计 - 工作流页面

### 5.1 文件位置
- 主文件：`web/packages/workflows/module/process/module.vue`
- 节点参数组件：`web/packages/workflows/module/process/component/nodeparameter.vue`

### 5.2 节点参数展示

#### 5.2.1 curNodeBaseParamsList - 添加字段过滤

**文件**：`nodeparameter.vue`

在 fields 数组中添加 `'viewId'` 和 `'datasourceId'`，使这两个字段在基础信息表单中显示。

#### 5.2.2 watch: nodeData - 回显逻辑

**文件**：`nodeparameter.vue`

在节点数据更新时，从 `jobContent` 读取 `viewId`/`datasourceId` 到根层级 `currentNode`：
- 如果 `currentNode.jobContent.viewId` 存在，设置到 `currentNode.viewId`
- 如果 `currentNode.jobContent.datasourceId` 存在，设置到 `currentNode.datasourceId`

#### 5.2.3 getCurrentNode() - 保存逻辑

**文件**：`nodeparameter.vue`

在获取当前节点数据时，将根层级的 `viewId`/`datasourceId` 同步到 `jobContent`：
- 检查是否存在 `jobContent`，不存在则创建
- 将 `param.viewId` 同步到 `param.jobContent.viewId`
- 将 `param.datasourceId` 同步到 `param.jobContent.datasourceId`

### 5.3 updateAppConnNode 失败处理

#### 5.3.1 saveCommonIframe - 抛出异常

**文件**：`module.vue`

**修改内容**：
- `updateAppConnNode` 的 `.catch()` 中：
  1. 设置 `this.loading = false`
  2. 使用 `Promise.reject(err)` 重新抛出异常

**效果**：
当 `updateAppConnNode` 请求失败时，异常会向上传播。

#### 5.3.2 saveNodeBaseInfo - 捕获异常

**文件**：`module.vue`

**修改内容**：
- 调用 `saveCommonIframe(node)` 时使用 `try-catch` 包裹
- 捕获异常后：
  1. 设置 `this.loading = false`
  2. 显示错误提示
  3. 直接 `return`，不执行后续的节点数据更新和 `autoSave`

**效果**：
当 `updateAppConnNode` 失败时：
1. 不更新内存中的节点数据（`this.json.nodes` 不变）
2. 不调用 `autoSave`（不调用 `saveFlow` 接口）
3. 用户可以看到错误提示

### 5.4 数据流转说明

```
工作流编辑页面数据流转：

回显流程：
后端返回 → jobContent[key] → watch nodeData → currentNode[key] → 表单显示

保存流程：
表单编辑 → currentNode[key] → getCurrentNode() → jobContent[key] → updateAppConnNode
                                        ↓
                              更新 this.json.nodes
                                        ↓
                                autoSave('paramsSave')
                                        ↓
                                  saveFlow 接口

失败处理：
updateAppConnNode 失败 → API reject → Promise.reject(err) → catch 捕获
                                        ↓
                            设置 loading = false，显示错误
                                        ↓
                        return 跳过（不更新数据，不调用 saveFlow）
```

## 六、校验逻辑设计

### 6.1 校验规则

| 节点类型 | 字段 | 校验类型 | 错误信息 |
|---------|------|---------|---------|
| tableau | viewId | 必填 + 真实性校验 | 视图ID不能为空 / 视图ID不存在 |
| tableauDataRefre | datasourceId | 必填 + 真实性校验 | 数据源ID不能为空 / 数据源ID不存在 |

### 6.2 后端校验实现

#### 6.2.1 工作流编辑页面（modifyJson）
使用必填校验规则（Required），在节点保存时进行校验，确保字段不为空。

#### 6.2.2 数据可视化批量编辑页面（batchEditFlow）

在 `batchEditFlow` 方法中，对每个节点调用 `validateTableauNode` 方法进行校验。

**校验流程**：
1. 判断是否为 tableau/tableauDataRefre 节点，不是则直接返回
2. **必填校验**：tableau 节点 viewId 不能为空，tableauDataRefre 节点 datasourceId 不能为空
3. **真实性校验**：构建 CommonAppConnNode 对象，调用 workflowNodeService.updateNode() 进行校验
4. 如果校验失败，抛出 ExternalOperationFailedException 异常

**调用位置**（batchEditFlow 方法中）：
```java
for (EditFlowRequest editFlowRequest : editFlowRequests) {
    NodeContentDO nodeContentByContentId = nodeContentMapper.getNodeContentByContentId(editFlowRequest.getId());
    Long targetFlowId = nodeContentByContentId.getFlowId();

    // 校验 tableau/tableauDataRefre 节点的 viewId/datasourceId
    validateTableauNode(editFlowRequest, nodeContentByContentId, targetFlowId, workspace, userName);

    // ... 其他处理
}
```

**关键代码**：
```java
/**
 * 判断是否为 tableau 或 tableauDataRefre 节点
 */
private boolean isTableauNode(String nodeType) {
    return "linkis.appconn.newVisualis.tableau".equals(nodeType)
            || "linkis.appconn.newVisualis.tableauDataRefre".equals(nodeType);
}

/**
 * 校验 tableau/tableauDataRefre 节点的 viewId/datasourceId
 * 1. 必填校验：tableau节点viewId不能为空，tableauDataRefre节点datasourceId不能为空
 * 2. 真实性校验：通过调用 workflowNodeService.updateNode 触发 AppConn 校验
 */
private void validateTableauNode(EditFlowRequest editFlowRequest, NodeContentDO nodeContentDO,
                                  Long flowId, Workspace workspace, String userName)
        throws ExternalOperationFailedException {
    String nodeType = nodeContentDO.getJobType();

    // 只处理 tableau 相关节点
    if (!isTableauNode(nodeType)) {
        return;
    }

    String viewId = editFlowRequest.getViewId();
    String datasourceId = editFlowRequest.getDatasourceId();

    // 必填校验
    if ("linkis.appconn.newVisualis.tableau".equals(nodeType) && StringUtils.isEmpty(viewId)) {
        throw new ExternalOperationFailedException(80001, "视图ID不能为空");
    }
    if ("linkis.appconn.newVisualis.tableauDataRefre".equals(nodeType) && StringUtils.isEmpty(datasourceId)) {
        throw new ExternalOperationFailedException(80001, "数据源ID不能为空");
    }

    // 真实性校验：构建 jobContent 并调用 updateNode
    Map<String, Object> jobContent = new HashMap<>();
    if (StringUtils.isNotEmpty(viewId)) {
        jobContent.put("viewId", viewId);
    }
    if (StringUtils.isNotEmpty(datasourceId)) {
        jobContent.put("datasourceId", datasourceId);
    }
    if (StringUtils.isNotEmpty(editFlowRequest.getTitle())) {
        jobContent.put("title", editFlowRequest.getTitle());
    }
    if (StringUtils.isNotEmpty(editFlowRequest.getDesc())) {
        jobContent.put("desc", editFlowRequest.getDesc());
    }

    // 获取 projectId
    DSSFlow flow = getFlow(flowId);
    Long projectId = flow.getProjectId();

    // 构建 CommonAppConnNode
    CommonAppConnNode node = new CommonAppConnNode();
    node.setNodeType(nodeType);
    node.setFlowId(flowId);
    node.setProjectId(projectId);
    node.setJobContent(jobContent);
    node.setWorkspace(workspace);
    node.setParams(jobContent);
    node.setDssLabels(Collections.singletonList(new EnvDSSLabel(DSSCommonUtils.ENV_LABEL_VALUE_DEV)));

    // 调用 updateNode 进行校验，如果校验失败会抛出异常
    workflowNodeService.updateNode(userName, node);
}
```

## 七、数据存储结构总结

```
节点JSON结构：

tableau节点:
{
  "jobContent": {
    "viewId": "view-xxx",            ← jobContent层（唯一存储位置）
    "refProjectId": null
  },
  "key": "xxx",
  "params": {...}
}

tableauDataRefre节点:
{
  "jobContent": {
    "datasourceId": "datasource-xxx",   ← jobContent层（唯一存储位置）
    "refProjectId": null
  },
  "key": "yyy",
  "params": {...}
}
```

## 八、修改文件清单

### 8.1 后端文件
| 文件 | 修改内容 |
|------|----------|
| `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/entity/request/EditFlowRequest.java` | 添加 viewId 和 datasourceId 字段及 getter/setter |
| `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java` | modifyJson 方法添加 viewId/datasourceId 处理逻辑；batchEditFlow 方法添加校验逻辑 |

### 8.2 前端文件（数据可视化页面）
| 文件 | 修改内容 |
|------|----------|
| `next-web/packages/accounts/module/dataVisualization/components/editDataVisualDrawer.vue` | getCurNodeBaseParamsList 添加字段过滤，handleOk 根据节点类型处理字段 |

### 8.3 前端文件（工作流页面）
| 文件 | 修改内容 |
|------|----------|
| `web/packages/workflows/module/process/component/nodeparameter.vue` | curNodeBaseParamsList 添加字段过滤，watch 添加回显逻辑，getCurrentNode 添加保存逻辑 |
| `web/packages/workflows/module/process/module.vue` | saveCommonIframe 更新失败的 reject 处理，saveNodeBaseInfo 添加 try-catch |

---

**文档版本**：v2.5
**创建日期**：2026-04-23
**更新日期**：2026-04-27
**设计负责人**：待定
