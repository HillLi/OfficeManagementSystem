package com.university.oms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * AI智能审核结果，用于公文AI审查的返回数据
 */
public class AiReviewResult {
    private boolean passed;
    /** 审核发现的问题列表 */
    private List<String> issues = new ArrayList<String>();
    /** 修改建议列表 */
    private List<String> suggestions = new ArrayList<String>();
    /** AI推荐的密级 */
    private String recommendedSecrecyLevel;
    /** 公文质量评分 */
    private double qualityScore;
    /** 与已有公文的最高相似度 */
    private double maxSimilarity;
    /** 提取的关键词 */
    private List<String> keywords = new ArrayList<String>();
    /** 检测到的敏感词 */
    private List<String> sensitiveWords = new ArrayList<String>();

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getRecommendedSecrecyLevel() {
        return recommendedSecrecyLevel;
    }

    public void setRecommendedSecrecyLevel(String recommendedSecrecyLevel) {
        this.recommendedSecrecyLevel = recommendedSecrecyLevel;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public double getMaxSimilarity() {
        return maxSimilarity;
    }

    public void setMaxSimilarity(double maxSimilarity) {
        this.maxSimilarity = maxSimilarity;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getSensitiveWords() {
        return sensitiveWords;
    }

    public void setSensitiveWords(List<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }
}
