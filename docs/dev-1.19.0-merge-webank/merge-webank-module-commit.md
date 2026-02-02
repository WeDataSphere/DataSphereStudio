# 历史合并模块分析文档
## 基于commit 19e2155ce

---

## 1. 提交信息概览

| 属性 | 值 |
|-----|---|
| **Commit ID** | `19e2155ce09a286bb88dd8c23eabd4521c7e33b7` |
| **提交时间** | 2026年1月28日 10:03:37 |
| **提交者** | SunpengWan <825559776@qq.com> |
| **提交信息** | `#AI_commit# merge webank maven module` |
| **变更类型** | 新增(A) / 修改(M) |

---

## 2. 合并模块清单 (共17个webank模块)

### 2.1 Apps应用层模块 (4个)

| 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|---------|---------|--------|
| 1 | `dss-apps/dss-apiservice-server-webank` | API服务WeBank版本 | 103 |
| 2 | `dss-apps/dss-datapipe-server-webank` | 数据管道WeBank版本 | 66 |
| 3 | `dss-apps/dss-scriptis-server-webank` | Scriptis WeBank版本 | 10 |
| 4 | `dss-apps/dss-user-guide/dss-user-guide-server-webank` | 用户指南WeBank版本 | 2 |

---

### 2.2 Commons公共模块 (4个)

| 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|---------|---------|--------|
| 5 | `dss-commons/dss-common-server-webank` | 公共服务WeBank版本 | 30 |
| 6 | `dss-commons/dss-sender-service-webank` | 发送服务WeBank版本 | 4 |
| 7 | `dss-commons/dss-detectionservice` | 检测服务模块 | 15 |
| 8 | `dss-commons/dss-error-code` | 错误码模块(含client/common/server子模块) | 22 |

---

### 2.3 Framework框架模块 (6个)

| 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|---------|---------|--------|
| 9 | `dss-framework/dss-framework-compute-resource-manager-webank` | 计算资源管理器WeBank版本 | 18 |
| 10 | `dss-framework/dss-framework-orchestrator-server-webank` | 编排服务器WeBank版本 | 22 |
| 11 | `dss-framework/dss-framework-project-server-webank` | 项目服务器WeBank版本 | 67 |
| 12 | `dss-framework/dss-framework-release-server-webank` | 发布服务器WeBank版本 | 49 |
| 13 | `dss-framework/dss-framework-workspace-server-webank` | 工作空间服务器WeBank版本 | 118 |
| 14 | `dss-framework/framework-plugins/dss-framework-migrate-server-webank` | 迁移服务器WeBank版本 | 2 |

---

### 2.4 Orchestrator编排模块 (3个)

| 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|---------|---------|--------|
| 15 | `dss-orchestrator/dss-orchestrator-common-webank` | 编排公共模块WeBank版本 | 34 |
| 16 | `dss-orchestrator/dss-orchestrator-db-webank` | 编排数据库模块WeBank版本 | 4 |
| 17 | `dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server-webank` | 工作流执行服务器WeBank版本 | 6 |
| 18 | `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server-webank` | 工作流服务器WeBank版本 | 22 |

---

## 3. 各模块详细功能说明

### 3.1 dss-apiservice-server-webank

**核心功能：** 数据API服务管理，提供SQL查询、数据访问控制、Token管理等功能

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Application | `DSSApiServiceServerApplication.scala` | Spring Boot启动类 |
| Restful API | `ApiServiceCoreRestfulApi.java` | API核心接口 |
| Restful API | `ApiServiceExecuteRestfulApi.java` | API执行接口 |
| Restful API | `ApiServiceTokenRestfulApi.java` | Token管理接口 |
| Service | `ApiServiceImpl.java` | API服务实现 |
| Service | `ApprovalServiceImpl.java` | 审批服务实现 |
| Service | `TokenQueryServiceImpl.java` | Token查询服务 |
| Execute | `LinkisJobSubmit.java` | Linkis任务提交 |
| Token | `JwtManager.java` | JWT Token管理 |
| Token | `JwtModuleTest.java` | Token测试 |
| Strategy | `ApiHiveEngineImpl.java` | Hive引擎实现 |
| Strategy | `ApiJdbcEngineImpl.java` | JDBC引擎实现 |
| Strategy | `ApiSparkEngineImpl.java` | Spark引擎实现 |

