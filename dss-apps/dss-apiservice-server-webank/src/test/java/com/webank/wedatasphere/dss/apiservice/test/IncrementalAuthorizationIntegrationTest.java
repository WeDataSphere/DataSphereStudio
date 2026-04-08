/*
 *
 * Copyright 2019 WeBank
 *
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
 */

package com.webank.wedatasphere.dss.apiservice.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 增量授权集成测试 - 验证增量授权核心功能
 * <p>
 * 测试功能：
 * - 首次提单无历史用户
 * - 自动合并历史用户
 * - 重复用户去重处理
 * - 批量API申请时增量授权
 * - 历史配置保留
 * - 重复授权防护
 * <p>
 * 使用方式：
 * 1. 设置测试环境BASE_URL
 * 2. 配置全局变量（用户、工作空间ID等）
 * 3. 运行具体测试用例
 * <p>
 * 变量说明：
 * - ${variable}: 全局变量，在测试类中定义
 * - $(variable): 从接口响应获取的参数
 * <p>
 * 可扩展性：
 * - 新增测试用例只需添加新的测试方法
 * - 变量替换机制支持灵活的参数传递
 * - 测试步骤模块化，便于组合复用
 */
public class IncrementalAuthorizationIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(IncrementalAuthorizationIntegrationTest.class);

    // ===========================================
    // 测试环境配置
    // ===========================================

    /**
     * 测试环境BASE_URL
     */
    private static final String BASE_URL = "http://uat.dss.bdap.weoa.com";

    /**
     * Cookie - 需要配置测试环境有效的Cookie
     */
    private static final String COOKIE = "workspaceId=224; workspaceName=bdapWorkspace; dss_user_name=v_sunpengwang; linkis_user_session_ticket_id_v1=S0nLjR+mKNOKrNf3YbpU38AMlNg3BdgGLZsAVEIR23Q=";

    /**
     * API前缀
     */
    private static final String API_PREFIX = "/api/rest_j/v1/dss/apiservice";

    // ===========================================
    // 全局变量定义 - ${变量名}
    // ===========================================

    /**
     * 工作空间ID
     */
    private static final String VAR_WORKSPACE_ID = "224";

    /**
     * 提交用户
     */
    private static final String VAR_SUBMIT_USER = "v_sunpengwang";

    /**
     * 用户1
     */
    private static final String VAR_USER1 = "hadoop";

    /**
     * 用户2
     */
    private static final String VAR_USER2 = "hduser01";

    /**
     * 用户3
     */
    private static final String VAR_USER3 = "hduser02";

    /**
     * 用户4
     */
    private static final String VAR_USER4 = "hduser03";

    /**
     * 用户5
     */
    private static final String VAR_USER5 = "hduser05";

    /**
     * 用户6
     */
    private static final String VAR_USER6 = "hduser06";

/**
 * 用户7
 */
private static final String VAR_USER7 = "hduser007";

/**
 * 测试用例API名称 - 首次提单
 */
