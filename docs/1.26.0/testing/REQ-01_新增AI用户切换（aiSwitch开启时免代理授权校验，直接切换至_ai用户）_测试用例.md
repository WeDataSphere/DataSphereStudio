# 新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户） 测试用例

> 需求：REQ-01（story [560976](http://dpms.weoa.com/#/product/100199/story/detail/560976)，ENHANCE/P2，功能属性=后端）
> 设计文档：v1.1 | 澄清纪要：[测试澄清](REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_测试澄清.md)
> 生成：dev-flow Stage 6（2026-11-16）| 测试负责人：stacyyan | 测试集：327880（SIT）/ 327881（回归）
> 执行环境递进：dev 冒烟（已完成，见 §五 冒烟执行记录）→ uat 验证 → sit 提测

## 一、被测接口与契约

| 项 | 内容 |
|---|---|
| 接口 | `POST /api/rest_j/v1/dss/scriptis/proxy/setProxyUser`（dss-apps-server，ScriptisProxyUserController 继承 DssProxyUserController） |
| 请求体 | `{"userName": "<当前用户>", "proxyUserName": "<目标代理用户>"}` |
| 成功响应 | `{"status":0,"message":"Success to add proxy user into cookie.","data":{}}` |
| 拒绝响应 | HTTP 400 `{"status":1,"message":"user <userName> have no permission to proxy to user <proxyUserName>",...}` |
| 新增依赖 | `GET {linkis-gateway}/api/rest_j/v1/jobhistory/governanceStationAdmin`（Token 认证，data.aiSwitch 原生布尔，Linkis ≥ release_v2.1.7） |
| 配置项 | `wds.linkis.gateway.url.v1`（fallback `Configuration.getGateWayURL()`）、`wds.dss.linkis.resource.admin.token.*`、`wds.dss.proxy.ai.switch.query.timeout.ms=3000` |

## 二、测试用例总表

### 2.1 功能用例（AC2.x，澄清纪要 F 组）

| 编号 | 用例名称 | 前置数据 | 测试场景 | 测试步骤 | 预期结果 | 验证点 | AC | 关联风险 |
|------|---------|---------|---------|---------|---------|--------|:--:|------|
| TC-F-01 | aiSwitch开启_本人派生_ai豁免切换成功 | Linkis `linkis.ai.user.enable=true`；用户 alexyang 无 alexyang→alexyang_ai 授权记录（dss_proxy_user 无记录） | aiSwitch=true 时豁免 isExists 免授权切换 | ①POST setProxyUser `{"userName":"alexyang","proxyUserName":"alexyang_ai"}` ②DevTools 检查响应 Set-Cookie | 返回 status=0 Success；代理 cookie（dss_user_session_proxy_ticket_id_v1）写入，名/值/maxAge/domain/path 与原授权成功路径产物一致 | 豁免路径切换成功；cookie 产物与基线一致 | AC2.1/AC2.3 | R-002 |
| TC-F-02 | aiSwitch开启_他人派生_ai拒绝 | aiSwitch=true；alexyang 无→otheruser_ai 授权记录 | 豁免范围不放大到他人派生 _ai | POST setProxyUser `{"userName":"alexyang","proxyUserName":"otheruser_ai"}` | HTTP 400 status=1，message="user alexyang have no permission to proxy to user otheruser_ai"（文案与现网一致）；不写 cookie | 他人派生 _ai 走原 isExists 被拒 | AC2.2 | R-002 |
| TC-F-03 | hadoop请求者两拒 | aiSwitch=true；hadoop 会话 | 请求者 hadoop 硬约束 | POST setProxyUser `{"userName":"hadoop","proxyUserName":"hadoop_ai"}` | HTTP 400 拒绝；dss-apps-server 日志无 query aiSwitch / bypass 记录（两拒短路先于外部调用） | 请求者 hadoop 不豁免且不触发 Linkis 调用 | AC2.2 | R-002/R2.1 |
| TC-F-04 | 派生源hadoop两拒 | aiSwitch=true；alexyang 会话且当前代理用户 cookie 为 hadoop（先以授权方式切到 hadoop 或预置 cookie） | 派生源 hadoop 硬约束 | POST setProxyUser `{"userName":"alexyang","proxyUserName":"hadoop_ai"}` | HTTP 400 拒绝；日志无 query aiSwitch 记录 | 派生源 hadoop 的 _ai 目标不豁免 | AC2.2 | R-002/R2.1 |
| TC-F-05 | 已授权用户走原校验不变 | aiSwitch=true；dss_proxy_user 预置 alexyang→otheruser 授权记录 | 有授权记录用户行为零变化 | POST setProxyUser `{"userName":"alexyang","proxyUserName":"otheruser"}` | status=0 Success（原 isExists 命中，与升级前一致） | 非豁免目标正常授权切换不受影响 | AC2.2 | R-002 |
| TC-F-06 | 本人切换分支零触碰 | DS_PROXY_SELF_ENABLE=true | proxyUserName==userName 原分支 | POST setProxyUser `{"userName":"alexyang","proxyUserName":"alexyang"}` | status=0 Success；日志无 query aiSwitch 记录（本人分支零开销） | 本人切换不新增外部调用 | AC2.2 | — |
| TC-F-07 | bypass审计日志留痕 | aiSwitch=true；alexyang 无授权记录 | 豁免命中留痕（R2.3 审计） | TC-F-01 执行后查 dss-apps-server 日志关键字 `bypass proxy authorization by aiSwitch` | 日志含 user=alexyang、target=alexyang_ai 完整字段 | 豁免命中可审计 | AC2.1 | R2.3 |

### 2.2 开关与降级用例（AC3.x，澄清纪要 D 组）

| 编号 | 用例名称 | 前置数据 | 测试场景 | 测试步骤 | 预期结果 | 验证点 | AC | 关联风险 |
|------|---------|---------|---------|---------|---------|--------|:--:|------|
| TC-D-01 | 开关关闭走原校验 | Linkis `linkis.ai.user.enable=false`（默认值，热加载生效） | aiSwitch=false 全量请求无豁免副作用 | ①POST alexyang→alexyang_ai ②POST alexyang→otheruser（有授权）③POST alexyang→alexyang（本人） | ①HTTP 400 拒绝（无授权，与升级前基线一致）②③status=0（原行为）；全部请求日志无 bypass 记录 | 开关关闭=豁免链路整体旁路 | AC3.2 | R-002 |
| TC-D-02 | Linkis不可达降级 | aiSwitch 环境本应为 true；网络隔离/停 linkis-mg | 七情形降级：网络不通 | ①POST alexyang→alexyang_ai ②查 dss-apps-server warn 日志 | 降级走原 isExists：HTTP 400 拒绝（无授权），响应文案与基线一致；warn 日志含完整 URL/异常类型/耗时毫秒 | 降级不抛异常不阻断；留痕三要素齐全 | AC3.1 | R-001/R1.2 |
| TC-D-03 | Token失效降级 | 临时改错 `wds.dss.linkis.resource.admin.token.value` 后重启/热更 | 七情形降级：401/403 | POST alexyang→alexyang_ai；查 warn 日志；**测后立即恢复正确 Token** | 降级走原校验 HTTP 400 拒绝；warn 含 401/403 异常类型 | 认证失败不阻断原链路 | — | R-004 |
| TC-D-04 | 低版本Linkis字段缺失降级 | 目标环境 Linkis < release_v2.1.7（响应无 aiSwitch 字段） | Gson 缺失字段 null→false | POST alexyang→alexyang_ai；查 warn 日志 | 降级走原校验；warn 含 field missing 类描述 | 旧版 Linkis 共存行为确定性一致 | — | R1.2 |
| TC-D-05 | 查询超时降级+耗时上限 | aiSwitch=true；对 linkis-mg 注入 ≥3000ms 延迟（tc/netem 或停 jobhistory 服务模拟挂起） | 超时收敛与单次语义 | ①POST alexyang→alexyang_ai 并计时 ②查 warn 日志 elapsed | 请求阻塞 ≤ 超时上限（3000ms+固定开销）后降级拒绝；warn 含 elapsed；无重试（仅 1 次 query aiSwitch 调用记录） | 不重试；耗时留痕作容量证据 | — | R3.2/R-005 |

### 2.3 回归用例（澄清纪要 R 组）

| 编号 | 用例名称 | 前置数据 | 测试场景 | 测试步骤 | 预期结果 | 验证点 | AC | 关联风险 |
|------|---------|---------|---------|---------|---------|--------|:--:|------|
| TC-R-01 | 基线对拍_aiSwitch=false三请求 | aiSwitch=false；alexyang 有 otheruser 授权记录 | 与未升级版本逐字段一致 | 升级前后各执行：本人切换/授权切换/无授权切换三请求，diff 响应 JSON | 三请求响应逐字段一致（status/message/data 结构） | 基线零漂移 | AC3.3 | — |
| TC-R-02 | release-server继承路由三请求 | release 路由（release-server 继承 DssProxyUserController）可访问 | 继承方行为透明 | 经 release 路由执行：本人切换/授权切换/无授权切换（含 _ai 豁免目标） | 与 scriptis 路由行为一致（豁免命中/拒绝文案相同） | R-003 继承路由一致性 | — | R-003 |
| TC-R-03 | 代理用户管理四接口回归 | alexyang 有授权记录 | 既有功能零影响 | 依次调 getProxyUser/getProxyUserList/add/revokeProxyUser（授权管理页常规操作） | 四接口响应与升级前一致 | §5.1 既有接口零回归 | — | — |
| TC-R-04 | ITSM导入代理用户回归 | ITSM 流程可发起 | dss-scriptis-server-webank 导入链路 | 走一次 ITSM 代理用户导入流程 | 导入成功，dss_proxy_user 记录正确 | §2.3 导入链路零回归 | — | — |

### 2.4 边界与安全用例（澄清纪要 B 组）

| 编号 | 用例名称 | 前置数据 | 测试场景 | 测试步骤 | 预期结果 | 验证点 | 关联风险 |
|------|---------|---------|---------|---------|---------|--------|------|
| TC-B-01 | 代理cookie缺失豁免集合退化 | alexyang 全新会话（从未切换过代理，无当前代理用户 cookie）→ 切 alexyang_ai | 豁免集合退化为单元素（仅 userName+_ai） | ①清 cookie 后仅登录 ②POST alexyang→alexyang_ai | status=0 Success（设计 X4：集合退化不报错） | 无代理 cookie 时豁免仍可用 | R-002 |
| TC-B-02 | 并发豁免切换 | aiSwitch=true | 并发无共享状态 | 10 并发 POST alexyang→alexyang_ai | 全部 status=0；无异常日志（E-5） | 并发行为一致 | R1.1 |
| TC-B-03 | 豁免路径并发查询 | aiSwitch=true | 并发触发 queryAiSwitch | 10 并发 TC-F-01 请求 | 无缓存无竞争；全部成功；Linkis 侧无错误 | 每请求独立查询（R1.1） | R1.1 |
| TC-B-04 | 大小写变体拒绝 | aiSwitch=true | hadoop/Hadoop 派生与 _AI 后缀变体 | ①POST alexyang→alexyang_AI ②POST Hadoop→Hadoop_ai（若账号存在则用 alexyang 会话代理 hadoop 场景） | ①大写 _AI 不豁免（精确全等），走原校验拒绝 ②hadoop 大小写变体两拒生效（equalsIgnoreCase） | 后缀精确全等+hadoop 判定大小写不敏感 | R-002 |

## 三、SDL 安全用例

无（本需求未触发 SDL 安全设计条目，Stage 2-Hook 段B G7 无 isEffective 条目）。

## 四、接口断言覆盖

| 端点 | 数据来源 | 覆盖用例 |
|------|---------|---------|
| POST /dss/scriptis/proxy/setProxyUser | 设计文档 §1.1 / API 契约（本文件 §一） | TC-F-01~07、TC-D-01~05、TC-R-01~02、TC-B-01~04（全部用例的数据来源接口） |
| GET /api/rest_j/v1/jobhistory/governanceStationAdmin（Linkis 侧，只读依赖） | 设计文档 §1.1.2 HC-001 | TC-F-07（调用留痕）、TC-D-02/03/04/05（降级情形） |

## 五、冒烟执行记录（dev，2026-11-16）

> 执行人：ryanchen（开发自验）；实例：bdpujes110003 dss-apps-server（eureka 单实例确认，dssxc 旧实例已下线）

| 冒烟项 | 对应用例 | 结果 | 证据 |
|--------|---------|:--:|------|
| S-2 ryanchen→ryanchen_ai 豁免切换 | TC-F-01 | ✅ | 200 Success ×7 次连跑全通过；bypass 日志 `user ryanchen bypass proxy authorization by aiSwitch, target ryanchen_ai` |
| S-3 切回本人 | TC-F-06 | ✅ | 200 Success |
| CTL hadoop_ai 双拒 | TC-F-03 | ✅ | 400 `have no permission to proxy to user hadoop_ai` |
| CTL 他人派生 _ai | TC-F-02 | ✅ | 400 `have no permission to proxy to user stacyyan_ai` |

**dev 环境冒烟结论：通过**（S-1/S-4 涉及前端页面与 Linkis 直调，由测试在 uat/sit 环境按 TC 全集补测）。

## 六、Feature 覆盖率报告

| 澄清纪要场景组 | 用例数 | 已覆盖 | 覆盖率 |
|---------------|:--:|:--:|:--:|
| F 功能（F-1~F-7） | 7 | TC-F-01~07 | 100% |
| D 开关降级（D-1~D-5） | 5 | TC-D-01~05 | 100% |
| R 回归（R-1~R-4） | 4 | TC-R-01~04 | 100% |
| B 边界（B-1~B-4） | 4 | TC-B-01~04 | 100% |

**风险必测覆盖核对**（触点① stabilityRiskTestRequirements 映射）：

| 风险 | 必测要求 | 覆盖用例 | 结论 |
|------|---------|---------|:--:|
| R-001 依赖 | 超时/降级/跨系统异常 | TC-D-02/TC-D-04/TC-D-05 | ✅ |
| R-002 越权 | 越权目标拒绝 | TC-F-02/03/04、TC-B-04 | ✅ |
| R-003 继承路由 | 路由一致性 | TC-R-02 | ✅ |
| R-004 配置 | 配置边界/回滚 | TC-D-01/TC-D-03 | ✅ |
| R-005 容量 | 降级耗时实测 | TC-D-05（elapsed 留痕） | ✅ |

## 七、执行前置与造数

| # | 项 | 责任 | 说明 |
|---|---|------|------|
| 1 | 测试账号 alexyang/otheruser（+alexyang_ai 等 _ai 目标存在性确认） | stacyyan | _ai 用户由 Linkis 侧 linkis.ai.user.enable 控制可见性 |
| 2 | dss_proxy_user 授权记录预置/清理（TC-F-05、TC-R-01/03） | stacyyan | TC-F-01 需确认无 alexyang→alexyang_ai 记录 |
| 3 | Linkis `linkis.ai.user.enable` 双向变更（热加载） | falinhe + ryanchen | TC-D-01 关→开；测后恢复默认 false |
| 4 | Token 错值注入与恢复（TC-D-03） | ryanchen | 测后立即恢复，登记变更窗口 |
| 5 | 超时注入手段（TC-D-05） | ryanchen + falinhe | netem/停服务二选一，SIT 环境执行 |
| 6 | uat/sit 环境 Linkis 版本 ≥ release_v2.1.7 核对 | falinhe | 设计 §3.3 核对清单 #3 |

## 八、验收标准

- dev 冒烟（§五）全通过 ✅（已完成）；
- uat：TC-F/D/B 全量通过 + TC-R-01 基线对拍零漂移；
- sit：全用例通过 + 回归测试集 327881 执行完毕；
- TC-D-05 耗时数据留档（R-005 容量证据）；
- 缺陷零遗留或降级为说明性备注。
