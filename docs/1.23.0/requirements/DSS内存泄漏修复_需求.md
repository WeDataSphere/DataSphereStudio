# DSS内存泄漏修复 需求文档

| 属性 | 值 |
|------|-----|
| 需求编号 | REQ-DSS-1.23.0-FIX-003 |
| 需求名称 | DSS内存泄漏修复 |
| 需求类型 | Bug修复（FIX） |
| 优先级 | P1 |
| 状态 | 待设计 |
| 版本 | dev-1.23.0 |
| 所属模块 | dss-common-server-webank、dss-standard-common、dss-appconn-loader（ViewFileSystem/Configuration 问题涉及 Hadoop/Linkis 客户端配置，归属边界详见 1.2 与 3.4） |
| 创建日期 | 2026-08-04 |

---

## 一、需求背景

### 1.1 业务场景

DSS 作为 WeBank 内部数据应用开发门户，其 `dss-server` 与 `dss-apps-server` 均为**长期运行的 Spring Boot 服务进程**，承载工作流编排、AppConn 热部署、员工信息查询、HDFS 访问等常驻型功能。这些服务一旦上线，通常以周/月为单位持续运行，期间会反复触发以下两类后台动作：

| 后台动作 | 触发频率 | 涉及代码 | 资源特征 |
|---------|---------|---------|---------|
| ESB 员工信息全量拉取 | 每小时 1 次（`Utils.defaultScheduler().scheduleAtFixedRate`，`HttpStaffInfoGetter.java:125-127`） | `HttpStaffInfoGetter.StaffThread.run()` | 向 `STAFF_INFO_MAP` 持续写入 StaffInfo 对象 |
| AppConn 加载/卸载/热部署 | 每次执行 `appconn-install.sh` / `appconn-refresh.sh` 时触发 | `CommonAppConnLoader.getAppConn()`（`CommonAppConnLoader.java:47-101`） | 创建 `AppConnClassLoader`（继承 `URLClassLoader`），打开 jar 文件句柄 |

长期运行场景下，若上述动作产生的对象/句柄无法被回收，堆内存将**单调增长**，最终触发频繁 Full GC 甚至 OOM，导致服务响应变慢、接口超时、进程被重启。本次需求即针对线上 MAT（Memory Analyzer Tool）分析发现的多处内存泄漏进行修复。

### 1.2 问题来源

问题来源于对三个环境堆内存 dump 的 MAT Leak Suspects 分析，原始截图与分析报告位于 `dss-image/memory-leak-analysis.md`，修复方案位于 `dss-image/memory-leak-solutions.md`。

| 环境 | 截图文件 | 总堆内存 | 关键发现 |
|------|---------|:-------:|---------|
| dss-apps-server | dss-apps-server.jpg | 82.2 MB | `HttpStaffInfoGetter` 的 `ConcurrentHashMap$Node[]` 占 23,939,272 bytes（27.79%） |
| dss-server-dev | dss-server-dev.jpg | - | `ViewFileSystem` 4 实例占 158,190,400 bytes（38.68%）；`Configuration` 39 实例占 56,703,128 bytes（13.86%）；`HttpStaffInfoGetter` 占 23,939,288 bytes（5.85%）；`AppStandardClassUtils` 占 7,755,312 bytes（1.90%） |
| dss-server-prod | dss-serverprod.jpg | - | `AppStandardClassUtils` 占 7,756,480 bytes（8.68%）；`URLJarFile` 568 实例占 9,190,544 bytes（10.29%） |

> **归属边界说明**：`ViewFileSystem` / `Configuration` 对象通常由 Linkis / Hadoop 客户端在任务提交、HDFS 访问时创建，**并非 DSS 代码直接 `new`**。其根因部分落在 DSS 代码边界之外，修复需结合 Hadoop 配置项（如 `fs.viewfs.impl.disable.cache=true`）并与 Linkis 侧协调，本文档在 3.4 节如实标注此边界与不确定性，不夸大 DSS 可直接修复的范围。

### 1.3 当前问题对业务的影响

| 影响维度 | 具体表现 |
|---------|---------|
| 服务稳定性 | 堆内存单调增长，长期运行后触发频繁 Full GC，STW 时间拉长，接口响应变慢甚至超时 |
| OOM 风险 | dev 环境 `ViewFileSystem` + `Configuration` 合计占用超 50% 堆内存，逼近 OOM 阈值；apps-server `HttpStaffInfoGetter` 占 27.79%，随员工数据增长持续放大 |
| 运维成本 | 内存泄漏导致需频繁重启服务进程以回收内存，打断长周期任务，增加夜间值班负担 |
| 文件句柄耗尽 | prod 环境 568 个 `URLJarFile` 句柄累积，长期运行可能触发 `Too many open files`，导致 AppConn 加载失败、HDFS 读写异常 |
| 功能连带 | AppConn 热部署/刷新失败会直接影响工作流编排与第三方系统（Schedulis、Visualis、Qualitis 等）集成 |

### 1.4 问题等级判定

| 维度 | 评估 |
|-----|------|
| 影响范围 | 三个环境（apps-server、dev、prod）均出现，覆盖所有长期运行的 DSS 服务 |
| 紧急程度 | P1（内存泄漏为慢性问题，短期不阻塞核心流程，但长期运行必发 Full GC/OOM） |
| 数据风险 | 不产生脏数据，不丢数据；但服务重启可能中断进行中的工作流任务 |
| 修复优先级 | HttpStaffInfoGetter 与 AppStandardClassUtils 为 P0（DSS 代码可直接修复，收益明确）；ViewFileSystem/Configuration 与 URLJarFile 为 P1（部分依赖 Linkis/Hadoop 配置协调） |

---

## 二、问题现象与复现路径

### 2.1 问题现象

