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
package com.webank.wedatasphere.dss.appconn.scheduler.utils;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.OrchestrationService;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.DSSOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.StructureOperation;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.webank.wedatasphere.dss.standard.app.structure.utils.StructureOperationUtils.tryStructureOperation;

/**
 * @author enjoyyin
 * @date 2022-03-14
 * @since 1.1.0
 */
public class OrchestrationOperationUtils {

    public static <K extends StructureRequestRef, V extends ResponseRef> V tryOrchestrationOperation(Supplier<OrchestrationService> getOrchestrationService,
                                                                                               Function<OrchestrationService, StructureOperation> getOrchestrationOperation,
                                                                                               Consumer<DSSOrchestrationContentRequestRef> dssOrchestrationContentRequestRefConsumer,
                                                                                               Consumer<RefOrchestrationContentRequestRef> refOrchestrationContentRequestRefConsumer,
                                                                                               BiFunction<StructureOperation, K, V> responseRefConsumer,
                                                                                               String errorMsg) {
        return tryStructureOperation(getOrchestrationService, getOrchestrationOperation, structureRequestRef -> {
            if(dssOrchestrationContentRequestRefConsumer != null && structureRequestRef instanceof DSSOrchestrationContentRequestRef) {
                dssOrchestrationContentRequestRefConsumer.accept((DSSOrchestrationContentRequestRef) structureRequestRef);
            }
            if(refOrchestrationContentRequestRefConsumer != null && structureRequestRef instanceof RefOrchestrationContentRequestRef) {
                refOrchestrationContentRequestRefConsumer.accept((RefOrchestrationContentRequestRef) structureRequestRef);
            }
        }, responseRefConsumer, errorMsg);
    }
    
}
