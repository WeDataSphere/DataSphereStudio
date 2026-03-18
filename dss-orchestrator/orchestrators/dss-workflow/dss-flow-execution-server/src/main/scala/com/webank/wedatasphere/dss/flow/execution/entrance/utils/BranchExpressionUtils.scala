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

package com.webank.wedatasphere.dss.flow.execution.entrance.utils

import java.util

import com.webank.wedatasphere.dss.common.entity.node.DSSEdge
import com.webank.wedatasphere.dss.flow.execution.entrance.conf.FlowExecutionEntranceConfiguration
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode

import scala.collection.JavaConversions._

object BranchExpressionUtils {

  val BranchNodeType = "workflow.branch"

  def isBranchNode(node: WorkflowNode): Boolean = node != null && BranchNodeType.equalsIgnoreCase(node.getNodeType)

  def sortEdges(edges: Seq[DSSEdge]): Seq[DSSEdge] = {
    edges.sortBy { edge =>
      Option(edge.getPriority).map(_.intValue()).getOrElse(Int.MaxValue)
    }
  }

  def buildEvaluationContext(node: WorkflowNode): Map[String, String] = {
    val params = Option(node).map(_.getDSSNode).map(_.getParams).orNull
    val flowVarMap = params match {
      case map: util.Map[_, _] if map.containsKey(FlowExecutionEntranceConfiguration.FLOW_VAR_MAP) =>
        map.get(FlowExecutionEntranceConfiguration.FLOW_VAR_MAP) match {
          case vars: util.Map[_, _] => vars.collect { case (k, v) if k != null && v != null => k.toString -> v.toString }.toMap
          case _ => Map.empty[String, String]
        }
      case _ => Map.empty[String, String]
    }
    flowVarMap ++ Map(
      "node.id" -> Option(node).map(_.getId).getOrElse(""),
      "node.name" -> Option(node).map(_.getName).getOrElse(""),
      "node.type" -> Option(node).map(_.getNodeType).getOrElse("")
    )
  }

  def evaluateCondition(condition: String, context: Map[String, String]): Boolean = {
    val normalized = Option(condition).map(_.trim).getOrElse("")
    if (normalized.isEmpty) {
      false
    } else {
      val expr = stripExpressionWrapper(normalized)
      if (expr.equalsIgnoreCase("true")) return true
      if (expr.equalsIgnoreCase("false")) return false
      val operators = Seq("==", "!=", ">=", "<=", ">", "<")
      operators.collectFirst {
        case operator if expr.contains(operator) =>
          val parts = expr.split(java.util.regex.Pattern.quote(operator), 2).map(_.trim)
          if (parts.length != 2) false else compare(resolveValue(parts(0), context), resolveValue(parts(1), context), operator)
      }.getOrElse {
        val resolved = resolveValue(expr, context)
        resolved.equalsIgnoreCase("true") || resolved.nonEmpty
      }
    }
  }

  private def stripExpressionWrapper(expression: String): String = {
    if (expression.startsWith("${") && expression.endsWith("}")) {
      expression.substring(2, expression.length - 1).trim
    } else expression
  }

  private def resolveValue(token: String, context: Map[String, String]): String = {
    val normalized = token.trim
    val unquoted = normalized.stripPrefix("\"").stripSuffix("\"").stripPrefix("'").stripSuffix("'")
    context.getOrElse(normalized, context.getOrElse(unquoted, unquoted))
  }

  private def compare(left: String, right: String, operator: String): Boolean = {
    (toBigDecimal(left), toBigDecimal(right)) match {
      case (Some(l), Some(r)) =>
        operator match {
          case "==" => l == r
          case "!=" => l != r
          case ">" => l > r
          case "<" => l < r
          case ">=" => l >= r
          case "<=" => l <= r
        }
      case _ =>
        operator match {
          case "==" => left == right
          case "!=" => left != right
          case ">" => left > right
          case "<" => left < right
          case ">=" => left >= right
          case "<=" => left <= right
        }
    }
  }

  private def toBigDecimal(value: String): Option[BigDecimal] = {
    try {
      Some(BigDecimal(value))
    } catch {
      case _: Throwable => None
    }
  }
}
