# ITSM接口字段适配_测试用例

## 一、测试概述

### 1.1 测试目的
验证ITSM接口字段适配功能，确保：
- 接口能够正常接收并解析包含新字段的请求报文
- 对于未知字段采用静默忽略策略，不抛出异常
- 保持向后兼容性，不影响现有功能正常运行

### 1.2 测试范围
| 接口 | 路径 | 说明 |
|-----|------|------|
| updateWorkspace | /dss/framework/workspace/updateWorkspace | 工作空间新建/修改 |
| addProjectAndOrchestratorWhite | /dss/framework/orchestrator/addOrchestratorWhite | 添加工作流白名单 |
| addUserProxy | /dss/scriptis/proxy/addUserProxy | 添加代理用户 |

### 1.3 测试环境
- DSS版本：1.22.0-SNAPSHOT
- JDK版本：1.8
- 测试类型：接口测试

---

## 二、测试用例

### 2.1 updateWorkspace 接口测试

#### TC-WS-001：正常请求-无未知字段
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-WS-001 |
| **用例名称** | 正常请求-无未知字段 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造标准ITSM请求报文（无未知字段）<br>2. 调用 /dss/framework/workspace/updateWorkspace 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"workspaceName\":\"test_workspace\",\"option\":\"add\",\"desc\":\"测试工作空间\"}]}",
  "externalId": "ITSM-001",
  "flowId": "FLOW-001",
  "operateUser": "admin",
  "requestTitle": "新建工作空间",
  "style": "default",
  "taskId": "TASK-001",
  "formId": "FORM-001"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 工作空间创建成功 |
| **优先级** | P0 |

#### TC-WS-002：包含未知字段-静默忽略
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-WS-002 |
| **用例名称** | 包含未知字段-静默忽略 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造包含未知字段的ITSM请求报文<br>2. 调用 /dss/framework/workspace/updateWorkspace 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"workspaceName\":\"test_workspace2\",\"option\":\"add\",\"desc\":\"测试工作空间\"}]}",
  "externalId": "ITSM-002",
  "flowId": "FLOW-002",
  "operateUser": "admin",
  "requestTitle": "新建工作空间",
  "style": "default",
  "taskId": "TASK-002",
  "formId": "FORM-002",
  "newField1": "新字段1",
  "newField2": "新字段2",
  "extraInfo": {"key": "value"}
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 工作空间创建成功<br>4. 未知字段被静默忽略，不影响业务 |
| **优先级** | P0 |

#### TC-WS-003：必填字段缺失
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-WS-003 |
| **用例名称** | 必填字段缺失-数据为空 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造data为空的请求报文<br>2. 调用 /dss/framework/workspace/updateWorkspace 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[]}",
  "externalId": "ITSM-003",
  "newField": "未知字段"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回错误信息 "data is empty" |
| **优先级** | P1 |

#### TC-WS-004：鉴权失败
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-WS-004 |
| **用例名称** | 鉴权失败-错误签名 |
| **前置条件** | DSS服务正常运行 |
| **测试步骤** | 1. 构造错误的鉴权头（timestamp/sign）<br>2. 调用 /dss/framework/workspace/updateWorkspace 接口<br>3. 检查返回结果 |
| **测试数据** | Header: timeStamp=invalid, sign=invalid |
| **预期结果** | 1. 返回状态码 403<br>2. 返回错误信息 "Authentication failed." |
| **优先级** | P1 |

#### TC-WS-005：修改工作空间-包含未知字段
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-WS-005 |
| **用例名称** | 修改工作空间-包含未知字段 |
| **前置条件** | 1. DSS服务正常运行<br>2. 已存在测试工作空间<br>3. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造修改工作空间的请求报文（包含未知字段）<br>2. 调用 /dss/framework/workspace/updateWorkspace 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"workspaceName\":\"existing_workspace\",\"option\":\"modify\",\"desc\":\"修改后的描述\"}]}",
  "externalId": "ITSM-005",
  "unknownField": "未知字段值"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 工作空间修改成功 |
| **优先级** | P1 |

---

### 2.2 addOrchestratorWhite 接口测试

