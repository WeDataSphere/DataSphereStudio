# 依赖包升级（netty-all / c3p0 / jackson-databind）设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.26.0-OPT-001 |
| 关联需求 | 无（安全合规驱动的依赖升级） |
| 设计类型 | 依赖升级（OPT） |
| 优先级 | P1 |
| 版本 | dev-1.26.0 |
| 所属模块 | 根 pom.xml（全局依赖管理）；dss-orchestrator/dss-workflow/dss-flow-execution-server；同步仓库 assembly/dss-package |
| 创建日期 | 2026-11-06 |

---

## 一、升级概述

### 1.1 背景与动机

DataSphere Studio (DSS) 1.26.0 迭代期间完成三项第三方依赖升级，均由**安全漏洞治理**驱动，辅以缺陷修复收益：

| 依赖 | 坐标 | 升级前 | 升级后 | 核心动机 |
|------|------|--------|--------|----------|
| netty-all | io.netty:netty-all | 4.2.5.Final | **4.2.13.Final** | 修复 2026-05 批次披露的 netty CVE（epoll DoS、DNS codec、HTTP 信息泄露等） |
| c3p0 | com.mchange:c3p0 | 0.9.5.5（传递依赖，推断） | **0.14.0**（显式钉住） | 修复 CVE-2026-55223（DataSource 反序列化 sink，可组合 RCE） |
| jackson-databind | com.fasterxml.jackson.core:jackson-databind | 2.14.1 | **2.18.12** | 修复 CVE-2025-52999（深度嵌套 DoS，影响 < 2.15.0）及 2.18.12 批次安全修复（含 CVE-2026-110879 PTV 绕过） |

三项升级的共同策略：**排除不可控的传递依赖版本，在受控位置显式钉住安全版本**，使最终打包产物的依赖清单满足安全扫描基线。

### 1.2 变更清单与提交记录

| # | 提交 | 仓库 | 日期 | 变更文件 |
|---|------|------|------|----------|
| 1 | `900b550fd` 升级jackson-databind包 | 主仓库 | 2026-09-29 | pom.xml（根） |
| 2 | `b5e1fac60` upgrade cp30 jar version to 0.14.0 | 主仓库 | 2026-11-05 | dss-flow-execution-server/pom.xml |
| 3 | `a6edbeddd` upgrade jackson-databind jar version to 2.18.12 | 主仓库 | 2026-11-05 | pom.xml（根） |
| 4 | `1bbbf7397` upgrade netty-all jar version to 4.2.13.Final | 同步仓库 | 2026-11-05 | assembly/dss-package/pom.xml |
| 5 | `0bd9d5125` upgrade jackson-databind jar version to 2.18.12（提交信息 "2.18.2" 为笔误，实际 diff 为 2.18.11→2.18.12） | 同步仓库 | 2026-11-05 | pom.xml（根） |

> 仓库约定：主仓库 = `dataspherestudio-wedatasphere-sync`（分支 dev-1.26.0）；同步仓库 = `dss-sync-demo/wedatasphere-dataspherestudio`。

### 1.3 升级收益

1. **netty-all 4.2.5 → 4.2.13.Final**：覆盖 4.2.6~4.2.13 的累计缺陷修复与安全修复，重点是 2026-05-04 发布说明中"fixes numerous security issues"对应的 CVE 批次：CVE-2026-42577（epoll 半关闭后 RST 检测失效，连接僵死 + CPU 空转 DoS，影响 4.2.0~4.2.12）、CVE-2026-42579（DNS codec 未强制 RFC 1035 域名约束）、CVE-2026-42584（HTTP 客户端 1xx 信息响应处理信息泄露）、CVE-2026-42582/42586 等。netty 官方强烈建议升级到该版本。
2. **c3p0 → 0.14.0**：修复 CVE-2026-55223（CVSS ≈ 6.3，中危）——c3p0 通过 JDBC DataSource 的 JavaBean 属性查找机制构成反序列化 gadget sink，与 classpath 上其他 gadget 库组合可实现远程代码执行；官方修复版本即 0.14.0。同时消除了 0.9.5.x 线上的历史 XXE 风险（CVE-2019-5427 等）。
3. **jackson-databind 2.14.1 → 2.18.12**：
   - 修复 CVE-2025-52999（高危，GHSA-h46c-h94j-95f3）：jackson-core 解析深度嵌套 JSON 时无限制递归导致 StackOverflowError，8KB 文档即可构造 4000 层嵌套触发 DoS，影响所有 < 2.15.0 版本；2.15.0 起提供 `StreamReadConstraints.maxNestingDepth()`（默认 1000 层）。
   - 2.18.12（2026-10-17 发布）为安全补丁版本，含 7 项 CVE 修复，其中 CVE-2026-110879 为多态反序列化 PolymorphicTypeValidator（PTV）绕过（影响 [2.10.0, 2.18.12) 区间，可导致 RCE）；另有 CVE-2026-104015/104016 等。
   - 附带收益：2.15~2.18 的功能性改进（更完善的处理约束体系、废弃 API 清理）。

