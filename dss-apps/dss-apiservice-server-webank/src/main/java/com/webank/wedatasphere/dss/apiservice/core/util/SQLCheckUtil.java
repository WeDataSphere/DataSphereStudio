/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.apiservice.core.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: jinyangrao
 */
public class SQLCheckUtil {

    // sql 注入检查
    private static String injectionReg = "(?:--)|" +
                        "(\\b(select|update|union|and|or|delete|insert|trancate|char|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
    private static Pattern paramInjectionPattern = Pattern.compile(injectionReg, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 1. -- 注释
     * 2. select * from table -- 注释
     * 但是不包含 -- '注释'
     * */
    private static String sqlCommentReg = "\\-\\-([^\\'\\r\\n]{0,}(\\'[^\\'\\r\\n]{0,}\\'){0,1}[^\\'\\r\\n]{0,}){0,}";
    private static Pattern sqlCommentPattern = Pattern.compile(sqlCommentReg, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * PySpark pattern
     * 使用正则表达式匹配 spark.sql(...) 或sqlContext(...)调用中的参数
     */
    private static final Pattern PYSPARK_PATTERN = Pattern.compile("(?:^|\\n|\\r\\n|\\r)[^\\r\\n#]*?(spark|sqlContext)\\.sql\\(\\s*" +
            "(?:f?\"\"\"((?s:.*?))\"\"\"(?:\\.format\\([^)]*\\))?" +  // 三引号双引号，支持 .format()
            "|f?'''((?s:.*?))'''(?:\\.format\\([^)]*\\))?" +         // 三引号单引号，支持 .format()
            "|f?\"((?:\\\\.|[^\"\\\\])*)\"(?:\\.format\\([^)]*\\))?" + // 普通双引号，支持 .format()
            "|f?'((?:\\\\.|[^'\\\\])*)'(?:\\.format\\([^)]*\\))?" +    // 普通单引号，支持 .format()
            "|(\\w+)(?:\\.format\\([^)]*\\))?" +                                             // 变量名 支持 .format()
            ")\\s*\\)", Pattern.CASE_INSENSITIVE);
    /**
     * PySpark assign pattern
     * 开启多行模式和 DOTALL 模式，不使用 ^ 限制变量必须出现在行首
     * 正则解释：
     * (\w+)\s*=\s*  匹配变量名及赋值符号
     * (?:"""((?:(?!""").)*)"""|'''((?:(?!''').)*)''')
     * 分别捕获三双引号和三单引号包围的内容
     */
    private static final Pattern PYSPARK_ASSIGN_PATTERN = Pattern.compile("(?m)[^\\r\\n#]*?(\\w+)\\s*=\\s*" +
            "(?:f?\"\"\"((?s:(?!\"\"\").)*)\"\"\"" +
            "|f?'''((?s:(?!''').)*)'''" +
            "|f?\"((?:\\\\.|[^\"\\\\])*)\"" +
            "|f?'((?:\\\\.|[^'\\\\])*)'" +
            ")", Pattern.CASE_INSENSITIVE);

    public static final String pySpark2Suffix=".py";

    public static final String pySpark3Suffix=".py3";

    public static final  String jdbcSuffix = ".jdbc";


    public static boolean doParamInjectionCheck(String str) {
        Matcher matcher = paramInjectionPattern.matcher(str);
        if(matcher.find()) {
            return true;
        }
        return false;
    }

    public static String sqlCommentReplace(String sql) {
        String newSql = sql.replaceAll(sqlCommentReg, "");
        return newSql;
    }

    public static void main(String[] args) {
        String sql = "--注释\n" +
                     "-- 注释\n" +
                     "select * from tb --注释\n";
        String newSql = sqlCommentReplace(sql);
        String[] selects = newSql.split("select");
        System.out.println(newSql);
        System.out.println(selects.length);
    }


    public static boolean isPySpark(String script){
        return isPyspark2(script) || isPyspark3(script);
    }

    public static boolean isPyspark3(String script){
        return StringUtils.endsWithIgnoreCase(script,pySpark3Suffix);
    }

    public static boolean isPyspark2(String script){
        return StringUtils.endsWithIgnoreCase(script,pySpark2Suffix);
    }


    public static boolean isJdbc(String script){

        return  StringUtils.endsWithIgnoreCase(script,jdbcSuffix);
    }

    public static String parsePySparkSql(String code){

        StringBuilder pySparkSql = new StringBuilder();

        Map<String, List<VarAssignment>> varMap = extractAssignedSqlVariables(code);

        Matcher matcher = PYSPARK_PATTERN.matcher(code);


        while (matcher.find()) {

            String sql = null;
            int referPos = -1;
            // 用spark.sql/sparkContext.sql的位置作为引用行号
            referPos = matcher.start(1);
            if (matcher.group(2) != null) {
                sql = matcher.group(2);
            } else if (matcher.group(3) != null) {
                sql = matcher.group(3);
            } else if (matcher.group(4) != null) {
                sql = matcher.group(4);

            } else if (matcher.group(5) != null) {
                sql = matcher.group(5);
            } else if (matcher.group(6) != null) {
                String varName = matcher.group(6);
                List<VarAssignment> assignments = varMap.get(varName);
                if (assignments != null) {
                    for (VarAssignment assign : assignments){
                        if (assign.getStartPos() < referPos){
                            sql = assign.getValue();
                        }
                    }
                }
            }

            if (sql == null || sql.isEmpty() || StringUtils.startsWithIgnoreCase(sql.trim(),"set")) {
                continue;
            }

            pySparkSql.append(sql);

            if(!sql.endsWith(";")){
                pySparkSql.append(";");
            }
        }

        return  pySparkSql.toString();

    }


    public  static  Map<String, List<VarAssignment>> extractAssignedSqlVariables(String code) {
        Map<String, List<VarAssignment>> varMap = new HashMap<>();
        Matcher matcher = PYSPARK_ASSIGN_PATTERN.matcher(code);
        while (matcher.find()) {
            String varName = matcher.group(1);
            // 变量位置
            int varPos = matcher.start(1);
            // 三双引号内容在 group2，三单引号内容在 group3
            String value = null;
            int startPos = 0, endPos = 0;
            if (matcher.group(2) != null){
                value = matcher.group(2);
                startPos = matcher.start(2);
                endPos = matcher.end(2) - 1;
            } else if (matcher.group(3) != null){
                value = matcher.group(3);
                startPos = matcher.start(3);
                endPos = matcher.end(3) - 1;
            } else if (matcher.group(4) != null){
                value = matcher.group(4);
                startPos = matcher.start(4);
                endPos = matcher.end(4) - 1;
            } else if (matcher.group(5) != null){
                value = matcher.group(5);
                startPos = matcher.start(5);
                endPos = matcher.end(5) - 1;
            }
            if (value != null) {
                // 计算赋值语句在原始代码中的起止位置
                VarAssignment assign = new VarAssignment(varName, value, varPos, startPos, endPos);
                varMap.compute(varName, (name, assigns) -> {
                    if (null == assigns){
                        assigns = new ArrayList<>();
                    }
                    assigns.add(assign);
                    return assigns;
                });
            }
        }
        return varMap;
    }



    public static class VarAssignment {
        private String varName;
        private String value;
        private int varPos;
        private int startPos;
        private int endPos;
        public VarAssignment(String varName, String value, int varPos, int startPos, int endPos) {
            this.varName = varName;
            this.value = value;
            this.varPos = varPos;
            this.startPos = startPos;
            this.endPos = endPos;
        }

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getVarPos() {
            return varPos;
        }

        public void setVarPos(int varPos) {
            this.varPos = varPos;
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public void setEndPos(int endPos) {
            this.endPos = endPos;
        }
    }


}