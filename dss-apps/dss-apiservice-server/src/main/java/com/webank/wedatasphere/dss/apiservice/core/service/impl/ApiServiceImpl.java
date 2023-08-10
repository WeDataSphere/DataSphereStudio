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
package com.webank.wedatasphere.dss.apiservice.core.service.impl;

import com.google.common.base.Splitter;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceQuery;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceToken;
import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiVersionStatusEnum;
import com.webank.wedatasphere.dss.apiservice.core.constant.SQLMetadataInfoCheckStatus;
import com.webank.wedatasphere.dss.apiservice.core.dao.*;
import com.webank.wedatasphere.dss.apiservice.core.datamap.DataMapApplyContentData;
import com.webank.wedatasphere.dss.apiservice.core.datamap.DataMapStatus;
import com.webank.wedatasphere.dss.apiservice.core.datamap.DataMapUtils;
import com.webank.wedatasphere.dss.apiservice.core.datamap.UUIDGenerator;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiExecuteException;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceParamsCheckException;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceQueryException;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceTokenException;
import com.webank.wedatasphere.dss.apiservice.core.execute.ExecuteCodeHelper;
import com.webank.wedatasphere.dss.apiservice.core.execute.LinkisJobSubmit;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiService;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalService;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalStatusListener;
import com.webank.wedatasphere.dss.apiservice.core.token.JwtManager;
import com.webank.wedatasphere.dss.apiservice.core.token.TokenAuth;
import com.webank.wedatasphere.dss.apiservice.core.util.ApiUtils;
import com.webank.wedatasphere.dss.apiservice.core.util.AssertUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.ibatis.annotations.Param;
import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.bml.protocol.BmlUpdateResponse;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.script.*;
import org.apache.linkis.storage.script.writer.StorageScriptFsWriter;
import org.apache.linkis.ujes.client.UJESClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiServiceImpl implements ApiService, ApprovalStatusListener {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceImpl.class);

    @Autowired
    private ApiServiceDao apiServiceDao;

    @Autowired
    private ApiServiceParamDao apiServiceParamDao;

    @Autowired
    private ApiServiceVersionDao apiServiceVersionDao;

    @Autowired
    private ApiServiceTokenManagerDao apiServiceTokenManagerDao;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    TokenAuth tokenAuth;

    @Autowired
    ApiServiceApprovalDao apiServiceApprovalDao;
    /**
     * Bml client
     */
    private BmlClient client;

    private UJESClient ujesClient;

    @PostConstruct
    public void init() {
        LOG.info("build client start ======");
        client = BmlClientFactory.createBmlClient();
        Map<String, String> props = new HashMap<>();
        ujesClient = LinkisJobSubmit.getClient(props);
        LOG.info("build client end =======");
        approvalService.registerApprovalStatusListener(this);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ApiServiceVo apiService) throws Exception {
        String user = apiService.getCreator();
        String resourceId = null;
        if (!checkUserWorkspace(apiService.getModifier(), apiService.getWorkspaceId().intValue())) {
            throw new ApiServiceParamsCheckException(800034, "创建数据服务工作空间检查失败，请确认工作空间ID是否正确! " + apiService.getWorkspaceId().intValue());
        }
        try {
            // check script path if already created
            String scriptPath = apiService.getScriptPath();
            // upload to bml
            Map<String, String> uploadResult = uploadBml(user, scriptPath, apiService.getMetadata(), apiService.getContent());
            // insert linkis_oneservice_config
            String version = uploadResult.get("version");
            resourceId = uploadResult.get("resourceId");
            apiServiceDao.insert(apiService);

            //insert into version
            ApiVersionVo apiVersionVo = new ApiVersionVo();
            apiVersionVo.setApiId(apiService.getId());
            apiVersionVo.setBmlResourceId(resourceId);
            apiVersionVo.setBmlVersion(version);
            apiVersionVo.setVersion(version);
            apiVersionVo.setCreator(user);
            apiVersionVo.setCreateTime(Calendar.getInstance().getTime());
            apiVersionVo.setSource(apiService.getScriptPath());
            //代理用户
            apiVersionVo.setExecuteUser(StringUtils.isEmpty(apiService.getExecuteUser())
                    ? apiService.getCreator() : apiService.getExecuteUser());
            //1为正常，0为禁用，2：未提单，3：已提单
            apiVersionVo.setStatus(ApiVersionStatusEnum.NOT_SUBMITTED.getIndex());
            apiVersionVo.setDescription(apiService.getDescription());
            apiVersionVo.setComment(apiService.getComment());
            //解析库表信息，并设置到apiVersionVo
            parseMetadataInfo(user, apiService, apiVersionVo);
            //顺序不能改变，版本信息依赖审批单信息
            apiServiceVersionDao.insert(apiVersionVo);

            // insert linkis_oneservice_params
            List<ParamVo> params = apiService.getParams();
            if (params != null && !params.isEmpty()) {
                for (ParamVo param : params) {
                    param.setApiVersionId(apiVersionVo.getId());
                    apiServiceParamDao.insert(param);
                }
            }
            genTokenForPublisher(apiService, apiVersionVo.getId(), ApiServiceConfiguration.API_TOKEN_FOREVER_DURATION.getValue());
        } catch (Exception e) {
            LOG.error("one service insert error", e);
            if (StringUtils.isNotBlank(resourceId)) {
//                removeBml(user, resourceId);
            }
            if (e.getCause() instanceof ErrorException) {
                throw (ErrorException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * 提单并修改数据库信息
     *
     * @param apiSubmitVo
     * @throws Exception 有异常时需要回滚数据库修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitToDm(ApiSubmitVo apiSubmitVo) throws Exception {
        String approvalNo = UUIDGenerator.genUUID();
        List<ApiVersionVo> submitApiVersionList = new ArrayList<>();
        for (ApiVersionIdVo apiVersionIdVo : apiSubmitVo.getSubmitApiInfos()) {
            ApiServiceVo apiServiceVo = apiServiceDao.queryById(apiVersionIdVo.getApiId());
            //最新的运行中和未提交版本
            ApiVersionVo apiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(apiVersionIdVo.getApiVersionId());
            submitApiVersionList.add(apiVersionVo);
            apiVersionVo.setAuthId(approvalNo);
            //更新auth_id
            apiServiceVersionDao.updateAuthIdByapiVersionId(apiVersionVo.getId(), approvalNo);
            //每个api version加一条approval记录
            addApprovalToDB(apiServiceVo, apiVersionVo, apiSubmitVo);
            //每个api插入创建者自己的token
//            genTokenForPublisher(apiServiceVo, apiVersionVo.getId(), apiSubmitVo.getDuration());
        }
        //发起dm提单
        sendApprovalToDM(approvalNo, apiSubmitVo, submitApiVersionList);
        // 修改为已提单状态
        submitApiVersionList.forEach(apiVersionVo -> {
            apiServiceVersionDao.updateApiVersionStatusById(apiVersionVo.getId(), ApiVersionStatusEnum.SUBMITTED.getIndex());
        });
    }

    @Override
    public void afterApprovalSuccess(ApprovalVo approvalVo) {
        ApiVersionVo apiVersionVo = apiServiceVersionDao.queryApiVersionByVersionId(approvalVo.getApiVersionId());
        //若此版本已被禁用，可能该api有更高版本先于此版本完成审批，则此版本不能改为运行中
        if (apiVersionVo.getStatus() != ApiVersionStatusEnum.DISABLE.getIndex()) {
            // 将该api小于此版本的历史版本状态改为禁用
            apiServiceVersionDao.updateApiVersionStatusBeforeId(approvalVo.getApiVersionId(), approvalVo.getApiId(), ApiVersionStatusEnum.DISABLE.getIndex());
            //将历史版本token禁用
            apiServiceTokenManagerDao.updateTokenStatusBeforeVersionId(approvalVo.getApiVersionId(), approvalVo.getApiId(), ApiCommonConstant.API_DISABLE_STATUS);
            //将此版本改为运行中
            apiServiceVersionDao.updateApiVersionStatusById(approvalVo.getApiVersionId(), ApiVersionStatusEnum.ENABLE.getIndex());
        } else {
            LOG.warn("api版本审批完成时已被禁用！apiVersionId为：{}, ApprovalName为：{}", approvalVo.getApiVersionId(), approvalVo.getApprovalName());
        }
    }

    @Override
    public void afterApprovalFailed(ApprovalVo approvalVo) {
        //审批不通过、作废时，此版本状态改为未提单，后续用户可继续基于此版本提单
        //这里可能审批中用户已经更新了数据服务，生成了一个新的未提单版本，那审批未通过的版本需要改为禁用
        if (getMaxVersion(approvalVo.getApiId()).getStatus() == ApiVersionStatusEnum.NOT_SUBMITTED.getIndex()) {
            apiServiceVersionDao.updateApiVersionStatusById(approvalVo.getApiVersionId(), ApiVersionStatusEnum.DISABLE.getIndex());
        } else {
            apiServiceVersionDao.updateApiVersionStatusById(approvalVo.getApiVersionId(), ApiVersionStatusEnum.NOT_SUBMITTED.getIndex());
        }
        //现在approval表的api_version_id字段需唯一，要删除api_version对应记录
        apiServiceApprovalDao.deleteByApiVersionId(approvalVo.getApiVersionId());
        apiServiceVersionDao.updateAuthIdByapiVersionId(approvalVo.getApiVersionId(), "");
    }

    //    @Override
//    @Transactional(rollbackFor = Exception.class)
//    @Deprecated
//    public void saveByApp(ApiServiceVo apiService) throws Exception {
//        String user = apiService.getCreator();
//        String resourceId = null;
//        try {
//            // check script path if already created
//            String scriptPath = apiService.getScriptPath();
//
//            // upload to bml
//            Map<String, String> uploadResult = uploadBml(user, scriptPath,
//                    apiService.getMetadata(), apiService.getContent());
//
//            // insert linkis_oneservice_config
//            String version = uploadResult.get("version");
//            resourceId = uploadResult.get("resourceId");
//            apiServiceDao.insert(apiService);
//
//            //insert into version
//            ApiVersionVo apiVersionVo = new ApiVersionVo();
//            apiVersionVo.setApiId(apiService.getId());
//            apiVersionVo.setBmlResourceId(resourceId);
//            apiVersionVo.setBmlVersion(version);
//            apiVersionVo.setVersion(version);
//            apiVersionVo.setCreator(user);
//            apiVersionVo.setCreateTime(Calendar.getInstance().getTime());
//            apiVersionVo.setSource(apiService.getScriptPath());
//            //1为正常 0为禁用
//            apiVersionVo.setStatus(1);
//            //生成审批记录，必须使用发布用户执行sql
//            checkApprovalFromDM(user, apiService, apiVersionVo);
//            //顺序不能改变，版本信息依赖审批单信息
//            apiServiceVersionDao.insert(apiVersionVo);
//
//            //todo 临时参数
//            addApprovalToDB(apiService, apiVersionVo, new ApiSubmitVo());
//
//            // insert linkis_oneservice_params
//            List<ParamVo> params = apiService.getParams();
//            if (params != null && !params.isEmpty()) {
//                for (ParamVo param : params) {
//                    param.setApiVersionId(apiVersionVo.getId());
//                    apiServiceParamDao.insert(param);
//                }
//            }
//            //insert a token record for self
//            genTokenForPublisher(apiService, apiVersionVo.getId());
//        } catch (Exception e) {
//            LOG.error("one service insert error", e);
//            if (StringUtils.isNotBlank(resourceId)) {
////                removeBml(user, resourceId);
//            }
//            if (e.getCause() instanceof ErrorException) {
//                throw (ErrorException) e.getCause();
//            }
//            throw e;
//        }
//    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApiServiceVo apiService) throws Exception {
        try {
            if (null != apiService.getTargetServiceId()) {
                ApiVersionVo maxTargetApiVersionVo = getMaxVersion(apiService.getTargetServiceId());
                if (!checkUserWorkspace(apiService.getModifier(), apiService.getWorkspaceId().intValue())) {
                    throw new ApiServiceQueryException(800035, "Only can update the api service in owner workspace! ");
                }
                if (maxTargetApiVersionVo.getCreator().equals(apiService.getModifier())) {
                    Map<String, String> updateResult = updateBml(apiService.getModifier(), maxTargetApiVersionVo.getBmlResourceId(),
                            apiService.getScriptPath(), apiService.getMetadata(), apiService.getContent());
                    apiService.setCreator(maxTargetApiVersionVo.getCreator());
                    apiService.setId(maxTargetApiVersionVo.getApiId());
                    apiServiceDao.updateToTarget(apiService);
                    LOG.info("Update to other Api Service, ID: " + apiService.getTargetServiceId() + ",resourceId:" + maxTargetApiVersionVo.getBmlResourceId());
                    String version = updateResult.get("version");
                    String resourceId = updateResult.get("resourceId");
                    ApiVersionVo apiServiceVersionVo;
                    // 最新版本状态是未提单时，不再新增版本。
                    if (maxTargetApiVersionVo.getStatus() == ApiVersionStatusEnum.NOT_SUBMITTED.getIndex()) {
                        maxTargetApiVersionVo.setBmlResourceId(resourceId);
                        maxTargetApiVersionVo.setBmlVersion(version);
                        //update时不更新version
//                        maxTargetApiVersionVo.setVersion(version);
                        maxTargetApiVersionVo.setSource(apiService.getScriptPath());
                        maxTargetApiVersionVo.setCreator(apiService.getModifier());
                        maxTargetApiVersionVo.setCreateTime(Calendar.getInstance().getTime());
                        maxTargetApiVersionVo.setExecuteUser(apiService.getExecuteUser());
                        maxTargetApiVersionVo.setDescription(apiService.getDescription());
                        maxTargetApiVersionVo.setComment(apiService.getComment());
                        parseMetadataInfo(apiService.getModifier(), apiService, maxTargetApiVersionVo);
                        apiServiceVersionDao.updateApiVersionInfoById(maxTargetApiVersionVo);
                        apiServiceVersionVo = maxTargetApiVersionVo;
                    } else {
                        apiServiceVersionVo = new ApiVersionVo();
                        apiServiceVersionVo.setApiId(apiService.getId());
                        apiServiceVersionVo.setBmlResourceId(resourceId);
                        apiServiceVersionVo.setBmlVersion(version);
                        //基于上一个版本加1，之前是用的bml_version
                        apiServiceVersionVo.setVersion(ApiUtils.newNormalVersion(maxTargetApiVersionVo.getVersion()));
                        apiServiceVersionVo.setCreator(apiService.getModifier());
                        apiServiceVersionVo.setCreateTime(Calendar.getInstance().getTime());
                        apiServiceVersionVo.setSource(apiService.getScriptPath());
                        apiServiceVersionVo.setExecuteUser(apiService.getExecuteUser());
                        apiServiceVersionVo.setStatus(ApiVersionStatusEnum.NOT_SUBMITTED.getIndex());
                        apiServiceVersionVo.setDescription(apiService.getDescription());
                        apiServiceVersionVo.setComment(apiService.getComment());
                        parseMetadataInfo(apiService.getModifier(), apiService, apiServiceVersionVo);
                        //顺序不能改变，版本信息依赖审批单信息
                        apiServiceVersionDao.insert(apiServiceVersionVo);
                        //为创建者生成token
                        genTokenForPublisher(apiService, apiServiceVersionVo.getId(), ApiServiceConfiguration.API_TOKEN_FOREVER_DURATION.getValue());
                    }
                    List<ParamVo> params = apiService.getParams();
                    //先删除apiversionId关联的param，在插入
                    apiServiceParamDao.deleteByApiVersionId(apiServiceVersionVo.getId());
                    if (params != null && !params.isEmpty()) {
                        for (ParamVo param : params) {
                            param.setApiVersionId(apiServiceVersionVo.getId());
                            apiServiceParamDao.insert(param);
                        }
                    }
//                    addApprovalToDB(apiService, apiServiceVersionVo, );
                } else {
                    throw new ApiServiceQueryException(800036, "Only can update the api service by owner! ");
                }
            } else {
                throw new ApiServiceQueryException(800037, "Target service id  can not be  null for update");
            }

        } catch (Exception e) {
            LOG.error("api service update error", e);
            if (e.getCause() instanceof ErrorException) {
                throw (ErrorException) e.getCause();
            }
            throw e;
        }
    }


    @Override
    public List<ApiServiceVo> query(ApiServiceQuery apiServiceQuery) throws ApiServiceQueryException {
        //todo 查询需要优化，量小时不影响效率，会先捞出所有api
        List<ApiServiceVo> queryList = apiServiceDao.query(apiServiceQuery);

        //查询该用户在所有api下时间最新的token记录，即最新版本对应的token记录
        List<TokenManagerVo> userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUser(apiServiceQuery.getUserName());

        List<ApiServiceVo> authQueryList = queryList.stream().filter(apiServiceVo -> {
            if (apiServiceVo.getCreator().equals(apiServiceQuery.getUserName())) {
                return true;
            }//对于授权用户，已禁用的api直接过滤掉，不返回给前端
            else if (apiServiceVo.getStatus().equals(ApiCommonConstant.API_DISABLE_STATUS)) {
                return false;
            }
            //过滤该用户token对应的apiService
            TokenManagerVo findUserTokenManagerVo = userTokenManagerVos.stream().filter(userTokenManagerVo ->
                    userTokenManagerVo.getApiId().equals(apiServiceVo.getId()) && userTokenManagerVo.getUser().equals(apiServiceQuery.getUserName())
            ).findAny().orElse(null);
            if (null != findUserTokenManagerVo) {
                //过滤token状态为有效的。注：token生成时是有效的，新的版本生成时会禁用老版本的token
                return findUserTokenManagerVo.getStatus().equals(ApiCommonConstant.API_ENABLE_STATUS);
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        //增加代码搜索等特殊API,不走所有用户授权
        queryList.stream().filter(apiServiceVo ->
                ApiUtils.isPublicApiService(apiServiceVo.getId())
        ).forEach(authQueryList::add);

        // query param
        if (!authQueryList.isEmpty()) {
            for (ApiServiceVo apiServiceVo : authQueryList) {
                // 最新的已审批通过的版本，若该api没有提交过审批，则为null
                ApiVersionVo maxApprovedVersion = getMaxApprovedVersion(apiServiceVo.getId());
                //search接口没必要返回params
//                apiServiceVo.setParams(apiServiceParamDao.queryByVersionId(maxApprovedVersion.getId()));
                apiServiceVo.setLatestVersionId(maxApprovedVersion == null ? null : maxApprovedVersion.getId());
                //前端需要展示当前运行中版本
                apiServiceVo.setVersionVos(new ArrayList<ApiVersionVo>() {{
                    add(maxApprovedVersion);
                }});
                //最新版本状态是未提单时，需要展示”有新版本“
                apiServiceVo.setExistNewerVersion(getMaxVersion(apiServiceVo.getId()).getStatus() == ApiVersionStatusEnum.NOT_SUBMITTED.getIndex());
            }
        }
        return authQueryList;
    }

    /**
     * 获取可选择提交审批的api
     *
     * @param username
     * @param workspaceId
     * @return
     * @throws ApiServiceQueryException
     */
    @Override
    public List<ApiServiceVo> queryAvailableSubmitApi(String username, Integer workspaceId) throws ApiServiceQueryException {
        List<ApiServiceVo> apiServiceVoList = apiServiceDao.queryByWorkspaceId(workspaceId, username);
        return apiServiceVoList.stream()
                //过滤被禁用的api
                .filter(l -> l.getStatus().equals(ApiCommonConstant.API_ENABLE_STATUS))
                //保留最新版本为未提单状态的，且
                .filter(apiServiceVo -> {
                    ApiVersionVo maxApiVersionVo = getMaxVersion(apiServiceVo.getId());
                    ApiVersionVo secondMaxVersionVo = getSecondMaxVersion(apiServiceVo.getId());
                    boolean res = maxApiVersionVo.getStatus() == ApiVersionStatusEnum.NOT_SUBMITTED.getIndex()
                            && (secondMaxVersionVo == null || secondMaxVersionVo.getStatus() != ApiVersionStatusEnum.SUBMITTED.getIndex());
                    if (res) {
                        apiServiceVo.setLatestVersionId(maxApiVersionVo.getId());
                        apiServiceVo.setParams(apiServiceParamDao.queryByVersionId(maxApiVersionVo.getId()));
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<ApiServiceVo> queryByWorkspaceId(Integer workspaceId, String userName) {
        List<ApiServiceVo> result = apiServiceDao.queryByWorkspaceId(workspaceId, userName);
        //不需要返回approvalNo
        result.forEach(apiServiceVo -> {
            ApiVersionVo apiVersionVo = getMaxVersion(apiServiceVo.getId());
            apiServiceVo.setExecuteUser(apiVersionVo.getExecuteUser());
            apiServiceVo.setDescription(apiVersionVo.getDescription());
            apiServiceVo.setComment(apiVersionVo.getComment());
        });
        return result;
    }

    /**
     * 页面点击进入使用时调用此方法获取api信息
     *
     * @param id
     * @param userName
     * @return
     * @throws Exception
     */
    @Override
    public ApiServiceVo queryById(Long id, String userName) throws Exception {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        AssertUtil.notNull(apiServiceVo, "未找到数据服务，有可能已经被删除");
        ApiVersionVo maxApiVersionVo;
        if (apiServiceVo.getCreator().equals(userName)) {
            maxApiVersionVo = getMaxVersion(apiServiceVo.getId());
        } else {
            //授权用户必须返回已审批通过的最新版本
            maxApiVersionVo = getMaxApprovedVersion(apiServiceVo.getId());
        }
        List<TokenManagerVo> userTokenManagerVos;
        //为代码搜索等数据服务添加特殊处理token流程，只允许管理员添加。
        if (ApiUtils.isPublicApiService(apiServiceVo.getId())) {
            userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUserAndVersionId(apiServiceVo.getCreator(), maxApiVersionVo.getId());
        } else {
            userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUserAndVersionId(userName, maxApiVersionVo.getId());
        }
        if (userTokenManagerVos.size() > 0) {
            Optional<TokenManagerVo> maxIdToken = userTokenManagerVos.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));
            apiServiceVo.setParams(apiServiceParamDao.queryByVersionId(maxApiVersionVo.getId()));
            apiServiceVo.setUserToken(maxIdToken.get().getToken());
            apiServiceVo.setLatestVersionId(maxApiVersionVo.getId());
        }
        //描述和备注改为从version表中获取
        apiServiceVo.setDescription(maxApiVersionVo.getDescription());
        apiServiceVo.setComment(maxApiVersionVo.getComment());
        return apiServiceVo;
    }

    @Override
    public ApiServiceVo queryByManager(Long id, String userName) throws Exception {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        AssertUtil.notNull(apiServiceVo, "未找到数据服务，有可能已经被删除");
        ApiVersionVo maxApiVersionVo;
        //确认创建者获取的是否应该是最新已提单版本，前端页面需要刷新审批单号
        if (apiServiceVo.getCreator().equals(userName)) {
            maxApiVersionVo = getMaxSubmittedVersion(apiServiceVo.getId());
            if (maxApiVersionVo == null) {
                //若找不到最新提交审批的版本，则返回最新版本，如新建api还未提单时
                maxApiVersionVo = getMaxVersion(apiServiceVo.getId());
            }
        } else {
            //授权用户必须返回已审批通过的最新版本
            maxApiVersionVo = getMaxApprovedVersion(apiServiceVo.getId());
        }
        List<TokenManagerVo> userTokenManagerVos;
        //为代码搜索等数据服务添加特殊处理token流程，只允许管理员添加。
        if (ApiUtils.isPublicApiService(apiServiceVo.getId())) {
            userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUserAndVersionId(apiServiceVo.getCreator(), maxApiVersionVo.getId());
        } else {
            userTokenManagerVos = apiServiceTokenManagerDao.queryByApplyUserAndVersionId(userName, maxApiVersionVo.getId());
        }
        if (userTokenManagerVos.size() > 0) {
            Optional<TokenManagerVo> maxIdToken = userTokenManagerVos.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));
            // query param
            apiServiceVo.setParams(apiServiceParamDao.queryByVersionId(maxApiVersionVo.getId()));
            apiServiceVo.setUserToken(maxIdToken.get().getToken());
            apiServiceVo.setLatestVersionId(maxApiVersionVo.getId());
            apiServiceVo.setApprovalVo(apiServiceApprovalDao.queryByVersionId(maxApiVersionVo.getId()));
        }
//        else {
//            return null;
//        }
        return apiServiceVo;
    }

    @Override
    public List<String> queryAllTags(String userName, Integer workspaceId) {
        //todo 会有历史版本的tag
        List<String> tags = apiServiceDao.queryAllTags(userName, workspaceId);
        List<String> tagList =
                tags.stream().filter(tag -> !StringUtils.isEmpty(tag)).map(tag -> Splitter.on(",").splitToList(tag)).flatMap(List::stream).distinct()
                        .collect(Collectors.toList());
        return tagList;
    }

    @Override
    public ApiServiceVo queryByScriptPath(@Param("scriptPath") String scriptPath) {
        List<ApiServiceVo> apiServiceList = apiServiceDao.queryByScriptPath(scriptPath);
        ApiServiceVo latestApiService = apiServiceList.stream().max(Comparator.comparing(ApiServiceVo::getModifyTime)).orElse(null);
        if (null == latestApiService) {
            return null;
        }
        List<ApiVersionVo> apiVersionVos = apiServiceVersionDao.queryApiVersionByApiServiceId(latestApiService.getId());
        ApiVersionVo maxVersion = apiVersionVos.stream().max(Comparator.comparing(ApiVersionVo::getVersion)).orElse(null);
        // query param
        if (null != maxVersion) {
            latestApiService.setParams(apiServiceParamDao.queryByVersionId(maxVersion.getId()));
            latestApiService.setLatestVersionId(maxVersion.getId());
        }
        return latestApiService;
    }

    @Override
    public Integer queryCountByPath(String scriptPath, String path) {
        return apiServiceDao.queryCountByPath(scriptPath, path);
    }

    @Override
    public Integer queryCountByName(String name) {
        return apiServiceDao.queryCountByName(name);
    }

    /**
     * @param id
     * @param userName
     * @return
     */
    @Override
    public Boolean enableApi(Long id, String userName) {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        if (!checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
            LOG.error("api service check workspace error");
            return false;
        }
        if (apiServiceVo.getCreator().equals(userName)) {
            Integer updateCount = apiServiceDao.enableApi(id);
//            ApiVersionVo maxTargetApiVersionVo = getMaxApprovedVersion(id);
//            apiServiceTokenManagerDao.enableTokenStatusByVersionId(maxTargetApiVersionVo.getId());
            //启用api是不改变version的状态
//            apiServiceVersionDao.updateApiVersionStatusById(maxTargetApiVersionVo.getId(), ApiVersionStatusEnum.ENABLE.getIndex());
            return updateCount > 0;
        } else {
            return false;
        }
    }

    /**
     * @param id       api record id
     * @param userName
     * @return
     */
    @Override
    public Boolean disableApi(Long id, String userName) {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        if (!checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
            LOG.error("api service check workspace error");
            return false;
        }
        if (apiServiceVo.getCreator().equals(userName)) {
            Integer updateCount = apiServiceDao.disableApi(id);
//            apiServiceTokenManagerDao.disableTokenStatusByApiId(id);
            //version表的status禁用含义是：新版本审批通过时，会禁用掉老的版本。
//            apiServiceVersionDao.updateAllApiVersionStatusByApiServiceId(id, ApiVersionStatusEnum.DISABLE.getIndex());
            return updateCount > 0;
        } else {
            return false;
        }
    }


    @Override
    public Boolean deleteApi(Long id, String userName) {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        if (!checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
            LOG.error("api service check workspace error");
            return false;
        }
        if (apiServiceVo.getCreator().equals(userName)) {
            Integer updateCount = apiServiceDao.deleteApi(id);
//            apiServiceTokenManagerDao.disableTokenStatusByApiId(id);
//            apiServiceVersionDao.updateAllApiVersionStatusByApiServiceId(id, 0);
            return updateCount > 0;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateComment(Long id, String comment, String userName) {
        ApiServiceVo apiServiceVo = apiServiceDao.queryById(id);
        if (!checkUserWorkspace(userName, apiServiceVo.getWorkspaceId().intValue())) {
            LOG.error("api service check workspace error");
            return false;
        }
        if (apiServiceVo.getCreator().equals(userName)) {
            Integer updateCount = apiServiceDao.updateApiServiceComment(id, comment);
            return updateCount > 0;
        } else {
            return false;
        }
    }

    private Map<String, String> uploadBml(String userName, String scriptPath, Map<String, Object> metadata, String scriptContent) {
        try {
            ScriptFsWriter writer = StorageScriptFsWriter.getScriptFsWriter(new FsPath(scriptPath), Consts.UTF_8.toString(), null);
            List<Variable> variableList = null;
            if (metadata.entrySet().size() > 0) {
                Variable[] v = VariableParser.getVariables(metadata);
                variableList = Arrays.stream(v).filter(var -> !StringUtils.isEmpty(var.getValue())).collect(Collectors.toList());

            }
            if (variableList != null) {
                writer.addMetaData(new ScriptMetaData(variableList.toArray(new Variable[0])));
            } else {
                writer.addMetaData(null);
            }
            writer.addRecord(new ScriptRecord(scriptContent));
            InputStream inputStream = writer.getInputStream();
            //  新增文件
            BmlUploadResponse resource = client.uploadResource(userName, scriptPath, inputStream);
            if (!resource.isSuccess()) {
                throw new IOException("upload bml error");
            }
            Map<String, String> result = new HashMap<>();
            result.put("resourceId", resource.resourceId());
            result.put("version", resource.version());
            return result;
        } catch (IOException e) {
            LOG.error("upload bml error", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> updateBml(String userName, String resourceId, String scriptPath, Map<String, Object> metadata, String scriptContent) {
        try {
            ScriptFsWriter writer = StorageScriptFsWriter.getScriptFsWriter(new FsPath(scriptPath), Consts.UTF_8.toString(), null);
            Variable[] v = VariableParser.getVariables(metadata);
            List<Variable> variableList = Arrays.stream(v).filter(var -> !StringUtils.isEmpty(var.getValue())).collect(Collectors.toList());
            writer.addMetaData(new ScriptMetaData(variableList.toArray(new Variable[0])));
            writer.addRecord(new ScriptRecord(scriptContent));
            InputStream inputStream = writer.getInputStream();

            //  更新文件
            BmlUpdateResponse resource = client.updateResource(userName, resourceId, "", inputStream);
            if (!resource.isSuccess()) {
                throw new IOException("update bml error");
            }
            Map<String, String> result = new HashMap<>();
            result.put("resourceId", resource.resourceId());
            result.put("version", resource.version());
            return result;
        } catch (IOException e) {
            LOG.error("update bml error", e);
            throw new RuntimeException(e);
        }
    }

    private void removeBml(String userName, String resourceId) {
        try {
            LOG.info("delete bml resource: userName: " + userName + ", resourceId: " + resourceId);
            client.deleteResource(userName, resourceId);
        } catch (Exception e) {
            LOG.error("remove bml error", e);
        }
    }

    /**
     * 提交一个linkis的scala任务，解析出脚本中的库表信息
     *
     * @param user
     * @param apiService
     * @param apiVersionVo
     * @throws Exception
     */
    void parseMetadataInfo(String user, ApiServiceVo apiService, ApiVersionVo apiVersionVo) throws Exception {
        //提单时生成uuid后设置
        apiVersionVo.setAuthId("");
        //发布时不允许使用代理用户执行
        Map<String, Object> metaDataInfo = ExecuteCodeHelper.getMetaDataInfoByExecute(user, apiService.getContent(), apiService.getMetadata(), apiService.getScriptPath());
        String metaDataInfoStr = metaDataInfo.get("0").toString();
        if (metaDataInfoStr.equals("[]")) {
            throw new ApiExecuteException(80056, "数据服务脚本没有库表信息!");
        }
        apiVersionVo.setMetadataInfo(metaDataInfoStr);
    }

    /**
     * 请求dm接口提单
     *
     * @param approvalNo
     * @param apiSubmitVo
     * @param apiVersionVoList
     * @return
     * @throws Exception
     */
    public SQLMetadataInfoCheckStatus sendApprovalToDM(String approvalNo, ApiSubmitVo apiSubmitVo, List<ApiVersionVo> apiVersionVoList) throws Exception {
        //封装dm表单信息
        List<DataMapApplyContentData> allDMApplyContentDatas = new ArrayList<>();
        List<String> wrongDbTables = new ArrayList<>();
        for (ApiVersionVo apiVersionVo : apiVersionVoList) {
            allDMApplyContentDatas.addAll(genDataMapApplyContentDatas(apiSubmitVo, apiVersionVo, apiVersionVo.getMetadataInfo(), wrongDbTables));
        }
        //批量提单时，提示所有数据服务的有问题的库表，方便用户排查
        if (!wrongDbTables.isEmpty()) {
            throw new ApiExecuteException(800044, "存在如下库表无法获取开发负责人信息！" + wrongDbTables);
        }
//        String applyDesc = apiService.getDescription() + "\nApplySQL:\n" + apiService.getContent();
        String applyDesc = apiSubmitVo.getBackgroundDesc();

        if (!DataMapUtils.applyDataMap(approvalNo, apiSubmitVo.getCreator(), apiSubmitVo.getApprovalName(), applyDesc,
                apiSubmitVo.getAttentionUser(), allDMApplyContentDatas)) {
            throw new Exception("审批单申请失败");
        } else {
            LOG.info("审批单申请成功！：{}", apiSubmitVo.getApprovalName());
        }
        return SQLMetadataInfoCheckStatus.LEGAL;
    }

    /**
     * 被saveByApp方法调用
     */
//    public SQLMetadataInfoCheckStatus checkApprovalFromDM(String user, ApiServiceVo apiService, ApiVersionVo apiVersionVo) throws Exception {
//        //             需要把发布者的SQL脚本内容，解析出对应的metadata库表信息
//
//        ApprovalVo approvalVo = apiService.getApprovalVo();
////        String approvalNo = UUIDGenerator.genUUID();
//        apiVersionVo.setAuthId(approvalVo.getApprovalNo());
//
//        //发布时不允许使用代理用户执行
//        Map<String, Object> metaDataInfo = ExecuteCodeHelper.getMetaDataInfoByExecute(user, apiService.getContent(), apiService.getMetadata(), apiService.getScriptPath());
//        String metaDataInfoStr = metaDataInfo.get("0").toString();
//
//        apiVersionVo.setMetadataInfo(metaDataInfoStr);
//
//        List<DataMapApplyContentData> dataMapApplyContentDatas = genDataMapApplyContentDatas(apiService, apiVersionVo, metaDataInfoStr);
//
//        // 如果metadatainfo的列表为空，及无库表信息
//        if (dataMapApplyContentDatas.size() == 0) {
//            throw new ApiExecuteException(80056, "新建的数据服务脚本没有库表信息!");
//        }
//
//        int dataMapStatus = DataMapUtils.requestDataMapStatus(approvalVo.getApprovalNo()).getIndex();
//        if (dataMapStatus != DataMapStatus.INITED.getIndex()
//                && dataMapStatus != DataMapStatus.APPROVING.getIndex()
//                && dataMapStatus != DataMapStatus.SUCCESS.getIndex()
//        ) {
//            throw new Exception("审批单查询失败:" + dataMapStatus + ",审批单号：" + approvalVo.getApprovalNo());
//        } else {
//            LOG.info("审批单申请成功！：{}", approvalVo.getApprovalNo(), approvalVo.getStatus());
//        }
//        return SQLMetadataInfoCheckStatus.LEGAL;
//    }
    @Override
    public List<DataMapApplyContentData> genDataMapApplyContentDatas(ApiSubmitVo apiSubmitVo, ApiVersionVo apiVersionVo, String metaDataInfo, List<String> wrongDbTables) throws Exception {
        List<DataMapApplyContentData> dataMapApplyContentDatas = new ArrayList<>();
        String user = apiVersionVo.getCreator();

        //metaDataInfo为该数据服务api所有的库表信息,如：[a.b,c.d]
        String rawMetaDataInfo = metaDataInfo.replaceAll("\\[", "").replaceAll("\\]", "");
        LOG.info("查询的库表信息为：{}", rawMetaDataInfo);
        if (!StringUtils.isBlank(rawMetaDataInfo)) {
            String[] dbTableNames = rawMetaDataInfo.split(",");
//            List<String> wrongDbTables = new ArrayList<>();
            for (String item : dbTableNames) {
                LOG.info("查询的库表信息：{}", item);
                //删除空格
                item = item.trim();
                String[] dbTable = item.split("\\.");
                LOG.info("查询的库表信息：{}", dbTable.length);
                String dbName = dbTable[0];
                String tableName = dbTable[1];
                // 构建提单的对象
                DataMapApplyContentData dataMapApplyContentData = new DataMapApplyContentData();
                // 没有配置的项可以直接在配置中获取，或者使用默认值
                dataMapApplyContentData.setApiName(apiSubmitVo.getApprovalName());
                dataMapApplyContentData.setApiCreator(user);
                //背景描述
                dataMapApplyContentData.setApiDescription(apiSubmitVo.getBackgroundDesc());
                dataMapApplyContentData.setUser(apiSubmitVo.getApplyUser());
                //业务重要度
                dataMapApplyContentData.setImportance(String.valueOf(apiSubmitVo.getImportance()));
                //生效时间
                dataMapApplyContentData.setTime(String.valueOf(apiSubmitVo.getDuration()));
                //代理用户
                if (StringUtils.isNotBlank(apiVersionVo.getExecuteUser())) {
                    dataMapApplyContentData.setProxyUser(apiVersionVo.getExecuteUser());
                } else {
                    //如果没有设置，那么就是creator作为代理用户
                    dataMapApplyContentData.setProxyUser(apiVersionVo.getCreator());
                }
                dataMapApplyContentData.setBeganTime(new Date(System.currentTimeMillis()));
                dataMapApplyContentData.setDbName(dbName);
                dataMapApplyContentData.setTableName(tableName);
                //调用dm接口查询库表的开发负责人
                Map<String, String> devPrinsAndStatus = new HashMap<>();
                try {
                    //todo
                    devPrinsAndStatus = DataMapUtils.getDevPrincipalsAndStatus(dbName, tableName, DataMapUtils.DATAMAP_DEVPRINS_ENV, DataMapUtils.DATAMAP_DEVPRINS_CLUSTERTYPE);
                    if (devPrinsAndStatus.isEmpty() || devPrinsAndStatus.get("devPrincipals") == null) {
                        ApiServiceVo apiServiceVo = apiServiceDao.queryById(apiVersionVo.getApiId());
                        wrongDbTables.add(apiServiceVo.getName() + "[" + dbName + "." + tableName + "]");
                    }
                } catch (Exception e) {
                    LOG.error("failed to getDevPrincipals: ", e);
                }
                dataMapApplyContentData.setDevPrincipals(devPrinsAndStatus.get("devPrincipals"));
                dataMapApplyContentData.setStatus(devPrinsAndStatus.get("status"));

                dataMapApplyContentDatas.add(dataMapApplyContentData);
            }
            //提示有问题的库表
//            if (!wrongDbTables.isEmpty()) {
//                throw new ApiExecuteException(800044, "存在如下库表无法获取开发负责人信息！" + wrongDbTables);
//            }
        } else {
            LOG.warn("数据服务脚本中，使用的库表信息为空！");
        }
        return dataMapApplyContentDatas;
    }


    public void addApprovalToDB(ApiServiceVo apiService, ApiVersionVo apiVersionVo, ApiSubmitVo apiSubmitVo) {
        ApprovalVo approvalVo = new ApprovalVo();
        BeanUtils.copyProperties(apiSubmitVo, approvalVo);
        // 需要插入审批单记录
        approvalVo.setApiId(apiService.getId());
        approvalVo.setApiVersionId(apiVersionVo.getId());
        approvalVo.setCreateTime(new Date());
        approvalVo.setUpdateTime(new Date());
        approvalVo.setDuration(Long.parseLong(apiSubmitVo.getDuration()));
        //单子初始状态
        approvalVo.setStatus(DataMapStatus.INITED.getIndex());
        approvalVo.setApprovalNo(apiVersionVo.getAuthId());
        //代理用户移到api_version表
//        if (StringUtils.isEmpty(approvalVo.getExecuteUser())) {
//            approvalVo.setExecuteUser(approvalVo.getCreator());
//        }
        apiServiceApprovalDao.insert(approvalVo);

    }

    public void genTokenForPublisher(ApiServiceVo apiService, Long apiVersionId, Long duration) throws ApiServiceTokenException {
        List<TokenManagerVo> tokenManagerVoList = new ArrayList<>();

        TokenManagerVo tokenManagerVo = new TokenManagerVo();
        tokenManagerVo.setApiId(apiService.getId());
        tokenManagerVo.setApiVersionId(apiVersionId);
        tokenManagerVo.setApplyTime(Calendar.getInstance().getTime());
//        tokenManagerVo.setDuration(1000L);
        tokenManagerVo.setDuration(duration);
        tokenManagerVo.setReason("create api service");
        tokenManagerVo.setStatus(1);
        tokenManagerVo.setIpWhitelist("");
        tokenManagerVo.setCaller("scriptis");
        tokenManagerVo.setUser(apiService.getCreator());
        tokenManagerVo.setPublisher(apiService.getCreator());

        ApiServiceToken apiServiceToken = new ApiServiceToken();
        apiServiceToken.setApplyUser(apiService.getCreator());
        apiServiceToken.setPublisher(apiService.getCreator());
        apiServiceToken.setApplyTime(tokenManagerVo.getApplyTime());
        apiServiceToken.setApiServiceId(tokenManagerVo.getApiId());

        tokenManagerVo.setToken(JwtManager.createToken(apiService.getCreator(), apiServiceToken, tokenManagerVo.getDuration()));
        //审批单号使用默认的0001，自己给自己授权
        tokenManagerVo.setApplySource(ApiCommonConstant.DEFAULT_APPROVAL_NO);
        tokenManagerVoList.add(tokenManagerVo);
        tokenAuth.saveTokensToDb(tokenManagerVoList, ApiCommonConstant.DEFAULT_APPROVAL_NO);
    }


    /**
     * 获取最新版本
     *
     * @param serviceId
     * @return
     */
    @Override
    public ApiVersionVo getMaxVersion(long serviceId) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(serviceId);
        // 之前是通过bml_version比较版本大小，改为通过自增id比较
        return apiVersionVoList.stream().max(Comparator.comparing(ApiVersionVo::getId)).orElse(null);
    }

    @Override
    public ApiVersionVo getSecondMaxVersion(long serviceId) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(serviceId);
        if (apiVersionVoList.size() <= 1) {
            return null;
        }
        apiVersionVoList.sort(Comparator.comparing(ApiVersionVo::getId));
        LOG.info("the secondary version_id is :{}", apiVersionVoList.get(apiVersionVoList.size() - 2).getId());
        return apiVersionVoList.get(apiVersionVoList.size() - 2);
    }

    /**
     * 获取最新的审批通过版本
     *
     * @param serviceId
     * @return
     */
    public ApiVersionVo getMaxApprovedVersion(long serviceId) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(serviceId);
        return apiVersionVoList.stream().filter(l -> l.getStatus() == ApiVersionStatusEnum.ENABLE.getIndex())
                .max(Comparator.comparing(ApiVersionVo::getId)).orElse(null);
    }

    /**
     * 获取最新的已提单版本，和运行中版本
     *
     * @param serviceId
     * @return
     */
    public ApiVersionVo getMaxSubmittedVersion(long serviceId) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(serviceId);
        return apiVersionVoList.stream().filter(l -> l.getStatus() == ApiVersionStatusEnum.ENABLE.getIndex()
                || l.getStatus() == ApiVersionStatusEnum.SUBMITTED.getIndex())
                .max(Comparator.comparing(ApiVersionVo::getId)).orElse(null);
    }

    @Override
    public boolean checkUserWorkspace(String userName, Integer workspaceId) {
        //todo cache user workspaceIds
        String workspaceIds = ExecuteCodeHelper.getUserWorkspaceIds(userName, ujesClient);
        LOG.info("检查数据服务使用的工作空间信息为：" + workspaceId + ", workspaceIds:" + workspaceIds);
        if (Arrays.stream(workspaceIds.split(",")).map(Integer::valueOf).collect(Collectors.toList()).contains(workspaceId)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void updateApiServiceBML(String serverUrl) {
        Map<String, Object> properties = new HashMap<>();
        BmlClient oldBmlClient = BmlClientFactory.createBmlClient(serverUrl, properties);
        List<ApiVersionVo> apiVersionVos = apiServiceVersionDao.queryApiVersion();
        for (ApiVersionVo apiVersionVo : apiVersionVos) {
            try {
                BmlDownloadResponse resource = oldBmlClient.downloadResource(apiVersionVo.getCreator(), apiVersionVo.getBmlResourceId(), apiVersionVo.getBmlVersion());
                if (resource.inputStream() == null) continue;
                BmlUploadResponse response = client.uploadResource(apiVersionVo.getCreator(), apiVersionVo.getSource(), resource.inputStream());
                AssertUtil.isTrue(response.isSuccess(), "上传bml错误");
                String resourceId = response.resourceId();
                String version = response.version();
                apiVersionVo.setBmlResourceId(resourceId);
                apiVersionVo.setBmlVersion(version);
                apiVersionVo.setVersion(version);
                apiServiceVersionDao.updateBmlResourceIdAndBmlVersionById(apiVersionVo);
            } catch (Exception e) {
                LOG.error("move bml error", e);
            }
        }
    }

    @Override
    public List<Map<String, Object>> checkApiServiceBML(String serverUrl) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        BmlClient oldBmlClient = BmlClientFactory.createBmlClient(serverUrl, properties);
        List<ApiVersionVo> apiVersionVos = apiServiceVersionDao.queryApiVersion();
        for (ApiVersionVo apiVersionVo : apiVersionVos) {
            Map<String, Object> resultMap = new HashMap<>();
            try {
                BmlDownloadResponse oldResource = oldBmlClient.downloadResource(apiVersionVo.getCreator(), apiVersionVo.getBmlResourceId(), apiVersionVo.getBmlVersion());
                if (oldResource.inputStream() == null) continue;
                BmlDownloadResponse resource = client.downloadResource(apiVersionVo.getCreator(), apiVersionVo.getBmlResourceId(), apiVersionVo.getBmlVersion());
                if (resource.inputStream() == null) continue;
                String oldReadStream = readStream(oldResource.inputStream());
                String readStream = readStream(resource.inputStream());
                if (!oldReadStream.equals(readStream)) {
                    Map<String, Object> resultBML = new HashMap<>();
                    resultBML.put("oldBML", oldReadStream);
                    resultBML.put("BML", readStream);
                    resultMap.put(apiVersionVo.getBmlResourceId(), resultBML);
                    resultList.add(resultMap);
                    LOG.info("检测到内容不匹配" + "bmlResourceId为" + apiVersionVo.getBmlResourceId(), oldReadStream, readStream);
                }
            } catch (Exception e) {
                LOG.error("check bml error", e);
            }
        }
        return resultList;
    }

    /**
     * 读取 InputStream 到 String字符串中
     */
    public static String readStream(InputStream in) {
        try {
            //<1>创建字节数组输出流，用来输出读取到的内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //<2>创建缓存大小
            byte[] buffer = new byte[1024]; // 1KB
            //每次读取到内容的长度
            int len = -1;
            //<3>开始读取输入流中的内容
            while ((len = in.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
                baos.write(buffer, 0, len);   //把读取到的内容写到输出流中
            }
            //<4> 把字节数组转换为字符串
            String content = baos.toString();
            //<5>关闭输入流和输出流
            in.close();
            baos.close();
            //<6>返回字符串结果
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