---

## 二、各包变更内容详解

### 2.1 jackson-databind（全局依赖管理，影响面最大）

**变更机制**：根 `pom.xml` 属性 `fasterxml.jackson.version`（pom.xml:50）在 `dependencyManagement` 中管理 `com.fasterxml.jackson.core:jackson-databind`（pom.xml:107-111）：

```xml
<!-- 根 pom.xml:50 -->
<fasterxml.jackson.version>2.18.12</fasterxml.jackson.version>

<!-- 根 pom.xml:107-111（dependencyManagement） -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${fasterxml.jackson.version}</version>
</dependency>
```

升级路径为两段式：2.14.1 → 2.18.11（提交 `900b550fd`，2026-09-29）→ 2.18.12（提交 `a6edbeddd`，2026-11-05，跟进 10 月中旬披露的 CVE-2026-110879 批次）。

**版本管理现状（已核实）**：
- 显式引用 `${fasterxml.jackson.version}` 的位置：根 pom dependencyManagement（jackson-databind）；`assembly/dss-package/pom.xml:125-127`（最终打包，jackson-databind）；`dss-appconn/appconns/dss-dolphinscheduler-appconn/pom.xml:52-60`（jackson-core + jackson-databind）。
- **存量覆盖点（本次未同步升级）**：`dss-appconn/dss-appconn-core/pom.xml:32` 自行声明 `<fasterxml.jackson.version>2.14.1</fasterxml.jackson.version>`，其 jackson-databind/jackson-annotations（pom.xml:68-77，provided scope）仍为 2.14.1。属性重名遮蔽父 pom，构成遗留漏洞版本残留点。
- **版本错位风险（已核实）**：根 pom dependencyManagement 只管理了 jackson-databind，未管理 jackson-core / jackson-annotations。本地 .m2 中 jackson-core 与 jackson-databind 并存 2.13.4(.2)/2.14.0/2.14.1/2.15.0 多版本，最终由 nearest-wins/最先声明原则裁决。databind 2.18.12 与旧版 core（如 2.14.1）在运行时混装存在 `NoSuchMethodError`/`StreamReadConstraints` 行为不一致的风险。jackson 官方要求 core/annotations/databind 三件套同版本。
- Scala 侧：全仓库无 `jackson-module-scala` 依赖（仅 dss-framework-workspace-server 的 distribution.xml:60 存在一条被注释掉的排除项），Scala JSON 走 json4s（`json4s-jackson_2.11`，各 appconn 中均为 exclusion），不受本次升级影响——**推断**：json4s 由 Linkis 侧传递引入并被主动排除，appconn 运行时使用宿主进程的 jackson。
- Spring Boot 2.7.18 基线 jackson 为 2.13.x：Spring MVC 的 `HttpMessageConverter`（`Jackson2ObjectMapperBuilder`）对 jackson 2.18 兼容（jackson 2.x 保持 API 向后兼容，JDK 8 基线不变）。

### 2.2 c3p0（dss-flow-execution-server 模块级钉版本）

**变更机制**：`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/pom.xml` 中，给 `org.apache.linkis:linkis-jobhistory:${linkis.version}` 依赖新增 c3p0 排除项（pom.xml:41-44），并新增显式依赖 `com.mchange:c3p0:0.14.0`（pom.xml:54-58）：

```xml
<!-- linkis-jobhistory 依赖（pom.xml:29-46），排除项新增：
     com.mchange:c3p0（pom.xml:41-44），与既有 mchange-commons-java 排除（pom.xml:37-40）并列 -->
<dependency>
    <artifactId>c3p0</artifactId>
    <groupId>com.mchange</groupId>
    <version>0.14.0</version>
</dependency>
```

完整 diff（提交 `b5e1fac60`）：

