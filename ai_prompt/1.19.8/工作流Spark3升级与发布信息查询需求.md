# 工作流Spark版本与发布信息查询需求

## 需求概述

本需求涉及工作流节点Spark版本的白名单控制调整，以及新增工作流发布信息查询接口。

## 需求背景

### 变更历史回顾

在 **1.19.6版本** 中，对工作流节点的Spark版本属性进行了**取消白名单限制**的修改：

- `NodeRestfulApi.java` 中的 `transfer` 方法删除了针对非白名单项目过滤 `sparkVersion` 的代码
- `DSSFlowServiceImpl.java` 中删除了 `handleWhiteNodeParams` 方法及其调用

**1.19.6版本的修改导致：**
- 所有节点（包括非白名单项目）都展示了Spark版本配置
- 白名单节点Spark版本自动添加功能被移除

### 本次需求（1.19.8版本）

本次需求需要对上述1.19.6版本的变更进行调整优化：

1. **恢复白名单限制**：重新添加工作流节点Spark版本的白名单限制
2. **AISQL节点特殊处理**：AISQL节点取消白名单限制（始终不显示Spark版本配置）
3. **新增发布信息查询接口**：提供工作流发布信息查询接口

---

## 需求详细说明

### 需求点1：Spark版本重新添加白名单限制

#### 需求描述
恢复工作流节点Spark版本的白名单机制，只有白名单项目中的节点才显示Spark版本配置选项。

#### 影响范围

**涉及模块：** `dss-workflow-server`

**主要涉及文件：**
1. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/restful/NodeRestfulApi.java`
   - 在 `transfer` 方法中恢复 `isWhite` 判断逻辑，过滤非白名单项目的 `sparkVersion` 配置

2. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java`
   - 恢复 `handleWhiteNodeParams` 方法，处理批量编辑时白名单节点的Spark版本参数

3. 白名单相关服务：
   - `ProjectOrchestratorWhiteService`：提供白名单校验和查询服务
   - `ProjectOrchestratorWhite`：白名单实体类

#### 白名单说明

白名单表结构（`project_orchestrator_white`）：
```java
public class ProjectOrchestratorWhite {
    private Long id;
    private Long projectId;           // 项目ID
    private String projectName;       // 项目名称
    private Long orchestratorId;      // 编排ID，0表示整个项目
    private String orchestratorName;  // 编排名称
    private String createTime;
    private String updateTime;
    private String createBy;
    private String updateBy;
}
```

**白名单校验方法：**
```java
boolean checkProjectAndOrchestratorIsWhite(Long projectId, Long orchestratorId);
```

#### NodeRestfulApi.java 修改点（参考）

在 `transfer(NodeInfo nodeInfo, HttpServletRequest req, boolean isWhite)` 方法中：
- 第199行左右，添加白名单判断：
```java
// 不在白名单, 则取消sparkVersion选项
if(!isWhite && "sparkVersion".equalsIgnoreCase(nodeUi.getKey())){
    continue;
}
```

#### DSSFlowServiceImpl.java 修改点（参考）

1. 恢复 `handleWhiteNodeParams` 方法，用于批量编辑时处理白名单节点参数
2. 在编辑流程的方法中恢复调用 `handleWhiteNodeParams(editFlowRequest)`

---

### 需求点2：AISQL节点取消白名单限制

#### 需求描述
AISQL取消白名单限制，且都不显示Spark版本配置选项（强制使用Spark3版本）。

#### 技术实现

**文件位置：** `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/restful/NodeRestfulApi.java`

**实现参考（现有代码约140行）：**
删除此段代码取消aisql节点的白名单限制
```java

// 不在白名单, 跳过aisql节点
if (!isWhite && "linkis.ai.sql".equalsIgnoreCase(nodeInfo.getNodeType())){
        continue;
}
```


---

### 需求点3：提供工作流发布信息查询接口

#### 需求描述
新增API接口，用于查询工作流发布信息。可以传入批量orchestratorId查询，取每个编排最新发布成功的一个版本，编排必须在同一个项目中。

#### 接口设计

**接口路径：** `/dss/framework/orchestrator/getReleaseInfo`

**请求方式：** POST