**数据访问层 (DAO/Mapper)：**
- `ApiServiceAccessDao` - API访问记录
- `ApiServiceApprovalDao` - 审批记录
- `ApiServiceBeanDao` - API Bean管理
- `ApiServiceDao` - API管理
- `ApiServiceParamDao` - API参数管理
- `ApiServiceTokenManagerDao` - Token管理
- `ApiServiceVersionDao` - API版本管理

**核心常量/枚举：**
- `ApiVersionStatusEnum` - API版本状态
- `ApprovalStatus` - 审批状态
- `ParamType` - 参数类型
- ` RequireEnum` - 必填枚举
- `SQLMetadataInfoCheckStatus` - 元数据检查状态
- `SaveTokenEnum` - Token保存枚举

---

### 3.2 dss-datapipe-server-webank

**核心功能：** DataMap数据管道服务，提供数据集管理、元数据查询、ITSM集成等功能

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Application | `DSSDatapipeServerApplication.scala` | Spring Boot启动类 |
| Restful | `DatasetRestful.java` | 数据集接口 |
| Restful | `SchemaInfoRestful.java` | Schema信息接口 |
| Service | `DSSWorkspaceService.java` | 工作空间服务 |
| Service | `SchemaInfoService.java` | Schema服务 |
| Service | `WebankDatasetServiceImpl.java` | 数据集服务实现 |
| Constant | `ClusterTypeEnum.java` | 集群类型 |
| Constant | `EnvTypeEnum.java` | 环境类型 |
| Constant | `UsageHeatTypeEnum.java` | 使用热度类型 |
| Transferor | `CustomTransferor.java` | 自定义传输 |
| Transferor | `ITSMTransferor.java` | ITSM传输 |
| Transferor | `TablesOwnerTransferor.java` | 表所有者传输 |

**DataMap数据模型：**
- `DMSBdpTableInfo` - BDP表信息
- `DMSBdpTablePartitionInfo` - 分区信息
- `DMSColumnMaskInfo` - 列掩码信息
- `DMSDatasetMaskInfoResult` - 数据集掩码结果
- `DMSDatasetSubsystem` - 数据集子系统
- `DMSchemaBaseInfoBean` - Schema基础信息
- `DMSTagBean` - 标签Bean
- `DMSpaceInfoBean` - 空间信息Bean

**配置文件：**
- `DataMapConnConf.scala` - DataMap连接配置

---

### 3.3 dss-scriptis-server-webank

**核心功能：** Scriptis脚本服务WeBank增强版本，支持ITSM集成

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Configuration | `WebankDSSScriptisConfiguration.java` | WeBank配置类 |
| Restful | `ScriptisRestful.java` | Scriptis接口 |
| Restful | `ScriptisTaskRestful.java` | Scriptis任务接口 |
| Service | `WebankScriptisAuthServiceImpl.java` | WeBank认证服务 |
| Ruler | `DataManipulationUserRuler.java` | 数据操作用户规则 |

**ITSM集成：**
- `ItsmRequest.java` - ITSM请求
- `ItsmResponse.java` - ITSM响应

---

### 3.4 dss-framework-project-server-webank

**核心功能：** 项目管理服务WeBank版本，支持项目复制、编排复制、EC模板等功能

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Job | `ProjectCopyJob.java` | 项目复制任务 |
| Job | `OrchestratorCopyJob.java` | 编排复制任务 |
| Job | `ProjectOperateJob.java` | 项目操作任务 |
| Job | `CheckProjectCopyTask.java` | 检查项目复制任务 |
| Service | `WebankDSSProjectService.java` | WeBank项目服务 |
| Service | `WebankDSSOrchestratorService.java` | WeBank编排服务 |
| Service | `WebankDSSProjectOperateService.java` | 项目操作服务 |
| Service | `OrchestratorECConfTemplateService.java` | EC配置模板服务 |
| Hook | `ProjectAuditHttpRequestHook.java` | 项目审计Hook |
| Entity/vo | `ProjectCopyVO.java` | 项目复制VO |
| Entity/vo | `OrchestratorCopyVO.java` | 编排复制VO |
| Entity/vo | `ProjectTemplateVO.java` | 项目模板VO |