#### TC-OW-001：正常请求-无未知字段
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-OW-001 |
| **用例名称** | 正常请求-无未知字段 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确<br>3. 项目和工作流已存在 |
| **测试步骤** | 1. 构造标准ITSM请求报文<br>2. 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"projectName\":\"test_project\",\"flowName\":\"test_flow\"}]}",
  "externalId": "ITSM-OW-001",
  "flowId": "FLOW-OW-001",
  "operateUser": "admin",
  "requestTitle": "添加白名单",
  "taskId": "TASK-OW-001",
  "formId": "FORM-OW-001"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 白名单添加成功 |
| **优先级** | P0 |

#### TC-OW-002：包含未知字段-静默忽略
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-OW-002 |
| **用例名称** | 包含未知字段-静默忽略 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确<br>3. 项目和工作流已存在 |
| **测试步骤** | 1. 构造包含未知字段的ITSM请求报文<br>2. 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"projectName\":\"test_project\",\"flowName\":\"test_flow2\"}]}",
  "externalId": "ITSM-OW-002",
  "flowId": "FLOW-OW-002",
  "operateUser": "admin",
  "requestTitle": "添加白名单",
  "taskId": "TASK-OW-002",
  "newField": "新字段值",
  "extraData": {"key": "value"}
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 白名单添加成功<br>4. 未知字段被静默忽略 |
| **优先级** | P0 |

#### TC-OW-003：项目不存在
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-OW-003 |
| **用例名称** | 项目不存在 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造不存在的项目名称<br>2. 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"projectName\":\"not_exist_project\",\"flowName\":\"test_flow\"}]}",
  "externalId": "ITSM-OW-003",
  "unknownField": "未知字段"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回错误信息 |
| **优先级** | P1 |

---

### 2.3 addUserProxy 接口测试

#### TC-AP-001：正常请求-无未知字段
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-AP-001 |
| **用例名称** | 正常请求-无未知字段 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造标准ITSM请求报文<br>2. 调用 /dss/scriptis/proxy/addUserProxy 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"user\":\"testuser\",\"hduser\":\"proxyuser\",\"expireTime\":\"2026-12-31\"}]}",
  "externalId": "ITSM-AP-001",
  "flowId": "FLOW-AP-001",
  "operateUser": "admin",
  "requestTitle": "添加代理用户",
  "style": "default",
  "taskId": "TASK-AP-001",
  "expireTime": "2026-12-31"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 代理用户添加成功 |
| **优先级** | P0 |

#### TC-AP-002：包含未知字段-静默忽略
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-AP-002 |
| **用例名称** | 包含未知字段-静默忽略 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造包含未知字段的ITSM请求报文<br>2. 调用 /dss/scriptis/proxy/addUserProxy 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"user\":\"testuser2\",\"hduser\":\"proxyuser2\",\"expireTime\":\"2026-12-31\"}]}",
  "externalId": "ITSM-AP-002",
  "flowId": "FLOW-AP-002",
  "operateUser": "admin",
  "requestTitle": "添加代理用户",
  "style": "default",
  "taskId": "TASK-AP-002",
  "expireTime": "2026-12-31",
  "newField1": "新字段1",
  "newField2": "新字段2",
  "extraInfo": {"key": "value"}
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 代理用户添加成功<br>4. 未知字段被静默忽略 |
| **优先级** | P0 |

#### TC-AP-003：数据为空
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-AP-003 |
| **用例名称** | 数据为空 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造data为空的请求报文<br>2. 调用 /dss/scriptis/proxy/addUserProxy 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[]}",
  "externalId": "ITSM-AP-003",
  "unknownField": "未知字段"
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回错误信息 "data is empty" |
| **优先级** | P1 |

#### TC-AP-004：鉴权失败
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-AP-004 |
| **用例名称** | 鉴权失败-错误签名 |
| **前置条件** | DSS服务正常运行 |
| **测试步骤** | 1. 构造错误的鉴权头（timestamp/sign）<br>2. 调用 /dss/scriptis/proxy/addUserProxy 接口<br>3. 检查返回结果 |
| **测试数据** | Header: timeStamp=invalid, sign=invalid |
| **预期结果** | 1. 返回状态码 403<br>2. 返回错误信息 "Authentication failed." |
| **优先级** | P1 |

