package com.university.oms.nlp;

import com.university.oms.design.DocumentProcessor;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

import java.util.List;

/**
 * Decorator that adds NLP-based quality analysis to the document processing chain.
 * Inserts before AiReviewDecorator in the chain.
 */
public class NlpQualityAnalysisDecorator implements DocumentProcessor {

    private static final int KEYWORD_TOP_N = 10;
    private static final double SIMILARITY_THRESHOLD = 0.8;

    private final DocumentProcessor wrapped;
    private final DocumentSimilarityService similarityService;

    public NlpQualityAnalysisDecorator(DocumentProcessor wrapped, DocumentSimilarityService similarityService) {
        this.wrapped = wrapped;
        this.similarityService = similarityService;
    }

    @Override
    public AiReviewResult process(Document document) {
        AiReviewResult result = wrapped.process(document);
        String content = document.getContent() != null ? document.getContent() : "";
        String title = document.getTitle() != null ? document.getTitle() : "";

        // 1. Extract keywords using TextRank
        List<String> keywords = NlpUtils.extractKeywords(title + " " + content, KEYWORD_TOP_N);
        result.setKeywords(keywords);

        // 2. Detect sensitive words
        List<String> sensitiveWords = SensitiveWordDetector.detect(content);
        result.setSensitiveWords(sensitiveWords);
        if (!sensitiveWords.isEmpty()) {
            result.getSuggestions().add("文档包含敏感词：" + String.join("、", sensitiveWords) + "，建议审慎处理。");
        }

        // 3. Duplicate detection via TF-IDF cosine similarity
        DocumentSimilarityService.SimilarityResult simResult = similarityService.findMostSimilar(content, document.getId());
        if (simResult != null) {
            result.setMaxSimilarity(simResult.getScore());
            if (simResult.getScore() >= SIMILARITY_THRESHOLD) {
                result.getIssues().add("与已有公文《" + simResult.getTitle() + "》相似度达"
                        + String.format("%.0f%%", simResult.getScore() * 100) + "，可能存在重复。");
            }
        }

        // 4. Compute quality score (0-100)
        double score = computeQualityScore(document, keywords, sensitiveWords, result.getMaxSimilarity());
        result.setQualityScore(score);

        return result;
    }

    /**
     * Quality scoring based on multiple dimensions:
     * - Title format compliance (20 points)
     * - Content length adequacy (25 points)
     * - Keyword density (25 points)
     * - Sensitive word penalty (30 points max deduction)
     * - Similarity penalty (up to 20 points deduction)
     */
    private double computeQualityScore(Document doc, List<String> keywords,
                                       List<String> sensitiveWords, double maxSimilarity) {
        double score = 100.0;
        String title = doc.getTitle() != null ? doc.getTitle() : "";
        String content = doc.getContent() != null ? doc.getContent() : "";

        // Title format: should start with "关于" (20 points)
        if (!title.startsWith("关于")) {
            score -= 20;
        }

        // Content length (25 points)
        if (content.length() < 30) {
            score -= 25;
        } else if (content.length() < 100) {
            score -= 15;
        } else if (content.length() < 300) {
            score -= 5;
        }

        // Keyword density: at least 3 meaningful keywords is good (25 points)
        if (keywords.size() < 3) {
            score -= 25;
        } else if (keywords.size() < 5) {
            score -= 10;
        }

        // Sensitive word penalty: 10 points per sensitive word, max 30
        score -= Math.min(30, sensitiveWords.size() * 10);

        // Similarity penalty
        if (maxSimilarity > 0.7) {
            score -= 20;
        } else if (maxSimilarity > 0.5) {
            score -= 10;
        }

        return Math.max(0, score);
    }
}