| 维度 | 内容 |
|-----|------|
| **问题概述** | DSS 长期运行服务（dss-server、dss-apps-server）堆内存与文件句柄单调增长，MAT 分析定位 4 类泄漏对象：`HttpStaffInfoGetter` 的 ConcurrentHashMap、`AppStandardClassUtils` 的 ClassLoader 缓存、`URLJarFile` 句柄、Hadoop `ViewFileSystem`/`Configuration` |
| **紧急程度** | P1 |
| **影响范围** | 所有长期运行的 DSS 服务进程；员工信息查询、AppConn 热部署、HDFS 访问路径均受影响 |
| **问题模块** | dss-common-server-webank、dss-standard-common、dss-appconn-loader（ViewFileSystem/Configuration 涉及 Linkis/Hadoop 客户端） |
| **复现概率** | 必现（服务运行足够长时间 + 定时刷新员工信息 / 反复加载卸载 AppConn） |
| **发现环境** | 生产环境（prod）、开发环境（dev）、apps-server 三环境均复现 |

### 2.2 复现步骤

**复现路径一：员工信息定时刷新导致内存增长（对应问题 1）**

| 步骤 | 操作 | 预期结果 | 实际结果 |
|:----:|------|---------|---------|
| 1 | 启动 dss-apps-server，记录初始堆内存与 `HttpStaffInfoGetter` 持有的 `STAFF_INFO_MAP` 大小 | - | - |
| 2 | 等待 `StaffThread` 定时任务执行（每小时 1 次，`HttpStaffInfoGetter.java:125-127`），或手动触发 ESB 全量拉取 | Map 大小应与 ESB 当前全量员工数一致，不随刷新次数增长 | ❌ 若 ESB 返回数据存在 key 漂移（大小写不一致、离职人员未移除），Map 单调增长，旧 entry 永不清理 |
| 3 | 连续运行 24 小时（24 次刷新），dump 堆内存并用 MAT 分析 | `HttpStaffInfoGetter` 占用稳定 | ❌ apps-server 环境 `HttpStaffInfoGetter` 的 `ConcurrentHashMap$Node[]` 占 23,939,272 bytes（27.79%） |

**复现路径二：AppConn 反复加载/卸载导致 jar 句柄累积（对应问题 2、3）**

| 步骤 | 操作 | 预期结果 | 实际结果 |
|:----:|------|---------|---------|
| 1 | 启动 dss-server，记录初始 jar 文件句柄数（`lsof -p <pid> \| grep jar \| wc -l`）与 `AppStandardClassUtils` 持有的 ClassLoader 数 | - | - |
| 2 | 反复执行 AppConn 加载/卸载（如 `appconn-install.sh` + `appconn-refresh.sh`）10 次以上，每次调用 `CommonAppConnLoader.getAppConn()`（`CommonAppConnLoader.java:47`）-> `AppStandardClassUtils.refreshClassloader()`（`AppStandardClassUtils.java:39`） | 旧 ClassLoader 被回收，jar 句柄数稳定，`AppStandardClassUtils` 内存占用稳定 | ❌ 旧 ClassLoader 仅被 `remove()`（`AppStandardClassUtils.java:43`）未 `close()`，jar 句柄持续累积 |
| 3 | dump 堆内存并用 MAT 分析 | `URLJarFile` 实例数与 `AppStandardClassUtils` 占用稳定 | ❌ prod 环境 568 个 `URLJarFile` 占 9,190,544 bytes（10.29%）；`AppStandardClassUtils` 占 7,756,480 bytes（8.68%） |

### 2.3 内存增长调用链/对象引用链

> 以下为**修复前**的对象引用链，用于根因说明。行号对应修复前代码位置（修复后行号可能变化，详见附录 10.1 索引表）。

**引用链一：HttpStaffInfoGetter（问题 1）**

```
Utils.defaultScheduler() (定时调度, HttpStaffInfoGetter.java:125-127)
  └──> StaffThread.run()  (HttpStaffInfoGetter.java:87-122)
        ├── HttpClient.execute(httpGet)  (line 94)         从 ESB 全量拉取员工 JSON
        ├── jsonArray.forEach(node -> ...)  (line 100)       遍历每条员工数据
        │     └── STAFF_INFO_MAP.put(staffInfo.getEnglishName(), staffInfo)  (line 113)  ❌ 直接向同一 Map 追加，从不清理
        └── 注：LOCK 字段 (line 39) 已声明但从未使用，无原子替换保护
              ↓
        GC Root: HttpStaffInfoGetter.class (Class 对象)
          └── STAFF_INFO_MAP (static ConcurrentHashMap, line 38)  ← 强引用，永不可达回收
                └── ConcurrentHashMap$Node[]  ← MAT Problem Suspect (apps-server 27.79%, dev 5.85%)
```

**引用链二：AppStandardClassUtils / URLJarFile（问题 2、3）**

```
CommonAppConnLoader.getAppConn()  (CommonAppConnLoader.java:47)
  └── AppStandardClassUtils.refreshClassloader(appConnName, () -> new AppConnClassLoader(...))  (line 57)
        ├── if CLASS_LOADER_MAP.containsKey(appConnName):   (AppStandardClassUtils.java:40)
        │     └── CLASS_LOADER_MAP.remove(appConnName)      (line 43)  ❌ 仅 remove，未 close()
        │           INSTANCES.remove(appConnName)            (line 44)
        └── getClassLoader(appConnName, createClassLoader)  (line 48)
              └── CLASS_LOADER_MAP.put(appConnName, new AppConnClassLoader(jars, parent))  (line 55)
                    └── AppConnClassLoader extends URLClassLoader  (AppConnClassLoader.java:25)
                          └── URLClassLoader 加载 jar 时:
                                ├── sun.net.www.protocol.jar.Handler 缓存 URLJarFile  ← prod Problem Suspect (10.29%, 568 实例)
                                └── Class 元数据 / Reflections 对象  ← dev/prod Problem Suspect (AppStandardClassUtils 1.90%/8.68%)

GC Root: AppStandardClassUtils.class (Class 对象)
  ├── INSTANCES (static ConcurrentHashMap, line 34)        ← AppStandardClassUtils 实例强引用
  └── CLASS_LOADER_MAP (static ConcurrentHashMap, line 35)  ← ClassLoader 强引用
        └── AppConnClassLoader -> URLJarFile[] / Class[]     ← 未 close 则不可回收
```

