package com.university.oms.nlp;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordDetectorTest {

    @Test
    void detectFindsSecrecyWords() {
        List<String> matches = SensitiveWordDetector.detect("本文件涉及国家秘密和涉密文件的处理规定");
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.contains("国家秘密")));
        assertTrue(matches.stream().anyMatch(m -> m.contains("涉密文件")));
    }

    @Test
    void detectFindsPoliticalWords() {
        List<String> matches = SensitiveWordDetector.detect("注意防范政治敏感内容的传播");
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.contains("政治敏感")));
    }

    @Test
    void detectFindsFinancialWords() {
        List<String> matches = SensitiveWordDetector.detect("内部财务机密不得外传，招标底价需要保密");
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.contains("财务机密")));
        assertTrue(matches.stream().anyMatch(m -> m.contains("招标底价")));
    }

    @Test
    void detectFindsPrivacyWords() {
        List<String> matches = SensitiveWordDetector.detect("请提供身份证号和银行卡号用于登记");
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.contains("身份证号")));
        assertTrue(matches.stream().anyMatch(m -> m.contains("银行卡号")));
    }

    @Test
    void detectReturnsEmptyForCleanText() {
        List<String> matches = SensitiveWordDetector.detect("关于开展学术研讨会的通知，请各单位按时参加。");
        assertTrue(matches.isEmpty());
    }

    @Test
    void detectHandlesNullAndEmpty() {
        assertTrue(SensitiveWordDetector.detect(null).isEmpty());
        assertTrue(SensitiveWordDetector.detect("").isEmpty());
        assertTrue(SensitiveWordDetector.detect("   ").isEmpty());
    }

    @Test
    void detectIncludesCategory() {
        List<String> matches = SensitiveWordDetector.detect("包含国家秘密的文件");
        for (String match : matches) {
            assertTrue(match.contains("(") && match.contains(")"),
                    "Match should include category in parentheses: " + match);
        }
    }

    @Test
    void detectCategoriesReturnsCorrectCategories() {
        Set<String> categories = SensitiveWordDetector.detectCategories("国家秘密和个人隐私");
        assertEquals(2, categories.size());
        assertTrue(categories.contains("涉密"));
        assertTrue(categories.contains("隐私"));
    }

    @Test
    void detectCategoriesEmptyForCleanText() {
        Set<String> categories = SensitiveWordDetector.detectCategories("正常的工作通知");
        assertTrue(categories.isEmpty());
    }
}
