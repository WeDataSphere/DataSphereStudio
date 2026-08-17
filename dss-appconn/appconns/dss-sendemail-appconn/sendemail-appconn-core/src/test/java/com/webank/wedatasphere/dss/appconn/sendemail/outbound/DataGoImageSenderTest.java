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

package com.webank.wedatasphere.dss.appconn.sendemail.outbound;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Pure-logic unit tests for the DataGo image outbound flow, mirroring the prior
 * FeishuMessageSenderTest style (no HTTP / no Scala-object invocation).
 *
 * Covers: text fallback, receiver parsing, image attachment detection, image size,
 * task status judgment, JSON escaping, and the sendFeishu control flag.
 */
public class DataGoImageSenderTest {

    private static final int IMAGE_MAXSIZE = 10485760; // 10MB
    private static final Set<String> TERMINAL = new HashSet<>(
        Arrays.asList("exported", "detected_fail", "detect_error", "export_failed"));
    private static final Set<String> FAILED_TERMINAL = new HashSet<>(
        Arrays.asList("detected_fail", "detect_error", "export_failed"));

    // ---- helpers mirroring production logic ----

    private static String resolveText(String subject) {
        return (subject != null && subject.trim().length() > 0) ? subject : "DSS Email Notification";
    }

    private static String[] parseReceivers(String feishuTo) {
        if (feishuTo == null || feishuTo.trim().isEmpty()) {
            return new String[0];
        }
        String[] parts = feishuTo.split(";");
        int n = 0;
        for (String p : parts) {
            if (p.trim().length() > 0) {
                n++;
            }
        }
        String[] out = new String[n];
        int i = 0;
        for (String p : parts) {
            String t = p.trim();
            if (t.length() > 0) {
                out[i++] = t;
            }
        }
        return out;
    }