```diff
@@ -38,6 +38,10 @@（linkis-jobhistory exclusions）
+                <exclusion>
+                    <artifactId>c3p0</artifactId>
+                    <groupId>com.mchange</groupId>
+                </exclusion>
@@ -47,6 +51,12 @@
+        <dependency>
+            <artifactId>c3p0</artifactId>
+            <groupId>com.mchange</groupId>
+            <version>0.14.0</version>
+        </dependency>
```

**关键事实（已核实）**：
- 该处理完全复刻既有模式：linkis-jobhistory 此前已排除 `mchange-commons-java` 并钉住 0.4.0（pom.xml:47-51），本次 c3p0 沿用同一"排除 + 钉版本"套路。
- DSS 源码中**无任何** `import com.mchange` / c3p0 直接使用；c3p0 是 linkis-jobhistory（其数据库访问链路连接池）的传递依赖，随 dss-flow-execution-server 打包进运行时 classpath。
- 本机 .m2 `com/mchange/c3p0` 下仅有 0.9.5.5 → 升级前传递版本**大概率**为 0.9.5.x（推断，依赖树需在 Linux 构建机核实）。
- **版本来源（推断，需构建环境验证）**：c3p0 0.14.0 与 mchange-commons-java 0.4.0 一样，公开 Maven Central 上不存在对应序列（公开线为 0.9.5.5 及 0.10.0-pre 系列），应为内部 Maven 镜像的安全补丁重打包版本；CVE-2026-55223 的官方修复版本恰为 0.14.0，版本号与公开修复线对齐。
- 同步仓库未包含此变更（同步仓库 flow-execution-server pom 无 c3p0 版本声明）。

### 2.3 netty-all（同步仓库打包层钉版本）

