# 基于AppConn的判断节点功能设计文档

## 文档版本信息

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| v1.0 | 2026-02-28 | DSS Team | 初始版本，基于AppConn架构设计判断节点 |

---

## 1. 需求概述

### 1.1 需求背景

在DataSphere Studio工作流中，用户需要根据上游节点的执行结果（如变量值、执行状态等）来决定是否执行下游节点。目前DSS工作流系统只支持线性执行流程，无法实现条件分支和跳过执行等逻辑。

### 1.2 需求目标

1. 新增判断类型节点，支持根据上游节点输出的变量值进行条件判断
2. 当条件满足时，执行下游节点；当条件不满足时，跳过下游节点执行
3. 支持多种比较操作符（等于、不等于、大于、小于、包含等）
4. 支持AND/OR逻辑组合多个条件
5. 采用 AppConn 架构实现，模块化设计，易于维护和扩展

### 1.3 功能范围

- 创建 `dss-condition-appconn` 模块
- 实现条件判断节点类型
- 支持节点配置界面录入条件表达式
- 在工作流执行时解析条件并控制下游节点执行
- 提供条件表达式的语法校验功能

---

## 2. AppConn 架构设计

### 2.1 模块结构

采用 AppConn 架构，创建独立的 `dss-condition-appconn` 模块，目录结构如下：

```
dss-appconn/
└── appconns/
    └── dss-condition-appconn/           # 判断节点 AppConn 模块
        ├── pom.xml                      # 父 pom 配置
        └── condition-appconn-core/       # 核心实现
            ├── pom.xml                  # 核心 pom 配置
            ├── src/
            │   ├── main/
            │   │   ├── assembly/
            │   │   │   └── distribution.xml  # 打包配置
            │   │   ├── java/
            │   │   │   └── com/webank/wedatasphere/dss/appconn/condition/
            │   │   │       ├── ConditionAppConn.java                    # AppConn 主类
            │   │   │       ├── ConditionDevelopmentStandard.java        # 开发标准
            │   │   │       ├── operation/
            │   │   │       │   └── ConditionRefExecutionOperation.java   # 执行操作
            │   │   │       ├── service/
            │   │   │       │   └── ConditionExecutionService.java        # 执行服务
            │   │   │       ├── entity/
            │   │   │       │   ├── ConditionExpression.java              # 条件表达式实体
            │   │   │       │   ├── ConditionRule.java                    # 条件规则实体
            │   │   │       │   └── ConditionContext.java                 # 条件上下文
            │   │   │       ├── evaluator/
            │   │   │       │   ├── ConditionEvaluator.java               # 条件求值器接口
            │   │   │       │   └── impl/
            │   │   │       │       ├── SimpleConditionEvaluator.java     # 简单条件求值器
            │   │   │       │       └── CompositeConditionEvaluator.java  # 复合条件求值器
            │   │   │       ├── parser/
            │   │   │       │   ├── ExpressionParser.java                # 表达式解析器
            │   │   │       │   └── VariableParser.java                   # 变量解析器
            │   │   │       ├── exception/
            │   │   │       │   ├── ConditionEvaluateException.java       # 条件求值异常
            │   │   │       │   ├── ExpressionParseException.java          # 表达式解析异常
            │   │   │       │   └── VariableNotFoundException.java        # 变量未找到异常
            │   │   │       └── utils/
            │   │   │           └── ConditionUtils.java                   # 条件工具类
            │   │   ├── resources/
            │   │   │   ├── appconn.properties                            # AppConn 配置
            │   │   │   └── init.sql                                      # 初始化 SQL
            │   │   ├── icons/
            │   │   │   └── condition.icon                               # 节点图标
            │   │   ├── pngs/
            │   │   │   └── condition.png                                # 节点图片
            │   │   └── svgs/
            │   │       └── condition.svg                                 # 节点 SVG
            │   └── test/
            │       └── java/
            │           └── com/webank/wedatasphere/dss/appconn/condition/
            │               ├── evaluator/
            │               │   └── ConditionEvaluatorTest.java           # 条件求值器测试
            │               └── parser/
            │                   └── ExpressionParserTest.java             # 表达式解析器测试
```

### 2.2 核心类设计

#### 2.2.1 ConditionAppConn（AppConn 主类）

**继承关系**：
- 继承：`AbstractAppConn`
- 实现：`OnlyDevelopmentAppConn`

**职责**：
- AppConn 的入口类，负责初始化开发标准

**代码结构**：
```java
package com.webank.wedatasphere.dss.appconn.condition;

import com.webank.wedatasphere.dss.appconn.core.ext.OnlyDevelopmentAppConn;
import com.webank.wedatasphere.dss.appconn.core.impl.AbstractAppConn;
import com.webank.wedatasphere.dss.standard.app.development.standard.DevelopmentIntegrationStandard;

/**
 * 条件判断节点 AppConn
 */
public class ConditionAppConn extends AbstractAppConn implements OnlyDevelopmentAppConn {

    private ConditionDevelopmentStandard developmentStandard;

    @Override
    protected void initialize() {
        developmentStandard = new ConditionDevelopmentStandard();
    }

    @Override
    public DevelopmentIntegrationStandard getOrCreateDevelopmentStandard() {
        return developmentStandard;
    }
}
```