**引用链三：ViewFileSystem / Configuration（问题 4，归属 Linkis/Hadoop 客户端）**

```
Linkis 任务提交 / HDFS 访问 (非 DSS 代码直接 new)
  └── FileSystem.get(uri, conf)  或  new Configuration()
        ├── ViewFileSystem 实例  (dev: 4 个, 各 ~39.5MB, 合计 158MB / 38.68%)
        │     └── 未调用 close()，或缓存 key 因 conf 不同而失效导致反复创建
        └── Configuration 实例  (dev: 39 个, 合计 56MB / 13.86%)
              └── 每次请求 new Configuration()，未全局复用

注：上述对象多由 Linkis/Hadoop 客户端创建，DSS 代码边界内可能仅涉及部分调用点，需在设计阶段逐点排查
```

### 2.4 影响范围

- **直接影响**：dss-server、dss-apps-server 长期运行后堆内存持续增长，触发 Full GC / OOM 风险
- **间接影响**：
  - AppConn 热部署/刷新失败（jar 句柄耗尽触发 `Too many open files`）
  - 员工信息查询接口（`getStaffInfoByUsername`、`getAllUsers` 等）因 Map 膨胀响应变慢
  - HDFS/ViewFS 相关功能因对象滥用产生内存压力
- **不受影响**：
  - 短期运行或刚重启的服务（泄漏尚未累积）
  - 不调用 ESB 员工接口、不触发 AppConn 刷新的纯前端操作

---

## 三、根因分析

### 3.1 问题 1：HttpStaffInfoGetter 的 STAFF_INFO_MAP 无限增长（5Why）

| 层级 | 问题 | 答案 |
|:----:|------|------|
| Why 1 | 为什么 apps-server/dev 环境 `HttpStaffInfoGetter` 占用 23.9MB+？ | 内部 `STAFF_INFO_MAP`（`HttpStaffInfoGetter.java:38`，`ConcurrentHashMap`）持续堆积 `StaffInfo` 对象 |
| Why 2 | 为什么 Map 持续堆积不回收？ | `StaffThread.run()`（`HttpStaffInfoGetter.java:87-122`）每小时全量拉取 ESB 数据后直接 `STAFF_INFO_MAP.put(...)`（line 113），**从不清理旧数据** |
| Why 3 | 为什么全量刷新不清空旧数据？ | 刷新逻辑直接向同一 Map 追加 put，缺少 `clear()`/整体替换机制；已声明的 `LOCK` 字段（line 39）从未被使用 |
| Why 4 | 为什么没有设计过期/替换策略？ | 初始实现假设 ESB 返回数据的 key（`englishName`）稳定一致，未考虑 key 漂移（大小写不一致、离职人员未移除、数据重复）导致 Map 单调增长 |
| Why 5（根本原因） | 为什么缺少缓存治理设计？ | **全量刷新场景应采用"原子替换"（先写临时 Map 再整体替换）而非"增量 put"，且缺少上限/过期保护**；`LOCK` 字段已预留同步原语但未落地使用 |

### 3.2 问题 2：AppStandardClassUtils 的 ClassLoader 缓存未释放（5Why）

| 层级 | 问题 | 答案 |
|:----:|------|------|
| Why 1 | 为什么 dev/prod 环境 `AppStandardClassUtils` 占 7.75MB？ | `INSTANCES` 与 `CLASS_LOADER_MAP`（`AppStandardClassUtils.java:34-35`）两个静态 `ConcurrentHashMap` 缓存了大量 AppConn 的 Class 元数据与 ClassLoader |
| Why 2 | 为什么缓存不被释放？ | `refreshClassloader`（`AppStandardClassUtils.java:39-49`）在 AppConn 重新加载时只 `remove()` 旧 ClassLoader（line 43），**未调用 `close()`** |
| Why 3 | 为什么不调用 `close()`？ | 实现遗漏了 `URLClassLoader.close()` 的资源释放；`remove()` 仅解除 Map 引用，但底层打开的 jar 文件句柄仍被 JVM 持有 |
| Why 4 | 为什么 jar 句柄持有导致内存增长？ | `URLClassLoader` 加载 jar 时缓存的 `Class` 对象与 `Reflections` 对象（`AppStandardClassUtils.java:83-94`）通过 ClassLoader 强引用驻留，无法被 GC |
| Why 5（根本原因） | 为什么 ClassLoader 生命周期管理缺失？ | **刷新/卸载时未闭环释放底层资源（jar 文件句柄）**，导致 Class 元数据与反射对象永久驻留；缺少显式卸载接口 |

### 3.3 问题 3：URLJarFile 句柄泄漏（5Why）

| 层级 | 问题 | 答案 |
|:----:|------|------|
| Why 1 | 为什么 prod 环境 568 个 `URLJarFile` 占 9.1MB？ | `URLClassLoader` 加载 jar 时 `sun.net.www.protocol.jar.Handler` 缓存了 `URLJarFile` 实例，被 `HashMap$Node[]` 引用 |
| Why 2 | 为什么 `URLJarFile` 不被释放？ | 对应的 `AppConnClassLoader`（`AppConnClassLoader.java:25`，继承 `URLClassLoader`）未被 `close()` |
| Why 3 | 为什么 `AppConnClassLoader` 不被 close？ | `CommonAppConnLoader.getAppConn()`（`CommonAppConnLoader.java:57`）调用 `refreshClassloader` 时，旧 ClassLoader 被 `remove` 但未 `close`（同问题 2 根因） |
| Why 4 | 为什么反复加载 AppConn 产生大量 ClassLoader？ | AppConn 热部署/刷新（`appconn-refresh.sh`）每次都创建新的 `AppConnClassLoader`，旧实例的资源未释放 |
| Why 5（根本原因） | 为什么资源释放链路断裂？ | **`URLJarFile` 句柄泄漏是问题 2（ClassLoader 未 close）的直接下游表现**，根因同问题 2：`refreshClassloader` 未调用 `URLClassLoader.close()` |

### 3.4 问题 4：Hadoop ViewFileSystem / Configuration 对象滥用（5Why）