**变更机制**：同步仓库 `assembly/dss-package/pom.xml`（最终打包模块）中 netty-all 由 4.2.5.Final 升至 4.2.13.Final（pom.xml:251-255）：

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.2.13.Final</version>
</dependency>
```

完整 diff（提交 `1bbbf7397`）：仅此一行版本号变更（4.2.5.Final → 4.2.13.Final）。

**关键事实（已核实）**：
- 版本控制策略为"**模块内排除 + 打包层统一**"：多个 server 模块对 netty-all 做 exclusion（dss-server/pom.xml:46-49、67-70；dss-framework/dss-framework-orchestrator-server/pom.xml:132-134、214-216；dss-framework/dss-framework-project-server-webank/pom.xml:159-161），由 assembly/dss-package 在打包时统一提供 netty 版本，避免各传递链路引入多版本 netty。
- DSS 源码中**无任何** `import io.netty` 直接使用；netty 由 Linkis（RPC 通信链路，linkis-common 等）传递使用，netty-all 钉版本影响所有最终分发包内的 Linkis 通信层。
- **重要差异（必须在发布前对齐）**：主仓库 `assembly/dss-package/pom.xml` 中**没有** netty-all 直接依赖（仅有 `netty` 排除项，pom.xml:115 附近），netty-all 4.2.13.Final 钉版本目前只存在于同步仓库。

---

## 三、影响范围分析

### 3.1 jackson-databind（全局）

| 维度 | 影响面 |
|------|--------|
| 受影响进程 | 全部 DSS server（dss-server 聚合的各服务：framework-project/orchestrator/apps/data-api/data-governance/guide、workflow-server、flow-execution-server 等）及所有 appconn 装载进程 |
| 功能链路 | REST API JSON 序列化/反序列化（Spring MVC）、与 Linkis 的 RPC/HTTP 消息序列化、工作流上下文 JSON、第三方 AppConn 调用报文 |
| 不受影响 | Scala 侧 json4s 序列化（无 jackson-module-scala 依赖）；fastjson/gson 链路（各自独立版本） |

### 3.2 c3p0（dss-flow-execution-server 单进程）

| 维度 | 影响面 |
|------|--------|
| 受影响进程 | 仅 dss-flow-execution-server（工作流执行引擎） |
| 功能链路 | linkis-jobhistory 组件在该进程内的数据库连接池（job 历史数据读写）；DSS 自身业务库连接使用 druid（根 pom druid.version=1.2.4，pom.xml:65），不走 c3p0 |
| 不受影响 | 其他 server（不携带 linkis-jobhistory + c3p0 组合）；调度执行链路本身 |

### 3.3 netty-all（同步仓库打包层）

| 维度 | 影响面 |
|------|--------|
| 受影响进程 | 同步仓库最终分发包内的全部 server 进程（Linkis RPC 通信层） |
| 功能链路 | DSS ↔ Linkis（Entrance/JobHistory/RPC）通信、服务间 RPC 序列化传输 |
| 不受影响 | 主仓库构建产物（主仓库未同步此变更，见 6.1） |

---

## 四、兼容性分析

| 项 | 结论 | 依据 |
|----|------|------|
| netty 4.2.x ↔ JDK 8 | **兼容** | netty 4.2.0.Final 官方公告：唯一新要求是 JDK 8+（4.1.x 基线为 JDK 6）；本项目 jdk.compile.version=1.8（根 pom.xml:47）。io_uring 传输需 JDK 9+，DSS 未使用 |
| netty 4.2.5 → 4.2.13 | 同 4.2.x 次版本线内升级，API 兼容 | 4.2.13.Final 为缺陷+安全修复版本；官方公告提示新增校验/资源限制可能使极端 workload 被拒，DSS 的 RPC 用量远低于阈值（推断） |
| jackson 2.18.x ↔ JDK 8 | **兼容** | jackson 2.x 全系保持 JDK 8 基线 |
| jackson 2.18 ↔ Spring Boot 2.7.18（基线 2.13.x） | **兼容**（需验证） | jackson 2.x API 向后兼容；`Jackson2ObjectMapperBuilder` 在 2.18 下正常工作。需在验证阶段覆盖 REST 序列化回归 |
| jackson 2.14 → 2.18 行为变化 | 低风险（需回归） | 2.15 引入 StreamReadConstraints 默认约束（嵌套深度 1000、文档长度等），超深嵌套的历史报文（若有）将被拒——DSS 报文为常规业务 JSON，预计无影响（推断） |
| jackson-core 版本错位 | **中风险** | 根 pom 只管理 databind；core/annotations 由传递链裁决（本地 .m2 存在 2.13.4/2.14.x/2.15.0 混装）。databind 2.18.12 + 旧 core 混装可能触发运行时错误，验证阶段必须核对最终 lib 目录（见 R-02） |
| c3p0 0.9.5.x → 0.14.0 | 低风险（需验证） | c3p0 对外 API（ComboPooledDataSource/配置项）稳定；c3p0 是 linkis-jobhistory 内部使用，DSS 不直接编码调用。0.14.0 来自内部镜像（推断），配置项兼容性需在验证阶段确认 |
| c3p0 0.14.0 ↔ mchange-commons-java 0.4.0 | 低风险 | c3p0 运行时依赖 mchange-commons-java，pom 中已同时钉住 0.4.0（pom.xml:47-51），组合与镜像发布配套（推断） |

---

## 五、风险分析与缓解措施

| 编号 | 风险 | 等级 | 缓解措施 |
|------|------|:----:|----------|
| R-01 | dss-appconn-core 子模块属性遮蔽（pom.xml:32 自声明 2.14.1），appconn 进程内仍残留带 CVE 的 jackson 2.14.1 | 高 | 后续迭代移除该子模块属性覆盖，统一走根 pom dependencyManagement；本迭代先在验证阶段扫描各 server lib 目录确认实际加载版本 |
| R-02 | jackson-core/annotations 未纳入 dependencyManagement，与 databind 2.18.12 版本错位 → 运行时 NoSuchMethodError / 约束行为不一致 | 中 | 验证阶段检查打包产物 lib 目录三件套版本；建议后续在根 pom 补齐 jackson-bom 导入或逐个管理 core/annotations |
| R-03 | c3p0 0.14.0 / mchange-commons-java 0.4.0 为内部镜像版本（推断），公网/离线环境无法解析，构建机依赖内部 Nexus | 中 | 在 Linux 构建机执行 `mvn dependency:tree -pl dss-flow-execution-server` 验证解析；发布文档标注内部仓库依赖 |
| R-04 | netty 4.2.13 安全修复新增校验/资源限制，极端报文可能被拒绝；且 4.2.13 之后又披露 HTTP 请求走私类问题（4.2.15+ 修复） | 低 | RPC 回归覆盖大报文/长连接场景；跟踪 netty 后续版本，评估升级至 4.2.15+（2026-11-02 已发布 4.2.20.Final） |
| R-05 | jackson 2.15+ 默认嵌套深度限制 1000，若存在超深嵌套历史数据将被拒 | 低 | 回归覆盖工作流上下文/节点大 JSON 读写场景；必要时配置 StreamReadConstraints 调大 |
| R-06 | 主仓库与同步仓库变更不同步（见 6.1），双线构建产物安全基线不一致 | 中 | 按同步状态清单逐项对齐后发布 |

---

## 六、验证方案与回滚

### 6.1 同步状态（发布前必须对齐）

| 变更 | 主仓库 | 同步仓库 | 动作 |
|------|:------:|:--------:|------|
| jackson-databind 2.18.12（根 pom 属性） | ✅ a6edbeddd | ✅ 0bd9d5125 | 已一致，无动作 |
| c3p0 0.14.0（flow-execution-server） | ✅ b5e1fac60 | ❌ 无此变更 | 同步仓库补齐 c3p0 排除+钉版本 |
| netty-all 4.2.13.Final（assembly/dss-package） | ❌ 主仓库 dss-package 无 netty-all 直接依赖 | ✅ 1bbbf7397 | 确认主仓库打包链路是否需要同步 netty-all 钉版本；若需要，按同步仓库方式在 dss-package 增加 |
| （遗留）dss-appconn-core jackson 2.14.1 覆盖 | ❌ 未升级 | — | 纳入后续迭代 |

### 6.2 验证步骤

1. **编译验证**（Linux 构建机）：
   - `mvn clean package -DskipTests`（全量，验证 R-03 内部依赖解析）
   - `mvn dependency:tree -pl dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server` 确认 c3p0=0.14.0、mchange-commons-java=0.4.0，无 0.9.5.x 残留
2. **打包产物依赖审计**：解包最终分发包，检查 lib 目录：jackson-databind 2.18.12 与 jackson-core/annotations 版本对齐（R-02）；netty-all 4.2.13.Final（同步仓库包）；无 c3p0 0.9.5.5
3. **启动验证**：dss-flow-execution-server 启动无 ClassNotFound/NoSuchMethod 错误；jobhistory 初始化正常（c3p0 连接池创建成功）
4. **功能回归**：
   - REST 接口序列化：登录、工作流 CRUD、orchestrator 列表（jackson）
   - 工作流端到端执行：提交 → 轮询状态 → 完成（flow-execution-server + Linkis RPC/netty + jobhistory 落库/c3p0）
   - 大 JSON 场景：超长节点上下文读写（R-05）
5. **安全扫描**：对打包产物执行依赖漏洞扫描，确认三项 CVE（CVE-2025-52999、CVE-2026-110879、CVE-2026-55223）及 netty 2026-05 批次不再命中

### 6.3 回滚方案

三项变更均为 pom 单点版本号/排除项变更，无代码改动：
- **jackson**：根 pom.xml:50 属性回退至 2.14.1（或中间态 2.18.11）
- **c3p0**：移除 flow-execution-server pom.xml:41-44 排除项与 pom.xml:54-58 显式依赖，恢复传递解析
- **netty-all**：assembly/dss-package/pom.xml:254 版本回退至 4.2.5.Final
回滚后重新打包即可，无数据/配置迁移。建议回滚决策以验证阶段 6.2-3/6.2-4 的失败现象为准。

---

## 七、参考资料

- netty 4.2.0 发布公告（JDK 8 基线）：https://netty.io/news/2025/04/03/4-2-0.html
- netty 4.2 迁移指南：https://netty.io/wiki/netty-4.2-migration-guide.html
- netty 4.2.13.Final 发布公告：https://netty.io/news/2026/05/04/4-2-13-Final.html
- CVE-2026-42577（epoll DoS，GHSA-rwm7-x88c-3g2p）：https://github.com/advisories/GHSA-rwm7-x88c-3g2p
- CVE-2026-55223（c3p0 反序列化 sink，GHSA-w6w4-rjh9-9r58，修复于 0.14.0）：https://github.com/swaldman/c3p0/security/advisories/GHSA-w6w4-rjh9-9r58
- CVE-2025-52999（jackson-core 深度嵌套 DoS，GHSA-h46c-h94j-95f3）：https://nvd.nist.gov/vuln/detail/CVE-2025-52999
- Jackson Release 2.18.12（含 CVE-2026-110879 等 7 项 CVE 修复）：https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.18.12
- Jackson 2.15 发布说明（StreamReadConstraints）：https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.15