#### 2.2.2 ConditionDevelopmentStandard（开发标准）

**继承关系**：
- 继承：`OnlyExecutionDevelopmentStandard`

**职责**：
- 创建 RefExecutionService
- 定义开发标准的行为

**代码结构**：
```java
package com.webank.wedatasphere.dss.appconn.condition;

import com.webank.wedatasphere.dss.appconn.condition.service.ConditionExecutionService;
import com.webank.wedatasphere.dss.standard.app.development.service.RefExecutionService;
import com.webank.wedatasphere.dss.standard.app.development.standard.OnlyExecutionDevelopmentStandard;

/**
 * 条件判断节点开发标准
 */
public class ConditionDevelopmentStandard extends OnlyExecutionDevelopmentStandard {

    @Override
    protected RefExecutionService createRefExecutionService() {
        return new ConditionExecutionService();
    }

    @Override
    public void init() {
        // 初始化逻辑
    }

    @Override
    public String getStandardName() {
        return "ConditionDevelopmentStandard";
    }
}
```

#### 2.2.3 ConditionExecutionService（执行服务）

**继承关系**：
- 继承：`AbstractRefExecutionService`

**职责**：
- 创建 RefExecutionOperation

**代码结构**：
```java
package com.webank.wedatasphere.dss.appconn.condition.service;

import com.webank.wedatasphere.dss.appconn.condition.operation.ConditionRefExecutionOperation;
import com.webank.wedatasphere.dss.standard.app.development.operation.RefExecutionOperation;
import com.webank.wedatasphere.dss.standard.app.development.service.AbstractRefExecutionService;

/**
 * 条件判断节点执行服务
 */
public class ConditionExecutionService extends AbstractRefExecutionService {

    @Override
    protected RefExecutionOperation createRefExecutionOperation() {
        return new ConditionRefExecutionOperation();
    }
}
```

#### 2.2.4 ConditionRefExecutionOperation（执行操作）

**继承关系**：
- 继承：`AbstractDevelopmentOperation`
- 实现：`RefExecutionOperation`

**职责**：
- 执行条件判断逻辑
- 控制下游节点的执行

**代码结构**：
```java
package com.webank.wedatasphere.dss.appconn.condition.operation;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.evaluator.ConditionEvaluator;
import com.webank.wedatasphere.dss.appconn.condition.evaluator.impl.CompositeConditionEvaluator;
import com.webank.wedatasphere.dss.appconn.condition.parser.ExpressionParser;
import com.webank.wedatasphere.dss.appconn.condition.parser.VariableParser;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.RefExecutionRequestRef;
import com.webank.wedatasphere.dss.standard.app.development.operation.AbstractDevelopmentOperation;
import com.webank.wedatasphere.dss.standard.app.development.operation.RefExecutionOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;

/**
 * 条件判断节点执行操作
 */
public class ConditionRefExecutionOperation
    extends AbstractDevelopmentOperation<RefExecutionRequestRef.RefExecutionRequestRefImpl, ResponseRef>
    implements RefExecutionOperation<RefExecutionRequestRef.RefExecutionRequestRefImpl> {

    private ExpressionParser expressionParser;
    private VariableParser variableParser;
    private ConditionEvaluator conditionEvaluator;

    @Override
    public void init() {
        super.init();
        expressionParser = new ExpressionParser();
        variableParser = new VariableParser();
        conditionEvaluator = new CompositeConditionEvaluator();
    }

    @Override
    public ExecutionResponseRef execute(RefExecutionRequestRef.RefExecutionRequestRefImpl requestRef) {
        try {
            // 1. 解析条件表达式
            String expressionString = requestRef.getNode().getContent();
            ConditionExpression expression = expressionParser.parse(expressionString);

            // 2. 解析变量
            ConditionContext context = variableParser.parse(requestRef);

            // 3. 执行条件判断
            boolean conditionMet = conditionEvaluator.evaluate(expression, context);

            // 4. 根据判断结果控制下游节点
            if (conditionMet) {
                logger.info("条件满足，执行下游节点");
                return new ExecutionResponseRefBuilder().success();
            } else {
                logger.info("条件不满足，跳过下游节点");
                return new ExecutionResponseRefBuilder().success();
            }

        } catch (Exception e) {
            logger.error("条件判断执行失败", e);
            return new ExecutionResponseRefBuilder()
                .setException(e)
                .setErrorMsg("条件判断执行失败: " + e.getMessage())
                .error();
        }
    }
}
```

---

## 3. 实体设计

### 3.1 ConditionExpression（条件表达式）

```java
package com.webank.wedatasphere.dss.appconn.condition.entity;

import java.util.List;

/**
 * 条件表达式实体
 */
public class ConditionExpression {

    /**
     * 逻辑关系：AND 或 OR
     */
    private String logicOperator;

    /**
     * 条件规则列表
     */
    private List<ConditionRule> rules;

    /**
     * 嵌套表达式（用于复杂条件）
     */
    private List<ConditionExpression> subExpressions;

    // getters and setters
}
```

### 3.2 ConditionRule（条件规则）