| 层级 | 问题 | 答案 |
|:----:|------|------|
| Why 1 | 为什么 dev 环境 `ViewFileSystem`+`Configuration` 合计占超 50% 堆内存？ | 4 个 `ViewFileSystem` 占 158,190,400 bytes（38.68%），39 个 `Configuration` 占 56,703,128 bytes（13.86%） |
| Why 2 | 为什么会有这么多实例？ | 通常由 Linkis/Hadoop 客户端在任务提交、HDFS 访问时创建，每次请求可能新建 `Configuration` 与 `FileSystem` 实例 |
| Why 3 | 为什么不复用？ | Hadoop `FileSystem.get(uri, conf)` 的缓存 key 基于 `URI + conf + user`，若每次传入的 `conf` 不同则缓存失效；`Configuration` 未全局共享 |
| Why 4 | 为什么 conf 每次不同 / FileSystem 未关闭？ | 业务代码或 Linkis 客户端可能每次 `new Configuration()`，且 `ViewFileSystem` 实例使用后未调用 `close()` |
| Why 5（根本原因） | 为什么缺少复用策略？ | **`Configuration`/`FileSystem` 复用策略缺失**；且这些对象多由 Linkis/Hadoop 客户端创建，**非 DSS 代码直接 `new`，根因部分在 DSS 边界之外，需结合配置项（如 `fs.viewfs.impl.disable.cache=true`）与 Linkis 协调** |

> **边界说明**：问题 4 的根因部分落在 Linkis/Hadoop 客户端侧，DSS 可直接修复的范围有限。本文档将其列为 P1，修复方向以"配置项治理 + DSS 侧调用点排查"为主，不承诺完全消除——具体可修复范围需在设计阶段逐点核实后确定。

### 3.5 跨环境共同问题汇总

| 问题对象 | 出现环境 | 占用情况 | 疑似根因 | DSS 可直接修复 |
|---------|---------|---------|---------|:-------------:|
| `HttpStaffInfoGetter`（`STAFF_INFO_MAP`） | apps-server、dev | apps-server 23,939,272 bytes（27.79%）；dev 23,939,288 bytes（5.85%） | 全量刷新不清空旧 Map，缺少原子替换/过期策略 | ✅ 是（P0） |
| `AppStandardClassUtils`（ClassLoader 缓存） | dev、prod | dev 7,755,312 bytes（1.90%）；prod 7,756,480 bytes（8.68%） | `refreshClassloader` 仅 `remove` 未 `close`，jar 句柄与 Class 元数据不释放 | ✅ 是（P0） |
| `URLJarFile`（jar 句柄） | prod | 568 实例，9,190,544 bytes（10.29%） | `URLClassLoader` 未 `close`，`URLJarFile` 累积 | ✅ 是（P1，随问题 2 一并修复） |
| `ViewFileSystem` / `Configuration` | dev | ViewFS 158,190,400 bytes（38.68%）；Configuration 56,703,128 bytes（13.86%） | Hadoop 客户端对象未复用/未关闭，多由 Linkis 创建 | ⚠️ 部分（P1，需 Linkis 协调） |

---

## 四、期望行为

### 4.1 修复后行为总览

| 场景 | 修复前行为 | 期望行为（修复后） |
|-----|----------|------------------|
| ESB 员工信息每小时全量刷新 | `STAFF_INFO_MAP.put(...)` 追加写入，旧数据永不清理，Map 单调增长 | 每次刷新先写入临时 Map，再原子替换（`clear()` + `putAll()`，加 `LOCK` 同步），Map 大小仅保留当前一份全量数据 |
| AppConn 反复加载/卸载 | `refreshClassloader` 仅 `remove` 旧 ClassLoader，jar 句柄与 Class 元数据累积 | `refreshClassloader` 在 `remove` 前 `close()` 旧 `URLClassLoader`，释放 jar 文件句柄 |
| AppConn 热部署（`appconn-refresh.sh`） | 每次刷新产生新 `AppConnClassLoader`，旧实例的 `URLJarFile` 不释放 | 旧 ClassLoader `close` 后 `URLJarFile` 实例可被 GC，jar 句柄数不随刷新次数增长 |
| HDFS/ViewFS 访问（Linkis 侧） | 每次请求可能新建 `Configuration`/`ViewFileSystem`，未复用未关闭 | DSS 侧调用点复用全局 `Configuration`；结合 `fs.viewfs.impl.disable.cache` 等配置与 Linkis 协调（边界外部分如实标注） |
| 员工信息查询接口（`getStaffInfoByUsername` 等） | 可用，但 Map 膨胀后响应变慢 | 行为与返回结构不变，响应速度不再随运行时间劣化 |

### 4.2 向后兼容性要求

- **接口签名不变**：`getStaffInfoByUsername`、`getAllUsers`、`getAllUsernames`、`getFullOrgNameByUsername`、`getAllDepartments` 等员工信息查询接口的签名与返回结构保持不变
- **AppConn 加载流程不变**：`CommonAppConnLoader.getAppConn()` 的调用方式与返回的 `AppConn` 实例行为保持不变
- **刷新时序不变**：`StaffThread` 仍按每小时 1 次的频率执行（`scheduleAtFixedRate`，`HttpStaffInfoGetter.java:125-127`），不调整调度周期
- **配置兼容**：新增的 Hadoop 配置项（如 `fs.viewfs.impl.disable.cache`）应可通过 `conf/dss.properties` 或 Linkis 配置覆盖，不强制修改既有部署
- **行为变更可观测**：`refreshClassloader` 新增 `close()` 调用需输出日志，便于运维确认资源释放

---

## 五、功能性需求