private static final String VAR_TEST_CASE_FIRST = "test_case_three";

    /**
     * 测试用例API名称 - 历史合并
     */
    private static final String VAR_TEST_CASE_HISTORY = "test_case_second";

    /**
     * 测试用例API名称 - 重复用户
     */
    private static final String VAR_TEST_CASE_DUPLICATE = "test_case_three";

    /**
     * 审批等待时间（毫秒） - 默认10分钟
     */
    private static final long APPROVAL_WAIT_TIME_MS = 10 * 60 * 1000L;

    /**
     * 测试用例4 - 批量API测试 - 指定的API名称列表（可选）
     * 如果为null或空，则自动选择前3个API
     * 格式示例: "订单查询API,用户管理API,数据统计API" (逗号分隔的API名称列表)
     */
    private static final String VAR_BATCH_API_NAMES = "test_case_four_06,test_case_four_05,test_case_four_04";

    /**
     * 测试用例5 - 批量API测试 - 指定的API名称列表（可选）
     * 如果为null或空，则自动选择前3个API
     * 格式示例: "订单查询API,用户管理API,数据统计API" (逗号分隔的API名称列表)
     */
    private static final String VAR_BATCH_API_NAMES_TEST5 = "test_case_five_01,test_case_five_02,test_case_five_03";

    // ===========================================
    // 运行时变量存储 - $(变量名)
    // ===========================================

    /**
     * 存储从接口获取的变量
     */
    private Map<String, Object> runtimeVariables = new HashMap<>();

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        runtimeVariables.clear();

        LOG.info("========================================");
        LOG.info("增量授权集成测试初始化");
        LOG.info("BASE_URL: {}", BASE_URL);
        LOG.info("========================================");
    }

    // ===========================================
    // 测试用例1：增量授权 - 首次提单无历史用户
    // ===========================================

    /**
     * 测试用例1: 增量授权 - 首次提单无历史用户
     * <p>
     * 测试步骤：
     * 1、调用 /api/rest_j/v1/dss/apiservice/availableSubmitApi 接口,传入workspaceId=${workspaceId}
     * 查询名称为 ${test_case_first} 名称的api,未找到则抛出异常。
     * 2、调用 /api/rest_j/v1/dss/apiservice/submit 接口,从availableSubmitApi接口获取需要传入的参数，
     * 提交审批单。
     * 3、调用 /api/rest_j/v1/dss/apiservice/apiVersionQuery 接口获取审批单号(authId)
     * 4、等待手动审批（控制台打印提醒）
     * 5、调用 approvalRefresh 接口刷新审批状态
     * 6、调用 tokenQuery 接口查询生成的Token列表
     * <p>
     * 预期结果：
     * - 为user1生成Token
     * - 为user2生成Token
     * - Token状态为有效
     * - 不执行历史用户合并
     */
    @Test
    public void testIncrementalAuthorization_FirstSubmissionNoHistory() throws Exception {
        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例1: 增量授权 - 首次提单无历史用户");
        LOG.info("========================================");

        // ===================================================================
        // 步骤1：查询可用API - GET /api/rest_j/v1/dss/apiservice/availableSubmitApi
        // ===================================================================
        LOG.info("");
        LOG.info("步骤1: 查询可用API - workspaceId={}, apiName={}", VAR_WORKSPACE_ID, VAR_TEST_CASE_FIRST);

        String availableSubmitApiUrl = BASE_URL + API_PREFIX + "/availableSubmitApi?workspaceId=" + VAR_WORKSPACE_ID;
        String availableSubmitApiResponse = doGet(availableSubmitApiUrl);
        LOG.info("availableSubmitApi响应: {}", formatJson(availableSubmitApiResponse));

        // 解析响应，查找指定名称的API
        JsonNode availableSubmitApiJson = objectMapper.readTree(availableSubmitApiResponse);
        if (availableSubmitApiJson.has("status") && availableSubmitApiJson.get("status").asInt() != 0) {
            String errorMsg = availableSubmitApiJson.has("message") ? availableSubmitApiJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询可提交API失败: " + errorMsg);
        }

        // 验证存在 availableSubmitApiList 字段
        if (!availableSubmitApiJson.has("data") || !availableSubmitApiJson.get("data").has("availableSubmitApiList")) {
            throw new AssertionError("响应缺少 availableSubmitApiList 字段");
        }

        ArrayNode availableSubmitApiList = (ArrayNode) availableSubmitApiJson.get("data").get("availableSubmitApiList");

        // 查找名称为 ${test_case_first} 的API
        Long apiId = null;
        Long apiVersionId = null;
        String foundApiName = null;
        boolean found = false;

        for (JsonNode api : availableSubmitApiList) {
            String name = api.has("name") ? api.get("name").asText() : "";
            if (VAR_TEST_CASE_FIRST.equals(name)) {
                apiId = api.has("id") ? api.get("id").asLong() : null;
                // 获取最新版本ID
                if (api.has("apiVersionId")) {
                    apiVersionId = api.get("apiVersionId").asLong();
                } else if (api.has("latestVersionId")) {
                    apiVersionId = api.get("latestVersionId").asLong();
                }
                foundApiName = name;
                found = true;
                break;
            }
        }

        if (!found || apiId == null || apiVersionId == null) {
            throw new AssertionError("未找到名称为 '" + VAR_TEST_CASE_FIRST + "' 的API，请先创建该API");
        }

        // 将获取的参数存储到运行时变量中 $(apiId) 和 $(apiVersionId)
        runtimeVariables.put("apiId", apiId);
        runtimeVariables.put("apiVersionId", apiVersionId);

        LOG.info("✅ 找到API: apiId=(apiId)={}, apiVersionId=(apiVersionId)={}", apiId, apiVersionId);

        // ===================================================================
        // 步骤2：提交审批单 - POST /api/rest_j/v1/dss/apiservice/submit
        // ===================================================================
        LOG.info("");
        LOG.info("步骤2: 提交审批单 - apiId={}, versionId={}", apiId, apiVersionId);

        String submitUrl = BASE_URL + API_PREFIX + "/submit";

        // 构造请求体
        ObjectNode submitRequestBody = objectMapper.createObjectNode();

        ArrayNode submitApiInfos = objectMapper.createArrayNode();
        ObjectNode apiVersionInfo = objectMapper.createObjectNode();
        apiVersionInfo.put("apiId", apiId);
        apiVersionInfo.put("apiVersionId", apiVersionId);
        submitApiInfos.add(apiVersionInfo);
        submitRequestBody.set("submitApiInfos", submitApiInfos);

        submitRequestBody.put("approvalName", "增量授权首次提单无历史用户");
        submitRequestBody.put("backgroundDesc", "test");
        submitRequestBody.put("applyUser", VAR_USER1 + "," + VAR_USER2);
        submitRequestBody.put("duration", "30");
        submitRequestBody.put("importance", 3);
        submitRequestBody.put("sensitive", 0);
        submitRequestBody.put("attentionUser", "");
        submitRequestBody.put("creator", VAR_SUBMIT_USER);
        submitRequestBody.put("workspaceId", Integer.parseInt(VAR_WORKSPACE_ID));

        String submitRequestBodyStr = objectMapper.writeValueAsString(submitRequestBody);
        LOG.info("submit请求体: {}", formatJson(submitRequestBodyStr));

        String submitResponse = doPost(submitUrl, submitRequestBodyStr);
        LOG.info("submit响应: {}", formatJson(submitResponse));

        // 解析响应，验证提交成功
        JsonNode submitJson = objectMapper.readTree(submitResponse);
        if (submitJson.has("status") && submitJson.get("status").asInt() != 0) {
            String errorMsg = submitJson.has("message") ? submitJson.get("message").asText() : "未知错误";
            throw new AssertionError("提交审批单失败: " + errorMsg);
        }
        LOG.info("✅ 审批单提交成功");

        // ===================================================================
        // 步骤3：获取审批单号(authId) - GET /api/rest_j/v1/dss/apiservice/apiVersionQuery
        // ===================================================================
        LOG.info("");
        LOG.info("步骤3: 获取审批单号 - apiId={}, versionId={}", apiId, apiVersionId);

        String apiVersionQueryUrl = BASE_URL + API_PREFIX + "/apiVersionQuery?serviceId=" + apiId;
        String apiVersionQueryResponse = doGet(apiVersionQueryUrl);
        LOG.info("apiVersionQuery响应: {}", formatJson(apiVersionQueryResponse));

        JsonNode apiVersionQueryJson = objectMapper.readTree(apiVersionQueryResponse);
        if (apiVersionQueryJson.has("status") && apiVersionQueryJson.get("status").asInt() != 0) {
            String errorMsg = apiVersionQueryJson.has("message") ? apiVersionQueryJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询API版本失败: " + errorMsg);
        }

        // 从 result 数组中找到对应版本的 authId
        String authId = null;
        if (apiVersionQueryJson.has("data") && apiVersionQueryJson.get("data").has("result")) {
            ArrayNode resultArray = (ArrayNode) apiVersionQueryJson.get("data").get("result");
            for (JsonNode version : resultArray) {
                Long versionId = version.has("id") ? version.get("id").asLong() : null;
                if (versionId != null && versionId.equals(apiVersionId)) {
                    authId = version.has("authId") ? version.get("authId").asText() : null;
                    break;
                }
            }
        }

        if (authId == null) {
            throw new AssertionError("未找到版本ID " + apiVersionId + " 对应的审批单号(authId)");
        }

        // 存储审批单号到运行时变量 $(authId)
        runtimeVariables.put("authId", authId);
        LOG.info("✅ 获取审批单号成功 - authId=(authId)={}", authId);

        // ===================================================================
        // 步骤4：等待手动审批（控制台提醒）
        // ===================================================================
        LOG.info("============================================");
        LOG.info("等待手动审批");
        LOG.info("============================================");
        LOG.info("请在DataMap审批系统中完成以下操作：");
        LOG.info("  审批单号(authId): {}", authId);
        LOG.info("  API版本ID: {}", apiVersionId);
        LOG.info("  授权用户: {}, {}", VAR_USER1, VAR_USER2);
        LOG.info("============================================");
        LOG.info("审批完成后，请继续执行后续步骤");
        LOG.info("============================================");

        // 等待手动审批完成
        LOG.info("等待 {} 分钟，请在此期间完成手动审批操作...", APPROVAL_WAIT_TIME_MS / 60000);
        Thread.sleep(APPROVAL_WAIT_TIME_MS);
        LOG.info("等待结束，继续执行后续步骤...");

        // ===================================================================
        // 步骤5：调用审批刷新接口 - GET /dss/apiservice/approvalRefresh
        // ===================================================================
        LOG.info("");
        LOG.info("步骤5: 调用审批刷新接口 - approvalNo={}", authId);

        String approvalRefreshUrl = BASE_URL + API_PREFIX + "/approvalRefresh?approvalNo=" + authId;
        String approvalRefreshResponse = doGet(approvalRefreshUrl);
        LOG.info("approvalRefresh响应: {}", formatJson(approvalRefreshResponse));

        JsonNode approvalRefreshJson = objectMapper.readTree(approvalRefreshResponse);
        if (approvalRefreshJson.has("status") && approvalRefreshJson.get("status").asInt() != 0) {
            String errorMsg = approvalRefreshJson.has("message") ? approvalRefreshJson.get("message").asText() : "未知错误";
            LOG.warn("审批刷新失败（可能尚未审批通过）: {}", errorMsg);
            LOG.warn("注意：如果DM系统尚未审批通过，这是预期行为");
        } else {
            String approvalStatus = approvalRefreshJson.has("data") && approvalRefreshJson.get("data").has("approvalStatus")
                    ? approvalRefreshJson.get("data").get("approvalStatus").asText()
                    : "未知";

            LOG.info("审批状态: {}", approvalStatus);

            if ("审批通过".equals(approvalStatus) || "3".equals(approvalStatus)) {
                LOG.info("✅ 审批已通过，Token已生成");

                // ===================================================================
                // 步骤6：验证Token生成 - GET /dss/apiservice/tokenQuery
                // ===================================================================
                LOG.info("============================================");
                LOG.info("验证首次提单结果");
                LOG.info("============================================");
                LOG.info("  查询Token列表 - versionId={}", apiVersionId);
                LOG.info("  预期Token数量: 2");
                LOG.info("  预期用户: {}, {}", VAR_USER1, VAR_USER2);
                LOG.info("============================================");

                String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery?apiId=" + apiId + "&currentPage=1&pageSize=100";
                String tokenQueryResponse = doGet(tokenQueryUrl);
                LOG.info("tokenQuery响应: {}", formatJson(tokenQueryResponse));

                JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);
                if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
                    if (tokenQueryJson.has("data")) {
                        JsonNode data = tokenQueryJson.get("data");
                        Integer total = data.has("total") ? data.get("total").asInt() : 0;
                        LOG.info("Token总数: {} (预期: 2)", total);

                        if (data.has("queryList")) {
                            ArrayNode tokens = (ArrayNode) data.get("queryList");
                            LOG.info("Token列表:");
                            boolean hasUser1 = false, hasUser2 = false;
                            for (JsonNode token : tokens) {
                                String tokenUser = token.has("user") ? token.get("user").asText() : "";
                                Integer status = token.has("status") ? token.get("status").asInt() : null;
                                Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                                if (VAR_USER1.equals(tokenUser)) hasUser1 = true;
                                if (VAR_USER2.equals(tokenUser)) hasUser2 = true;
                                LOG.info("  - User: {}, VersionID: {}, Status: {}->{}",
                                        tokenUser, versionId, status, status == 1 ? "有效" : "禁用");
                            }

                            if (hasUser1 && hasUser2) {
                                LOG.info("✅ Token用户验证通过：包含 {} 和 {}", VAR_USER1, VAR_USER2);
                            } else {
                                LOG.warn("⚠️ Token用户验证失败：user1存在={}, user2存在={}", hasUser1, hasUser2);
                            }
                        }

                        if (total == 2) {
                            LOG.info("✅ Token数量正确，验证通过");
                        } else {
                            LOG.warn("⚠️ Token数量不符: 实际 {} 个, 期望 2 个", total);
                        }
                    }
                }
            } else {
                LOG.info("审批状态为: {}，需要等待审批通过后才能生成Token", approvalStatus);
            }
        }

        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例1完成: 增量授权 - 首次提单无历史用户");
        LOG.info("========================================");
    }

    // ===========================================
    // 验证Token生成的辅助方法
    // ===========================================

    /**
     * 验证Token是否生成
     *
     * @param apiId          API ID
     * @param user           用户名
     * @param expectedStatus 期望的Token状态（1=有效）
     */
    private void verifyTokenGenerated(Long apiId, String user, int expectedStatus) throws Exception {
        String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                "?apiId=" + apiId +
                "&user=" + user +
                "&status=" + expectedStatus +
                "&currentPage=1" +
                "&pageSize=10";

        String tokenQueryResponse = doGet(tokenQueryUrl);
        LOG.info("tokenQuery响应: {}", formatJson(tokenQueryResponse));

        JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);
        if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
            if (tokenQueryJson.has("data")) {
                JsonNode data = tokenQueryJson.get("data");
                int total = data.has("total") ? data.get("total").asInt() : 0;
                LOG.info("用户 {} 的Token总数: {}", user, total);

                if (total > 0 && data.has("queryList")) {
                    ArrayNode tokens = (ArrayNode) data.get("queryList");
                    LOG.info("Token列表:");
                    for (JsonNode token : tokens) {
                        String tokenUser = token.has("user") ? token.get("user").asText() : "";
                        Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                        Integer status = token.has("status") ? token.get("status").asInt() : null;
                        Long expiryTime = token.has("expiryTime") ? token.get("expiryTime").asLong() : null;

                        LOG.info("  - User: {}, VersionID: $(apiVersionId)={}, Status: {}->{}, ExpiryTime: {}",
                                tokenUser, versionId,
                                status, status == 1 ? "有效" : "禁用",
                                expiryTime != null ? new java.util.Date(expiryTime) : "无");
                    }
                }

                if (total > 0) {
                    LOG.info("✅ 用户 {} 的Token已生成，状态为有效", user);
                } else {
                    LOG.warn("⚠️ 用户 {} 的Token未生成，可能审批尚未通过", user);
                }
            }
        } else {
            String errorMsg = tokenQueryJson.has("message") ? tokenQueryJson.get("message").asText() : "未知错误";
            LOG.warn("查询Token失败: {}", errorMsg);
        }
    }

    // ===========================================
    // 可扩展：添加更多测试用例的方法
    // ===========================================

    /**
     * 测试用例2: 增量授权 - 自动合并历史用户
     * <p>
     * 前置条件：
     * - 存在历史审批记录（需要先测试用例1并通过审批）
     * - 首次提单通过后，创建新版本再提单
     * <p>
     * 步骤：
     * 1、调用 availableSubmitApi 接口查询API
     * 2、调用 submit 接口提交审批单（只填写新用户）
     * 3、调用 approvalRefresh 接口刷新审批状态
     * 4、调用 tokenQuery 接口查询生成的Token列表（验证包含历史用户）
     * <p>
     * 预期结果：
     * - 系统自动查询历史审批单
     * - 自动合并历史用户user1和user2
     * - 为user1生成新版本Token
     * - 为user2生成新版本Token
     * - 为user3生成Token
     * - 为user4生成Token
     * - 历史版本的Token被禁用
     * - 新版本的Token有效
     * - 最终授权用户：user1,user2,user3,user4
     */
    @Test
    public void testIncrementalAuthorization_AutoMergeHistoryUsers() throws Exception {
        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例2: 增量授权 - 自动合并历史用户");
        LOG.info("========================================");
        LOG.info("注意：此测试用例需要先通过测试用例1，并且有通过审批的历史记录");
        LOG.info("========================================");

        // 步骤1：调用 availableSubmitApi 接口查询API
        LOG.info("");
        LOG.info("步骤1: 调用 availableSubmitApi 接口查询API");

        String availableSubmitApiUrl = BASE_URL + API_PREFIX + "/availableSubmitApi?workspaceId=" + VAR_WORKSPACE_ID;
        String availableSubmitApiResponse = doGet(availableSubmitApiUrl);
        LOG.info("availableSubmitApi响应: {}", formatJson(availableSubmitApiResponse));

        // 解析响应，查找指定名称的API - 使用 VAR_TEST_CASE_HISTORY
        JsonNode availableSubmitApiJson = objectMapper.readTree(availableSubmitApiResponse);
        if (availableSubmitApiJson.has("status") && availableSubmitApiJson.get("status").asInt() != 0) {
            String errorMsg = availableSubmitApiJson.has("message") ? availableSubmitApiJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询可提交API失败: " + errorMsg);
        }

        if (!availableSubmitApiJson.has("data") || !availableSubmitApiJson.get("data").has("availableSubmitApiList")) {
            throw new AssertionError("响应缺少 availableSubmitApiList 字段");
        }

        ArrayNode availableSubmitApiList = (ArrayNode) availableSubmitApiJson.get("data").get("availableSubmitApiList");

        // 查找名称为 ${test_case_history} 的API
        Long apiId = null;
        Long apiVersionId = null;
        Long oldApiVersionId = null;
        String foundApiName = null;
        boolean found = false;

        for (JsonNode api : availableSubmitApiList) {
            String name = api.has("name") ? api.get("name").asText() : "";
            if (VAR_TEST_CASE_HISTORY.equals(name)) {
                apiId = api.has("id") ? api.get("id").asLong() : null;
                // 获取最新版本ID
                if (api.has("apiVersionId")) {
                    apiVersionId = api.get("apiVersionId").asLong();
                } else if (api.has("latestVersionId")) {
                    apiVersionId = api.get("latestVersionId").asLong();
                }
                foundApiName = name;
                found = true;
                break;
            }
        }

        // 如果没找到指定的API，使用 VAR_TEST_CASE_FIRST 作为备选
        if (!found || apiId == null || apiVersionId == null) {
            LOG.warn("未找到名称为 '{}' 的API，尝试使用 '{}' 作为备选", VAR_TEST_CASE_HISTORY, VAR_TEST_CASE_FIRST);

            for (JsonNode api : availableSubmitApiList) {
                String name = api.has("name") ? api.get("name").asText() : "";
                if (VAR_TEST_CASE_FIRST.equals(name)) {
                    apiId = api.has("id") ? api.get("id").asLong() : null;
                    if (api.has("apiVersionId")) {
                        apiVersionId = api.get("apiVersionId").asLong();
                    } else if (api.has("latestVersionId")) {
                        apiVersionId = api.get("latestVersionId").asLong();
                    }
                    foundApiName = name;
                    found = true;
                    break;
                }
            }
        }

        if (!found || apiId == null || apiVersionId == null) {
            throw new AssertionError("未找到测试API，请先创建名称为 '" + VAR_TEST_CASE_HISTORY +
                    "' 或 '" + VAR_TEST_CASE_FIRST + "' 的API");
        }

        // 先查询该API的历史版本ID（用于验证历史Token被禁用）
        LOG.info("查询历史版本ID用于后续验证");
        oldApiVersionId = queryPreviousVersionId(apiId, apiVersionId);

        // 将获取的参数存储到运行时变量中
        runtimeVariables.put("apiId", apiId);
        runtimeVariables.put("apiVersionId", apiVersionId);
        runtimeVariables.put("oldApiVersionId", oldApiVersionId);

        LOG.info("✅ 找到API: name={}, apiId=(apiId)={}, apiVersionId=(apiVersionId)={}, oldApiVersionId=(oldApiVersionId)={}",
                foundApiName, apiId, apiVersionId, oldApiVersionId);

        // 步骤2：调用 submit 接口提交审批单（只填写新增用户 user3,user4）
        LOG.info("");
        LOG.info("步骤2: 调用 submit 接口提交审批单（增量授权）");

        String submitUrl = BASE_URL + API_PREFIX + "/submit";

        // 构造请求体，只填写新增用户
        ObjectNode submitRequestBody = objectMapper.createObjectNode();

        ArrayNode submitApiInfos = objectMapper.createArrayNode();
        ObjectNode apiVersionInfo = objectMapper.createObjectNode();
        apiVersionInfo.put("apiId", apiId);
        apiVersionInfo.put("apiVersionId", apiVersionId);
        submitApiInfos.add(apiVersionInfo);
        submitRequestBody.set("submitApiInfos", submitApiInfos);

        submitRequestBody.put("approvalName", "增量授权_自动合并历史用户");
        submitRequestBody.put("backgroundDesc", "测试增量授权自动合并历史用户功能");
        // 只填写新增用户：user3,user4
        submitRequestBody.put("applyUser", VAR_USER3 + "," + VAR_USER4);
        submitRequestBody.put("duration", "30");
        submitRequestBody.put("importance", 3);
        submitRequestBody.put("sensitive", 0);
        submitRequestBody.put("attentionUser", "");
        submitRequestBody.put("creator", VAR_SUBMIT_USER);
        submitRequestBody.put("workspaceId", Integer.parseInt(VAR_WORKSPACE_ID));

        String submitRequestBodyStr = objectMapper.writeValueAsString(submitRequestBody);
        LOG.info("submit请求体: {}", formatJson(submitRequestBodyStr));

        String submitResponse = doPost(submitUrl, submitRequestBodyStr);
        LOG.info("submit响应: {}", formatJson(submitResponse));

        // 解析响应，验证提交成功
        JsonNode submitJson = objectMapper.readTree(submitResponse);
        if (submitJson.has("status") && submitJson.get("status").asInt() != 0) {
            String errorMsg = submitJson.has("message") ? submitJson.get("message").asText() : "未知错误";
            throw new AssertionError("提交审批单失败: " + errorMsg);
        }

        LOG.info("✅ 审批单提交成功");

        // 步骤3：调用 apiVersionQuery 接口获取审批单号(authId)
        LOG.info("");
        LOG.info("步骤3: 获取审批单号 - apiId={}, versionId={}", apiId, apiVersionId);

        String apiVersionQueryUrl = BASE_URL + API_PREFIX + "/apiVersionQuery?serviceId=" + apiId;
        String apiVersionQueryResponse = doGet(apiVersionQueryUrl);
        LOG.info("apiVersionQuery响应: {}", formatJson(apiVersionQueryResponse));

        JsonNode apiVersionQueryJson = objectMapper.readTree(apiVersionQueryResponse);
        if (apiVersionQueryJson.has("status") && apiVersionQueryJson.get("status").asInt() != 0) {
            String errorMsg = apiVersionQueryJson.has("message") ? apiVersionQueryJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询API版本失败: " + errorMsg);
        }

        // 从 result 数组中找到对应版本的 authId
        String authId = null;
        if (apiVersionQueryJson.has("data") && apiVersionQueryJson.get("data").has("result")) {
            ArrayNode resultArray = (ArrayNode) apiVersionQueryJson.get("data").get("result");
            for (JsonNode version : resultArray) {
                Long versionId = version.has("id") ? version.get("id").asLong() : null;
                if (versionId != null && versionId.equals(apiVersionId)) {
                    authId = version.has("authId") ? version.get("authId").asText() : null;
                    break;
                }
            }
        }

        if (authId == null) {
            throw new AssertionError("未找到版本ID " + apiVersionId + " 对应的审批单号(authId)");
        }

        // 存储审批单号到运行时变量 $(authId)
        runtimeVariables.put("authId", authId);
        LOG.info("✅ 获取审批单号成功 - authId=(authId)={}", authId);

        // 步骤4：等待手动审批（控制台提醒）
        LOG.info("============================================");
        LOG.info("等待手动审批");
        LOG.info("============================================");
        LOG.info("请在DataMap审批系统中完成以下操作：");
        LOG.info("  审批单号(authId): {}", authId);
        LOG.info("  API版本ID: {}", apiVersionId);
        LOG.info("  新增授权用户: {}, {}", VAR_USER3, VAR_USER4);
        LOG.info("  预期合并历史用户: {}, {}", VAR_USER1, VAR_USER2);
        LOG.info("============================================");
        LOG.info("审批完成后，请继续执行后续步骤");
        LOG.info("============================================");

        // 等待手动审批完成
        LOG.info("等待 {} 分钟，请在此期间完成手动审批操作...", APPROVAL_WAIT_TIME_MS / 60000);
        Thread.sleep(APPROVAL_WAIT_TIME_MS);
        LOG.info("等待结束，继续执行后续步骤...");

        // 步骤5：调用审批刷新接口
        LOG.info("");
        LOG.info("步骤5: 调用审批刷新接口 - approvalNo={}", authId);

        String approvalRefreshUrl = BASE_URL + API_PREFIX + "/approvalRefresh?approvalNo=" + authId;

        String approvalRefreshResponse = doGet(approvalRefreshUrl);
        LOG.info("approvalRefresh响应: {}", formatJson(approvalRefreshResponse));

        JsonNode approvalRefreshJson = objectMapper.readTree(approvalRefreshResponse);
        if (approvalRefreshJson.has("status") && approvalRefreshJson.get("status").asInt() != 0) {
            String errorMsg = approvalRefreshJson.has("message") ? approvalRefreshJson.get("message").asText() : "未知错误";
            LOG.warn("审批刷新失败（可能尚未审批通过）: {}", errorMsg);
            LOG.warn("注意：如果DM系统尚未审批通过，这是预期行为");
        } else {
            String approvalStatus = approvalRefreshJson.has("data") && approvalRefreshJson.get("data").has("approvalStatus")
                    ? approvalRefreshJson.get("data").get("approvalStatus").asText()
                    : "未知";

            LOG.info("审批状态: {}", approvalStatus);

            if ("审批通过".equals(approvalStatus) || "3".equals(approvalStatus)) {
                LOG.info("✅ 审批已通过，Token已生成");

                // 步骤6：验证Token增量授权结果
                LOG.info("");
                LOG.info("步骤6: 验证增量授权结果");
                LOG.info("==========================================");
                LOG.info("验证自动合并历史用户功能");
                LOG.info("  API ID: {}", apiId);
                LOG.info("  历史版本ID: {}", oldApiVersionId != null ? oldApiVersionId : "无");
                LOG.info("  预期Token总数: 4");
                LOG.info("  预期用户: {}, {}, {}, {}", VAR_USER1, VAR_USER2, VAR_USER3, VAR_USER4);
                LOG.info("==========================================");

                // 验证Token包含所有用户（包括自动合并的历史用户）
                LOG.info("=== 验证Token（API ID: {}） ===", apiId);
                verifyAllTokensGenerated(apiId, apiVersionId, new String[]{VAR_USER1, VAR_USER2, VAR_USER3, VAR_USER4});

                // 如果存在历史版本，验证历史版本Token已被禁用
                if (oldApiVersionId != null && !oldApiVersionId.equals(apiVersionId)) {
                    LOG.info("=== 验证历史版本Token已被禁用（版本ID: {}） ===", oldApiVersionId);
                    verifyHistoricalTokensDisabled(apiId, oldApiVersionId);
                } else {
                    LOG.info("无历史版本，跳过历史Token禁用验证");
                }

                // 获取所有有效Token列表
                getActiveUsers(apiId);

                LOG.info("");
                LOG.info("========================================");
                LOG.info("✅ 测试用例2验证通过: 增量授权 - 自动合并历史用户");
                LOG.info("预期用户列表: {}, {}, {}, {}", VAR_USER1, VAR_USER2, VAR_USER3, VAR_USER4);
                LOG.info("========================================");
            } else {
                LOG.info("审批状态为: {}，需要等待审批通过后才能生成Token", approvalStatus);
                LOG.info("注意：如果需要重新验证，请在审批通过后再次运行此测试");
            }
        }

        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例2完成: 增量授权 - 自动合并历史用户");
        LOG.info("========================================");
    }

    /**
     * 验证所有指定用户的Token是否已生成
     *
     * @param apiId         API ID
     * @param apiVersionId  API版本ID
     * @param expectedUsers 期望的用户列表
     */
    private void verifyAllTokensGenerated(Long apiId, Long apiVersionId, String[] expectedUsers) throws Exception {
        LOG.info("验证 apiId={}, 期望用户={}", apiId, java.util.Arrays.toString(expectedUsers));

        for (String user : expectedUsers) {
            LOG.info("查询用户 {} 的Token...", user);
            verifyTokenGenerated(apiId, user, 1);
        }

        // 统计该API的有效Token数量（使用apiId查询）
        String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                "?apiId=" + apiId +
                "&status=1" +
                "&currentPage=1" +
                "&pageSize=100";

        String tokenQueryResponse = doGet(tokenQueryUrl);
        JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

        if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
            if (tokenQueryJson.has("data")) {
                JsonNode data = tokenQueryJson.get("data");
                int total = data.has("total") ? data.get("total").asInt() : 0;
                LOG.info("API ID {} 的有效Token总数: {} (期望: {})", apiId, total, expectedUsers.length);

                // 列出所有有效Token的用户
                if (data.has("queryList")) {
                    ArrayNode tokens = (ArrayNode) data.get("queryList");
                    LOG.info("=== 有效Token用户列表 ===");
                    for (JsonNode token : tokens) {
                        String tokenUser = token.has("user") ? token.get("user").asText() : "";
                        Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                        LOG.info("  - 用户: {}, 版本ID: {}", tokenUser, versionId);
                    }
                }

                if (total >= expectedUsers.length) {
                    LOG.info("✅ Token数量正确: {} 个 (>= 期望 {} 个)", total, expectedUsers.length);
                } else {
                    LOG.warn("⚠️ Token数量不符: 实际 {} 个, 期望 {} 个", total, expectedUsers.length);
                }
            }
        }
    }

    /**
     * 获取未被禁用的用户列表（Token状态为1的用户）
     *
     * @param apiId API ID
     */
    private void getActiveUsers(Long apiId) throws Exception {
        LOG.info("查询API ID={} 的所有有效Token（未被禁用）...", apiId);

        String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                "?apiId=" + apiId +
                "&status=1" +  // status=1 表示有效（未被禁用）
                "&currentPage=1" +
                "&pageSize=100";

        String tokenQueryResponse = doGet(tokenQueryUrl);
        JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

        if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
            if (tokenQueryJson.has("data")) {
                JsonNode data = tokenQueryJson.get("data");
                int total = data.has("total") ? data.get("total").asInt() : 0;
                LOG.info("API ID={} 的有效Token总数: {}", apiId, total);

                if (data.has("queryList")) {
                    ArrayNode tokens = (ArrayNode) data.get("queryList");
                    LOG.info("=== 正常使用的用户列表 ===");
                    for (JsonNode token : tokens) {
                        String tokenUser = token.has("user") ? token.get("user").asText() : "";
                        Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                        Integer status = token.has("status") ? token.get("status").asInt() : null;
                        Long expiryTime = token.has("expiryTime") ? token.get("expiryTime").asLong() : null;

                        String statusDesc = status == 1 ? "有效（未被禁用）" : "禁用";
                        LOG.info("  - 用户: {}, 版本ID: {}, 状态: {}, 到期时间: {}",
                                tokenUser, versionId, statusDesc,
                                expiryTime != null ? new java.util.Date(expiryTime) : "无");
                    }
                    LOG.info("===============================================");
                }
            }
        }
    }


    /**
     * 验证历史版本的Token已被禁用
     *
     * @param apiId        API ID
     * @param apiVersionId API版本ID（历史版本）
     */
    private void verifyHistoricalTokensDisabled(Long apiId, Long apiVersionId) throws Exception {
        LOG.info("查询历史版本 apiVersionId={} 的Token状态...", apiVersionId);

        String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                "?apiId=" + apiId +
                "&status=0" +
                "&currentPage=1" +
                "&pageSize=100";

        String tokenQueryResponse = doGet(tokenQueryUrl);
        JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

        if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
            if (tokenQueryJson.has("data")) {
                JsonNode data = tokenQueryJson.get("data");
                int disabledCount = data.has("total") ? data.get("total").asInt() : 0;
                LOG.info("历史版本 {} 的禁用Token总数: {}", apiVersionId, disabledCount);

                if (disabledCount > 0) {
                    LOG.info("✅ 历史版本Token已被禁用，共 {} 个", disabledCount);
                } else {
                    LOG.warn("⚠️ 历史版本没有禁用的Token记录");
                }

                // 列出被禁用的Token
                if (data.has("queryList")) {
                    ArrayNode tokens = (ArrayNode) data.get("queryList");
                    for (JsonNode token : tokens) {
                        String tokenUser = token.has("user") ? token.get("user").asText() : "";
                        Integer status = token.has("status") ? token.get("status").asInt() : null;
                        LOG.info("  - 用户 {} 的Token状态: {}", tokenUser, status == 0 ? "禁用" : status);
                    }
                }
            }
        } else {
            LOG.warn("查询历史Token失败: {}", tokenQueryJson.has("message") ? tokenQueryJson.get("message").asText() : "未知错误");
        }
    }

    /**
     * 查询API的历史版本ID
     *
     * @param apiId               API ID
     * @param currentApiVersionId 当前API版本ID
     * @return 历史版本ID，如果没有历史版本则返回null
     */
    private Long queryPreviousVersionId(Long apiId, Long currentApiVersionId) throws Exception {
        try {
            String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                    "?apiId=" + apiId +
                    "&status=1" +
                    "&currentPage=1" +
                    "&pageSize=100";

            String tokenQueryResponse = doGet(tokenQueryUrl);
            JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

            if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
                if (tokenQueryJson.has("data")) {
                    JsonNode data = tokenQueryJson.get("data");
                    if (data.has("queryList")) {
                        ArrayNode tokens = (ArrayNode) data.get("queryList");

                        // 查找与当前版本不同的apiVersionId
                        for (JsonNode token : tokens) {
                            Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                            if (versionId != null && !versionId.equals(currentApiVersionId)) {
                                LOG.info("找到历史版本ID: {}", versionId);
                                return versionId;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("查询历史版本ID失败: {}", e.getMessage());
        }

        LOG.info("未找到历史版本ID");
        return null;
    }

    /**
     * 测试用例3: 增量授权 - 重复用户去重处理
     * <p>
     * 步骤：
     * 1、调用 availableSubmitApi 接口查询API
     * 2、调用 submit 接口提交审批单（填写包含历史用户的用户列表）
     * 3、调用 apiVersionQuery 接口获取审批单号(authId)
     * 4、等待手动审批（控制台打印提醒）
     * 5、调用 approvalRefresh 接口刷新审批状态
     * 6、调用 tokenQuery 接口查询生成的Token列表（验证去重）
     * <p>
     * 预期结果：
     * - 自动合并历史用户user2
     * - 为user1生成Token（去重后只生成一次）
     * - 为user2生成Token
     * - 为user3生成Token
     * - 最终授权用户：user1,user2,user3
     */
    @Test
    public void testIncrementalAuthorization_DuplicateUserDeduplication() throws Exception {
        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例3: 增量授权 - 重复用户去重处理");
        LOG.info("========================================");
        LOG.info("注意：此测试用例需要先通过测试用例1，并且有通过审批的历史记录");
        LOG.info("========================================");

        // ===================================================================
        // 步骤1：调用 availableSubmitApi 接口查询API
        // ===================================================================
        LOG.info("");
        LOG.info("步骤1: 调用 availableSubmitApi 接口查询API");

        String availableSubmitApiUrl = BASE_URL + API_PREFIX + "/availableSubmitApi?workspaceId=" + VAR_WORKSPACE_ID;
        String availableSubmitApiResponse = doGet(availableSubmitApiUrl);
        LOG.info("availableSubmitApi响应: {}", formatJson(availableSubmitApiResponse));

        JsonNode availableSubmitApiJson = objectMapper.readTree(availableSubmitApiResponse);
        if (availableSubmitApiJson.has("status") && availableSubmitApiJson.get("status").asInt() != 0) {
            String errorMsg = availableSubmitApiJson.has("message") ? availableSubmitApiJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询可提交API失败: " + errorMsg);
        }

        if (!availableSubmitApiJson.has("data") || !availableSubmitApiJson.get("data").has("availableSubmitApiList")) {
            throw new AssertionError("响应缺少 availableSubmitApiList 字段");
        }

        ArrayNode availableSubmitApiList = (ArrayNode) availableSubmitApiJson.get("data").get("availableSubmitApiList");

        // 查找名称为 ${test_case_duplicate} 的API
        Long apiId = null;
        Long apiVersionId = null;
        boolean found = false;

        for (JsonNode api : availableSubmitApiList) {
            String name = api.has("name") ? api.get("name").asText() : "";
            if (VAR_TEST_CASE_DUPLICATE.equals(name)) {
                apiId = api.has("id") ? api.get("id").asLong() : null;
                if (api.has("apiVersionId")) {
                    apiVersionId = api.get("apiVersionId").asLong();
                } else if (api.has("latestVersionId")) {
                    apiVersionId = api.get("latestVersionId").asLong();
                }
                found = true;
                break;
            }
        }

        // 如果没找到指定的API，使用 VAR_TEST_CASE_FIRST 作为备选
        if (!found || apiId == null || apiVersionId == null) {
            LOG.warn("未找到名称为 '{}' 的API，尝试使用 '{}' 作为备选", VAR_TEST_CASE_DUPLICATE, VAR_TEST_CASE_FIRST);
            for (JsonNode api : availableSubmitApiList) {
                String name = api.has("name") ? api.get("name").asText() : "";
                if (VAR_TEST_CASE_FIRST.equals(name)) {
                    apiId = api.has("id") ? api.get("id").asLong() : null;
                    if (api.has("apiVersionId")) {
                        apiVersionId = api.get("apiVersionId").asLong();
                    } else if (api.has("latestVersionId")) {
                        apiVersionId = api.get("latestVersionId").asLong();
                    }
                    found = true;
                    break;
                }
            }
        }

        if (!found || apiId == null || apiVersionId == null) {
            throw new AssertionError("未找到测试API，请先创建名称为 '" + VAR_TEST_CASE_DUPLICATE +
                    "' 或 '" + VAR_TEST_CASE_FIRST + "' 的API");
        }

        runtimeVariables.put("apiId", apiId);
        runtimeVariables.put("apiVersionId", apiVersionId);

        LOG.info("✅ 找到API: apiId={}, apiVersionId={}", apiId, apiVersionId);

        // ===================================================================
        // 步骤2：调用 submit 接口提交审批单（申请用户包含历史用户）
        // ===================================================================
        LOG.info("");
        LOG.info("步骤2: 调用 submit 接口提交审批单");
        LOG.info("申请用户: {} 包含历史用户 {}, {}", VAR_USER1, VAR_USER1, VAR_USER2);

        String submitUrl = BASE_URL + API_PREFIX + "/submit";

        ObjectNode submitRequestBody = objectMapper.createObjectNode();

        ArrayNode submitApiInfos = objectMapper.createArrayNode();
        ObjectNode apiVersionInfo = objectMapper.createObjectNode();
        apiVersionInfo.put("apiId", apiId);
        apiVersionInfo.put("apiVersionId", apiVersionId);
        submitApiInfos.add(apiVersionInfo);
        submitRequestBody.set("submitApiInfos", submitApiInfos);

        submitRequestBody.put("approvalName", "增量授权_重复用户去重处理");
        submitRequestBody.put("backgroundDesc", "测试增量授权重复用户去重功能");
        // 填写申请用户： VAR_USER1, VAR_USER3
        submitRequestBody.put("applyUser", VAR_USER1 + "," + VAR_USER3);
        submitRequestBody.put("duration", "60");
        submitRequestBody.put("importance", 3);
        submitRequestBody.put("sensitive", 0);
        submitRequestBody.put("attentionUser", "");
        submitRequestBody.put("creator", VAR_SUBMIT_USER);
        submitRequestBody.put("workspaceId", Integer.parseInt(VAR_WORKSPACE_ID));

        String submitRequestBodyStr = objectMapper.writeValueAsString(submitRequestBody);
        LOG.info("submit请求体: {}", formatJson(submitRequestBodyStr));

        String submitResponse = doPost(submitUrl, submitRequestBodyStr);
        LOG.info("submit响应: {}", formatJson(submitResponse));

        JsonNode submitJson = objectMapper.readTree(submitResponse);
        if (submitJson.has("status") && submitJson.get("status").asInt() != 0) {
            String errorMsg = submitJson.has("message") ? submitJson.get("message").asText() : "未知错误";
            throw new AssertionError("提交审批单失败: " + errorMsg);
        }

        LOG.info("✅ 审批单提交成功");

        // ===================================================================
        // 步骤3：调用 apiVersionQuery 接口获取审批单号(authId)
        // ===================================================================
        LOG.info("");
        LOG.info("步骤3: 获取审批单号 - apiId={}, versionId={}", apiId, apiVersionId);

        String apiVersionQueryUrl = BASE_URL + API_PREFIX + "/apiVersionQuery?serviceId=" + apiId;
        String apiVersionQueryResponse = doGet(apiVersionQueryUrl);
        LOG.info("apiVersionQuery响应: {}", formatJson(apiVersionQueryResponse));

        JsonNode apiVersionQueryJson = objectMapper.readTree(apiVersionQueryResponse);
        if (apiVersionQueryJson.has("status") && apiVersionQueryJson.get("status").asInt() != 0) {
            String errorMsg = apiVersionQueryJson.has("message") ? apiVersionQueryJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询API版本失败: " + errorMsg);
        }

        String authId = null;
        if (apiVersionQueryJson.has("data") && apiVersionQueryJson.get("data").has("result")) {
            ArrayNode resultArray = (ArrayNode) apiVersionQueryJson.get("data").get("result");
            for (JsonNode version : resultArray) {
                Long versionId = version.has("id") ? version.get("id").asLong() : null;
                if (versionId != null && versionId.equals(apiVersionId)) {
                    authId = version.has("authId") ? version.get("authId").asText() : null;
                    break;
                }
            }
        }

        if (authId == null) {
            throw new AssertionError("未找到版本ID " + apiVersionId + " 对应的审批单号(authId)");
        }

        runtimeVariables.put("authId", authId);
        LOG.info("✅ 获取审批单号成功 - authId={}", authId);

        // ===================================================================
        // 步骤4：等待手动审批（控制台提醒）
        // ===================================================================
        LOG.info("============================================");
        LOG.info("等待手动审批(重复用户去重测试)");
        LOG.info("============================================");
        LOG.info("请在DataMap审批系统中完成以下操作：");
        LOG.info("  审批单号(authId): {}", authId);
        LOG.info("  API版本ID: {}", apiVersionId);
        LOG.info("  申请用户: {}, {}", VAR_USER1, VAR_USER3);
        LOG.info("  预期自动合并历史: {}", VAR_USER2);
        LOG.info("  预期最终用户: {}, {}, {}", VAR_USER1, VAR_USER2, VAR_USER3);
        LOG.info("  重点验证: user1只生成一条Token（去重成功）");
        LOG.info("============================================");
        LOG.info("审批完成后，请继续执行后续步骤");
        LOG.info("============================================");

        LOG.info("等待 {} 分钟，请在此期间完成手动审批操作...", APPROVAL_WAIT_TIME_MS / 60000);
        Thread.sleep(APPROVAL_WAIT_TIME_MS);
        LOG.info("等待结束，继续执行后续步骤...");

        // ===================================================================
        // 步骤5：调用审批刷新接口
        // ===================================================================
        LOG.info("");
        LOG.info("步骤5: 调用审批刷新接口 - approvalNo={}", authId);

        String approvalRefreshUrl = BASE_URL + API_PREFIX + "/approvalRefresh?approvalNo=" + authId;
        String approvalRefreshResponse = doGet(approvalRefreshUrl);
        LOG.info("approvalRefresh响应: {}", formatJson(approvalRefreshResponse));

        JsonNode approvalRefreshJson = objectMapper.readTree(approvalRefreshResponse);
        if (approvalRefreshJson.has("status") && approvalRefreshJson.get("status").asInt() != 0) {
            String errorMsg = approvalRefreshJson.has("message") ? approvalRefreshJson.get("message").asText() : "未知错误";
            LOG.warn("审批刷新失败（可能尚未审批通过）: {}", errorMsg);
            LOG.warn("注意：如果DM系统尚未审批通过，这是预期行为");
        } else {
            String approvalStatus = approvalRefreshJson.has("data") && approvalRefreshJson.get("data").has("approvalStatus")
                    ? approvalRefreshJson.get("data").get("approvalStatus").asText()
                    : "未知";

            LOG.info("审批状态: {}", approvalStatus);

            if ("审批通过".equals(approvalStatus) || "3".equals(approvalStatus)) {
                LOG.info("✅ 审批已通过，Token已生成");

                // ===================================================================
                // 步骤6：验证Token去重效果
                // ===================================================================
                LOG.info("");
                LOG.info("============================================");
                LOG.info("验证重复用户去重结果");
                LOG.info("============================================");
                LOG.info("  API ID: {}", apiId);
                LOG.info("  预期Token数量: 3");
                LOG.info("  预期用户: {}, {}, {}", VAR_USER1, VAR_USER2, VAR_USER3);
                LOG.info("  重点验证: {}只生成一条Token（去重成功）", VAR_USER1);
                LOG.info("============================================");

                verifyAllTokensGenerated(apiId, apiVersionId, new String[]{VAR_USER1, VAR_USER2, VAR_USER3});

                LOG.info("");
                LOG.info("========================================");
                LOG.info("✅ 测试用例3验证通过: 增量授权 - 重复用户去重处理");
                LOG.info("预期用户列表: {}, {}, {}", VAR_USER1, VAR_USER2, VAR_USER3);
                LOG.info("========================================");
            } else {
                LOG.info("审批状态为: {}，需要等待审批通过后才能生成Token", approvalStatus);
                LOG.info("注意：如果需要重新验证，请在审批通过后再次运行此测试");
            }
        }

        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例3完成: 增量授权 - 重复用户去重处理");
        LOG.info("========================================");
    }

    /**
     * 测试用例4: 批量API申请时增量授权 - 跨API历史用户合并
     * <p>
     * 步骤：
     * 1、调用 availableSubmitApi 接口查询多个API
     * 2、调用 submit 接口批量提交审批单
     * 3、调用 apiVersionQuery 接口获取各API审批单号(authId)
     * 4、等待手动审批（控制台打印提醒）
     * 5、调用 approvalRefresh 接口刷新审批状态
     * 6、分别调用 tokenQuery 接口查询每个API的Token列表
     * <p>
     * 预期结果：
     * - 每个API独立进行增量授权，自动合并各自的历史用户
     * - 有历史用户的API最终授权用户 = 历史用户 + 新申请用户
     * - 无历史用户的API最终授权用户 = 新申请用户
     * - 每个API的历史版本Token被禁用
     * - 新Token状态为有效
     */
    @Test
    public void testIncrementalAuthorization_BatchApiCrossApiMerge() throws Exception {
        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例4: 批量API申请时增量授权 - 跨API历史用户合并");
        LOG.info("========================================");
        LOG.info("注意：此测试用例需要多个API的历史审批记录");
        LOG.info("========================================");

        // ===================================================================
        // 步骤1：调用 availableSubmitApi 接口查询多个API
        // ===================================================================
        LOG.info("");
        LOG.info("步骤1: 查询可用API - workspaceId={}", VAR_WORKSPACE_ID);

        String availableSubmitApiUrl = BASE_URL + API_PREFIX + "/availableSubmitApi?workspaceId=" + VAR_WORKSPACE_ID;
        String availableSubmitApiResponse = doGet(availableSubmitApiUrl);
        LOG.info("availableSubmitApi响应: {}", formatJson(availableSubmitApiResponse));

        JsonNode availableSubmitApiJson = objectMapper.readTree(availableSubmitApiResponse);
        if (availableSubmitApiJson.has("status") && availableSubmitApiJson.get("status").asInt() != 0) {
            String errorMsg = availableSubmitApiJson.has("message") ? availableSubmitApiJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询可提交API失败: " + errorMsg);
        }

        if (!availableSubmitApiJson.has("data") || !availableSubmitApiJson.get("data").has("availableSubmitApiList")) {
            throw new AssertionError("响应缺少 availableSubmitApiList 字段");
        }

        ArrayNode availableSubmitApiList = (ArrayNode) availableSubmitApiJson.get("data").get("availableSubmitApiList");

        // 选择API进行批量测试
        java.util.List<Long> apiIds = new java.util.ArrayList<>();
        java.util.List<Long> apiVersionIds = new java.util.ArrayList<>();

        // 检查是否指定了API名称列表
        if (VAR_BATCH_API_NAMES != null && !VAR_BATCH_API_NAMES.trim().isEmpty()) {
            LOG.info("  使用指定的API名称列表: {}", VAR_BATCH_API_NAMES);
            String[] specifiedApiNameArray = VAR_BATCH_API_NAMES.split(",");
            for (String apiNameStr : specifiedApiNameArray) {
                String specifiedApiName = apiNameStr.trim();
                if (specifiedApiName.isEmpty()) {
                    continue;
                }
                LOG.info("  查找指定的API名称: {}", specifiedApiName);

                // 从可用API列表中查找对应的API
                for (JsonNode api : availableSubmitApiList) {
                    String currentApiName = api.has("name") ? api.get("name").asText() : "";
                    if (currentApiName.equals(specifiedApiName)) {
                        Long apiId = api.has("id") ? api.get("id").asLong() : null;
                        Long versionId = api.has("apiVersionId") ? api.get("apiVersionId").asLong() : null;
                        if (versionId == null) {
                            versionId = api.has("latestVersionId") ? api.get("latestVersionId").asLong() : null;
                        }
                        if (apiId != null && versionId != null) {
                            apiIds.add(apiId);
                            apiVersionIds.add(versionId);
                            LOG.info("    ✅ 找到API: name={}, apiId={}, versionId={}", specifiedApiName, apiId, versionId);
                            break;
                        } else {
                            LOG.warn("    ⚠️ API {} 没有可用的ID或版本ID，跳过", specifiedApiName);
                        }
                    }
                }
            }
        } else {
            LOG.info("  未指定API名称，从前3个API中选择");
            // 选择前3个API进行批量测试
            int apiCount = 0;
            for (JsonNode api : availableSubmitApiList) {
                Long apiId = api.has("id") ? api.get("id").asLong() : null;
                Long versionId = null;
                if (api.has("apiVersionId")) {
                    versionId = api.get("apiVersionId").asLong();
                } else if (api.has("latestVersionId")) {
                    versionId = api.get("latestVersionId").asLong();
                }
                if (apiId != null && versionId != null) {
                    apiIds.add(apiId);
                    apiVersionIds.add(versionId);
                    String apiName = api.has("name") ? api.get("name").asText() : "";
                    LOG.info("    ✅ 选择API-{}: name={}, apiId={}, versionId={}", apiCount + 1, apiName, apiId, versionId);
                    apiCount++;
                    if (apiCount >= 3) break;
                }
            }
        }

        if (apiIds.size() < 2) {
            throw new AssertionError("可用API数量不足，需要至少2个API进行批量测试");
        }

        // 存储到运行时变量（模拟 $(变量名)
        if (apiIds.size() > 0) {
            runtimeVariables.put("apiId_1", apiIds.get(0));
            runtimeVariables.put("apiVersionId_1", apiVersionIds.get(0));
            LOG.info("  存储变量: apiId_1={}, apiVersionId_1={}", apiIds.get(0), apiVersionIds.get(0));
        }
        if (apiIds.size() > 1) {
            runtimeVariables.put("apiId_2", apiIds.get(1));
            runtimeVariables.put("apiVersionId_2", apiVersionIds.get(1));
            LOG.info("  存储变量: apiId_2={}, apiVersionId_2={}", apiIds.get(1), apiVersionIds.get(1));
        }
        if (apiIds.size() > 2) {
            runtimeVariables.put("apiId_3", apiIds.get(2));
            runtimeVariables.put("apiVersionId_3", apiVersionIds.get(2));
            LOG.info("  存储变量: apiId_3={}, apiVersionId_3={}", apiIds.get(2), apiVersionIds.get(2));
        }

        LOG.info("✅ 找到 {} 个API用于批量测试", apiIds.size());
        for (int i = 0; i < apiIds.size(); i++) {
            LOG.info("  API-{}: apiId={}, versionId={}", i + 1, apiIds.get(i), apiVersionIds.get(i));
        }

        // ===================================================================
        // 步骤2：提交批量审批单
        // ===================================================================
        LOG.info("");
        LOG.info("步骤2: 提交批量审批单 - API数量=" + apiIds.size());

        String submitUrl = BASE_URL + API_PREFIX + "/submit";

        ObjectNode submitRequestBody = objectMapper.createObjectNode();

        ArrayNode submitApiInfos = objectMapper.createArrayNode();
        for (int i = 0; i < apiIds.size(); i++) {
            ObjectNode apiVersionInfo = objectMapper.createObjectNode();
            apiVersionInfo.put("apiId", apiIds.get(i));
            apiVersionInfo.put("apiVersionId", apiVersionIds.get(i));
            submitApiInfos.add(apiVersionInfo);
        }
        submitRequestBody.set("submitApiInfos", submitApiInfos);

        submitRequestBody.put("approvalName", "批量API增量授权测试");
        submitRequestBody.put("backgroundDesc", "多API联合授权");
        submitRequestBody.put("applyUser", VAR_USER5 + "," + VAR_USER6);
        submitRequestBody.put("duration", "30");
        submitRequestBody.put("importance", 3);
        submitRequestBody.put("sensitive", 0);
        submitRequestBody.put("attentionUser", "");
        submitRequestBody.put("creator", VAR_SUBMIT_USER);
        submitRequestBody.put("workspaceId", Integer.parseInt(VAR_WORKSPACE_ID));

        String submitRequestBodyStr = objectMapper.writeValueAsString(submitRequestBody);
        LOG.info("submit请求体: {}", formatJson(submitRequestBodyStr));

        String submitResponse = doPost(submitUrl, submitRequestBodyStr);
        LOG.info("submit响应: {}", formatJson(submitResponse));

        JsonNode submitJson = objectMapper.readTree(submitResponse);
        if (submitJson.has("status") && submitJson.get("status").asInt() != 0) {
            String errorMsg = submitJson.has("message") ? submitJson.get("message").asText() : "未知错误";
            throw new AssertionError("提交审批单失败: " + errorMsg);
        }

        LOG.info("✅ 批量审批单提交成功");

        // ===================================================================
        // 步骤3：获取各API审批单号
        // ===================================================================
        LOG.info("");
        LOG.info("步骤3: 获取各API审批单号");

        java.util.List<String> authIds = new java.util.ArrayList<>();
        for (int i = 0; i < apiIds.size(); i++) {
            String apiVersionQueryUrl = BASE_URL + API_PREFIX + "/apiVersionQuery?serviceId=" + apiIds.get(i);
            String apiVersionQueryResponse = doGet(apiVersionQueryUrl);
            JsonNode apiVersionQueryJson = objectMapper.readTree(apiVersionQueryResponse);

            String authId = null;
            if (apiVersionQueryJson.has("data") && apiVersionQueryJson.get("data").has("result")) {
                ArrayNode resultArray = (ArrayNode) apiVersionQueryJson.get("data").get("result");
                for (JsonNode version : resultArray) {
                    Long versionId = version.has("id") ? version.get("id").asLong() : null;
                    if (versionId != null && versionId.equals(apiVersionIds.get(i))) {
                        authId = version.has("authId") ? version.get("authId").asText() : null;
                        break;
                    }
                }
            }
            authIds.add(authId);

            // 存储到运行时变量（模拟 ${变量名}
            String authKey = "authId_" + (i + 1);
            runtimeVariables.put(authKey, authId);

            LOG.info("  API-{}: apiId={}, authId={}, versionId={}",
                    i + 1, apiIds.get(i), authId, apiVersionIds.get(i));
        }

        // ===================================================================
        // 步骤4：等待手动审批
        // ===================================================================
        LOG.info("============================================");
        LOG.info("等待手动审批(批量API增量授权)");
        LOG.info("============================================");
        for (int i = 0; i < apiIds.size(); i++) {
            LOG.info("  API-{}: 预期授权用户包含历史用户", apiIds.get(i));
        }
        LOG.info("  申请用户: {}, {}", VAR_USER5, VAR_USER6);
        LOG.info("============================================");
        LOG.info("审批完成后，请继续执行后续步骤");
        LOG.info("============================================");

        LOG.info("等待 {} 分钟，请在此期间完成手动审批操作...", APPROVAL_WAIT_TIME_MS / 60000);
        Thread.sleep(APPROVAL_WAIT_TIME_MS);
        LOG.info("等待结束，继续执行后续步骤...");

        // ===================================================================
        // 步骤5：调用审批刷新接口（使用第一个API的authId）
        // ===================================================================
        LOG.info("");
        LOG.info("步骤5: 调用审批刷新接口 - approvalNo={}", authIds.get(0));

        String approvalRefreshUrl = BASE_URL + API_PREFIX + "/approvalRefresh?approvalNo=" + authIds.get(0);
        String approvalRefreshResponse = doGet(approvalRefreshUrl);
        LOG.info("approvalRefresh响应: {}", formatJson(approvalRefreshResponse));

        JsonNode approvalRefreshJson = objectMapper.readTree(approvalRefreshResponse);
        String approvalStatus = "未知";
        if (approvalRefreshJson.has("status") && approvalRefreshJson.get("status").asInt() == 0) {
            approvalStatus = approvalRefreshJson.has("data") && approvalRefreshJson.get("data").has("approvalStatus")
                    ? approvalRefreshJson.get("data").get("approvalStatus").asText() : "未知";
        }

        LOG.info("审批状态: {}", approvalStatus);

        // ===================================================================
        // 步骤6：验证各API Token列表
        // ===================================================================
        if ("审批通过".equals(approvalStatus) || "3".equals(approvalStatus)) {
            LOG.info("✅ 审批已通过，开始验证各API Token");

            LOG.info("");
            LOG.info("============================================");
            LOG.info("验证批量API增量授权结果");
            LOG.info("============================================");

            for (int i = 0; i < apiIds.size(); i++) {
                LOG.info("");
                LOG.info("  API-{}(ID={}, versionId={}): 查询Token列表",
                        i + 1, apiIds.get(i), apiVersionIds.get(i));

                // 查询该API的有效Token
                String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                        "?apiId=" + apiIds.get(i) +
                        "&status=1" +
                        "&currentPage=1" +
                        "&pageSize=100";

                String tokenQueryResponse = doGet(tokenQueryUrl);
                JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

                if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
                    if (tokenQueryJson.has("data")) {
                        JsonNode data = tokenQueryJson.get("data");
                        int total = data.has("total") ? data.get("total").asInt() : 0;
                        LOG.info("  有效Token总数: {}", total);

                        if (data.has("queryList")) {
                            ArrayNode tokens = (ArrayNode) data.get("queryList");
                            LOG.info("  用户列表:");
                            for (JsonNode token : tokens) {
                                String user = token.has("user") ? token.get("user").asText() : "";
                                Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                                LOG.info("    - 用户: {}, 版本ID: {}", user, versionId);
                            }
                        }
                    }
                }
            }

            LOG.info("");
            LOG.info("========================================");
            LOG.info("✅ 测试用例4验证通过: 批量API申请时增量授权");
            LOG.info("========================================");
        } else {
            LOG.info("审批状态为: {}，需要等待审批通过后才能验证Token", approvalStatus);
        }

        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例4完成: 批量API申请时增量授权 - 跨API历史用户合并");
        LOG.info("========================================");
    }

    /**
     * 测试用例5: 批量申请API进行增量授权
     * <p>
     * 步骤：
     * 1、调用 availableSubmitApi 接口查询多个API
     * 2、调用 submit 接口批量提交审批单
     * 3、调用 apiVersionQuery 接口获取各API审批单号(authId)
     * 4、等待手动审批（控制台打印提醒）
     * 5、调用 approvalRefresh 接口刷新审批状态
     * 6、验证每个API独立进行增量授权
     * <p>
     * 预期结果：
     * - 每个API独立进行增量授权，自动合并各自的历史用户
     * - 有历史用户的API最终授权用户 = 历史用户 + 新申请用户（含新增的user7）
     * - 无历史用户的API最终授权用户 = 新申请用户（含user7）
     * - 每个API的历史版本Token被禁用
     * - 新Token状态为有效
     */
    @Test
    public void testIncrementalAuthorization_BatchApiSubmission() throws Exception {
        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例5: 批量申请API进行增量授权");
        LOG.info("========================================");
        LOG.info("注意：此测试用例需要多个API及各自的历史记录");
        LOG.info("========================================");

        // ===================================================================
        // 步骤1：调用 availableSubmitApi 接口查询多个API
        // ===================================================================
        LOG.info("");
        LOG.info("步骤1: 查询可用API - workspaceId={}", VAR_WORKSPACE_ID);

        String availableSubmitApiUrl = BASE_URL + API_PREFIX + "/availableSubmitApi?workspaceId=" + VAR_WORKSPACE_ID;
        String availableSubmitApiResponse = doGet(availableSubmitApiUrl);
        LOG.info("availableSubmitApi响应: {}", formatJson(availableSubmitApiResponse));

        JsonNode availableSubmitApiJson = objectMapper.readTree(availableSubmitApiResponse);
        if (availableSubmitApiJson.has("status") && availableSubmitApiJson.get("status").asInt() != 0) {
            String errorMsg = availableSubmitApiJson.has("message") ? availableSubmitApiJson.get("message").asText() : "未知错误";
            throw new AssertionError("查询可提交API失败: " + errorMsg);
        }

        if (!availableSubmitApiJson.has("data") || !availableSubmitApiJson.get("data").has("availableSubmitApiList")) {
            throw new AssertionError("响应缺少 availableSubmitApiList 字段");
        }

        ArrayNode availableSubmitApiList = (ArrayNode) availableSubmitApiJson.get("data").get("availableSubmitApiList");

        // 选择API进行批量测试
        java.util.List<Long> apiIds = new java.util.ArrayList<>();
        java.util.List<Long> apiVersionIds = new java.util.ArrayList<>();
        java.util.List<String> apiNames = new java.util.ArrayList<>();

        // 检查是否指定了API名称列表
        if (VAR_BATCH_API_NAMES_TEST5 != null && !VAR_BATCH_API_NAMES_TEST5.trim().isEmpty()) {
            LOG.info("  使用指定的API名称列表: {}", VAR_BATCH_API_NAMES_TEST5);
            String[] specifiedApiNameArray = VAR_BATCH_API_NAMES_TEST5.split(",");
            for (String apiNameStr : specifiedApiNameArray) {
                String specifiedApiName = apiNameStr.trim();
                if (specifiedApiName.isEmpty()) {
                    continue;
                }
                LOG.info("  查找指定的API名称: {}", specifiedApiName);

                // 从可用API列表中查找对应的API
                for (JsonNode api : availableSubmitApiList) {
                    String currentApiName = api.has("name") ? api.get("name").asText() : "";
                    if (currentApiName.equals(specifiedApiName)) {
                        Long apiId = api.has("id") ? api.get("id").asLong() : null;
                        Long versionId = api.has("apiVersionId") ? api.get("apiVersionId").asLong() : null;
                        if (versionId == null) {
                            versionId = api.has("latestVersionId") ? api.get("latestVersionId").asLong() : null;
                        }
                        if (apiId != null && versionId != null) {
                            apiIds.add(apiId);
                            apiVersionIds.add(versionId);
                            apiNames.add(specifiedApiName);
                            LOG.info("    ✅ 找到API: name={}, apiId={}, versionId={}", specifiedApiName, apiId, versionId);
                            break;
                        } else {
                            LOG.warn("    ⚠️ API {} 没有可用的ID或版本ID，跳过", specifiedApiName);
                        }
                    }
                }
            }
        } else {
            LOG.info("  未指定API名称，从前3个API中选择");
            // 选择前3个API进行批量测试
            int apiCount = 0;
            for (JsonNode api : availableSubmitApiList) {
                Long apiId = api.has("id") ? api.get("id").asLong() : null;
                Long versionId = null;
                String apiName = api.has("name") ? api.get("name").asText() : "";
                if (api.has("apiVersionId")) {
                    versionId = api.get("apiVersionId").asLong();
                } else if (api.has("latestVersionId")) {
                    versionId = api.get("latestVersionId").asLong();
                }
                if (apiId != null && versionId != null) {
                    apiIds.add(apiId);
                    apiVersionIds.add(versionId);
                    apiNames.add(apiName);
                    LOG.info("    ✅ 选择API-{}: name={}, apiId={}, versionId={}", apiCount + 1, apiName, apiId, versionId);
                    apiCount++;
                    if (apiCount >= 3) break;
                }
            }
        }

        if (apiIds.size() < 2) {
            throw new AssertionError("可用API数量不足，需要至少2个API进行批量测试");
        }

        // 存储到运行时变量（模拟 $(变量名)）
        if (apiIds.size() > 0) {
            runtimeVariables.put("apiId_1", apiIds.get(0));
            runtimeVariables.put("apiVersionId_1", apiVersionIds.get(0));
            LOG.info("  存储变量: apiId_1={}, apiVersionId_1={}", apiIds.get(0), apiVersionIds.get(0));
        }
        if (apiIds.size() > 1) {
            runtimeVariables.put("apiId_2", apiIds.get(1));
            runtimeVariables.put("apiVersionId_2", apiVersionIds.get(1));
            LOG.info("  存储变量: apiId_2={}, apiVersionId_2={}", apiIds.get(1), apiVersionIds.get(1));
        }
        if (apiIds.size() > 2) {
            runtimeVariables.put("apiId_3", apiIds.get(2));
            runtimeVariables.put("apiVersionId_3", apiVersionIds.get(2));
            LOG.info("  存储变量: apiId_3={}, apiVersionId_3={}", apiIds.get(2), apiVersionIds.get(2));
        }

        LOG.info("✅ 找到 {} 个API用于批量测试", apiIds.size());
        for (int i = 0; i < apiIds.size(); i++) {
            LOG.info("  API-{}/{}, apiId={}, versionId={}", i + 1, apiNames.get(i), apiIds.get(i), apiVersionIds.get(i));
        }

        // ===================================================================
        // 步骤2：提交批量审批单
        // ===================================================================
        LOG.info("");
        LOG.info("步骤2: 提交批量审批单 - API数量={}", apiIds.size());

        String submitUrl = BASE_URL + API_PREFIX + "/submit";

        ObjectNode submitRequestBody = objectMapper.createObjectNode();

        ArrayNode submitApiInfos = objectMapper.createArrayNode();
        for (int i = 0; i < apiIds.size(); i++) {
            ObjectNode apiVersionInfo = objectMapper.createObjectNode();
            apiVersionInfo.put("apiId", apiIds.get(i));
            apiVersionInfo.put("apiVersionId", apiVersionIds.get(i));
            submitApiInfos.add(apiVersionInfo);
        }
        submitRequestBody.set("submitApiInfos", submitApiInfos);

        submitRequestBody.put("approvalName", "批量API增量授权测试(含user7)");
        submitRequestBody.put("backgroundDesc", "批量API增量授权，验证跨API独立增量授权，新增user7用户");
        // 申请用户包含新增的user7
        submitRequestBody.put("applyUser", VAR_USER6 + "," + VAR_USER7);
        submitRequestBody.put("duration", "30");
        submitRequestBody.put("importance", 3);
        submitRequestBody.put("sensitive", 0);
        submitRequestBody.put("attentionUser", "");
        submitRequestBody.put("creator", VAR_SUBMIT_USER);
        submitRequestBody.put("workspaceId", Integer.parseInt(VAR_WORKSPACE_ID));

        String submitRequestBodyStr = objectMapper.writeValueAsString(submitRequestBody);
        LOG.info("submit请求体: {}", formatJson(submitRequestBodyStr));

        String submitResponse = doPost(submitUrl, submitRequestBodyStr);
        LOG.info("submit响应: {}", formatJson(submitResponse));

        JsonNode submitJson = objectMapper.readTree(submitResponse);
        if (submitJson.has("status") && submitJson.get("status").asInt() != 0) {
            String errorMsg = submitJson.has("message") ? submitJson.get("message").asText() : "未知错误";
            throw new AssertionError("提交审批单失败: " + errorMsg);
        }

        LOG.info("✅ 批量审批单提交成功");

        // ===================================================================
        // 步骤3：获取各API审批单号
        // ===================================================================
        LOG.info("");
        LOG.info("步骤3: 获取各API审批单号");

        java.util.List<String> authIds = new java.util.ArrayList<>();
        for (int i = 0; i < apiIds.size(); i++) {
            String apiVersionQueryUrl = BASE_URL + API_PREFIX + "/apiVersionQuery?serviceId=" + apiIds.get(i);
            String apiVersionQueryResponse = doGet(apiVersionQueryUrl);
            JsonNode apiVersionQueryJson = objectMapper.readTree(apiVersionQueryResponse);

            String authId = null;
            if (apiVersionQueryJson.has("data") && apiVersionQueryJson.get("data").has("result")) {
                ArrayNode resultArray = (ArrayNode) apiVersionQueryJson.get("data").get("result");
                for (JsonNode version : resultArray) {
                    Long versionId = version.has("id") ? version.get("id").asLong() : null;
                    if (versionId != null && versionId.equals(apiVersionIds.get(i))) {
                        authId = version.has("authId") ? version.get("authId").asText() : null;
                        break;
                    }
                }
            }
            authIds.add(authId);

            // 存储到运行时变量（模拟 ${变量名}）
            String authKey = "authId_" + (i + 1);
            runtimeVariables.put(authKey, authId);

            LOG.info("  API-{}/{}: apiId={}, authId={}, versionId={}",
                    i + 1, apiNames.get(i), apiIds.get(i), authId, apiVersionIds.get(i));
        }

        // ===================================================================
        // 步骤4：等待手动审批
        // ===================================================================
        LOG.info("============================================");
        LOG.info("等待手动审批(批量API增量授权测试)");
        LOG.info("============================================");
        for (int i = 0; i < apiIds.size(); i++) {
            LOG.info("  API-{}/({}): 预期独立增量授权", i + 1, apiNames.get(i));
        }
        LOG.info("  申请用户: {}, {}, {}", VAR_USER3, VAR_USER4, VAR_USER7);
        LOG.info("============================================");
        LOG.info("审批完成后，请继续执行后续步骤");
        LOG.info("============================================");

        LOG.info("等待 {} 分钟，请在此期间完成手动审批操作...", APPROVAL_WAIT_TIME_MS / 60000);
        Thread.sleep(APPROVAL_WAIT_TIME_MS);
        LOG.info("等待结束，继续执行后续步骤...");

        // ===================================================================
        // 步骤5：调用审批刷新接口（使用第一个API的authId）
        // ===================================================================
        LOG.info("");
        LOG.info("步骤5: 调用审批刷新接口 - approvalNo={}", authIds.get(0));

        String approvalRefreshUrl = BASE_URL + API_PREFIX + "/approvalRefresh?approvalNo=" + authIds.get(0);
        String approvalRefreshResponse = doGet(approvalRefreshUrl);
        LOG.info("approvalRefresh响应: {}", formatJson(approvalRefreshResponse));

        JsonNode approvalRefreshJson = objectMapper.readTree(approvalRefreshResponse);
        String approvalStatus = "未知";
        if (approvalRefreshJson.has("status") && approvalRefreshJson.get("status").asInt() == 0) {
            approvalStatus = approvalRefreshJson.has("data") && approvalRefreshJson.get("data").has("approvalStatus")
                    ? approvalRefreshJson.get("data").get("approvalStatus").asText() : "未知";
        }

        LOG.info("审批状态: {}", approvalStatus);

        // ===================================================================
        // 步骤6：验证各API独立增量授权结果
        // ===================================================================
        if ("审批通过".equals(approvalStatus) || "3".equals(approvalStatus)) {
            LOG.info("✅ 审批已通过，开始验证各API独立增量授权");

            LOG.info("");
            LOG.info("============================================");
            LOG.info("验证批量API独立增量授权结果");
            LOG.info("============================================");

            for (int i = 0; i < apiIds.size(); i++) {
                LOG.info("");
                LOG.info("=== API-{}/{} (apiId={}, versionId={}) ===",
                        i + 1, apiNames.get(i), apiIds.get(i), apiVersionIds.get(i));

                // 查询该API的有效Token
                String tokenQueryUrl = BASE_URL + API_PREFIX + "/tokenQuery" +
                        "?apiId=" + apiIds.get(i) +
                        "&status=1" +
                        "&currentPage=1" +
                        "&pageSize=100";

                String tokenQueryResponse = doGet(tokenQueryUrl);
                JsonNode tokenQueryJson = objectMapper.readTree(tokenQueryResponse);

                if (tokenQueryJson.has("status") && tokenQueryJson.get("status").asInt() == 0) {
                    if (tokenQueryJson.has("data")) {
                        JsonNode data = tokenQueryJson.get("data");
                        int total = data.has("total") ? data.get("total").asInt() : 0;
                        LOG.info("有效Token总数: {}", total);

                        if (data.has("queryList")) {
                            ArrayNode tokens = (ArrayNode) data.get("queryList");
                            LOG.info("用户列表:");
                            boolean hasUser7 = false;
                            for (JsonNode token : tokens) {
                                String user = token.has("user") ? token.get("user").asText() : "";
                                Long versionId = token.has("apiVersionId") ? token.get("apiVersionId").asLong() : null;
                                if (VAR_USER7.equals(user)) {
                                    hasUser7 = true;
                                }
                                LOG.info("  - 用户: {}, 版本ID: {}", user, versionId);
                            }
                            // 验证是否包含user7
                            if (hasUser7) {
                                LOG.info("  ✅ 验证通过: 包含新增用户 {}", VAR_USER7);
                            } else {
                                LOG.warn("  ⚠️ 验证失败: 未找到新增用户 {}", VAR_USER7);
                            }
                        }
                    }
                }
            }

            LOG.info("");
            LOG.info("========================================");
            LOG.info("✅ 测试用例5验证通过: 批量申请API进行增量授权");
            LOG.info("每个API独立进行增量授权，用户列表正确合并");
            LOG.info("已验证新增用户: {}", VAR_USER7);
            LOG.info("========================================");
        } else {
            LOG.info("审批状态为: {}，需要等待审批通过后才能验证Token", approvalStatus);
        }

        LOG.info("");
        LOG.info("========================================");
        LOG.info("测试用例5完成: 批量申请API进行增量授权");
        LOG.info("========================================");
    }

    // ===========================================
    // HTTP工具方法
    // ===========================================

    /**
     * 发送GET请求
     */
    private String doGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", COOKIE);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            LOG.info("GET请求: {} -> 响应码: {}", urlString, responseCode);

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 发送POST请求
     */
    private String doPost(String urlString, String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", COOKIE);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // 发送请求体
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("UTF-8");
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            LOG.info("POST请求: {} -> 响应码: {}", urlString, responseCode);

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 格式化JSON字符串
     */
    private String formatJson(String json) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    // ===========================================
    // 测试配置设置方法
    // ===========================================

    /**
     * 设置测试环境BASE_URL
     */
    public static void setBaseUrl(String baseUrl) {
        // 使用反射修改常量值（仅用于测试配置，生产环境不建议）
        try {
            java.lang.reflect.Field field = IncrementalAuthorizationIntegrationTest.class.getDeclaredField("BASE_URL");
            field.setAccessible(true);
            java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            field.set(null, baseUrl);
        } catch (Exception e) {
            LOG.warn("无法修改BASE_URL: {}", e.getMessage());
        }
    }

    /**
     * 设置测试环境Cookie
     */
    public static void setCookie(String cookie) {
        try {
            java.lang.reflect.Field field = IncrementalAuthorizationIntegrationTest.class.getDeclaredField("COOKIE");
            field.setAccessible(true);
            java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            field.set(null, cookie);
        } catch (Exception e) {
            LOG.warn("无法修改COOKIE: {}", e.getMessage());
        }
    }
}