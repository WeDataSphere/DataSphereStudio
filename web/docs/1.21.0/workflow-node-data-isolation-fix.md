# 工作流节点脚本数据隔离问题修复

## 问题描述

项目A复制为项目B后，修改项目B的工作流节点脚本并运行，如果项目B的脚本处于运行态，进入项目A的同一节点脚本时，发现项目A的数据已经被项目B覆盖了。存在数据污染问题。如果项目B中工作流节点脚本不处于运行态，就没有这个问题。

## 问题根源

### 1. IndexedDB 缓存键冲突

项目复制时，节点的 `key` 可能不变，导致项目A和项目B中相同位置的节点使用相同的缓存键：

```
项目A节点缓存键: node.key = "node_123"
项目B节点缓存键: node.key = "node_123"  (复制后key不变)
```

由于缓存键相同，项目B的运行态数据会覆盖项目A的缓存数据。

### 2. 两个独立的缓存系统

系统中存在两个独立的 IndexedDB 缓存系统：

| 缓存系统 | 所属包 | 使用场景 | 存储内容 |
|---------|-------|---------|---------|
| `workflowIndexedDB` | workflows | 工作流画布上的运行时控制台 | 节点运行状态、日志、进度 |
| `IndexedDB` | scriptis | 双击节点打开的编辑器视图 | 脚本内容、执行结果、历史记录 |

### 3. 关联操作查找失败

修改缓存键格式后，通过 `node.key` 查找 `work.id` 的逻辑会匹配失败，导致导入脚本后已打开的编辑器内容不会刷新。

## 修复方案

### 修改文件清单

| 文件路径 | 修改内容 |
|---------|---------|
| `workflows/module/process/component/console.vue` | 添加 `flowId` 属性，使用 `flowId_nodeKey` 作为缓存键 |
| `workflows/module/process/module.vue` | 向 `console` 组件传递 `flowId` 属性；在 `associateScript` 和 `saveNodeBaseInfo` 中给 node 添加 `contextID` |
| `scriptis/module/workbench/container.vue` | 使用 `contextID_nodeKey` 作为缓存键；修复 `Workbench:updateFlowsTab`、`Workbench:updateFlowsNodeName`、`Workbench:pasteInEditor` 中的 ID 匹配逻辑 |
| `scriptis/module/workbench/script/history.vue` | 修复 `currentNodeKey` 生成逻辑 |

### 修复详情

#### 1. console.vue（工作流运行时控制台）

**文件路径**: `workflows/module/process/component/console.vue`

**修改内容**:

```javascript
// 新增 props
props: {
  // ... 其他 props
  flowId: {
    type: [String, Number],
    default: ''
  }
},

// 新增计算属性
computed: {
  cacheNodeId() {
    const nodeKey = (this.node && this.node.key) || '';
    return this.flowId ? `${this.flowId}_${nodeKey}` : nodeKey;
  },
  // ...
},

// 修改缓存相关方法，使用 cacheNodeId 替代 node.key
createScript() {
  this.script = new Script({
    nodeId: this.cacheNodeId,
    // ...
  });
  this.dispatch('workflowIndexedDB:addNodeCache', {
    nodeId: this.cacheNodeId,
    value: this.script,
  });
},
```

#### 2. module.vue（工作流模块）

**文件路径**: `workflows/module/process/module.vue`

**修改内容**:

```vue
<!-- 向 console 组件传递 flowId -->
<console
  :flowId="flowId"
  ...
/>
```

```javascript
// 在 associateScript 方法中给 node 添加 contextID
associateScript(node, path, cb) {
  node.contextID = this.contextID;
  // ...
}

// 在 saveNodeBaseInfo 方法中给 arg 添加 contextID
async saveNodeBaseInfo(arg) {
  // ...
  arg.contextID = this.contextID;
  this.$emit('saveBaseInfo', arg);
  this.dispatch('Workbench:updateFlowsNodeName', arg);
  // ...
}
```

#### 3. container.vue（编辑器视图容器）

**文件路径**: `scriptis/module/workbench/container.vue`

**修改内容**:

```javascript
// 生成唯一的缓存键（所有使用 currentNodeKey 的地方）
const nodeKey = this.node ? this.node.key : ''
const contextID = this.node ? (this.node.contextID || '') : ''
const currentNodeKey = contextID
  ? `${encodeURIComponent(contextID)}_${nodeKey}`
  : nodeKey
```