### 5.1 核心功能 P0

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P0-01 | STAFF_INFO_MAP 原子替换 | `StaffThread.run()`（`HttpStaffInfoGetter.java:87-122`）全量刷新时，先写入临时 Map，再在 `LOCK`（line 39）同步下 `clear()` + `putAll()` 原子替换，保证 Map 仅保留当前一份全量数据 |
| F-P0-02 | 旧 ClassLoader 资源释放 | `AppStandardClassUtils.refreshClassloader`（`AppStandardClassUtils.java:39-49`）在 `remove()` 旧 ClassLoader 前调用 `URLClassLoader.close()`，释放 jar 文件句柄 |
| F-P0-03 | 员工信息查询接口回归 | 修复后 `getStaffInfoByUsername`、`getAllUsers`、`getAllUsernames` 等接口行为与返回结构不变 |
| F-P0-04 | AppConn 加载功能回归 | 修复后 `CommonAppConnLoader.getAppConn()` 的 AppConn 加载/热部署流程正常，返回的 `AppConn` 实例行为不变 |

### 5.2 增强功能 P1

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P1-01 | URLJarFile 句柄治理 | 通过 F-P0-02 的 `close()` 调用，使 `URLJarFile` 实例可被 GC，prod 环境 jar 句柄数不随 AppConn 刷新次数累积 |
| F-P1-02 | Hadoop Configuration 复用 | DSS 侧涉及 HDFS/ViewFS 访问的调用点排查并复用全局 `Configuration`，避免每次 `new Configuration()`；结合 `fs.viewfs.impl.disable.cache` 等配置治理（边界外部分与 Linkis 协调） |
| F-P1-03 | 刷新日志可观测 | `StaffThread` 刷新后输出当前 Map size；`refreshClassloader` 在 `close` 旧 ClassLoader 时输出日志，便于运维监控内存与句柄趋势 |

### 5.3 功能不包含

| 编号 | 不包含项 | 说明 |
|:----:|---------|------|
| N-01 | 不替换缓存实现框架 | 不引入 Guava Cache / Caffeine 替换原生 `ConcurrentHashMap`（原子替换已可解决泄漏，引入新框架超出最小修复范围） |
| N-02 | 不调整 ESB 拉取频率 | `StaffThread` 仍保持每小时 1 次，不改为按需缓存或增量同步（需求阶段不做技术选型） |
| N-03 | 不修改 AppConnClassLoader 的类加载策略 | `AppConnClassLoader`（`AppConnClassLoader.java:25`）的 `loadClass` 逻辑不变，仅确保 `close()` 可用 |
| N-04 | 不承诺完全消除 ViewFileSystem/Configuration 问题 | 该问题根因部分在 Linkis/Hadoop 客户端侧，DSS 仅修复可直接控制的调用点与配置，边界外部分如实标注依赖协调 |
| N-05 | 不改动 WeakReference/SoftReference 缓存策略 | 不将 `INSTANCES`/`CLASS_LOADER_MAP` 改为弱引用（需评估 AppConn 生命周期，超出本次修复范围） |

### 5.4 修复方案候选（由设计阶段决定）

> 以下为候选方案方向，需求阶段仅列出，不在本文档做技术选型。

| 候选 | 方向 | 优点 | 缺点 |
|:----:|------|------|------|
| 候选A（HttpStaffInfoGetter） | 临时 Map + `LOCK` 同步下 `clear()`+`putAll()` 原子替换 | 改动最小，复用已声明的 `LOCK`（line 39），立即解决泄漏 | 刷新瞬间存在短暂"Map 已 clear 但未 putAll 完"的窗口，需 `LOCK` 保护读路径或接受读默认值 |
| 候选B（HttpStaffInfoGetter） | 引入 Guava Cache/Caffeine，设置 `maximumSize` 与 `expireAfterWrite` | 自带上限/过期/统计 | 引入新依赖，改动范围大 |
| 候选C（AppStandardClassUtils） | `refreshClassloader` 中 `remove` 前 `close()` 旧 `URLClassLoader` | 直接闭环资源释放，与现有双检锁结构兼容 | `close()` 后若有线程仍持有旧 ClassLoader 引用并尝试加载类，会抛异常（需评估并发） |
| 候选D（AppStandardClassUtils） | 将 `CLASS_LOADER_MAP` 改为 `WeakHashMap`，依赖 GC 回收 | 自动回收，无需显式 close | 弱引用回收时机不可控，jar 句柄仍可能延迟释放；且 `close()` 仍需补齐 |
| 候选E（ViewFileSystem/Configuration） | DSS 侧复用全局 `Configuration` + 配置 `fs.viewfs.impl.disable.cache=true` | DSS 侧可控部分立即见效 | 边界外部分依赖 Linkis 侧协调，无法保证完全消除 |

---

## 六、非功能性需求

### 6.1 兼容性

| 影响项 | 要求 |
|-------|------|
| JDK 版本 | 保持 JDK 8 兼容；`URLClassLoader.close()` 自 JDK 7 可用，无版本风险 |
| Linkis 依赖 | 不调整 Linkis `1.18.3-wds` 依赖版本；ViewFileSystem/Configuration 治理不修改 Linkis 客户端代码，仅通过配置项协调 |
| 现有部署 | 不新增数据库表、不修改表结构；配置项变更可通过 `conf/dss.properties` 覆盖，不强制改 `assembly/config/config.sh` |
| AppConn 插件 | 已安装的 AppConn 插件无需重新打包；`close()` 逻辑对 AppConn 业务代码透明 |
| 员工信息查询调用方 | 接口签名与返回结构不变，调用方无需改造 |

### 6.2 安全性

| 安全项 | 要求 |
|-------|------|
| ESB 鉴权 | `StaffThread` 的 ESB 签名逻辑（`generateSignature`，`HttpStaffInfoGetter.java:63-68`）不变，不放宽鉴权 |
| ClassLoader 安全 | `close()` 仅释放资源，不改变 AppConn 类加载的隔离边界；close 逻辑仅在 `refreshClassloader` 内部触发，无外部主动卸载入口 |
| 配置项安全 | 新增 Hadoop 配置项不暴露敏感信息，不削弱 HDFS 访问鉴权 |

### 6.3 性能与资源占用

