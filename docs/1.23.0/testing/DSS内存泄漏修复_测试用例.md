# DSS 内存泄漏修复 测试用例

| 属性 | 值 |
|------|-----|
| 测试编号 | TST-DSS-1.23.0-FIX-003 |
| 关联需求 | REQ-DSS-1.23.0-FIX-003 |
| 关联设计 | DES-DSS-1.23.0-FIX-003 |
| 测试类型 | 单元测试 + 集成测试 + 接口测试 + 回归测试 |
| 优先级 | P1 |
| 版本 | dev-1.23.0 |
| 创建日期 | 2026-08-04 |

---

## 一、测试概要

### 1.1 测试目的

验证 DSS 内存泄漏修复（`HttpStaffInfoGetter` 缓存无限增长 + `AppStandardClassUtils` 未关闭旧 ClassLoader）的两处 P0 修复，确保：

1. **HttpStaffInfoGetter 缓存不再无限增长**：每次全量刷新后 `STAFF_INFO_MAP.size()` 等于当前 ESB 全量返回条数，连续运行后堆内存中该 Map 占用稳定，不再保持 23.9MB+ 残留。
2. **旧 ClassLoader 被正确关闭**：`refreshClassloader` 替换 ClassLoader 时调用 `URLClassLoader.close()`，jar 文件句柄数不随 AppConn 加载次数累积。
3. **URLJarFile 句柄可释放**：连续触发 AppConn 加载/卸载 10 次以上，`lsof -p <pid> | grep jar | wc -l` 不持续增长；MAT 分析 `URLJarFile` 实例数较 prod 568 实例显著下降。
4. **员工信息查询接口行为不变**：`getStaffInfoByUsername`、`getAllUsers`、`getAllDepartments`、`getFullOrgNameByUsername`、`getAllUsernames` 返回语义与修复前一致。
5. **AppConn 加载/热部署流程不变**：`CommonAppConnLoader.getAppConn` 调用路径不变，AppConn 加载与执行功能回归通过。

### 1.2 测试范围

| 模块 | 类/接口 | 是否主修复对象 |
|------|---------|:------------:|
| dss-common-server-webank | `HttpStaffInfoGetter`（`StaffThread.run()` 原子替换） | ✅ 是 |
| dss-standard-common | `AppStandardClassUtils`（`refreshClassloader` close 旧 CL + `closeClassLoader`） | ✅ 是 |
| dss-appconn-loader | `AppConnClassLoader`（继承 URLClassLoader，close 由父类提供） | ❌ 否（不改动，间接验证） |
| dss-appconn-loader | `CommonAppConnLoader.getAppConn`（调用 refreshClassloader） | ❌ 否（回归） |
| 员工信息查询接口 | `getStaffInfoByUsername` / `getAllUsers` / `getAllDepartments` / `getFullOrgNameByUsername` / `getAllUsernames` | ❌ 否（回归） |

### 1.3 测试环境前置条件

| 项 | 要求 |
|---|------|
| DSS 服务版本 | dev-1.23.0-SNAPSHOT（含本次修复代码） |
| JDK 版本 | 1.8 |
| 数据库 | MySQL 5.7+ |
| SIT 环境 | dss-apps-server、dss-server 正常运行，可 dump 堆内存 |
| ESB HR 接口 | `EsbConf.ESB_HTTP_URL` + `ESB_HR_STAFF_URL` 可达，`ESB_APPID`/`ESB_TOKEN` 配置正确 |
| AppConn 已安装 | 至少安装 workflow、scriptis 两个 AppConn（`assembly/bin/appconn-install.sh`） |
| 热部署脚本 | `appconn-install.sh` + `appconn-refresh.sh` 可用 |
| MAT 工具 | Memory Analyzer Tool，用于 heap dump 分析 |
| 监控命令 | `jmap`、`lsof`、`jstack` 可用（Linux 环境） |
| 单元测试框架 | JUnit 5 + Mockito |

> 注：CLAUDE.md 已声明 `assembly/bin/install.sh` 在 Windows 下中止，shell 流程类用例需在 Linux/WSL 环境执行。

### 1.4 测试数据

| 数据类型 | 说明 |
|---------|------|
| ESB 员工数据 | 全量员工 JSON（含 englishName、orgFullName 等字段），可通过 mock ESB 接口返回不同数据集验证"不累积" |
| 测试 AppConn | 已安装的 workflow、scriptis AppConn，用于反复加载/卸载 |
| 测试用户 | 任意已登录 DSS 用户，用于员工信息查询接口回归 |

### 1.5 关键代码位置索引

