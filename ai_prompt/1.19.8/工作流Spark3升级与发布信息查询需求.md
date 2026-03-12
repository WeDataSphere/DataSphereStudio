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
4. **新增白名单管理接口**：提供ITSM鉴权接口和普通接口，用于添加工作流白名单

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
    private String reason;            // 原因（新增）
    private String type;              // 类型（新增，如 schedulis）
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

### 需求点4：提供工作流白名单管理接口

#### 需求描述
新增API接口，用于管理工作流白名单。提供两个接口：
1. ITSM鉴权接口：供ITSM系统调用，支持批量添加白名单
2. 普通接口：供用户界面调用，支持单条添加白名单

#### 接口设计

##### 接口1：addOrchestratorWhite（ITSM鉴权接口）

**接口信息：**
| 项目 | 值 |
|------|-----|
| 接口路径 | `/dss/framework/orchestrator/addOrchestratorWhite` |
| 请求方式 | POST |
| Content-Type | application/json |

**请求参数：**
```json
{
  "createDate": "2024-03-10",
  "createUser": "hadoop",
  "data": "{\"dataList\":[{\"projectName\":\"项目名称\",\"orchestratorName\":\"工作流名称\"}]}",
  "externalId": "ITSM流程ID"
}
```

**请求头：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| timeStamp | String | 是 | 时间戳 |
| sign | String | 是 | 签名 |

**参数说明：**
- `createUser`：创建用户名
- `data`：JSON字符串，包含dataList数组
- `dataList`：批量添加的白名单项列表
  - `projectName`：项目名称（必填）
  - `orchestratorName`：工作流名称（必填，支持通配符 `*` 表示项目下所有工作流）

**响应格式：**
```json
{
  "retCode": 0,
  "retDetail": "Success to add orchestrator white"
}
```

**鉴权逻辑：**
1. 从请求头获取 `timeStamp` 和 `sign` 参数
2. 调用 `WorkspaceUtils.validateAuth(timestamp, sign)` 进行鉴权验证
3. 鉴权失败返回403状态码和错误信息

##### 接口2：addOrchestratorWhiteSimple（普通接口）

**接口信息：**
| 项目 | 值 |
|------|-----|
| 接口路径 | `/dss/framework/orchestrator/addOrchestratorWhiteSimple` |
| 请求方式 | POST |
| Content-Type | application/json |

**请求类定义：**
```java
public class AddOrchestratorWhiteRequest {
    private String projectName;       // 项目名称（必填）
    private String orchestratorName;  // 工作流名称（可选，空值时自动设置为*）
    private String reason;            // 原因（可选）
}
```

**请求参数：**
```json
{
  "projectName": "项目名称",
  "orchestratorName": "工作流名称",
  "reason": "添加原因"
}
```

**参数说明：**
- `projectName`：必填，项目名称
- `orchestratorName`：可选，工作流名称
  - 不传或传空值时，自动设置为通配符 `*`
- `reason`：可选，添加白名单的原因

**响应格式：**
```json
{
  "method": "/dss/framework/orchestrator/addOrchestratorWhiteSimple",
  "status": 0,
  "message": "添加工作流白名单成功"
}
```

#### 实现要点

1. **新增请求类 `AddOrchestratorWhiteRequest`**：
   - 位置：`dss-framework/dss-framework-orchestrator-server/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/entity/request/AddOrchestratorWhiteRequest.java`
   - 字段：`projectName`、`orchestratorName`、`reason`

2. **新增Restful接口**：在 `DSSFrameworkOrchestratorRestful` 中添加新端点
   ```java
   // ITSM鉴权接口
   @RequestMapping(path = "addOrchestratorWhite", method = RequestMethod.POST)
   public ItsmResponse addProjectAndOrchestratorWhite(@RequestBody ItsmRequest itsmRequest, HttpServletRequest req, HttpServletResponse resp)

   // 普通接口
   @RequestMapping(path = "addOrchestratorWhiteSimple", method = RequestMethod.POST)
   public Message addOrchestratorWhiteSimple(@RequestBody AddOrchestratorWhiteRequest request)
   ```

3. **业务逻辑**：
   - 参数验证（projectName必填）
   - 查询项目信息
   - 查询工作流ID（支持通配符）
   - 创建白名单记录
     - 设置 `reason` 字段为请求参数中的 reason
     - 设置 `type` 字段为固定值 "schedulis"
   - 调用 `projectOrchestratorWhiteService.addProjectOrchestratorWhite()` 保存

