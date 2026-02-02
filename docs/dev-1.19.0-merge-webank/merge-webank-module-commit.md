# DSS Maven模块合并设计文档

## 1. 文档概述

### 1.1 文档目的
本文档旨在详细描述将 `wedatasphere-dataspherestudio` 项目的Maven模块合并到 `dataspherestudio-wedatasphere-sync` 项目（目标项目）的完整设计方案。

### 1.2 适用范围
- 开发人员：模块合并实施
- 项目经理：合并进度跟踪
- 测试人员：合并后功能验证

### 1.3 项目信息

| 属性 | 源项目                             | 目标项目 |
|------|---------------------------------|---------|
| **路径** | `wedatasphere-dataspherestudio` | `dataspherestudio-wedatasphere-sync` |
| **ArtifactId** | `dss-webank`                    | `dss` |
| **当前分支** | `1.19.0-webank-merge`             | `dev-1.19.0-merge-webank` |
| **版本** | `1.19.0-merge-webank`           | `1.19.0-merge-webank-SNAPSHOT` |
| **Linkis版本** | `1.14.2-wds`                    | `1.14.2-wds` |
| **Java版本** | 1.8                             | 1.8 |
| **Scala版本** | 2.11.12                         | 2.11.12 |
| **Spring Boot** | 2.7.11                          | 2.7.18 |

### 1.4 合并目标
将源项目中的特有webank模块合并到目标项目，形成统一的DSS项目结构。

---

## 2. 项目结构对比

### 2.1 源项目模块结构

```
wedatasphere-dataspherestudio/
├── dss-appconn/
│   ├── appconns/
│   │   ├── dss-datachecker-appconn-webank/
│   │   ├── dss-eventchecker-appconn-webank/
│   │   ├── dss-orchestrator-framework-appconn-webank/
│   │   ├── dss-schedulis-appconn-webank/
│   │   ├── dss-scheduler-appconn-webank/
│   │   ├── dss-sendemail-appconn-webank/
│   │   ├── dss-scriptis-appconn-webank/
│   │   ├── dss-sso-appconn-webank/
│   │   └── dss-workflow-appconn-webank/
│   └── pom.xml
├── dss-apps/
│   ├── dss-apps-server-webank/
│   └── pom.xml
├── dss-server-webank/
├── plugins/
│   ├── azkaban/linkis-jobtype/
│   └── dss-bes/
├── assembly/
├── pom.xml
```

### 2.2 目标项目模块结构

```
dataspherestudio-wedatasphere-sync/
├── dss-commons/                          # 公共模块
│   ├── dss-common/
│   ├── dss-contextservice/
│   ├── dss-detectionservice/              # 已合并
│   ├── dss-error-code/                   # 已合并 (含3子模块)
│   ├── dss-sender-service/
│   ├── dss-common-server-webank/         # 已合并
│   └── dss-sender-service-webank/        # 已合并
├── dss-standard/                         # 标准模块
├── dss-orchestrator/                     # 编排模块
│   ├── dss-orchestrator-common-webank/   # 已合并
│   ├── dss-orchestrator-db-webank/       # 已合并
│   ├── orchestrators/dss-workflow/
│   │   ├── dss-workflow-server-webank/   # 已合并
│   │   └── dss-flow-execution-server-webank/ # 已合并
│   └── pom.xml
├── dss-appconn/                          # AppConn模块
│   ├── dss-appconn-core/
│   ├── dss-appconn-loader/
│   ├── dss-appconn-manager/
│   ├── dss-scheduler-appconn/            # 原有
│   ├── dss-scheduler-appconn-webank/     # 已合并 (commit 5d7f95f6c)
│   ├── linkis-appconn-engineplugin/
│   └── appconns/
│       ├── dss-datachecker-appconn/      # 无webank后缀
│       ├── dss-dolphinscheduler-appconn/
│       ├── dss-eventchecker-appconn/     # 无webank后缀
│       ├── dss-schedulis-appconn/        # 无webank后缀
│       ├── dss-workflow-appconn/         # 无webank后缀
│       └── ...
├── dss-framework/                        # 框架模块
│   ├── dss-framework-orchestrator-server-webank/ # 已合并
│   ├── dss-framework-project-server-webank/      # 已合并
│   ├── dss-framework-workspace-server-webank/    # 已合并
│   ├── dss-framework-release-server-webank/      # 已合并
│   └── dss-framework-*-server-webank/    # 其他已合并的webank模块
├── dss-apps/                             # 应用模块
│   ├── dss-apps-server/
│   ├── dss-apiservice-server/            # 原有
│   ├── dss-apiservice-server-webank/     # 已合并
│   ├── dss-scriptis-server/
│   ├── dss-scriptis-server-webank/       # 已合并
│   ├── dss-datapipe-server-webank/       # 已合并
│   ├── dss-user-guide-server-webank/     # 已合并
│   └── dss-data-api/
├── dss-server/
├── dss-git/
├── plugins/
│   ├── linkis/dss-gateway-support/
│   ├── azkaban/linkis-jobtype/
│   └── dolphinscheduler/
├── assembly/
├── pom.xml
└── CLAUDE.md
```