| 文件 | 行号 | 说明 |
|------|:----:|------|
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 38 | `STAFF_INFO_MAP` 声明（ConcurrentHashMap） |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 39 | `LOCK` 声明（修复后复用为原子替换锁） |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 88-132 | `StaffThread.run()` 方法（修复对象：tempMap + 原子替换） |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 115 | `tempMap.put(...)` -- 改：原写 STAFF_INFO_MAP |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 124-127 | `synchronized(LOCK){ clear(); putAll(tempMap); }` 原子替换（新增） |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 128 | 新增日志 `Staff info refreshed, current size: {}` |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 125-127 | static 块：`scheduleAtFixedRate(new StaffThread(), 0, 1, HOURS)` |
| `dss-commons/dss-common-server-webank/.../HttpStaffInfoGetter.java` | 130-170 | 5 个查询方法（不改动，回归对象） |
| `dss-standard/dss-standard-common/.../AppStandardClassUtils.java` | 41-54 | `refreshClassloader` 方法（修复对象：remove 后 close） |
| `dss-standard/dss-standard-common/.../AppStandardClassUtils.java` | 47-49 | `oldClassLoader` 保存 + `closeClassLoader` 调用（新增） |
| `dss-standard/dss-standard-common/.../AppStandardClassUtils.java` | 56-65 | `closeClassLoader` 私有方法（新增） |
| `dss-appconn/dss-appconn-loader/.../CommonAppConnLoader.java` | 57 | `refreshClassloader` 调用点（不改动，间接验证） |

---

## 二、测试用例总览表

### 2.1 主修复对象测试用例（TC-LEAK-001 ~ TC-LEAK-010）

| 用例编号 | 标题 | 类型 | 优先级 | 关联 AC | 关联 IT | 预期结果摘要 |
|---------|------|------|:------:|:-------:|:-------:|------------|
| TC-LEAK-001 | 员工信息连续刷新不累积 | 集成测试 | P0 | AC-01 | IT-01/IT-02 | size 不单调增长；日志输出 Staff info refreshed |
| TC-LEAK-002 | 员工信息刷新失败保留旧数据 | 集成测试 | P1 | AC-01 | - | ESB 异常时不清空 Map，保留上次成功数据 |
| TC-LEAK-003 | 员工信息查询命中与未命中 | 接口测试 | P0 | AC-04 | IT-05 | 命中返回 StaffInfo，未命中返回 null |
| TC-LEAK-004 | 员工信息查询-null 入参返回默认部门 | 接口测试 | P1 | AC-04 | IT-05 | null 入参返回 null/DEFAULT_DEPARTMENT |
| TC-LEAK-005 | 原子替换期间并发读不抛异常 | 集成测试 | P1 | AC-06 | - | 并发查询不抛异常；可接受短暂返回 null/默认值 |
| TC-LEAK-006 | AppConn 连续加载/卸载 jar 句柄不增长 | 集成测试 | P0 | AC-02 | IT-04 | lsof jar 句柄稳定；日志 Closed old URLClassLoader |
| TC-LEAK-007 | close 旧 ClassLoader 日志验证 | 集成测试 | P1 | AC-02 | IT-03 | refresh 输出 Closed old URLClassLoader for appConn |
| TC-LEAK-008 | close 异常降级 warn 不阻断刷新 | 单元测试 | P1 | AC-05 | IT-06 | mock close 抛 IOException；refresh 正常返回新 CL；warn 日志 |
| TC-LEAK-009 | URLJarFile 实例数下降 | 集成测试 | P1 | AC-03 | IT-07 | MAT 分析 URLJarFile 实例数较 prod 568 显著下降 |
| TC-LEAK-010 | 非 URLClassLoader 不 close | 单元测试 | P1 | AC-05 | - | 传入非 URLClassLoader 不抛异常，不调用 close |

### 2.2 回归测试用例（TC-REG-001 ~ TC-REG-003）

| 用例编号 | 标题 | 类型 | 优先级 | 关联 AC | 预期结果摘要 |
|---------|------|------|:------:|:-------:|------------|
| TC-REG-001 | 员工信息 5 个查询接口回归 | 接口测试 | P0 | AC-04 | 5 个查询方法返回正常，行为与修复前一致 |
| TC-REG-002 | AppConn 热部署功能回归 | 集成测试 | P0 | AC-05 | appconn-refresh.sh 热部署后工作流创建/编辑/执行正常 |
| TC-REG-003 | HDFS/ViewFS 功能回归 | 集成测试 | P1 | AC-07 | HDFS 读写正常；边界外部分依赖 Linkis 协调，不强制量化 |

### 2.3 测试用例统计

| 类型 | 数量 | P0 | P1 |
|------|:----:|:--:|:--:|
| 主修复对象 | 10 | 3 | 7 |
| 回归测试 | 3 | 2 | 1 |
| **合计** | **13** | **5** | **8** |

---

## 三、详细测试用例 - 主修复对象

### 3.1 TC-LEAK-001：员工信息连续刷新不累积

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-001 |
| **用例名称** | 员工信息连续刷新不累积 |
| **优先级** | P0 |
| **关联 AC** | AC-01 |
| **关联 IT** | IT-01、IT-02 |
| **测试类型** | 集成测试 |

**前置条件**：
1. dss-apps-server 已部署 dev-1.23.0-SNAPSHOT（含修复代码）并正常运行
2. ESB HR 接口可达，返回全量员工 JSON
3. 记录初始 `HttpStaffInfoGetter` 持有的 `STAFF_INFO_MAP.size()` 与堆内存占用（`jmap -histo:live <pid> | grep HttpStaffInfoGetter`）

