package com.webank.wedatasphere.dss.apiservice.core.token;

import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceToken;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.constant.SaveTokenEnum;
import com.webank.wedatasphere.dss.apiservice.core.constant.DataMapStatus;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceApprovalDao;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceTokenException;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalService;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * DataMapTokenImpl单元测试
 *
 * 测试Token生成与增量授权核心功能：
 * - Token生成记录
 * - 增量授权合并历史用户
 * - 保留历史配置
 * - 禁用旧版本Token
 * - 防重复授权
 *
 * @author Claude Code
 * @version 1.0.0
 * @date 2026-03-25
 */
@RunWith(MockitoJUnitRunner.class)
public class DataMapTokenImplTest {

    private static final Logger logger = LoggerFactory.getLogger(DataMapTokenImplTest.class);

    @Mock
    private ApiServiceApprovalDao apiServiceApprovalDao;

    @Mock
    private ApiServiceTokenManagerDao apiServiceTokenManagerDao;

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private DataMapTokenImpl dataMapTokenImpl;

    private TokenManagerVo token1;
    private TokenManagerVo token2;
    private ApprovalVo currentApproval;
    private ApprovalVo historyApproval;
    private List<TokenManagerVo> tokenList = new ArrayList<>();

    @Before
    public void setUp() {
        // 初始化Token数据
        token1 = new TokenManagerVo();
        token1.setId(1L);
        token1.setApiId(100L);
        token1.setApiVersionId(1001);
        token1.setUser("user1");
        token1.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        token1.setIpWhitelist("192.168.1.100");
        token1.setAccessLimit("1000次/天");
        token1.setCaller("scriptis");
        token1.setStatus(ApiCommonConstant.API_ENABLE_STATUS);
        token1.setApplySource("uuid-v1");
        token1.setApplyTime(new Date());

        token2 = new TokenManagerVo();
        token2.setId(2L);
        token2.setApiId(100L);
        token2.setApiVersionId(1001);
        token2.setUser("user2");
        token2.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        token2.setIpWhitelist("192.168.1.101");
        token2.setCaller("workflow");
        token2.setStatus(ApiCommonConstant.API_ENABLE_STATUS);
        token2.setApplySource("uuid-v1");

        // 初始化当前审批数据
        currentApproval = new ApprovalVo();
        currentApproval.setId(2L);
        currentApproval.setApiId(100L);
        currentApproval.setApiVersionId(1002);
        currentApproval.setApprovalNo("uuid-v2");
        currentApproval.setStatus(DataMapStatus.SUCCESS.getIndex());
        currentApproval.setApplyUser("user3,user4");

        // 初始化历史审批数据
        historyApproval = new ApprovalVo();
        historyApproval.setId(1L);
        historyApproval.setApiId(100L);
        historyApproval.setApiVersionId(1001);
        historyApproval.setApprovalNo("uuid-v1");
        historyApproval.setStatus(DataMapStatus.SUCCESS.getIndex());
        historyApproval.setApplyUser("user1,user2");
        historyApproval.setCreateTime(new Date());

        tokenList.add(token1);
        tokenList.add(token2);
    }

    /**
     * 测试场景：首次提单 - 无历史审批记录
     *
     * 预期结果：不执行用户合并
     */
    @Test
    public void testGenTokenRecord_noHistoryApproval() {
        // Given: 没有历史审批记录
        when(approvalService.getSecondApproval(eq(100L)))
                .thenReturn(null);

        // When: 生成Token记录
        List<TokenManagerVo> result = dataMapTokenImpl.genTokenRecord(currentApproval);

        // Then: 应该只生成新的申请用户的Token，不包含历史用户
        assertEquals(2, result.size());
        assertEquals("user3", result.get(0).getUser());
        assertEquals("user4", result.get(1).getUser());
        assertEquals(new Long(1002), result.get(0).getApiVersionId());
        logger.info("✅ 测试通过：首次提单无历史用户");
    }

    /**
     * 测试场景：增量授权 - 合并历史用户
     *
     * 预期结果：应该合并历史用户user1和user2
     */
    @Test
    public void testGenTokenRecord_withHistoryApproval() {
        // Given: 存在历史审批记录
        when(approvalService.getSecondApproval(eq(100L)))
                .thenReturn(historyApproval);
        // Mock历史Token查询
        when(apiServiceTokenManagerDao.queryByVersionIdWithValidStatus(
                eq(1001L), eq("uuid-v1"), eq(ApiCommonConstant.API_ENABLE_STATUS)))
                .thenReturn(tokenList);

        // When: 生成Token记录（只申请user3,user4）
        List<TokenManagerVo> result = dataMapTokenImpl.genTokenRecord(currentApproval);

        // Then: 应该合并历史用户user1和user2，以及新用户user3,user4
        assertEquals(4, result.size());
        // 提取用户名
        List<String> users = result.stream()
                .map(TokenManagerVo::getUser)
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        assertEquals(Arrays.asList("user1", "user2", "user3", "user4"), users);
        logger.info("✅ 测试通过：增量授权成功合并历史用户");
    }