| 指标 | 修复前（MAT 实测） | 修复后目标 |
|-----|------------------|----------|
| `HttpStaffInfoGetter` 堆占用（apps-server） | 23,939,272 bytes（27.79%） | 刷新后回落至当前 ESB 全量数据单份大小，不再随运行时间单调增长 |
| `HttpStaffInfoGetter` 堆占用（dev） | 23,939,288 bytes（5.85%） | 同上 |
| `AppStandardClassUtils` 堆占用（dev） | 7,755,312 bytes（1.90%） | AppConn 卸载/刷新后旧 Class 元数据可回收，占用不随刷新次数累积 |
| `AppStandardClassUtils` 堆占用（prod） | 7,756,480 bytes（8.68%） | 同上 |
| `URLJarFile` 实例数（prod） | 568 实例，9,190,544 bytes（10.29%） | AppConn 反复加载/卸载后实例数稳定，不随刷新次数增长 |
| jar 文件句柄数 | 随 AppConn 刷新次数累积 | `lsof -p <pid> \| grep jar \| wc -l` 在连续 10 次刷新后保持稳定 |
| `ViewFileSystem` 堆占用（dev） | 4 实例，158,190,400 bytes（38.68%） | DSS 侧可控部分下降；边界外部分依赖 Linkis 协调，不设硬性量化目标 |
| `Configuration` 堆占用（dev） | 39 实例，56,703,128 bytes（13.86%） | DSS 侧调用点复用全局 Configuration 后下降；边界外部分依赖协调 |
| Full GC 频率 | 长期运行后频繁 Full GC | 修复 P0 两项后，Full GC 频率显著下降（具体阈值由运维监控确认） |

### 6.4 可观测性

| 可观测项 | 要求 |
|---------|------|
| 员工信息刷新日志 | `StaffThread` 每次 `clear()`+`putAll()` 后输出 `LOGGER.info("Staff info refreshed, current size: {}", STAFF_INFO_MAP.size())`，便于监控 Map 规模 |
| ClassLoader 释放日志 | `refreshClassloader` 在 `close()` 旧 ClassLoader 时输出 `LOGGER.info("Closed old URLClassLoader for appConn: {}", ...)`；失败时 `LOGGER.warn(...)` |
| 句柄监控 | 建议运维补充 `lsof -p <pid> \| grep jar \| wc -l` 定时采集，纳入监控大盘（运维侧，非代码改动） |
| 堆内存监控 | 建议修复后重新 dump 堆内存用 MAT 对比，验证 `HttpStaffInfoGetter`/`AppStandardClassUtils`/`URLJarFile` 占用下降 |

---

## 七、影响面分析

### 7.1 受影响调用方/模块梳理

| 调用方/模块 | 涉及功能 | 本次影响 |
|------------|---------|---------|
| 员工信息查询接口（`getStaffInfoByUsername`、`getAllUsers`、`getAllUsernames`、`getFullOrgNameByUsername`、`getAllDepartments`） | 工作流人员选择、部门展示、代理用户查询 | ✅ 读 `STAFF_INFO_MAP` 路径需评估原子替换瞬间的并发可见性（`LOCK` 保护或接受短暂读默认值） |
| AppConn 加载/卸载/热部署（`CommonAppConnLoader`、`appconn-install.sh`、`appconn-refresh.sh`） | 第三方系统（Schedulis、Visualis、Qualitis 等）集成 | ✅ `refreshClassloader` 新增 `close()`，需回归 AppConn 加载/刷新流程 |
| `AppStandardClassUtils.getInstance` / `getClassLoader` / `getReflections` | AppConn 类反射加载 | ✅ `close()` 后旧 ClassLoader 不可再用，需确认无线程持有旧引用 |
| HDFS/ViewFS 访问（DSS 侧调用点） | 工作流产物读写、结果导出 | ⚠️ 需排查 DSS 代码中 `new Configuration()` / `FileSystem.get` 的调用点，复用全局 Configuration |
| Linkis 任务提交（边界外） | EngineConn 启动、任务执行 | ⚠️ 不在 DSS 直接代码范围内，依赖 Linkis 侧配置协调 |

> **待确认项**：设计阶段需补充排查 DSS 代码中所有 `new Configuration()` 与 `FileSystem.get(...)` 的调用点，确认哪些可由 DSS 直接治理。

### 7.2 配置变更

| 变更项 | 说明 | 归属 |
|-------|------|------|
| `fs.viewfs.impl.disable.cache` | 可选配置，控制 ViewFileSystem 缓存行为；是否启用需与 Linkis 协调 | Hadoop/Linkis 配置 |
| 全局 `Configuration` 复用 | DSS 侧引入 `HadoopConfigurationHolder`（或类似）持有单例 Configuration，避免 `new Configuration()` | DSS 代码（设计阶段确定） |
| 员工信息刷新机制 | 无配置变更，仅代码逻辑调整（原子替换） | DSS 代码 |
| ClassLoader close 机制 | 无配置变更，仅代码逻辑调整 | DSS 代码 |

### 7.3 代码影响范围

| 模块 | 文件 | 改造类型 |
|-----|------|---------|
| dss-common-server-webank | `HttpStaffInfoGetter.java` | 修改 `StaffThread.run()`（line 87-122）：临时 Map + `LOCK` 同步下原子替换；启用已声明的 `LOCK`（line 39） |
| dss-standard-common | `AppStandardClassUtils.java` | 修改 `refreshClassloader`（line 39-49）：`remove` 前 `close()` 旧 ClassLoader；新增 `closeClassLoader` 私有方法 |
| dss-appconn-loader | `AppConnClassLoader.java` | 可选：显式重写 `close()`（当前继承 `URLClassLoader.close()` 已可用，未来扩展时建议显式重写） |
| dss-appconn-loader | `CommonAppConnLoader.java` | 无需改动（通过 `refreshClassloader` 间接生效） |
| DSS HDFS 访问调用点 | 待排查的 `new Configuration()` / `FileSystem.get` 调用点 | 复用全局 Configuration；具体文件需在设计阶段逐点核实 |

---

## 八、验收标准（三段式）

