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
import org.apache.linkis.common.utils.Logging

import scala.collection.JavaConversions._
import scala.collection.mutable

object BranchExpressionUtils extends Logging {

  val BranchNodeType = "workflow.branch"
  val BranchRuleKey = "branch.rules"
  private val ConditionPrefix = "condition."
  private val OnSuccessPrefix = "on.success."
  private val OnFailurePrefix = "on.failure."

  case class BranchRule(condition: String, targetName: Option[String], onFailureTarget: Option[String])
  private case class IndexedBranchRule(condition: Option[String] = None, targetName: Option[String] = None, onFailure: Option[String] = None)

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

  def getBranchRuleText(node: WorkflowNode): String = {
    val params = Option(node).map(_.getDSSNode).map(_.getParams).orNull
    params match {
      case map: util.Map[_, _] =>
        getStringValue(map.get(BranchRuleKey))
          .orElse {
            map.get("configuration") match {
              case configuration: util.Map[_, _] =>
                configuration.get("special") match {
                  case special: util.Map[_, _] => getStringValue(special.get(BranchRuleKey))
                  case _ => None
                }
              case _ => None
            }
          }.getOrElse("")
      case _ => ""
    }
  }

  def parseBranchRules(raw: String): Seq[BranchRule] = {
    val indexedRules = mutable.LinkedHashMap[Int, IndexedBranchRule]()
    Option(raw).map(_.split("[\\r\\n;]+").toSeq).getOrElse(Seq.empty)
      .map(_.trim)
      .filter(_.nonEmpty)
      .foreach { line =>
        parseBranchRuleEntry(line).foreach { case (ruleType, index, ruleValue) =>
          val current = indexedRules.getOrElse(index, IndexedBranchRule())
          val updated = ruleType match {
            case ConditionPrefix => current.copy(condition = Some(ruleValue))
            case OnSuccessPrefix => current.copy(targetName = Some(ruleValue))
            case OnFailurePrefix => current.copy(onFailure = Some(ruleValue))
          }
          indexedRules.put(index, updated)
        }
      }
    indexedRules.toSeq.sortBy(_._1).flatMap { case (_, rule) =>
      (rule.condition.map(_.trim), rule.targetName.map(_.trim).filter(_.nonEmpty), rule.onFailure.map(_.trim).filter(_.nonEmpty)) match {
        case (Some(condition), targetName, onFailureTarget)
          if condition.nonEmpty && !isUnsupportedDefaultKeyword(condition) && (targetName.isDefined || onFailureTarget.isDefined) =>
          Some(BranchRule(condition, targetName, onFailureTarget))
        case _ => None
      }
    }
  }

  private def parseBranchRuleEntry(line: String): Option[(String, Int, String)] = {
    val separatorIndex = Option(line).map(_.indexOf('=')).getOrElse(-1)
    if (separatorIndex <= 0) {
      warn(s"Invalid branch rule syntax: $line")
      None
    } else {
      val key = line.substring(0, separatorIndex).trim
      val value = line.substring(separatorIndex + 1)
      parseIndexedRuleKey(key) match {
        case Some((ruleType, index)) => Some((ruleType, index, value))
        case None =>
          warn(s"Invalid branch rule syntax: $line")
          None
      }
    }
  }

  private def parseIndexedRuleKey(key: String): Option[(String, Int)] = {
    if (key.startsWith(ConditionPrefix)) {
      parseRuleIndex(key.substring(ConditionPrefix.length)).map(index => (ConditionPrefix, index))
    } else if (key.startsWith(OnSuccessPrefix)) {
      parseRuleIndex(key.substring(OnSuccessPrefix.length)).map(index => (OnSuccessPrefix, index))
    } else if (key.startsWith(OnFailurePrefix)) {
      parseRuleIndex(key.substring(OnFailurePrefix.length)).map(index => (OnFailurePrefix, index))
    } else {
      None
    }
  }

  private def parseRuleIndex(rawIndex: String): Option[Int] = {
    try {
      Some(rawIndex.trim.toInt)
    } catch {
      case _: Throwable => None
    }
  }

  def evaluateCondition(condition: String, context: Map[String, String]): Boolean = {
    val normalized = Option(condition).map(_.trim).getOrElse("")
    if (normalized.isEmpty) {
      false
    } else {
      if (normalized.startsWith("${") && normalized.endsWith("}")) {
        warn(s"Invalid branch condition syntax, wrapper `$${...}` is not allowed: $condition")
        return false
      }
      val expr = normalized
      if (isUnsupportedDefaultKeyword(expr)) return false
      val operators = Seq("==", "!=", ">=", "<=", ">", "<")
      if (!operators.exists(expr.contains)) {
        warn(s"Invalid branch condition syntax, explicit comparison is required: $condition")
        return false
      }
      operators.collectFirst {
        case operator if expr.contains(operator) =>
          val parts = expr.split(java.util.regex.Pattern.quote(operator), 2).map(_.trim)
          if (parts.length != 2) {
            warn(s"Invalid branch condition syntax: $condition")
            false
          } else {
            (resolveValue(parts(0), context), resolveValue(parts(1), context)) match {
              case (Some(left), Some(right)) =>
                val matched = compare(left, right, operator)
                info(s"Branch condition evaluated: expr=$expr, left=$left, operator=$operator, right=$right, matched=$matched")
                matched
              case _ =>
                warn(s"Branch condition unresolved token: expr=$expr, leftToken=${parts(0)}, rightToken=${parts(1)}, contextKeys=${context.keys.toSeq.sorted.mkString(",")}")
                false
            }
          }
      }.getOrElse(false)
    }
  }

  private def stripExpressionWrapper(expression: String): String = {
    if (expression.startsWith("${") && expression.endsWith("}")) {
      expression.substring(2, expression.length - 1).trim
    } else expression
  }

  private def resolveValue(token: String, context: Map[String, String]): Option[String] = {
    val normalized = Option(token).map(_.trim).getOrElse("")
    if (normalized.isEmpty) {
      None
    } else {
      val unquoted = normalized.stripPrefix("\"").stripSuffix("\"").stripPrefix("'").stripSuffix("'")
      if (isQuotedToken(normalized)) {
        Some(unquoted)
      } else {
        context.get(normalized)
          .orElse(context.get(unquoted))
          .orElse(if (isLiteralToken(unquoted)) Some(unquoted) else None)
      }
    }
  }

  private def isQuotedToken(token: String): Boolean = {
    (token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))
  }

  private def isLiteralToken(token: String): Boolean = {
    token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false") || toBigDecimal(token).nonEmpty
  }

  private def getStringValue(value: Any): Option[String] = Option(value).map(_.toString.trim).filter(_.nonEmpty)
  private def isUnsupportedDefaultKeyword(condition: String): Boolean = {
    val normalized = Option(condition).map(_.trim.toLowerCase).getOrElse("")
    normalized == "default" || normalized == "else" || normalized == "*"
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
          case ">" | "<" | ">=" | "<=" =>
            warn(s"Branch numeric comparison requires numeric operands: left=$left, operator=$operator, right=$right")
            false
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


