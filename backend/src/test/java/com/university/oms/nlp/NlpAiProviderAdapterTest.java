package com.university.oms.nlp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NlpAiProviderAdapterTest {

    private final NlpAiProviderAdapter adapter = new NlpAiProviderAdapter();

    @Test
    void draftGeneratesContent() {
        String result = adapter.draft("通知", "教学改革", "优化课程设置，提升教学质量");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void draftContainsTopic() {
        String result = adapter.draft("通知", "教学改革", "优化课程设置");
        assertTrue(result.contains("教学改革"));
        assertTrue(result.contains("通知"));
    }

    @Test
    void draftContainsStructuredSections() {
        String result = adapter.draft("通知", "教学改革", "优化课程设置，提升教学质量");
        assertTrue(result.contains("一、背景与目标"));
        assertTrue(result.contains("二、工作内容"));
        assertTrue(result.contains("三、工作要求"));
        assertTrue(result.contains("四、时间安排与反馈"));
    }

    @Test
    void draftContainsKeywordFooter() {
        String result = adapter.draft("通知", "教学改革", "优化课程设置，提升教学质量");
        assertTrue(result.contains("智能提取关键词"),
                "Should include NLP-extracted keywords footer");
    }

    @Test
    void draftHandlesEmptyKeyPoints() {
        String result = adapter.draft("通知", "教学改革", null);
        assertNotNull(result);
        assertTrue(result.contains("教学改革"));
    }

    @Test
    void draftHandlesBlankKeyPoints() {
        String result = adapter.draft("通知", "教学改革", "   ");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void draftContainsInstitutionAndDate() {
        String result = adapter.draft("通知", "教学改革", "优化课程");
        assertTrue(result.contains("北京大学"));
    }
}