### 8.1 HttpStaffInfoGetter 刷新后内存不增长（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-01 | 前置条件 | dss-apps-server 正常运行，记录初始 `HttpStaffInfoGetter` 持有的 `STAFF_INFO_MAP.size()` 与堆内存占用 |
| AC-01 | 操作步骤 | 连续等待/触发 `StaffThread` 全量刷新 5 次以上（模拟 5 小时运行），每次刷新后记录 `STAFF_INFO_MAP.size()`；dump 堆内存用 MAT 分析 `HttpStaffInfoGetter` 占用 |
| AC-01 | 预期结果 | 1) 每次刷新后 `STAFF_INFO_MAP.size()` 与 ESB 当前全量员工数一致，不随刷新次数单调增长；2) 日志输出 `Staff info refreshed, current size: N`；3) MAT 分析 `HttpStaffInfoGetter` 的 `ConcurrentHashMap$Node[]` 占用回落至单份全量数据大小，不再保持 23.9MB+ 残留 |

### 8.2 AppConn 反复加载/卸载后 jar 句柄不增长（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-02 | 前置条件 | dss-server 正常运行，记录初始 jar 文件句柄数（`lsof -p <pid> \| grep jar \| wc -l`）与 `AppStandardClassUtils` 持有的 ClassLoader 数 |
| AC-02 | 操作步骤 | 反复执行 AppConn 加载/卸载（`appconn-install.sh` + `appconn-refresh.sh`）10 次以上，每次记录 jar 句柄数；日志确认 `Closed old URLClassLoader for appConn: ...` 输出 |
| AC-02 | 预期结果 | 1) jar 文件句柄数在 10 次刷新后保持稳定（允许小幅波动，不单调增长）；2) 日志输出 `Closed old URLClassLoader` 表明旧 ClassLoader 已 close；3) `AppStandardClassUtils` 的 `CLASS_LOADER_MAP.size()` 与当前已加载 AppConn 数一致 |

### 8.3 URLJarFile 实例数下降（P1）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-03 | 前置条件 | 在 prod 或类 prod 环境运行 dss-server，已完成 AC-02 的 AppConn 反复加载/卸载 |
| AC-03 | 操作步骤 | dump 堆内存用 MAT 分析 `sun.net.www.protocol.jar.URLJarFile` 实例数与占用 |
| AC-03 | 预期结果 | 1) `URLJarFile` 实例数较修复前（prod 568 实例）显著下降；2) `URLJarFile` 占用不再为 Problem Suspect（修复前 9,190,544 bytes / 10.29%）；3) 连续刷新后实例数稳定 |

### 8.4 员工信息查询接口回归（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-04 | 前置条件 | `StaffThread` 至少成功刷新 1 次，`STAFF_INFO_MAP` 已填充当前全量员工数据 |
| AC-04 | 操作步骤 | 分别调用 `getStaffInfoByUsername("存在的用户")`、`getStaffInfoByUsername("不存在的用户")`、`getStaffInfoByUsername(null)`、`getAllUsers()`、`getAllUsernames()`、`getAllDepartments()`、`getFullOrgNameByUsername("存在的用户")` |
| AC-04 | 预期结果 | 1) 存在用户返回对应 `StaffInfo`；不存在用户返回 `null`；`null` 入参返回 `null`/默认部门；2) `getAllUsers`/`getAllUsernames` 返回集合大小与 `STAFF_INFO_MAP.size()` 一致；3) 行为与修复前一致（回归不破坏） |

### 8.5 AppConn 功能回归（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-05 | 前置条件 | 已安装至少 2 个 AppConn（如 workflow、scriptis） |
| AC-05 | 操作步骤 | 执行 `appconn-refresh.sh` 热部署刷新；触发工作流创建/编辑/执行等依赖 AppConn 的操作 |
| AC-05 | 预期结果 | 1) AppConn 刷新成功，无 `ClassNotFoundException`/`NoClassDefFoundError`；2) 工作流创建/编辑/执行功能正常；3) 第三方系统（Schedulis/Visualis/Qualitis）集成功能正常 |

### 8.6 原子替换并发可见性验证（P1）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-06 | 前置条件 | dss-apps-server 运行中，并发模拟员工信息查询 |
| AC-06 | 操作步骤 | 在 `StaffThread` 执行 `clear()`+`putAll()` 原子替换期间，并发调用 `getStaffInfoByUsername` |
| AC-06 | 预期结果 | 1) 查询不抛异常；2) 替换瞬间可能返回 `null`/默认值（可接受，由 `LOCK` 保护范围决定）；3) 替换完成后查询返回正确数据；4) 不出现数据不一致（如部分新部分旧） |

### 8.7 HDFS/ViewFS 功能回归（P1）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-07 | 前置条件 | DSS 侧 HDFS 访问调用点已复用全局 Configuration（若设计阶段确认可修复） |
| AC-07 | 操作步骤 | 触发工作流产物读写、结果导出等 HDFS 访问操作 |
| AC-07 | 预期结果 | 1) HDFS 读写功能正常；2) MAT 分析 DSS 侧 `Configuration` 实例数下降；3) `ViewFileSystem` 相关功能正常（边界外部分如实标注，不强制量化） |

---

## 九、风险与依赖

| 编号 | 风险/依赖 | 等级 | 应对措施 |
|:----:|---------|:----:|---------|
| R-01 | `STAFF_INFO_MAP` 原子替换瞬间，并发读可能命中"已 clear 未 putAll 完"的窗口 | 中 | 用已声明的 `LOCK`（line 39）保护读路径，或接受短暂返回 `null`/默认值（查询接口已有 null 兜底逻辑，`HttpStaffInfoGetter.java:135-136`）；设计阶段评估是否需读写锁 |
| R-02 | `close()` 旧 ClassLoader 后，若有线程仍持有旧引用并尝试加载类，会抛异常 | 中 | 评估 AppConn 刷新时的并发上下文切换（`CommonAppConnLoader.java:58` 的 `setContextClassLoader`）；确保 close 在所有引用解除后执行 |
| R-03 | ViewFileSystem/Configuration 根因部分在 Linkis/Hadoop 客户端侧，DSS 无法完全修复 | 高 | 如实标注边界；DSS 侧仅修复可直接控制的调用点与配置；边界外部分提 Linkis 侧 issue 或协调配置，不承诺完全消除 |
| R-04 | ESB 接口稳定性影响员工信息刷新（刷新失败时 Map 行为） | 低 | 原子替换方案中，ESB 拉取失败时不清空旧 Map（`catch` 块不触达 `clear()`），保留上一次成功数据，不影响业务 |
| R-05 | `appconn-refresh.sh` 热部署流程频繁触发，close 与新 ClassLoader 创建存在时序竞争 | 中 | `refreshClassloader` 已有双检锁（`AppStandardClassUtils.java:40-46`）；close 操作纳入同步块，确保原子性 |
| D-01 | ViewFileSystem/Configuration 治理依赖 Linkis 侧配置（如 `fs.viewfs.impl.disable.cache`）与客户端复用策略 | 高 | 需与 Linkis 团队协调确认配置项与客户端行为；DSS 侧先行修复可控部分 |
| D-02 | MAT 验证依赖运维重新 dump 堆内存并对比 | 低 | 修复部署后由运维在 dev/prod 环境重新 dump，用 MAT 对比 Problem Suspects 变化 |

