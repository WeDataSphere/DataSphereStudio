# REQ-02 / REQ-03 安全组件升级（c3p0 / netty）— 测试方案

> 关联需求：REQ-02 c3p0 组件升级（[559049](http://dpms.weoa.com/#/product/100199/story/detail/559049)）、REQ-03 netty 组件升级（[559050](http://dpms.weoa.com/#/product/100199/story/detail/559050)）
> 设计文档：[依赖包升级_netty-all_c3p0_jackson-databind_设计.md](../design/依赖包升级_netty-all_c3p0_jackson-databind_设计.md)（§6.2 验证步骤 / §6.3 回滚 / §6.4 收口记录）
> 实施提交：b5e1fac60（c3p0 钉版本，第一段）+ 8dde410a6（根 POM 统一管理/netty-bom/dss-package 钉版本，收口段）
> 生成：2026-11-13，dev-flow 流程文档补齐

## 一、测试范围与环境

| 环境 | 用途 | 前置条件 |
|------|------|---------|
| dev | 冒烟 | Linux 构建机全量打包成功（含内部镜像依赖解析） |
| uat | 验证 | 工作流执行链路可用（flow-execution-server + Linkis） |
| sit | 提测 | 测试集关联（testSetId 待 A5 补录） |

变更性质：**pom 依赖治理，无业务代码改动**——测试重点是"依赖正确性 + 运行时兼容 + 受影响链路回归"，无需新功能用例。

## 二、依赖正确性验证（构建期）

| 编号 | 验证项 | 命令/方法 | 通过标准 | 状态 |
|------|--------|----------|---------|:----:|
| V-1 | c3p0 版本解析 | `mvn dependency:tree -pl dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server -Dincludes=com.mchange` | c3p0=0.14.0 且 mchange-commons-java=0.6.0，无 0.9.5.x/0.4.0 残留 | ✅ 本机已过 |
| V-2 | netty 版本裁决 | `mvn dependency:tree -pl assembly/dss-package -Dincludes=io.netty` | netty-all=4.2.13.Final；分拆模块（codec/handler/common 等）无 4.2.5/4.2.7 混装 | ✅ 本机已过 + dev 部署包审计过（dss-commons 全部 netty 模块统一 4.2.13.Final，零混装） |
| V-3 | Linux 构建机全量打包 | `mvn clean package -DskipTests` | 成功（R-03：c3p0 0.14.0/mchange 0.6.0 为内部镜像版本，构建机依赖内部 Nexus） | ✅ PACE流水线1039300005095610 Linux构建机编译成功（build#367/#342）并部署 dev（2026-11-16 16:30 重启生效） |
| V-4 | 打包产物依赖审计 | 解包最终分发包检查 lib | c3p0-0.14.0 + mchange-commons-java-0.6.0 成对（dss-server lib 实测）；netty-all+全部分拆模块统一 4.2.13.Final 零混装（dss-commons 347 jars 实测）；jackson databind/core=2.18.12。⚠️ 遗留观察项：dss-server lib 中 jackson-annotations-2.15.0 与 2.18.12 不齐（dss-commons 侧已对齐；服务启动+REST 序列化正常，风险低，记录跟踪） | ✅（2026-11-16 dev 部署包实测） |
| V-5 | 安全扫描 | 对打包产物执行依赖漏洞扫描 | CVE-2025-52999、CVE-2026-110879、CVE-2026-55223 及 netty 2026-05 批次不再命中（缺实际发布包扫描时不宣称漏洞已清零） | ⏳ |

## 三、运行时验证（启动 + 功能回归）

| 编号 | 验证项 | 场景 | 通过标准 |
|------|--------|------|---------|
| R-1 | flow-execution-server 启动 | 部署后启动 | ✅ DSS-SERVER-DEV/PROD、DSS-APPS-SERVER 2026-11-16 16:12-16:14 全新注册 eureka UP 心跳正常；dss-server-dev.out 扫描 NoSuchMethod=0、ClassNotFound=0、c3p0 异常=0 |
| R-2 | JobHistory 读写 | 工作流提交后查历史 | job 历史数据正常落库/查询（c3p0 链路） |
| R-3 | Linkis RPC 回归（netty） | 工作流提交 → 轮询状态 → 完成 | 长连接稳定；大报文传输正常（R-04 极端报文拒绝场景覆盖）；断连/超时后重连正常 |
| R-4 | REST 序列化回归（jackson，附带验证） | 登录、工作流 CRUD、orchestrator 列表 | ✅ 2026-11-16 REQ-01 冒烟请求经 apps-server 的 200/400 JSON 响应序列化正常（jackson 2.18.12 实际承载） |
| R-5 | 大 JSON 场景（R-05） | 超长节点上下文读写 | 2.15+ StreamReadConstraints 默认限制（嵌套深度 1000）不误伤业务报文 |
| R-6 | 端到端 | 工作流完整执行链路 | 提交 → 执行 → 完成 → 历史可查（覆盖 flow-execution-server + Linkis RPC/netty + jobhistory 落库/c3p0 全链） |

## 四、回滚验证

| 编号 | 场景 | 操作 | 验证点 |
|------|------|------|--------|
| RB-1 | c3p0 回滚 | revert 8dde410a6 + b5e1fac60（恢复传递解析） | 重新打包后 c3p0 回 0.9.5.x，启动正常 |
| RB-2 | netty 回滚 | revert 8dde410a6（dss-package 移除 netty-all + 根 pom 移除 bom 导入） | 重新打包后 netty 回传递版本，Linkis RPC 正常 |
| RB-3 | 回滚决策依据 | 6.2-3 启动失败 / 6.2-4 功能回归失败现象为准 | 无数据/配置迁移 |

## 五、测试关注点（工单第四节口径）

1. **依赖配套**：c3p0 0.14.0 + mchange-commons-java 0.6.0 成对验证（单升任一即失败）；
2. **JobHistory 初始化与读写**：c3p0 连接池创建 + job 历史读写；
3. **工作流提交和状态轮询**：netty RPC 链路回归；
4. **失败/超时与整包回滚**：RB-1/RB-2；
5. **SDL**：平台 SDL 已回读为不涉及（开源组件升级）；漏洞清零声明以实际发布包扫描结果为准。

## 六、待办与关联

| # | 项 | 状态 |
|---|---|------|
| 1 | REQ-01（AI 用户切换）测试澄清纪要已单独成稿：[REQ-01_..._测试澄清.md](REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_测试澄清.md) | ✅ |
| 2 | 两个安全工单与 REQ-01 合并同步外网（docs/1.26.0 整目录纳入搬运） | ⏳ patch 搬运时一并执行 |
| 3 | 同步仓库 c3p0 排除+钉版本补齐（设计 §6.1 第 2 行） | ⏳ 同步侧待办 |
| 4 | Linux 构建机全量打包 + 产物审计（V-3/V-4） | ✅ 2026-11-16 随 PACE 流水线部署 dev 完成；安全扫描（V-5）⏳ 待对发布包执行 |
| 5 | DPMS 测试用例同步（12 条：V-1~V-5/R-1~R-6/RB-1~RB-2，testCaseId 52158358~52158386） | ✅ 已关联测试集 327880；story 559049 关联 10 条（c3p0 域）、story 559050 关联 10 条（netty 域），共享用例双向关联 |

## 七、dev 环境验证记录（2026-11-16，执行人 ryanchen）

| 验证项 | 结果 | 证据 |
|--------|------|------|
| V-2 netty 版本裁决（部署包） | ✅ | dss-commons lib：netty-all + buffer/codec-hadoop3/handler/transport/epoll native 等全部分拆模块统一 4.2.13.Final，无 4.2.5/4.2.7；dss-server-dev.out 启动 classpath 打印 15 处 netty jar 全部 4.2.13.Final |
| V-4 c3p0 产物审计 | ✅ | dss-server lib：c3p0-0.14.0.jar + mchange-commons-java-0.6.0.jar 成对；dss-flow-execution-server-1.26.0-20261116.jar 为今日构建；quartz-2.3.2 在位（传递来源确认）；无 0.9.5.x 残留 |
| R-1 启动验证 | ✅ | 三 DSS 服务 16:30 重启后 eureka 注册 UP 心跳正常；NoSuchMethod=0 / ClassNotFound=0 / c3p0 异常=0 |
| R-4 REST 序列化 | ✅ | REQ-01 冒烟流量（setProxyUser 200/400）经 apps-server 正常序列化 |
| netty 运行日志 | ✅ | 仅 1 条无害 WARN（hscan RASP shaded netty `com.webank.hscan.thirdparty.io.netty` TCP_NODELAY，第三方 RASP 自身命名空间，与本次升级无关） |
| R-2/R-3/R-6 工作流执行链路 | ⏳ | 需实际提交工作流走 提交→执行→轮询→历史查询 全链，留待 uat/sit 正式用例（stacyyan） |
| V-5 安全扫描 | ⏳ | 对发布包执行依赖漏洞扫描后关闭 |