### 2.3 模块对应关系分析

| 源项目模块 | 目标项目对应模块 | 合并状态 |
|-----------|-----------------|---------|
| `dss-appconn/dss-scheduler-appconn-webank` | `dss-appconn/dss-scheduler-appconn-webank` | 已合并 (commit 5d7f95f6c) |
| `dss-appconn/appconns/dss-datachecker-appconn-webank` | `dss-appconn/appconns/dss-datachecker-appconn` | 待合并 |
| `dss-appconn/appconns/dss-eventchecker-appconn-webank` | `dss-appconn/appconns/dss-eventchecker-appconn` | 待合并 |
| `dss-appconn/appconns/dss-orchestrator-framework-appconn-webank` | - | 待新增 |
| `dss-appconn/appconns/dss-schedulis-appconn-webank` | `dss-appconn/appconns/dss-schedulis-appconn` | 待合并 |
| `dss-appconn/appconns/dss-scriptis-appconn-webank` | `dss-appconn/appconns/dss-scriptis-appconn` | 待合并 |
| `dss-appconn/appconns/dss-sendemail-appconn-webank` | `dss-appconn/appconns/dss-sendemail-appconn` | 待合并 |
| `dss-appconn/appconns/dss-sso-appconn-webank` | `dss-appconn/appconns/dss-sso-appconn` | 待合并 |
| `dss-appconn/appconns/dss-workflow-appconn-webank` | `dss-appconn/appconns/dss-workflow-appconn` | 待合并 |
| `dss-apps/dss-apps-server-webank` | `dss-apps/dss-apps-server` | 待合并 |
| `dss-server-webank` | `dss-server` | 待合并 |
| `plugins/dss-bes` | - | 待新增 |

---

## 3. 历史合并记录

### 3.1 第一次合并 (commit 19e2155ce)

**提交时间：** 2026年1月28日 10:03:37
**提交信息：** `#AI_commit# merge webank maven module`

#### 已合并模块列表 (18个)

| 分类 | 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|------|---------|---------|--------|
| **Apps应用层** | 1 | `dss-apps/dss-apiservice-server-webank` | API服务WeBank版本 | 103 |
| | 2 | `dss-apps/dss-datapipe-server-webank` | 数据管道WeBank版本 | 66 |
| | 3 | `dss-apps/dss-scriptis-server-webank` | Scriptis WeBank版本 | 10 |
| | 4 | `dss-apps/dss-user-guide/dss-user-guide-server-webank` | 用户指南WeBank版本 | 2 |
| **Commons公共模块** | 5 | `dss-commons/dss-common-server-webank` | 公共服务WeBank版本 | 30 |
| | 6 | `dss-commons/dss-sender-service-webank` | 发送服务WeBank版本 | 4 |
| | 7 | `dss-commons/dss-detectionservice` | 检测服务模块 | 15 |
| | 8 | `dss-commons/dss-error-code` | 错误码模块 | 22 |
| **Framework框架** | 9 | `dss-framework/dss-framework-compute-resource-manager-webank` | 计算资源管理器 | 18 |
| | 10 | `dss-framework/dss-framework-orchestrator-server-webank` | 编排服务器 | 22 |
| | 11 | `dss-framework/dss-framework-project-server-webank` | 项目服务器 | 67 |
| | 12 | `dss-framework/dss-framework-release-server-webank` | 发布服务器 | 49 |
| | 13 | `dss-framework/dss-framework-workspace-server-webank` | 工作空间服务器 | 118 |
| | 14 | `dss-framework/framework-plugins/dss-framework-migrate-server-webank` | 迁移服务器 | 2 |
| | 15 | `dss-framework/framework-plugins/dws-migrate-to-dss` | DWS迁移工具 | 25 |
| **Orchestrator编排** | 16 | `dss-orchestrator/dss-orchestrator-common-webank` | 编排公共模块 | 34 |
| | 17 | `dss-orchestrator/dss-orchestrator-db-webank` | 编排数据库模块 | 4 |
| | 18 | `dss-orchestrator/orchestrators/dss-workflow/dss-workflow-server-webank` | 工作流服务器 | 22 |
| | | `dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server-webank` | 工作流执行 | 6 |