**测试步骤**：
1. 等待定时刷新触发，或手动触发 `StaffThread.run()`（通过反射或调大调度频率），连续刷新 5 次以上（模拟 5 小时运行）
2. 第 2 次刷新前，mock ESB 接口返回**不同 key 集合**的员工数据（如 englishName 加前缀、移除部分人员），模拟 key 漂移/离职场景
3. 每次刷新后记录 `STAFF_INFO_MAP.size()`（从日志 `Staff info refreshed, current size: N` 读取）
4. 第 5 次刷新后，dump 堆内存并用 MAT 分析 `HttpStaffInfoGetter` 占用：
```bash
jmap -dump:live,format=b,file=/tmp/dss_after5.hprof <pid>
```

**测试数据**：
- 第 1 次 ESB 返回：含 `user01~user5000` 共 5000 条
- 第 2 次 ESB 返回：含 `user01~user5000` + 新增 `user5001~user6000`，移除 `user01~user1000`（共 5000 条，key 集合与第 1 次不同）

**预期结果**：
1. 每次刷新后 `STAFF_INFO_MAP.size()` 与 ESB 当前全量返回条数一致（第 1 次=5000，第 2 次=5000），**不随刷新次数单调增长**（修复前会累积到 6000+）
2. 日志每次输出 `Staff info refreshed, current size: 5000`
3. MAT 分析 `HttpStaffInfoGetter` 的 `ConcurrentHashMap$Node[]` 占用回落至单份全量数据大小，**不再保持 23,939,272 bytes（27.79%）残留**
4. 第 2 次刷新后 Map 中**不含**第 1 次独有的 `user01~user1000`（验证 clear 生效）

**校验方式**：
```bash
# 1. 查看刷新日志
tail -f logs/dss-apps-server.log | grep "Staff info refreshed"
# 期望：每次 current size 与 ESB 返回条数一致

# 2. dump 堆内存分析
jmap -dump:live,format=b,file=/tmp/dss_leak001.hprof <pid>
# 用 MAT 打开，Leak Suspects 中 HttpStaffInfoGetter 的 ConcurrentHashMap$Node[]
# 不再为 Problem Suspect，占用回落至单份全量大小
```

**后置清理**：无

---

### 3.2 TC-LEAK-002：员工信息刷新失败保留旧数据

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-002 |
| **用例名称** | 员工信息刷新失败保留旧数据 |
| **优先级** | P1 |
| **关联 AC** | AC-01 |
| **关联 IT** | - |
| **测试类型** | 集成测试 |

**前置条件**：
1. dss-apps-server 正常运行，`STAFF_INFO_MAP` 已成功刷新至少 1 次（size=N>0）
2. 模拟 ESB 接口不可达（断网或 mock 返回 500）

**测试步骤**：
1. 记录当前 `STAFF_INFO_MAP.size()` = N（从日志读取）
2. 断开 ESB 接口（或 mock ESB 返回异常），触发 `StaffThread.run()` 执行
3. 等待刷新执行完毕（日志出现 `fail to get esb response, reason is ...`）
4. 调用 `getStaffInfoByUsername("已存在的用户")` 验证数据仍在

**预期结果**：
1. ESB 异常时 `synchronized(LOCK)` 块不执行（`catch` 块捕获异常，tempMap 随方法栈销毁），**STAFF_INFO_MAP 不被 clear**
2. `STAFF_INFO_MAP.size()` 仍为 N（保留上次成功刷新的数据）
3. `getStaffInfoByUsername("已存在的用户")` 仍返回对应 `StaffInfo`（查询接口可用性不受影响）
4. 日志输出 `fail to get esb response, reason is ...`，**不输出** `Staff info refreshed`

**校验方式**：
```bash
# 查看异常日志（不刷新成功）
grep "fail to get esb response" logs/dss-apps-server.log | tail -1
grep "Staff info refreshed" logs/dss-apps-server.log | tail -1
# 期望：异常日志有新增；Staff info refreshed 无新增（本次刷新失败）
```

**后置清理**：恢复 ESB 接口可达

---

### 3.3 TC-LEAK-003：员工信息查询命中与未命中

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-003 |
| **用例名称** | 员工信息查询命中与未命中 |
| **优先级** | P0 |
| **关联 AC** | AC-04 |
| **关联 IT** | IT-05 |
| **测试类型** | 接口测试 |

**前置条件**：
1. `StaffThread` 至少成功刷新 1 次，`STAFF_INFO_MAP` 已填充当前全量员工数据
2. 已知一个存在的 englishName（如 `zhangsan`）和一个不存在的 englishName（如 `nosuchuser123`）

**测试步骤**：
1. 调用 `getStaffInfoByUsername("zhangsan")`（命中场景）
2. 调用 `getStaffInfoByUsername("nosuchuser123")`（未命中场景）
3. 调用 `getAllUsers()`，校验返回 List 大小
4. 调用 `getAllUsernames()`，校验返回 Set 大小