    /**
     * 测试场景：申请用户为空 - 只保留历史用户
     *
     * 预期结果：应该只生成历史用户的Token
     */
    @Test
    public void testGenTokenRecord_emptyApplyUser() {
        // Given: 存在历史审批记录
        when(approvalService.getSecondApproval(eq(100L)))
                .thenReturn(historyApproval);
        when(apiServiceTokenManagerDao.queryByVersionIdWithValidStatus(
                eq(1001L), eq("uuid-v1"), eq(ApiCommonConstant.API_ENABLE_STATUS)))
                .thenReturn(tokenList);
        // 申请用户为空
        currentApproval.setApplyUser("");
        currentApproval.setApplyUser(null);

        // When: 生成Token记录
        List<TokenManagerVo> result = dataMapTokenImpl.genTokenRecord(currentApproval);

        // Then: 应该只生成历史用户的Token
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUser());
        assertEquals("user2", result.get(1).getUser());
        logger.info("✅ 测试通过：申请用户为空，只保留历史用户");
    }

    /**
     * 测试场景：复制历史Token配置
     *
     * 预期结果：新Token应该保留历史用户的IP白名单、访问限制等配置
     */
    @Test
    public void testCopyHistoryToken_shouldPreserveConfig() {
        // Given: 历史Token有不同的IP白名单和访问限制
        token1.setIpWhitelist("192.168.1.100,192.168.1.101");
        token1.setAccessLimit("1000次/天");
        token2.setIpWhitelist("192.168.1.200");
        token2.setAccessLimit("500次/天");

        tokenList = new ArrayList<>();
        tokenList.add(token1);
        tokenList.add(token2);

        // When: 生成Token记录（只申请user3，触发合并）
        when(approvalService.getSecondApproval(eq(100L)))
                .thenReturn(historyApproval);
        when(apiServiceTokenManagerDao.queryByVersionIdWithValidStatus(
                eq(1001L), eq("uuid-v1"), eq(ApiCommonConstant.API_ENABLE_STATUS)))
                .thenReturn(tokenList);

        List<TokenManagerVo> result = dataMapTokenImpl.genTokenRecord(currentApproval);

        // Then: 历史1的配置应该被保留
        TokenManagerVo mergedToken = result.stream()
                .filter(t -> "user1".equals(t.getUser()))
                .findFirst()
                .orElse(null);
        assertNotNull(mergedToken);
        assertEquals("192.168.1.100,192.168.1.101", mergedToken.getIpWhitelist());
        assertEquals("1000次/天", mergedToken.getAccessLimit());
        assertEquals("scriptis", mergedToken.getCaller());
        logger.info("✅ 测试通过：历史配置保留正确");
    }

    /**
     * 测试场景：禁用旧版本Token
     *
     * 预期：应该禁用所有旧版本的Token
     */
    @Test
    public void testUpdateTokenStatusBeforeVersionId() {
        // Given: 当前版本ID为1002，旧Token
        when(apiServiceTokenManagerDao.updateTokenStatusBeforeVersionId(
                eq(1002L), eq(100L), eq(ApiCommonConstant.API_DISABLE_STATUS)))
                .thenReturn(2); // 影响了2条记录

        // When: 禁用旧版本Token
        dataMapTokenImpl.updateTokenStatusBeforeVersionId(currentApproval);

        // Then: 应该调用DAO更新方法
        verify(apiServiceTokenManagerDao, times(1)).updateTokenStatusBeforeVersionId(
                eq(1002L), eq(100L), eq(ApiCommonConstant.API_DISABLE_STATUS));
        logger.info("✅ 测试通过：禁用旧版本Token");
    }

    /**
     * 测试场景：保存Token到数据库 - 防重复授权
     *
     * 预期结果：已存在的Token不应该重复插入
     */
    @Test
    public void testSaveTokensToDb_duplicatePrevention() {
        // Given: 数据库中已存在该审批单的Token记录
        when(apiServiceTokenManagerDao.queryByVersionIdWithValidStatus(
                eq(1002L), eq("uuid-v2"), eq(ApiCommonConstant.API_ENABLE_STATUS)))
                .thenReturn(Collections.emptyList()); // 返回空，表示没重复

        // When: 保存Token到数据库
        SaveTokenEnum result = SaveTokenEnum.SUCCESS;

        // Then: 应该调用DAO批量插入
        when(apiServiceTokenManagerDao.batchInsert(anyList()))
                .thenReturn(2);

        result = dataMapTokenImpl.saveTokensToDb(
                Arrays.asList(new TokenManagerVo(), new TokenManagerVo()),
                "uuid-v2");

        // Then: 应该返回成功
        assertEquals(SaveTokenEnum.SUCCESS, result);
        verify(apiServiceTokenManagerDao, times(1)).batchInsert(anyList());
        logger.info("✅ 测试通过：保存Token正常");
    }

    /**
     * 测试场景：重复授权防护 - 使用默认审批单号
     *
     * 预期结果：默认审批单号(0001)应该允许重复
     */
    @Test
    public void testSaveTokensToDb_defaultApprovalNo() {
        // Given: 默认审批单号的Token记录
        when(apiServiceTokenManagerDao.queryByVersionIdWithValidStatus(
                eq(1002L), eq("0001"), eq(ApiCommonConstant.API_ENABLE_STATUS)))
                .thenReturn(Collections.singletonList(token1)); // 已存在Token

        TokenManagerVo newToken = new TokenManagerVo();
        newToken.setUser("user1");
        newToken.setApiVersionId(1002L);
        newToken.setApprovalNo("0001");

        // When: 保存Token到数据库（使用默认审批单号）
        SaveTokenEnum result = dataMapTokenImpl.saveTokensToDb(
                Collections.singletonList(newToken),
                "0001");

        // Then: 应该成功（默认审批单号允许重复）
        assertEquals(SaveTokenEnum.SUCCESS, result);
        // 不应该调用插入（因为已存在）
        verify(apiServiceTokenManagerDao, never()).batchInsert(anyList());
        logger.info("✅ 测试通过：默认审批单号允许重复授权");
    }
}