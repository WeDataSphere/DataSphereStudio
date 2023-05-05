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

package com.webank.wedatasphere.dss.appconn.schedulis.conversion;

import com.webank.wedatasphere.dss.appconn.schedulis.SchedulisAppConn;
import com.webank.wedatasphere.dss.appconn.schedulis.entity.AzkabanConvertedRel;
import com.webank.wedatasphere.dss.appconn.schedulis.utils.SchedulisHttpUtils;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.ZipHelper;
import com.webank.wedatasphere.dss.orchestrator.converter.standard.operation.DSSToRelConversionOperation;
import com.webank.wedatasphere.dss.orchestrator.converter.standard.ref.ProjectToRelConversionRequestRef;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.sso.origin.request.action.DSSUploadAction;
import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestService;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.ProjectResponseRef;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ConvertedRel;
import com.webank.wedatasphere.dss.workflow.conversion.operation.WorkflowToRelSynchronizer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.linkis.httpclient.request.BinaryBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzkabanWorkflowToRelSynchronizer implements WorkflowToRelSynchronizer {

    public static final Logger LOGGER = LoggerFactory.getLogger(AzkabanWorkflowToRelSynchronizer.class);

    private String projectUrl;
    private DSSToRelConversionOperation dssToRelConversionOperation;
    //匹配wtss返回的错误信息
    private static final Pattern ERROR_PATTERN = Pattern.compile("(?<=Error uploading project properties)[\\s\\S]+.job");
    private static final int SCHEDULIS_MAX_SIZE = 250;

    public void init() {
        String baseUrl = dssToRelConversionOperation.getConversionService().getAppInstance().getBaseUrl();
        this.projectUrl = baseUrl.endsWith("/") ? baseUrl + "manager" : baseUrl + "/manager";
    }

    @Override
    public void setDSSToRelConversionOperation(DSSToRelConversionOperation dssToRelConversionOperation) {
        this.dssToRelConversionOperation = dssToRelConversionOperation;
        init();
    }

    @Override
    public void syncToRel(ConvertedRel convertedRel) {
        String tmpSavePath;
        AzkabanConvertedRel azkabanConvertedRel = (AzkabanConvertedRel) convertedRel;
        ProjectToRelConversionRequestRef projectToRelConversionRequestRef = azkabanConvertedRel.getDSSToRelConversionRequestRef();
        try {
            String projectName = projectToRelConversionRequestRef.getDSSProject().getName();
            //前置检查，若项目在schedulis不存在，直接返回
            if (!searchProjectExists(projectToRelConversionRequestRef.getDSSProject().getName(), projectToRelConversionRequestRef.getWorkspace())) {
                throw new DSSRuntimeException(90012, "the project: " + projectName + " is not exists in schedulis.(项目在schedulis不存在，请检查是否在schedulis中已被删除)");
            }
            String projectPath = azkabanConvertedRel.getStorePath();
            tmpSavePath = ZipHelper.zip(projectPath);
            //upload zip to Azkaban
            uploadProject(projectToRelConversionRequestRef.getWorkspace(), tmpSavePath,
                    projectToRelConversionRequestRef.getDSSProject().getName(),
                    projectToRelConversionRequestRef.getUserName(),
                    projectToRelConversionRequestRef.getApprovalId());
        } catch (Exception e) {
            throw new DSSRuntimeException(90012, ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    private boolean searchProjectExists(String projectName, Workspace workspace) {
        LOGGER.info("begin to search Schedulis project before upload, projectName is {}.", projectName);
        Map<String, Object> params = new HashMap<>(2);
        params.put("project", projectName);
        params.put("ajax", "fetchprojectflows");
        try {
            String responseBody = SchedulisHttpUtils.getHttpGetResult(projectUrl, params,
                    dssToRelConversionOperation.getConversionService().getSSORequestService().createSSORequestOperation(SchedulisAppConn.SCHEDULIS_APPCONN_NAME),
                    workspace);
            LOGGER.info("responseBody from Schedulis is: {}.", responseBody);
            Map<String, Object> map = DSSCommonUtils.COMMON_GSON.fromJson(responseBody, Map.class);
            String errorInfo = (String) map.get("error");
            if (errorInfo != null && (errorInfo.contains("Project " + projectName + " doesn't exist")
                    //schedulis已删除但未永久删除的项目返回这个
                    || errorInfo.contains("Permission denied. Need READ access"))) {
                return false;
            } else if (errorInfo != null) {
                throw new ExternalOperationFailedException(90012, errorInfo);
            }
            return true;
        } catch (Exception e) {
            throw new ExternalOperationFailedException(90117, "Failed to search Schedulis project name!", e);
        }
    }

    private void uploadProject(Workspace workspace, String tmpSavePath, String projectName, String releaseUser, String approvalId) throws Exception {

        File file = new File(tmpSavePath);
        InputStream inputStream = new FileInputStream(file);
        try {
            BinaryBody binaryBody = BinaryBody.apply("file", inputStream, file.getName(), "application/zip");
            List<BinaryBody> binaryBodyList = new ArrayList<>();
            binaryBodyList.add(binaryBody);
            DSSUploadAction uploadAction = new DSSUploadAction(binaryBodyList);
            uploadAction.getFormParams().put("ajax", "upload");
            uploadAction.getParameters().put("ajax", "upload");

            uploadAction.getFormParams().put("project", projectName);
            uploadAction.getParameters().put("project", projectName);

            if (StringUtils.isNotBlank(approvalId)) {
                uploadAction.getFormParams().put("itsmId", approvalId);
                uploadAction.getParameters().put("itsmId", approvalId);
            }
            uploadAction.setUrl(projectUrl);
            String body =
                    SchedulisHttpUtils.getHttpResult(projectUrl, uploadAction,
                            dssToRelConversionOperation.getConversionService().getSSORequestService()
                                    .createSSORequestOperation(SchedulisAppConn.SCHEDULIS_APPCONN_NAME), workspace);
            if (body != null && DSSCommonUtils.COMMON_GSON.fromJson(body, Map.class).get("error") != null) {
                throw new ExternalOperationFailedException(50063, "upload project to schedulis failed." + body);
            }
        } catch (Exception e) {
            throw new DSSRuntimeException(90012, dealSchedulisErrorMsg(ExceptionUtils.getRootCauseMessage(e)));
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private String dealSchedulisErrorMsg(String errorMsg) {
        Matcher matcher = ERROR_PATTERN.matcher(errorMsg);
        if (matcher.find() && matcher.group().length() >= SCHEDULIS_MAX_SIZE) {
            errorMsg = "wokflow name " + matcher.group().split("/")[1] + " is to long, please abide the rules of schedulis: projectName + workflowName*3 + 12 <= 250 ";
        }
        return errorMsg;
    }
}