### 3.2 第二次合并 (commit 5d7f95f6c)

**提交时间：** 2026年1月28日 15:25:44
**提交信息：** `fix: 修复模块合并后,编译出现的问题`

#### 新增模块 (1个)

| 序号 | 模块路径 | 功能描述 | 文件数 |
|-----|---------|---------|--------|
| 1 | `dss-appconn/dss-scheduler-appconn-webank` | 调度器AppConn WeBank版本 | 22 |

#### 主要组件

| 组件类型 | 类名/文件 | 功能 |
|---------|----------|------|
| AppConn | `InternalSchedulerAppConn.java` | 内部调度器AppConn |
| Operation | `RefFlowCompareInfoUploadOperation.java` | 流程比较信息上传 |
| Operation | `RefOrchestrationActiveFlagOperation.java` | 编排激活标志 |
| Operation | `RefOrchestrationCronOperation.java` | 编排调度 |
| Operation | `RefOrchestrationExecutionInfoOperation.java` | 编排执行信息 |
| Operation | `RefOrchestrationScheduleInfoOperation.java` | 编排调度信息 |
| Operation | `RefOrchestrationUncronOperation.java` | 取消编排调度 |
| Operation | `RefProxyUserFetchOperation.java` | 代理用户获取 |
| RequestRef | `RefOrchestrationActiveFlagRequestRef.java` | 编排激活标志请求引用 |
| RequestRef | `RefOrchestrationCronRequestRef.java` | 编排调度请求引用 |
| ResponseRef | `RefOrchestrationExecutionInfoResponseRef.java` | 执行信息响应引用 |
| ResponseRef | `RefOrchestrationScheduleInfoResponseRef.java` | 调度信息响应引用 |
| ResponseRef | `RefProxyUserFetchResponseRef.java` | 代理用户响应引用 |

#### 修改的文件

| 文件 | 变更原因 |
|-----|---------|
| `dss-appconn/pom.xml` | 添加scheduler-appconn-webank模块声明 |
| `dss-framework/dss-framework-project-server-webank/pom.xml` | 修复编译依赖问题 |
| `dss-framework/dss-framework-workspace/src/test/.../DSSWorkspaceUserRestfulTest.java` | 修复测试 |

---

## 4. 需要合并的模块清单

### 4.1 待合并的核心模块（共11个）

| 序号 | 源模块路径 | 合并策略 | 优先级 |
|------|-----------|---------|-------|
| 1 | `dss-appconn/appconns/dss-datachecker-appconn-webank` | 合并到现有模块或作为子模块 | 高 |
| 2 | `dss-appconn/appconns/dss-eventchecker-appconn-webank` | 合并到现有模块或作为子模块 | 高 |
| 3 | `dss-appconn/appconns/dss-orchestrator-framework-appconn-webank` | 新增模块 | 中 |
| 4 | `dss-appconn/appconns/dss-schedulis-appconn-webank` | 合并到现有模块或作为子模块 | 中 |
| 5 | `dss-appconn/appconns/dss-scriptis-appconn-webank` | 合并到现有模块或作为子模块 | 中 |
| 6 | `dss-appconn/appconns/dss-sendemail-appconn-webank` | 合并到现有模块 | 高 |
| 7 | `dss-appconn/appconns/dss-sso-appconn-webank` | 合并到现有模块或作为子模块 | 低 |
| 8 | `dss-appconn/appconns/dss-workflow-appconn-webank` | 合并到现有模块或作为子模块 | 高 |
| 9 | `dss-apps/dss-apps-server-webank` | 合并到现有模块 | 中 |
| 10 | `dss-server-webank` | 合并到现有模块 | 高 |
| 11 | `plugins/dss-bes` | 新增模块 | 低 |