**预期结果**：
1. 命中场景：返回对应 `StaffInfo` 对象，`getEnglishName()` = `zhangsan`
2. 未命中场景：返回 `null`
3. `getAllUsers()` 返回 `List<StaffInfo>`，size = `STAFF_INFO_MAP.size()`
4. `getAllUsernames()` 返回 `Set<String>`，size = `STAFF_INFO_MAP.size()`
5. 行为与修复前一致（查询方法未改动，回归不破坏）

**校验方式**：
- 单元测试或接口调用断言返回值

**后置清理**：无

---

### 3.4 TC-LEAK-004：员工信息查询-null 入参返回默认部门

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-004 |
| **用例名称** | 员工信息查询-null 入参返回默认部门 |
| **优先级** | P1 |
| **关联 AC** | AC-04 |
| **关联 IT** | IT-05 |
| **测试类型** | 接口测试（边界场景） |

**前置条件**：
1. `STAFF_INFO_MAP` 已填充全量员工数据

**测试步骤**：
1. 调用 `getFullOrgNameByUsername(null)`
2. 调用 `getStaffInfoByUsername(null)`
3. 调用 `getFullOrgNameByUsername("nosuchuser123")`（未命中）

**预期结果**：
1. `getFullOrgNameByUsername(null)` 返回 `DEFAULT_DEPARTMENT`（`基础科技产品部-大数据平台室`，line 41）
2. `getStaffInfoByUsername(null)` 返回 `null`
3. `getFullOrgNameByUsername("nosuchuser123")` 返回 `DEFAULT_DEPARTMENT`（未命中走兜底）
4. 行为与修复前一致（null/未命中兜底逻辑未改动）

**后置清理**：无

---

### 3.5 TC-LEAK-005：原子替换期间并发读不抛异常

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-005 |
| **用例名称** | 原子替换期间并发读不抛异常 |
| **优先级** | P1 |
| **关联 AC** | AC-06 |
| **关联 IT** | - |
| **测试类型** | 集成测试（并发场景） |

**前置条件**：
1. dss-apps-server 运行中，`STAFF_INFO_MAP` 已有数据
2. 并发测试工具可用（多线程模拟查询）

**测试步骤**：
1. 启动 10 个并发线程，持续调用 `getStaffInfoByUsername("zhangsan")`
2. 同时触发 `StaffThread.run()` 执行 `clear()` + `putAll(tempMap)` 原子替换
3. 持续运行 1 分钟，记录是否有异常抛出

**预期结果**：
1. 并发查询**不抛异常**（ConcurrentHashMap 读操作无锁，`synchronized(LOCK)` 仅保护写）
2. 替换瞬间可能返回 `null`/默认值（clear 后 putAll 前的极短窗口，业务可接受）
3. 替换完成后查询返回正确数据
4. 不出现数据不一致（如部分新部分旧 -- 因 clear+putAll 在锁内原子执行）

**校验方式**：
```java
// 伪代码：并发读 + 并发写
ExecutorService pool = Executors.newFixedThreadPool(10);
CountDownLatch latch = new CountDownLatch(1);
// 10 个读线程
for (int i = 0; i < 10; i++) {
    pool.submit(() -> {
        latch.await();
        for (int j = 0; j < 1000; j++) {
            StaffInfo s = getter.getStaffInfoByUsername("zhangsan");
            // 断言不抛异常；s 可能为 null（替换瞬间）或 StaffInfo
        }
    });
}
// 1 个写线程触发刷新
pool.submit(() -> { latch.await(); invokeStaffThreadRun(); });
latch.countDown();
// 期望：无异常抛出
```

**后置清理**：无

---

### 3.6 TC-LEAK-006：AppConn 连续加载/卸载 jar 句柄不增长

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-006 |
| **用例名称** | AppConn 连续加载/卸载 jar 句柄不增长 |
| **优先级** | P0 |
| **关联 AC** | AC-02 |
| **关联 IT** | IT-04 |
| **测试类型** | 集成测试 |

**前置条件**：
1. dss-server 正常运行
2. 已安装至少 1 个 AppConn（如 workflow）
3. 记录初始 jar 文件句柄数：`lsof -p <pid> | grep jar | wc -l` = N0

**测试步骤**：
1. 连续执行 AppConn 加载/卸载 10 次以上：
```bash
for i in $(seq 1 10); do
  sh assembly/bin/appconn-install.sh workflow
  sh assembly/bin/appconn-refresh.sh workflow
  echo "Round $i: $(lsof -p <pid> | grep jar | wc -l)"
done
```
2. 每轮记录 jar 句柄数，确认日志输出 `Closed old URLClassLoader for appConn: ...`
3. 第 10 轮后，dump 堆内存用 MAT 分析 `AppStandardClassUtils` 的 `CLASS_LOADER_MAP.size()`

**预期结果**：
1. jar 文件句柄数在 10 次刷新后**保持稳定**（允许小幅波动，不单调增长）；修复前会随刷新次数持续累积
2. 日志每轮输出 `Closed old URLClassLoader for appConn: ...`，表明旧 ClassLoader 已 close
3. `AppStandardClassUtils` 的 `CLASS_LOADER_MAP.size()` 与当前已加载 AppConn 数一致（不残留旧 CL）
4. 不出现 `Too many open files` 错误