**请求参数：**
```java
public class ReleaseInfoRequest {
    private Integer workspaceId;                       // 工作空间ID（必填）
    private Integer projectId;                         // 项目ID（必填）
    private List<Long> orchestratorIds;                // 编排ID列表（必填，支持批量查询）
    private HashMap<String, Object> labels;            // 标签信息
    private String releaseUser;                        // 发布人
    private String startTime;                          // 发布开始时间
    private String endTime;                            // 发布结束时间
    private String comment;                            // 描述
}
```

**请求参数说明：**
- `workspaceId`：必填，工作空间ID
- `projectId`：必填，项目ID
- `orchestratorIds`：必填，编排ID列表，支持批量查询，所有编排必须属于同一个项目

**响应格式：**
```json
{
    "method": "/dss/framework/orchestrator/getReleaseInfo",
    "status": 0,
    "message": "获取发布信息成功",
    "data": {
        "releaseInfoList": [
            {
                "id": 1,
                "status": "发布成功",
                "recode": "版本描述",
                "releaseUser": "发布用户",
                "version": "v1.0.0",
                "lastModifyUser": "修改用户",
                "releaseTime": "2024-01-01 12:00:00",
                "errorMessage": null,
                "orchestratorVersionId": 100,
                "appId": 1000,
                "orchestratorId": 100,
                "orchestratorName": "编排名称",
                "projectId": 1000,
                "workspaceId": 224
            }
        ]
    }
}
```

**响应字段说明：**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 发布记录ID |
| status | String | 发布状态 |
| recode | String | 版本描述 |
| releaseUser | String | 发布用户 |
| version | String | 版本号 |
| lastModifyUser | String | 最后修改用户 |
| releaseTime | String | 发布时间 |
| errorMessage | String | 错误信息（如果发布失败） |
| orchestratorVersionId | Long | 编排版本ID |
| appId | Long | 应用ID |
| **orchestratorId** | Long | **编排ID（新增）** |
| **orchestratorName** | String | **编排名称（新增）** |
| **projectId** | Integer | **项目ID（新增）** |
| **workspaceId** | Integer | **工作空间ID（新增）** |

#### 参考接口：getReleaseHistory

**接口路径：** `/dss/framework/orchestrator/getReleaseHistory`

**服务层位置：**
- 接口：`WebankAppService` (`WebankOrchestratorServer`)
- 实现：`WebankAppServiceImpl`

**现有方法签名：**
```java
Pair<Integer, List<ReleaseHistoryDetail>> getReleaseHistory(ReleaseHistoryRequest request) throws DSSErrorException;
```

#### 实现要点

1. **新增Service方法**：在 `WebankAppService` 接口中添加新方法
   ```java
   List<ReleaseInfoVO> getReleaseInfo(ReleaseInfoRequest request) throws DSSErrorException;
   ```

2. **新增Restful接口**：在 `WebankOrchestratorRestful` 中添加新端点
   ```java
   @RequestMapping(path = "/getReleaseInfo", method = RequestMethod.POST)
   public Message getReleaseInfo(@RequestBody ReleaseInfoRequest request)
   ```

3. **新增响应VO类**：`ReleaseInfoVO`，在 `ReleaseHistoryDetail` 基础上新增字段
   ```java
   public class ReleaseInfoVO extends ReleaseHistoryDetail {
       private Long orchestratorId;        // 编排ID
       private String orchestratorName;    // 编排名称
       private Integer projectId;          // 项目ID
       private Integer workspaceId;        // 工作空间ID
   }
   ```

4. **实现Service方法**：在 `WebankAppServiceImpl` 中实现查询逻辑
   - 校验所有orchestratorId是否属于同一个projectId
   - 对每个orchestratorId查询最新发布成功的版本
   - 不使用 PageHelper 分页
   - 通过 RPC 调用 `ReleaseService` 或 `ProjectServer`

5. **Mapper查询**：扩展现有的 `ReleaseTaskMapper`，添加批量查询方法

6. **SQL查询逻辑**：对每个orchestratorId，查询state状态为"success"的最新一条发布记录

   ```sql
   SELECT * FROM release_task
   WHERE orchestrator_id = ?
     AND state = 'success'
   ORDER BY release_time DESC
   LIMIT 1
   ```

7. **关联查询编排信息**：从 `dss_orchestrator_info` 表获取 `orchestratorName`

#### 关键区别（对比getReleaseHistory）

