package com.webank.wedatasphere.dss.framework.compute.resource.manager.client;

import com.webank.wedatasphere.dss.framework.compute.resource.manager.conf.LinkisConnConf;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.ConfigurationTemplate;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ApplyECConfTemplateRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceKillRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.UpdateKeyMappingRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ApplyECConfTemplateResponse;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.CategoryLabel;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ECInstance;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.GovernanceStationAdminResponse;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.vo.QueueInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 资源管理客户端
 * Author: xlinliu
 * Date: 2022/11/25
 */
public interface ResourceManageClient {
    String FETCH_ECINSTANCE_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/linkisManager/ecinfo/ecList";
    String KILL_ECINSTANCE_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/linkisManager/rm/enginekillAsyn";
    String TEST_USER_INITIATED_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/filesystem/getUserRootPath";

    String QUERY_QUEUE_INFO=LinkisConnConf.LINKIS_URL+"/api/rest_j/v1/linkisManager/rm/queueresources";
    String CONF_ALL_ENGINE_TYPE = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/engineType";
    String CONF_CATEGORY_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/getCategory";
    String CONF_TEMPLATE_ADD_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/template/updateKeyMapping";
    String CONF_TEMPLATE_QUERY_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/template/queryKeyInfoList";
    String CONF_UI_QUERY_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/getItemList";
    String CONF_APPLY_TEMPLATE_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/configuration/template/apply";
    String CONF_GOVERNANCE_ADMIN_PATH = LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/jobhistory/governanceStationAdmin";

    Set<String> ENGINE_TYPE_IN_YARN_QUEUE=new HashSet<>(Arrays.asList("hive","spark","flink"));

      /**
       * 获取引擎实例列表
       * @param ecInstanceRequest 请求参数
       * @param operateUser 操作人
       * @return
       */
    List<ECInstance> fetchECInstance(ECInstanceRequest ecInstanceRequest,String operateUser);

      /**
       * 批量kill引擎
       * @param killRequest 请求参数
       * @param operateUser 操作人
       */
    void batchKillECInstance(List<ECInstanceKillRequest> killRequest,String operateUser);

    /**
     * 判断用户是否初始化完成
     * @param userName 用户名
     * @return 是否初始化完成
     */
    boolean testUserInitiated(String userName);

    /**
     * 获取队列信息
     * @param queueName 队列名
     * @param operateUser 操作人
     */
    QueueInfo getQueueInfo(String queueName,String operateUser);

    /**
     * 获取所有引擎类型
     * @param operateUser 操作人
     */
    List<String> getEngineTypes(String operateUser);
    /**
     * 获取应用、引擎类型树
     * @param operateUser 操作人
     */
    List<CategoryLabel> getApplicationEngineTypeCategory(String operateUser);

    /**
     * 保存配置模板
     * @param template 配置模板
     */
    void saveECConfTemplate(UpdateKeyMappingRequest template);

    /**
     * 查询模板的参数值
     * @param templateId
     * @return
     */
    ConfigurationTemplate getECConfTemplate(String templateId,String operator);

    List<ConfigurationTemplate.Item> getTemplateParamConf(String engineType, String operateUser);

    /**
     * 执行模板应用规则，将模板下发到用户
     * @param request
     */
    ApplyECConfTemplateResponse applyECConfTemplate(ApplyECConfTemplateRequest request);

    /**
     * 检查当前用户是否为超级管理员
     * @param userName
     */
    GovernanceStationAdminResponse governanceStationAdmin(String userName);

    /**
     * 检查当前用户是否为超级管理员
     * @param userName
     */
    boolean checkSuperAdmin(String userName);
}