**枚举：**
- `HandOverTypeEnum` - 移交类型
- `ProjectOperateRecordStatusEnum` - 操作状态
- `ProjectOperateTypeEnum` - 操作类型

---

### 3.5 dss-framework-release-server-webank

**核心功能：** 发布框架服务，支持项目/编排批量发布、自动发布等功能

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Job | `OrchestratorReleaseJob.java` | 编排发布任务 |
| Job | `BatchReleaseReleaseJob.java` | 批量发布任务 |
| Job | `ReleaseJobDaemon.java` | 发布守护任务 |
| Job | `CheckOrchestratorReleaseJobTask.java` | 检查发布任务 |
| Hook | `CommonReleaseHook.java` | 通用发布Hook |
| Hook | `AlertReleaseHook.java` | 告警发布Hook |
| Service | `ReleaseService.java` | 发布服务 |
| Service | `ProjectService.java` | 项目服务 |
| Service | `TaskService.java` | 任务服务 |

**核心类：**
- `ReleaseTask.java` - 发布任务实体
- `ReleaseContext.java` - 发布上下文
- `ReleaseEnv.java` - 发布环境
- `ReleaseStatus.java` - 发布状态

**常量：**
- `ReleaseCodeEnum.java` - 发布代码枚举
- `ReleaseConstant.java` - 发布常量

---

### 3.6 dss-framework-workspace-server-webank

**核心功能：** 工作空间管理服务WeBank版本，提供EC配置模板、EC释放策略、权限管理等功能

**主要组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Service | `ECConfTemplateService.java` | EC配置模板服务 |
| Service | `WebankDSSWorkspaceRoleService.java` | 角色服务 |
| Service | `WebankDSSWorkspaceECService.java` | EC服务 |
| Service | `WebankDSSWorkspaceDeptService.java` | 部门服务 |
| Service | `ECReleaseSummaryInfoNotificationService.java` | EC释放汇总通知 |
| Job | `ECInstanceReleaseExecuteTask.java` | EC实例释放任务 |
| Strategy | `DefaultECKillingPriorityQueue.java` | 默认EC杀优先队列 |
| Strategy | `UserFairECKillingPriorityQueue.java` | 用户公平EC杀队列 |

**核心实体：**
- `ECConfTemplate` - EC配置模板
- `ECConfTemplateApplyRule` - EC配置模板应用规则
- `ECReleaseStrategy` - EC释放策略
- `ECKillHistoryRecord` - EC杀历史记录
- `PermissionUser` - 权限用户
- `PermissionDepartment` - 权限部门

---

### 3.7 dss-error-code

**核心功能：** 错误码统一管理，提供错误码同步、匹配、通知等功能

**子模块结构：**

| 子模块 | 功能 |
|-------|------|
| `dss-error-code-client` | 错误码客户端 |
| `dss-error-code-common` | 错误码公共模块 |
| `dss-error-code-server` | 错误码服务端 |

**核心组件 (client子模块)：**
- `LinkisErrorCodeClient.java` - Linkis错误码客户端
- `LinkisErrorCodeSynchronizer.java` - 错误码同步器
- `LinkisErrorCodeManager.java` - 错误码管理器
- `ErrorCodeHandler` - 错误码处理器
- `LinkisJobhistoryAop.scala` - 任务历史AOP
- `RestfulSolutionUrlAOP.scala` - 解决方案URL AOP

---

### 3.8 dss-detectionservice

**核心功能：** 检测服务，支持消息检测、方法检测等功能

