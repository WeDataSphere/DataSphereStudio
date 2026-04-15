package com.webank.wedatasphere.dss.plugins.azkaban.linkis.jobtype.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchExpressionUtils {

    private static final Set<String> DEFAULT_RULE_VALUES = new HashSet<>(Arrays.asList("default", "else", "*"));

    private BranchExpressionUtils() {
    }

    public static List<BranchRule> parseBranchRules(String raw) {
        List<BranchRule> rules = new ArrayList<>();
        if (isBlank(raw)) {
            return rules;
        }
        String[] lines = raw.split("[\\r\\n;]+");
        for (String line : lines) {
            BranchRule branchRule = parseBranchRule(line);
            if (branchRule != null) {
                rules.add(branchRule);
            }
        }
        return rules;
    }


    public static boolean evaluateCondition(String condition, Map<String, String> context) {
        String normalized = trimToEmpty(condition);
        if (normalized.isEmpty()) {
            return false;
        }
        String expr = stripExpressionWrapper(normalized);
        if ("true".equalsIgnoreCase(expr)) {
            return true;
        }
        if ("false".equalsIgnoreCase(expr)) {
            return false;
        }
        if (isUnsupportedDefaultKeyword(expr)) {
            return false;
        }
        String[] operators = new String[]{"==", "!=", ">=", "<=", ">", "<"};
        for (String operator : operators) {
            int index = expr.indexOf(operator);
            if (index > 0) {
                String leftToken = expr.substring(0, index).trim();
                String rightToken = expr.substring(index + operator.length()).trim();
                String leftValue = resolveValue(leftToken, context);
                String rightValue = resolveValue(rightToken, context);
                if (leftValue == null || rightValue == null) {
                    return false;
                }
                return compare(leftValue, rightValue, operator);
            }
        }
        String resolved = resolveValue(expr, context);
        return resolved != null && (!resolved.isEmpty() || "true".equalsIgnoreCase(resolved));
    }

    private static BranchRule parseBranchRule(String line) {
        if (isBlank(line)) {
            return null;
        }
        int separatorIndex = line.lastIndexOf('=');
        if (separatorIndex <= 0 || separatorIndex >= line.length() - 1) {
            return null;
        }
        String condition = line.substring(0, separatorIndex).trim();
        String targetName = line.substring(separatorIndex + 1).trim();
        if (condition.isEmpty() || targetName.isEmpty() || isUnsupportedDefaultKeyword(condition)) {
            return null;
        }
        return new BranchRule(condition, targetName);
    }

    private static String stripExpressionWrapper(String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            return expression.substring(2, expression.length() - 1).trim();
        }
        return expression;
    }

    private static String resolveValue(String token, Map<String, String> context) {
        String normalized = trimToEmpty(token);
        if (normalized.isEmpty()) {
            return null;
        }
        String unquoted = normalized.replaceAll("^['\"]|['\"]$", "");
        if (isQuotedToken(normalized)) {
            return unquoted;
        }
        if (context != null) {
            String value = context.get(normalized);
            if (value == null) {
                value = context.get(unquoted);
            }
            if (value != null) {
                return value;
            }
        }
        if (isLiteralToken(unquoted)) {
            return unquoted;
        }
        return null;
    }

    private static boolean compare(String left, String right, String operator) {
        BigDecimal leftDecimal = toBigDecimal(left);
        BigDecimal rightDecimal = toBigDecimal(right);
        if (leftDecimal != null && rightDecimal != null) {
            int compare = leftDecimal.compareTo(rightDecimal);
            switch (operator) {
                case "==":
                    return compare == 0;
                case "!=":
                    return compare != 0;
                case ">":
                    return compare > 0;
                case "<":
                    return compare < 0;
                case ">=":
                    return compare >= 0;
                case "<=":
                    return compare <= 0;
                default:
                    return false;
            }
        }
        switch (operator) {
            case "==":
                return left.equals(right);
            case "!=":
                return !left.equals(right);
            case ">":
                return left.compareTo(right) > 0;
            case "<":
                return left.compareTo(right) < 0;
            case ">=":
                return left.compareTo(right) >= 0;
            case "<=":
                return left.compareTo(right) <= 0;
            default:
                return false;
        }
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isQuotedToken(String token) {
        return (token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"));
    }

    private static boolean isLiteralToken(String token) {
        return "true".equalsIgnoreCase(token) || "false".equalsIgnoreCase(token) || toBigDecimal(token) != null;
    }

    private static boolean isUnsupportedDefaultKeyword(String condition) {
        if (isBlank(condition)) {
            return false;
        }
        String normalized = trimToEmpty(condition).toLowerCase();
        return "default".equals(normalized) || "else".equals(normalized) || "*".equals(normalized);
    }
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }


    public static class BranchRule {
        private final String condition;
        private final String targetName;

        public BranchRule(String condition, String targetName) {
            this.condition = condition;
            this.targetName = targetName;
        }

        public String getCondition() {
            return condition;
        }

        public String getTargetName() {
            return targetName;
        }
    }
}
