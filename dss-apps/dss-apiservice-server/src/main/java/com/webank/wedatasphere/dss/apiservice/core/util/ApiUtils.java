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

package com.webank.wedatasphere.dss.apiservice.core.util;

import com.webank.wedatasphere.dss.apiservice.core.exception.*;
import com.webank.wedatasphere.dss.apiservice.core.service.ApiService;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiServiceVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.MessageVo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.server.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration.PUBLIC_API_LIST;

/**
 *
 */
public class ApiUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ApiUtils.class);

    public static final Pattern WRITABLE_PATTERN = Pattern.compile("^\\s*(insert|update|delete|drop|alter|create).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    public static Message buildMessageAndResponse(Integer errorCode,String msg){
        LOG.error("ErrorCode:"+errorCode+", Msg:"+msg);
        Message message = Message.error(msg);
        return message;
    }


    public static Message doAndResponse(TryOperation tryOperation, String method, String failMessage) {
        try {
            Message message = tryOperation.operateAndGetMessage();
            return setMethod(message, method);
        } catch (ConstraintViolationException e) {
            LOG.error("api error, method: " + method, e);
            return new BeanValidationExceptionMapper().toResponse(e);
        } catch (WarnException e) {
            LOG.error("api error, method: " + method, e);
            return setMethod(Message.warn(e.getMessage()), method);
        } catch (AssertException e) {
            LOG.error("api error, method: " + method, e);
            return setMethod(Message.error(e.getMessage()), method);

        } catch (ApiExecuteException e) {
        LOG.error("api error, method: " + method, e);
        return setMethod(Message.error(e.getMessage()), method);
        }
        catch (ApiServiceQueryException e) {
            LOG.error("api error, method: " + method, e);
            return setMethod(Message.error(e.getMessage()), method);
        }
        catch (ApiServiceTokenException e){
            LOG.error("api error, method: " + method, e);
            return setMethod(Message.error(e.getMessage()), method);
        }
        catch (Exception e) {
            LOG.error("api error, method: " + method, e);
            return setMethod(Message.error(failMessage, e), method);
        }
    }

    /**
     * @param tryOperation operate function
     */
    public static Message doAndResponse(Operation tryOperation) {
        Message msg = null;
        try {
              msg = tryOperation.operateAndGetMessage();
            return msg;
        } catch (ConstraintViolationException e) {
            LOG.error("api error ", e);
            return new BeanValidationExceptionMapper().toResponse(e);
        } catch (WarnException e) {
            LOG.error("api error ", e);
            return Message.error("系统异常");
        } catch (AssertException e) {
            LOG.error("api error ", e);
            return Message.error(e.getMessage());
        }catch (ApiServiceRuntimeException e){
            LOG.error("api error ", e);
            return Message.error(e.getMessage());
        }
        catch (Exception e) {
            LOG.error("api error ", e);
            return Message.error(String.valueOf(e.getCause()));
        }
    }

    public static Object setMsg(String message) {
        MessageVo messageVo = new MessageVo();
        messageVo.setMessage(message);

        return messageVo;
    }

    private static Message setMethod(Message message, String method) {
        message.setMethod(method);
        return message;
    }

    @FunctionalInterface
    public interface TryOperation {

        /**
         * Operate method
         */
        Message operateAndGetMessage() throws Exception;
    }

    @FunctionalInterface
    public interface Operation {

        /**
         * Operate method
         */
        Message operateAndGetMessage() throws Exception;
    }

    public static  void checkApiCreateParams(ApiService apiService, ApiServiceVo apiServiceVo, ApprovalVo approvalVo, Validator beanValidator) throws ApiServiceParamsCheckException {

        int existCount= apiService.queryCountByName(apiServiceVo.getName());
        if(existCount>0){
            throw new ApiServiceParamsCheckException(100013,"已经存在该名称的数据服务，请修改名称后重新提交");
        }

        if (StringUtils.isBlank(apiServiceVo.getAliasName())) {
            throw new  ApiServiceParamsCheckException(10001,"创建数据服务缺少中文名");
        }

        if (StringUtils.isBlank(apiServiceVo.getScriptPath())) {
            throw new  ApiServiceParamsCheckException(10002,"创建数据服务缺少脚本路径");
        }
        if (StringUtils.isBlank(apiServiceVo.getContent())) {
            throw new  ApiServiceParamsCheckException(10003,"创建数据服务缺少脚本内容");
        }

        if (null == apiServiceVo.getWorkspaceId() || apiServiceVo.getWorkspaceId() < 0) {
            throw new  ApiServiceParamsCheckException(10004,"创建数据服务缺少工作空间ID "+apiServiceVo.getWorkspaceId());
        }

        if(!apiService.checkUserWorkspace(apiServiceVo.getModifier(),apiServiceVo.getWorkspaceId().intValue())){
            throw new ApiServiceParamsCheckException(100014,"创建数据服务工作空间检查失败，请确认工作空间ID是否正确! "+apiServiceVo.getWorkspaceId().intValue());
        }


        if (apiServiceVo.getContent().contains(";") && !apiServiceVo.getContent().toLowerCase().startsWith("use ")) {
            throw new  ApiServiceParamsCheckException(10005,"创建数据服务脚本内容包含分号，不允许执行多条SQL");
        }

//                     check data change script
        if (WRITABLE_PATTERN.matcher(apiServiceVo.getContent()).matches()) {
            throw new  ApiServiceParamsCheckException(10006,"创建数据服务脚本内容只支持SELECT查询语句");
        }

        Map<String, Object> metadata = apiServiceVo.getMetadata();
        if (apiServiceVo.getScriptPath().endsWith(".jdbc")) {
            if (MapUtils.isEmpty(metadata)) {
                throw new  ApiServiceParamsCheckException(10007,"创建数据服务没有数据源元数据信息");
            }

            Map<String, Object> configuration = (Map<String, Object>) metadata.get("configuration");
            if (MapUtils.isEmpty(configuration)) {
                throw new  ApiServiceParamsCheckException(10008,"创建数据服务配置信息为空");

            }

            Map<String, Object> datasource = (Map<String, Object>) configuration.get("datasource");
            if (MapUtils.isEmpty(datasource)) {
                throw new  ApiServiceParamsCheckException(10009,"创建数据服务配置信息中没有数据源");
            }
        }


        Set<ConstraintViolation<ApiServiceVo>> result = beanValidator.validate(apiServiceVo, Default.class);
        if (result.size() > 0) {
            throw new ConstraintViolationException(result);
        }


        if (StringUtils.isBlank(approvalVo.getApprovalName())) {
            throw new  ApiServiceParamsCheckException(10010,"创建数据服务缺少审批单名字");
        }

        if (StringUtils.isBlank(approvalVo.getApplyUser())) {
           throw new  ApiServiceParamsCheckException(10011,"创建数据服务缺少申请用户");

        }

    }

    public static boolean isPublicApiService(Long id){
      return   Arrays.asList(PUBLIC_API_LIST.getValue().split(",")).contains(id.toString());
    }

    public static String newNormalVersion(String oldVersion) {
        long num = Long.parseLong(oldVersion.substring(1)) + 1;
        String tmp = "00000" + num;
        return "v" + tmp.substring(tmp.length() - 6);
    }


}
