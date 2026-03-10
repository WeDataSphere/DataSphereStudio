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
3. **新增发布信息查询接口**：提供根据项目名称和编排名称查询工作流发布信息的接口

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
新增API接口，用于查询工作流发布信息。可以传入项目名称和批量编排名称查询，取每个编排最新发布成功的一个版本。

#### 接口设计

**接口路径：** `/dss/framework/orchestrator/getReleaseInfo`

**请求方式：** POST

**请求参数：**
```java
public class ReleaseInfoRequest {
    private String projectName;                       // 项目名称（必填）
    private List<String> orchestratorNames;          // 编排名称列表（必填，支持批量查询）
}
```

**请求参数说明：**
- `projectName`：必填，项目名称
- `orchestratorNames`：必填，编排名称列表，支持批量查询

**响应格式：**
```json
{
    "method": "/dss/framework/orchestrator/getReleaseInfo",
    "status": 0,
    "message": "获取发布信息成功",
    "data": {
        "releaseInfoList": [
            {
                "orchestratorId": 100,
                "orchestratorName": "编排名称",
                "status": "Success",
                "releaseUser": "发布用户",
                "releaseTime": "2024-01-01 12:00:00",
                "projectId": 1000,
                "projectName": "项目名称"
            }
        ]
    }
}
```

**响应字段说明：**
| 字段名 | 类型 | 说明 |
|--------|------|------|
| orchestratorId | Long | 编排ID |
| orchestratorName | String | 编排名称 |
| status | String | 发布状态 |
| releaseUser | String | 发布用户 |
| releaseTime | String | 发布时间 |
| projectId | Long | 项目ID |
| projectName | String | 项目名称 |

#### 实现要点

1. **新增请求类 `ReleaseInfoRequest`**：
   - 位置：`dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/protocol/ReleaseInfoRequest.java`
   - 字段：`projectName`、`orchestratorNames`

2. **新增响应VO类 `ReleaseInfoVO`**：
   - 位置：`dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseInfoVO.java`
   - 字段：`orchestratorId`、`orchestratorName`、`status`、`releaseUser`、`releaseTime`、`projectId`、`projectName`

3. **新增Service方法**：在 `WebankAppService` 接口中添加新方法
   ```java
   List<ReleaseInfoVO> getReleaseInfo(ReleaseInfoRequest request) throws DSSErrorException;
   ```

4. **新增Restful接口**：在 `WebankOrchestratorRestful` 中添加新端点
   ```java
   @RequestMapping(path = "/getReleaseInfo", method = RequestMethod.POST)
   public Message getReleaseInfo(@RequestBody ReleaseInfoRequest request)
   ```

5. **新增Mapper方法**：在 `WebankOrchestratorMapper` 中添加查询方法
   ```java
   List<ReleaseInfoVO> getReleaseInfoByNames(ReleaseInfoRequest request);
   ```

6. **SQL查询逻辑**：
   ```sql
   select a.id as orchestrator_id,
          a.name as orchestrator_name,
          b.status,
          b.release_user,
          b.release_time,
          c.id as project_id,
          c.name as project_name
   from dss_orchestrator_info a
   join dss_project c on a.project_id = c.id
   left join (
     select orchestrator_id,
            release_user,
            status,
            DATE_FORMAT(max(update_time),'%Y-%m-%d %T') AS release_time
     from dss_release_task
     where status = 'Success'
     group by orchestrator_id
   ) b on a.id = b.orchestrator_id
   where a.name in (#{orchestratorNames})
     and c.name = #{projectName}
   ```


#### 业务规则

1. **编排校验**：所有传入的编排名称必须属于指定的项目（通过SQL join自动校验）
2. **发布状态**：只返回发布成功（status = 'Success'）的记录
3. **最新版本**：每个编排只返回最新的一条发布成功记录
4. **参数校验**：
   - `projectName` 必填
   - `orchestratorNames` 不能为空
   - `orchestratorNames` 列表大小建议不超过100

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
- [ ] 支持批量传入orchestratorNames查询
- [ ] 根据projectName和orchestratorNames查询
- [ ] 每个编排只返回最新发布成功的一条记录
- [ ] 响应只包含7个指定字段：orchestratorId、orchestratorName、status、releaseUser、releaseTime、projectId、projectName
- [ ] 无分页参数，接口返回数据格式正确

---

## 涉及文件清单

### 后端Java文件
1. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/restful/NodeRestfulApi.java`
2. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/service/impl/DSSFlowServiceImpl.java`
3. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/restful/WebankOrchestratorRestful.java`
4. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/service/WebankAppService.java`
5. `dss-framework/dss-framework-orchestrator-server-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/service/impl/WebankAppServiceImpl.java`
6. `dss-orchestrator/dss-orchestrator-db-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/db/dao/WebankOrchestratorMapper.java`
7. `dss-orchestrator/dss-orchestrator-db-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/db/dao/impl/WebankOrchestratorMapper.xml`
8. `dss-framework/dss-framework-release-server-webank/src/main/java/com/webank/wedatasphere/dss/framework/release/dao/ProjectMapper.java`
9. `dss-framework/dss-framework-release-server-webank/src/main/java/com/webank/wedatasphere/dss/framework/release/dao/impl/projectMapper.xml`

### 实体类（新增/修改）
1. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/protocol/ReleaseInfoRequest.java` (新增)
2. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseInfoVO.java` (新增)
3. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseHistoryDetail.java`
4. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/entity/ProjectOrchestratorWhite.java`

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
1. 调用接口根据projectName和单个orchestratorName查询发布信息
2. 调用接口根据projectName和多个orchestratorName（批量）查询发布信息
3. 验证返回的数据只包含7个指定字段
4. 验证每个编排只返回最新发布成功的一条记录
5. 验证orchestratorName不属于指定projectName时不会返回错误数据

---

## 版本信息
- 需求版本：1.19.8
- 参考版本：1.19.6（Spark版本白名单取消）
- 文档创建时间：2026-03-06
- 文档更新时间：2026-03-10