```java
package com.webank.wedatasphere.dss.appconn.condition.entity;

/**
 * 条件规则实体
 */
public class ConditionRule {

    /**
     * 变量引用，格式：${UUID.output.variableName}
     */
    private String variableReference;

    /**
     * 比较操作符：EQ(等于), NE(不等于), GT(大于), LT(小于),
     *              GE(大于等于), LE(小于等于), CONTAINS(包含), NOT_CONTAINS(不包含)
     */
    private String operator;

    /**
     * 目标值
     */
    private String targetValue;

    /**
     * 目值类型：STRING, NUMBER, BOOLEAN
     */
    private String targetType;

    // getters and setters
}
```

### 3.3 ConditionContext（条件上下文）

```java
package com.webank.wedatasphere.dss.appconn.condition.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件上下文，存储变量及其值
 */
public class ConditionContext {

    /**
     * 变量值映射
     */
    private Map<String, Object> variables = new HashMap<>();

    public void putVariable(String variableName, Object value) {
        variables.put(variableName, value);
    }

    public Object getVariable(String variableName) {
        return variables.get(variableName);
    }

    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }

    // getters and setters
}
```

---

## 4. 条件求值器设计

### 4.1 ConditionEvaluator（接口）

```java
package com.webank.wedatasphere.dss.appconn.condition.evaluator;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;

/**
 * 条件求值器接口
 */
public interface ConditionEvaluator {

    /**
     * 评估条件表达式
     * @param expression 条件表达式
     * @param context 条件上下文
     * @return 是否满足条件
     */
    boolean evaluate(ConditionExpression expression, ConditionContext context);
}
```

### 4.2 SimpleConditionEvaluator（简单条件求值器）

```java
package com.webank.wedatasphere.dss.appconn.condition.evaluator.impl;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionRule;
import com.webank.wedatasphere.dss.appconn.condition.evaluator.ConditionEvaluator;

/**
 * 简单条件求值器，处理单个条件规则
 */
public class SimpleConditionEvaluator implements ConditionEvaluator {

    @Override
    public boolean evaluate(ConditionExpression expression, ConditionContext context) {
        if (expression.getRules() == null || expression.getRules().isEmpty()) {
            return false;
        }

        for (ConditionRule rule : expression.getRules()) {
            if (evaluateRule(rule, context)) {
                return true;
            }
        }

        return false;
    }

    private boolean evaluateRule(ConditionRule rule, ConditionContext context) {
        // 1. 获取变量值
        String variableName = rule.getVariableReference();
        Object variableValue = context.getVariable(variableName);

        if (variableValue == null) {
            return false;
        }

        // 2. 根据比较操作符进行比较
        String operator = rule.getOperator();
        String targetValue = rule.getTargetValue();
        String targetType = rule.getTargetType();

        switch (operator) {
            case "EQ":
                return evaluateEquals(variableValue, targetValue, targetType);
            case "NE":
                return !evaluateEquals(variableValue, targetValue, targetType);
            case "GT":
                return evaluateGreaterThan(variableValue, targetValue, targetType);
            case "LT":
                return evaluateLessThan(variableValue, targetValue, targetType);
            case "GE":
                return evaluateGreaterThanOrEquals(variableValue, targetValue, targetType);
            case "LE":
                return evaluateLessThanOrEquals(variableValue, targetValue, targetType);
            case "CONTAINS":
                return evaluateContains(variableValue, targetValue);
            case "NOT_CONTAINS":
                return !evaluateContains(variableValue, targetValue);
            default:
                throw new IllegalArgumentException("不支持的操作符: " + operator);
        }
    }

    private boolean evaluateEquals(Object value, String target, String targetType) {
        // 实现等于比较
        // ...
    }

    private boolean evaluateGreaterThan(Object value, String target, String targetType) {
        // 实现大于比较
        // ...
    }

    private boolean evaluateLessThan(Object value, String target, String targetType) {
        // 实现小于比较
        // ...
    }

    private boolean evaluateGreaterThanOrEquals(Object value, String target, String targetType) {
        // 实现大于等于比较
        // ...
    }

    private boolean evaluateLessThanOrEquals(Object value, String target, String targetType) {
        // 实现小于等于比较
        // ...
    }

    private boolean evaluateContains(Object value, String target) {
        // 实现包含比较
        // ...
    }
}
```

### 4.3 CompositeConditionEvaluator（复合条件求值器）

```java
package com.webank.wedatasphere.dss.appconn.condition.evaluator.impl;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.evaluator.ConditionEvaluator;

/**
 * 复合条件求值器，处理 AND/OR 逻辑
 */
public class CompositeConditionEvaluator implements ConditionEvaluator {

    private SimpleConditionEvaluator simpleEvaluator;

    public CompositeConditionEvaluator() {
        simpleEvaluator = new SimpleConditionEvaluator();
    }

    @Override
    public boolean evaluate(ConditionExpression expression, ConditionContext context) {
        // 如果有子表达式，递归评估子表达式
        if (expression.getSubExpressions() != null && !expression.getSubExpressions().isEmpty()) {
            return evaluateSubExpressions(expression, context);
        }

        // 否则使用简单求值器
        return simpleEvaluator.evaluate(expression, context);
    }

    private boolean evaluateSubExpressions(ConditionExpression expression, ConditionContext context) {
        String logicOperator = expression.getLogicOperator();

        if ("AND".equalsIgnoreCase(logicOperator)) {
            // 所有子表达式都必须满足
            for (ConditionExpression subExpr : expression.getSubExpressions()) {
                if (!evaluate(subExpr, context)) {
                    return false;
                }
            }
            return true;
        } else if ("OR".equalsIgnoreCase(logicOperator)) {
            // 至少一个子表达式满足
            for (ConditionExpression subExpr : expression.getSubExpressions()) {
                if (evaluate(subExpr, context)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException("不支持逻辑操作符: " + logicOperator);
        }
    }
}
```