4. **数据库变更**：
   - 在 `dss_project_orchestrator_white` 表中添加 `reason` 字段：`VARCHAR(255) COMMENT '原因'`
   - 在 `dss_project_orchestrator_white` 表中添加 `type` 字段：`VARCHAR(50) COMMENT '类型'`
   - 更新 MyBatis 映射文件中的 insert 语句，包含 `reason` 和 `type` 字段

#### 业务规则

1. **通配符支持**
   - `orchestratorName` 为 `*` 时，表示该项目的所有工作流都在白名单中
   - 此时 `orchestratorId` 设置为 0

2. **项目验证**
   - 必须传入有效的 `projectName`
   - 项目不存在时返回错误

3. **工作流验证**
   - 当 `orchestratorName` 不是通配符时，必须传入有效的 `orchestratorName`
   - 工作流不存在时返回错误
   - 工作流必须属于指定的项目

4. **ITSM批量处理**
   - ITSM接口支持批量添加，会收集所有错误统一返回
   - 普通接口只支持单条添加

5. **用户身份**
   - ITSM接口：从请求中获取createUser
   - 普通接口：从SecurityFilter.getLoginUsername()获取登录用户

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

### 验收点4：白名单管理接口
- [ ] ITSM接口鉴权功能正常，timestamp和sign验证正确
- [ ] ITSM接口支持批量添加白名单，dataList正确处理
- [ ] 普通接口正常响应，使用登录用户身份
- [ ] 支持通配符 `*` 添加项目下所有工作流到白名单
- [ ] 普通接口orchestratorName为空时自动转为通配符
- [ ] 项目不存在时返回正确错误信息
- [ ] 工作流不存在时返回正确错误信息
- [ ] 白名单记录正确插入数据库，字段值正确
- [ ] 普通接口的 `reason` 字段正确保存到数据库
- [ ] 普通接口的 `type` 字段固定设置为 "schedulis"
- [ ] 审计日志正常记录

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
10. `dss-framework/dss-framework-orchestrator-server/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/restful/DSSFrameworkOrchestratorRestful.java` (修改)

### 实体类（新增/修改）
1. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/protocol/ReleaseInfoRequest.java` (新增)
2. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseInfoVO.java` (新增)
3. `dss-orchestrator/dss-orchestrator-common-webank/src/main/java/com/webank/wedatasphere/dss/orchestrator/common/entity/ReleaseHistoryDetail.java`
4. `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server/src/main/java/com/webank/wedatasphere/dss/workflow/entity/ProjectOrchestratorWhite.java` (修改，添加reason和type字段)
5. `dss-framework/dss-framework-orchestrator-server/src/main/java/com/webank/wedatasphere/dss/orchestrator/server/entity/request/AddOrchestratorWhiteRequest.java` (新增)

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

### 测试场景4：白名单管理接口

#### ITSM接口测试
1. 调用ITSM接口，传入正确的timestamp和sign，验证鉴权成功
2. 调用ITSM接口，传入错误的timestamp或sign，验证鉴权失败返回403
3. 调用ITSM接口，批量添加多个白名单记录，验证所有记录都正确添加
4. 调用ITSM接口，dataList中包含错误数据（如项目不存在），验证错误信息被正确收集和返回
5. 调用ITSM接口，orchestratorName为通配符*，验证项目下所有工作流都被添加到白名单

#### 普通接口测试
1. 调用普通接口，传入projectName和orchestratorName，验证白名单添加成功
2. 调用普通接口，传入projectName但orchestratorName为空，验证自动转为通配符*
3. 调用普通接口，传入不存在的projectName，验证返回正确的错误信息
4. 调用普通接口，传入不存在的orchestratorName，验证返回正确的错误信息
5. 验证普通接口使用登录用户身份作为createUser
6. 验证普通接口正确记录审计日志
7. 调用普通接口，传入reason参数，验证reason字段正确保存到数据库
8. 调用普通接口，验证type字段固定设置为"schedulis"
9. 查询数据库，验证reason和type字段正确插入

---

## 版本信息
- 需求版本：1.19.8
- 参考版本：1.19.6（Spark版本白名单取消）
- 文档创建时间：2026-03-06
- 文档更新时间：2026-03-10