**校验方式**：
```bash
# 1. jar 句柄数趋势
for i in $(seq 1 10); do
  sh assembly/bin/appconn-refresh.sh workflow
  echo "Round $i jar handles: $(lsof -p <pid> | grep jar | wc -l)"
done
# 期望：末轮句柄数 ≈ N0，不单调增长

# 2. close 日志
grep "Closed old URLClassLoader for appConn" logs/dss-server.log | wc -l
# 期望：≥ 10（每次 refresh 至少 1 条）
```

**后置清理**：无

---

### 3.7 TC-LEAK-007：close 旧 ClassLoader 日志验证

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-007 |
| **用例名称** | close 旧 ClassLoader 日志验证 |
| **优先级** | P1 |
| **关联 AC** | AC-02 |
| **关联 IT** | IT-03 |
| **测试类型** | 集成测试 |

**前置条件**：
1. dss-server 正常运行，已加载某 AppConn（如 workflow）
2. 日志级别 INFO（`Closed old URLClassLoader` 为 `LOGGER.info`）

**测试步骤**：
1. 执行 `appconn-refresh.sh workflow` 触发 `CommonAppConnLoader.getAppConn` -> `refreshClassloader`
2. 检查日志是否输出 `Closed old URLClassLoader for appConn: ...`
3. 重复刷新，确认每次替换旧 CL 都有 close 日志

**预期结果**：
1. 每次 `refreshClassloader` 替换 ClassLoader 时输出 `LOGGER.info("Closed old URLClassLoader for appConn: {}", classLoader)`
2. AppConn 正常加载并执行（close 旧 CL 不影响新 CL 使用）
3. 返回的 ClassLoader 为新建实例（`refreshClassloader_NewClassLoaderReturned`）

**校验方式**：
```bash
grep "Closed old URLClassLoader for appConn" logs/dss-server.log | tail -3
# 期望：每次 refresh 有 1 条 INFO 日志
```

**后置清理**：无

---

### 3.8 TC-LEAK-008：close 异常降级 warn 不阻断刷新

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-008 |
| **用例名称** | close 异常降级 warn 不阻断刷新 |
| **优先级** | P1 |
| **关联 AC** | AC-05 |
| **关联 IT** | IT-06 |
| **测试类型** | 单元测试 |

**前置条件**：
1. 单元测试环境已搭建（JUnit 5 + Mockito）
2. `AppStandardClassUtils` 已注入 mock 的 ClassLoader

**测试步骤**：

单元测试代码（伪代码）：

```java
@Test
@DisplayName("close抛IOException-降级warn不阻断refresh")
void testCloseClassLoader_IOExceptionLoggedAsWarn() throws Exception {
    // Given：mock 一个 URLClassLoader，close() 抛 IOException
    URLClassLoader mockCL = mock(URLClassLoader.class);
    doThrow(new IOException("jar deleted")).when(mockCL).close();
    // CLASS_LOADER_MAP 预置该 CL
    putToClassLoaderMap("test-appconn", mockCL);

    // When：触发 refreshClassloader
    ClassLoader newCL = AppStandardClassUtils.refreshClassloader("test-appconn",
            () -> new URLClassLoader(new URL[0], getClass().getClassLoader()));

    // Then
    assertNotNull(newCL);                      // refresh 正常返回新 CL
    assertNotSame(mockCL, newCL);              // 新 CL != 旧 CL
    verify(mockCL, times(1)).close();          // close 被调用
    // 日志断言：LOGGER.warn("Failed to close old URLClassLoader for appConn", e)
    // 可用 LogCaptor 或 mock Logger 验证 warn 被调用
}
```

**预期结果**：
1. `closeClassLoader` 捕获 IOException，降级为 `LOGGER.warn("Failed to close old URLClassLoader for appConn", e)`
2. `refreshClassloader` 正常返回新 ClassLoader，**不抛异常**（close 失败不阻断主流程）
3. `verify(mockCL).close()` 验证 close 被调用 1 次

**后置清理**：无（单元测试自动清理）

---

### 3.9 TC-LEAK-009：URLJarFile 实例数下降

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-009 |
| **用例名称** | URLJarFile 实例数下降 |
| **优先级** | P1 |
| **关联 AC** | AC-03 |
| **关联 IT** | IT-07 |
| **测试类型** | 集成测试（MAT 分析） |

**前置条件**：
1. 在 prod 或类 prod 环境运行 dss-server，已完成 TC-LEAK-006 的 AppConn 反复加载/卸载（10 次以上）
2. 修复前 baseline：prod 环境 568 个 `URLJarFile` 实例占用 9,190,544 bytes（10.29%）

**测试步骤**：
1. 在 SIT 环境连续运行 24 小时（含多次 AppConn 加载/卸载）
2. dump 堆内存：
```bash
jmap -dump:live,format=b,file=/tmp/dss_leak009.hprof <pid>
```
3. 用 MAT 打开，分析 `sun.net.www.protocol.jar.URLJarFile` 实例数与占用