---

## 5. 合并策略设计

### 5.1 AppConn模块合并策略

#### 策略概述：
目标项目中部分AppConn模块已存在（无webank后缀），源项目中有对应的webank版本。

存在两种合并策略：

**策略A：作为webank子模块合并**
```xml
<!-- 在对应AppConn的 pom.xml 中添加webank模块 -->
<modules>
    <module>appconn-core</module>
    <module>appconn-webank</module>  <!-- 新增 WeBank 扩展 -->
</modules>
```

**策略B：直接合并到现有模块**
将源模块代码合并到目标模块，通过配置开关控制功能。

#### 推荐策略：

| 模块 | 推荐策略 | 原因 |
|-----|---------|------|
| dss-sendemail-appconn | 策略A | 有明确的子模块结构（esb-support, itsm-support） |
| dss-datachecker-appconn | 策略B | 功能差异较小，通过配置区分 |
| dss-eventchecker-appconn | 策略B | 功能差异较小，通过配置区分 |
| dss-schedulis-appconn | 策略A | 编排相关可能差异较大 |
| dss-scriptis-appconn | 策略A | ITSM集成等差异较大 |
| dss-workflow-appconn | 策略A | 核心编排差异较大 |
| dss-sso-appconn | 策略B | 单点登录功能差异可配置 |

### 5.2 服务器模块合并策略

| 模块 | 合并策略 | 说明 |
|------|---------|------|
| dss-server-webank | 复制到 dss-server | 由于是顶层服务器模块，直接合并 |
| dss-apps-server-webank | 复制到 dss-apps-server | 已有对应版本，合并webank增强 |

### 5.3 BES插件合并策略

`plugins/dss-bes` 是宝信应用服务器相关插件，作为新增模块直接添加到目标项目的 `plugins` 目录下。

---

## 6. 配置版本差异分析

### 6.1 关键版本差异

| 配置项 | 源项目版本 | 目标项目版本 | 冲突说明 |
|-------|-----------|-------------|---------|
| beanutils.version | 1.11.0 | 1.9.4 | 需要统一为目标版本或更新 |
| spring.boot.version | 2.7.11 | 2.7.18 | 目标版本更新，需测试兼容性 |
| xstream.version | 1.4.21 | 1.4.20 | 源版本更新，需测试兼容性 |
| commons-lang3.version | 3.8.1 | 3.8.1 | 无冲突 ✓ |
| dss.version | 1.19.0-merge-webank-SNAPSHOT | 1.19.0-merge-webank-SNAPSHOT | 无冲突 ✓ |
| linkis.version | 1.14.2-wds | 1.14.2-wds | 无冲突 ✓ |

### 6.2 新增依赖

源项目中需要继承的额外依赖：

```xml
<!-- reflections (可能在目标项目中原有) -->
<reflections.version>0.9.12</reflections.version>
<jersey-bean-validation.version>2.21</jersey-bean-validation.version>
<aspectj.version>1.9.5</aspectj.version>

<!-- BES相关依赖 -->
<bes.version>9.5.5.0141</bes.version>

<!-- 外部依赖 -->
<dependency>
    <groupId>com.webank.bdp</groupId>
    <artifactId>email-client</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 6.3 编译配置差异

| 配置项 | 源项目 | 目标项目 | 说明 |
|-------|-------|---------|------|
| recompileMode | incremental | all | 目标项目配置更保守 |
| useZincServer | true | false | 目标项目禁用Zinc |

---

## 7. 潜在冲突识别

### 7.1 包冲突

| 包名 | 源项目 | 目标项目 | 描述 |
|-----|-------|---------|------|
| `com.webank.wedatasphere.dss.appconn.sendemail.*` | 是 | 是 | sendemail AppConn |
| `com.webank.wedatasphere.dss.appconn.dssdatachecker.*` | 是 | 是 | datachecker AppConn |
| `com.webank.wedatasphere.dss.appconn.checkevent.*` | 是 | 是 | eventchecker AppConn |
| `com.webank.wedatasphere.dss.appconn.scheduler.*` | 是 | 是 | scheduler AppConn |
| `com.webank.wedatasphere.dss.appconn.workflow.*` | 是 | 是 | workflow AppConn |

### 7.2 类冲突

| 类路径 | 潜在冲突 | 解决方案 |
|-------|---------|---------|
| InternalSchedulerAppConn | 可能与原scheduler冲突 | 已在commit 5d7f95f6c中通过webank子模块解决 |

### 7.3 配置文件冲突

| 文件 | 潜在冲突 | 解决方案 |
|-----|---------|---------|
| `application.yml` | server配置 | 合并后保留差异配置 |
| `application-dss.yml` | 数据源配置 | 需要统一配置入口 |

---

## 8. 模块依赖关系

### 8.1 核心依赖图

```
dss-apps/
├── dss-apiservice-server-webank (已合并)
│   ├── depends: dss-detectionservice
│   └── depends: dss-error-code-client
├── dss-datapipe-server-webank (已合并)
│   └── depends: dss-common-server-webank
├── dss-scriptis-server-webank (已合并)
│   └── depends: dss-common-server-webank
├── dss-user-guide-server-webank (已合并)
└── dss-apps-server-webank (待合并)

