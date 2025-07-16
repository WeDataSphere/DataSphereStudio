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
package com.webank.wedatasphere.dss.framework.admin.restful;

import com.webank.wedatasphere.dss.common.utils.GlobalLimitsUtils;
import org.apache.linkis.server.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author enjoyyin
 * @date 2022-03-29
 * @since 0.5.0
 */
@RequestMapping(path = "/dss/framework/admin", produces = {"application/json"})
@RestController
public class DSSFeatureAuthRestfulApi {

    @RequestMapping(value = "/globalLimits",method = RequestMethod.GET)
    public Message globalLimits() {
        return Message.ok().data("globalLimits", GlobalLimitsUtils.getAllGlobalLimits());
    }

    @RequestMapping(value = "/globalLimits/{globalLimitName}",method = RequestMethod.GET)
    public Message globalLimit(@PathVariable("globalLimitName") String globalLimitName) {
        return Message.ok().data("globalLimitName", globalLimitName)
                .data("content", GlobalLimitsUtils.getGlobalLimitMap(globalLimitName));
    }

}
