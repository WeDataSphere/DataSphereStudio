# 【低风险】【系统优化】【DSS】【安全漏洞】【后端需求】netty组件升级

| 项 | 内容 |
|-----|-----|
| 需求名称 | 【低风险】【系统优化】【DSS】【安全漏洞】【后端需求】netty组件升级 |
| 所属版本 | DSS 1.26.0（分支 `dev-1.26.0`） |
| 需求编号 | REQ-03 |
| DPMS Story | [559050](http://dpms.weoa.com/#/product/100199/story/detail/559050)（status=3 实现中，releasePlanId=181933 DSS 1.26.0，priority=1 高，创建/负责人 ryanchen） |
| 文档版本 | v1.0 |
| 创建日期 | 2026-11-13 |
| 功能属性 | 后端（依赖治理，无业务代码改动） |
| 设计文档 | [依赖包升级_netty-all_c3p0_jackson-databind_设计.md](../design/依赖包升级_netty-all_c3p0_jackson-databind_设计.md) + 本文档 §收口章节 |
| 关联工单 | c3p0 组件升级（[559049](http://dpms.weoa.com/#/product/100199/story/detail/559049)，REQ-02） |

---

## 一、需求背景

netty 当前使用版本（4.2.5.Final）存在多个 CVE 漏洞（CVE-2023-44487、CVE-2022-41881、CVE-2020-11612、CVE-2019-20445、CVE-2021-37137 及 2026-05 批次 CVE-2026-42577/42579/42584/42582/42586 等），存在环境问题漏洞、可导致拒绝服务攻击等。

DSS 源码不直接使用 netty（无 `import io.netty`）；netty 由 Linkis（RPC 通信链路，linkis-common 等）传递使用，netty 版本影响所有最终分发包内的 DSS↔Linkis 通信层。版本控制策略为"模块内排除 + 打包层统一"：多个 server 模块对 netty-all 做 exclusion，由 assembly/dss-package 在打包时统一提供 netty 版本。

## 二、需求描述

将 netty-all 升级至 **4.2.13.Final**（覆盖 2026-05 批次安全修复），本次实施（工单 559050 核验补充口径，commit 8dde410a6）：

1. **根 POM 导入 netty-bom**：`dependencyManagement` 导入 `io.netty:netty-bom:${netty.version}`（4.2.13.Final），统一管理 netty 分拆模块版本，消除传递链多版本混装；
2. **主仓库 assembly/dss-package 显式引入 netty-all 4.2.13.Final**：补齐主仓库与同步仓库（dss-sync-demo，commit 1bbbf7397 已钉）的钉版本缺口——同步状态清单（设计文档 §6.1）要求的发布前对齐项；
3. **保留既有排除项**：各 server 模块与 dss-package 的既有 netty 排除不变；
4. 不变更业务接口或数据结构。

> 背景事实（设计文档已核实）：主仓库 assembly/dss-package 原无 netty-all 直接依赖（仅 netty 排除项），4.2.13.Final 钉版本此前只存在于同步仓库——本次收口消除双线构建产物安全基线不一致（R-06）。

## 三、验收标准

| 编号 | 验收条件 |
|------|---------|
| AC-1 | dependency:tree 确认 netty-all=4.2.13.Final（已验证 ✅，provided 链路） |
| AC-2 | 根 pom netty-bom 导入生效，netty 分拆模块（netty-common/codec/handler 等）由 bom 统一裁决为 4.2.13.Final，无 4.2.5/4.2.7 残留 |
| AC-3 | assembly/dss-package 打包产物 lib 目录 netty-all 为 4.2.13.Final，与同步仓库产物一致 |
| AC-4 | 公共及各服务 lib 无旧 Netty 模块混装 |
| AC-5 | Linkis RPC 回归：长连接、大报文、断连/超时、工作流提交与状态轮询 |
| AC-6 | 安全扫描 netty 2026-05 批次 CVE（CVE-2026-42577/42579/42584/42582/42586）不再命中 |

## 四、范围外

- netty 4.2.15+ 的 HTTP 请求走私类修复（工单 R-04 标注跟踪后续版本，2026-11-02 已发布 4.2.20.Final，评估后另行升级）；
- io_uring 传输（需 JDK 9+，DSS 未使用，JDK 8 基线不变）。

## 五、风险与回滚

| 风险 | 等级 | 缓解 |
|------|:---:|------|
| netty 4.2.13 安全修复新增校验/资源限制，极端报文可能被拒 | 低 | RPC 回归覆盖大报文/长连接场景 |
| 4.2.13 之后又披露 HTTP 请求走私类问题（4.2.15+ 修复） | 低 | 跟踪 netty 后续版本，评估升级至 4.2.20.Final |

回滚：assembly/dss-package 移除 netty-all 依赖 + 根 pom 移除 netty-bom 导入（revert 单提交），版本回退至传递解析，重新打包即可，无数据/配置迁移。

## 六、测试关注点

检查公共及各服务 lib 无旧 Netty 模块混装，覆盖 Linkis RPC 长连接、大报文、断连/超时、工作流提交与状态轮询。缺实际发布包扫描时不宣称漏洞已清零；平台 SDL 已回读为不涉及（开源组件升级）。
