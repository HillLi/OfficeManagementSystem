package com.university.oms.nlp;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NlpUtilsTest {

    @Test
    void tokenizeReturnsFilteredWords() {
        List<String> tokens = NlpUtils.tokenize("关于开展学术研讨会的通知");
        assertNotNull(tokens);
        // should extract meaningful words, not just single chars or stop words
        for (String token : tokens) {
            assertTrue(token.length() >= 2, "Token should be at least 2 chars: " + token);
        }
    }

    @Test
    void tokenizeHandlesNull() {
        assertTrue(NlpUtils.tokenize(null).isEmpty());
        assertTrue(NlpUtils.tokenize("").isEmpty());
        assertTrue(NlpUtils.tokenize("   ").isEmpty());
    }

    @Test
    void tokenizeFiltersStopWords() {
        List<String> tokens = NlpUtils.tokenize("这是一个关于学术的通知");
        assertFalse(tokens.contains("的"));
        assertFalse(tokens.contains("是"));
        assertFalse(tokens.contains("这"));
        assertFalse(tokens.contains("一个"));
    }

    @Test
    void extractKeywordsReturnsTopN() {
        String text = "为了规范教学管理，提高教学质量，加强师资队伍建设，完善教学评估体系，"
                + "各学院应积极推进教学改革，优化课程设置，提升教学水平。";
        List<String> keywords = NlpUtils.extractKeywords(text, 5);
        assertNotNull(keywords);
        assertTrue(keywords.size() <= 5);
        assertFalse(keywords.isEmpty());
    }

    @Test
    void extractKeywordsHandlesEmpty() {
        assertTrue(NlpUtils.extractKeywords(null, 5).isEmpty());
        assertTrue(NlpUtils.extractKeywords("", 5).isEmpty());
    }

    @Test
    void computeTfIdfReturnsVector() {
        List<String> corpus = Arrays.asList(
                "学术研究是大学的重要任务",
                "教学质量需要不断提高",
                "学术交流促进科研发展"
        );
        Map<String, Double> vector = NlpUtils.computeTfIdf("学术研究和教学发展", corpus);
        assertFalse(vector.isEmpty());
        // "学术" appears in 2 of 3 docs, should have lower IDF than unique words
        assertTrue(vector.containsKey("学术"));
    }

    @Test
    void computeTfIdfHandlesEmptyCorpus() {
        Map<String, Double> vector = NlpUtils.computeTfIdf("测试文本", Collections.<String>emptyList());
        assertFalse(vector.isEmpty());
    }

    @Test
    void computeTfIdfHandlesEmptyText() {
        Map<String, Double> vector = NlpUtils.computeTfIdf("", Arrays.asList("语料"));
        assertTrue(vector.isEmpty());
    }

    @Test
    void cosineSimilarityIdenticalVectors() {
        Map<String, Double> v = new HashMap<String, Double>();
        v.put("学术", 2.0);
        v.put("研究", 1.5);
        assertEquals(1.0, NlpUtils.cosineSimilarity(v, v), 0.001);
    }

    @Test
    void cosineSimilarityOrthogonalVectors() {
        Map<String, Double> v1 = new HashMap<String, Double>();
        v1.put("学术", 1.0);
        Map<String, Double> v2 = new HashMap<String, Double>();
        v2.put("体育", 1.0);
        assertEquals(0.0, NlpUtils.cosineSimilarity(v1, v2), 0.001);
    }

    @Test
    void cosineSimilarityEmptyVectors() {
        assertEquals(0.0, NlpUtils.cosineSimilarity(new HashMap<String, Double>(), new HashMap<String, Double>()), 0.001);
    }

    @Test
    void cosineSimilarityPartialOverlap() {
        Map<String, Double> v1 = new HashMap<String, Double>();
        v1.put("学术", 1.0);
        v1.put("研究", 1.0);
        Map<String, Double> v2 = new HashMap<String, Double>();
        v2.put("学术", 1.0);
        v2.put("教学", 1.0);
        double sim = NlpUtils.cosineSimilarity(v1, v2);
        assertTrue(sim > 0 && sim < 1.0, "Partial overlap should be between 0 and 1, got " + sim);
    }
}
