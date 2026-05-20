package com.university.oms.model;

import java.util.ArrayList;
import java.util.List;

public class AiReviewResult {
    private boolean passed;
    private List<String> issues = new ArrayList<String>();
    private List<String> suggestions = new ArrayList<String>();
    private String recommendedSecrecyLevel;

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
}