#### TC-AP-005：多个未知字段
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-AP-005 |
| **用例名称** | 多个未知字段 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 构造包含多个未知字段的请求报文<br>2. 调用 /dss/scriptis/proxy/addUserProxy 接口<br>3. 检查返回结果 |
| **测试数据** | ```json
{
  "createDate": "2026-04-27",
  "createUser": "admin",
  "data": "{\"dataList\":[{\"user\":\"testuser3\",\"hduser\":\"proxyuser3\"}]}",
  "externalId": "ITSM-AP-005",
  "field1": "value1",
  "field2": "value2",
  "field3": "value3",
  "field4": {"nested": "object"},
  "field5": [1, 2, 3]
}
``` |
| **预期结果** | 1. 返回状态码 200<br>2. 返回 success: true<br>3. 代理用户添加成功<br>4. 所有未知字段被静默忽略 |
| **优先级** | P1 |

---

### 2.4 向后兼容性测试

#### TC-BC-001：旧版请求格式兼容
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-BC-001 |
| **用例名称** | 旧版请求格式兼容 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 使用旧版ITSM请求格式（标准字段）<br>2. 调用所有三个接口<br>3. 检查返回结果 |
| **测试数据** | 使用ITSM升级前的标准请求格式 |
| **预期结果** | 1. 所有接口返回正常<br>2. 功能与升级前一致 |
| **优先级** | P0 |

#### TC-BC-002：混合场景测试
| 项目 | 内容 |
|-----|------|
| **用例编号** | TC-BC-002 |
| **用例名称** | 混合场景测试 |
| **前置条件** | 1. DSS服务正常运行<br>2. ITSM鉴权配置正确 |
| **测试步骤** | 1. 交替使用旧版和新增字段的请求<br>2. 调用各接口<br>3. 验证功能一致性 |
| **测试数据** | 旧版请求 + 新版请求（含未知字段） |
| **预期结果** | 1. 所有请求正常处理<br>2. 功能表现一致 |
| **优先级** | P1 |

---

## 三、测试数据汇总

### 3.1 测试账号
| 账号类型 | 账号 | 说明 |
|---------|------|------|
| 管理员 | admin | 超级管理员 |
| 普通用户 | testuser | 普通用户 |
| 代理用户 | proxyuser | 代理用户 |

### 3.2 ITSM鉴权参数
| 参数 | 说明 |
|-----|------|
| timeStamp | 时间戳（毫秒） |
| sign | SHA256(secretKey + timestamp) |

---

## 四、测试执行记录

| 用例编号 | 执行结果 | 执行人 | 执行日期 | 备注 |
|---------|---------|-------|---------|------|
| TC-WS-001 | | | | |
| TC-WS-002 | | | | |
| TC-WS-003 | | | | |
| TC-WS-004 | | | | |
| TC-WS-005 | | | | |
| TC-OW-001 | | | | |
| TC-OW-002 | | | | |
| TC-OW-003 | | | | |
| TC-AP-001 | | | | |
| TC-AP-002 | | | | |
| TC-AP-003 | | | | |
| TC-AP-004 | | | | |
| TC-AP-005 | | | | |
| TC-BC-001 | | | | |
| TC-BC-002 | | | | |

---

## 五、测试总结

### 5.1 测试用例统计
| 类型 | 数量 | P0 | P1 |
|-----|------|-----|-----|
| updateWorkspace接口 | 5 | 2 | 3 |
| addOrchestratorWhite接口 | 3 | 2 | 1 |
| addUserProxy接口 | 5 | 2 | 3 |
| 向后兼容性测试 | 2 | 1 | 1 |
| **合计** | **15** | **7** | **8** |

### 5.2 验收标准
- 所有P0用例必须通过
- P1用例通过率 ≥ 90%
- 无严重缺陷遗留

---

**文档版本**：v1.0
**创建日期**：2026-04-27
**更新日期**：2026-04-27
**需求关联**：[ITSM接口字段适配_需求](../requirements/ITSM接口字段适配_需求.md)
**设计关联**：[ITSM接口字段适配_设计](../design/ITSM接口字段适配_设计.md)
