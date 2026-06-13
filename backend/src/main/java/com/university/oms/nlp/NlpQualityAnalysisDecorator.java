package com.university.oms.nlp;

import com.university.oms.design.DocumentProcessor;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

import java.util.List;

/**
 * NLP质量分析装饰器，在文档处理链中增加关键词提取、敏感词检测、重复度检测和质量评分
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

        // 1. 使用TextRank提取关键词
        List<String> keywords = NlpUtils.extractKeywords(title + " " + content, KEYWORD_TOP_N);
        result.setKeywords(keywords);

        // 2. 敏感词检测
        List<String> sensitiveWords = SensitiveWordDetector.detect(content);
        result.setSensitiveWords(sensitiveWords);
        if (!sensitiveWords.isEmpty()) {
            result.getSuggestions().add("文档包含敏感词：" + String.join("、", sensitiveWords) + "，建议审慎处理。");
        }

        // 3. 通过TF-IDF余弦相似度进行重复检测
        DocumentSimilarityService.SimilarityResult simResult = similarityService.findMostSimilar(content, document.getId());
        if (simResult != null) {
            result.setMaxSimilarity(simResult.getScore());
            if (simResult.getScore() >= SIMILARITY_THRESHOLD) {
                result.getIssues().add("与已有公文《" + simResult.getTitle() + "》相似度达"
                        + String.format("%.0f%%", simResult.getScore() * 100) + "，可能存在重复。");
            }
        }

        // 4. 计算质量评分（0-100分）
        double score = computeQualityScore(document, keywords, sensitiveWords, result.getMaxSimilarity());
        result.setQualityScore(score);

        return result;
    }

    /**
     * 多维度质量评分：
     * - 标题格式合规性（20分）
     * - 内容长度充分性（25分）
     * - 关键词丰富度（25分）
     * - 敏感词扣分（最多扣30分）
     * - 相似度扣分（最多扣20分）
     */
    private double computeQualityScore(Document doc, List<String> keywords,
                                       List<String> sensitiveWords, double maxSimilarity) {
        double score = 100.0;
        String title = doc.getTitle() != null ? doc.getTitle() : "";
        String content = doc.getContent() != null ? doc.getContent() : "";

        // 标题格式：应以"关于"开头（20分）
        if (!title.startsWith("关于")) {
            score -= 20;
        }

        // 内容长度（25分）
        if (content.length() < 30) {
            score -= 25;
        } else if (content.length() < 100) {
            score -= 15;
        } else if (content.length() < 300) {
            score -= 5;
        }

        // 关键词密度：至少3个有意义的关键词为佳（25分）
        if (keywords.size() < 3) {
            score -= 25;
        } else if (keywords.size() < 5) {
            score -= 10;
        }

        // 敏感词扣分：每个敏感词扣10分，最多扣30分
        score -= Math.min(30, sensitiveWords.size() * 10);

        // 相似度扣分
        if (maxSimilarity > 0.7) {
            score -= 20;
        } else if (maxSimilarity > 0.5) {
            score -= 10;
        }

        return Math.max(0, score);
    }
}
