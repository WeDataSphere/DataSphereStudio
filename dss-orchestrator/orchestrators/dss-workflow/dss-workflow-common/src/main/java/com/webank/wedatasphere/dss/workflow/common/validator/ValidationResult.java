/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.workflow.common.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DAG 结构校验结果。设计依据：design-doc §4.2。
 *
 * <p>承载全部校验问题（error + warn，批量返回，非遇错即停）。
 * 仅 error 阻断保存；warn 允许确认继续。{@link #isPassed()} 判定是否放行（无 error 即通过）。</p>
 *
 * <p>线程安全：本类在构造完成后（issues 列表不再变动）可被并发只读访问；
 * 实际使用中由校验器单次调用产出后即不可变。</p>
 */
public class ValidationResult {

    private final List<ValidationIssue> issues;

    public ValidationResult() {
        this.issues = new ArrayList<>();
    }

    public ValidationResult(List<ValidationIssue> issues) {
        this.issues = (issues == null) ? new ArrayList<ValidationIssue>() : new ArrayList<ValidationIssue>(issues);
    }

    /** 是否存在 error 级问题（边引用/环路/重名/解析失败） */
    public boolean hasErrors() {
        for (ValidationIssue issue : issues) {
            if (issue.getLevel() == IssueLevel.ERROR) {
                return true;
            }
        }
        return false;
    }

    /** 是否存在 warn 级问题（开始结束结构） */
    public boolean hasWarnings() {
        for (ValidationIssue issue : issues) {
            if (issue.getLevel() == IssueLevel.WARN) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否放行。仅 error 阻断；warn 不阻断（允许确认继续）。
     */
    public boolean isPassed() {
        return !hasErrors();
    }

    /** 返回全部问题（不可变视图） */
    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    /** 仅 error 级问题 */
    public List<ValidationIssue> getErrors() {
        List<ValidationIssue> errors = new ArrayList<>();
        for (ValidationIssue issue : issues) {
            if (issue.getLevel() == IssueLevel.ERROR) {
                errors.add(issue);
            }
        }
        return errors;
    }

    /** 仅 warn 级问题 */
    public List<ValidationIssue> getWarnings() {
        List<ValidationIssue> warnings = new ArrayList<>();
        for (ValidationIssue issue : issues) {
            if (issue.getLevel() == IssueLevel.WARN) {
                warnings.add(issue);
            }
        }
        return warnings;
    }

    public void addIssue(ValidationIssue issue) {
        if (issue != null) {
            issues.add(issue);
        }
    }

    public void addAll(List<ValidationIssue> newIssues) {
        if (newIssues != null) {
            issues.addAll(newIssues);
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{passed=" + isPassed()
                + ", errors=" + getErrors().size()
                + ", warnings=" + getWarnings().size()
                + ", issues=" + issues + '}';
    }
}