**核心组件：**

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| Service | `DetectionService.scala` - | 检测服务 |
| Message | `DetectionMessage.java` | 检测消息 |
| Message | `TextMessage.java` | 文本消息 |
| Message | `StreamMessage.java` | 流消息 |
| Message | `SubmitInformation.java` | 提交信息 |
| Method | `DetectionMethod.java` | 检测方法 |
| Method | `OfficialDetectionMethod.java` | 官方检测方法 |
| Result | `DetectionResult.java` | 检测结果 |
| Result | `NormalDetectionResult.java` | 正常检测结果 |
| Result | `DetectionResultFactory.java` | 结果工厂 |

---

## 4. 修改的文件

### 4.1 根目录文件

| 文件 | 变更类型 | 说明 |
|-----|---------|------|
| `.gitignore` | M(M) | Git忽略文件更新 |
| `pom.xml` | M(M) | 根POM更新 |

### 4.2 子模块POM文件更新

| 文件 | 变更类型 | 说明 |
|-----|---------|------|
| `dss-appconn/appconns/dss-datachecker-appconn/pom.xml` | M(M) |
| `dss-appconn/appconns/dss-eventchecker-appconn/src/main/.../EventCheckerHttpUtils.java` | M(M) |
| `dss-apps/dss-data-api/pom.xml` | M(M) |
| `dss-apps/dss-data-governance/dss-data-asset-server/pom.xml` | M(M) |
| `dss-apps/dss-mide-server/pom.xml` | M(M) |
| `dss-apps/pom.xml` | M(M) |
| `dss-commons/dss-common-server-webank/pom.xml` | A(A) |
| `dss-commons/dss-detectionservice/pom.xml` | A(A) |
| `dss-commons/dss-error-code/dss-error-code-client/pom.xml` | A(A) |
| `dss-commons/dss-error-code/dss-error-code-common/pom.xml` | A(A) |
| `dss-commons/dss-error-code/dss-error-code-server/pom.xml` | A(A) |
| `dss-commons/dss-error-code/pom.xml` | A(A) |
| `dss-commons/dss-sender-service-webank/pom.xml` | A(A) |
| `dss-commons/pom.xml` | M(M) |
| `dss-framework/dss-framework-compute-resource-manager-webank/pom.xml` | A(A) |
| `dss-framework/dss-framework-orchestrator-server-webank/pom.xml` | A(A) |
| `dss-framework/dss-framework-project-server-webank/pom.xml` | A(A) |
| `dss-framework/dss-framework-release-server-webank/pom.xml` | A(A) |
| `dss-framework/dss-framework-workspace-server-webank/pom.xml` | A(A) |
| `dss-framework/framework-plugins/dss-framework-migrate-server-webank/pom.xml` | A(A) |
| `dss-framework/framework-plugins/dws-migrate-to-dss/pom.xml` | A(A) |
| `dss-framework/pom.xml` | M(M) |
| `dss-orchestrator/dss-orchestrator-common-webank/pom.xml` | A(A) |
| `dss-orchestrator/dss-orchestrator-db-webank/pom.xml` | A(A) |
| `dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server-webank/pom.xml` | A(A) |
| `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server-webank/pom.xml` | A(A) |
| `dss-orchestrator/orchestrators/dss-workflow/pom.xml` | M(M) |
| `dss-orchestrator/pom.xml` | M(M) |

---

## 5. 模块依赖关系

### 5.1 核心依赖图