---

## 5. 解析器设计

### 5.1 ExpressionParser（表达式解析器）

```java
package com.webank.wedatasphere.dss.appconn.condition.parser;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionRule;
import com.webank.wedatasphere.dss.appconn.condition.exception.ExpressionParseException;

/**
 * 表达式解析器
 */
public class ExpressionParser {

    /**
     * 解析表达式字符串为 ConditionExpression 对象
     * @param expressionString 表达式字符串（JSON 格式）
     * @return ConditionExpression 对象
     */
    public ConditionExpression parse(String expressionString) throws ExpressionParseException {
        try {
            // 使用 JSON 解析器解析表达式字符串
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(expressionString, ConditionExpression.class);
        } catch (Exception e) {
            throw new ExpressionParseException("表达式解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 校验表达式语法
     * @param expressionString 表达式字符串
     * @return 是否有效
     */
    public boolean validate(String expressionString) {
        try {
            parse(expressionString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 5.2 VariableParser（变量解析器）

```java
package com.webank.wedatasphere.dss.appconn.condition.parser;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.exception.VariableNotFoundException;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.RefExecutionRequestRef;

/**
 * 变量解析器
 */
public class VariableParser {

    /**
     * 解析变量引用并填充上下文
     * @param requestRef 执行请求
     * @return ConditionContext 对象
     */
    public ConditionContext parse(RefExecutionRequestRef.RefExecutionRequestRefImpl requestRef)
            throws VariableNotFoundException {
        ConditionContext context = new ConditionContext();

        // 从执行上下文中获取变量
        Map<String, Object> variables = requestRef.getRuntimeMap();
        if (variables != null) {
            variables.forEach((key, value) -> context.putVariable(key, value));
        }

        // 从上游节点获取输出变量
        Map<String, Object> outputMap = requestRef.getOutputMap();
        if (outputMap != null) {
            outputMap.forEach((key, value) -> context.putVariable(key, value));
        }

        return context;
    }

    /**
     * 解析变量引用字符串
     * 例如：${22c317df-3b18-4937-911d-4a19a7d78f19.output.count}
     * 返回：22c317df-3b18-4937-911d-4a19a7d78f19.output.count
     */
    public String parseVariableReference(String variableRef) {
        if (variableRef == null || !variableRef.startsWith("${") || !variableRef.endsWith("}")) {
            throw new IllegalArgumentException("无效的变量引用格式: " + variableRef);
        }
        return variableRef.substring(2, variableRef.length() - 1);
    }
}
```

---

## 6. 异常设计

### 6.1 ConditionEvaluateException（条件求值异常）

```java
package com.webank.wedatasphere.dss.appconn.condition.exception;

/**
 * 条件求值异常
 */
public class ConditionEvaluateException extends RuntimeException {

    public ConditionEvaluateException(String message) {
        super(message);
    }

    public ConditionEvaluateException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 6.2 ExpressionParseException（表达式解析异常）

```java
package com.webank.wedatasphere.dss.appconn.condition.exception;

/**
 * 表达式解析异常
 */
public class ExpressionParseException extends RuntimeException {

    public ExpressionParseException(String message) {
        super(message);
    }

    public ExpressionParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 6.3 VariableNotFoundException（变量未找到异常）

```java
package com.webank.wedatasphere.dss.appconn.condition.exception;

/**
 * 变量未找到异常
 */
public class VariableNotFoundException extends RuntimeException {

    public VariableNotFoundException(String message) {
        super(message);
    }