dss-commons/
├── dss-common-server-webank (已合并)
├── dss-sender-service-webank (已合并)
├── dss-detectionservice (已合并)
└── dss-error-code/ (已合并)
    ├── dss-error-code-client
    ├── dss-error-code-common
    └── dss-error-code-server

dss-framework/
├── dss-framework-project-server-webank (已合并)
├── dss-framework-orchestrator-server-webank (已合并)
├── dss-framework-workspace-server-webank (已合并)
├── dss-framework-release-server-webank (已合并)
├── dss-framework-compute-resource-manager-webank (已合并)
└── framework-plugins/
    ├── dss-framework-migrate-server-webank (已合并)
    └── dws-migrate-to-dss (已合并)

dss-orchestrator/
├── dss-orchestrator-common-webank (已合并)
├── dss-orchestrator-db-webank (已合并)
└── orchestrators/dss-workflow/
    ├── dss-workflow-server-webank (已合并)
    └── dss-flow-execution-server-webank (已合并)

dss-appconn/
├── dss-scheduler-appconn (原有)
├── dss-scheduler-appconn-webank (已合并 - commit 5d7f95f6c)
├── appconns/
│   ├── dss-datachecker-appconn (原有)
│   ├── dss-eventchecker-appconn (原有)
│   ├── dss-schedulis-appconn (原有)
│   ├── dss-workflow-appconn (原有)
│   ├── dss-sendemail-appconn (原有)
│   └── dss-scriptis-appconn (原有)
```

---

## 9. 新合并模块详解 (commit 5d7f95f6c)

### 9.1 dss-scheduler-appconn-webank

**核心功能：** 内部调度器AppConn WeBank版本，增强调度操作能力

**核心类结构：**

```
dss-scheduler-appconn-webank/
├── src/main/java/com/webank/wedatasphere/dss/workflow/core/
│   ├── InternalSchedulerAppConn.java           # 内部调度器AppConn
│   ├── operation/                              # 操作层
│   │   ├── RefFlowCompareInfoUploadOperation.java
│   │   ├── RefOrchestrationActiveFlagOperation.java
│   │   ├── RefOrchestrationCronOperation.java
│   │   ├── RefOrchestrationExecutionInfoOperation.java
│   │   ├── RefOrchestrationScheduleInfoOperation.java
│   │   ├── RefOrchestrationUncronOperation.java
│   │   └── RefProxyUserFetchOperation.java
│   └── ref/                                    # 引用层
│       ├── RefOrchestrationActiveFlagRequestRef.java
│       ├── RefOrchestrationCronRequestRef.java
│       ├── RefOrchestrationExecutionInfoResponseRef.java
│       ├── RefOrchestrationScheduleInfoResponseRef.java
│       ├── RefProxyUserFetchResponseRef.java
│       └── impl/                               # 实现层
│           ├── RefOrchestrationActiveFlagRequestRefImpl.java
│           ├── RefOrchestrationCronRequestRefImpl.java
│           ├── RefOrchestrationExecutionInfoResponseRefImpl.java
│           ├── RefOrchestrationScheduleInfoResponseRefImpl.java
│           └── RefProxyUserFetchResponseRefImpl.java
└── pom.xml
```

**Operation功能映射：**

| Operation类 | 功能描述 | 对应操作 |
|------------|---------|---------|
| RefFlowCompareInfoUploadOperation | 流程比较信息上传 | 比较工作流差异 |
| RefOrchestrationActiveFlagOperation | 编排激活标志 | 激活/停用编排 |
| RefOrchestrationCronOperation | 编排调度操作 | 定时任务设置 |
| RefOrchestrationExecutionInfoOperation | 编排执行信息 | 查询执行状态 |
| RefOrchestrationScheduleInfoOperation | 编排调度信息 | 查询调度配置 |
| RefOrchestrationUncronOperation | 取消编排调度 | 取消定时任务 |
| RefProxyUserFetchOperation | 代理用户获取 | 获取代理用户信息 |

**RequestRef/ResponseRef结构：**

| Ref类型 | 用途 |
|---------|------|
| RequestRef | 定义请求数据结构和参数 |
| ResponseRef | 定义响应数据结构和结果 |

---

## 10. WeBank特有功能清单

### 10.1 ITSM集成

| 模块 | 功能点 | 状态 |
|-----|-------|------|
| dss-datapipe-server-webank | ITSM请求处理、数据集审批 | 已合并 |
| dss-common-server-webank | IMS告警服务、通知发送 | 已合并 |
| dss-framework-workspace-server-webank | EC释放IMS通知 | 已合并 |
| dss-scriptis-server-webank | ITSM认证集成 | 已合并 |

### 10.2 ESB集成

| 模块 | 功能点 | 状态 |
|-----|-------|------|
| dss-common-server-webank | ESB配置、ESB客户端 | 已合并 |
| dss-common-server-webank | ESB告警配置 | 已合并 |

### 10.3 项目/编排复制

| 模块 | 功能点 | 状态 |
|-----|-------|------|
| dss-framework-project-server-webank | 项目内复制、跨项目复制 | 已合并 |
| dss-framework-release-server-webank | 批量发布、自动发布 | 已合并 |

### 10.4 EC资源管理

| 模块 | 功能点 | 状态 |
|-----|-------|------|
| dss-framework-workspace-server-webank | EC配置模板、EC释放策略 | 已合并 |
| dss-framework-compute-resource-manager-webank | Linkis资源管理 | 已合并 |

### 10.5 调度器增强

| 模块 | 功能点 | 状态 |
|-----|-------|------|
| dss-scheduler-appconn-webank | 增强调度操作、编排比较 | 已合并 (commit 5d7f95f6c) |

---

## 11. 合并进度跟踪表

### 11.1 已完成合并

| 序号 | 模块 | 提交 | 状态 | 备注 |
|-----|------|------|------|------|
| 1 | dss-apiservice-server-webank | 19e2155ce | ✓ | 103个文件 |
| 2 | dss-datapipe-server-webank | 19e2155ce | ✓ | 66个文件 |
| 3 | dss-scriptis-server-webank | 19e2155ce | ✓ | 10个文件 |
| 4 | dss-user-guide-server-webank | 19e2155ce | ✓ | 2个文件 |
| 5 | dss-common-server-webank | 19e2155ce | ✓ | 30个文件 |
| 6 | dss-sender-service-webank | 19e2155ce | ✓ | 4个文件 |
| 7 | dss-detectionservice | 19e2155ce | ✓ | 15个文件 |
| 8 | dss-error-code | 19e2155ce | ✓ | 22个文件 |
| 9 | dss-framework-compute-resource-manager-webank | 19e2155ce | ✓ | 18个文件 |
| 10 | dss-framework-orchestrator-server-webank | 19e2155ce | ✓ | 22个文件 |
| 11 | dss-framework-project-server-webank | 19e2155ce | ✓ | 67个文件 |
| 12 | dss-framework-release-server-webank | 19e2155ce | ✓ | 49个文件 |
| 13 | dss-framework-workspace-server-webank | 19e2155ce | ✓ | 118个文件 |
| 14 | dss-framework-migrate-server-webank | 19e2155ce | ✓ | 2个文件 |
| 15 | dws-migrate-to-dss | 19e2155ce | ✓ | 25个文件 |
| 16 | dss-orchestrator-common-webank | 19e2155ce | ✓ | 34个文件 |
| 17 | dss-orchestrator-db-webank | 19e2155ce | ✓ | 4个文件 |
| 18 | dss-workflow-server-webank | 19e2155ce | ✓ | 22个文件 |
| 19 | dss-flow-execution-server-webank | 19e2155ce | ✓ | 6个文件 |
| 20 | dss-scheduler-appconn-webank | 5d7f95f6c | ✓ | 22个文件 |

### 11.2 待合并模块清单

| 序号 | 模块路径 | 优先级 | 预估文件数 |
|-----|-----------|--------|-----------|
| 1 | dss-appconn/appconns/dss-datachecker-appconn-webank | 高 | ~20 |
| 2 | dss-appconn/appconns/dss-eventchecker-appconn-webank | 高 | ~15 |
| 3 | dss-appconn/appconns/dss-orchestrator-framework-appconn-webank | 中 | ~30 |
| 4 | dss-appconn/appconns/dss-schedulis-appconn-webank | 中 | ~25 |
| 5 | dss-appconn/appconns/dss-scriptis-appconn-webank | 中 | ~20 |
| 6 | dss-appconn/appconns/dss-sendemail-appconn-webank | 高 | ~40 |
| 7 | dss-appconn/appconns/dss-sso-appconn-webank | 低 | ~10 |
| 8 | dss-appconn/appconns/dss-workflow-appconn-webank | 高 | ~30 |
| 9 | dss-apps/dss-apps-server-webank | 中 | ~50 |
| 10 | dss-server-webank | 高 | ~80 |
| 11 | plugins/dss-bes | 低 | ~15 |

---

## 12. 编译验证步骤

### 12.1 编译命令

```bash
# 清理之前编译产物
mvn clean