```
dss-apps/
├── dss-apiservice-server-webank
│   ├── depends: dss-detectionservice
│   └── depends: dss-error-code-client
├── dss-datapipe-server-webank
│   └── depends: dss-common-server-webank
├── dss-scriptis-server-webank
│   └── depends: dss-common-server-webank
└── dss-user-guide-server-webank

dss-commons/
├── dss-common-server-webank (公共基础服务)
│   ├── ESB集成
│   ├── IMS集成
│   ├── 发布通知
│   └── 用户访问审计
├── dss-sender-service-webank (发送服务增强)
├── dss-detectionservice (检测服务)
└── dss-error-code/ (错误码管理)
    ├── dss-error-code-client
    ├── dss-error-code-common
    └── dss-error-code-server

dss-framework/
├── dss-framework-project-server-webank (项目管理)
│   └── depends: dss-orchestrator-common-webank
├── dss-framework-orchestrator-server-webank (编排管理)
│   └── depends: dss-orchestrator-common-webank
├── dss-framework-workspace-server-webank (工作空间管理)
│   └── depends: dss-common-server-webank
├── dss-framework-release-server-webank (发布管理)
│   └── depends: dss-orchestrator-common-webank
├── dss-framework-compute-resource-manager-webank (资源管理)
│   └── depends: dss-common-server-webank
└── framework-plugins/
    ├── dss-framework-migrate-server-webank (迁移服务)
    └── dws-migrate-to-dss (DWS迁移)

dss-orchestrator/
├── dss-orchestrator-common-webank (编排公共模块)
├── dss-orchestrator-db-webank (编排数据库模块)
└── orchestrators/dss-workflow/
    ├── dss-workflow-server-webank (工作流服务)
    │   └── depends: dss-orchestrator-common-webank
    └── dss-flow-execution-server-webank (工作流执行)
        └── depends: dss-orchestrator-common-webank
```

---

## 6. WeBank特有功能清单

### 6.1 ITSM集成

| 模块 | 功能点 |
|-----|-------|
| dss-datapipe-server-webank | ITSM请求处理、数据集审批 |
| dss-common-server-webank | IMS告警服务、通知发送 |
| dss-framework-workspace-server-webank | EC释放IMS通知 |
| dss-scriptis-server-webank | ITSM认证集成 |

### 6.2 ESB集成

| 模块 | 功能点 |
|-----|-------|
| dss-common-server-webank | ESB配置、ESB客户端 |
| dss-common-server-webank | ESB告警配置 |

### 6.3 项目/编排复制

| 模块 | 功能点 |
|-----|-------|
| dss-framework-project-server-webank | 项目内复制、跨项目复制 |
| dss-framework-release-server-webank | 批量发布、自动发布 |

### 6.4 EC资源管理

| 模块 | 功能点 |
|-----|-------|
| dss-framework-workspace-server-webank | EC配置模板、EC释放策略 |
| dss-framework-compute-resource-manager-webank | Linkis资源管理 |

---

## 7. 数据库相关

### 7.1 新增Mapper/DAO层

以下模块包含数据库访问层：

| 模块 | Mapper/DAO |
|-----|-----------|
| dss-apiservice-server-webank | ApiServiceMapper, ApiServiceBeanMapper等 |
| dss-datapipe-server-webank | DSSWorkspaceUserMapper, DatasetScanRecordMapper |
| dss-framework-project-server-webank | WebankDSSProjectMapper, ECTemplateWorkflowMapper |
| dss-framework-workspace-server-webank | ECConfigTemplateMapper, WebankDSSWorkspaceDeptMapper |
| dss-framework-release-server-webank | ProjectMapper, ReleaseTaskMapper |
| dss-error-code-server | ErrorCodeMapper |
| dss-orchestrator-db-webank | WebankOrchestratorMapper |

---

## 8. 配置汇总

### 8.1 新增配置类

| 模块 | 配置类 |
|-----|-------|
| dss-apiservice-server-webank | ApiServiceConfiguration |
| dss-datapipe-server-webank | DSSDataPipeConfiguration, DataMapConnConf |
| dss-scriptis-server-webank | WebankDSSScriptisConfiguration |
| dss-common-server-webank | CommonServerConfiguration, WDSMybatisConfiguration, EsbConf |
| dss-compute-resource-manager-webank | LinkisConnConf |
| dss-framework-workspace-server-webank | WorkspaceConfiguration, WorkspaceSpringConf |
| dss-error-code-client | ClientConfiguration |
| dss-error-code-common | CommonConf |

---

## 9. 潜在影响分析

### 9.1 对现有系统的影响