    public VariableNotFoundException(String variableName) {
        super("变量未找到: " + variableName);
    }
}
```

---

## 7. 配置文件设计

### 7.1 appconn.properties

```properties
#
# Copyright 2019 WeBank
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# AppConn 基础配置
dss.appconn.condition.name=条件判断节点
dss.appconn.condition.version=1.0.0
dss.appconn.condition.description=根据上游节点输出的变量值进行条件判断

# 条件求值配置
dss.appconn.condition.evaluator.timeout=30000
dss.appconn.condition.evaluator.maxDepth=10

# 表达式解析配置
dss.appconn.condition.parser.strictMode=true
dss.appconn.condition.parser.maxExpressionLength=10000

# 安全配置
dss.appconn.condition.security.enableSandbox=true
dss.appconn.condition.security.allowedPackages=java.lang,java.util
```

### 7.2 init.sql

```sql
--
-- Copyright 2019 WeBank
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- 插入节点类型定义
INSERT INTO dss_node_type (node_type_name, node_type_desc, node_type_icon, appconn_name)
VALUES ('condition', '条件判断节点', '/dss/appconn/condition/icon.png', 'dss-condition-appconn');

-- 插入节点参数配置
INSERT INTO dss_node_parameter (node_type, parameter_name, parameter_desc, parameter_type, required, default_value)
VALUES
    ('condition', 'expression', '条件表达式', 'TEXT', 1, ''),
    ('condition', 'logicOperator', '逻辑操作符', 'SELECT', 0, 'AND'),
    ('condition', 'skipOnFalse', '条件不满足时跳过', 'BOOLEAN', 0, 'true');
```

---

## 8. 前端配置

### 8.1 节点类型注册

在 `web` 模块的节点类型配置中添加判断节点：

```javascript
// 在节点类型注册文件中添加
{
  type: 'linkis.control.condition',
  name: '条件判断',
  icon: 'condition-icon',
  group: 'control',
  appConn: 'dss-condition-appconn',
  editorComponent: 'ConditionNodeEditor',
  supportsInput: true,
  supportsOutput: true,
  description: '根据上游节点输出的变量值进行条件判断，控制下游节点的执行'
}
```

### 8.2 节点编辑器组件

创建条件判断节点的编辑器组件 `ConditionNodeEditor.vue`：

```vue
<template>
  <div class="condition-node-editor">
    <el-form :model="nodeData" label-width="120px">
      <el-form-item label="条件表达式">
        <el-input
          v-model="nodeData.expression"
          type="textarea"
          :rows="6"
          placeholder="请输入条件表达式"
          @blur="validateExpression"
        />
      </el-form-item>

      <el-form-item label="逻辑操作符">
        <el-select v-model="nodeData.logicOperator">
          <el-option label="AND（所有条件都满足）" value="AND" />
          <el-option label="OR（至少一个条件满足）" value="OR" />
        </el-select>
      </el-form-item>

      <el-form-item label="条件规则">
        <condition-rule-list
          v-model="nodeData.rules"
          @add-rule="addRule"
          @remove-rule="removeRule"
        />
      </el-form-item>

