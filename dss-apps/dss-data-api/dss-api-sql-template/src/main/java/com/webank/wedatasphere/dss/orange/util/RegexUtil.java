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
package com.webank.wedatasphere.dss.orange.util;

import java.util.regex.Pattern;


public class RegexUtil {

    public static String replace(String content, String item, String newItem) {
        return content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])", newItem);
    }

    public static void main(String[] args) {
        boolean matches = "item".matches( "item" + "[.,:\\s\\[]");

        boolean item = Pattern.compile("item[.,:\\s\\[]").matcher("item").matches();

//        String aa = "item[0].name".replaceFirst("^\\s*" + "item" + "(?![^.,:\\s])", "aa");
//        System.out.println(aa);
        System.out.println(item);
    }
}
