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
package com.webank.wedatasphere.dss.standard.app.development.ref;

import java.util.Map;

/**
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface DSSJobContentRequestRef<R extends DSSJobContentRequestRef<R>>
        extends DevelopmentRequestRef<R> {

    /**
     * When some DevelopmentOperations, such as {@code RefCreationOperation}, {@code RefImportOperation} are called, the dssJobContent will be set in, so we can use it to
     * create a one-to-one only related third appConn refJob.
     * <br/>
     *     {@code DSSJobContentConstant} contains all the keys of {@code dssJobContent}, please see {@code DSSJobContentConstant} to know all these keys
     *     DSS will fill in.
     * <br/>
     * This method is usually called by DSS framework, the third-part AppConn just need to use {@code getDSSJobContent()} to
     * get the dssJobContent to create a one-to-one only related third appConn refJob.
     * @param dssJobContent a dssJobContent created by users in DSS front-web.
     * @return this class
     */
    default R setDSSJobContent(Map<String, Object> dssJobContent) {
        setParameter("dssJobContent", dssJobContent);
        return (R) this;
    }

    /**
     * This is the content of the DSS job content, when users try to create a DSS job in DSS front-web.
     * the third-part AppConn usually will call this method to get dssJobContent to do some operation.
     * @return a dssJobContent related a dss job created by dss framework.
     */
    default Map<String, Object> getDSSJobContent() {
        return (Map<String, Object>) getParameter("dssJobContent");
    }

}