**预期结果**：
1. `URLJarFile` 实例数较修复前（prod 568 实例）**显著下降**
2. `URLJarFile` 占用**不再为 Problem Suspect**（修复前 9,190,544 bytes / 10.29%）
3. 连续刷新后实例数稳定（不随 AppConn 刷新次数累积）

**校验方式**：
- MAT -> Histogram -> 过滤 `URLJarFile`，对比修复前后实例数与 retained size

**后置清理**：无

---

### 3.10 TC-LEAK-010：非 URLClassLoader 不 close

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-LEAK-010 |
| **用例名称** | 非 URLClassLoader 不 close |
| **优先级** | P1 |
| **关联 AC** | AC-05 |
| **关联 IT** | - |
| **测试类型** | 单元测试 |

**前置条件**：
1. 单元测试环境已搭建（JUnit 5 + Mockito）

**测试步骤**：

单元测试代码（伪代码）：

```java
@Test
@DisplayName("传入非URLClassLoader-不抛异常不调用close")
void testRefreshClassloader_NonUrlClassLoader_NoClose() throws Exception {
    // Given：mock 一个非 URLClassLoader 的 ClassLoader
    ClassLoader nonUrlCL = mock(ClassLoader.class);
    putToClassLoaderMap("test-appconn2", nonUrlCL);

    // When
    ClassLoader newCL = AppStandardClassUtils.refreshClassloader("test-appconn2",
            () -> new URLClassLoader(new URL[0], getClass().getClassLoader()));

    // Then
    assertNotNull(newCL);
    // 非 URLClassLoader，closeClassLoader 中 instanceof 判断为 false，不调用 close
    // nonUrlCL 无 close() 方法（ClassLoader 无 close），验证不抛 ClassCastException
}
```

**预期结果**：
1. `closeClassLoader` 判断 `instanceof URLClassLoader` 为 false 时，**不调用 close**，直接返回
2. **不抛 ClassCastException**
3. `refreshClassloader` 正常返回新 ClassLoader

**设计依据**：`closeClassLoader` 的 `instanceof` 防御性判断，避免传入非 URLClassLoader（如 AppClassLoader）时异常。当前 `CLASS_LOADER_MAP` 存储的均为 `AppConnClassLoader`（继承 URLClassLoader），但防御性判断为未来扩展留余地。

**后置清理**：无

---

## 四、回归测试用例

### 4.1 TC-REG-001：员工信息 5 个查询接口回归

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-REG-001 |
| **用例名称** | 员工信息 5 个查询接口回归 |
| **优先级** | P0 |
| **关联 AC** | AC-04 |
| **关联 IT** | IT-05 |
| **测试类型** | 接口测试（回归） |

**前置条件**：
1. `StaffThread` 至少成功刷新 1 次，`STAFF_INFO_MAP` 已填充当前全量员工数据

**测试步骤**：
1. 调用 `getStaffInfoByUsername("zhangsan")`（命中）
2. 调用 `getAllUsers()`，校验 List 大小
3. 调用 `getAllUsernames()`，校验 Set 大小
4. 调用 `getAllDepartments()`，校验部门列表
5. 调用 `getFullOrgNameByUsername("zhangsan")`，校验组织名

**预期结果**：
1. `getStaffInfoByUsername` 返回 `StaffInfo` 或 `null`
2. `getAllUsers()` 返回 `List<StaffInfo>`，size = `STAFF_INFO_MAP.size()`
3. `getAllUsernames()` 返回 `Set<String>`，size = `STAFF_INFO_MAP.size()`
4. `getAllDepartments()` 返回 `List<String>`，来自当前全量数据（去重）
5. `getFullOrgNameByUsername` 返回 orgFullName 或 `DEFAULT_DEPARTMENT`
6. 行为与修复前完全一致（5 个查询方法均未改动，仅 `StaffThread.run()` 内部写入逻辑变化）

**后置清理**：无

---

### 4.2 TC-REG-002：AppConn 热部署功能回归

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-REG-002 |
| **用例名称** | AppConn 热部署功能回归 |
| **优先级** | P0 |
| **关联 AC** | AC-05 |
| **关联 IT** | IT-03 |
| **测试类型** | 集成测试（回归） |

**前置条件**：
1. 已安装至少 2 个 AppConn（workflow、scriptis）
2. DSS 前端可访问，工作流编辑器可用

**测试步骤**：
1. 执行 `appconn-refresh.sh` 热部署刷新所有 AppConn
2. 在 DSS 前端创建新工作流，拖入 workflow 节点（依赖 workflow AppConn）
3. 编辑工作流，拖入 scriptis 节点（依赖 scriptis AppConn）
4. 执行工作流，观察是否正常运行
5. 检查日志是否有 `ClassNotFoundException` / `NoClassDefFoundError`

