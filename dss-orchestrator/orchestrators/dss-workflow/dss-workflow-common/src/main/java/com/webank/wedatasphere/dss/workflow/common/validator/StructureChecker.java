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

import java.util.List;

/**
 * 单项 DAG 结构检查器。设计依据：design-doc §4.3。
 *
 * <p>每个 Checker 职责单一、只读、批量返回本检查项的问题（不遇错即停）。
 * 共享一份 {@link DAGContext}（一次解析、四项复用）。</p>
 */
public interface StructureChecker {

    /**
     * 执行本项检查。
     *
     * @param ctx 已构建的 DAG 上下文（只读）
     * @return 本项检出的全部问题列表；无问题返回空列表，永不返回 null
     */
    List<ValidationIssue> check(DAGContext ctx);
}
