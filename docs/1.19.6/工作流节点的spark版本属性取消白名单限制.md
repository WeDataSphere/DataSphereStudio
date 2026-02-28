# Spark版本代码回退记录

## 回退时间
2026年2月11日

## 回退原因
工作流节点的spark版本属性取消白名单限制

## 涉及文件

### 1. NodeRestfulApi.java
**文件路径**: `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/restful/NodeRestfulApi.java`

**回退内容**:
删除了在 `transfer` 方法中过滤 `sparkVersion` 的代码（第199-202行）

**回退前代码**:
```java
//避免重复的ui key，因为第三方组件可能会重复配置。
if (keySet.contains(nodeUi.getKey())) {
    continue;
}

// 不在白名单, 则取消sparkVersion选项
if(!isWhite && "sparkVersion".equalsIgnoreCase(nodeUi.getKey())){
    continue;
}

NodeUiVO nodeUiVO = new NodeUiVO();
```

**回退后代码**:
```java
//避免重复的ui key，因为第三方组件可能会重复配置。
if (keySet.contains(nodeUi.getKey())) {
    continue;
}

NodeUiVO nodeUiVO = new NodeUiVO();
```

### 2. DSSFlowServiceImpl.java
**文件路径**: `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java`

**回退内容**:

#### 2.1 删除 `handleWhiteNodeParams` 方法
该方法用于在批量编辑时处理白名单节点的 spark 版本参数。

#### 2.2 删除方法调用
在第 2324 行删除了对 `handleWhiteNodeParams` 方法的调用。

**回退前代码**:
```java
// 批量编辑 白名单项目中的节点和非白名单中的节点(非白名单的节点没有spark版本属性), 会修改白名单节点的spark版本
handleWhiteNodeParams(editFlowRequest);

editFlowRequestsList.add(editFlowRequest);
```

**回退后代码**:
```java
editFlowRequestsList.add(editFlowRequest);
```

## 回退操作汇总

| 操作项 | 状态 |
|--------|------|
| 删除 NodeRestfulApi.java 中的 sparkVersion 过滤 | ✅ 完成 |
| 删除 DSSFlowServiceImpl.java 中的 handleWhiteNodeParams 方法 | ✅ 完成 |
| 删除 DSSFlowServiceImpl.java 第2324行的方法调用 | ✅ 完成 |
| 代码编译检查 | ✅ 无错误 |

## 验证结果

1. 全局搜索 `sparkVersion` 和 `handleWhiteNodeParams`，无任何匹配结果
2. 无 linter 错误
3. 所有与 spark 版本相关的代码已成功回退

## 影响范围

- 所有相关节点都会展示spark版本配置,之前的需求确定 aisql节点强制使用spark3版本, 没有spark版本配置。
- 白名单节点 spark 版本自动添加功能已移除
