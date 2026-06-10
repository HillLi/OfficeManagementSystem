package com.university.oms.model;

import java.util.ArrayList;
import java.util.List;

public class AiReviewResult {
    private boolean passed;
    private List<String> issues = new ArrayList<String>();
    private List<String> suggestions = new ArrayList<String>();
    private String recommendedSecrecyLevel;
    private double qualityScore;
    private double maxSimilarity;
    private List<String> keywords = new ArrayList<String>();
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
