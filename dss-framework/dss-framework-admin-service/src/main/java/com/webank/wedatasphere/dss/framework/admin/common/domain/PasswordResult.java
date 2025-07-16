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
package com.webank.wedatasphere.dss.framework.admin.common.domain;


public enum PasswordResult  {

    PASSWORD_RULE_PASS("11000", "密码校验通过"),

    PASSWORD_STRENGTH_ERROR("11002", "密码应包括数字、小写字母、大写字母和特殊符号四种类型字符（长度为8-26位）"),

    PASSWORD_RELATED_USERNAME_ERROR("11003", "密码应与用户名无关"),

    PASSWORD_STARTER_ERROR("11004", "密码须以字母开头"),

    PASSWORD_WITH_DICT_WORD_ERROR("11005", "密码中不应出现弱密码字典中的禁用字段"),

    PASSWORD_KEYBOARD_CONTINUOUS_ERROR("11006", "密码中不应出现键盘序");

    private final String code;

    private final String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }



    PasswordResult(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