# 跳过测试编译
mvn compile -DskipTests

# 打包验证
mvn package -DskipTests
```

### 12.2 分模块编译

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

# 编译appconn模块
mvn compile -DskipTests -pl dss-appconn/dss-scheduler-appconn-webank
```

---

## 13. 风险评估

### 13.1 高风险项

| 风险项 | 描述 | 影响 | 缓解措施 |
|-------|------|------|---------|
| 依赖版本冲突 | spring-boot、beanutils等版本不一致 | 可能导致运行时异常 | 统一版本号，充分测试 |
| 类包冲突 | 相同全限定名的类存在差异 | 编译失败或运行时错误 | 代码审查，必要时重命名 |
| 配置覆盖 | webank配置与原配置冲突 | 配置不生效 | 增加配置分层和命名 |
| 数据库表冲突 | 表结构不一致 | 部署失败 | 提前做版本号比较和迁移脚本 |

### 13.2 中风险项

| 风险项 | 描述 | 影响 | 缓解措施 |
|-------|------|------|---------|
| 功能回归 | 合并后原功能异常 | 业务中断 | 完整回归测试 |
| 性能下降 | 新模块增加开销 | 用户体验下降 | 性能基准测试 |
| 环境兼容性 | BES等新增硬件依赖 | 部署范围受限 | 提前兼容性测试 |

