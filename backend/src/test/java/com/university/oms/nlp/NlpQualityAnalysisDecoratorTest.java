package com.university.oms.nlp;

import com.university.oms.design.AiReviewDecorator;
import com.university.oms.design.BaseDocumentProcessor;
import com.university.oms.design.DocumentProcessor;
import com.university.oms.design.SecrecyCheckDecorator;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NlpQualityAnalysisDecoratorTest {

    private DocumentProcessor chain;

    /**
     * Stub similarity service that returns null (no similar documents).
     */
    private static class NoDocSimilarityService extends DocumentSimilarityService {
        NoDocSimilarityService() {
            super(null);
        }

        @Override
        public SimilarityResult findMostSimilar(String content, Long excludeId) {
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        DocumentSimilarityService similarityService = new NoDocSimilarityService();
        chain = new NlpQualityAnalysisDecorator(
                new AiReviewDecorator(
                        new SecrecyCheckDecorator(
                                new BaseDocumentProcessor())),
                similarityService);
    }

    @Test
    void extractsKeywords() {
        Document doc = createDocument("关于开展学术研讨会的通知", "各学院应积极推进教学改革，优化课程设置，提升教学水平。"
                + "加强师资队伍建设，完善教学评估体系。");
        AiReviewResult result = chain.process(doc);
        assertFalse(result.getKeywords().isEmpty(), "Should extract keywords from content");
    }

    @Test
    void detectsSensitiveWords() {
        Document doc = createDocument("关于保密工作的规定", "本规定涉及国家秘密和涉密文件的管理，严禁泄露商业秘密。");
        AiReviewResult result = chain.process(doc);
        assertFalse(result.getSensitiveWords().isEmpty(), "Should detect sensitive words");
        assertTrue(result.getSensitiveWords().stream().anyMatch(w -> w.contains("国家秘密")));
    }

    @Test
    void computesQualityScoreForGoodDocument() {
        Document doc = createDocument("关于教学改革的通知", "为了提高教学质量，各学院应积极推进课程改革，加强师资队伍建设，"
                + "完善教学评估体系，优化教学资源配置，提升人才培养质量。");
        AiReviewResult result = chain.process(doc);
        assertTrue(result.getQualityScore() > 50,
                "Good document should have score > 50, got " + result.getQualityScore());
    }

    @Test
    void computesQualityScoreForPoorDocument() {
        Document doc = createDocument("通知", "短内容");
        AiReviewResult result = chain.process(doc);
        assertTrue(result.getQualityScore() < 60,
                "Poor document should have low score, got " + result.getQualityScore());
    }

    @Test
    void maxSimilarityIsZeroWhenNoSimilarDocs() {
        Document doc = createDocument("关于科研项目的申请", "请各单位积极申报科研项目，加强科研团队建设。");
        AiReviewResult result = chain.process(doc);
        assertEquals(0.0, result.getMaxSimilarity(), 0.001);
    }

    @Test
    void preservesBaseChainBehavior() {
        Document doc = createDocument("...", "短");
        AiReviewResult result = chain.process(doc);
        // Base processor should still flag short title
        assertFalse(result.getIssues().isEmpty());
        assertFalse(result.isPassed());
    }

    @Test
    void secrecyCheckStillBlocksRestrictedDocs() {
        Document doc = createDocument("关于机密事项的通知", "这是一份机密文件的内容。");
        doc.setSecrecyLevel("机密");
        AiReviewResult result = chain.process(doc);
        assertFalse(result.isPassed());
        assertTrue(result.getIssues().stream().anyMatch(i -> i.contains("涉密")));
    }

    private Document createDocument(String title, String content) {
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setDocType("通知");
        doc.setDocNo("校发〔2026〕1号");
        doc.setSecrecyLevel("公开");
        return doc;
    }
}
