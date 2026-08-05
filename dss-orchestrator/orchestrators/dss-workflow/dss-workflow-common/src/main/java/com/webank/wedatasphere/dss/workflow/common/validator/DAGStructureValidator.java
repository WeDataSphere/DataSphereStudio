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

/**
 * DAG 结构合法性校验器（纯函数式只读）。设计依据：design-doc §4.1 / §10。
 *
 * <p>契约：
 * <ul>
 *   <li>输入完整工作流 JSON（含 nodes + edges），输出校验问题列表。</li>
 *   <li><b>只读</b>：不修改 jsonFlow、不写库、无副作用、不改结构。</li>
 *   <li>批量返回全部问题（非遇错即停）：依次执行 4 项检查并汇总。</li>
 *   <li>性能：O(V+E)，典型 &lt; 100 节点 &lt; 50ms。</li>
 *   <li>永不返回 null；jsonFlow 解析异常时包成 1 条 PARSE_FAILED error 返回（不抛异常，design §8.3）。</li>
 * </ul>
 * </p>
 *
 * <p>4 项检查：① 边引用 / ② 环路(Kahn) / ③ 重名 = ERROR；④ 开始结束结构 = WARN。</p>
 */
public interface DAGStructureValidator {

    /**
     * 校验工作流 DAG 结构。
     *
     * <p>核心逻辑：
     * <ol>
     *   <li>解析 nodes/edges（复用 WorkFlowParser）。</li>
     *   <li>构建 DAGContext（标识集合/邻接表/入度出度，D-1 key 优先/id fallback）。</li>
     *   <li>依次执行 4 项检查（边引用/环路/重名/开始结束结构）。</li>
     *   <li>汇总返回 ValidationResult（含 error/warn 分级）。</li>
     * </ol>
     * </p>
     *
     * @param jsonFlow 完整工作流 JSON（来自 SaveFlowRequest.json，可为 null/格式错误）
     * @return 校验结果（永不返回 null；无问题时 issues 为空、isPassed=true）
     */
    ValidationResult validate(String jsonFlow);
}