---

## 14. 验收标准

### 14.1 编译验收

| 检查项 | 要求 | 验证方法 |
|-------|------|---------|
| Maven编译 | 无错误，无警告 | `mvn clean compile` |
| 依赖冲突 | 无依赖冲突 | `mvn dependency:analyze` |
| 打包成功 | 产出正确的JAR/WAR包 | `mvn package -DskipTests` |

### 14.2 测试验收

| 检查项 | 要求 | 验证方法 |
|-------|------|---------|
| 单元测试通过率 | >95% | `mvn test` |
| 集成测试通过率 | 100% | 集成环境验证 |
| 功能验证 | 所有新功能正常工作 | 功能测试清单 |

---

## 15. 附录

### 15.1 参考

- DSS官方文档：https://wedatasphere.github.io/dss-web/#/zh-CN/
- 历史合并commit 19e2155ce分析文档
- 历史合并commit 5d7f95f6c：调度器AppConn增强

### 15.2 命令速查表

```bash
# 常用Maven命令
mvn clean                    # 清理构建产物
mvn compile                  # 编译
mvn test                     # 运行测试
mvn package                  # 打包
mvn dependency:tree          # 查看依赖树
mvn dependency:analyze       # 分析依赖

# Git常用命令
git checkout -b <branch>     # 创建新分支
git status                   # 查看状态
git log --oneline            # 查看提交历史
git show <commit-id>         # 查看提交详情
```

### 15.3 关键技术栈

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
| 1.1 | 2026-02-02 | Claude Code | 更新合并记录、添加commit 5d7f95f6c分析 |