**预期结果**：
1. AppConn 刷新成功，无 `ClassNotFoundException` / `NoClassDefFoundError`
2. 工作流创建/编辑/执行功能正常
3. 第三方系统（Schedulis/Visualis/Qualitis）集成功能正常
4. close 旧 ClassLoader 不影响新 CL 的类加载（`refreshClassloader` 返回新 CL 后用新 CL 加载类）

**校验方式**：
```bash
grep -E "ClassNotFoundException|NoClassDefFoundError" logs/dss-server.log | tail -5
# 期望：无新增异常
grep "Loaded appConn" logs/dss-server.log | tail -3
# 期望：各 AppConn 正常加载
```

**后置清理**：无

---

### 4.3 TC-REG-003：HDFS/ViewFS 功能回归

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-REG-003 |
| **用例名称** | HDFS/ViewFS 功能回归 |
| **优先级** | P1 |
| **关联 AC** | AC-07 |
| **关联 IT** | - |
| **测试类型** | 集成测试（回归） |

**前置条件**：
1. DSS 侧 HDFS 访问调用点正常（ViewFileSystem/Configuration 的 Linkis 待协调项不影响 DSS 侧功能）

**测试步骤**：
1. 触发工作流产物读写（如保存工作流到 HDFS）
2. 触发结果导出（如 Scriptis 查询结果导出）
3. 观察 HDFS 读写是否正常

**预期结果**：
1. HDFS 读写功能正常
2. DSS 自身 HDFS 访问不受本次修复影响
3. **边界外部分如实标注**：ViewFileSystem/Configuration 的堆内存治理部分依赖 Linkis 侧配置（如 `fs.viewfs.impl.disable.cache=true`），本次不强制量化验收（见设计 4.4 待协调项）

**后置清理**：无

---

## 五、回归测试范围（基于设计 7.3）

| 范围 | 测试要点 | 关联用例 |
|------|---------|---------|
| 员工信息查询 | `getStaffInfoByUsername`、`getAllUsers`、`getAllDepartments`、`getFullOrgNameByUsername`、`getAllUsernames` 返回正常 | TC-LEAK-003、TC-LEAK-004、TC-REG-001 |
| AppConn 加载 | `CommonAppConnLoader.getAppConn` 正常加载各 AppConn（workflow、scriptis 等） | TC-LEAK-006、TC-LEAK-007、TC-REG-002 |
| AppConn 执行 | 加载后的 AppConn 可正常执行工作流节点 | TC-REG-002 |
| AppConn 热部署 | `appconn-install.sh` + `appconn-refresh.sh` 热加载后正常工作 | TC-LEAK-006、TC-REG-002 |
| HDFS / ViewFS 功能 | DSS 自身 HDFS 访问正常（ViewFileSystem 待协调项不影响 DSS 侧功能回归） | TC-REG-003 |

---

## 六、验收标准覆盖矩阵

### 6.1 需求 AC 覆盖检查

| 验收标准 | 描述 | 关联用例 | 覆盖状态 |
|---------|------|---------|:--------:|
| AC-01 | HttpStaffInfoGetter 刷新后内存不增长 | TC-LEAK-001、TC-LEAK-002 | ✅ 完全覆盖 |
| AC-02 | AppConn 反复加载/卸载后 jar 句柄不增长 | TC-LEAK-006、TC-LEAK-007 | ✅ 完全覆盖 |
| AC-03 | URLJarFile 实例数下降 | TC-LEAK-009 | ✅ 完全覆盖 |
| AC-04 | 员工信息查询接口回归 | TC-LEAK-003、TC-LEAK-004、TC-REG-001 | ✅ 完全覆盖 |
| AC-05 | AppConn 功能回归 | TC-LEAK-008、TC-LEAK-010、TC-REG-002 | ✅ 完全覆盖 |
| AC-06 | 原子替换并发可见性 | TC-LEAK-005 | ✅ 完全覆盖 |
| AC-07 | HDFS/ViewFS 功能回归 | TC-REG-003 | ✅ 完全覆盖（边界外如实标注） |

**AC 覆盖率**：7/7 = 100%

### 6.2 设计 IT 集成测试场景覆盖检查

| 集成测试场景 | 描述 | 关联用例 | 覆盖状态 |
|------------|------|---------|:--------:|
| IT-01 | 员工信息刷新主场景 | TC-LEAK-001 | ✅ 完全覆盖 |
| IT-02 | 连续刷新不累积 | TC-LEAK-001 | ✅ 完全覆盖 |
| IT-03 | AppConn 加载主场景 | TC-LEAK-007、TC-REG-002 | ✅ 完全覆盖 |
| IT-04 | AppConn 连续加载/卸载 | TC-LEAK-006 | ✅ 完全覆盖 |
| IT-05 | 员工信息查询回归 | TC-LEAK-003、TC-LEAK-004、TC-REG-001 | ✅ 完全覆盖 |
| IT-06 | close 异常不阻断 | TC-LEAK-008 | ✅ 完全覆盖 |
| IT-07 | MAT heap dump 验证 | TC-LEAK-009 | ✅ 完全覆盖 |

**IT 覆盖率**：7/7 = 100%

### 6.3 单元测试覆盖点检查（设计 7.1）