| 影响项 | 影响描述 | 影响等级 |
|-------|---------|---------|
| 数据库表结构 | 新增大量webank相关表 | 高 |
| 配置文件 | 需要新增webank相关配置 | 中 |
| 服务端口 | 需要规划新增服务端口 | 中 |
| 依赖管理 | 新增外部依赖(ESB/ITSM) | 低 |
| API接口 | 新增大量RESTful API | 低 |

### 9.2 需要额外配置的外部服务

| 服务名称 | 用途 | 配置项示例 |
|---------|------|-----------|
| ESB服务 | 企业服务总线 | wds.dss.esb.endpoint |
| IMS服务 | 统一消息服务 | wds.dss.ims.endpoint |
| ITSM服务 | IT服务管理 | wds.dss.itsm.request.url |
| DataMap服务 | 数据地图 | wds.linkis.datamap.token |
| Linkis元数据 | Linkis元数据服务 | wds.linkis.metadata.datamap.ip |

---

## 10. 遗留工作清单

根据此次提交的历史合并，以下模块尚未合并：

| 序号 | 源模块路径 | 状态 |
|-----|-----------|------|
| 1 | dss-appconn/appconns/dss-datachecker-appconn-webank | 待合并 |
| 2 | dss-appconn/appconns/dss-eventchecker-appconn-webank | 待合并 |
| 3 | dss-appconn/appconns/dss-orchestrator-framework-appconn-webank | 待合并 |
| 4 | dss-appconn/appconns/dss-schedulis-appconn-webank | 待合并 |
| 5 | dss-appconn/appconns/dss-scriptis-appconn-webank | 待合并 |
| 6 | dss-appconn/appconns/dss-sendemail-appconn-webank | 待合并 |
| 7 | dss-appconn/appconns/dss-sso-appconn-webank | 待合并 |
| 8 | dss-appconn/appconns/dss-workflow-appconn-webank | 待合并 |
| 9 | dss-apps/dss-apps-server-webank | 待合并 |
| 10 | dss-server-webank | 待合并 |
| 11 | plugins/dss-bes | 待合并 |

---

## 11. 编译验证建议

### 11.1 编译命令

```bash
# 清理之前编译产物
mvn clean

# 跳过测试编译
mvn compile -DskipTests

# 打包验证
mvn package -DskipTests
```

### 11.2 分模块编译

```bash
# 编译apps模块
mvn compile -DskipTests -pl dss-apps/dss-apiservice-server-webank
mvn compile -DskipTests -pl dss-apps/dss-datapipe-server-webank

# 编译commons模块
mvn compile -DskipTests -pl dss-commons/dss-common-server-webank
mvn compile -DskipTests -pl dss-commons/dss-error-code

# 编译framework模块
mvn compile -DskipTests -pl dss-framework/dss-framework-project-server-webank
mvn compile -DskipTests -pl dss-framework/dss-framework-workspace-server-webank

# 编译orchestrator模块
mvn compile -DskipTests -pl dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server-webank
mvn compile -DskipTests -pl dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server-webank
```

---

## 12. 附录

### 12.1 变更统计

| 分类 | 数量 |
|-----|------|
| 新增文件 | 约600+ |
| 修改文件 | 19 |
| 新增模块 | 17 |
| 新增独立模块(dws-migrate-to-dss) | 1 |

### 12.2 主要变更类型分布

| 变更类型 | 文件数量 |
|---------|---------|
| Java源文件 | 约500 |
| Scala源文件 | 约40 |
| XML Mapper文件 | 约30 |
| Assembly配置 | 约20 |
| 脚本文件(bin/sh) | 约4 |

### 12.3 关键技术栈

| 技术 | 版本/描述 |
|-----|----------|
| Spring Boot | 2.7.x |
| Spring Cloud | 2021.0.8 |
| MyBatis | 持久层框架 |
| Linkis | 1.14.2-wds |
| Scala | 2.11.12 |
| JDK | 1.8 |
| Maven | 3.3.3+ |

---

**文档版本历史**

| 版本 | 日期 | 修改人 | 修改内容 |
|-----|------|-------|---------|
| 1.0 | 2026-02-02 | Claude Code | 初始版本基于commit 19e2155ce |