| 对比项 | getReleaseHistory | getReleaseInfo（新增） |
|--------|------------------|------------------------|
| 查询范围 | 单个编排的历史发布记录 | 批量编排的最新发布成功版本 |
| 分页 | 支持（currentPage, pageSize） | 不支持 |
| 返回值 | Pair\<Integer, List\> | List\<ReleaseInfoVO\> |
| 查询条件 | orchestratorId, releaseUser, 时间范围 | projectId, orchestratorIds[] |
| 返回字段 | 基础发布信息 | 基础发布信息 + orchestratorId, orchestratorName, projectId, workspaceId |
| 结果数量 | 可能多条历史记录 | 每个编排最多一条（最新发布成功） |

#### 业务规则

1. **编排校验**：所有传入的 `orchestratorIds` 必须属于同一个 `projectId`，否则抛出异常
2. **发布状态**：只返回发布成功（state = 'success'）的记录
3. **最新版本**：每个编排只返回最新的一条发布成功记录
4. **参数校验**：
   - `workspaceId` 必填
   - `projectId` 必填
   - `orchestratorIds` 不能为空
   - `orchestratorIds` 列表大小建议不超过100

---

## 验收标准

### 验收点1：Spark版本白名单限制
- [ ] 白名单项目中的节点正常显示Spark版本配置
- [ ] 非白名单项目中的节点不显示Spark版本配置
- [ ] 批量编辑时白名单节点的Spark版本参数正常处理

### 验收点2：AISQL节点特殊处理
- [ ] AISQL节点在白名单项目中不显示Spark版本配置
- [ ] AISQL节点在非白名单项目中不显示Spark版本配置
- [ ] 其他类型节点按白名单规则显示/隐藏Spark版本配置

### 验收点3：发布信息查询接口
- [ ] 接口正常响应，返回发布信息列表
- [ ] 支持批量传入orchestratorIds查询
- [ ] 校验所有orchestratorId必须属于同一个projectId
- [ ] 每个编排只返回最新发布成功的一条记录
- [ ] 响应包含新增字段：orchestratorId、orchestratorName、projectId、workspaceId
- [ ] 无分页参数
- [ ] 接口返回数据格式正确

---

## 涉及文件清单

### 后端Java文件
1. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/restful/NodeRestfulApi.java`
2. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java`
3. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/restful/WebankOrchestratorRestful.java`
4. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/service/WebankAppService.java`
5. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/service/impl/WebankAppServiceImpl.java`
6. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/entity/request/ReleaseInfoRequest.java` (新增)
7. `dss-framework/dss-framework-release-server-webank/src/main/java/com/webank/wedatasphere/dss/framework/release/dao/ReleaseTaskMapper.java` (修改)
8. `dss-framework/dss-framework-release-server-webank/src/main/java/com/webank/wedatasphere/dss/framework/release/dao/impl/releaseTaskMapper.xml` (修改)

### 实体类
1. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseHistoryDetail.java`
2. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/entity/ProjectOrchestratorWhite.java`
3. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/entity/response/ReleaseInfoVO.java` (新增)

---

## 测试建议

### 测试场景1：白名单限制恢复
1. 创建白名单项目，在工作流中添加节点，确认显示Spark版本配置
2. 创建非白名单项目，在工作流中添加节点，确认不显示Spark版本配置
3. 批量编辑多节点，验证Spark版本参数处理正常

### 测试场景2：AISQL节点
1. 在白名单项目中创建AISQL节点，确认不显示Spark版本配置
2. 在非白名单项目中创建AISQL节点，确认不显示Spark版本配置
3. 创建SQL节点，在白名单项目中显示Spark版本，非白名单项目不显示

### 测试场景3：发布信息查询接口
1. 调用接口查询单个编排的最新发布信息
2. 调用接口查询多个编排（批量orchestratorIds）的最新发布信息
3. 验证所有orchestratorId属于同一个projectId时正常返回
4. 验证orchestratorId属于不同projectId时抛出异常
5. 验证每个编排只返回最新发布成功的一条记录
6. 验证响应包含orchestratorId、orchestratorName等新增字段
7. 组合条件筛选

---

## 版本信息
- 需求版本：1.19.8
- 参考版本：1.19.6（Spark版本白名单取消）
- 文档创建时间：2026-03-06