| 单元测试方法 | 覆盖场景 | 关联用例 | 覆盖状态 |
|------------|---------|---------|:--------:|
| testRefresh_StaffInfoMapReplacedNotAccumulated | 连续两次刷新，key 集合不同 | TC-LEAK-001 | ✅ |
| testRefresh_FailureKeepsOldData | ESB 请求异常 | TC-LEAK-002 | ✅ |
| testGetStaffInfoByUsername_HitAndMiss | 查询命中与未命中 | TC-LEAK-003 | ✅ |
| testGetAllUsers_ReturnsCurrentSnapshot | 刷新后查询全量 | TC-REG-001 | ✅ |
| testConcurrentReadDuringRefresh | 刷新期间并发读 | TC-LEAK-005 | ✅ |
| testRefreshClassloader_ClosesOldClassLoader | refresh 替换 ClassLoader | TC-LEAK-007 | ✅ |
| testRefreshClassloader_NewClassLoaderReturned | refresh 返回新 CL | TC-LEAK-007 | ✅ |
| testRefreshClassloader_NonUrlClassLoader_NoClose | 传入非 URLClassLoader | TC-LEAK-010 | ✅ |
| testCloseClassLoader_IOExceptionLoggedAsWarn | close 抛 IOException | TC-LEAK-008 | ✅ |
| testGetClassLoader_ConcurrentSameName_SingleInstance | 并发 getClassLoader 同一 name | TC-LEAK-006 | ✅ |

**单元测试覆盖率**：10/10 = 100%

### 6.4 决策与边界场景覆盖检查

| 决策/场景 | 描述 | 关联用例 | 覆盖状态 |
|----------|------|---------|:--------:|
| 决策一（原子替换不引入 Caffeine） | tempMap + clear + putAll 原子替换 | TC-LEAK-001 | ✅ |
| 决策二（close 时机与异常降级） | remove 后 close；IOException 降级 warn | TC-LEAK-007、TC-LEAK-008 | ✅ |
| 决策三（URLJarFile 随 close 解决） | URLJarFile 实例数下降 | TC-LEAK-009 | ✅ |
| 决策四（ViewFileSystem 待协调） | HDFS/ViewFS 边界外如实标注 | TC-REG-003 | ✅ |
| 边界场景-刷新失败保留旧数据 | ESB 异常时不清空 Map | TC-LEAK-002 | ✅ |
| 边界场景-null 入参兜底 | null/未命中返回默认部门 | TC-LEAK-004 | ✅ |
| 不新增 removeClassLoader | 无该方法（设计已决定不预留死代码） | - | ✅ 不涉及测试 |

---

## 七、测试执行记录

| 用例编号 | 执行结果 | 执行人 | 执行日期 | 备注 |
|---------|---------|-------|---------|------|
| TC-LEAK-001 | | | | |
| TC-LEAK-002 | | | | |
| TC-LEAK-003 | | | | |
| TC-LEAK-004 | | | | |
| TC-LEAK-005 | | | | |
| TC-LEAK-006 | | | | |
| TC-LEAK-007 | | | | |
| TC-LEAK-008 | | | | |
| TC-LEAK-009 | | | | |
| TC-LEAK-010 | | | | |
| TC-REG-001 | | | | |
| TC-REG-002 | | | | |
| TC-REG-003 | | | | |

---

## 八、测试总结

### 8.1 测试用例统计

| 类型 | 数量 | P0 | P1 |
|------|:----:|:--:|:--:|
| 主修复对象 | 10 | 3 | 7 |
| 回归测试 | 3 | 2 | 1 |
| **合计** | **13** | **5** | **8** |

### 8.2 验收标准

- 所有 P0 用例必须通过（TC-LEAK-001、TC-LEAK-003、TC-LEAK-006、TC-REG-001、TC-REG-002）
- P1 用例通过率 ≥ 90%
- 无严重缺陷遗留
- AC-01 ~ AC-07 全部覆盖（覆盖率 100%）
- IT-01 ~ IT-07 全部覆盖（覆盖率 100%）
- 单元测试方法 10/10 全部覆盖

### 8.3 测试通过判据

1. **HttpStaffInfoGetter 缓存治理**：连续刷新 5 次后 `STAFF_INFO_MAP.size()` 不单调增长，MAT 分析 `ConcurrentHashMap$Node[]` 占用回落至单份全量数据大小。
2. **AppStandardClassUtils ClassLoader 释放**：AppConn 连续加载/卸载 10 次以上，`lsof -p <pid> | grep jar | wc -l` 保持稳定，日志输出 `Closed old URLClassLoader`，MAT 分析 `URLJarFile` 实例数显著下降。
3. **回归不破坏**：员工信息查询 5 接口、AppConn 加载/热部署/执行、HDFS 访问功能与修复前一致。

---

**文档版本**：v1.0
**创建日期**：2026-08-04
**需求关联**：[DSS内存泄漏修复_需求](../requirements/DSS内存泄漏修复_需求.md)
**设计关联**：[DSS内存泄漏修复_设计](../design/DSS内存泄漏修复_设计.md)