    private static String toJsonArray(String[] receivers) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < receivers.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escapeJson(receivers[i])).append("\"");
        }
        return sb.append("]").toString();
    }

    private static boolean isImageAttachment(String name, String mediaType) {
        if (name != null && name.toLowerCase().endsWith(".png")) {
            return true;
        }
        return mediaType != null && mediaType.toLowerCase().startsWith("image/");
    }

    private static boolean sizeExceeds(long size) {
        return size > IMAGE_MAXSIZE;
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static boolean isSuccess(String s) {
        return "exported".equals(norm(s));
    }

    private static boolean isTerminal(String s) {
        return TERMINAL.contains(norm(s));
    }

    private static boolean isFailedTerminal(String s) {
        return FAILED_TERMINAL.contains(norm(s));
    }

    private static String escapeJson(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    // ---- text fallback ----

    @Test
    public void testResolveText_nullSubject_usesDefault() {
        assertEquals("DSS Email Notification", resolveText(null));
    }

    @Test
    public void testResolveText_blankSubject_usesDefault() {
        assertEquals("DSS Email Notification", resolveText("   "));
    }

    @Test
    public void testResolveText_nonBlankSubject_usesOriginal() {
        assertEquals("2026年7月审计报表", resolveText("2026年7月审计报表"));
    }

    // ---- receiver parsing ----

    @Test
    public void testParseReceivers_null_returnsEmpty() {
        assertEquals(0, parseReceivers(null).length);
    }

    @Test
    public void testParseReceivers_empty_returnsEmpty() {
        assertEquals(0, parseReceivers("").length);
    }

    @Test
    public void testParseReceivers_whitespaceOnly_returnsEmpty() {
        assertEquals(0, parseReceivers("   ").length);
    }

    @Test
    public void testParseReceivers_multipleSemicolonSplit() {
        String[] r = parseReceivers("zhangsan;lisi;wangwu");
        assertEquals(3, r.length);
        assertEquals("zhangsan", r[0]);
        assertEquals("lisi", r[1]);
        assertEquals("wangwu", r[2]);
    }

    @Test
    public void testParseReceivers_onlySeparatorsAndSpaces_returnsEmpty() {
        assertEquals(0, parseReceivers("  ;  ;  ").length);
    }

    @Test
    public void testParseReceivers_trimsEach() {
        String[] r = parseReceivers("  zhangsan  ;  lisi  ");
        assertEquals(2, r.length);
        assertEquals("zhangsan", r[0]);
        assertEquals("lisi", r[1]);
    }

    @Test
    public void testToJsonArray_single() {
        assertEquals("[\"burdezhang\"]", toJsonArray(new String[]{"burdezhang"}));
    }

    @Test
    public void testToJsonArray_multiple() {
        assertEquals("[\"zhangsan\",\"lisi\",\"wangwu\"]",
            toJsonArray(new String[]{"zhangsan", "lisi", "wangwu"}));
    }

    // ---- image attachment detection ----

    @Test
    public void testIsImageAttachment_pngName() {
        assertTrue(isImageAttachment("chart.png", null));
    }

    @Test
    public void testIsImageAttachment_imageMediaType() {
        assertTrue(isImageAttachment("photo", "image/jpeg"));
    }

    @Test
    public void testIsImageAttachment_csvRejected() {
        assertFalse(isImageAttachment("report.csv", "text/csv"));
    }

    @Test
    public void testIsImageAttachment_pdfRejected() {
        assertFalse(isImageAttachment("summary.pdf", "application/pdf"));
    }

    // ---- image size ----

    @Test
    public void testSizeExceeds_withinLimit_passes() {
        assertFalse(sizeExceeds(1024L));
    }

    @Test
    public void testSizeExceeds_overLimit_rejected() {
        assertTrue(sizeExceeds(IMAGE_MAXSIZE + 1L));
    }

    @Test
    public void testSizeExceeds_boundary_passes() {
        assertFalse(sizeExceeds(IMAGE_MAXSIZE));
    }

    // ---- task status judgment ----

    @Test
    public void testIsSuccess_exportedAndCase() {
        assertTrue(isSuccess("exported"));
        assertTrue(isSuccess("EXPORTED"));
        assertTrue(isSuccess("  exported "));
    }

    @Test
    public void testIsTerminal_terminalSet() {
        assertTrue(isTerminal("exported"));
        assertTrue(isTerminal("detected_fail"));
        assertTrue(isTerminal("detect_error"));
        assertTrue(isTerminal("export_failed"));
    }

    @Test
    public void testIsTerminal_nonTerminal_returnsFalse() {
        assertFalse(isTerminal("pending"));
        assertFalse(isTerminal("detecting"));
        assertFalse(isTerminal("detected_pass"));
    }

    @Test
    public void testIsFailedTerminal_failedOnly() {
        assertTrue(isFailedTerminal("detected_fail"));
        assertTrue(isFailedTerminal("detect_error"));
        assertTrue(isFailedTerminal("export_failed"));
        assertFalse(isFailedTerminal("exported"));
    }

    @Test
    public void testStatus_nullAndUnknown_doNotCrash() {
        assertFalse(isTerminal(null));
        assertFalse(isSuccess(null));
        assertFalse(isFailedTerminal(null));
        assertFalse(isTerminal("foobar"));
    }

    // ---- JSON escaping ----

    @Test
    public void testEscapeJson_null_returnsEmpty() {
        assertEquals("", escapeJson(null));
    }

    @Test
    public void testEscapeJson_plain_unchanged() {
        assertEquals("burdezhang", escapeJson("burdezhang"));
    }

    @Test
    public void testEscapeJson_doubleQuote() {
        assertEquals("a\\\"b", escapeJson("a\"b"));
    }

    @Test
    public void testEscapeJson_newline() {
        assertEquals("a\\nb", escapeJson("a\nb"));
    }

    @Test
    public void testEscapeJson_backslash() {
        assertEquals("a\\\\b", escapeJson("a\\b"));
    }

    // ---- sendFeishu control flag ----

    private static boolean shouldSend(String sendFeishu, String feishuTo) {
        return "true".equalsIgnoreCase(sendFeishu)
            && feishuTo != null && !feishuTo.trim().isEmpty();
    }

    @Test
    public void testShouldSend_false_skips() {
        assertFalse(shouldSend("false", "zhangsan"));
    }

    @Test
    public void testShouldSend_trueButEmptyFeishuTo_skips() {
        assertFalse(shouldSend("true", ""));
    }

    @Test
    public void testShouldSend_trueButNullFeishuTo_skips() {
        assertFalse(shouldSend("true", null));
    }

    @Test
    public void testShouldSend_trueAndValidFeishuTo_sends() {
        assertTrue(shouldSend("true", "zhangsan"));
    }

    // ---- loginUser (dss_user_name) = executeUser, fallback submitUser ----

    private static String resolveLoginUser(String executeUser, String submitUser) {
        if (executeUser != null && executeUser.length() > 0) {
            return executeUser;
        }
        return submitUser == null ? "" : submitUser;
    }

    @Test
    public void testResolveLoginUser_executeUserPresent_usesExecuteUser() {
        assertEquals("v_sunpengwang", resolveLoginUser("v_sunpengwang", "burdezhang"));
    }

    @Test
    public void testResolveLoginUser_executeUserEmpty_fallsBackToSubmitUser() {
        assertEquals("burdezhang", resolveLoginUser("", "burdezhang"));
    }

    @Test
    public void testResolveLoginUser_executeUserNull_fallsBackToSubmitUser() {
        assertEquals("burdezhang", resolveLoginUser(null, "burdezhang"));
    }

    @Test
    public void testResolveLoginUser_bothEmpty_returnsEmpty() {
        assertEquals("", resolveLoginUser("", ""));
    }

    @Test
    public void testResolveLoginUser_bothNull_returnsEmpty() {
        assertEquals("", resolveLoginUser(null, null));
    }

    // ---- base64 cleaning (data-URI prefix + CRLF/whitespace stripping) ----

    private static String cleanBase64(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.startsWith("data:")) {
            String marker = ";base64,";
            int idx = s.indexOf(marker);
            if (idx >= 0) {
                s = s.substring(idx + marker.length());
            } else if (s.indexOf(',') >= 0) {
                s = s.substring(s.indexOf(',') + 1);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Test
    public void testCleanBase64_dataUriPrefixStripped() {
        assertEquals("iVBOR==", cleanBase64("data:image/png;base64,iVBOR=="));
    }

    @Test
    public void testCleanBase64_crlfStripped() {
        assertEquals("iVBORw0KGgoAAAANSUhEUg==",
            cleanBase64("iVBORw0KGgoAAAANS\r\nUhEUg=="));
    }

    @Test
    public void testCleanBase64_cleanUnchanged() {
        assertEquals("iVBORw0KGgoAAAANSUhEUg==", cleanBase64("iVBORw0KGgoAAAANSUhEUg=="));
    }
}