---

## 十、附录

### 10.1 关键代码位置索引

| 文件 | 行号 | 说明 |
|------|:----:|------|
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 38 | `STAFF_INFO_MAP` 声明（泄漏对象，static ConcurrentHashMap） |
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 39 | `LOCK` 字段（已声明未使用，修复后启用原子替换同步） |
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 87-122 | `StaffThread.run()`（全量刷新逻辑，修复点：临时 Map + 原子替换） |
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 113 | `STAFF_INFO_MAP.put(...)`（Bug 点：直接追加，从不清理） |
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 125-127 | static 块：`scheduleAtFixedRate` 每小时调度 |
| `dss-commons/dss-common-server-webank/src/main/java/com/webank/wedatasphere/dss/common/server/esb/http/HttpStaffInfoGetter.java` | 129-170 | 员工信息查询接口（`getFullOrgNameByUsername`、`getStaffInfoByUsername`、`getAllDepartments`、`getAllUsers`、`getAllUsernames`），回归对象 |
| `dss-standard/dss-standard-common/src/main/java/com/webank/wedatasphere/dss/standard/common/utils/AppStandardClassUtils.java` | 34-35 | `INSTANCES` 与 `CLASS_LOADER_MAP` 声明（泄漏对象，static ConcurrentHashMap） |
| `dss-standard/dss-standard-common/src/main/java/com/webank/wedatasphere/dss/standard/common/utils/AppStandardClassUtils.java` | 39-49 | `refreshClassloader`（修复点：`remove` 前 `close` 旧 ClassLoader） |
| `dss-standard/dss-standard-common/src/main/java/com/webank/wedatasphere/dss/standard/common/utils/AppStandardClassUtils.java` | 51-61 | `getClassLoader`（双检锁创建 ClassLoader，不改动） |
| `dss-standard/dss-standard-common/src/main/java/com/webank/wedatasphere/dss/standard/common/utils/AppStandardClassUtils.java` | 83-94 | `getReflections`（持有 Reflections 对象，通过 ClassLoader 强引用驻留） |
| `dss-appconn/dss-appconn-loader/src/main/java/com/webank/wedatasphere/dss/appconn/loader/clazzloader/AppConnClassLoader.java` | 25 | `AppConnClassLoader extends URLClassLoader`（`close()` 由父类提供，可选显式重写） |
| `dss-appconn/dss-appconn-loader/src/main/java/com/webank/wedatasphere/dss/appconn/loader/loader/CommonAppConnLoader.java` | 47-101 | `getAppConn`（AppConn 加载入口） |
| `dss-appconn/dss-appconn-loader/src/main/java/com/webank/wedatasphere/dss/appconn/loader/loader/CommonAppConnLoader.java` | 57 | 调用 `refreshClassloader`（问题 2、3 触发点） |

### 10.2 相关接口/模块

| 接口/模块 | 说明 | 本次是否修改 |
|---------|------|:----------:|
| `StaffInfoGetter.getStaffInfoByUsername` | 员工信息查询（`HttpStaffInfoGetter.java:142-148`） | ❌ 接口不修改（读路径需评估并发） |
| `StaffInfoGetter.getAllUsers` | 全量员工查询（`HttpStaffInfoGetter.java:162-165`） | ❌ 接口不修改 |
| `StaffInfoGetter.getAllUsernames` | 全量用户名查询（`HttpStaffInfoGetter.java:167-170`） | ❌ 接口不修改 |
| `StaffInfoGetter.getAllDepartments` | 全量部门查询（`HttpStaffInfoGetter.java:150-160`） | ❌ 接口不修改 |
| `StaffInfoGetter.getFullOrgNameByUsername` | 组织名查询（`HttpStaffInfoGetter.java:129-140`） | ❌ 接口不修改 |
| `AppStandardClassUtils.refreshClassloader` | ClassLoader 刷新（`AppStandardClassUtils.java:39-49`） | ✅ 修复对象（新增 close） |
| `CommonAppConnLoader.getAppConn` | AppConn 加载（`CommonAppConnLoader.java:47-101`） | ⚠️ 一般不改动（间接生效） |
| `appconn-install.sh` / `appconn-refresh.sh` | AppConn 安装/热部署脚本 | ❌ 脚本不修改（行为通过代码修复间接生效） |

### 10.3 预估工时

| 任务 | 工时（人天） |
|-----|------------|
| HttpStaffInfoGetter 原子替换改造与并发评估 | 0.5 |
| AppStandardClassUtils close 改造 | 0.5 |
| AppConn 加载/卸载回归测试（含 jar 句柄验证） | 1.0 |
| 员工信息查询接口回归测试 | 0.5 |
| ViewFileSystem/Configuration DSS 侧调用点排查与配置治理 | 1.0 |
| MAT 堆内存对比验证（三环境） | 0.5 |
| 文档更新 | 0.3 |
| Linkis 侧协调（ViewFileSystem/Configuration 边界外部分） | 0.5 |
| **合计** | **4.8** |

---

**文档版本**：v1.0
**创建日期**：2026-08-04
**需求负责人**：待定
**优先级**：P1（当前迭代完成）
