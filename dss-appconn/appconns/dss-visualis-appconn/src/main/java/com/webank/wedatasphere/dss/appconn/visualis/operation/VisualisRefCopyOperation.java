package com.webank.wedatasphere.dss.appconn.visualis.operation;

import com.webank.wedatasphere.dss.appconn.visualis.model.VisualisPostAction;
import com.webank.wedatasphere.dss.appconn.visualis.utils.URLUtils;
import com.webank.wedatasphere.dss.standard.app.development.operation.RefCopyOperation;
import com.webank.wedatasphere.dss.standard.app.development.ref.RefJobContentResponseRef;
import com.webank.wedatasphere.dss.standard.app.development.ref.impl.ThirdlyRequestRef;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;

public class VisualisRefCopyOperation
        extends VisualisDevelopmentOperation<ThirdlyRequestRef.CopyWitContextRequestRefImpl, RefJobContentResponseRef>
        implements RefCopyOperation<ThirdlyRequestRef.CopyWitContextRequestRefImpl> {

    @Override
    public RefJobContentResponseRef copyRef(ThirdlyRequestRef.CopyWitContextRequestRefImpl requestRef) throws ExternalOperationFailedException {
        String url = getBaseUrl() + URLUtils.PROJECT_COPY_URL;
        String nodeType = requestRef.getType().toLowerCase();
        logger.info("The {} of Visualis try to copy ref RefJobContent: {} in url {}.", nodeType, requestRef.getRefJobContent(), url);

        VisualisPostAction visualisPostAction = new VisualisPostAction();
        visualisPostAction.setUser(requestRef.getUserName());
        visualisPostAction.addRequestPayload("projectVersion", "v1");
        visualisPostAction.addRequestPayload("flowVersion", requestRef.getNewVersion());
        visualisPostAction.addRequestPayload("contextID", requestRef.getContextId());
        return OperationStrategyFactory.getInstance()
                .getOperationStrategy(getDevelopmentService().getAppInstance(), nodeType)
                .copyRef(requestRef, url, visualisPostAction);
    }
}