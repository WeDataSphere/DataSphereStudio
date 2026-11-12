# 【低风险】【系统优化】【DSS】【安全漏洞】【后端需求】c3p0组件升级

| 项 | 内容 |
|-----|-----|
| 需求名称 | 【低风险】【系统优化】【DSS】【安全漏洞】【后端需求】c3p0组件升级 |
| 所属版本 | DSS 1.26.0（分支 `dev-1.26.0`） |
| 需求编号 | REQ-02 |
| DPMS Story | [559049](http://dpms.weoa.com/#/product/100199/story/detail/559049)（status=3 实现中，releasePlanId=181933 DSS 1.26.0，priority=1 高，创建/负责人 ryanchen） |
| 文档版本 | v1.0 |
| 创建日期 | 2026-11-13 |
| 功能属性 | 后端（依赖治理，无业务代码改动） |
| 设计文档 | [依赖包升级_netty-all_c3p0_jackson-databind_设计.md](../design/依赖包升级_netty-all_c3p0_jackson-databind_设计.md) + 本文档 §收口章节 |
| 关联工单 | netty 组件升级（[559050](http://dpms.weoa.com/#/product/100199/story/detail/559050)，REQ-03） |

---

## 一、需求背景

c3p0 存在 CVE-2026-55223、CVE-2018-20433、CVE-2019-5427 等 CVE：DataSource 与 ConnectionPoolDataSource 的 getConnection()/getPooledConnection() 被 JavaBean 工具当作属性读取，攻击者利用反序列化入口触发 JDBC 驱动调用，导致反序列化代码注入（gadget sink，可与 classpath 上其他 gadget 库组合实现 RCE）。

DSS 源码不直接使用 c3p0（无 `import com.mchange`），c3p0 为 linkis-jobhistory 的传递依赖，随 dss-flow-execution-server 打包进运行时 classpath（job 历史数据读写的数据库连接池）。

## 二、需求描述

将 c3p0 升级至 **0.14.0**（CVE-2026-55223 官方修复版本），分两段实施：

### 2.1 第一段（已完成，commit b5e1fac60，2026-11-05）

dss-flow-execution-server 模块级钉版本：linkis-jobhistory 依赖新增 c3p0 排除项（与既有 mchange-commons-java 排除并列），显式依赖 `com.mchange:c3p0:0.14.0`。

### 2.2 第二段收口（本次，commit 8dde410a6，工单核验补充口径）

按工单 559049 核验补充要求：

1. **根 POM 统一管理 c3p0 及支持库**：根 pom.xml `dependencyManagement` 新增 c3p0、mchange-commons-java 管理（版本属性 `c3p0.version=0.14.0`、`mchange.commons.java.version=0.6.0`）；
2. **mchange-commons-java 0.4.0 → 0.6.0**：按 c3p0 0.14.0 官方 POM 配套对齐（flow-execution-server 既有钉 0.4.0 一并升级）；
3. **flow-execution-server 引用统一属性**：模块内 c3p0/mchange-commons-java 版本号改为 `${c3p0.version}`/`${mchange.commons.java.version}`，保留 linkis-jobhistory 传递排除项；
4. 不变更业务接口或数据结构。

## 三、验收标准

| 编号 | 验收条件 |
|------|---------|
| AC-1 | dependency:tree 确认 dss-flow-execution-server 内 c3p0=0.14.0、mchange-commons-java=0.6.0，无 0.9.5.x/0.4.0 残留（已验证 ✅） |
| AC-2 | flow-execution-server 编译通过（已验证 ✅） |
| AC-3 | Linux 构建机全量 `mvn clean package -DskipTests` 成功（c3p0 0.14.0/mchange 0.6.0 为内部镜像版本，需构建机依赖内部 Nexus——R-03 验证） |
| AC-4 | dss-flow-execution-server 启动无 ClassNotFound/NoSuchMethod 错误；jobhistory 初始化正常（c3p0 连接池创建成功） |
| AC-5 | 打包产物 lib 目录无 c3p0 0.9.5.5；安全扫描 CVE-2026-55223 不再命中 |
| AC-6 | 工作流端到端回归：提交 → 轮询状态 → 完成（jobhistory 落库链路） |

## 四、范围外

- dss-appconn-core 的 jackson 2.14.1 属性遮蔽（工单标注纳入后续迭代）；
- 其他 server 模块（c3p0 仅随 flow-execution-server 打包）；
- Druid（DSS 业务库连接池，与 c3p0 无关）。

## 五、风险与回滚

| 风险 | 等级 | 缓解 |
|------|:---:|------|
| c3p0 0.14.0 / mchange 0.6.0 为内部镜像版本（推断），公网/离线环境无法解析 | 中 | Linux 构建机 dependency:tree 验证；发布文档标注内部仓库依赖 |
| c3p0 0.14.0 配置项兼容性 | 低 | 0.14.0 API（ComboPooledDataSource/配置项）稳定；c3p0 为 linkis-jobhistory 内部使用，DSS 不直接编码调用 |

回滚：移除 flow-execution-server 排除项与显式依赖（恢复传递解析）+ 根 pom 移除两段管理（ revert 单提交），重新打包即可，无数据/配置迁移。

## 六、测试关注点

依赖配套（c3p0 0.14.0 + mchange 0.6.0 成对）、JobHistory 初始化与读写、工作流提交和状态轮询、失败/超时与整包回滚。缺实际发布包扫描时不宣称漏洞已清零；平台 SDL 已回读为不涉及（开源组件升级）。
