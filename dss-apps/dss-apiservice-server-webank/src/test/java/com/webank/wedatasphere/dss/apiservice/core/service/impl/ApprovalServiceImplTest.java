package com.webank.wedatasphere.dss.apiservice.core.service.impl;

import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceApprovalDao;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.constant.DataMapStatus;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalServiceImpl单元测试
 *
 * 测试审批服务核心功能，包括：
 * - 获取上一个通过的审批单（增量授权）
 * - 按API ID和状态查询审批记录
 * - 审批状态刷新
 */
@RunWith(MockitoJUnitRunner.class)
public class ApprovalServiceImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(ApprovalServiceImplTest.class);

    @Mock
    private ApiServiceApprovalDao apiServiceApprovalDao;

    @Mock
    private ApiServiceVersionDao apiServiceVersionDao;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private ApprovalVo approvalVo1;
    private ApprovalVo approvalVo2;
    private List<ApprovalVo> approvalVoList;

    @Before
    public void setUp() {
        // 初始化测试数据
        approvalVo1 = new ApprovalVo();
        approvalVo1.setId(1L);
        approvalVo1.setApiId(100L);
        approvalVo1.setApiVersionId(1001);
        approvalVo1.setApprovalName("测试审批单V1");
        approvalVo1.setApprovalNo("uuid-v1");
        approvalVo1.setStatus(DataMapStatus.SUCCESS.getIndex());
        approvalVo1.setCreateTime(new java.util.Date());

        approvalVo2 = new ApprovalVo();
        approvalVo2.setId(2L);
        approvalVo2.setApiId(100L);
        approvalVo2.setApiVersionId(1002);
        approvalVo2.setApprovalName("测试审批单V2");
        approvalVo2.setApprovalNo("uuid-v2");
        approvalVo2.setStatus(DataMapStatus.APPROVING.getIndex());
        approvalVo2.setCreateTime(new java.util.Date());

        approvalVoList = Arrays.asList(approvalVo1, approvalVo2);
    }

    /**
     * 测试场景：首次提单（无历史审批记录）
     *
     * 预期结果：getSecondApproval应该返回null
     */
    @Test
    public void testGetSecondApproval_noHistory() {
        // Given: API没有历史审批记录
        when(apiServiceApprovalDao.queryByApiIdAndStatus(
                eq(100L), eq(DataMapStatus.SUCCESS.getIndex())))
                .thenReturn(Collections.emptyList());

        // When: 获取上一个通过的审批单
        ApprovalVo result = approvalService.getSecondApproval(100L);

        // Then: 应该返回null
        assertNull(result);
        LOG.info("✅ 测试通过：无历史审批记录，返回null");
    }

    /**
     * 测试场景：只有一条审批通过的记录
     *
     * 预期结果：getSecondApproval应该返回null
     */
    @Test
    public void testGetSecondApproval_onlyOneSuccess() {
        // Given: API只有1条审批通过的记录
        when(apiServiceApprovalDao.queryByApiIdAndStatus(
                eq(100L), eq(DataMapStatus.SUCCESS.getIndex())))
                .thenReturn(Arrays.asList(approvalVo1));

        // When: 获取上一个通过的审批单
        ApprovalVo result = approvalService.getSecondApproval(100L);

        // Then: 应该返回null（只有1条记录）
        assertNull(result);
        LOG.info("✅ 测试通过：只有1条审批通过的记录，返回null");
    }

    /**
     * 测试场景：有2条审批通过的记录
     *
     * 预期结果：getSecondApproval应该返回第一条旧的审批单
     */
    @Test
    public void testGetSecondApproval_twoSuccess() {
        // Given: API有2条审批通过的记录（按创建时间排序后）
        approvalVo1.setCreateTime(new java.util.Date(1000000)); // 较早的时间
        approvalVo2.setCreateTime(new java.util.Date(2000000)); // 较新的时间

        when(apiServiceApprovalDao.queryByApiIdAndStatus(
                eq(100L), eq(DataMapStatus.SUCCESS.getIndex())))
                .thenReturn(Arrays.asList(approvalVo1, approvalVo2)));

        // When: 获取上一个通过的审批单
        ApprovalVo result = approvalService.getSecondApproval(100L);

        // Then: 应该返回第一创建时间较早的审批单
        assertNotNull(result);
        assertEquals(new Long(1L), result.getId());
        assertEquals("uuid-v1", result.getApprovalNo());
        LOG.info("✅ 测试通过：返回第一条审批单");
    }

    /**
     * 测试场景：按API ID和状态查询审批记录
     *
     * 预期结果：应该返回对应的审批记录列表
     */
    @Test
    public void testQueryByApiIdAndStatus() {
        // Given: 存在审批通过的记录
        when(apiServiceApprovalDao.queryByApiIdAndStatus(
                eq(100L), eq(DataMapStatus.SUCCESS.getIndex())))
                .thenReturn(Collections.singletonList(approvalVo2));

        // When: 按API ID和状态查询审批记录
        List<ApprovalVo> result = approvalService.queryByApiIdAndStatus(100L, DataMapStatus.SUCCESS.getIndex());

        // Then: 应该返回审批通过的审批单列表
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new Long(2L), result.get(0).getId());
        assertEquals(DataMapStatus.SUCCESS.getIndex(), result.get(0).getStatus());
    }

    /**
     * 测试场景：查询结果为空
     *
     * 预期结果：应该返回空列表
     */
    @Test
    public void testQueryByApiIdAndStatus_empty() {
        // Given: 没有对应的审批记录
        when(apiServiceApprovalDao.queryByApiIdAndStatus(
                eq(999L), eq(DataMapStatus.SUCCESS.getIndex())))
                .thenReturn(Collections.emptyList());

        // When: 按API ID和状态查询审批记录
        List<ApprovalVo> result = approvalService.queryByApiIdAndStatus(999L, DataMapStatus.SUCCESS.getIndex());

        // Then: 应该返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试场景：审批状态查询异常
     *
     * 预期结果：应该抛出异常
     */
    @Test(expected = RuntimeException.class)
    public void testQueryByApiIdAndStatus_exception() {
        // Given: 查询时抛出异常
        when(apiServiceApprovalDao.queryByApiIdAndStatus(any(), anyInt()))
                .thenThrow(new RuntimeException("Database connection error"));

        // When: 按API ID和状态查询审批记录
        approvalService.queryByApiIdAndStatus(100L, DataMapStatus.SUCCESS.getIndex());

        // Then: 应该抛出异常（由@Test注解声明）
    }
}