```javascript
// 修复 Workbench:updateFlowsTab 中的 ID 匹配
"Workbench:updateFlowsTab"(node, data) {
  const contextID = node.contextID || ''
  const nodeId = contextID
    ? `${encodeURIComponent(contextID)}_${node.key}`
    : node.key
  const work = find(this.worklist, (work) => {
    return work.id === nodeId  // 使用新格式匹配
  })
  // ...
}

// 修复 Workbench:updateFlowsNodeName 中的 ID 匹配
"Workbench:updateFlowsNodeName"(node) {
  const contextID = node.contextID || ''
  const nodeId = contextID
    ? `${encodeURIComponent(contextID)}_${node.key}`
    : node.key
  this.worklist = this.worklist.map((work) => {
    if (work.id === nodeId) {  // 使用新格式匹配
      work.nodeName = node.title
    }
    return work
  })
}

// 修复 Workbench:pasteInEditor 中的 ID 匹配
"Workbench:pasteInEditor"(value, node = {}) {
  // ...
  const contextID = node.contextID || ''
  const nodeId = contextID
    ? `${encodeURIComponent(contextID)}_${node.key}`
    : node.key
  const work = find(this.worklist, (work) => work.id === nodeId)
  if (work && nodeId === this.current) {
    this.dispatch("Workbench:insertValue", {
      id: nodeId,
      value,
    })
  }
}
```

**重要说明**:
- 使用 `encodeURIComponent` 对 `contextID` 进行编码，避免 `contextID` 中包含特殊字符（如冒号 `:`）导致 URI 解析失败
- 先提取 `nodeKey` 再拼接，避免 `contextID` 有值但 `this.node` 为 `undefined` 时的空指针异常

#### 4. history.vue（历史记录）

**文件路径**: `scriptis/module/workbench/script/history.vue`

**修改内容**:

```javascript
// 修复 currentNodeKey 生成逻辑
currentNodeKey: (() => {
  const nodeKey = this.node ? this.node.key : ''
  const contextID = this.node ? (this.node.contextID || '') : ''
  return contextID ? `${encodeURIComponent(contextID)}_${nodeKey}` : nodeKey
})(),
```

## 修复效果

修复后，不同工作流的节点缓存将使用不同的键：

```
项目A节点缓存键: "flowId_A_node_123" 或 "contextID_A_node_123"
项目B节点缓存键: "flowId_B_node_123" 或 "contextID_B_node_123"
```

即使项目复制后节点 `key` 相同，缓存数据也不会相互覆盖。

## 关联操作兼容性

修改缓存键格式后，需要同步修复以下通过 `node.key` 查找 `work.id` 的场景：

| 方法 | 触发场景 | 修复方式 |
|-----|---------|---------|
| `Workbench:updateFlowsTab` | 右键导入脚本后刷新已打开的编辑器 | 使用 `node.contextID` 生成新格式 ID 匹配 |
| `Workbench:updateFlowsNodeName` | 保存节点基础信息后更新编辑器标签名 | 使用 `node.contextID` 生成新格式 ID 匹配 |
| `Workbench:pasteInEditor` | 在编辑器中粘贴内容 | 使用 `node.contextID` 生成新格式 ID 匹配 |
| `history.vue` 的 `currentNodeKey` | 从历史记录打开脚本 | 使用 `node.contextID` 生成新格式 ID |

**关键点**: `module.vue` 中 `associateScript` 和 `saveNodeBaseInfo` 方法在调用 `dispatch` 传递 `node` 对象前，需要先给 `node` 添加 `contextID` 属性，否则 `container.vue` 中无法获取到 `contextID` 来生成正确的匹配 ID。

## 两个修改的必要性说明

### 为什么两个修改都是必要的？

这两个修改针对不同的使用场景，使用不同的 IndexedDB 存储系统：

| 场景 | 入口 | 使用组件 | 缓存系统 | 修复方案 |
|-----|------|---------|---------|---------|
| 工作流画布上右键节点 | 右键菜单 → 控制台 | `console.vue` | `workflowIndexedDB` | 使用 `flowId` 区分 |
| 双击节点打开编辑器 | 双击节点 | `container.vue` | `IndexedDB` | 使用 `contextID` 区分 |

### 只修改一个是否可行？

不可行。只修改其中一个只能解决部分场景的数据污染问题：

- 只修改 `container.vue`：双击节点编辑器视图的数据隔离正常，但右键控制台的运行态数据仍会污染
- 只修改 `console.vue`：右键控制台的运行态数据隔离正常，但双击编辑器的数据仍会污染

## 测试验证

### 测试步骤

1. 创建工作流项目A，添加一个脚本节点
2. 复制项目A为项目B
3. 在项目B中双击打开同一节点，修改脚本内容并运行
4. 在项目B脚本运行期间，切换到项目A打开同一节点
5. 验证项目A的节点数据是否正常，未被项目B覆盖
6. 在项目A中右键节点导入脚本，验证已打开的编辑器内容是否刷新

### 预期结果

- 项目A和项目B的节点数据相互独立
- 项目B的运行态数据不会影响项目A
- 刷新页面后数据隔离仍然有效
- 右键导入脚本后已打开的编辑器内容正常刷新

## 相关文件

- `workflows/module/process/component/console.vue`
- `workflows/module/process/module.vue`
- `workflows/module/indexedDB/index.js`
- `workflows/service/db/node.js`
- `scriptis/module/workbench/container.vue`
- `scriptis/module/workbench/script/script.vue`
- `scriptis/module/workbench/script/history.vue`
- `scriptis/module/indexedDB/index.js`
- `shared/common/config/db.js`