      <el-form-item label="变量引用帮助">
        <variable-picker
          :available-variables="availableVariables"
          @select="insertVariable"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import ConditionRuleList from './ConditionRuleList.vue'
import VariablePicker from './VariablePicker.vue'

export default {
  name: 'ConditionNodeEditor',
  components: {
    ConditionRuleList,
    VariablePicker
  },
  props: {
    nodeData: {
      type: Object,
      required: true
    },
    availableVariables: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    addRule() {
      if (!this.nodeData.rules) {
        this.nodeData.rules = []
      }
      this.nodeData.rules.push({
        variableReference: '',
        operator: 'EQ',
        targetValue: '',
        targetType: 'STRING'
      })
    },
    removeRule(index) {
      this.nodeData.rules.splice(index, 1)
    },
    insertVariable(variable) {
      // 插入变量引用到表达式
    },
    validateExpression() {
      // 校验表达式语法
    }
  }
}
</script>
```

---

## 9. 工作流执行集成

### 9.1 判断节点执行流程图

```
┌─────────────────────────────────────────────────────────────┐
│                       工作流开始                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      执行上游节点                              │
│                   (如：SQL查询节点)                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 输出变量：node_123.count = 150
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      判断节点执行                              │
│  1. 收集上游节点输出变量                                        │
│  2. 解析条件表达式：${node_123.count} > 100                  │
│  3. 执行判断：150 > 100 = TRUE                                │
│  4. 返回判断结果：conditionResult = TRUE                     │
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │ conditionResult = TRUE      │
                ▼                           │
┌──────────────────────────────┐            │ conditionResult = FALSE
│    执行下游节点A              │            │
│  (如：数据处理节点)            │            │
└──────────────────────────────┘            │
                │                            │
                ▼                            ▼
┌──────────────────────────────┐  ┌─────────────────────────────┐
│    执行下游节点B              │  │     跳过下游节点              │
│  (如：数据导出节点)            │  │  标记状态为"已跳过"            │
└──────────────────────────────┘  └─────────────────────────────┘
                │                            │
                └─────────────┬──────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       工作流结束                              │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 执行时序图

```
用户          工作流调度器      判断节点      变量收集器      表达式解析器      下游节点
 │                 │              │              │              │               │
 │──执行工作流─────>│              │              │              │               │
 │                 │──收集变量───>│              │              │               │
 │                 │              │──获取上游变量──>│              │               │
 │                 │              │<──变量列表─────│              │               │
 │                 │              │──解析表达式─────────────────>│               │
 │                 │              │<──解析结果───────────────────│               │
 │                 │              │──执行判断───────────────────>│               │
 │                 │              │<──判断结果(TRUE)─────────────│               │
 │                 │<─执行结果───│              │              │               │
 │                 │──执行下游────────────────────────────────────────────────>│
 │                 │              │              │              │               │
 │<──执行完成───────│              │              │              │               │
```

### 9.3 节点执行流程

判断节点的工作流执行流程如下：

```
1. 用户在工作流画布中配置判断节点
   ↓
2. 保存工作流时，将节点配置（条件表达式）保存到数据库
   ↓
3. 工作流执行时，Orchestrator 解析节点依赖关系
   ↓
4. 当上游节点执行完成后，执行判断节点
   ↓
5. ConditionRefExecutionOperation 执行：
   a) 解析条件表达式
   b) 从执行上下文中获取变量值
   c) 执行条件求值
   d) 根据求值结果控制下游节点执行
```

### 9.4 下游节点控制机制

在 DSS Orchestrator 中，需要修改节点执行逻辑以支持条件节点：

```java
// 在工作流执行器中添加条件判断逻辑
public class WorkflowExecutor {

    public void executeNode(WorkflowNode node) {
        // 检查节点类型
        if (isConditionNode(node)) {
            // 执行条件判断节点
            ConditionResult result = executeConditionNode(node);

            if (!result.isConditionMet()) {
                // 条件不满足，跳过下游节点
                skipDownstreamNodes(node);
                return;
            }
        }

        // 正常执行节点
        executeNodeNormal(node);
    }

    private boolean isConditionNode(WorkflowNode node) {
        return "condition".equals(node.getNodeType());
    }

    private void skipDownstreamNodes(WorkflowNode node) {
        // 获取下游节点
        List<WorkflowNode> downstreamNodes = getDownstreamNodes(node);

        // 标记为跳过
        for (WorkflowNode downstream : downstreamNodes) {
            downstream.setSkip(true);
        }
    }
}
```

---

## 10. 测试用例

### 10.1 条件求值器测试

```java
package com.webank.wedatasphere.dss.appconn.condition.evaluator;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionContext;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionRule;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConditionEvaluatorTest {

    @Test
    public void testEvaluateEquals() {
        ConditionContext context = new ConditionContext();
        context.putVariable("count", "100");

        ConditionExpression expression = new ConditionExpression();
        ConditionRule rule = new ConditionRule();
        rule.setVariableReference("count");
        rule.setOperator("EQ");
        rule.setTargetValue("100");
        rule.setTargetType("STRING");

        expression.setRules(Arrays.asList(rule));

        ConditionEvaluator evaluator = new SimpleConditionEvaluator();
        boolean result = evaluator.evaluate(expression, context);
        assertTrue(result);
    }

    @Test
    public void testEvaluateGreaterThan() {
        ConditionContext context = new ConditionContext();
        context.putVariable("count", "100");

        ConditionExpression expression = new ConditionExpression();
        ConditionRule rule = new ConditionRule();
        rule.setVariableReference("count");
        rule.setOperator("GT");
        rule.setTargetValue("50");
        rule.setTargetType("NUMBER");

        expression.setRules(Arrays.asList(rule));

        ConditionEvaluator evaluator = new SimpleConditionEvaluator();
        boolean result = evaluator.evaluate(expression, context);
        assertTrue(result);
    }

    @Test
    public void testEvaluateAnd() {
        ConditionContext context = new ConditionContext();
        context.putVariable("count", "100");
        context.putVariable("status", "success");

        ConditionExpression expression = new ConditionExpression();
        expression.setLogicOperator("AND");

        ConditionRule rule1 = new ConditionRule();
        rule1.setVariableReference("count");
        rule1.setOperator("GT");
        rule1.setTargetValue("50");
        rule1.setTargetType("NUMBER");

        ConditionRule rule2 = new ConditionRule();
        rule2.setVariableReference("status");
        rule2.setOperator("EQ");
        rule2.setTargetValue("success");
        rule2.setTargetType("STRING");

        expression.setRules(Arrays.asList(rule1, rule2));

        ConditionEvaluator evaluator = new CompositeConditionEvaluator();
        boolean result = evaluator.evaluate(expression, context);
        assertTrue(result);
    }

    @Test
    public void testEvaluateOr() {
        ConditionContext context = new ConditionContext();
        context.putVariable("count", "30");
        context.putVariable("status", "success");

        ConditionExpression expression = new ConditionExpression();
        expression.setLogicOperator("OR");

        ConditionRule rule1 = new ConditionRule();
        rule1.setVariableReference("count");
        rule1.setOperator("GT");
        rule1.setTargetValue("50");
        rule1.setTargetType("NUMBER");

        ConditionRule rule2 = new ConditionRule();
        rule2.setVariableReference("status");
        rule2.setOperator("EQ");
        rule2.setTargetValue("success");
        rule2.setTargetType("STRING");

        expression.setRules(Arrays.asList(rule1, rule2));

        ConditionEvaluator evaluator = new CompositeConditionEvaluator();
        boolean result = evaluator.evaluate(expression, context);
        assertTrue(result);
    }
}
```

### 10.2 表达式解析器测试

```java
package com.webank.wedatasphere.dss.appconn.condition.parser;

import com.webank.wedatasphere.dss.appconn.condition.entity.ConditionExpression;
import com.webank.wedatasphere.dss.appconn.condition.exception.ExpressionParseException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExpressionParserTest {

    @Test
    public void testParseValidExpression() throws ExpressionParseException {
        String expressionString = "{\"rules\":[{\"variableReference\":\"count\",\"operator\":\"EQ\",\"targetValue\":\"100\",\"targetType\":\"STRING\"}]}";

        ExpressionParser parser = new ExpressionParser();
        ConditionExpression expression = parser.parse(expressionString);

        assertNotNull(expression);
        assertNotNull(expression.getRules());
        assertEquals(1, expression.getRules().size());
        assertEquals("count", expression.getRules().get(0).getVariableReference());
    }

    @Test(expected = ExpressionParseException.class)
    public void testParseInvalidExpression() throws ExpressionParseException {
        String invalidExpression = "{invalid json}";

        ExpressionParser parser = new ExpressionParser();
        parser.parse(invalidExpression);
    }

    @Test
    public void testValidateValidExpression() {
        String validExpression = "{\"rules\":[{\"variableReference\":\"count\",\"operator\":\"EQ\",\"targetValue\":\"100\",\"targetType\":\"STRING\"}]}";

        ExpressionParser parser = new ExpressionParser();
        boolean isValid = parser.validate(validExpression);
        assertTrue(isValid);
    }

    @Test
    public void testValidateInvalidExpression() {
        String invalidExpression = "{invalid json}";

        ExpressionParser parser = new ExpressionParser();
        boolean isValid = parser.validate(invalidExpression);
        assertFalse(isValid);
    }
}
```

---

## 11. 部署方案

### 11.1 模块打包

在 `dss-appconn/pom.xml` 中添加新模块：

```xml
<modules>
    <!-- ... existing modules ... -->
    <module>appconns/dss-condition-appconn</module>
</modules>
```

### 11.2 安装步骤

1. **编译打包**
   ```bash
   cd dss-appconn/appconns/dss-condition-appconn
   mvn clean package
   ```

2. **部署到 DSS 服务器**
   ```bash
   # 将打包后的文件复制到 DSS 安装目录
   cp target/out.zip ${DSS_HOME}/appConn/dss-condition-appconn/

   # 解压
   cd ${DSS_HOME}/appConn/dss-condition-appconn/
   unzip out.zip
   ```

3. **重启 DSS 服务**
   ```bash
   sh ${DSS_HOME}/bin/dss-daemon.sh restart
   ```

4. **验证安装**
   - 登录 DSS 前端
   - 在工作流编辑器中查看是否出现"条件判断"节点
   - 创建测试工作流验证功能

---

## 12. 使用示例

### 12.1 示例1：简单条件判断

**场景**：根据上游SQL节点的查询结果数量，判断是否执行后续节点

**配置**：
```
节点类型：条件判断
逻辑操作符：AND
条件规则：
  - 变量引用：${22c317df-3b18-4937-911d-4a19a7d78f19.output.count}
    操作符：GT
    目标值：0
    类型：NUMBER
```

**工作流**：
```
SQL节点 -> 条件判断节点 -> [条件满足] -> 数据导出节点
                            -> [条件不满足] -> 结束
```

### 12.2 示例2：复合条件判断

**场景**：需要同时满足多个条件才能执行后续节点

**配置**：
```
节点类型：条件判断
逻辑操作符：AND
条件规则：
  - 变量引用：${node1.output.count}
    操作符：GT
    目标值：100
    类型：NUMBER

  - 变量引用：${node2.output.status}
    操作符：EQ
    目标值：success
    类型：STRING

  - 变量引用：${node3.output.hasError}
    操作符：EQ
    目标值：false
    类型：BOOLEAN
```

### 12.3 示例3：OR逻辑条件判断

**场景**：满足任一条件即可执行后续节点

**配置**：
```
节点类型：条件判断
逻辑操作符：OR
条件规则：
  - 变量引用：${node1.output.count}
    操作符：GT
    目标值：1000
    类型：NUMBER

  - 变量引用：${node2.output.isPriority}
    操作符：EQ
    目标值：true
    类型：BOOLEAN
```

---

## 13. JSON 数据结构示例

### 13.1 工作流 JSON（包含判断节点）

```json
{
  "name": "条件判断示例工作流",
  "nodes": [
    {
      "key": "22c317df-3b18-4937-911d-4a19a7d78f19",
      "nodeName": "SQL查询节点",
      "nodeType": "spark.sql",
      "content": "SELECT count(*) as count FROM table",
      "desc": "",
      "layout": {
        "x": 100,
        "y": 100,
        "w": 200,
        "h": 80
      },
      "createTime": "2026-02-28 10:00:00",
      "modifyTime": "2026-02-28 10:00:00",
      "contextID": {
        "flowID": 1,
        "nodeName": "SQL查询节点",
        "nodeNameStr": ""
      },
      "orcVersion": "v1"
    },
    {
      "key": "ee921e65-5a6d-4248-b74f-d4d0f62fa839",
      "nodeName": "条件判断节点",
      "nodeType": "condition",
      "content": "{\"logicOperator\":\"AND\",\"rules\":[{\"variableReference\":\"${22c317df-3b18-4937-911d-4a19a7d78f19.output.count}\",\"operator\":\"GT\",\"targetValue\":\"0\",\"targetType\":\"NUMBER\"}]}",
      "desc": "根据SQL查询结果判断是否继续执行",
      "layout": {
        "x": 100,
        "y": 250,
        "w": 200,
        "h": 80
      },
      "createTime": "2026-02-28 10:00:00",
      "modifyTime": "2026-02-28 10:00:00",
      "contextID": {
        "flowID": 1,
        "nodeName": "条件判断节点",
        "nodeNameStr": ""
      },
      "orcVersion": "v1"
    },
    {
      "key": "aa321e65-5a6d-4248-b74f-d4d0f62fa840",
      "nodeName": "数据导出节点",
      "nodeType": "linkis.io.export",
      "content": "导出数据",
      "desc": "",
      "layout": {
        "x": 100,
        "y": 400,
        "w": 200,
        "h": 80
      },
      "createTime": "2026-02-28 10:00:00",
      "modifyTime": "2026-02-28 10:00:00",
      "contextID": {
        "flowID": 1,
        "nodeName": "数据导出节点",
        "nodeNameStr": ""
      },
      "orcVersion": "v1"
    }
  ],
  "edges": [
    {
      "source": "22c317df-3b18-4937-911d-4a19a7d78f19",
      "target": "ee921e65-5a6d-4248-b74f-d4d0f62fa839",
      "linkType": "straight",
      "sourceLocation": "bottom",
      "targetLocation": "top"
    },
    {
      "source": "ee921e65-5a6d-4248-b74f-d4d0f62fa839",
      "target": "aa321e65-5a6d-4248-b74f-d4d0f62fa840",
      "linkType": "straight",
      "sourceLocation": "bottom",
      "targetLocation": "top"
    }
  ]
}
```

---

## 14. 优势与改进

### 14.1 采用 AppConn 架构的优势

1. **模块化设计**：判断节点作为独立的 AppConn 模块，与核心系统解耦，便于维护和升级

2. **易于扩展**：可以独立开发、测试和部署，不影响其他功能模块

3. **标准化接口**：遵循 DSS AppConn 标准，与其他 AppConn 模块保持一致的接口规范

4. **可复用性**：条件求值、表达式解析等核心逻辑可以在其他 AppConn 中复用

5. **插件化架构**：用户可以根据需要启用或禁用判断节点 AppConn

### 14.2 与原设计方案的对比

| 对比项 | 原方案（节点扩展） | 新方案（AppConn） |
|--------|------------------|------------------|
| 代码耦合度 | 较高，与核心系统耦合 | 低，独立模块 |
| 开发效率 | 中等 | 高，可独立开发 |
| 测试难度 | 较高，需要集成测试 | 低，可单元测试 |
| 部署灵活性 | 较低，需要整体部署 | 高，可独立部署 |
| 维护成本 | 较高 | 低 |
| 扩展性 | 受限 | 强 |

---

## 15. 风险与挑战

### 15.1 技术风险

1. **变量解析复杂性**：从执行上下文中正确提取变量值可能遇到挑战
   - 缓解措施：完善的变量解析器设计，充分的单元测试

2. **表达式求值安全性**：用户输入的表达式可能存在安全风险
   - 缓解措施：使用表达式沙箱，限制可访问的类和方法

3. **性能影响**：条件求值可能影响工作流执行性能
   - 缓解措施：优化求值算法，添加缓存机制

### 15.2 业务风险

1. **用户学习成本**：用户需要学习条件表达式的语法
   - 缓解措施：提供详细的文档和示例，友好的UI界面

2. **向后兼容性**：需要确保不影响现有工作流的执行
   - 缓解措施：充分测试，提供升级指南

---

## 16. 后续优化方向

### 16.1 功能增强

1. **可视化条件构建器**：提供图形化界面构建条件表达式，降低使用难度

2. **表达式模板**：提供常用的条件表达式模板，快速配置

3. **调试模式**：在开发环境支持条件求值的调试和日志输出

4. **条件预览**：在配置时预览条件的求值结果

### 16.2 性能优化

1. **表达式缓存**：缓存已解析的表达式，避免重复解析

2. **并行求值**：对于复杂条件，支持并行求值提高性能

3. **增量更新**：支持变量值的增量更新，减少不必要的求值

### 16.3 安全增强

1. **权限控制**：支持对条件表达式的访问权限控制

2. **审计日志**：记录条件求值的执行日志，便于审计

3. **表达式白名单**：限制可使用的表达式函数和操作符

---

## 17. 附录

### 17.1 支持的比较操作符

| 操作符 | 说明 | 示例 |
|--------|------|------|
| EQ | 等于 | count EQ 100 |
| NE | 不等于 | count NE 0 |
| GT | 大于 | count GT 50 |
| LT | 小于 | count LT 100 |
| GE | 大于等于 | count GE 10 |
| LE | 小于等于 | count LE 1000 |
| CONTAINS | 包含 | message CONTAINS "error" |
| NOT_CONTAINS | 不包含 | message NOT_CONTAINS "error" |

### 17.2 变量引用格式

```
格式：${节点UUID.output.变量名}

示例：
${22c317df-3b18-4937-911d-4a19a7d78f19.output.count}
${ee921e65-5a6d-4248-b74f-d4d0f62fa839.output.status}
${aa321e65-5a6d-4248-b74f-d4d0f62fa840.output.message}
```

### 17.3 条件表达式JSON格式

```json
{
  "logicOperator": "AND",
  "rules": [
    {
      "variableReference": "${node1.output.count}",
      "operator": "GT",
      "targetValue": "100",
      "targetType": "NUMBER"
    },
    {
      "variableReference": "${node2.output.status}",
      "operator": "EQ",
      "targetValue": "success",
      "targetType": "STRING"
    }
  ]
}
```

---

## 18. 变更记录

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0 | 2026-02-28 | 初始版本，基于AppConn架构设计判断节点 |
