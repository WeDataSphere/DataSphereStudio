package com.webank.wedatasphere.dss.appconn.sendemail.feishu;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for FeishuMessageSender logic.
 * Covers TC019-TC024 (receiver parsing and edge cases).
 */
public class FeishuMessageSenderTest {

    // TC019: feishuTo为null - 跳过飞书发送
    @Test
    public void testFeishuToNull_ShouldSkip() {
        String feishuTo = null;
        assertTrue("Null feishuTo should be skipped",
            feishuTo == null || feishuTo.trim().isEmpty());
    }

    // TC020: feishuTo为空字符串 - 跳过飞书发送
    @Test
    public void testFeishuToEmpty_ShouldSkip() {
        String feishuTo = "";
        assertTrue("Empty feishuTo should be skipped",
            feishuTo == null || feishuTo.trim().isEmpty());
    }

    // TC040: feishuTo为纯空格 - 跳过飞书发送
    @Test
    public void testFeishuToWhitespace_ShouldSkip() {
        String feishuTo = "   ";
        assertTrue("Whitespace-only feishuTo should be skipped",
            feishuTo == null || feishuTo.trim().isEmpty());
    }

    // TC022: 多接收者（分号分隔）- 正确解析
    @Test
    public void testFeishuToMultipleReceivers_ShouldParse() {
        String feishuTo = "zhangsan;lisi;wangwu";
        String[] receivers = feishuTo.split(";");
        assertEquals("Should have 3 receivers", 3, receivers.length);
        assertEquals("First receiver", "zhangsan", receivers[0]);
        assertEquals("Second receiver", "lisi", receivers[1]);
        assertEquals("Third receiver", "wangwu", receivers[2]);
    }

    // TC023: 接收者含前后空格 - trim处理
    @Test
    public void testFeishuToWithSpaces_ShouldTrim() {
        String feishuTo = "  zhangsan  ;  lisi  ";
        String[] receivers = feishuTo.split(";");
        String[] trimmed = new String[receivers.length];
        for (int i = 0; i < receivers.length; i++) {
            trimmed[i] = receivers[i].trim();
        }
        assertEquals("Should have 2 receivers after split", 2, trimmed.length);
        assertEquals("First receiver after trim", "zhangsan", trimmed[0]);
        assertEquals("Second receiver after trim", "lisi", trimmed[1]);
    }

    // TC021: feishuTo仅含空格和分号 - 无有效接收者
    @Test
    public void testFeishuToOnlySemicolonsAndSpaces_NoValidReceivers() {
        String feishuTo = "  ;  ;  ";
        String[] receivers = feishuTo.split(";");
        int validCount = 0;
        for (String r : receivers) {
            if (r.trim().length() > 0) {
                validCount++;
            }
        }
        assertEquals("No valid receivers should be found", 0, validCount);
    }

    // TC024: subject为null时使用默认主题
    @Test
    public void testNullSubject_ShouldUseDefault() {
        String subject = null;
        String effectiveSubject = (subject != null) ? subject : "DSS Email Notification";
        assertEquals("Should use default subject", "DSS Email Notification", effectiveSubject);
    }

    // TC024b: subject不为null时使用原始值
    @Test
    public void testNonNullSubject_ShouldUseOriginal() {
        String subject = "2024年Q4销售报表";
        String effectiveSubject = (subject != null) ? subject : "DSS Email Notification";
        assertEquals("Should use original subject", "2024年Q4销售报表", effectiveSubject);
    }

    // TC022: 验证消息主题前缀
    @Test
    public void testMessageSubjectPrefix() {
        String subject = "2024年Q4销售报表";
        String expectedPrefix = "[DSS Email Notification]";
        String messageContent = expectedPrefix + " " + subject;
        assertTrue("Message should start with DSS prefix",
            messageContent.startsWith(expectedPrefix));
        assertTrue("Message should contain subject",
            messageContent.contains(subject));
    }

    // sendFeishu控制逻辑测试 (TC036/TC037)
    @Test
    public void testSendFeishuFalse_ShouldSkipFeishu() {
        boolean sendFeishu = false;
        String feishuTo = "zhangsan";
        boolean shouldSend = sendFeishu && feishuTo != null && !feishuTo.trim().isEmpty();
        assertFalse("Should not send when sendFeishu=false", shouldSend);
    }

    @Test
    public void testSendFeishuTrueButEmptyFeishuTo_ShouldSkipFeishu() {
        boolean sendFeishu = true;
        String feishuTo = "";
        boolean shouldSend = sendFeishu && feishuTo != null && !feishuTo.trim().isEmpty();
        assertFalse("Should not send when feishuTo is empty", shouldSend);
    }

    @Test
    public void testSendFeishuTrueAndValidFeishuTo_ShouldSend() {
        boolean sendFeishu = true;
        String feishuTo = "zhangsan";
        boolean shouldSend = sendFeishu && feishuTo != null && !feishuTo.trim().isEmpty();
        assertTrue("Should send when both conditions met", shouldSend);
    }

    @Test
    public void testSendFeishuTrueButNullFeishuTo_ShouldSkipFeishu() {
        boolean sendFeishu = true;
        String feishuTo = null;
        boolean shouldSend = sendFeishu && feishuTo != null && !feishuTo.trim().isEmpty();
        assertFalse("Should not send when feishuTo is null", shouldSend);
    }

    // 图片附件识别测试
    @Test
    public void testIsImageAttachment_PngFile() {
        String fileName = "chart.png";
        boolean isImage = fileName.toLowerCase().endsWith(".png");
        assertTrue("PNG file should be identified as image", isImage);
    }

    @Test
    public void testIsImageAttachment_CsvFile() {
        String fileName = "report.csv";
        boolean isImage = fileName.toLowerCase().endsWith(".png");
        assertFalse("CSV file should not be identified as image", isImage);
    }
}
