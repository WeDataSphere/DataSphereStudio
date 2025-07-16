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
package com.webank.wedatasphere.dss.orange.tag;

import com.webank.wedatasphere.dss.orange.node.ForeachSqlNode;
import com.webank.wedatasphere.dss.orange.node.MixedSqlNode;
import com.webank.wedatasphere.dss.orange.node.SqlNode;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;


public class ForeachHandler implements TagHandler {
    @Override
    public void handle(Element element, List<SqlNode> targetContents) {
        List<SqlNode> contents = XmlParser.parseElement(element);

        String open = element.attributeValue("open");
        String close = element.attributeValue("close");
        String collection = element.attributeValue("collection");
        String separator = element.attributeValue("separator");
        String item = element.attributeValue("item");
        String index = element.attributeValue("index");

        if (StringUtils.isBlank(collection)) {
            throw new RuntimeException("<foreach> attribute missing : collection");
        }
        if (StringUtils.isBlank(item)) {
            item = "item";
        }
        if (StringUtils.isBlank(index)) {
            index = "index";
        }

        ForeachSqlNode foreachSqlNode = new ForeachSqlNode(collection, open, close, separator, item, index, new MixedSqlNode(contents));
        targetContents.add(foreachSqlNode);

    }
}
